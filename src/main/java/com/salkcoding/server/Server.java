package com.salkcoding.server;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {

    private static ExecutorService executorService;
    private static final ConcurrentHashMap<UUID, Calculator> connectionMap = new ConcurrentHashMap<>();

    public static void disconnect(UUID uuid) {
        Calculator removed = connectionMap.remove(uuid);
        if (removed != null) {
            //Logging
            System.out.println("Connection uuid " + uuid + " disconnected");
            System.out.println("Number of connections currently maintained: " + connectionMap.size());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        if (executorService != null)
            throw new IllegalStateException("Previous static objects are remain on memory");

        //Maximize thread usage
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try (ServerSocket serverSocket = new ServerSocket(50000)) {
            System.out.println("Server ready with port number " + serverSocket.getLocalPort());
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connection request from " + socket.getInetAddress().getHostAddress());

                //Create UUID doesn't conflict
                UUID uuid;
                do {
                    uuid = UUID.randomUUID();
                } while (connectionMap.containsKey(uuid));

                //Create calculator instance
                Calculator calculator = new Calculator(uuid, socket);
                connectionMap.put(uuid, calculator);

                //Run on other thread
                executorService.execute(calculator);

                //Logging
                System.out.println("Connected " + socket.getInetAddress().getHostAddress() + " (connection uuid: " + uuid + ")");
                System.out.println("Number of connections currently maintained: " + connectionMap.size());
            }
        } catch (InterruptedIOException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Not accept client connection anymore.
        executorService.shutdown();
        //Wait for remain client connection will be closed.
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
}
