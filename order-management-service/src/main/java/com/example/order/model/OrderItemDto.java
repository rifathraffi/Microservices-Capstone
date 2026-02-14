package com.example.order.model;

import java.math.BigDecimal;

/**
 * Java 17 record for Order Item DTO.
 */
public record OrderItemDto(
	Long id,
	String sku,
	Integer quantity,
	BigDecimal unitPrice
) {}
