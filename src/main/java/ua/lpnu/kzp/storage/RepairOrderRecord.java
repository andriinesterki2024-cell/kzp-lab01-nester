package ua.lpnu.kzp.storage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import ua.lpnu.kzp.RepairKind;
import ua.lpnu.kzp.RepairOrder;

/**
 * Плоский незмінний запис заявки для збереження у CSV — клас запису варіанта 16 з таблиці ЛР5:
 * {@code RepairOrderRecord: customer, device, fault, date, cost, kind}; CSV-стовпці «клієнт, пристрій,
 * несправність, дата, вартість, вид»; ключ round-trip — {@code customer + date}.
 *
 * <p>Порядок стовпців визначає експортер за Java-іменами полів: cost, customer, date, device, fault, kind.</p>
 */
public final class RepairOrderRecord {

    /** Кількість полів у CSV-записі. */
    public static final int FIELD_COUNT = 6;

    @CsvColumn("вартість")
    private final double cost;

    @CsvColumn("клієнт")
    private final String customer;

    @CsvColumn("дата")
    private final LocalDate date;

    @CsvColumn("пристрій")
    private final String device;

    @CsvColumn("несправність")
    private final String fault;

    @CsvColumn("вид")
    private final RepairKind kind;

    /**
     * Створює запис і перевіряє інваріанти.
     *
     * @param customer клієнт, непорожній
     * @param device   пристрій, непорожній
     * @param fault    несправність, непорожня
     * @param date     дата заявки, не {@code null}
     * @param cost     вартість, скінченна і не менша за 0
     * @param kind     вид заявки, не {@code null}
     * @throws IllegalArgumentException якщо порушено інваріант
     */
    public RepairOrderRecord(String customer, String device, String fault, LocalDate date,
                             double cost, RepairKind kind) {
        this(checkText(customer, "клієнт"), checkText(device, "пристрій"), checkText(fault, "несправність"),
                Objects.requireNonNull(date, "date"), checkCost(cost), Objects.requireNonNull(kind, "kind"), true);
    }

    /* Присвоює вже перевірені значення. */
    private RepairOrderRecord(String customer, String device, String fault, LocalDate date,
                              double cost, RepairKind kind, boolean checked) {
        this.customer = customer;
        this.device = device;
        this.fault = fault;
        this.date = date;
        this.cost = cost;
        this.kind = kind;
    }

    /**
     * Будує запис із заявки будь-якого підтипу (клієнт → customer, діагноз → fault, вартість робіт → cost).
     *
     * @param order заявка
     * @return запис для збереження
     */
    public static RepairOrderRecord fromOrder(RepairOrder order) {
        return new RepairOrderRecord(order.client(), order.device(), order.diagnosis(),
                order.date(), order.price(), order.kind());
    }

    /**
     * Відновлює запис з полів CSV у порядку стовпців експортера (cost, customer, date, device, fault, kind).
     *
     * @param fields значення полів
     * @return запис
     * @throws DataStorageException якщо кількість полів, число, дата, вид або інваріант некоректні
     */
    public static RepairOrderRecord fromFields(List<String> fields) throws DataStorageException {
        if (fields.size() != FIELD_COUNT) {
            throw new DataStorageException(String.format(
                    "очікується %d полів, отримано %d", FIELD_COUNT, fields.size()));
        }
        double cost = parseCost(fields.get(0));
        LocalDate date = parseDate(fields.get(2));
        RepairKind kind = parseKind(fields.get(5));
        try {
            return new RepairOrderRecord(fields.get(1), fields.get(3), fields.get(4), date, cost, kind);
        } catch (IllegalArgumentException e) {
            throw new DataStorageException("Порушено інваріант: " + e.getMessage(), e);
        }
    }

    /**
     * Перевіряє ключ round-trip варіанта: клієнт і дата заявки.
     *
     * @param wantedCustomer клієнт
     * @param wantedDate     дата
     * @return {@code true}, якщо збігаються обидва поля
     */
    public boolean hasKey(String wantedCustomer, LocalDate wantedDate) {
        return customer.equals(wantedCustomer) && date.equals(wantedDate);
    }

    /**
     * Повертає клієнта.
     *
     * @return ім'я
     */
    public String customer() {
        return customer;
    }

    /**
     * Повертає пристрій.
     *
     * @return назва
     */
    public String device() {
        return device;
    }

    /**
     * Повертає несправність.
     *
     * @return опис
     */
    public String fault() {
        return fault;
    }

    /**
     * Повертає дату заявки.
     *
     * @return дата
     */
    public LocalDate date() {
        return date;
    }

    /**
     * Повертає вартість.
     *
     * @return гривні
     */
    public double cost() {
        return cost;
    }

    /**
     * Повертає вид заявки.
     *
     * @return вид
     */
    public RepairKind kind() {
        return kind;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RepairOrderRecord that)) {
            return false;
        }
        return Double.compare(cost, that.cost) == 0 && customer.equals(that.customer)
                && date.equals(that.date) && device.equals(that.device)
                && fault.equals(that.fault) && kind == that.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cost, customer, date, device, fault, kind);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "RepairOrderRecord[%s, %s, %s, %s, %.2f, %s]",
                customer, device, fault, date, cost, kind);
    }

    private static String checkText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("порожнє поле " + name);
        }
        return value;
    }

    private static double checkCost(double cost) {
        if (!Double.isFinite(cost) || cost < 0.0) {
            throw new IllegalArgumentException("вартість має бути скінченним числом не меншим за 0");
        }
        return cost;
    }

    private static LocalDate parseDate(String text) throws DataStorageException {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new DataStorageException("Некоректна дата в полі «дата»: " + text, e);
        }
    }

    private static double parseCost(String text) throws DataStorageException {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new DataStorageException("Некоректне число в полі «вартість»: " + text, e);
        }
    }

    private static RepairKind parseKind(String text) throws DataStorageException {
        try {
            return RepairKind.valueOf(text);
        } catch (IllegalArgumentException e) {
            throw new DataStorageException("Невідомий вид заявки: " + text, e);
        }
    }
}
