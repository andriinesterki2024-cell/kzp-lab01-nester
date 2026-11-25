package ua.lpnu.kzp;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import ua.lpnu.kzp.storage.AnnotatedFields;
import ua.lpnu.kzp.storage.CsvExporter;
import ua.lpnu.kzp.storage.DataStorageException;
import ua.lpnu.kzp.storage.RepairRecord;

/**
 * Порівнює час пошуку анотованих полів без кешу і з кешем (завдання із зірочкою).
 *
 * <p>Запуск: {@code java -cp target/lab01-5.0.0.jar ua.lpnu.kzp.ExportTiming}.
 * Вимірювання орієнтовне: спершу виконується прогрів JIT, далі береться середнє на одну операцію.</p>
 */
public final class ExportTiming {

    /** Кількість повторів пошуку полів. */
    static final int LOOKUPS = 200_000;

    /** Кількість записів у тестовому експорті. */
    static final int RECORDS = 1_000;

    /* Забороняє створення екземплярів службового класу. */
    private ExportTiming() {
    }

    /**
     * Результат вимірювання в наносекундах на операцію.
     *
     * @param scanNanos  пошук полів рефлексією без кешу
     * @param cacheNanos пошук полів із кешем
     * @param exportNanos експорт {@value #RECORDS} записів у CSV (з кешем)
     */
    record Result(double scanNanos, double cacheNanos, double exportNanos) {
    }

    /**
     * Вимірює час.
     *
     * @return середні значення на операцію
     * @throws DataStorageException якщо експорт не вдався
     */
    static Result measure() throws DataStorageException {
        List<RepairRecord> records = new ArrayList<>();
        for (int i = 1; i <= RECORDS; i++) {
            records.add(new RepairRecord(i, "Клієнт " + i, "Пристрій, модель " + i, DeviceKind.OTHER,
                    LocalDate.of(2026, 1, 1).plusDays(i % 365), 1.5, 100.0 + i));
        }
        int sink = 0;
        for (int i = 0; i < LOOKUPS / 10; i++) { // прогрів
            sink += AnnotatedFields.scan(RepairRecord.class).size() + AnnotatedFields.of(RepairRecord.class).size();
        }
        long start = System.nanoTime();
        for (int i = 0; i < LOOKUPS; i++) {
            sink += AnnotatedFields.scan(RepairRecord.class).size();
        }
        double scan = (System.nanoTime() - start) / (double) LOOKUPS;
        start = System.nanoTime();
        for (int i = 0; i < LOOKUPS; i++) {
            sink += AnnotatedFields.of(RepairRecord.class).size();
        }
        double cache = (System.nanoTime() - start) / (double) LOOKUPS;
        int repeats = 50;
        start = System.nanoTime();
        for (int i = 0; i < repeats; i++) {
            sink += CsvExporter.toCsv(RepairRecord.class, records).length();
        }
        double export = (System.nanoTime() - start) / (double) repeats;
        if (sink == 42) { // не дає JIT викинути цикли як мертвий код
            throw new IllegalStateException();
        }
        return new Result(scan, cache, export);
    }

    /**
     * Друкує результати вимірювання.
     *
     * @param args не використовуються
     * @throws DataStorageException якщо експорт не вдався
     */
    public static void main(String[] args) throws DataStorageException {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        Result r = measure();
        out.printf("Пошук полів без кешу (scan):  %10.1f нс%n", r.scanNanos());
        out.printf("Пошук полів із кешем (of):     %10.1f нс%n", r.cacheNanos());
        out.printf("Прискорення пошуку полів:      %10.1f разів%n", r.scanNanos() / r.cacheNanos());
        out.printf("Експорт %d записів у CSV:    %10.3f мс%n", RECORDS, r.exportNanos() / 1_000_000);
    }
}
