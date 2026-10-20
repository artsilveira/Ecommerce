package br.com.casadasmolduras;

import br.com.casadasmolduras.model.Category;
import br.com.casadasmolduras.repository.CategoryRepository;

import java.sql.SQLException;

public class DatabaseConnectionTest {

    public static void main(String[] args) {
        CategoryRepository repository = new CategoryRepository();

        try {
            for (Category category : repository.findAllActive()) {
                System.out.printf(
                        "%d | %s | %s%n",
                        category.id(),
                        category.name(),
                        category.slug()
                );
            }
        } catch (SQLException | IllegalStateException exception) {
            System.err.println(
                    "Falha ao consultar categorias: " + exception.getMessage()
            );
            System.exit(1);
        }
    }
}