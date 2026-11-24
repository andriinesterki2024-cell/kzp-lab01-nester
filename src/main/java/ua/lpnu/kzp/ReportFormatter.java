package ua.lpnu.kzp;

import java.util.Locale;

/** Формує текст звіту; той самий текст виводиться в консоль і записується у файл. */
public final class ReportFormatter {

    /* Забороняє створення екземплярів службового класу. */
    private ReportFormatter() {
    }

    /**
     * Форматує показники у таблицю з фіксованою кількістю знаків після крапки.
     *
     * @param s обчислені показники
     * @return готовий текст звіту з платформним роздільником рядків
     */
    public static String format(RepairSummary s) {
        StringBuilder sb = new StringBuilder();
        // Locale.ROOT гарантує крапку як десятковий роздільник на будь-якій ОС.
        sb.append(String.format(Locale.ROOT, "=== Звіт ремонтної майстерні ===%n"));
        sb.append(String.format(Locale.ROOT, "%-32s %12s%n", "Показник", "Значення"));
        sb.append(String.format(Locale.ROOT, "%-32s %12d%n",
                "Кількість коректних записів", s.validCount()));
        sb.append(String.format(Locale.ROOT, "%-32s %12.2f%n",
                "Сумарний час ремонтів, год", s.totalHours()));
        sb.append(String.format(Locale.ROOT, "%-32s %12.2f%n",
                "Загальний виторг, грн", s.totalRevenue()));
        if (s.validCount() > 0) {
            sb.append(String.format(Locale.ROOT, "%-32s %12.2f%n",
                    "Найдовший ремонт, год", s.longestHours()));
            sb.append(String.format(Locale.ROOT, "  (клієнт: %s; пристрій: %s)%n",
                    s.longestClient(), s.longestDevice()));
        } else {
            sb.append(String.format(Locale.ROOT, "Коректних записів не знайдено%n"));
        }
        sb.append(String.format(Locale.ROOT, "Пропущено рядків: %d%n", s.errors().size()));
        for (String error : s.errors()) {
            sb.append(String.format(Locale.ROOT, "  %s%n", error));
        }
        return sb.toString();
    }
}
