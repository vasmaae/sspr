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

    private class ForkJoinMinFinder extends RecursiveTask<Integer> {
        private final int start, end;
        private static final int THRESHOLD = 10;

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

    public int processMatrixForkJoinPoll() {
        return new ForkJoinMinFinder().findMin();
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
}
