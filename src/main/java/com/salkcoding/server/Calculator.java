package com.salkcoding.server;

import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;

public class Calculator implements Runnable {

    private final UUID uuid;
    private final Socket socket;

    public Calculator(UUID uuid, Socket socket) {
        this.uuid = uuid;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            //Input stream
            InputStream inputStream = socket.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            Scanner scanner = new Scanner(bufferedInputStream);

            //Output stream
            OutputStream outputStream = socket.getOutputStream();
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            PrintWriter writer = new PrintWriter(bufferedOutputStream, true);

            String line;
            while (true) {
                //Just enter \n then, disconnect
                line = scanner.nextLine();
                if (line.isEmpty()) {
                    System.out.println("Disconnected with " + socket.getInetAddress().getHostAddress() + "(" + uuid + ")");
                    break;
                }

                //Send result
                try {
                    BigDecimal result = calculate(line);
                    writer.println(result.toPlainString());

                    //Logging
                    System.out.println("Request from " + uuid + ": " + line + ", Response: " + result.toPlainString());
                } catch (ArithmeticException | IllegalArgumentException e) {
                    writer.println(e.getMessage());
                    //Logging
                    System.out.println("Request from " + uuid + ": " + line + ", Response: " + e.getMessage());
                }
            }

            //Close In/OutputStream automatically, So we don't have to close scanner and writer
            socket.close();
            Server.disconnect(uuid);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private BigDecimal calculate(String line) throws ArithmeticException, IllegalArgumentException {
        String[] split = line.split(" ");
        if (split.length < 3)
            throw new IllegalArgumentException("Too few arguments");
        if (split.length > 3)
            throw new IllegalArgumentException("Too many arguments");
        if (!isNumeric(split[0]))
            throw new IllegalArgumentException("First argument must be a number");
        if (!Operator.isOperator(split[1]))
            throw new IllegalArgumentException("Second argument must be an arithmetic operator");
        if (!isNumeric(split[2]))
            throw new IllegalArgumentException("Third argument must be a number");

        BigDecimal a = new BigDecimal(split[0]);
        BigDecimal b = new BigDecimal(split[2]);
        Operator operator = Operator.getOperator(split[1]);

        return switch (operator) {
            case ADD:
                yield a.add(b);
            case SUB:
                yield a.subtract(b);
            case MUL:
                yield a.multiply(b);
            case DIV:
                yield a.divide(b, MathContext.DECIMAL32);
        };
    }

    private boolean isNumeric(String string) {
        try {
            //If argument is not a number then NumberFormatException will be break out
            new BigDecimal(string);
        } catch (NumberFormatException ignored) {
            return false;
        }
        return true;
    }

}
