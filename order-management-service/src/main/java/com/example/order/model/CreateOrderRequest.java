package com.example.order.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Java 17 record for create order request.
 */
public record CreateOrderRequest(
	@NotBlank String customerId,
	@NotEmpty @Valid List<OrderItemRequest> items
) {}
