package br.com.casadasmolduras.model;

import java.util.List;

public record ProductDetail(
        long id,
        String name,
        String slug,
        String description,
        String material,
        Category category,
        boolean featured,
        List<ProductVariant> variants,
        List<ProductImage> images
) {

    public ProductDetail {
        variants = List.copyOf(variants);
        images = List.copyOf(images);
    }
}