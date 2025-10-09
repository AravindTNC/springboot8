package com.example.TaskNew8.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // EMAIL VERIFICATION
    public void sendVerificationEmail(String toEmail, String verificationToken, String baseUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verify Your Email - TaskNew8");
            
            String verificationUrl = baseUrl + "/auth/verify-email?token=" + verificationToken;
            
            String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #2196F3;">Welcome to TaskNew8!</h2>
                        <p>Hello,</p>
                        <p>Thank you for registering with TaskNew8. Please verify your email address to complete your registration.</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" 
                               style="background-color: #2196F3; color: white; padding: 12px 30px; 
                                      text-decoration: none; border-radius: 5px; display: inline-block;">
                                Verify Email Address
                            </a>
                        </div>
                        <p style="color: #666; font-size: 14px;">
                            Or copy and paste this link into your browser:<br>
                            <a href="%s">%s</a>
                        </p>
                        <p style="color: #666; font-size: 14px;">
                            If you did not create an account, please ignore this email.
                        </p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                        <p style="color: #999; font-size: 12px;">
                            Best regards,<br>
                            TaskNew8 Team
                        </p>
                    </div>
                </body>
                </html>
                """, verificationUrl, verificationUrl, verificationUrl);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Verification email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email. Please try again later.");
        }
    }

    // PASSWORD RESET
    public void sendPasswordResetEmail(String toEmail, String resetToken, String baseUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request - TaskNew8");
            
            String resetUrl = baseUrl + "/auth/reset-password?token=" + resetToken;
            
            String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Password Reset Request</h2>
                    <p>Hello,</p>
                    <p>You have requested to reset your password for your TaskNew8 account. Please use the following link to proceed:</p>

                    <p>
                        <a href="%s">%s</a>
                    </p>
                    
                    <p>This link will expire in 1 hour.</p>
                    <p>If you did not request this password reset, please ignore this email.</p>
                    <hr>
                    <p>
                        Best regards,<br>
                        TaskNew8 Team
                    </p>
                </body>
                </html>
                """, resetUrl, resetUrl, resetUrl);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email. Please try again later.");
        }
    }
}