package com.example.order.service;

import com.example.order.client.ProductCatalogClient;
import com.example.order.model.OrderEntity;
import com.example.order.model.OrderEntity.OrderStatusEnum;
import com.example.order.model.OrderItem;
import com.example.order.model.CreateOrderRequest;
import com.example.order.model.OrderDto;
import com.example.order.model.OrderItemDto;
import com.example.order.repository.OrderRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class OrderService {

	private static final Logger log = LoggerFactory.getLogger(OrderService.class);

	private static final String ORDER_ASYNC_LOG = """
		[ASYNC] Order lifecycle event:
		Order: %s | Status: %s | Total: %s | Customer: %s
		""";

	private final OrderRepository orderRepository;
	private final ProductCatalogClient productCatalogClient;

	public OrderService(OrderRepository orderRepository, ProductCatalogClient productCatalogClient) {
		this.orderRepository = orderRepository;
		this.productCatalogClient = productCatalogClient;
	}

	@Transactional
	public OrderDto createOrder(CreateOrderRequest request) {
		String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		OrderEntity order = new OrderEntity(orderNumber, request.customerId());

		BigDecimal total = BigDecimal.ZERO;
		for (var itemReq : request.items()) {
			OrderItem item = new OrderItem(itemReq.sku(), itemReq.quantity(), itemReq.unitPrice());
			order.addItem(item);
			total = total.add(item.getSubtotal());
		}
		order.setTotalAmount(total);

		OrderEntity saved = orderRepository.save(order);
		processOrderAsync(saved.getId());
		return toDto(saved);
	}

	/**
	 * Asynchronously processes order (verify inventory, update status).
	 */
	@Async
	public CompletableFuture<Void> processOrderAsync(Long orderId) {
		return CompletableFuture.runAsync(() -> {
			try {
				Optional<OrderEntity> opt = orderRepository.findById(orderId);
				if (opt.isEmpty()) return;

				OrderEntity order = opt.get();
				log.info(ORDER_ASYNC_LOG.formatted(
						order.getOrderNumber(),
						order.getStatus(),
						order.getTotalAmount(),
						order.getCustomerId()));

				// Async verification with Product Catalog (fire-and-forget style)
				for (OrderItem item : order.getItems()) {
					productCatalogClient.getProductBySku(item.getSku())
							.doOnNext(p -> log.info("Product verified: {} qty={}", p.sku(), p.quantity()))
							.doOnError(e -> log.warn("Product check failed for SKU {}: {}", item.getSku(), e.getMessage()))
							.subscribe();
				}

				// Simulate async confirmation
				orderRepository.findById(orderId).ifPresent(o -> {
					o.setStatus(OrderStatusEnum.CONFIRMED);
					orderRepository.save(o);
					log.info("Order {} confirmed asynchronously", o.getOrderNumber());
				});
			} catch (Exception e) {
				log.error("Async order processing failed for orderId={}", orderId, e);
			}
		});
	}

	@Transactional(readOnly = true)
	public List<OrderDto> findByCustomer(String customerId) {
		return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
				.map(this::toDto)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public Optional<OrderDto> findByOrderNumber(String orderNumber) {
		return orderRepository.findByOrderNumber(orderNumber).map(this::toDto);
	}

	@Transactional(readOnly = true)
	public Optional<OrderDto> findById(Long id) {
		return orderRepository.findById(id).map(this::toDto);
	}

	@Transactional
	public Optional<OrderDto> updateStatus(Long id, OrderStatusEnum status) {
		return orderRepository.findById(id)
				.map(o -> {
					o.setStatus(status);
					return toDto(orderRepository.save(o));
				});
	}

	@Transactional
	public Optional<OrderDto> cancel(Long id) {
		return updateStatus(id, OrderStatusEnum.CANCELLED);
	}

	private OrderDto toDto(OrderEntity o) {
		List<OrderItemDto> itemDtos = o.getItems().stream()
				.map(i -> new OrderItemDto(i.getId(), i.getSku(), i.getQuantity(), i.getUnitPrice()))
				.collect(Collectors.toList());
		return new OrderDto(
				o.getId(),
				o.getOrderNumber(),
				o.getCustomerId(),
				o.getStatus().name(),
				o.getTotalAmount(),
				o.getCreatedAt(),
				o.getUpdatedAt(),
				itemDtos
		);
	}
}
