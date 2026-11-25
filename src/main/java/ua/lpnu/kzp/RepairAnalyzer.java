package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.List;

/**
 * Перетворює рядки CSV на об'єкти {@link Repair} і обчислює показники варіанта 16.
 *
 * <p>Розбір і перевірка рядка повністю делеговані {@link Repair#fromCsv(String)};
 * цей клас лише збирає коректні записи в список, фіксує помилки з номерами рядків
 * і рахує показники за списком об'єктів.</p>
 */
public final class RepairAnalyzer {

    /* Забороняє створення екземплярів службового класу. */
    private RepairAnalyzer() {
    }

    /**
     * Обробляє всі рядки: коректні перетворює на {@link Repair}, хибні пропускає з поясненням.
     *
     * @param lines рядки вхідного файла
     * @return підсумкові показники та перелік помилок
     */
    public static RepairSummary analyze(List<String> lines) {
        ParsedRepairs parsed = parse(lines);
        return summarize(parsed.repairs(), parsed.errors());
    }

    /**
     * Перетворює рядки на записи; хибні рядки потрапляють до списку помилок із номером.
     *
     * @param lines рядки вхідного файла
     * @return коректні записи різних підтипів і повідомлення про пропущені рядки
     */
    public static ParsedRepairs parse(List<String> lines) {
        List<Repair> repairs = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (int index = 0; index < lines.size(); index++) {
            try {
                // До списку потрапляє лише об'єкт, що пройшов усі перевірки конструктора.
                repairs.add(Repair.fromCsv(lines.get(index)));
            } catch (IllegalArgumentException e) {
                // Помилка одного рядка не зупиняє обробку файла.
                errors.add(String.format("Рядок %d: %s", index + 1, e.getMessage()));
            }
        }
        return new ParsedRepairs(repairs, errors);
    }

    /**
     * Обчислює показники за списком коректних записів.
     *
     * @param repairs коректні записи
     * @param errors  повідомлення про пропущені рядки
     * @return кількість, сумарний час, загальний виторг і найдовший ремонт
     */
    public static RepairSummary summarize(List<Repair> repairs, List<String> errors) {
        double totalHours = 0.0;
        double totalRevenue = 0.0;
        Repair longest = null;
        for (Repair repair : repairs) {
            totalHours += repair.hours();
            totalRevenue += repair.price();
            // Строга нерівність: при однаковій тривалості лишається перший запис.
            if (longest == null || repair.hours() > longest.hours()) {
                longest = repair;
            }
        }
        return new RepairSummary(repairs.size(), totalHours, totalRevenue,
                longest == null ? 0.0 : longest.hours(),
                longest == null ? "" : longest.client(),
                longest == null ? "" : longest.device(),
                errors);
    }
}
