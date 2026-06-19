package com.aims.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplateBuilder {

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    public String buildOrderConfirmationHtml(String name, String orderCode, long totalAmount, String transactionRef) {
        String txRows = (transactionRef != null && !transactionRef.isBlank())
            ? infoRow("VQR Reference",    "<code style='font-family:monospace;color:#1DB954'>" + transactionRef + "</code>") +
              infoRow("Transfer Content", "<code style='font-family:monospace;color:rgba(255,255,255,0.7)'>ORDER " + orderCode + "</code>")
            : "";
        String body =
            "<p style='margin:0 0 24px;font-size:14px;color:rgba(255,255,255,0.6);line-height:1.8'>" +
            "Hi <strong style='color:rgba(255,255,255,0.85)'>" + name + "</strong>, your order has been placed. " +
            "Please scan the QR code in your order to complete payment." +
            "</p>" +
            infoBox("#1DB954",
                infoRow("Order Reference", "<code style='font-family:monospace;color:#1DB954'>" + orderCode + "</code>") +
                infoRow("Status",          badge("PENDING PAYMENT", "#f59e0b")) +
                infoRow("Total",           "<strong style='color:#1DB954;font-size:15px'>" +
                    String.format("%,d", totalAmount).replace(",", ".") + " VND</strong>") +
                txRows
            ) +
            divider() +
            "<p style='margin:0;font-size:12px;color:rgba(255,255,255,0.35);line-height:1.8'>" +
            "Click the button below to view your order and complete payment." +
            "</p>" +
            ctaButton("View my order", frontendUrl + "/order/" + orderCode, "#1DB954", "#000");
        return layout("#1DB954", "Order Confirmed", "Thank you for your purchase. We're on it.", body);
    }

    public String buildPaymentConfirmationHtml(String name, String orderCode, long totalAmount,
                                                String transactionId, String captureId, String paidAt) {
        String captureRow = (captureId != null && !captureId.isBlank())
            ? infoRow("Capture ID", "<code style='font-family:monospace;color:rgba(255,255,255,0.7)'>" + captureId + "</code>")
            : "";
        String body =
            "<p style='margin:0 0 24px;font-size:14px;color:rgba(255,255,255,0.6);line-height:1.8'>" +
            "Hi <strong style='color:rgba(255,255,255,0.85)'>" + name + "</strong>, " +
            "your payment has been received and your order is now pending review by our team." +
            "</p>" +
            infoBox("#1DB954",
                infoRow("Order Reference", "<code style='font-family:monospace;color:#1DB954'>" + orderCode + "</code>") +
                infoRow("Status",          badge("PAID — PENDING REVIEW", "#1DB954")) +
                infoRow("Total",           "<strong style='color:#1DB954;font-size:15px'>" +
                    String.format("%,d", totalAmount).replace(",", ".") + " VND</strong>") +
                infoRow("Transaction ID",  "<code style='font-family:monospace;color:rgba(255,255,255,0.7)'>" + transactionId + "</code>") +
                captureRow +
                infoRow("Payment Date",    "<span style='color:rgba(255,255,255,0.6)'>" + paidAt + "</span>")
            ) +
            divider() +
            "<p style='margin:0;font-size:12px;color:rgba(255,255,255,0.35);line-height:1.8'>" +
            "Your order will be reviewed shortly. You'll receive another email once it is approved or rejected." +
            "</p>" +
            ctaButton("View my order", frontendUrl + "/order/" + orderCode, "#1DB954", "#000");
        return layout("#1DB954", "Payment Confirmed", "Your payment was received successfully.", body);
    }

    public String buildOrderApprovedHtml(String name, String orderCode) {
        String body =
            "<p style='margin:0 0 24px;font-size:14px;color:rgba(255,255,255,0.6);line-height:1.8'>" +
            "Good news, <strong style='color:rgba(255,255,255,0.85)'>" + name + "</strong>. " +
            "Your order has been reviewed and approved by our team. " +
            "Your items are being prepared for dispatch." +
            "</p>" +
            infoBox("#1DB954",
                infoRow("Order Reference", "<code style='font-family:monospace;color:#1DB954'>" + orderCode + "</code>") +
                infoRow("Status", badge("APPROVED", "#1DB954")) +
                infoRow("Next step", "<span style='color:rgba(255,255,255,0.6)'>Packing &amp; dispatch</span>")
            ) +
            divider() +
            "<p style='margin:0;font-size:12px;color:rgba(255,255,255,0.35);line-height:1.8'>" +
            "Standard delivery takes 2–4 business days. Rush orders in Hanoi and Ho Chi Minh City " +
            "are dispatched same-day or next morning." +
            "</p>" +
            ctaButton("View order", frontendUrl + "/order/" + orderCode, "#1DB954", "#000");
        return layout("#1DB954", "Order Approved", "Your order is confirmed and being prepared.", body);
    }

    public String buildOrderRejectedHtml(String name, String orderCode, String reason) {
        String refundSection =
            "<div style='margin-top:20px;background:rgba(255,255,255,0.02);border:1px solid rgba(255,255,255,0.06);" +
            "border-radius:10px;padding:18px 22px'>" +
            "<p style='margin:0 0 12px;font-size:11px;font-weight:700;letter-spacing:0.14em;color:rgba(255,255,255,0.35)'>REFUND POLICY</p>" +
            "<div style='font-size:13px;color:rgba(255,255,255,0.55);line-height:1.8'>" +
            "<div style='margin-bottom:8px'>" +
            "<span style='color:#1DB954;font-weight:700'>PayPal</span> — " +
            "Your refund is processed automatically and should appear within 3–5 business days." +
            "</div>" +
            "<div>" +
            "<span style='color:#f59e0b;font-weight:700'>VietQR</span> — " +
            "Our team will contact you directly to arrange a manual bank transfer. " +
            "Allow 1–3 business days after this notification." +
            "</div>" +
            "</div>" +
            "</div>";
        String body =
            "<p style='margin:0 0 24px;font-size:14px;color:rgba(255,255,255,0.6);line-height:1.8'>" +
            "Hi <strong style='color:rgba(255,255,255,0.85)'>" + name + "</strong>, " +
            "we were unable to fulfill your order at this time. " +
            "We understand this is frustrating, and we apologise for the inconvenience. " +
            "A full refund will be processed to your original payment method." +
            "</p>" +
            infoBox("#ef4444",
                infoRow("Order Reference", "<code style='font-family:monospace;color:#f87171'>" + orderCode + "</code>") +
                infoRow("Status", badge("REJECTED", "#f87171")) +
                infoRow("Reason", "<span style='color:rgba(255,255,255,0.6);text-align:right;max-width:280px'>" +
                    (reason != null && !reason.isBlank() ? reason : "Please contact our support team for details.") +
                    "</span>")
            ) +
            refundSection +
            divider() +
            "<p style='margin:0;font-size:12px;color:rgba(255,255,255,0.35);line-height:1.8'>" +
            "If you have questions about your refund or would like to place a new order, " +
            "contact us at <strong style='color:rgba(255,255,255,0.5)'>support@aims.store</strong>." +
            "</p>" +
            ctaButton("Contact support", frontendUrl + "/contact", "rgba(255,255,255,0.08)", "#fff");
        return layout("#ef4444", "Order Update", "We have an important update about your recent order.", body);
    }

    public String buildOrderCancelledHtml(String name, String orderCode, boolean refundIssued) {
        String refundNote = refundIssued
            ? "Your refund has been processed automatically and should appear within 3–5 business days."
            : "For VietQR payments, our team will contact you to arrange the manual transfer.";
        String body =
            "<p style='margin:0 0 24px;font-size:14px;color:rgba(255,255,255,0.6);line-height:1.8'>" +
            "Hi <strong style='color:rgba(255,255,255,0.85)'>" + name + "</strong>, " +
            "your cancellation has been confirmed. " + refundNote +
            "</p>" +
            infoBox("rgba(255,255,255,0.08)",
                infoRow("Order Reference", "<code style='font-family:monospace;color:rgba(255,255,255,0.5)'>" + orderCode + "</code>") +
                infoRow("Status", badge("CANCELLED", "rgba(255,255,255,0.45)")) +
                infoRow("Refund", badge(refundIssued ? "AUTO-PROCESSED" : "MANUAL TRANSFER", refundIssued ? "#1DB954" : "#f59e0b"))
            ) +
            divider() +
            "<p style='margin:0;font-size:12px;color:rgba(255,255,255,0.35);line-height:1.8'>" +
            "Questions? Write to <strong style='color:rgba(255,255,255,0.5)'>support@aims.store</strong>." +
            "</p>" +
            ctaButton("Browse the catalog", frontendUrl + "/search", "#1DB954", "#000");
        return layout("rgba(255,255,255,0.3)", "Order Cancelled", "Your cancellation has been processed.", body);
    }

    public String buildPasswordResetHtml(String name, String newPassword) {
        String body =
            "<p style='margin:0 0 24px;font-size:14px;color:rgba(255,255,255,0.6);line-height:1.8'>" +
            "Hi <strong style='color:rgba(255,255,255,0.85)'>" + name + "</strong>, " +
            "an administrator has reset your AIMS account password. " +
            "Please log in with the temporary password below and change it immediately." +
            "</p>" +
            "<div style='background:rgba(96,165,250,0.07);border:1px solid rgba(96,165,250,0.2);" +
            "border-radius:12px;padding:28px;text-align:center;margin-bottom:24px'>" +
            "<p style='margin:0 0 10px;font-size:10px;font-weight:700;letter-spacing:0.2em;color:rgba(255,255,255,0.35)'>TEMPORARY PASSWORD</p>" +
            "<code style='font-size:26px;font-weight:700;color:#60a5fa;letter-spacing:0.06em;font-family:monospace'>" +
            newPassword + "</code>" +
            "</div>" +
            "<p style='margin:0;font-size:12px;color:rgba(239,68,68,0.75);line-height:1.7;font-weight:500'>" +
            "Change this password immediately after logging in. Do not share it with anyone." +
            "</p>";
        return layout("#60a5fa", "Password Reset", "Your AIMS staff account password has been reset.", body);
    }

    public String buildNewsletterConfirmationHtml(String email) {
        String body =
            "<p style='margin:0 0 24px;font-size:14px;color:rgba(255,255,255,0.6);line-height:1.8'>" +
            "You're now subscribed to AIMS updates. We'll send you new arrivals, restocks, " +
            "and occasional promotions — no spam, unsubscribe anytime." +
            "</p>" +
            infoBox("#1DB954",
                infoRow("Subscribed email", "<span style='color:rgba(255,255,255,0.8)'>" + email + "</span>") +
                infoRow("What to expect", "<span style='color:rgba(255,255,255,0.6)'>New arrivals &amp; restocks</span>")
            ) +
            divider() +
            "<p style='margin:0;font-size:12px;color:rgba(255,255,255,0.35);line-height:1.8'>" +
            "To unsubscribe, reply to this email with the subject line <strong>UNSUBSCRIBE</strong>." +
            "</p>" +
            ctaButton("Browse the catalog", frontendUrl + "/search", "#1DB954", "#000");
        return layout("#1DB954", "You're subscribed", "Welcome to the AIMS mailing list.", body);
    }

    public String buildContactMessageHtml(String senderName, String senderEmail, String subject, String message) {
        String body =
            "<p style='margin:0 0 24px;font-size:14px;color:rgba(255,255,255,0.6);line-height:1.8'>" +
            "A new message was submitted through the AIMS contact form." +
            "</p>" +
            infoBox("rgba(255,255,255,0.08)",
                infoRow("From", "<strong style='color:#fff'>" + senderName + "</strong>") +
                infoRow("Email", "<a href='mailto:" + senderEmail + "' style='color:#1DB954;text-decoration:none'>" + senderEmail + "</a>") +
                infoRow("Subject", subject != null && !subject.isBlank() ? subject : "—")
            ) +
            "<div style='background:rgba(255,255,255,0.03);border:1px solid rgba(255,255,255,0.07);border-radius:12px;padding:24px;margin:20px 0'>" +
            "<p style='margin:0 0 10px;font-size:10px;font-weight:700;letter-spacing:0.2em;color:rgba(255,255,255,0.35)'>MESSAGE</p>" +
            "<p style='margin:0;font-size:14px;color:rgba(255,255,255,0.7);line-height:1.8;white-space:pre-wrap'>" + message + "</p>" +
            "</div>" +
            divider() +
            "<p style='margin:0;font-size:12px;color:rgba(255,255,255,0.35);line-height:1.8'>" +
            "Reply directly to this email to respond to " + senderName + "." +
            "</p>";
        return layout("rgba(255,255,255,0.2)", "New Contact Message", "Submitted via the AIMS contact form.", body);
    }

    public String buildUserBlockedHtml(String username, String reason) {
        String body =
            "<p style='margin:0 0 24px;font-size:14px;color:rgba(255,255,255,0.6);line-height:1.8'>" +
            "Your AIMS account <strong style='color:rgba(255,255,255,0.85)'>" + username + "</strong> " +
            "has been suspended by an administrator. You will not be able to log in until reinstated." +
            "</p>" +
            infoBox("#ef4444",
                infoRow("Account", "<code style='font-family:monospace;color:#f87171'>" + username + "</code>") +
                infoRow("Status", badge("SUSPENDED", "#ef4444")) +
                infoRow("Reason", "<span style='color:rgba(255,255,255,0.6)'>" +
                    (reason != null && !reason.isBlank() ? reason : "Policy violation") + "</span>")
            ) +
            divider() +
            "<p style='margin:0;font-size:12px;color:rgba(255,255,255,0.35);line-height:1.8'>" +
            "If you believe this is a mistake, contact support at " +
            "<strong style='color:rgba(255,255,255,0.5)'>support@aims.store</strong>." +
            "</p>";
        return layout("#ef4444", "Account Suspended", "Your AIMS account has been suspended.", body);
    }

    public String buildUserUnblockedHtml(String username) {
        String body =
            "<p style='margin:0 0 24px;font-size:14px;color:rgba(255,255,255,0.6);line-height:1.8'>" +
            "Your AIMS account <strong style='color:rgba(255,255,255,0.85)'>" + username + "</strong> " +
            "has been reinstated. You may now log in and access all features." +
            "</p>" +
            infoBox("#1DB954",
                infoRow("Account", "<code style='font-family:monospace;color:#1DB954'>" + username + "</code>") +
                infoRow("Status", badge("ACTIVE", "#1DB954"))
            ) +
            ctaButton("Log in to AIMS", frontendUrl + "/login", "#1DB954", "#000");
        return layout("#1DB954", "Account Reinstated", "Your access has been restored.", body);
    }

    public String buildUserDeactivatedHtml(String username) {
        String body =
            "<p style='margin:0 0 24px;font-size:14px;color:rgba(255,255,255,0.6);line-height:1.8'>" +
            "Your AIMS account <strong style='color:rgba(255,255,255,0.85)'>" + username + "</strong> " +
            "has been deactivated. If you need assistance, please contact the administrator." +
            "</p>" +
            infoBox("rgba(255,255,255,0.08)",
                infoRow("Account", "<code style='font-family:monospace;color:rgba(255,255,255,0.5)'>" + username + "</code>") +
                infoRow("Status", badge("DEACTIVATED", "rgba(255,255,255,0.4)"))
            ) +
            divider() +
            "<p style='margin:0;font-size:12px;color:rgba(255,255,255,0.35);line-height:1.8'>" +
            "Contact support at <strong style='color:rgba(255,255,255,0.5)'>support@aims.store</strong> for inquiries." +
            "</p>";
        return layout("rgba(255,255,255,0.3)", "Account Deactivated", "Your AIMS account has been deactivated.", body);
    }

    public String buildRoleChangedHtml(String username, String newRole) {
        String body =
            "<p style='margin:0 0 24px;font-size:14px;color:rgba(255,255,255,0.6);line-height:1.8'>" +
            "The role for your AIMS account <strong style='color:rgba(255,255,255,0.85)'>" + username + "</strong> " +
            "has been updated by an administrator." +
            "</p>" +
            infoBox("#60a5fa",
                infoRow("Account", "<code style='font-family:monospace;color:#60a5fa'>" + username + "</code>") +
                infoRow("New role", badge(newRole.toUpperCase(), "#60a5fa"))
            ) +
            divider() +
            "<p style='margin:0;font-size:12px;color:rgba(255,255,255,0.35);line-height:1.8'>" +
            "If you did not expect this change, contact support at " +
            "<strong style='color:rgba(255,255,255,0.5)'>support@aims.store</strong>." +
            "</p>" +
            ctaButton("Log in to AIMS", frontendUrl + "/login", "#60a5fa", "#000");
        return layout("#60a5fa", "Role Updated", "Your account permissions have changed.", body);
    }

    public String buildManagerRefundHtml(String orderCode, long amount, String customerName) {
        String body =
            "<p style='margin:0 0 24px;font-size:14px;color:rgba(255,255,255,0.6);line-height:1.8'>" +
            "A VietQR order has been cancelled or rejected and requires a manual bank transfer refund. " +
            "Please process this at your earliest convenience and record it in the system." +
            "</p>" +
            infoBox("#f59e0b",
                infoRow("Order Reference", "<code style='font-family:monospace;color:#f59e0b'>" + orderCode + "</code>") +
                infoRow("Customer", customerName) +
                infoRow("Amount to refund", "<strong style='color:#f59e0b;font-size:15px'>" +
                    String.format("%,d", amount).replace(",", ".") + " VND</strong>") +
                infoRow("Payment method", badge("VietQR", "#f59e0b"))
            ) +
            divider() +
            "<p style='margin:0;font-size:12px;color:rgba(255,255,255,0.35);line-height:1.8'>" +
            "VietQR does not support automated refunds. Contact the customer directly, " +
            "process the bank transfer, then mark the refund as complete in the admin dashboard." +
            "</p>";
        return layout("#f59e0b", "Manual Refund Required", "Action required — VietQR refund needs processing.", body);
    }

    private String layout(String accentColor, String headerTitle, String headerSubtitle, String body) {
        return "<!DOCTYPE html><html lang='en'><head>" +
               "<meta charset='UTF-8'>" +
               "<meta name='viewport' content='width=device-width,initial-scale=1.0'>" +
               "<title>" + headerTitle + " — AIMS</title>" +
               "</head>" +
               "<body style='margin:0;padding:0;background:#050505;" +
               "font-family:-apple-system,BlinkMacSystemFont,\"Segoe UI\",Helvetica,Arial,sans-serif;" +
               "color:#e8e8e8;-webkit-font-smoothing:antialiased'>" +
               "<table width='100%' cellpadding='0' cellspacing='0' role='presentation' style='background:#050505;min-height:100vh'>" +
               "<tr><td align='center' style='padding:48px 16px 64px'>" +
               "<table width='600' cellpadding='0' cellspacing='0' role='presentation' style='max-width:600px;width:100%'>" +
               "<tr><td style='padding-bottom:36px;text-align:center'>" +
               "<div style='display:inline-block;background:rgba(29,185,84,0.08);border:1px solid rgba(29,185,84,0.18);border-radius:14px;padding:14px 28px'>" +
               "<span style='font-size:20px;font-weight:800;letter-spacing:-0.04em;color:#fff'>AIMS" +
               "<span style='color:#1DB954'>.</span></span>" +
               "<div style='font-size:9px;letter-spacing:0.22em;color:rgba(255,255,255,0.3);font-weight:600;margin-top:2px'>AN INTERNET MEDIA STORE</div>" +
               "</div>" +
               "</td></tr>" +
               "<tr><td style='background:rgba(255,255,255,0.03);border:1px solid rgba(255,255,255,0.07);border-radius:20px;overflow:hidden'>" +
               "<div style='background:linear-gradient(135deg," + accentColor + "22 0%," + accentColor + "08 50%,transparent 100%);" +
               "padding:40px 44px 36px;border-bottom:1px solid rgba(255,255,255,0.06)'>" +
               "<h1 style='margin:0 0 10px;font-size:26px;font-weight:800;letter-spacing:-0.03em;color:#fff;line-height:1.2'>" +
               headerTitle + "</h1>" +
               "<p style='margin:0;font-size:14px;color:rgba(255,255,255,0.5);line-height:1.6'>" + headerSubtitle + "</p>" +
               "</div>" +
               "<div style='padding:36px 44px'>" + body + "</div>" +
               "</td></tr>" +
               "<tr><td style='padding-top:28px;text-align:center'>" +
               "<p style='margin:0 0 6px;font-size:11px;color:rgba(255,255,255,0.2);line-height:1.7'>" +
               "AIMS Media Store — An Internet Media Store</p>" +
               "<p style='margin:0;font-size:11px;color:rgba(255,255,255,0.14)'>" +
               "School of ICT, HUST · Hanoi, Vietnam</p>" +
               "</td></tr>" +
               "</table></td></tr></table>" +
               "</body></html>";
    }

    private String divider() {
        return "<div style='height:1px;background:rgba(255,255,255,0.06);margin:24px 0'></div>";
    }

    private String infoRow(String label, String value) {
        return "<div style='display:flex;justify-content:space-between;align-items:center;" +
               "padding:11px 0;border-bottom:1px solid rgba(255,255,255,0.04)'>" +
               "<span style='font-size:12px;color:rgba(255,255,255,0.4);font-weight:500'>" + label + "</span>" +
               "<span style='font-size:13px;color:#fff;font-weight:600;text-align:right'>" + value + "</span>" +
               "</div>";
    }

    private String badge(String text, String hex) {
        return "<span style='display:inline-block;padding:3px 12px;border-radius:9999px;" +
               "background:" + hex + "22;color:" + hex + ";font-size:10px;font-weight:700;letter-spacing:0.08em'>" +
               text + "</span>";
    }

    private String ctaButton(String text, String href, String bg, String fg) {
        return "<div style='text-align:center;margin-top:28px'>" +
               "<a href='" + href + "' style='display:inline-block;padding:14px 36px;" +
               "background:" + bg + ";color:" + fg + ";font-size:14px;font-weight:700;" +
               "border-radius:9999px;text-decoration:none;letter-spacing:0.01em'>" +
               text + "</a></div>";
    }

    private String infoBox(String accentColor, String content) {
        return "<div style='background:" + accentColor + "0d;border:1px solid " + accentColor + "28;" +
               "border-radius:12px;padding:20px 24px;margin:20px 0'>" + content + "</div>";
    }
}
