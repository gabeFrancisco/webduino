package com.ellyon;

public class App {
    public static void main(String[] args) throws Exception {
        // System.out.println("Deus abençoe a sua vida!");

        Thread.sleep(2000);

        Server server = new Server();
        server.handleServer();
    }
}
