package ua.lpnu.kzp;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;

/**
 * Заявка на ремонт — спільний абстрактний базовий тип варіанта 16 (таблиця ЛР3:
 * базовий тип {@code RepairOrder}, підтипи {@link WarrantyRepair} і {@link PaidRepair},
 * enum {@link RepairKind}, поліморфна операція — остаточна вартість {@link #finalCost()}).
 *
 * <p>Ієрархія запечатана ({@code sealed ... permits}), бо видів заявок рівно два. Спільні поля й
 * інваріанти зосереджено тут і не дублюються в підтипах:</p>
 * <ul>
 *   <li>{@code client}, {@code device}, {@code diagnosis} — не {@code null} і не порожні;</li>
 *   <li>{@code hours} — скінченне і більше за 0 (правило ЛР1);</li>
 *   <li>{@code price} — скінченна і не менша за 0;</li>
 *   <li>{@code kind} — вид, що відповідає підтипу;</li>
 *   <li>{@code date} — дата заявки ({@link LocalDate}), {@code status} — стан (з ЛР4).</li>
 * </ul>
 * <p>Числові правила делеговано record {@link HoursPrice}. Стан незмінний: усі поля
 * {@code private final}, сеттерів немає.</p>
 */
public abstract sealed class RepairOrder permits WarrantyRepair, PaidRepair {

    /** Кількість обов'язкових полів у рядку CSV (формат ЛР1). */
    public static final int FIELD_COUNT = 5;

    /**
     * Найбільша кількість полів. Необов'язкові поля йдуть за обов'язковими в порядку
     * {@code kind;date;status} (вид з ЛР3, дата й стан — з ЛР4).
     */
    public static final int MAX_FIELD_COUNT = 8;

    /** Дата заявки за замовчуванням для рядків без поля дати (формати ЛР1–ЛР3). */
    public static final LocalDate DEFAULT_DATE = LocalDate.of(2026, 1, 1);

    /** Стан за замовчуванням для рядків без поля стану: архівні заявки формату ЛР1–ЛР3 завершені. */
    public static final RepairStatus DEFAULT_STATUS = RepairStatus.DONE;

    /** Вид заявки за замовчуванням для рядків формату ЛР1 (без шостого поля). */
    public static final RepairKind DEFAULT_KIND = RepairKind.PAID;

    private final String client;
    private final String device;
    private final String diagnosis;
    private final double hours;
    private final double price;
    private final RepairKind kind;
    private final LocalDate date;
    private final RepairStatus status;

    /**
     * Ініціалізує й перевіряє спільну частину заявки; викликається з конструктора підтипу через {@code super(...)}.
     *
     * @param client    клієнт, непорожній
     * @param device    пристрій, непорожній
     * @param diagnosis діагноз, непорожній
     * @param hours     тривалість ремонту, год; скінченна і більша за 0
     * @param price     вартість робіт, грн; скінченна і не менша за 0
     * @param kind      вид заявки, який задає підтип
     * @param date      дата заявки, не {@code null}
     * @param status    стан заявки, не {@code null}
     * @throws IllegalArgumentException якщо значення порушує інваріант
     */
    protected RepairOrder(String client, String device, String diagnosis, double hours, double price,
                          RepairKind kind, LocalDate date, RepairStatus status) {
        // Перевірки виконуються під час обчислення аргументів, ще до створення об'єкта,
        // тож частково ініціалізована заявка не може з'явитися (SpotBugs CT_CONSTRUCTOR_THROW).
        this(requireText(client, "client (клієнт)"),
                requireText(device, "device (пристрій)"),
                requireText(diagnosis, "diagnosis (діагноз)"),
                new HoursPrice(hours, price),
                Objects.requireNonNull(kind, "kind"),
                Objects.requireNonNull(date, "date"),
                Objects.requireNonNull(status, "status"));
    }

    /* Присвоює вже перевірені значення; сам нічого не перевіряє. */
    private RepairOrder(String client, String device, String diagnosis, HoursPrice hoursPrice,
                        RepairKind kind, LocalDate date, RepairStatus status) {
        this.client = client;
        this.device = device;
        this.diagnosis = diagnosis;
        this.hours = hoursPrice.hours();
        this.price = hoursPrice.price();
        this.kind = kind;
        this.date = date;
        this.status = status;
    }

    /**
     * Остаточна вартість для клієнта — поліморфна операція варіанта 16; формулу задає підтип.
     *
     * @return сума до сплати, грн, округлена до копійки
     */
    public abstract double finalCost();

    /**
     * Фабрика: створює підтип за видом заявки. Це єдине місце, де враховується вид;
     * власні поля підтипів отримують значення за замовчуванням.
     *
     * @param client    клієнт
     * @param device    пристрій
     * @param diagnosis діагноз
     * @param hours     тривалість, год
     * @param price     вартість робіт, грн
     * @param kind      вид заявки
     * @return {@link WarrantyRepair} або {@link PaidRepair}
     * @throws IllegalArgumentException якщо значення порушують інваріанти
     */
    public static RepairOrder create(String client, String device, String diagnosis,
                                     double hours, double price, RepairKind kind) {
        return create(client, device, diagnosis, hours, price, kind, DEFAULT_DATE, DEFAULT_STATUS);
    }

    /**
     * Фабрика з датою й станом заявки (ЛР4). Це єдине місце, де враховується вид.
     *
     * @param client    клієнт
     * @param device    пристрій
     * @param diagnosis діагноз (несправність)
     * @param hours     тривалість, год
     * @param price     вартість робіт, грн
     * @param kind      вид заявки
     * @param date      дата заявки
     * @param status    стан заявки
     * @return {@link WarrantyRepair} або {@link PaidRepair}
     * @throws IllegalArgumentException якщо значення порушують інваріанти
     */
    public static RepairOrder create(String client, String device, String diagnosis, double hours,
                                     double price, RepairKind kind, LocalDate date, RepairStatus status) {
        return switch (Objects.requireNonNull(kind, "kind")) {
            case WARRANTY -> new WarrantyRepair(client, device, diagnosis, hours, price, date, status,
                    WarrantyRepair.DEFAULT_WARRANTY_MONTHS);
            case PAID -> new PaidRepair(client, device, diagnosis, hours, price, date, status,
                    PaidRepair.DEFAULT_DISCOUNT_RATE);
        };
    }

    /**
     * Створює заявку з рядка CSV {@code client;device;diagnosis;hours;price[;kind[;date[;status]]]}.
     *
     * <p>Формати ЛР1 (5 полів) і ЛР3 (6 полів) лишаються коректними: відсутні поля отримують
     * значення {@link #DEFAULT_KIND}, {@link #DEFAULT_DATE}, {@link #DEFAULT_STATUS}, тому
     * контрольний файл і звіт ЛР1 не змінюються. Дата — ISO {@code YYYY-MM-DD}.</p>
     *
     * @param line рядок файла
     * @return заявка відповідного підтипу
     * @throws IllegalArgumentException з поясненням причини, якщо рядок некоректний
     */
    public static RepairOrder fromCsv(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("порожній рядок");
        }
        // -1 зберігає порожні поля в кінці рядка.
        String[] fields = line.split(";", -1);
        if (fields.length < FIELD_COUNT) {
            throw new IllegalArgumentException(String.format(
                    "очікується %d полів, отримано %d", FIELD_COUNT, fields.length));
        }
        if (fields.length > MAX_FIELD_COUNT) {
            throw new IllegalArgumentException(String.format(
                    "очікується не більше %d полів, отримано %d", MAX_FIELD_COUNT, fields.length));
        }
        String client = requireText(fields[0], "client (клієнт)");
        String device = requireText(fields[1], "device (пристрій)");
        String diagnosis = requireText(fields[2], "diagnosis (діагноз)");
        double hours = parseNumber(fields[3], "hours");
        double price = parseNumber(fields[4], "price");
        RepairKind kind = fields.length > 5 ? RepairKind.parse(fields[5]) : DEFAULT_KIND;
        LocalDate date = fields.length > 6 ? parseDate(fields[6]) : DEFAULT_DATE;
        RepairStatus status = fields.length > 7 ? RepairStatus.parse(fields[7]) : DEFAULT_STATUS;
        return create(client, device, diagnosis, hours, price, kind, date, status);
    }

    /**
     * Повертає новий будівник (вид за замовчуванням — платний ремонт).
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
    public final String client() {
        return client;
    }

    /**
     * Повертає пристрій.
     *
     * @return назва пристрою
     */
    public final String device() {
        return device;
    }

    /**
     * Повертає діагноз.
     *
     * @return діагноз або вид робіт
     */
    public final String diagnosis() {
        return diagnosis;
    }

    /**
     * Повертає тривалість ремонту.
     *
     * @return години, більше за 0
     */
    public final double hours() {
        return hours;
    }

    /**
     * Повертає вартість робіт (без урахування знижок підтипу).
     *
     * @return гривні, не менше за 0
     */
    public final double price() {
        return price;
    }

    /**
     * Повертає вид заявки.
     *
     * @return значення {@link RepairKind}
     */
    public final RepairKind kind() {
        return kind;
    }

    /**
     * Повертає дату заявки.
     *
     * @return дата
     */
    public final LocalDate date() {
        return date;
    }

    /**
     * Повертає стан заявки.
     *
     * @return значення {@link RepairStatus}
     */
    public final RepairStatus status() {
        return status;
    }

    /**
     * Чи заявка незавершена (нова або в роботі).
     *
     * @return {@code true}, якщо стан не {@link RepairStatus#DONE}
     */
    public final boolean isOpen() {
        return status.isOpen();
    }

    /**
     * Повертає тривалість і вартість як незмінне значення.
     *
     * @return record {@link HoursPrice}
     */
    public final HoursPrice hoursPrice() {
        return new HoursPrice(hours, price);
    }

    /**
     * Округлює суму до копійки (half-up) для остаточної вартості.
     *
     * @param amount сума
     * @return сума з двома знаками після крапки
     */
    protected static double roundMoney(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }

    /**
     * Дві заявки рівні, якщо вони одного підтипу й мають однакові спільні поля; підтипи
     * додатково порівнюють власні поля. Використано {@code getClass()}: заявки різних видів
     * мають різну поведінку, тож не можуть бути рівними.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        RepairOrder that = (RepairOrder) other;
        return Double.compare(hours, that.hours) == 0
                && Double.compare(price, that.price) == 0
                && client.equals(that.client)
                && device.equals(that.device)
                && diagnosis.equals(that.diagnosis)
                && kind == that.kind
                && date.equals(that.date)
                && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), client, device, diagnosis, hours, price, kind, date, status);
    }

    /**
     * Повертає подання з назвою підтипу й двома знаками після крапки (Locale.ROOT).
     *
     * @return рядок на зразок {@code PaidRepair[client=..., price=2750.00, kind=PAID, date=2026-01-01, status=DONE]}
     */
    @Override
    public String toString() {
        return String.format(Locale.ROOT,
                "%s[client=%s, device=%s, diagnosis=%s, hours=%.2f, price=%.2f, kind=%s, date=%s, status=%s]",
                getClass().getSimpleName(), client, device, diagnosis, hours, price, kind, date, status);
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

    private static LocalDate parseDate(String text) {
        String trimmed = text.strip();
        try {
            return LocalDate.parse(trimmed);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(String.format(
                    "поле date має бути датою YYYY-MM-DD, отримано \"%s\"", trimmed), e);
        }
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
     * Будівник заявки. {@link #build()} викликає фабрику {@link RepairOrder#create} і конструктор
     * підтипу, тому жодна перевірка не обходиться; незадані числа лишаються NaN і відхиляються.
     */
    public static final class Builder {
        private String client;
        private String device;
        private String diagnosis;
        private double hours = Double.NaN;
        private double price = Double.NaN;
        private RepairKind kind = DEFAULT_KIND;
        private LocalDate date = DEFAULT_DATE;
        private RepairStatus status = DEFAULT_STATUS;

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
         * Задає вартість робіт.
         *
         * @param value гривні
         * @return цей будівник
         */
        public Builder price(double value) {
            this.price = value;
            return this;
        }

        /**
         * Задає вид заявки.
         *
         * @param value вид
         * @return цей будівник
         */
        public Builder kind(RepairKind value) {
            this.kind = value;
            return this;
        }

        /**
         * Задає дату заявки.
         *
         * @param value дата
         * @return цей будівник
         */
        public Builder date(LocalDate value) {
            this.date = value;
            return this;
        }

        /**
         * Задає стан заявки.
         *
         * @param value стан
         * @return цей будівник
         */
        public Builder status(RepairStatus value) {
            this.status = value;
            return this;
        }

        /**
         * Створює заявку через фабрику {@link RepairOrder#create}.
         *
         * @return нова незмінна заявка
         * @throws IllegalArgumentException якщо значення порушують інваріанти
         */
        public RepairOrder build() {
            return create(client, device, diagnosis, hours, price, kind, date, status);
        }
    }
}
