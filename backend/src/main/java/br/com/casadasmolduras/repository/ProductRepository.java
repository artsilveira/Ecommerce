package br.com.casadasmolduras.repository;

import br.com.casadasmolduras.config.DatabaseConnection;
import br.com.casadasmolduras.model.Category;
import br.com.casadasmolduras.model.ProductPage;
import br.com.casadasmolduras.model.ProductSummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
}
