import mpi.MPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class Matrix {
    private final int[][] matrix;
    private final int numberOfChars;

    public Matrix(int rows, int cols, int numberOfChars) {
        matrix = new int[rows][cols];
        this.numberOfChars = numberOfChars;
        Random r = new Random();
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                matrix[i][j] = r.nextInt((int) Math.pow(10, numberOfChars));
    }

    public int processMatrixSingleThread() {
        int min = matrix[0][1];
        for (int i = 0; i < matrix.length - 1; i++)
            for (int j = i + 1; j < matrix[i].length; j++)
                min = Math.min(min, matrix[i][j]);
        return min;
    }

    public int processMatrixThreadPoolExecutor() {
        int numThreads = Runtime.getRuntime().availableProcessors();

        try (ExecutorService executorService = Executors.newFixedThreadPool(numThreads)) {
            List<Future<Integer>> futures = new ArrayList<>();

            for (int i = 0; i < matrix.length - 1; i++) {
                final int row = i;

                futures.add(executorService.submit(() -> {
                    int min = matrix[row][row + 1];
                    for (int j = row + 1; j < matrix[row].length; j++)
                        min = Math.min(min, matrix[row][j]);
                    return min;
                }));
            }

            int min = futures.getFirst().get();
            for (Future<Integer> future : futures)
                min = Math.min(min, future.get());


            executorService.shutdown();
            return min;
        } catch (ExecutionException | InterruptedException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public int processMatrixForkJoinPoll() {
        return new ForkJoinMinFinder().findMin();
    }

    public int processMatrixMPI(String args[]) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int[] globalMin = new int[1];

        if (rank == 0) {
            int[] dims = new int[]{matrix.length, matrix[0].length};
            for (int i = 1; i < size; i++)
                MPI.COMM_WORLD.Send(dims, 0, 2, MPI.INT, i, 0);


            int rowsPerProcess = matrix.length / size;
            int remainder = matrix.length % size;

            int startRow = rowsPerProcess + (rank < remainder ? 1 : 0);
            int endRow = Math.min(matrix.length, startRow + rowsPerProcess + (rank + 1 < remainder ? 1 : 0));

            for (int i = 1; i < size; i++) {
                int processStartRow = i * rowsPerProcess + Math.min(i, remainder);
                int processEndRow = Math.min(matrix.length, processStartRow + rowsPerProcess + (i < remainder ? 1 : 0));
                int numRows = processEndRow - processStartRow;

                if (numRows > 0)
                    for (int r = processStartRow; r < processEndRow; r++)
                        MPI.COMM_WORLD.Send(matrix[r], 0, matrix[r].length, MPI.INT, i, r);
            }

            int localMin = findLocalMin(0, startRow);

            int[] remoteMin = new int[1];
            for (int i = 1; i < size; i++) {
                MPI.COMM_WORLD.Recv(remoteMin, 0, 1, MPI.INT, i, 99);
                localMin = Math.min(localMin, remoteMin[0]);
            }

            globalMin[0] = localMin;
        } else {
            int[] dims = new int[2];
            MPI.COMM_WORLD.Recv(dims, 0, 2, MPI.INT, 0, 0);
            int rows = dims[0];
            int cols = dims[1];

            int rowsPerProcess = rows / size;
            int remainder = rows % size;
            int startRow = rank * rowsPerProcess + Math.min(rank, remainder);
            int endRow = Math.min(rows, startRow + rowsPerProcess + (rank < remainder ? 1 : 0));
            int numRows = endRow - startRow;

            int[][] localMatrix = new int[numRows][cols];

            if (numRows > 0) {
                for (int r = 0; r < numRows; r++) {
                    MPI.COMM_WORLD.Recv(localMatrix[r], 0, cols, MPI.INT, 0, startRow + r);
                }

                int localMin = Integer.MAX_VALUE;
                for (int i = 0; i < numRows; i++) {
                    int globalRow = startRow + i;
                    for (int j = globalRow + 1; j < cols; j++) {
                        localMin = Math.min(localMin, localMatrix[i][j]);
                    }
                }

                int[] minArray = new int[]{localMin};
                MPI.COMM_WORLD.Send(minArray, 0, 1, MPI.INT, 0, 99);
            }
        }

        MPI.Finalize();
        if (rank == 0)
            return globalMin[0];
        else
            return 0;
    }

    private int findLocalMin(int startRow, int endRow) {
        if (startRow >= matrix.length - 1) {
            return Integer.MAX_VALUE;
        }

        int min = Integer.MAX_VALUE;
        for (int i = startRow; i < endRow && i < matrix.length - 1; i++) {
            for (int j = i + 1; j < matrix[i].length; j++) {
                min = Math.min(min, matrix[i][j]);
            }
        }
        return min;
    }

    @Override
    public String toString() {
        if (matrix.length > 100)
            return "Matrix Size: " + matrix.length + "x" + matrix[0].length;

        StringBuilder sb = new StringBuilder();
        for (int[] ints : matrix) {
            for (int anInt : ints)
                sb.append(String.format("%" + numberOfChars + "d ", anInt));
            sb.append("\n");
        }
        return sb.toString();
    }

    private class ForkJoinMinFinder extends RecursiveTask<Integer> {
        private static final int THRESHOLD = 10;
        private final int start, end;

        public ForkJoinMinFinder() {
            this.start = 0;
            this.end = matrix.length;
        }

        public ForkJoinMinFinder(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected Integer compute() {
            if (end - start <= THRESHOLD) {
                int min = matrix[start][start + 1];
                for (int i = start; i < end - 1; i++)
                    for (int j = i + 1; j < matrix[i].length; j++)
                        min = Math.min(min, matrix[i][j]);
                return min;
            } else {
                int mid = (start + end) / 2;
                ForkJoinMinFinder leftTask = new ForkJoinMinFinder(start, mid);
                ForkJoinMinFinder rightTask = new ForkJoinMinFinder(mid, end);
                leftTask.fork();
                int rightRes = rightTask.compute();
                int leftRes = leftTask.join();
                return Math.min(leftRes, rightRes);
            }
        }

        public int findMin() {
            try (ForkJoinPool pool = new ForkJoinPool()) {
                return pool.invoke(new ForkJoinMinFinder());
            }
        }
    }
}
