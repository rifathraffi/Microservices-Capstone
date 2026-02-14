package com.example.order.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * Java 17 record for order item in create request.
 */
public record OrderItemRequest(
	@NotBlank String sku,
	@Min(1) Integer quantity,
	@DecimalMin("0.01") BigDecimal unitPrice
) {}
