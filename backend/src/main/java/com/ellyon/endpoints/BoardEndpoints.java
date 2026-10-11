package com.ellyon.endpoints;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import com.fazecast.jSerialComm.SerialPort;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class BoardEndpoints {
    // private HttpExchange exchange;
    private HttpServer server;
    private SerialPort arduino;
    private String portPath = "";
    private final AtomicReference<String> lastReading = new AtomicReference<>("Sem leitura");

    public BoardEndpoints(
            HttpServer server) {
        this.server = server;
    }

    private void startSensorReading() {
        arduino.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
                0,
                0);

        Thread thread = new Thread(() -> {
            byte[] buffer = new byte[128];

            while (arduino.isOpen()) {
                int bytesLidos = arduino.readBytes(
                        buffer, buffer.length);

                if (bytesLidos > 0) {
                    String dados = new String(
                            buffer, 0, bytesLidos,
                            StandardCharsets.UTF_8);

                    System.out.print(dados);
                }
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    public HttpContext readUltrasonic() {
        return this.server.createContext("/ultrasonic", exchange -> {
            try {
                String value = lastReading.get();

                String json = "{\"valor\":\"" + value + "\"}";
                this.sendJson(exchange, json, 200);

                // try(var output = exchange.getResponseBody()){
                // output.write(response);
                // }

            } catch (Exception e) {
                exchange.close();
            }
        });
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
                        sendJson(exchange, "Falha ao abrir a porta serial", 500);
                    }
                }

                sendJson(exchange, portPath, 200);

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

                }

            } finally {
                exchange.close();
            }
        });
    }

    public HttpContext standby() {
        return this.server.createContext("/standby", exchange -> {
            String cmd = "STANDBY\n";
            byte[] data = cmd.getBytes();

            try {
                this.arduino.writeBytes(data, data.length);

                System.out.println("STANDBY!!");
                exchange.sendResponseHeaders(200, -1);

            } catch (Exception ex) {
                System.out.println("Erro ao enviar comando STANDBY" + ex.getMessage());
                exchange.sendResponseHeaders(500, -1);
            } finally {
                exchange.close();
            }
        });
    }

    public HttpContext activate() {
        return this.server.createContext("/activate", exchange -> {
            String cmd = "INIT\n";
            byte[] data = cmd.getBytes();

            try {
                this.arduino.writeBytes(data, data.length);

                System.out.println("INIT!!");
                startSensorReading();
                exchange.sendResponseHeaders(200, -1);

            } catch (Exception ex) {
                System.out.println("Erro ao enviar comando INIT" + ex.getMessage());
                exchange.sendResponseHeaders(500, -1);
            } finally {
                exchange.close();
            }
        });
    }

    private void sendJson(HttpExchange exchange, String json, int code) throws IOException {

        if (json == null || json.isEmpty()) {
            exchange.sendResponseHeaders(code, -1);
            exchange.close();
            return;
        }
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");

        exchange.sendResponseHeaders(code, response.length);

        try (var output = exchange.getResponseBody()) {
            output.write(response);
        }
    }
}
