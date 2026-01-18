package com.example.mainservice.service;

import com.example.mainservice.dto.AuthResponse;
import com.example.mainservice.dto.ForgotPasswordRequest;
import com.example.mainservice.dto.ForgotPasswordResponse;
import com.example.mainservice.dto.LoginRequest;
import com.example.mainservice.dto.ResetPasswordRequest;
import com.example.mainservice.dto.SignupRequest;
import com.example.mainservice.entity.Admin;
import com.example.mainservice.entity.Doctor;
import com.example.mainservice.entity.PasswordResetToken;
import com.example.mainservice.entity.Patient;
import com.example.mainservice.repository.AdminRepo;
import com.example.mainservice.repository.DoctorRepo;
import com.example.mainservice.repository.PasswordResetTokenRepository;
import com.example.mainservice.repository.PatientRepo;
import com.example.mainservice.security.CustomUserDetails;
import com.example.mainservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public AuthResponse login(LoginRequest loginRequest) {
        // Validate role is provided
        if (loginRequest.getRole() == null || loginRequest.getRole().trim().isEmpty()) {
            throw new RuntimeException("Role is required for login");
        }

        String requestedRole = loginRequest.getRole().toUpperCase().trim();
        
        // Check if user exists before attempting authentication
        boolean userExists = false;
        String actualRole = null;
        
        if ("PATIENT".equals(requestedRole)) {
            userExists = patientRepo.findByUsername(loginRequest.getUsername()).isPresent();
            if (userExists) actualRole = "PATIENT";
        } else if ("DOCTOR".equals(requestedRole)) {
            userExists = doctorRepo.findByUsername(loginRequest.getUsername()).isPresent();
            if (userExists) actualRole = "DOCTOR";
        } else if ("ADMIN".equals(requestedRole)) {
            userExists = adminRepo.findByUsername(loginRequest.getUsername()).isPresent();
            if (userExists) actualRole = "ADMIN";
        }
        
        if (!userExists) {
            throw new RuntimeException("No " + requestedRole + " account found with username: " + loginRequest.getUsername());
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
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

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1); // Token expires in 1 hour

        // Save reset token
        PasswordResetToken token = PasswordResetToken.builder()
                .token(resetToken)
                .username(username)
                .role(role)
                .expiryDate(expiryDate)
                .used(false)
                .build();

        passwordResetTokenRepository.save(token);

        // Send reset email (logs to console in development)
        emailService.sendPasswordResetEmail(email, resetToken, username);

        // Build reset link
        String resetLink = emailService.getResetLink(resetToken);

        // Return response with token and link for development use
        // In production, remove resetToken and resetLink from response
        return ForgotPasswordResponse.builder()
                .message("Password reset link has been sent to your email. Please check your inbox.")
                .resetToken(resetToken) // For development - remove in production
                .resetLink(resetLink)   // For development - remove in production
                .build();
    }

    /**
     * Reset password using reset token
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String token = request.getToken().trim();
        String newPassword = request.getNewPassword();

        // Find token
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(token);
        
        if (tokenOptional.isEmpty()) {
            throw new RuntimeException("Invalid or expired reset token");
        }

        PasswordResetToken resetToken = tokenOptional.get();

        // Check if token is expired
        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new RuntimeException("Reset token has expired. Please request a new password reset.");
        }

        // Check if token is already used
        if (resetToken.isUsed()) {
            throw new RuntimeException("This reset token has already been used. Please request a new password reset.");
        }

        // Update password based on role
        String username = resetToken.getUsername();
        String role = resetToken.getRole();
        String encodedPassword = passwordEncoder.encode(newPassword);

        switch (role) {
            case "PATIENT":
                Patient patient = patientRepo.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Patient not found"));
                patient.setPassword(encodedPassword);
                patientRepo.save(patient);
                break;

            case "DOCTOR":
                Doctor doctor = doctorRepo.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Doctor not found"));
                doctor.setPassword(encodedPassword);
                doctorRepo.save(doctor);
                break;

            case "ADMIN":
                Admin admin = adminRepo.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Admin not found"));
                admin.setPassword(encodedPassword);
                adminRepo.save(admin);
                break;

            default:
                throw new RuntimeException("Invalid role");
        }

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}
