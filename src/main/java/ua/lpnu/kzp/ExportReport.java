package ua.lpnu.kzp;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import ua.lpnu.kzp.storage.AnnotatedFields;
import ua.lpnu.kzp.storage.CsvExporter;
import ua.lpnu.kzp.storage.DataStorageException;
import ua.lpnu.kzp.storage.JsonExporter;
import ua.lpnu.kzp.storage.RepairOrderCsvReader;
import ua.lpnu.kzp.storage.RepairOrderRecord;
import ua.lpnu.kzp.storage.Repository;

/**
 * Експортує заявки у CSV/JSON, читає CSV назад і повідомляє результат round-trip.
 */
public final class ExportReport {

    /* Забороняє створення екземплярів службового класу. */
    private ExportReport() {
    }

    /**
     * Перетворює заявки будь-яких підтипів на записи й складає їх у сховище.
     *
     * @param orders заявки
     * @return сховище записів
     */
    public static Repository<RepairOrderRecord> toRecords(List<RepairOrder> orders) {
        Repository<RepairOrderRecord> records = new Repository<>();
        orders.forEach(order -> records.add(RepairOrderRecord.fromOrder(order)));
        return records;
    }

    /**
     * Виконує експорт і round-trip; помилки не зупиняють програму, а повідомляються з першопричиною.
     *
     * @param orders  заявки
     * @param csv     шлях CSV або {@code null}
     * @param json    шлях JSON або {@code null}
     * @return текст блоку для консолі
     */
    public static String run(List<RepairOrder> orders, Path csv, Path json) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "--- CSV-експорт ---%n"));
        List<RepairOrderRecord> records = toRecords(orders).all();
        try {
            if (csv != null) {
                CsvExporter.write(csv, RepairOrderRecord.class, records);
                List<RepairOrderRecord> restored = RepairOrderCsvReader.read(csv);
                sb.append(String.format(Locale.ROOT, "CSV збережено: %s%n", csv));
                sb.append(String.format(Locale.ROOT, "Заголовок: %s%n",
                        String.join(",", AnnotatedFields.columns(RepairOrderRecord.class))));
                sb.append(String.format(Locale.ROOT, "Експортовано записів: %d%n", records.size()));
                sb.append(String.format(Locale.ROOT, "Відновлено записів: %d%n", restored.size()));
                sb.append(String.format(Locale.ROOT, "Round-trip рівність: %b%n", restored.equals(records)));
            }
            if (json != null) {
                JsonExporter.write(json, RepairOrderRecord.class, records);
                sb.append(String.format(Locale.ROOT, "JSON збережено: %s%n", json));
            }
        } catch (DataStorageException e) {
            sb.append(String.format(Locale.ROOT, "Помилка експорту: %s%n", e.getMessage()));
            if (e.getCause() != null) {
                sb.append(String.format(Locale.ROOT, "Причина: %s%n", e.getCause()));
            }
        }
        return sb.toString();
    }
}
