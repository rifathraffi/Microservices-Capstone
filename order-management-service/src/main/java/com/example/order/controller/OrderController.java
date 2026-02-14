package com.example.order.controller;

import com.example.order.model.CreateOrderRequest;
import com.example.order.model.OrderDto;
import com.example.order.model.OrderEntity.OrderStatusEnum;
import com.example.order.service.OrderService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	public ResponseEntity<OrderDto> create(@Valid @RequestBody CreateOrderRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
	}

	@GetMapping("/{id}")
	public ResponseEntity<OrderDto> getById(@PathVariable Long id) {
		return orderService.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/number/{orderNumber}")
	public ResponseEntity<OrderDto> getByOrderNumber(@PathVariable String orderNumber) {
		return orderService.findByOrderNumber(orderNumber)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping
	public List<OrderDto> listByCustomer(@RequestParam String customerId) {
		return orderService.findByCustomer(customerId);
	}

	@PatchMapping("/{id}/status/{status}")
	public ResponseEntity<OrderDto> updateStatus(
			@PathVariable Long id,
			@PathVariable OrderStatusEnum status) {
		return orderService.updateStatus(id, status)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping("/{id}/cancel")
	public ResponseEntity<OrderDto> cancel(@PathVariable Long id) {
		return orderService.cancel(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
}
