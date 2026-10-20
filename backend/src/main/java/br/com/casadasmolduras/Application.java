package br.com.casadasmolduras;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Application {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 8080),
                0
        );

        server.createContext("/health", Application::handleHealth);

        server.start();

        System.out.println("Servidor iniciado em http://127.0.0.1:8080");
    }

    private static void handleHealth(HttpExchange exchange)
            throws IOException {

        try (exchange) {
            if (!exchange.getRequestURI().getPath().equals("/health")) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.getResponseHeaders().set("Allow", "GET");
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            byte[] response = "OK".getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set(
                    "Content-Type",
                    "text/plain; charset=UTF-8"
            );

            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
        }
    }
}