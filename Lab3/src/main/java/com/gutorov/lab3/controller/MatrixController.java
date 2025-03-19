package com.gutorov.lab3.controller;

import com.gutorov.lab3.service.MatrixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/matrix")
public class MatrixController {
    @Autowired
    private MatrixService matrixService;

    @PostMapping("/find-min-above-diagonal")
    public int getMin(@RequestBody int[][] matrix, @RequestParam int startRow, @RequestParam int endRow) {
        long startTime = System.nanoTime();
        int result = matrixService.findMin(matrix, startRow, endRow);
        long endTime = System.nanoTime();
        double executionTimeMs = (endTime - startTime) / 1_000_000.0;
        System.out.println("Time: " + executionTimeMs + " ms");
        return result;
    }
}
