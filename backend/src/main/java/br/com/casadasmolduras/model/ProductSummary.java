package br.com.casadasmolduras.model;

import java.math.BigDecimal;

public record ProductSummary(
    long id,
    String name,
    String slug,
    Category category,
    BigDecimal startingPrice,
    boolean available,
    boolean featured
) {
}
