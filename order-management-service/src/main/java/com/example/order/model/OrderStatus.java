package com.example.order.model;

/**
 * Java 17 sealed type for order status.
 */
public sealed interface OrderStatus
		permits OrderStatus.Pending, OrderStatus.Confirmed, OrderStatus.Shipped, OrderStatus.Delivered, OrderStatus.Cancelled {

	record Pending() implements OrderStatus {}
	record Confirmed() implements OrderStatus {}
	record Shipped() implements OrderStatus {}
	record Delivered() implements OrderStatus {}
	record Cancelled() implements OrderStatus {}
}
