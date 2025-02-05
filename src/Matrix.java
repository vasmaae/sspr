import java.util.Random;
import java.util.concurrent.*;

public class Matrix {
    private final int[][] matrix;
    private final int numberOfChars;

    public Matrix(int rows, int cols, int numberOfChars) {
        matrix = new int[rows][cols];
        this.numberOfChars = numberOfChars;
        Random rand = new Random();
        System.out.println(1000 * numberOfChars);

        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                matrix[i][j] = rand.nextInt((int) Math.pow(10, numberOfChars));
    }

    public int processMatrixSingleThread() {
        int min = matrix[0][1];

        for (int i = 0; i < matrix.length; i++)
            for (int j = i + 1; j < matrix[i].length; j++)
                if (matrix[i][j] < min)
                    min = matrix[i][j];

        return min;
    }

    public int processMatrixThreadPoolExecutor() throws InterruptedException, ExecutionException {
        int numThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        int size = matrix.length;
        int chunkSize = size / numThreads;
        Future<Integer>[] futures = new Future[numThreads];

        for (int t = 0; t < numThreads; t++) {
            final int startRow = t * chunkSize;
            final int endRow = (t == numThreads - 1) ? size : startRow + chunkSize;
            futures[t] = executor.submit(() -> {
                int localMin = Integer.MAX_VALUE;
                for (int i = startRow; i < endRow; i++) {
                    for (int j = i + 1; j < size; j++) {
                        if (matrix[i][j] < localMin) {
                            localMin = matrix[i][j];
                        }
                    }
                }
                return localMin;
            });
        }

        int globalMin = Integer.MAX_VALUE;
        for (Future<Integer> future : futures) {
            int localMin = future.get();
            if (localMin < globalMin) {
                globalMin = localMin;
            }
        }

        executor.shutdown();
        return globalMin;
    }

    private static class ForkJoinMinFinder extends RecursiveTask<Integer> {
        private final int[][] matrix;
        private final int startRow;
        private final int endRow;

        public ForkJoinMinFinder(int[][] matrix, int startRow, int endRow) {
            this.matrix = matrix;
            this.startRow = startRow;
            this.endRow = endRow;
        }

        @Override
        protected Integer compute() {
            if (endRow - startRow <= 10) {
                int localMin = Integer.MAX_VALUE;
                for (int i = startRow; i < endRow; i++) {
                    for (int j = i + 1; j < matrix.length; j++) {
                        if (matrix[i][j] < localMin) {
                            localMin = matrix[i][j];
                        }
                    }
                }
                return localMin;
            } else {
                int mid = (startRow + endRow) / 2;
                ForkJoinMinFinder leftTask = new ForkJoinMinFinder(matrix, startRow, mid);
                ForkJoinMinFinder rightTask = new ForkJoinMinFinder(matrix, mid, endRow);
                leftTask.fork();
                int rightResult = rightTask.compute();
                int leftResult = leftTask.join();
                return Math.min(leftResult, rightResult);
            }
        }

        public static int findMin(int[][] matrix) {
            ForkJoinPool pool = new ForkJoinPool();
            return pool.invoke(new ForkJoinMinFinder(matrix, 0, matrix.length));
        }
    }

    public int processMatrixForkJoinPoll() {
        int min = ForkJoinMinFinder.findMin(matrix);
        return min;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                sb.append(String.format("%" + numberOfChars + "d", matrix[i][j]));
                sb.append(" ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
