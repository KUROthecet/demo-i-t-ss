package com.aims.dto.response;

import lombok.Data;
import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaypalResponseDto {
	private String id;
    private String status; // e.g., "CREATED", "APPROVED", "COMPLETED"
}
