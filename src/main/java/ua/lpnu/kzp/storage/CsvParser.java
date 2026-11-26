package ua.lpnu.kzp.storage;

import java.util.ArrayList;
import java.util.List;

/**
 * Розбір CSV машиною станів: усередині лапок кома й перенесення рядка є даними,
 * поза лапками — роздільниками. На відміну від {@code split(",")}, правильно обробляє
 * поле {@code "Петренко, Іван"} і подвоєні лапки.
 */
public final class CsvParser {

    /* Забороняє створення екземплярів службового класу. */
    private CsvParser() {
    }

    /**
     * Розбирає весь документ на записи (не фізичні рядки). Приймає {@code \n}, {@code \r\n} і {@code \r}.
     * Порожній текст дає порожній список; останній роздільник рядка не створює порожнього запису.
     *
     * @param text вміст CSV
     * @return список записів, кожен — список полів
     * @throws DataStorageException для незакритих лапок або тексту після закриваючих лапок
     */
    public static List<List<String>> parseDocument(String text) throws DataStorageException {
        List<List<String>> records = new ArrayList<>();
        List<String> record = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        boolean afterQuote = false;
        boolean recordStarted = false;
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                        afterQuote = true;
                    }
                } else {
                    field.append(c);
                }
            } else if (c == ',') {
                record.add(field.toString());
                field.setLength(0);
                afterQuote = false;
                recordStarted = true;
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }
                record.add(field.toString());
                records.add(record);
                record = new ArrayList<>();
                field.setLength(0);
                afterQuote = false;
                recordStarted = false;
            } else if (afterQuote) {
                throw new DataStorageException(String.format(
                        "CSV-запис %d: текст після закриваючих лапок", records.size() + 1));
            } else if (c == '"' && field.length() == 0) {
                inQuotes = true;
                recordStarted = true;
            } else {
                field.append(c);
                recordStarted = true;
            }
            i++;
        }
        if (inQuotes) {
            throw new DataStorageException(String.format(
                    "CSV-запис %d: незакриті лапки", records.size() + 1));
        }
        if (recordStarted || field.length() > 0 || !record.isEmpty()) {
            record.add(field.toString());
            records.add(record);
        }
        return records;
    }
}
