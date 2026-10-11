package com.ellyon;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;

import com.ellyon.endpoints.BoardEndpoints;
import com.sun.net.httpserver.HttpServer;

public class Server {
    private HttpServer server = null;
    private BoardEndpoints boardEndpoints = null;

    public Server() throws IOException {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        } catch (BindException ex) {
            System.out.println("Endereço em uso!");
        }
    }

    public void handleServer() {
        // Métodos que servirão os endpoints para a placa Arduino
        // ==================================================================================================
        this.boardEndpoints = new BoardEndpoints(server);

        boardEndpoints.setPort();
        boardEndpoints.connect();
        boardEndpoints.standby();
        boardEndpoints.activate();

        // ==================================================================================================

        server.createContext("/style.css", exchange -> {
            try {
                var stream = App.class.getResourceAsStream("/style.css");

                if (stream == null) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }

                byte[] css = stream.readAllBytes();

                exchange.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
                exchange.sendResponseHeaders(200, css.length);
                exchange.getResponseBody().write(css);

            } finally {
                exchange.close();
            }
        });

        // Assets
        server.createContext("/assets/", exchange -> {
            String caminho = exchange.getRequestURI().getPath();

            if (caminho.contains("..") || caminho.contains("\\")) {
                exchange.sendResponseHeaders(400, -1);
                exchange.close();
                return;
            }

            String recurso = caminho.substring(1);

            try (var stream = App.class.getClassLoader()
                    .getResourceAsStream(recurso)) {

                if (stream == null) {
                    exchange.sendResponseHeaders(404, -1);
                    exchange.close();
                    return;
                }

                byte[] dados = stream.readAllBytes();

                String tipo = switch (recurso.substring(
                        recurso.lastIndexOf('.') + 1).toLowerCase()) {
                    case "css" -> "text/css";
                    case "js" -> "text/javascript";
                    case "png" -> "image/png";
                    case "jpg", "jpeg" -> "image/jpeg";
                    case "svg" -> "image/svg+xml";
                    case "gif" -> "image/gif";
                    case "webp" -> "image/webp";
                    case "ico" -> "image/x-icon";
                    default -> "application/octet-stream";
                };

                exchange.getResponseHeaders().set(
                        "Content-Type", tipo);

                exchange.sendResponseHeaders(200, dados.length);
                exchange.getResponseBody().write(dados);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                exchange.close();
            }
        });

        // Página de index.html
        server.createContext("/", exchange -> {
            try {
                var stream = App.class.getResourceAsStream("/index.html");
                if (stream == null) {
                    throw new IllegalStateException("Arquivo index.html não encontrado!");
                }
                byte[] page = stream.readAllBytes();

                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, page.length);
                exchange.getResponseBody().write(page);
            } finally {
                exchange.close();
            }

        });

        server.start();

        System.out.println(
                "Servidor iniciado em http://127.0.0.1:8080");
    }
}
