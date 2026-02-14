package com.example.order.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Java 17 record for Order DTO.
 */
public record OrderDto(
	Long id,
	String orderNumber,
	String customerId,
	String status,
	BigDecimal totalAmount,
	Instant createdAt,
	Instant updatedAt,
	List<OrderItemDto> items
) {}
