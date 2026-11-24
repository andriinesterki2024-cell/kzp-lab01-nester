package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.List;

/**
 * Перевіряє рядки CSV про ремонти та обчислює показники варіанта 16.
 *
 * <p>Формат рядка: {@code client;device;diagnosis;hours;price}.
 * Хибний рядок не зупиняє обробку: у список помилок додається номер рядка та причина.</p>
 */
public final class RepairAnalyzer {

    /** Кількість полів у коректному записі. */
    static final int FIELD_COUNT = 5;

    /** Індекси полів запису. */
    static final int CLIENT = 0;
    static final int DEVICE = 1;
    static final int DIAGNOSIS = 2;
    static final int HOURS = 3;
    static final int PRICE = 4;

    /* Забороняє створення екземплярів службового класу. */
    private RepairAnalyzer() {
    }

    /**
     * Обробляє всі рядки: перевіряє кожен і включає в показники лише коректні.
     *
     * @param lines рядки вхідного файла
     * @return підсумкові показники та перелік помилок
     */
    public static RepairSummary analyze(List<String> lines) {
        List<String> errors = new ArrayList<>();
        int validCount = 0;
        double totalHours = 0.0;
        double totalRevenue = 0.0;
        double longestHours = 0.0;
        String longestClient = "";
        String longestDevice = "";

        for (int index = 0; index < lines.size(); index++) {
            int lineNumber = index + 1;
            String[] fields;
            try {
                fields = parseLine(lines.get(index));
            } catch (IllegalArgumentException e) {
                // Повідомлення містить номер рядка та причину, обробка триває.
                errors.add(String.format("Рядок %d: %s", lineNumber, e.getMessage()));
                continue;
            }

            double hours = Double.parseDouble(fields[HOURS]);
            double price = Double.parseDouble(fields[PRICE]);

            // До статистики потрапляють лише записи, що пройшли всі перевірки.
            validCount++;
            totalHours += hours;
            totalRevenue += price;
            // Строга нерівність: при однаковій тривалості лишається перший запис.
            if (hours > longestHours) {
                longestHours = hours;
                longestClient = fields[CLIENT];
                longestDevice = fields[DEVICE];
            }
        }

        return new RepairSummary(validCount, totalHours, totalRevenue,
                longestHours, longestClient, longestDevice, errors);
    }

    /**
     * Розбирає й перевіряє один рядок CSV.
     *
     * @param line рядок файла
     * @return масив із п'яти обрізаних полів
     * @throws IllegalArgumentException з поясненням причини, якщо рядок некоректний
     */
    static String[] parseLine(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("порожній рядок");
        }
        // -1 зберігає порожні поля в кінці рядка, щоб "a;b;c;1;" дало 5 полів.
        String[] fields = line.split(";", -1);
        if (fields.length != FIELD_COUNT) {
            throw new IllegalArgumentException(String.format(
                    "очікується %d полів, отримано %d", FIELD_COUNT, fields.length));
        }
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].strip();
        }
        if (fields[CLIENT].isEmpty()) {
            throw new IllegalArgumentException("порожнє поле client (клієнт)");
        }
        if (fields[DEVICE].isEmpty()) {
            throw new IllegalArgumentException("порожнє поле device (пристрій)");
        }
        if (fields[DIAGNOSIS].isEmpty()) {
            throw new IllegalArgumentException("порожнє поле diagnosis (діагноз)");
        }

        double hours = parseNumber(fields[HOURS], "hours");
        double price = parseNumber(fields[PRICE], "price");
        if (hours <= 0.0) {
            throw new IllegalArgumentException("hours має бути більшим за 0");
        }
        if (price < 0.0) {
            throw new IllegalArgumentException("price не може бути від'ємним");
        }
        return fields;
    }

    /**
     * Перетворює текст на число.
     *
     * @param text      значення поля
     * @param fieldName назва поля для повідомлення
     * @return число
     * @throws IllegalArgumentException якщо значення нечислове
     */
    private static double parseNumber(String text, String fieldName) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format(
                    "поле %s має нечислове значення \"%s\"", fieldName, text), e);
        }
    }
}
