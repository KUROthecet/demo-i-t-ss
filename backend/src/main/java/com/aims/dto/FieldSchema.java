package com.aims.dto;

import java.util.List;

public record FieldSchema(
    String key,
    String attributeKey,
    String label,
    String type,
    List<String> options,
    boolean required
) {}
