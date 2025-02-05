public class Main {
    public static void main(String[] args) {
        try {
            long startTime = 0, endTime = 0;
            int min = 0;
            Matrix matrix = new Matrix(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            System.out.println(matrix);

            startTime = System.nanoTime();
            min = matrix.processMatrixSingleThread();
            endTime = System.nanoTime();
            printResults("SingleThread", min, endTime - startTime);

            startTime = System.nanoTime();
            min = matrix.processMatrixThreadPoolExecutor();
            endTime = System.nanoTime();
            printResults("ThreadPoolExecutor", min, endTime - startTime);

            startTime = System.nanoTime();
            min = matrix.processMatrixForkJoinPoll();
            endTime = System.nanoTime();
            printResults("ForkJoinPoll", min, endTime - startTime);

        } catch (NumberFormatException e) {
            System.out.println("Put numbers, not a letters, stupid idiot.");
        } catch (Exception e) {
            System.out.println("Kurwa idzie.");
        }
    }

    private static void printResults(String algorithm, int min, long time) {
        System.out.println(algorithm + ": min = " + min + ", time = " + time / 1000000);
    }
}