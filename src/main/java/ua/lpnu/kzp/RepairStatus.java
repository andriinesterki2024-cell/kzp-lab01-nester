package ua.lpnu.kzp;

import java.util.Locale;

/**
 * Стан заявки на ремонт (лабораторна робота № 4: фільтр «заявка незавершена»).
 *
 * <p>Значення в CSV: {@code new}, {@code in_progress}, {@code done} (без урахування регістру)
 * або український підпис.</p>
 */
public enum RepairStatus {

    /** Заявку прийнято, ремонт ще не розпочато. */
    NEW("нова"),

    /** Ремонт виконується. */
    IN_PROGRESS("у роботі"),

    /** Ремонт завершено. */
    DONE("виконана");

    private final String label;

    /* Конструктор enum викликається JVM один раз для кожної константи. */
    RepairStatus(String label) {
        this.label = label;
    }

    /**
     * Повертає підпис для звіту.
     *
     * @return український підпис стану
     */
    public String label() {
        return label;
    }

    /**
     * Чи заявка незавершена (нова або в роботі).
     *
     * @return {@code true} для всіх станів, крім {@link #DONE}
     */
    public boolean isOpen() {
        return this != DONE;
    }

    /**
     * Розбирає стан із поля CSV.
     *
     * @param text {@code new}, {@code in_progress}, {@code done} або український підпис
     * @return стан
     * @throws IllegalArgumentException якщо текст не відповідає жодному стану
     */
    public static RepairStatus parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("порожнє поле status (стан)");
        }
        String value = text.strip();
        for (RepairStatus status : values()) {
            if (status.name().equalsIgnoreCase(value) || status.label.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException(String.format(Locale.ROOT,
                "невідомий стан заявки \"%s\" (очікується new, in_progress або done)", value));
    }
}
