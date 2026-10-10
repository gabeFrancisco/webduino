package com.ellyon;

import java.net.InetSocketAddress;
// import java.nio.file.Files;
// import java.nio.file.Path;

import com.fazecast.jSerialComm.SerialPort;
import com.sun.net.httpserver.HttpServer;

public class App {
    private static SerialPort arduino;

    public static void main(String[] args) throws Exception {
        // System.out.println("Deus abençoe a sua vida!");

        // Essa porta vai ser dinamica mais tarde!
        arduino = SerialPort.getCommPort("/dev/ttyUSB0");
        arduino.setBaudRate(9600);

        if (!arduino.openPort()) {
            System.out.println("Erro ao abrir a porta! Verifique!");
            return;
        }

        Thread.sleep(2000);

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);

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
}
