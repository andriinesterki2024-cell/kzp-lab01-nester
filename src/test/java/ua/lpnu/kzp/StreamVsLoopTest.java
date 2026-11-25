package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** Доводить, що кожен потоковий запит дає той самий результат, що й цикл. */
class StreamVsLoopTest {

    static Stream<List<Repair>> datasets() throws IOException {
        return Stream.of("input.csv", "input-lab03.csv", "input-lab04.csv")
                .map(name -> {
                    try {
                        return RepairAnalyzer.parse(FileReport.readLines(Path.of("data", name))).repairs();
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                });
    }

    @ParameterizedTest
    @MethodSource("datasets")
    void filterMatchesLoop(List<Repair> data) {
        for (double threshold : new double[] {0, 650, 1000, 3200, 5000}) {
            assertEquals(LoopReference.expensive(data, threshold), RepairQueries.expensive(data, threshold));
        }
    }

    @ParameterizedTest
    @MethodSource("datasets")
    void mapMatchesLoop(List<Repair> data) {
        assertEquals(LoopReference.clientNames(data), RepairQueries.clientNames(data));
    }

    @ParameterizedTest
    @MethodSource("datasets")
    void groupingMatchesLoop(List<Repair> data) {
        assertEquals(LoopReference.countByDeviceKind(data), RepairQueries.countByDeviceKind(data));
    }

    @ParameterizedTest
    @MethodSource("datasets")
    void statisticsMatchLoop(List<Repair> data) {
        double[] loop = LoopReference.hoursMinMaxSumCount(data);
        DoubleSummaryStatistics stats = RepairQueries.hoursStatistics(data);
        assertEquals(loop[0], stats.getMin(), 1e-9);
        assertEquals(loop[1], stats.getMax(), 1e-9);
        assertEquals(loop[2], stats.getSum(), 1e-9);
        assertEquals((long) loop[3], stats.getCount());
    }

    @ParameterizedTest
    @MethodSource("datasets")
    void topNMatchesLoopForEveryN(List<Repair> data) {
        for (int n = 0; n <= data.size() + 1; n++) {
            assertEquals(LoopReference.mostExpensive(data, n), RepairQueries.mostExpensive(data, n));
        }
    }

    @ParameterizedTest
    @MethodSource("datasets")
    void searchMatchesLoop(List<Repair> data) {
        for (String client : List.of("Коваль Оксана", "бондар марія", "Невідомий")) {
            assertEquals(LoopReference.findByClient(data, client),
                    RepairQueries.findByClient(data, client).orElse(null));
        }
    }
}
