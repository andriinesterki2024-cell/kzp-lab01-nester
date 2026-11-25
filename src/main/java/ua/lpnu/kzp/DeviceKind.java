package ua.lpnu.kzp;

import java.util.Locale;

/**
 * Фіксована множина типів пристроїв, які приймає майстерня.
 *
 * <p>Тип визначається за першим словом назви пристрою з CSV (наприклад,
 * «Ноутбук Lenovo IdeaPad» → {@link #LAPTOP}). Значення {@link #OTHER} потрібне
 * для сумісності: попередні роботи приймали будь-яку непорожню назву пристрою.</p>
 */
public enum DeviceKind {
    /** Ноутбук. */
    LAPTOP("Ноутбук", false),
    /** Смартфон. */
    PHONE("Смартфон", false),
    /** Планшет. */
    TABLET("Планшет", false),
    /** Принтер. */
    PRINTER("Принтер", false),
    /** Ігрова консоль. */
    CONSOLE("Ігрова консоль", false),
    /** Навушники. */
    HEADPHONES("Навушники", false),
    /** Телевізор — великогабаритна техніка, ремонт із виїздом майстра. */
    TV("Телевізор", true),
    /** Пральна машина — побутова техніка. */
    WASHER("Пральна машина", true),
    /** Мікрохвильова піч — побутова техніка. */
    MICROWAVE("Мікрохвильова піч", true),
    /** Холодильник — побутова техніка. */
    FRIDGE("Холодильник", true),
    /** Пристрій, якого немає в довіднику. */
    OTHER("Інше", false);

    private final String label;
    private final boolean appliance;

    DeviceKind(String label, boolean appliance) {
        this.label = label;
        this.appliance = appliance;
    }

    /**
     * Повертає українську назву типу.
     *
     * @return назва для звіту
     */
    public String label() {
        return label;
    }

    /**
     * Чи є пристрій побутовою (великогабаритною) технікою, яку ремонтують із виїздом.
     *
     * @return {@code true} для побутової техніки
     */
    public boolean isAppliance() {
        return appliance;
    }

    /**
     * Визначає тип за назвою пристрою без урахування регістру та зайвих пробілів.
     *
     * @param device назва пристрою, наприклад «Пральна машина Bosch»
     * @return відповідний тип або {@link #OTHER}, якщо назва невідома
     */
    public static DeviceKind fromDevice(String device) {
        if (device == null) {
            return OTHER;
        }
        String normalized = device.strip().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        for (DeviceKind kind : values()) {
            if (kind != OTHER && normalized.startsWith(kind.label.toLowerCase(Locale.ROOT))) {
                return kind;
            }
        }
        return OTHER;
    }
}
