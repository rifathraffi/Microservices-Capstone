package com.example.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * Async client for Product Catalog Service via WebClient.
 */
@Component
public class ProductCatalogClient {

	private final WebClient webClient;

	public ProductCatalogClient(
			WebClient.Builder builder,
			@Value("${app.product-catalog.url:http://product-catalog-service}") String baseUrl) {
		this.webClient = builder.baseUrl(baseUrl).build();
	}

	public Mono<ProductInfo> getProductBySku(String sku) {
		return webClient.get()
				.uri("/api/v1/products/sku/{sku}", sku)
				.retrieve()
				.bodyToMono(ProductInfo.class)
				.onErrorResume(e -> Mono.empty());
	}

	public Mono<Boolean> adjustInventory(String sku, int delta) {
		return webClient.patch()
				.uri(uriBuilder -> uriBuilder.path("/api/v1/products/{sku}/inventory")
						.queryParam("delta", delta)
						.build(sku))
				.retrieve()
				.bodyToMono(InventoryResponse.class)
				.map(r -> "success".equals(r.status()))
				.onErrorReturn(false);
	}

	public record ProductInfo(Long id, String name, String description, java.math.BigDecimal price, Integer quantity, String sku) {}
	public record InventoryResponse(String sku, int quantity, String status) {}
}
