public class Main {
    public static void main(String[] args) {
        try {
//            temporary убрал ибо хз чо там в mpi
//            if (args.length != 4) // размеры матрицы, количество цифр в числе (хз зачем сделал, но уже лень убирать) и аргумент сколько лаб запускать
//                throw new IllegalArgumentException("Put fucking numbers into a fucking command line.");

            Matrix matrix = new Matrix(Integer.parseInt(args[0]),
                    Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            System.out.println(matrix);

            if (args[3].contains("1"))
                demoLab1(matrix);
            if (args[3].contains("2"))
                demoLab2(matrix, args);

        } catch (NumberFormatException e) {
            System.out.println("Put numbers, not a letters, stupid idiot.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void demoLab1(Matrix matrix) {
        long startTime, endTime;
        int min;

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
    }

    private static void demoLab2(Matrix matrix, String[] args) {
        long startTime, endTime;
        int min;

        startTime = System.nanoTime();
        min = matrix.processMatrixMPI(args);
        endTime = System.nanoTime();
        printResults("MPI", min, endTime - startTime);
    }

    private static void printResults(String algorithm, int min, long time) {
        System.out.println(algorithm + ": min = " + min + ", time = " + time / 1000000);
    }
}