package com.gutorov.lab3.client;

import org.springframework.web.client.RestTemplate;

import java.util.Random;

public class MatrixClient {
    private static final RestTemplate restTemplate = new RestTemplate();
    private static String serviceUrl1;
    private static String serviceUrl2;
    private final int[][] matrix;

    public MatrixClient(int rows, int cols, String serviceUrl1, String serviceUrl2) {
        matrix = new int[rows][cols];
        Random r = new Random();
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                matrix[i][j] = r.nextInt();

        MatrixClient.serviceUrl1 = "http://" + serviceUrl1 + "/matrix/find-min-above-diagonal";
        MatrixClient.serviceUrl2 = "http://" + serviceUrl2 + "/matrix/find-min-above-diagonal";
    }

    public int findMinAboveDiagonal() {
        int[] mins = new int[2];

        Thread thread1 = new Thread(() -> {
            int min = restTemplate.postForObject(serviceUrl1 + "?startRow=" + 0 +
                    "&endRow=" + (matrix.length / 2), matrix, int.class);
            mins[0] = min;
        });
        Thread thread2 = new Thread(() -> {
            int min = restTemplate.postForObject(serviceUrl2 + "?startRow=" + (matrix.length / 2) +
                    "&endRow=" + matrix.length, matrix, int.class);
            mins[1] = min;
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return Math.min(mins[0], mins[1]);
    }
}
