package ua.lpnu.kzp;

import java.util.Locale;

/**
 * Вид заявки на ремонт (enum варіанта 16 з таблиці ЛР3: {@code RepairKind}).
 *
 * <p>Множина видів фіксована: гарантійний ремонт і платний ремонт. Кожна константа має
 * підпис для звіту. Значення порівнюються через {@code ==}, бо кожна константа існує
 * в одному екземплярі.</p>
 */
public enum RepairKind {

    /** Гарантійний ремонт: вартість для клієнта покриває гарантія. */
    WARRANTY("гарантійний"),

    /** Платний ремонт: клієнт сплачує вартість робіт (зі знижкою за правилом). */
    PAID("платний");

    private final String label;

    /* Конструктор enum викликається JVM один раз для кожної константи. */
    RepairKind(String label) {
        this.label = label;
    }

    /**
     * Повертає підпис для звіту.
     *
     * @return український підпис виду
     */
    public String label() {
        return label;
    }

    /**
     * Розбирає вид із поля CSV: назва константи без урахування регістру
     * ({@code WARRANTY}, {@code paid}) або український підпис ({@code гарантійний}).
     *
     * @param text текст поля
     * @return вид заявки
     * @throws IllegalArgumentException якщо текст не відповідає жодному виду
     */
    public static RepairKind parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("порожнє поле kind (вид)");
        }
        String value = text.strip();
        for (RepairKind kind : values()) {
            if (kind.name().equalsIgnoreCase(value) || kind.label.equalsIgnoreCase(value)) {
                return kind;
            }
        }
        throw new IllegalArgumentException(String.format(Locale.ROOT,
                "невідомий вид ремонту \"%s\" (очікується WARRANTY або PAID)", value));
    }
}
