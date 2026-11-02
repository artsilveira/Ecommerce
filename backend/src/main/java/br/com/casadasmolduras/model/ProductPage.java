package br.com.casadasmolduras.model;

import java.util.List;

public record ProductPage(
    List<ProductSummary> items,
    int page,
    int pageSize,
    boolean hasNext
) {
    public ProductPage {
        items = List.copyOf(items);
    }
}
