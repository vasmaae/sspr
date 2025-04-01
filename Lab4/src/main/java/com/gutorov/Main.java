package com.gutorov;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Конфигурация Ignite для клиентского узла
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setClientMode(true); // Режим клиента

        TcpDiscoverySpi spi = new TcpDiscoverySpi();
        TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
        ipFinder.setAddresses(Arrays.asList("192.168.1.101:47500", "192.168.1.102:47500"));
        spi.setIpFinder(ipFinder);
        cfg.setDiscoverySpi(spi);

        try (Ignite ignite = Ignition.start(cfg)) {
            // Пример матрицы (4x4)
            List<List<Integer>> matrix = new ArrayList<>();
            matrix.add(Arrays.asList(1, 2, 3, 4));
            matrix.add(Arrays.asList(5, 6, 7, 8));
            matrix.add(Arrays.asList(9, 10, 11, 12));
            matrix.add(Arrays.asList(13, 14, 15, 16));

            int n = matrix.size(); // Размер матрицы
            List<ClusterNode> nodes = new ArrayList<>(ignite.cluster().forServers().nodes());
            int nodesCount = nodes.size();

            if (nodesCount == 0) {
                System.out.println("Нет доступных серверных узлов!");
                return;
            }

            long startTime = System.currentTimeMillis();

            // Разделение матрицы по строкам между узлами
            int rowsPerNode = n / nodesCount;
            List<IgniteCallable<Integer>> tasks = new ArrayList<>();

            for (int i = 0; i < nodesCount; i++) {
                int startRow = i * rowsPerNode;
                int endRow = (i == nodesCount - 1) ? n : (i + 1) * rowsPerNode;
                List<List<Integer>> subMatrix = matrix.subList(startRow, endRow);

                // Создание задачи для каждой подматрицы
                tasks.add(new MatrixMinTask(subMatrix, startRow, n));
            }

            // Выполнение всех задач на серверных узлах
            List<Integer> results = (List<Integer>) ignite.compute().call(tasks);

            // Нахождение общего минимума
            List<Integer> validResults = new ArrayList<>();
            for (Integer result : results) {
                if (result != null) {
                    validResults.add(result);
                }
            }
            int globalMin = validResults.isEmpty() ? -1 : Collections.min(validResults);

            long endTime = System.currentTimeMillis();
            System.out.println("Минимальный элемент выше главной диагонали: " + globalMin);
            System.out.println("Время выполнения: " + (endTime - startTime) + " мс");
        }
    }
}