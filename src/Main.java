public class Main {
    public static void main(String[] args) {
        try {
            long startTime = 0, endTime = 0;
            int min = 0;
            Matrix matrix = new Matrix(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
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
            System.out.println("Вводи только числа и ничего другого, быдло.");
        } catch (Exception e) {
            System.out.println("Coś poszło nie tak, po prostu zerżnąłem — pierdolę, pieprzyłem jego matkę, " +
                    "pieprzyłem jego siostrę, łamałem głowę cegłami, zrzucałem z balkonu, przewracałem, " +
                    "znowu pieprzyłem, potem jego ojca w dupę pieprzyłem, potem kazałem jego ojcu pieprzyć w dupę jego siostrę, " +
                    "suko, a jego, pedarasa, kurwa, zmuszałem to wszystko do oglądania, a potem gówno zjadło ogólne " +
                    "(to, co wszyscy jego pieprzeni krewni nosili), kurwa. Kurwa idzie.");
        }
    }

    private static void printResults(String algorithm, int min, long time) {
        System.out.println(algorithm + ": min = " + min + ", time = " + time);
    }
}