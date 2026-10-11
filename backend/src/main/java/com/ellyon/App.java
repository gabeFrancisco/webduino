package com.ellyon;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;

import com.fazecast.jSerialComm.SerialPort;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.nio.charset.StandardCharsets;

public class App {
    private static SerialPort arduino;
    private static String portPath = "";

    public static boolean setPort() {
        SerialPort[] ports = SerialPort.getCommPorts();

        for (SerialPort port : ports) {
            System.out.println("Procurando porta: " + port.getSystemPortPath());
            port.setBaudRate(9600);

            if (port.openPort()) {
                arduino = port;

                System.out.println("Arduino conectado em: " + port.getSystemPortPath());
                portPath = port.getSystemPortPath();
            }

            return true;
        }

        System.out.println("Não foi possivel encontrar uma porta!");
        return false;
    }

    public static void main(String[] args) throws Exception {

        // System.out.println("Deus abençoe a sua vida!");

        // Essa porta vai ser dinamica mais tarde!

        Thread.sleep(2000);
        HttpServer server = null;

        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        } catch (BindException ex) {
            System.out.println("Endereço em uso!");
        }

        server.createContext("/connect", exchange -> {
            try {
                if (arduino == null || !arduino.isOpen()) {
                    if (!setPort()) {
                        sendJson(exchange, "Falha ao abrir a porta serial");
                        return;
                    }
                }

                sendJson(exchange, portPath);

            } catch (Exception e) {
                e.printStackTrace();

                try {
                    byte[] erro = "{\"erro\":\"Erro interno no servidor\"}"
                            .getBytes(StandardCharsets.UTF_8);

                    exchange.getResponseHeaders().set(
                            "Content-Type",
                            "application/json; charset=UTF-8");

                    exchange.sendResponseHeaders(500, erro.length);
                    exchange.getResponseBody().write(erro);

                } catch (IOException ignored) {
                    // A conexão pode já ter sido encerrada.
                }

            } finally {
                exchange.close();
            }
        });

        // CSS
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

        // Requisições
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

    private static void sendJson(HttpExchange exchange, String json) throws IOException {
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");

        exchange.sendResponseHeaders(200, response.length);

        try (var output = exchange.getResponseBody()) {
            output.write(response);
        }
    }
}
