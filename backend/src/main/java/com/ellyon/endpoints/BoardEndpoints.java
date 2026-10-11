package com.ellyon.endpoints;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fazecast.jSerialComm.SerialPort;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class BoardEndpoints {
    // private HttpExchange exchange;
    private HttpServer server;
    private SerialPort arduino;
    private String portPath = "";

    public BoardEndpoints(HttpServer server) {
        this.server = server;
    }

    public boolean setPort() {
        SerialPort[] ports = SerialPort.getCommPorts();

        for (SerialPort port : ports) {
            System.out.println("Procurando porta: " + port.getSystemPortPath());
            port.setBaudRate(9600);

            if (port.openPort()) {
                this.arduino = port;

                System.out.println("Arduino conectado em: " + port.getSystemPortPath());
                portPath = port.getSystemPortPath();
            }

            return true;
        }

        System.out.println("Não foi possivel encontrar uma porta!");
        return false;
    }

    public HttpContext connect() {
        return this.server.createContext("/connect", exchange -> {
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
    }

    private void sendJson(HttpExchange exchange, String json) throws IOException {
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");

        exchange.sendResponseHeaders(200, response.length);

        try (var output = exchange.getResponseBody()) {
            output.write(response);
        }
    }
}
