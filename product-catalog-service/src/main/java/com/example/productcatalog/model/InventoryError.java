package com.example.productcatalog.model;

/**
 * Error result for inventory operations.
 */
public record InventoryError(String sku, String message) implements InventoryResult {}
