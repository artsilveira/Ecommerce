package br.com.casadasmolduras.model;

import java.math.BigDecimal;

public record ProductVariant(
        long id,
        String sku,
        BigDecimal widthCm,
        BigDecimal heightCm,
        String measurementReference,
        String finish,
        BigDecimal price,
        int availableQuantity
) {
}