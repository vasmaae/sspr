package com.gutorov;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.compute.ComputeJob;
import org.apache.ignite.compute.ComputeJobAdapter;
import org.apache.ignite.compute.ComputeJobResult;
import org.apache.ignite.compute.ComputeTaskSplitAdapter;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setClientMode(true);
        cfg.setPeerClassLoadingEnabled(true);

        TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();
        TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();

        List<String> addresses = new ArrayList<>();
        addresses.add("192.168.1.102:47500..47509");
        ipFinder.setAddresses(addresses);
        discoverySpi.setIpFinder(ipFinder);
        discoverySpi.setSocketTimeout(5000);
        discoverySpi.setAckTimeout(5000);
        cfg.setDiscoverySpi(discoverySpi);

        try (Ignite ignite = Ignition.start(cfg)) {
            System.out.println("Connected to cluster. Number of nodes: " + ignite.cluster().nodes().size());

            int[][] matrix = generateMatrix(500, 500);

            // printMatrix(matrix);

            long startTime = System.nanoTime();
            ClusterGroup computeGroup = ignite.cluster().forServers();
            Integer min = ignite.compute(computeGroup).execute(new MatrixMinTask(matrix), null);
            long endTime = System.nanoTime();

            System.out.println("Minimal element above main diagonal: " + min);
            System.out.println("Execution time: " + (endTime - startTime) / 1_000_000 + " ms");
        } catch (Exception e) {
            System.err.println("Failed to start Ignite or execute computation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int[][] generateMatrix(int rows, int cols) {
        int[][] matrix = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = (int) (Math.random() * 100);
            }
        }
        return matrix;
    }

    private static void printMatrix(int[][] matrix) {
        System.out.println("Matrix:");
        for (int[] row : matrix) {
            for (int val : row) {
                System.out.print(val + "\t");
            }
            System.out.println();
        }
    }

    private static class MatrixMinTask extends ComputeTaskSplitAdapter<Void, Integer> implements Serializable {
        private static final long serialVersionUID = 1L;
        private final int[][] matrix;

        public MatrixMinTask(int[][] matrix) {
            this.matrix = matrix;
        }

        @Override
        protected List<ComputeJob> split(int gridSize, Void arg) {
            List<ComputeJob> jobs = new ArrayList<>();
            int n = matrix.length;

            for (int i = 0; i < n; i++) {
                jobs.add(new MatrixMinJob(matrix, i));
            }

            return jobs;
        }

        @Override
        public Integer reduce(List<ComputeJobResult> results) {
            int globalMin = Integer.MAX_VALUE;

            for (ComputeJobResult res : results) {
                Integer localMin = res.getData();
                if (localMin != null && localMin < globalMin) {
                    globalMin = localMin;
                }
            }

            return globalMin == Integer.MAX_VALUE ? null : globalMin;
        }
    }

    private static class MatrixMinJob extends ComputeJobAdapter implements Serializable {
        private static final long serialVersionUID = 1L;
        private final int[][] matrix;
        private final int rowIndex;

        public MatrixMinJob(int[][] matrix, int rowIndex) {
            this.matrix = matrix;
            this.rowIndex = rowIndex;
        }

        @Override
        public Object execute() {
            int localMin = Integer.MAX_VALUE;
            int n = matrix.length;

            for (int j = rowIndex + 1; j < n; j++) {
                if (matrix[rowIndex][j] < localMin) {
                    localMin = matrix[rowIndex][j];
                }
            }

            return localMin == Integer.MAX_VALUE ? null : localMin;
        }
    }
}