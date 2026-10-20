package br.com.casadasmolduras.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private DatabaseConnection() {
    }

    public static Connection open() throws SQLException {
        String url = requiredEnvironmentVariable("DB_URL");
        String user = requiredEnvironmentVariable("DB_USER");
        String password = requiredEnvironmentVariable("DB_PASSWORD");

        return DriverManager.getConnection(url, user, password);
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Variável de ambiente obrigatória não configurada: " + name
            );
        }

        return value;
    }
}
