package com.aims.adapter;

public record PaymentResult(boolean success, String transactionId, String message) {
}
