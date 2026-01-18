package com.example.mainservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Email service for sending password reset emails.
 * Currently logs the reset link for development purposes.
 * To enable actual email sending, configure Spring Mail in application.properties
 * and uncomment the email sending code below.
 */
@Slf4j
@Service
public class EmailService {

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Get the password reset link
     * @param resetToken The password reset token
     * @param role The user role (DOCTOR, PATIENT, ADMIN) to determine redirect destination
     */
    public String getResetLink(String resetToken, String role) {
        return frontendUrl + "/reset-password?token=" + resetToken + "&role=" + role.toUpperCase();
    }

    /**
     * Send password reset email to user
     * @param email User's email address
     * @param resetToken Password reset token
     * @param username User's username
     * @param role User's role (DOCTOR, PATIENT, ADMIN)
     */
    public void sendPasswordResetEmail(String email, String resetToken, String username, String role) {
        String resetLink = getResetLink(resetToken, role);
        
        String subject = "Password Reset Request";
        String body = buildPasswordResetEmailBody(username, resetLink, resetToken);
        
        // Log for development (remove in production)
        log.info("=== PASSWORD RESET EMAIL ===");
        log.info("To: {}", email);
        log.info("Subject: {}", subject);
        log.info("Reset Link: {}", resetLink);
        log.info("Reset Token: {}", resetToken);
        log.info("Body:\n{}", body);
        log.info("===========================");
        
        // TODO: Uncomment below to send actual email
        // Uncomment when Spring Mail is configured:
        /*
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
            log.info("Password reset email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
        */
    }

    private String buildPasswordResetEmailBody(String username, String resetLink, String resetToken) {
        return String.format(
            "Hello %s,\n\n" +
            "You have requested to reset your password.\n\n" +
            "Click the following link to reset your password:\n" +
            "%s\n\n" +
            "Or use this token: %s\n\n" +
            "This link will expire in 1 hour.\n\n" +
            "If you did not request this password reset, please ignore this email.\n\n" +
            "Best regards,\n" +
            "Healthcare System",
            username, resetLink, resetToken
        );
    }
}
