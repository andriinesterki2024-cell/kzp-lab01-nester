package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * Незмінний запис про ремонт у майстерні (варіант 16).
 *
 * <p>Стан зберігається в {@code private final} полях і не має сеттерів, тому після
 * успішного створення об'єкт завжди задовольняє інваріанти:</p>
 * <ul>
 *   <li>{@code client}, {@code device}, {@code diagnosis} — не {@code null} і не порожні;</li>
 *   <li>{@code hours} — скінченне і більше за 0;</li>
 *   <li>{@code price} — скінченна і не менша за 0.</li>
 * </ul>
 * <p>Числові правила делеговано record {@link WorkCost}, текстові — методу
 * {@link #requireText(String, String)}; і конструктор, і {@link #fromCsv(String)},
 * і {@link Builder} використовують ті самі перевірки.</p>
 */
public final class Repair {

    /** Кількість полів у рядку CSV. */
    public static final int FIELD_COUNT = 5;

    private final String client;
    private final String device;
    private final String diagnosis;
    private final double hours;
    private final double price;

    /**
     * Створює запис про ремонт і перевіряє всі інваріанти.
     *
     * @param client    клієнт, непорожній
     * @param device    пристрій, непорожній
     * @param diagnosis діагноз або вид робіт, непорожній
     * @param hours     тривалість ремонту, год; скінченна і більша за 0
     * @param price     вартість, грн; скінченна і не менша за 0
     * @throws IllegalArgumentException якщо будь-яке значення порушує інваріант
     */
    public Repair(String client, String device, String diagnosis, double hours, double price) {
        this.client = requireText(client, "client (клієнт)");
        this.device = requireText(device, "device (пристрій)");
        this.diagnosis = requireText(diagnosis, "diagnosis (діагноз)");
        // Конструктор record кидає IllegalArgumentException для хибних чисел.
        WorkCost cost = new WorkCost(hours, price);
        this.hours = cost.hours();
        this.price = cost.price();
    }

    /**
     * Створює запис з одного рядка CSV формату {@code client;device;diagnosis;hours;price}.
     *
     * <p>Порядок перевірок збігається з лабораторною роботою № 1: порожній рядок,
     * кількість полів, текстові поля, числовий формат, діапазон чисел. Тому
     * повідомлення про помилки у звіті не змінилися.</p>
     *
     * @param line рядок файла
     * @return коректний запис
     * @throws IllegalArgumentException з поясненням причини, якщо рядок некоректний
     */
    public static Repair fromCsv(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("порожній рядок");
        }
        // -1 зберігає порожні поля в кінці рядка, щоб "a;b;c;1;" дало 5 полів.
        String[] fields = line.split(";", -1);
        if (fields.length != FIELD_COUNT) {
            throw new IllegalArgumentException(String.format(
                    "очікується %d полів, отримано %d", FIELD_COUNT, fields.length));
        }
        // Текстові поля перевіряються до чисел, як і в попередній роботі.
        String client = requireText(fields[0], "client (клієнт)");
        String device = requireText(fields[1], "device (пристрій)");
        String diagnosis = requireText(fields[2], "diagnosis (діагноз)");
        double hours = parseNumber(fields[3], "hours");
        double price = parseNumber(fields[4], "price");
        return new Repair(client, device, diagnosis, hours, price);
    }

    /**
     * Повертає новий будівник для покрокового створення запису.
     *
     * @return порожній будівник
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Повертає клієнта.
     *
     * @return ім'я клієнта
     */
    public String client() {
        return client;
    }

    /**
     * Повертає пристрій.
     *
     * @return назва пристрою
     */
    public String device() {
        return device;
    }

    /**
     * Повертає діагноз.
     *
     * @return діагноз або вид робіт
     */
    public String diagnosis() {
        return diagnosis;
    }

    /**
     * Повертає тривалість ремонту.
     *
     * @return години, більше за 0
     */
    public double hours() {
        return hours;
    }

    /**
     * Повертає вартість ремонту.
     *
     * @return гривні, не менше за 0
     */
    public double price() {
        return price;
    }

    /**
     * Повертає тривалість і вартість як незмінне значення.
     *
     * @return record {@link WorkCost}
     */
    public WorkCost cost() {
        return new WorkCost(hours, price);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Repair that)) {
            return false;
        }
        return Double.compare(hours, that.hours) == 0
                && Double.compare(price, that.price) == 0
                && client.equals(that.client)
                && device.equals(that.device)
                && diagnosis.equals(that.diagnosis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(client, device, diagnosis, hours, price);
    }

    /**
     * Повертає зрозуміле подання з двома знаками після крапки (Locale.ROOT).
     *
     * @return рядок на зразок {@code Repair[client=Бондар Марія, device=..., hours=4.50, price=2750.00]}
     */
    @Override
    public String toString() {
        return String.format(Locale.ROOT,
                "Repair[client=%s, device=%s, diagnosis=%s, hours=%.2f, price=%.2f]",
                client, device, diagnosis, hours, price);
    }

    /**
     * Перевіряє текстове поле: не {@code null} і не порожнє після обрізання пробілів.
     *
     * @param value     значення
     * @param fieldName назва поля для повідомлення
     * @return обрізане значення
     */
    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("порожнє поле " + fieldName);
        }
        return value.strip();
    }

    private static double parseNumber(String text, String fieldName) {
        String trimmed = text.strip();
        try {
            return Double.parseDouble(trimmed);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format(
                    "поле %s має нечислове значення \"%s\"", fieldName, trimmed), e);
        }
    }

    /**
     * Будівник для зручного створення {@link Repair}. Метод {@link #build()} викликає
     * конструктор, тому будівник не обходить жодної перевірки. Повторне використання
     * будівника не змінює раніше створених об'єктів, бо вони незмінні.
     */
    public static final class Builder {
        private String client;
        private String device;
        private String diagnosis;
        private double hours = Double.NaN;
        private double price = Double.NaN;

        private Builder() {
        }

        /**
         * Задає клієнта.
         *
         * @param value ім'я клієнта
         * @return цей будівник
         */
        public Builder client(String value) {
            this.client = value;
            return this;
        }

        /**
         * Задає пристрій.
         *
         * @param value назва пристрою
         * @return цей будівник
         */
        public Builder device(String value) {
            this.device = value;
            return this;
        }

        /**
         * Задає діагноз.
         *
         * @param value діагноз
         * @return цей будівник
         */
        public Builder diagnosis(String value) {
            this.diagnosis = value;
            return this;
        }

        /**
         * Задає тривалість ремонту.
         *
         * @param value години
         * @return цей будівник
         */
        public Builder hours(double value) {
            this.hours = value;
            return this;
        }

        /**
         * Задає вартість ремонту.
         *
         * @param value гривні
         * @return цей будівник
         */
        public Builder price(double value) {
            this.price = value;
            return this;
        }

        /**
         * Створює запис через конструктор {@link Repair}; незадані числа залишаються NaN
         * і відхиляються тією самою перевіркою.
         *
         * @return новий незмінний запис
         * @throws IllegalArgumentException якщо значення порушують інваріанти
         */
        public Repair build() {
            return new Repair(client, device, diagnosis, hours, price);
        }
    }
}
