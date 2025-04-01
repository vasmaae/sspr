package com.gutorov;

import org.apache.ignite.lang.IgniteCallable;

import java.util.List;

public class MatrixMinTask implements IgniteCallable<Integer> {
    private final List<List<Integer>> subMatrix;
    private final int startRow;
    private final int n;

    public MatrixMinTask(List<List<Integer>> subMatrix, int startRow, int n) {
        this.subMatrix = subMatrix;
        this.startRow = startRow;
        this.n = n;
    }

    @Override
    public Integer call() {
        int min = Integer.MAX_VALUE;
        for (int k = 0; k < subMatrix.size(); k++) {
            int i = startRow + k; // Глобальный индекс строки
            for (int j = i + 1; j < n; j++) { // Элементы выше главной диагонали
                int val = subMatrix.get(k).get(j);
                if (val < min) min = val;
            }
        }
        return min == Integer.MAX_VALUE ? null : min; // Если нет элементов, возвращаем null
    }
}