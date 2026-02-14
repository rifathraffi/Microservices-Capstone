package com.example.productcatalog.model;

/**
 * Java 17 sealed hierarchy for inventory operation results.
 */
public sealed interface InventoryResult permits InventorySuccess, InventoryError {
}
