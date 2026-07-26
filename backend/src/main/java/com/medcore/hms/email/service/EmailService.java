package com.medcore.hms.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Async
    public void sendOtpEmail(String toEmail, String firstName, String otp) {
        String subject = "MedCore HMS – Your Verification Code";
        String body = buildOtpHtml(firstName, otp);
        send(toEmail, subject, body);
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String firstName) {
        String subject = "Welcome to MedCore HMS!";
        String body = buildWelcomeHtml(firstName);
        send(toEmail, subject, body);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String firstName, String otp) {
        String subject = "MedCore HMS – Password Reset Code";
        String body = buildPasswordResetHtml(firstName, otp);
        send(toEmail, subject, body);
    }

    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent to: {} | subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildOtpHtml(String firstName, String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8"/>
                  <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background: #f4f6f9; margin: 0; padding: 0; }
                    .wrapper { max-width: 560px; margin: 40px auto; background: #ffffff; border-radius: 12px;
                               box-shadow: 0 4px 20px rgba(0,0,0,0.08); overflow: hidden; }
                    .header { background: linear-gradient(135deg, #1a73e8, #0d47a1); padding: 32px 40px; }
                    .header h1 { color: #ffffff; margin: 0; font-size: 24px; font-weight: 700; letter-spacing: -0.5px; }
                    .header p  { color: rgba(255,255,255,0.85); margin: 6px 0 0; font-size: 14px; }
                    .body { padding: 36px 40px; }
                    .body p  { color: #374151; font-size: 15px; line-height: 1.6; margin: 0 0 16px; }
                    .otp-box { background: #f0f4ff; border: 2px dashed #1a73e8; border-radius: 10px;
                               text-align: center; padding: 20px; margin: 24px 0; }
                    .otp-box .code { font-size: 42px; font-weight: 800; letter-spacing: 10px;
                                     color: #1a73e8; font-family: monospace; }
                    .otp-box .note { color: #6b7280; font-size: 13px; margin-top: 8px; }
                    .footer { background: #f9fafb; padding: 20px 40px; text-align: center; }
                    .footer p { color: #9ca3af; font-size: 12px; margin: 0; }
                  </style>
                </head>
                <body>
                  <div class="wrapper">
                    <div class="header">
                      <h1>🏥 MedCore HMS</h1>
                      <p>Hospital Management System</p>
                    </div>
                    <div class="body">
                      <p>Hi <strong>%s</strong>,</p>
                      <p>Use the verification code below to confirm your email address.</p>
                      <div class="otp-box">
                        <div class="code">%s</div>
                        <div class="note">Expires in <strong>5 minutes</strong></div>
                      </div>
                      <p>If you didn't request this, you can safely ignore this email.</p>
                    </div>
                    <div class="footer">
                      <p>&copy; 2026 MedCore HMS &nbsp;|&nbsp; This is an automated message, please do not reply.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(firstName, otp);
    }

    private String buildWelcomeHtml(String firstName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8"/>
                  <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background: #f4f6f9; margin: 0; padding: 0; }
                    .wrapper { max-width: 560px; margin: 40px auto; background: #ffffff; border-radius: 12px;
                               box-shadow: 0 4px 20px rgba(0,0,0,0.08); overflow: hidden; }
                    .header { background: linear-gradient(135deg, #10b981, #047857); padding: 32px 40px; }
                    .header h1 { color: #ffffff; margin: 0; font-size: 24px; font-weight: 700; }
                    .header p  { color: rgba(255,255,255,0.85); margin: 6px 0 0; font-size: 14px; }
                    .body { padding: 36px 40px; }
                    .body p  { color: #374151; font-size: 15px; line-height: 1.6; margin: 0 0 16px; }
                    .badge { display: inline-block; background: #d1fae5; color: #065f46; border-radius: 99px;
                             padding: 6px 18px; font-weight: 600; font-size: 14px; }
                    .footer { background: #f9fafb; padding: 20px 40px; text-align: center; }
                    .footer p { color: #9ca3af; font-size: 12px; margin: 0; }
                  </style>
                </head>
                <body>
                  <div class="wrapper">
                    <div class="header">
                      <h1>🏥 MedCore HMS</h1>
                      <p>Hospital Management System</p>
                    </div>
                    <div class="body">
                      <p>Hi <strong>%s</strong>,</p>
                      <p>Welcome to <strong>MedCore HMS</strong>! Your account has been verified and is now active.</p>
                      <p><span class="badge">✅ Email Verified</span></p>
                      <p>You can now log in and access all features available to your role.</p>
                    </div>
                    <div class="footer">
                      <p>&copy; 2026 MedCore HMS &nbsp;|&nbsp; This is an automated message, please do not reply.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(firstName);
    }

    private String buildPasswordResetHtml(String firstName, String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8"/>
                  <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background: #f4f6f9; margin: 0; padding: 0; }
                    .wrapper { max-width: 560px; margin: 40px auto; background: #ffffff; border-radius: 12px;
                               box-shadow: 0 4px 20px rgba(0,0,0,0.08); overflow: hidden; }
                    .header { background: linear-gradient(135deg, #f59e0b, #b45309); padding: 32px 40px; }
                    .header h1 { color: #ffffff; margin: 0; font-size: 24px; font-weight: 700; }
                    .header p  { color: rgba(255,255,255,0.85); margin: 6px 0 0; font-size: 14px; }
                    .body { padding: 36px 40px; }
                    .body p  { color: #374151; font-size: 15px; line-height: 1.6; margin: 0 0 16px; }
                    .otp-box { background: #fffbeb; border: 2px dashed #f59e0b; border-radius: 10px;
                               text-align: center; padding: 20px; margin: 24px 0; }
                    .otp-box .code { font-size: 42px; font-weight: 800; letter-spacing: 10px;
                                     color: #b45309; font-family: monospace; }
                    .otp-box .note { color: #6b7280; font-size: 13px; margin-top: 8px; }
                    .footer { background: #f9fafb; padding: 20px 40px; text-align: center; }
                    .footer p { color: #9ca3af; font-size: 12px; margin: 0; }
                  </style>
                </head>
                <body>
                  <div class="wrapper">
                    <div class="header">
                      <h1>🏥 MedCore HMS</h1>
                      <p>Password Reset Request</p>
                    </div>
                    <div class="body">
                      <p>Hi <strong>%s</strong>,</p>
                      <p>We received a request to reset your password. Use the code below:</p>
                      <div class="otp-box">
                        <div class="code">%s</div>
                        <div class="note">Expires in <strong>5 minutes</strong></div>
                      </div>
                      <p>If you didn't request a password reset, please ignore this email and your account will remain secure.</p>
                    </div>
                    <div class="footer">
                      <p>&copy; 2026 MedCore HMS &nbsp;|&nbsp; This is an automated message, please do not reply.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(firstName, otp);
    }
}
