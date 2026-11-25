package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * Спільний абстрактний тип запису про ремонт у майстерні (варіант 16).
 *
 * <p>Ієрархія запечатана: дозволені лише підтипи {@link GadgetRepair} (ремонт у
 * майстерні) та {@link ApplianceRepair} (ремонт побутової техніки з виїздом майстра).
 * Спільні поля та інваріанти зосереджено тут і не дублюються в підтипах; підтипи
 * по-різному реалізують {@link #totalCost()}, {@link #warrantyDays()} і {@link #tierLabel()}.
 * Підтип обирає лише фабрика {@link #create}.</p>
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
public abstract sealed class Repair permits GadgetRepair, ApplianceRepair {

    /** Кількість полів у рядку CSV. */
    public static final int FIELD_COUNT = 5;

    private final String client;
    private final String device;
    private final String diagnosis;
    private final double hours;
    private final double price;
    private final DeviceKind kind;

    /**
     * Ініціалізує й перевіряє спільну частину запису для підтипу.
     *
     * @param client    клієнт, непорожній
     * @param device    пристрій, непорожній
     * @param diagnosis діагноз або вид робіт, непорожній
     * @param hours     тривалість ремонту, год; скінченна і більша за 0
     * @param price     вартість, грн; скінченна і не менша за 0
     * @throws IllegalArgumentException якщо будь-яке значення порушує інваріант
     */
    protected Repair(String client, String device, String diagnosis, double hours, double price) {
        // Усі перевірки виконуються під час обчислення аргументів, ще до створення
        // об'єкта, тож частково ініціалізований Repair не може з'явитися (CT_CONSTRUCTOR_THROW).
        this(requireText(client, "client (клієнт)"),
                requireText(device, "device (пристрій)"),
                requireText(diagnosis, "diagnosis (діагноз)"),
                new WorkCost(hours, price));
    }

    /* Присвоює вже перевірені значення; сам нічого не перевіряє й не кидає винятків. */
    private Repair(String client, String device, String diagnosis, WorkCost cost) {
        this.client = client;
        this.device = device;
        this.diagnosis = diagnosis;
        this.hours = cost.hours();
        this.price = cost.price();
        this.kind = DeviceKind.fromDevice(device);
    }

    /**
     * Фабрика: обирає підтип за типом пристрою. Це єдине місце, де враховується тип.
     *
     * @param client    клієнт
     * @param device    пристрій
     * @param diagnosis діагноз
     * @param hours     тривалість, год
     * @param price     вартість ремонту, грн
     * @return {@link ApplianceRepair} для побутової техніки, інакше {@link GadgetRepair}
     * @throws IllegalArgumentException якщо значення порушують інваріанти
     */
    public static Repair create(String client, String device, String diagnosis, double hours, double price) {
        return DeviceKind.fromDevice(device).isAppliance()
                ? new ApplianceRepair(client, device, diagnosis, hours, price, ApplianceRepair.DEFAULT_CALL_OUT_FEE)
                : new GadgetRepair(client, device, diagnosis, hours, price, GadgetRepair.DEFAULT_WARRANTY_DAYS);
    }

    /**
     * Повна вартість для клієнта за правилом підтипу.
     *
     * @return вартість, грн
     */
    public abstract double totalCost();

    /**
     * Гарантійний строк після ремонту за правилом підтипу.
     *
     * @return кількість днів
     */
    public abstract int warrantyDays();

    /**
     * Назва рівня обслуговування для поліморфних показників.
     *
     * @return підпис підтипу
     */
    public abstract String tierLabel();

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
        return create(client, device, diagnosis, hours, price);
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
     * Повертає тип пристрою, визначений за назвою.
     *
     * @return значення {@link DeviceKind}
     */
    public DeviceKind kind() {
        return kind;
    }

    /**
     * Повертає тривалість і вартість як незмінне значення.
     *
     * @return record {@link WorkCost}
     */
    public WorkCost cost() {
        return new WorkCost(hours, price);
    }

    /**
     * Два записи рівні, якщо вони одного підтипу й мають однакові спільні поля;
     * підтипи додатково порівнюють власні поля. Використано {@code getClass()}, а не
     * {@code instanceof}, бо записи різних підтипів мають різну поведінку.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Repair that = (Repair) other;
        return Double.compare(hours, that.hours) == 0
                && Double.compare(price, that.price) == 0
                && client.equals(that.client)
                && device.equals(that.device)
                && diagnosis.equals(that.diagnosis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), client, device, diagnosis, hours, price);
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
     * фабрику {@link Repair#create} і конструктор підтипу, тому будівник не обходить жодної перевірки. Повторне використання
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
         * Створює запис через фабрику {@link Repair#create}; незадані числа залишаються NaN
         * і відхиляються тією самою перевіркою.
         *
         * @return новий незмінний запис
         * @throws IllegalArgumentException якщо значення порушують інваріанти
         */
        public Repair build() {
            return create(client, device, diagnosis, hours, price);
        }
    }
}
