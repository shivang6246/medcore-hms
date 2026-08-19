package com.medcore.hms.billing.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@medcore.com}")
    private String fromEmail;

    @Async
    public void sendInvoicePdfEmail(String toEmail, String invoiceNumber, String patientName, BigDecimal amountPaid, byte[] pdfBytes) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Cannot send invoice email: Patient has no email address configured.");
            return;
        }

        log.info("Preparing to send invoice PDF email for invoice {} to {}", invoiceNumber, toEmail);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("MedCore HMS - Payment Confirmation & Invoice #" + invoiceNumber);

            String htmlMsg = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;">
                        <div style="background-color: #10b981; color: #ffffff; padding: 20px; text-align: center;">
                            <h1 style="margin: 0; font-size: 24px;">Payment Received!</h1>
                            <p style="margin: 5px 0 0 0; font-size: 14px;">MedCore Health Care System</p>
                        </div>
                        <div style="padding: 25px; color: #333333; line-height: 1.6;">
                            <p>Dear <strong>%s</strong>,</p>
                            <p>Thank you for your payment. Your payment of <strong>$%s</strong> for Invoice <strong>%s</strong> has been successfully processed.</p>
                            <p>We have attached the official PDF invoice receipt to this email for your records.</p>
                            <div style="background-color: #f9fafb; border-left: 4px solid #10b981; padding: 15px; margin: 20px 0;">
                                <p style="margin: 0; font-weight: bold; color: #111827;">Invoice Summary:</p>
                                <p style="margin: 5px 0 0 0;">Invoice Number: <strong>%s</strong></p>
                                <p style="margin: 5px 0 0 0;">Amount Paid: <strong>$%s</strong></p>
                                <p style="margin: 5px 0 0 0;">Status: <span style="color: #10b981; font-weight: bold;">PAID</span></p>
                            </div>
                            <p>If you have any questions or require assistance, feel free to reply to this email or contact support at billing@medcore.com.</p>
                            <p style="margin-top: 30px; font-size: 13px; color: #6b7280;">Best regards,<br><strong>MedCore HMS Team</strong></p>
                        </div>
                        <div style="background-color: #f3f4f6; padding: 15px; text-align: center; font-size: 12px; color: #9ca3af;">
                            This is an automated notification from MedCore Hospital Management System.
                        </div>
                    </div>
                    """.formatted(patientName, amountPaid.setScale(2, java.math.RoundingMode.HALF_UP), invoiceNumber, invoiceNumber, amountPaid.setScale(2, java.math.RoundingMode.HALF_UP));

            helper.setText(htmlMsg, true);

            if (pdfBytes != null && pdfBytes.length > 0) {
                helper.addAttachment("Invoice-" + invoiceNumber + ".pdf", new ByteArrayResource(pdfBytes));
            }

            mailSender.send(message);
            log.info("Invoice email successfully sent to {}", toEmail);

        } catch (Exception e) {
            log.warn("Failed to send invoice email to {}: {}. Proceeding without breaking flow.", toEmail, e.getMessage());
        }
    }
}
