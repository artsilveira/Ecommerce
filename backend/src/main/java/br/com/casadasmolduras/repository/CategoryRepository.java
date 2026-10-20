package br.com.casadasmolduras.repository;

import br.com.casadasmolduras.config.DatabaseConnection;
import br.com.casadasmolduras.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {

    public List<Category> findAllActive() throws SQLException {
        String sql = """
            SELECT id, name, slug
            FROM public.categories
            WHERE is_active = true
            ORDER BY id
            """;

        List<Category> categories = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.open();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()
        ) {
            while (result.next()) {
                Category category = new Category(
                        result.getLong("id"),
                        result.getString("name"),
                        result.getString("slug")
                );

                categories.add(category);
            }
        }

        return categories;
    }
}