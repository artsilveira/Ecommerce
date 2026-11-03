package br.com.casadasmolduras.repository;

import br.com.casadasmolduras.config.DatabaseConnection;
import br.com.casadasmolduras.model.Category;
import br.com.casadasmolduras.model.ProductPage;
import br.com.casadasmolduras.model.ProductSummary;
import br.com.casadasmolduras.model.ProductDetail;
import br.com.casadasmolduras.model.ProductImage;
import br.com.casadasmolduras.model.ProductVariant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRepository {

    public static final int PAGE_SIZE = 12;

    public ProductPage findActivePage(int page) throws SQLException {
        if (page < 1) {
            throw new IllegalArgumentException("A página deve ser maior que zero.");
        }

        String sql = """
            SELECT
                p.id,
                p.name,
                p.slug,
                p.is_featured,
                c.id AS category_id,
                c.name AS category_name,
                c.slug AS category_slug,
                MIN(v.price) AS starting_price,
                BOOL_OR(v.stock_quantity > v.reserved_quantity) AS available
            FROM public.products AS p
            JOIN public.categories AS c ON c.id = p.category_id
            JOIN public.product_variants AS v ON v.product_id = p.id
            WHERE p.is_active = true
              AND c.is_active = true
              AND v.is_active = true
            GROUP BY p.id, c.id
            ORDER BY p.id
            LIMIT ? OFFSET ?
            """;

        List<ProductSummary> products = new ArrayList<>();
        long offset = ((long) page - 1) * PAGE_SIZE;

        try (
            Connection connection = DatabaseConnection.open();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, PAGE_SIZE + 1);
            statement.setLong(2, offset);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Category category = new Category(
                        result.getLong("category_id"),
                        result.getString("category_name"),
                        result.getString("category_slug")
                    );
                    products.add(new ProductSummary(
                        result.getLong("id"),
                        result.getString("name"),
                        result.getString("slug"),
                        category,
                        result.getBigDecimal("starting_price"),
                        result.getBoolean("available"),
                        result.getBoolean("is_featured")
                    ));
                }
            }
        }

        boolean hasNext = products.size() > PAGE_SIZE;
        if (hasNext) {
            products.removeLast();
        }
        return new ProductPage(products, page, PAGE_SIZE, hasNext);
    }

    public Optional<ProductDetail> findActiveById(long productId)
            throws SQLException {

        String sql = """
        SELECT
            p.id,
            p.name,
            p.slug,
            p.description,
            p.material,
            p.is_featured,
            c.id AS category_id,
            c.name AS category_name,
            c.slug AS category_slug
        FROM public.products AS p
        JOIN public.categories AS c
            ON c.id = p.category_id
        WHERE p.id = ?
          AND p.is_active = true
          AND c.is_active = true
          AND EXISTS (
              SELECT 1
              FROM public.product_variants AS v
              WHERE v.product_id = p.id
                AND v.is_active = true
          )
        """;

        try (
                Connection connection = DatabaseConnection.open();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, productId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }

                Category category = new Category(
                        result.getLong("category_id"),
                        result.getString("category_name"),
                        result.getString("category_slug")
                );

                ProductDetail product = new ProductDetail(
                        result.getLong("id"),
                        result.getString("name"),
                        result.getString("slug"),
                        result.getString("description"),
                        result.getString("material"),
                        category,
                        result.getBoolean("is_featured"),
                        findActiveVariants(connection, productId),
                        findImages(connection, productId)
                );

                return Optional.of(product);
            }
        }
    }

    private List<ProductVariant> findActiveVariants(
            Connection connection,
            long productId
    ) throws SQLException {

        String sql = """
        SELECT
            id,
            sku,
            width_cm,
            height_cm,
            measurement_reference,
            finish,
            price,
            stock_quantity - reserved_quantity AS available_quantity
        FROM public.product_variants
        WHERE product_id = ?
          AND is_active = true
        ORDER BY id
        """;

        List<ProductVariant> variants = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, productId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    variants.add(new ProductVariant(
                            result.getLong("id"),
                            result.getString("sku"),
                            result.getBigDecimal("width_cm"),
                            result.getBigDecimal("height_cm"),
                            result.getString("measurement_reference"),
                            result.getString("finish"),
                            result.getBigDecimal("price"),
                            result.getInt("available_quantity")
                    ));
                }
            }
        }

        return variants;
    }

    private List<ProductImage> findImages(
            Connection connection,
            long productId
    ) throws SQLException {

        String sql = """
        SELECT
            id,
            image_url,
            alt_text,
            display_order
        FROM public.product_images
        WHERE product_id = ?
        ORDER BY display_order, id
        """;

        List<ProductImage> images = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, productId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    images.add(new ProductImage(
                            result.getLong("id"),
                            result.getString("image_url"),
                            result.getString("alt_text"),
                            result.getInt("display_order")
                    ));
                }
            }
        }

        return images;
    }
}
