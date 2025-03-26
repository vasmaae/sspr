package com.gutorov.lab3;

import com.gutorov.lab3.client.MatrixClient;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Lab3Application {
    public static void main(String[] args) {
        try {
            if (args.length != 4 || Integer.parseInt(args[0]) < 1 || Integer.parseInt(args[1]) < 1) {
                System.out.println("Invalid arguments");
                System.exit(1);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid arguments");
            System.exit(1);
        }

        MatrixClient matrixClient = new MatrixClient(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2], args[3]);
        long startTime = System.currentTimeMillis();
        int min = matrixClient.findMinAboveDiagonal();
        long endTime = System.currentTimeMillis();
        System.out.println("Min above diagonal: " + min);
    }
}
