package com.salkcoding.client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.Scanner;

public class Client {

    private static final File configurationFile = new File(Paths.get("").toAbsolutePath().toString(), "config.dat");

    private static String serverAddress;
    private static int serverPort;

    public static void main(String[] args) {
        try {
            //Read from file
            loadConfiguration();
        } catch (IOException e) {
            //If exception occurred, then init with default values
            serverAddress = "localhost";
            serverPort = 50000;
            System.out.println("Load configuration failed!");
            System.out.println("Set as default, server address " + serverAddress + ", server port " + serverPort);
        }

        System.out.println("Connect to Server...");
        try (Socket socket = new Socket(serverAddress, serverPort)) {
            System.out.println("Connect to " + socket.getInetAddress().getHostAddress() + " successfully");
            //Input stream
            InputStream inputStream = socket.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            Scanner scanner = new Scanner(bufferedInputStream);

            //Output stream
            OutputStream outputStream = socket.getOutputStream();
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            PrintWriter writer = new PrintWriter(bufferedOutputStream, true);

            try (Scanner keyboard = new Scanner(System.in)) {
                while (true) {
                    //\n means exit
                    String request = keyboard.nextLine();
                    if (request.isEmpty()) break;

                    writer.println(request);
                    System.out.println("Request: " + request);

                    String response = scanner.nextLine();
                    System.out.println("Response: " + response);
                }
                writer.println("");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadConfiguration() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(configurationFile);
        //Read address and port
        try (Scanner scanner = new Scanner(fileInputStream)) {
            serverAddress = scanner.nextLine();
            serverPort = scanner.nextInt();
        }
        System.out.println("Load configuration successfully!");
        System.out.println("Server address " + serverAddress + ", server port " + serverPort);
    }
}
