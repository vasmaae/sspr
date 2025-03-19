package com.gutorov.lab3.service;

import org.springframework.stereotype.Service;

@Service
public class MatrixService {
    public int findMin(int[][] matrix, int startRow, int endRow) {
        if (matrix == null || matrix.length == 0 || startRow >= endRow || endRow > matrix.length) {
            throw new IllegalArgumentException("Invalid matrix or row range");
        }

        int min = Integer.MAX_VALUE;
        int n = matrix.length;

        for (int i = startRow; i < endRow; i++) {
            for (int j = i + 1; j < n; j++) {
                if (matrix[i][j] < min) {
                    min = matrix[i][j];
                }
            }
        }
        return min == Integer.MAX_VALUE ? -1 : min;
    }
}
