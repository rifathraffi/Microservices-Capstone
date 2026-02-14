package com.example.productcatalog.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Java 17 record for Product DTO.
 */
public record ProductDto(
	Long id,
	@NotBlank String name,
	String description,
	@NotNull @DecimalMin("0.01") BigDecimal price,
	@NotNull @Min(0) Integer quantity,
	@NotBlank String sku
) {}
