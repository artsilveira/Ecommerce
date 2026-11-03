package br.com.casadasmolduras.model;

public record ProductImage(
        long id,
        String imageUrl,
        String altText,
        int displayOrder
) {
}