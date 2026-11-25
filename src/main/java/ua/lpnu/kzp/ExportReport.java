package ua.lpnu.kzp;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import ua.lpnu.kzp.storage.AnnotatedFields;
import ua.lpnu.kzp.storage.CsvExporter;
import ua.lpnu.kzp.storage.DataStorageException;
import ua.lpnu.kzp.storage.JsonExporter;
import ua.lpnu.kzp.storage.RepairCsvReader;
import ua.lpnu.kzp.storage.RepairRecord;
import ua.lpnu.kzp.storage.Repository;

/**
 * Експортує ремонти у CSV/JSON, читає CSV назад і повідомляє результат round-trip.
 */
public final class ExportReport {

    /** Дата прийому першої квитанції; кожна наступна — на день пізніше. */
    public static final LocalDate FIRST_ACCEPTED_ON = LocalDate.of(2026, 1, 1);

    /* Забороняє створення екземплярів службового класу. */
    private ExportReport() {
    }

    /**
     * Перетворює ремонти на записи квитанцій: номер — порядковий, дата — від {@link #FIRST_ACCEPTED_ON}.
     *
     * @param repairs ремонти будь-яких підтипів
     * @return сховище записів
     */
    public static Repository<RepairRecord> toRecords(List<Repair> repairs) {
        Repository<RepairRecord> records = new Repository<>();
        for (int i = 0; i < repairs.size(); i++) {
            records.add(RepairRecord.fromRepair(repairs.get(i), i + 1, FIRST_ACCEPTED_ON.plusDays(i)));
        }
        return records;
    }

    /**
     * Виконує експорт і round-trip; помилки не зупиняють програму, а повідомляються з першопричиною.
     *
     * @param repairs ремонти
     * @param csv     шлях CSV або {@code null}
     * @param json    шлях JSON або {@code null}
     * @return текст блоку для консолі
     */
    public static String run(List<Repair> repairs, Path csv, Path json) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "--- CSV-експорт ---%n"));
        List<RepairRecord> records = toRecords(repairs).all();
        try {
            if (csv != null) {
                CsvExporter.write(csv, RepairRecord.class, records);
                List<RepairRecord> restored = RepairCsvReader.read(csv);
                sb.append(String.format(Locale.ROOT, "CSV збережено: %s%n", csv));
                sb.append(String.format(Locale.ROOT, "Заголовок: %s%n",
                        String.join(",", AnnotatedFields.columns(RepairRecord.class))));
                sb.append(String.format(Locale.ROOT, "Експортовано записів: %d%n", records.size()));
                sb.append(String.format(Locale.ROOT, "Відновлено записів: %d%n", restored.size()));
                sb.append(String.format(Locale.ROOT, "Round-trip рівність: %b%n", restored.equals(records)));
            }
            if (json != null) {
                JsonExporter.write(json, RepairRecord.class, records);
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
