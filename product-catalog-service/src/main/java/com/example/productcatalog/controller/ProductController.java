package com.example.productcatalog.controller;

import com.example.productcatalog.model.InventoryError;
import com.example.productcatalog.model.InventoryResult;
import com.example.productcatalog.model.InventorySuccess;
import com.example.productcatalog.model.ProductDto;
import com.example.productcatalog.service.ProductService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping
	public List<ProductDto> list() {
		return productService.findAll();
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductDto> getById(@PathVariable Long id) {
		return productService.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/sku/{sku}")
	public ResponseEntity<ProductDto> getBySku(@PathVariable String sku) {
		return productService.findBySku(sku)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductDto dto) {
		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(dto));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<ProductDto> update(@PathVariable Long id, @Valid @RequestBody ProductDto dto) {
		return productService.update(id, dto)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PatchMapping("/{sku}/inventory")
	public ResponseEntity<Map<String, Object>> adjustInventory(
			@PathVariable String sku,
			@RequestParam int delta
	) {
		InventoryResult result = productService.adjustInventory(sku, delta);
		if (result instanceof InventorySuccess s) {
			return ResponseEntity.ok(Map.of(
					"sku", s.sku(),
					"quantity", s.newQuantity(),
					"status", "success"
			));
		}
		InventoryError e = (InventoryError) result;
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
				"sku", e.sku(),
				"message", e.message(),
				"status", "error"
		));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		productService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
