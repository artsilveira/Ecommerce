package br.com.casadasmolduras.handler;

import br.com.casadasmolduras.http.JsonResponse;
import br.com.casadasmolduras.model.ProductPage;
import br.com.casadasmolduras.repository.ProductRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductHandler implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(ProductHandler.class.getName());
    private final ProductRepository repository;

    public ProductHandler(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            if (!exchange.getRequestURI().getPath().equals("/api/products")) {
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

            int page;
            try {
                page = parsePage(exchange.getRequestURI().getRawQuery());
            } catch (IllegalArgumentException exception) {
                JsonResponse.send(exchange, 400, Map.of(
                    "error", "INVALID_QUERY",
                    "message", "Use somente page, uma única vez, com um inteiro positivo até 2147483647."
                ));
                return;
            }

            ProductPage products;
            try {
                products = repository.findActivePage(page);
            } catch (SQLException | IllegalStateException exception) {
                LOGGER.log(Level.SEVERE, "Falha ao consultar produtos.", exception);
                JsonResponse.send(exchange, 500, Map.of(
                    "error", "INTERNAL_SERVER_ERROR",
                    "message", "Não foi possível consultar os produtos."
                ));
                return;
            }

            JsonResponse.send(exchange, 200, products);
        }
    }

    private static int parsePage(String rawQuery) {
        if (rawQuery == null || rawQuery.isEmpty()) {
            return 1;
        }

        String[] parameters = rawQuery.split("&", -1);
        if (parameters.length != 1) {
            throw new IllegalArgumentException("Parâmetros inválidos.");
        }

        String[] pair = parameters[0].split("=", 2);
        if (pair.length != 2) {
            throw new IllegalArgumentException("Parâmetro sem valor.");
        }

        String name = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
        String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
        if (!name.equals("page") || !value.matches("[0-9]+")) {
            throw new IllegalArgumentException("Parâmetro inválido.");
        }

        int page = Integer.parseInt(value);
        if (page < 1) {
            throw new IllegalArgumentException("Página inválida.");
        }
        return page;
    }
}
