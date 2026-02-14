package com.example.productcatalog.model;

/**
 * Success result for inventory operations.
 */
public record InventorySuccess(String sku, int newQuantity) implements InventoryResult {}
