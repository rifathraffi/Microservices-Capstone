package com.example.productcatalog.service;

import com.example.productcatalog.model.InventoryError;
import com.example.productcatalog.model.InventoryResult;
import com.example.productcatalog.model.InventorySuccess;
import com.example.productcatalog.model.Product;
import com.example.productcatalog.model.ProductDto;
import com.example.productcatalog.repository.ProductRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

	private static final String INVENTORY_LOG_TEMPLATE = """
		Inventory update for SKU: %s | Previous: %d | New: %d | Operation: %s
		""";

	private final ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public List<ProductDto> findAll() {
		return productRepository.findAll().stream()
				.map(this::toDto)
				.collect(Collectors.toList());
	}

	public Optional<ProductDto> findById(Long id) {
		return productRepository.findById(id).map(this::toDto);
	}

	public Optional<ProductDto> findBySku(String sku) {
		return productRepository.findBySku(sku).map(this::toDto);
	}

	@Transactional
	public ProductDto create(ProductDto dto) {
		if (productRepository.existsBySku(dto.sku())) {
			throw new IllegalArgumentException("Product with SKU " + dto.sku() + " already exists");
		}
		Product product = new Product(
				dto.name(),
				dto.description() != null ? dto.description() : "",
				dto.price(),
				dto.quantity(),
				dto.sku()
		);
		return toDto(productRepository.save(product));
	}

	@Transactional
	public Optional<ProductDto> update(Long id, ProductDto dto) {
		return productRepository.findById(id)
				.map(p -> {
					p.setName(dto.name());
					p.setDescription(dto.description());
					p.setPrice(dto.price());
					p.setQuantity(dto.quantity());
					return toDto(productRepository.save(p));
				});
	}

	@Transactional
	public InventoryResult adjustInventory(String sku, int delta) {
		return productRepository.findBySku(sku)
				.map(p -> {
					int prev = p.getQuantity();
					int next = Math.max(0, prev + delta);
					p.setQuantity(next);
					productRepository.save(p);
					// Text block used for structured log (Java 17)
					System.getLogger(ProductService.class.getName()).log(System.Logger.Level.INFO,
							INVENTORY_LOG_TEMPLATE.formatted(sku, prev, next, delta >= 0 ? "ADD" : "REMOVE"));
					return (InventoryResult) new InventorySuccess(sku, next);
				})
				.orElse(new InventoryError(sku, "Product not found"));
	}

	@Transactional
	public void deleteById(Long id) {
		productRepository.deleteById(id);
	}

	private ProductDto toDto(Product p) {
		return new ProductDto(
				p.getId(),
				p.getName(),
				p.getDescription(),
				p.getPrice(),
				p.getQuantity(),
				p.getSku()
		);
	}
}
