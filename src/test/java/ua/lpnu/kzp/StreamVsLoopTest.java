package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Порівнює потокові запити з еталонними циклами на трьох наборах даних. */
class StreamVsLoopTest {

    private static List<RepairOrder> load(String file) throws IOException {
        return RepairAnalyzer.parse(FileReport.readLines(Path.of("data", file))).orders();
    }

    @ParameterizedTest
    @ValueSource(strings = {"input.csv", "input-lab03.csv", "input-lab04.csv"})
    void filterMatchesLoop(String file) throws IOException {
        List<RepairOrder> orders = load(file);
        assertEquals(LoopReference.openOrders(orders), RepairQueries.openOrders(orders));
    }

    @ParameterizedTest
    @ValueSource(strings = {"input.csv", "input-lab03.csv", "input-lab04.csv"})
    void mapMatchesLoop(String file) throws IOException {
        List<RepairOrder> orders = load(file);
        assertEquals(LoopReference.devices(orders), RepairQueries.devices(orders));
    }

    @ParameterizedTest
    @ValueSource(strings = {"input.csv", "input-lab03.csv", "input-lab04.csv"})
    void groupingMatchesLoop(String file) throws IOException {
        List<RepairOrder> orders = load(file);
        assertEquals(LoopReference.costByKind(orders), RepairQueries.costByKind(orders));
    }

    @ParameterizedTest
    @ValueSource(strings = {"input.csv", "input-lab03.csv", "input-lab04.csv"})
    void statisticsMatchLoop(String file) throws IOException {
        List<RepairOrder> orders = load(file);
        double[] loop = LoopReference.costStatistics(orders);
        DoubleSummaryStatistics stream = RepairQueries.costStatistics(orders);
        assertEquals(loop[0], stream.getCount());
        assertEquals(loop[1], stream.getMin(), 1e-9);
        assertEquals(loop[2], stream.getMax(), 1e-9);
        assertEquals(loop[3], stream.getSum(), 1e-9);
    }

    @ParameterizedTest
    @ValueSource(strings = {"input.csv", "input-lab03.csv", "input-lab04.csv"})
    void topNMatchesLoopForEveryN(String file) throws IOException {
        List<RepairOrder> orders = load(file);
        for (int n = 0; n <= orders.size() + 1; n++) {
            assertEquals(LoopReference.mostExpensive(orders, n), RepairQueries.mostExpensive(orders, n));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"Коваль Оксана", "Шевчук Андрій", "Іваненко Петро", "Немає Такого"})
    void searchMatchesLoop(String client) throws IOException {
        List<RepairOrder> orders = load("input-lab04.csv");
        assertEquals(LoopReference.findByClient(orders, client), RepairQueries.findByClient(orders, client));
    }
}
