package ua.lpnu.kzp;

import java.util.List;

/**
 * Незмінний набір обчислених показників варіанта 16.
 *
 * @param validCount    кількість коректних записів
 * @param totalHours    сумарний час ремонтів, год
 * @param totalRevenue  загальний виторг, грн
 * @param longestHours  тривалість найдовшого ремонту, год
 * @param longestClient клієнт найдовшого ремонту
 * @param longestDevice пристрій найдовшого ремонту
 * @param errors        повідомлення про пропущені рядки
 */
public record RepairSummary(
        int validCount,
        double totalHours,
        double totalRevenue,
        double longestHours,
        String longestClient,
        String longestDevice,
        List<String> errors) {

    /**
     * Створює копію списку помилок, щоб зовнішній код не міг його змінити.
     */
    public RepairSummary {
        errors = List.copyOf(errors);
    }
}
