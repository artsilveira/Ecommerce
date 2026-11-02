package br.com.casadasmolduras.handler;

import br.com.casadasmolduras.http.JsonResponse;
import br.com.casadasmolduras.model.Category;
import br.com.casadasmolduras.repository.CategoryRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CategoryHandler implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(CategoryHandler.class.getName());
    private final CategoryRepository repository;

    public CategoryHandler(CategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            if (!exchange.getRequestURI().getPath().equals("/api/categories")) {
                JsonResponse.send(exchange, 404, Map.of(
                    "error", "NOT_FOUND",
                    "message", "Recurso não encontrado."
                ));
                return;
            }

            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.getResponseHeaders().set("Allow", "GET");
                JsonResponse.send(exchange, 405, Map.of(
                    "error", "METHOD_NOT_ALLOWED",
                    "message", "Método não permitido."
                ));
                return;
            }

            List<Category> categories;
            try {
                categories = repository.findAllActive();
            } catch (SQLException | IllegalStateException exception) {
                LOGGER.log(Level.SEVERE, "Falha ao consultar categorias.", exception);
                JsonResponse.send(exchange, 500, Map.of(
                    "error", "INTERNAL_SERVER_ERROR",
                    "message", "Não foi possível consultar as categorias."
                ));
                return;
            }

            JsonResponse.send(exchange, 200, categories);
        }
    }
}
