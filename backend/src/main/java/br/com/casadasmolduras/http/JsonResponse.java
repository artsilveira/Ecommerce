package br.com.casadasmolduras.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class JsonResponse {

    private static final Gson GSON = new Gson();

    private JsonResponse() {
    }

    public static void send(HttpExchange exchange, int status, Object body)
            throws IOException {
        byte[] response = GSON.toJson(body).getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
            "Content-Type", "application/json; charset=UTF-8"
        );
        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
    }
}
