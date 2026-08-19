package com.medcore.hms.billing.service;

import com.medcore.hms.billing.entity.Invoice;
import com.medcore.hms.billing.entity.InvoiceItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class InvoicePdfService {

    public byte[] generateInvoicePdf(Invoice invoice) {
        log.info("Generating PDF for Invoice: {}", invoice.getInvoiceNumber());

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                PDType1Font fontOblique = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

                float margin = 40;
                float yPosition = 780;

                // Header - Title
                contentStream.beginText();
                contentStream.setFont(fontBold, 20);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("MEDCORE HMS - INVOICE");
                contentStream.endText();

                // Status Badge
                String statusStr = "STATUS: " + invoice.getStatus().name();
                contentStream.beginText();
                contentStream.setFont(fontBold, 12);
                contentStream.newLineAtOffset(420, yPosition);
                contentStream.showText(statusStr);
                contentStream.endText();

                yPosition -= 25;

                // Sub-header line
                contentStream.setLineWidth(1.0f);
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(PDRectangle.A4.getWidth() - margin, yPosition);
                contentStream.stroke();

                yPosition -= 20;

                // Hospital & Invoice Info
                contentStream.beginText();
                contentStream.setFont(fontRegular, 10);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("MedCore Health Care Center");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(fontBold, 10);
                contentStream.newLineAtOffset(350, yPosition);
                contentStream.showText("Invoice #: " + invoice.getInvoiceNumber());
                contentStream.endText();

                yPosition -= 15;

                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                contentStream.beginText();
                contentStream.setFont(fontRegular, 10);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Email: billing@medcore.com | Tel: +1-800-MEDCORE");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(fontRegular, 10);
                contentStream.newLineAtOffset(350, yPosition);
                contentStream.showText("Issue Date: " + (invoice.getIssueDate() != null ? invoice.getIssueDate().format(fmt) : "N/A"));
                contentStream.endText();

                yPosition -= 15;

                contentStream.beginText();
                contentStream.setFont(fontRegular, 10);
                contentStream.newLineAtOffset(350, yPosition);
                contentStream.showText("Due Date:   " + (invoice.getDueDate() != null ? invoice.getDueDate().format(fmt) : "N/A"));
                contentStream.endText();

                yPosition -= 25;

                // Patient Details Section
                contentStream.beginText();
                contentStream.setFont(fontBold, 11);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("PATIENT DETAILS");
                contentStream.endText();

                yPosition -= 15;

                String patientName = invoice.getPatient() != null
                        ? invoice.getPatient().getFirstName() + " " + invoice.getPatient().getLastName()
                        : "Unknown";
                String patientIdStr = invoice.getPatient() != null ? invoice.getPatient().getPatientId() : "N/A";
                String patientEmail = invoice.getPatient() != null && invoice.getPatient().getEmail() != null
                        ? invoice.getPatient().getEmail() : "N/A";
                String patientPhone = invoice.getPatient() != null && invoice.getPatient().getPhone() != null
                        ? invoice.getPatient().getPhone() : "N/A";

                contentStream.beginText();
                contentStream.setFont(fontRegular, 10);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Name: " + patientName + " (ID: " + patientIdStr + ")");
                contentStream.endText();

                yPosition -= 15;

                contentStream.beginText();
                contentStream.setFont(fontRegular, 10);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Email: " + patientEmail + " | Phone: " + patientPhone);
                contentStream.endText();

                yPosition -= 30;

                // Table Header
                contentStream.setLineWidth(0.5f);
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(PDRectangle.A4.getWidth() - margin, yPosition);
                contentStream.stroke();

                yPosition -= 15;

                contentStream.beginText();
                contentStream.setFont(fontBold, 10);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Description");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(fontBold, 10);
                contentStream.newLineAtOffset(260, yPosition);
                contentStream.showText("Category");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(fontBold, 10);
                contentStream.newLineAtOffset(360, yPosition);
                contentStream.showText("Unit Price");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(fontBold, 10);
                contentStream.newLineAtOffset(430, yPosition);
                contentStream.showText("Qty");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(fontBold, 10);
                contentStream.newLineAtOffset(480, yPosition);
                contentStream.showText("Total");
                contentStream.endText();

                yPosition -= 10;

                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(PDRectangle.A4.getWidth() - margin, yPosition);
                contentStream.stroke();

                yPosition -= 18;

                // Table Items
                if (invoice.getItems() != null) {
                    for (InvoiceItem item : invoice.getItems()) {
                        String desc = item.getDescription() != null ? item.getDescription() : "Item";
                        if (desc.length() > 30) desc = desc.substring(0, 27) + "...";
                        String cat = item.getCategory() != null ? item.getCategory().name() : "OTHER";
                        String priceStr = "Rs. " + (item.getUnitPrice() != null ? item.getUnitPrice().toString() : "0.00");
                        String qtyStr = String.valueOf(item.getQuantity() != null ? item.getQuantity() : 1);
                        String totalStr = "Rs. " + (item.getTotalPrice() != null ? item.getTotalPrice().toString() : "0.00");

                        contentStream.beginText();
                        contentStream.setFont(fontRegular, 9);
                        contentStream.newLineAtOffset(margin, yPosition);
                        contentStream.showText(desc);
                        contentStream.endText();

                        contentStream.beginText();
                        contentStream.setFont(fontRegular, 9);
                        contentStream.newLineAtOffset(260, yPosition);
                        contentStream.showText(cat);
                        contentStream.endText();

                        contentStream.beginText();
                        contentStream.setFont(fontRegular, 9);
                        contentStream.newLineAtOffset(360, yPosition);
                        contentStream.showText(priceStr);
                        contentStream.endText();

                        contentStream.beginText();
                        contentStream.setFont(fontRegular, 9);
                        contentStream.newLineAtOffset(430, yPosition);
                        contentStream.showText(qtyStr);
                        contentStream.endText();

                        contentStream.beginText();
                        contentStream.setFont(fontRegular, 9);
                        contentStream.newLineAtOffset(480, yPosition);
                        contentStream.showText(totalStr);
                        contentStream.endText();

                        yPosition -= 16;
                        if (yPosition < 150) break;
                    }
                }

                yPosition -= 10;
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(PDRectangle.A4.getWidth() - margin, yPosition);
                contentStream.stroke();

                yPosition -= 20;

                // Financial Summary Box
                float summaryX = 320;
                String discountLabel = "Discount";
                if (invoice.getDiscountPercentage() != null && invoice.getDiscountPercentage().compareTo(java.math.BigDecimal.ZERO) > 0) {
                    discountLabel += " (" + invoice.getDiscountPercentage().setScale(1, java.math.RoundingMode.HALF_UP) + "%)";
                }

                contentStream.beginText();
                contentStream.setFont(fontRegular, 10);
                contentStream.newLineAtOffset(summaryX, yPosition);
                contentStream.showText("Subtotal:       INR " + formatAmount(invoice.getSubtotal()));
                contentStream.endText();

                yPosition -= 15;
                contentStream.beginText();
                contentStream.setFont(fontRegular, 10);
                contentStream.newLineAtOffset(summaryX, yPosition);
                contentStream.showText(discountLabel + ":  -INR " + formatAmount(invoice.getDiscountAmount()));
                contentStream.endText();

                yPosition -= 15;
                contentStream.beginText();
                contentStream.setFont(fontRegular, 10);
                contentStream.newLineAtOffset(summaryX, yPosition);
                contentStream.showText("Tax:           +INR " + formatAmount(invoice.getTaxAmount()));
                contentStream.endText();

                yPosition -= 15;
                contentStream.beginText();
                contentStream.setFont(fontBold, 11);
                contentStream.newLineAtOffset(summaryX, yPosition);
                contentStream.showText("Grand Total:    INR " + formatAmount(invoice.getGrandTotal()));
                contentStream.endText();

                yPosition -= 15;
                contentStream.beginText();
                contentStream.setFont(fontRegular, 10);
                contentStream.newLineAtOffset(summaryX, yPosition);
                contentStream.showText("Paid Amount:    INR " + formatAmount(invoice.getPaidAmount()));
                contentStream.endText();

                yPosition -= 15;
                contentStream.beginText();
                contentStream.setFont(fontBold, 11);
                contentStream.newLineAtOffset(summaryX, yPosition);
                contentStream.showText("Balance Due:    INR " + formatAmount(invoice.getBalanceDue()));
                contentStream.endText();

                // Footer
                contentStream.beginText();
                contentStream.setFont(fontOblique, 9);
                contentStream.newLineAtOffset(margin, 40);
                contentStream.showText("Thank you for choosing MedCore HMS. This is a computer-generated invoice.");
                contentStream.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF for invoice {}", invoice.getInvoiceNumber(), e);
            throw new RuntimeException("Failed to generate invoice PDF: " + e.getMessage(), e);
        }
    }

    private String formatAmount(BigDecimal amount) {
        return amount != null ? amount.setScale(2, java.math.RoundingMode.HALF_UP).toString() : "0.00";
    }
}
