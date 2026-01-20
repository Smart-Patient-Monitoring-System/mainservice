package com.example.mainservice.service;

import com.example.mainservice.dto.AuthResponse;
import com.example.mainservice.dto.ForgotPasswordRequest;
import com.example.mainservice.dto.ForgotPasswordResponse;
import com.example.mainservice.dto.LoginRequest;
import com.example.mainservice.dto.ResetPasswordRequest;
import com.example.mainservice.dto.SignupRequest;
import com.example.mainservice.entity.Admin;
import com.example.mainservice.entity.Doctor;
import com.example.mainservice.entity.LoginOtpSession;
import com.example.mainservice.entity.PasswordResetToken;
import com.example.mainservice.entity.Patient;

import java.util.List;
import com.example.mainservice.repository.AdminRepo;
import com.example.mainservice.repository.DoctorRepo;
import com.example.mainservice.repository.LoginOtpSessionRepository;
import com.example.mainservice.repository.PasswordResetTokenRepository;
import com.example.mainservice.repository.PatientRepo;
import com.example.mainservice.security.CustomUserDetails;
import com.example.mainservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final PatientRepo patientRepo;
    private final DoctorRepo doctorRepo;
    private final AdminRepo adminRepo;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final LoginOtpSessionRepository loginOtpSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public AuthResponse login(LoginRequest loginRequest) {
        // Validate role is provided
        if (loginRequest.getRole() == null || loginRequest.getRole().trim().isEmpty()) {
            throw new RuntimeException("Role is required for login");
        }

        String requestedRole = loginRequest.getRole().toUpperCase().trim();
        String email = loginRequest.getEmail().trim();

        try {
            if ("PATIENT".equals(requestedRole)) {
                return startPatientOtpLogin(email, loginRequest.getPassword());
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            loginRequest.getPassword()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Validate that the user's role matches the requested role
            String userRole = userDetails.getRole().toUpperCase();

            if (!requestedRole.equals(userRole)) {
                throw new RuntimeException("Invalid role. This account is registered as " + userRole + ", not " + requestedRole + ". Please login as " + userRole);
            }

            String token = jwtUtil.generateToken(userDetails, userDetails.getRole());

            return AuthResponse.builder()
                    .token(token)
                    .username(userDetails.getUsername())
                    .email(userDetails.getEmail())
                    .role(userDetails.getRole())
                    .name(userDetails.getDisplayName())
                    .build();
        } catch (org.springframework.security.core.AuthenticationException e) {
            // Keep error responses simple (avoid user enumeration)
            throw new RuntimeException("Invalid email or password");
        }
    }

    @Transactional
    private AuthResponse startPatientOtpLogin(String email, String rawPassword) {
        Optional<Patient> patientOptional = patientRepo.findByEmail(email);
        if (patientOptional.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }
        Patient patient = patientOptional.get();

        if (!passwordEncoder.matches(rawPassword, patient.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Allow only one active OTP session per patient (simple + safe)
        loginOtpSessionRepository.deleteByPatientId(patient.getId());

        String otp = generateOtp6();
        String sessionId = UUID.randomUUID().toString();

        LoginOtpSession session = LoginOtpSession.builder()
                .sessionId(sessionId)
                .patientId(patient.getId())
                .otpHash(passwordEncoder.encode(otp))
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .attempts(0)
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        loginOtpSessionRepository.save(session);
        emailService.sendLoginOtpEmail(patient.getEmail(), otp, patient.getName());

        return AuthResponse.builder()
                .otpRequired(true)
                .loginSessionId(sessionId)
                .username(patient.getUsername())
                .email(patient.getEmail())
                .role("PATIENT")
                .name(patient.getName())
                .build();
    }

    public AuthResponse verifyPatientLoginOtp(String loginSessionId, String otp) {
        LoginOtpSession session = loginOtpSessionRepository.findBySessionId(loginSessionId)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP session"));

        if (session.isUsed()) {
            throw new RuntimeException("OTP already used. Please login again.");
        }
        if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
            throw new RuntimeException("OTP expired. Please login again.");
        }
        if (session.getAttempts() >= 5) {
            throw new RuntimeException("Too many invalid attempts. Please login again.");
        }

        session.setAttempts(session.getAttempts() + 1);

        boolean ok = passwordEncoder.matches(otp, session.getOtpHash());
        if (!ok) {
            loginOtpSessionRepository.save(session);
            throw new RuntimeException("Invalid OTP");
        }

        session.setUsed(true);
        loginOtpSessionRepository.save(session);

        Patient patient = patientRepo.findById(session.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        CustomUserDetails userDetails = new CustomUserDetails(
                patient.getId(),
                patient.getUsername(),
                patient.getPassword(),
                patient.getEmail(),
                patient.getName(),
                "PATIENT"
        );

        String token = jwtUtil.generateToken(userDetails, "PATIENT");

        return AuthResponse.builder()
                .token(token)
                .username(patient.getUsername())
                .email(patient.getEmail())
                .role("PATIENT")
                .name(patient.getName())
                .build();
    }

    private static final SecureRandom OTP_RANDOM = new SecureRandom();

    private String generateOtp6() {
        int code = OTP_RANDOM.nextInt(1_000_000);
        return String.format("%06d", code);
    }

    public AuthResponse signup(SignupRequest signupRequest) {
        // Currently only patient signup is supported via /api/auth/signup.
        // Doctors should be created via /api/doctor/create.
        String role = signupRequest.getRole().toUpperCase();
        if (!"PATIENT".equals(role)) {
            throw new RuntimeException("Only PATIENT signup is supported via this endpoint");
        }

        if (patientRepo.existsByUsername(signupRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        if (patientRepo.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        Patient patient = Patient.builder()
                .name(signupRequest.getName())
                .dateOfBirth(signupRequest.getDateOfBirth())
                .address(signupRequest.getAddress())
                .email(signupRequest.getEmail())
                .nicNo(signupRequest.getNicNo())
                .gender(signupRequest.getGender())
                .contactNo(signupRequest.getContactNo())
                .guardiansName(signupRequest.getGuardianName())
                .guardiansContactNo(signupRequest.getGuardianContactNo())
                .username(signupRequest.getUsername())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .bloodType(signupRequest.getBloodType())
                .guardianRelationship(signupRequest.getGuardianType())
                .allergies(signupRequest.getCurrentAllergies())
                .currentMedications(signupRequest.getCurrentMedications())
                .pastSurgeries(signupRequest.getPastSurgeries())
                .build();

        patient = patientRepo.save(patient);

        CustomUserDetails userDetails = new CustomUserDetails(
                patient.getId(),
                patient.getUsername(),
                patient.getPassword(),
                patient.getEmail(),
                patient.getName(),
                "PATIENT"
        );
        String token = jwtUtil.generateToken(userDetails, "PATIENT");

        return AuthResponse.builder()
                .token(token)
                .username(patient.getUsername())
                .email(patient.getEmail())
                .role("PATIENT")
                .name(patient.getName())
                .build();
    }

    /**
     * Request password reset - generates a token and sends email
     * Returns the reset token and link for development purposes (when email is not configured)
     */
    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        String emailOrUsername = request.getEmailOrUsername().trim();
        String role = request.getRole().toUpperCase();

        // Find user by email or username based on role
        String username = null;
        String email = null;

        switch (role) {
            case "PATIENT":
                Optional<Patient> patientByEmail = patientRepo.findByEmail(emailOrUsername);
                Optional<Patient> patientByUsername = patientRepo.findByUsername(emailOrUsername);
                
                if (patientByEmail.isPresent()) {
                    Patient patient = patientByEmail.get();
                    username = patient.getUsername();
                    email = patient.getEmail();
                } else if (patientByUsername.isPresent()) {
                    Patient patient = patientByUsername.get();
                    username = patient.getUsername();
                    email = patient.getEmail();
                } else {
                    throw new RuntimeException("No patient found with the provided email or username");
                }
                break;

            case "DOCTOR":
                Optional<Doctor> doctorByEmail = doctorRepo.findByEmail(emailOrUsername);
                Optional<Doctor> doctorByUsername = doctorRepo.findByUsername(emailOrUsername);
                
                if (doctorByEmail.isPresent()) {
                    Doctor doctor = doctorByEmail.get();
                    username = doctor.getUsername();
                    email = doctor.getEmail();
                } else if (doctorByUsername.isPresent()) {
                    Doctor doctor = doctorByUsername.get();
                    username = doctor.getUsername();
                    email = doctor.getEmail();
                } else {
                    throw new RuntimeException("No doctor found with the provided email or username");
                }
                break;

            case "ADMIN":
                Optional<Admin> adminByEmail = adminRepo.findByEmail(emailOrUsername);
                Optional<Admin> adminByUsername = adminRepo.findByUsername(emailOrUsername);
                
                if (adminByEmail.isPresent()) {
                    Admin admin = adminByEmail.get();
                    username = admin.getUsername();
                    email = admin.getEmail();
                } else if (adminByUsername.isPresent()) {
                    Admin admin = adminByUsername.get();
                    username = admin.getUsername();
                    email = admin.getEmail();
                } else {
                    throw new RuntimeException("No admin found with the provided email or username");
                }
                break;

            default:
                throw new RuntimeException("Invalid role. Must be DOCTOR, PATIENT, or ADMIN");
        }

        // Delete any existing reset tokens for this user
        passwordResetTokenRepository.deleteByUsernameAndRole(username, role);

        // Generate JWT reset token (expires in 1 hour)
        String resetToken = jwtUtil.generatePasswordResetToken(username, role);
        
        // Calculate expiry date (1 hour from now)
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

        // Save reset token to database for tracking (used flag)
        PasswordResetToken token = PasswordResetToken.builder()
                .token(resetToken)
                .username(username)
                .role(role)
                .expiryDate(expiryDate)
                .used(false)
                .build();

        passwordResetTokenRepository.save(token);

        // Send reset email (logs to console in development)
        emailService.sendPasswordResetEmail(email, resetToken, username, role);

        // Build reset link
        String resetLink = emailService.getResetLink(resetToken, role);

        // For security, do not return resetToken/resetLink to the client.
        return ForgotPasswordResponse.builder()
                .message("Password reset link has been sent to your email. Please check your inbox.")
                .resetToken(null)
                .resetLink(null)
                .build();
    }

    /**
     * Reset password using JWT reset token
     * @return The role of the user whose password was reset (for redirect purposes)
     */
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        String token = request.getToken().trim();
        String currentPassword = request.getCurrentPassword();
        String newPassword = request.getNewPassword();

        // Validate JWT token (checks expiration and signature)
        if (!jwtUtil.validatePasswordResetToken(token)) {
            throw new RuntimeException("Invalid or expired reset token");
        }

        // Extract username and role from JWT token
        String username;
        String role;
        try {
            username = jwtUtil.extractUsername(token);
            role = jwtUtil.extractRole(token);
        } catch (Exception e) {
            throw new RuntimeException("Invalid reset token format");
        }

        // Check if token exists in database and is not already used
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(token);
        
        if (tokenOptional.isPresent()) {
            PasswordResetToken resetToken = tokenOptional.get();
            
            // Check if token is already used
            if (resetToken.isUsed()) {
                throw new RuntimeException("This reset token has already been used. Please request a new password reset.");
            }
            
            // Verify username and role match
            if (!resetToken.getUsername().equals(username) || !resetToken.getRole().equals(role)) {
                throw new RuntimeException("Token does not match user information");
            }
        } else {
            // Token not in database - might be from old system or deleted
            // Still allow if JWT is valid (for backward compatibility)
            // But log a warning
        }

        // Update password based on role (also validate current password)
        String encodedPassword = passwordEncoder.encode(newPassword);

        switch (role) {
            case "PATIENT":
                Patient patient = patientRepo.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Patient not found"));
                if (!passwordEncoder.matches(currentPassword, patient.getPassword())) {
                    throw new RuntimeException("Current password is incorrect");
                }
                patient.setPassword(encodedPassword);
                patientRepo.save(patient);
                break;

            case "DOCTOR":
                Doctor doctor = doctorRepo.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Doctor not found"));
                if (!passwordEncoder.matches(currentPassword, doctor.getPassword())) {
                    throw new RuntimeException("Current password is incorrect");
                }
                doctor.setPassword(encodedPassword);
                doctorRepo.save(doctor);
                break;

            case "ADMIN":
                Admin admin = adminRepo.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Admin not found"));
                if (!passwordEncoder.matches(currentPassword, admin.getPassword())) {
                    throw new RuntimeException("Current password is incorrect");
                }
                admin.setPassword(encodedPassword);
                adminRepo.save(admin);
                break;

            default:
                throw new RuntimeException("Invalid role");
        }

        // Mark token as used in database (if it exists)
        if (tokenOptional.isPresent()) {
            PasswordResetToken resetToken = tokenOptional.get();
            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);
        }
        
        // Return the role so frontend knows where to redirect
        return role;
    }
}
