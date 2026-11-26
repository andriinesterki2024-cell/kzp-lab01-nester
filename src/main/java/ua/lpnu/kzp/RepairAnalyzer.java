package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.List;

/**
 * Перетворює рядки CSV на об'єкти {@link RepairOrder} і обчислює показники варіанта 16.
 *
 * <p>Розбір і перевірка рядка повністю делеговані {@link RepairOrder#fromCsv(String)};
 * цей клас лише збирає коректні записи в список, фіксує помилки з номерами рядків
 * і рахує показники за списком об'єктів.</p>
 */
public final class RepairAnalyzer {

    /* Забороняє створення екземплярів службового класу. */
    private RepairAnalyzer() {
    }

    /**
     * Обробляє всі рядки: коректні перетворює на {@link RepairOrder}, хибні пропускає з поясненням.
     *
     * @param lines рядки вхідного файла
     * @return підсумкові показники та перелік помилок
     */
    public static RepairSummary analyze(List<String> lines) {
        ParsedOrders parsed = parse(lines);
        return summarize(parsed.orders(), parsed.errors());
    }

    /**
     * Перетворює рядки на заявки; хибні рядки потрапляють до списку помилок із номером.
     *
     * @param lines рядки вхідного файла
     * @return коректні заявки різних підтипів і повідомлення про пропущені рядки
     */
    public static ParsedOrders parse(List<String> lines) {
        List<RepairOrder> repairs = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (int index = 0; index < lines.size(); index++) {
            try {
                // До списку потрапляє лише об'єкт, що пройшов усі перевірки конструктора.
                repairs.add(RepairOrder.fromCsv(lines.get(index)));
            } catch (IllegalArgumentException e) {
                // Помилка одного рядка не зупиняє обробку файла.
                errors.add(String.format("Рядок %d: %s", index + 1, e.getMessage()));
            }
        }
        return new ParsedOrders(repairs, errors);
    }

    /**
     * Обчислює показники за списком коректних записів.
     *
     * @param repairs коректні записи
     * @param errors  повідомлення про пропущені рядки
     * @return кількість, сумарний час, загальний виторг і найдовший ремонт
     */
    public static RepairSummary summarize(List<RepairOrder> repairs, List<String> errors) {
        double totalHours = 0.0;
        double totalRevenue = 0.0;
        RepairOrder longest = null;
        for (RepairOrder repair : repairs) {
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
