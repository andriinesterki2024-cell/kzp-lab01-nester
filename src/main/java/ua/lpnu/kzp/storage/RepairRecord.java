package ua.lpnu.kzp.storage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import ua.lpnu.kzp.DeviceKind;
import ua.lpnu.kzp.Repair;

/**
 * Плоский незмінний запис квитанції ремонту для збереження у CSV (варіант 16).
 *
 * <p>Ключ запису — номер квитанції {@code ticket}. Порядок стовпців визначає експортер
 * за Java-іменами полів: acceptedOn, client, device, hours, kind, price, ticket.</p>
 */
public final class RepairRecord {

    /** Кількість полів у CSV-записі. */
    public static final int FIELD_COUNT = 7;

    @CsvColumn("прийнято")
    private final LocalDate acceptedOn;

    @CsvColumn("клієнт")
    private final String client;

    @CsvColumn("пристрій")
    private final String device;

    @CsvColumn("години")
    private final double hours;

    @CsvColumn("тип")
    private final DeviceKind kind;

    @CsvColumn("вартість")
    private final double price;

    @CsvColumn("квитанція")
    private final int ticket;

    /**
     * Створює запис і перевіряє інваріанти.
     *
     * @param ticket     номер квитанції, більше нуля
     * @param client     клієнт, непорожній
     * @param device     пристрій, непорожній
     * @param kind       тип пристрою, не {@code null}
     * @param acceptedOn дата прийому, не {@code null}
     * @param hours      тривалість, скінченна і більша за 0
     * @param price      вартість, скінченна і не менша за 0
     * @throws IllegalArgumentException якщо порушено інваріант
     */
    public RepairRecord(int ticket, String client, String device, DeviceKind kind,
                        LocalDate acceptedOn, double hours, double price) {
        this(checkTicket(ticket), checkText(client, "клієнт"), checkText(device, "пристрій"),
                Objects.requireNonNull(kind, "kind"), Objects.requireNonNull(acceptedOn, "acceptedOn"),
                checkHours(hours), checkPrice(price), true);
    }

    /* Присвоює вже перевірені значення. */
    private RepairRecord(int ticket, String client, String device, DeviceKind kind,
                         LocalDate acceptedOn, double hours, double price, boolean checked) {
        this.ticket = ticket;
        this.client = client;
        this.device = device;
        this.kind = kind;
        this.acceptedOn = acceptedOn;
        this.hours = hours;
        this.price = price;
    }

    /**
     * Будує запис із ремонту будь-якого підтипу.
     *
     * @param repair     ремонт
     * @param ticket     номер квитанції
     * @param acceptedOn дата прийому
     * @return запис для збереження
     */
    public static RepairRecord fromRepair(Repair repair, int ticket, LocalDate acceptedOn) {
        return new RepairRecord(ticket, repair.client(), repair.device(), repair.kind(),
                acceptedOn, repair.hours(), repair.price());
    }

    /**
     * Відновлює запис з полів CSV у порядку стовпців експортера.
     *
     * @param fields значення полів
     * @return запис
     * @throws DataStorageException якщо кількість полів, число, дата, тип або інваріант некоректні
     */
    public static RepairRecord fromFields(List<String> fields) throws DataStorageException {
        if (fields.size() != FIELD_COUNT) {
            throw new DataStorageException(String.format(
                    "очікується %d полів, отримано %d", FIELD_COUNT, fields.size()));
        }
        LocalDate accepted = parseDate(fields.get(0));
        double hours = parseDouble(fields.get(3), "години");
        DeviceKind kind = parseKind(fields.get(4));
        double price = parseDouble(fields.get(5), "вартість");
        int ticket = parseInt(fields.get(6));
        try {
            return new RepairRecord(ticket, fields.get(1), fields.get(2), kind, accepted, hours, price);
        } catch (IllegalArgumentException e) {
            throw new DataStorageException("Порушено інваріант: " + e.getMessage(), e);
        }
    }

    /**
     * Перевіряє ключ запису.
     *
     * @param wantedTicket номер квитанції
     * @return {@code true}, якщо номер збігається
     */
    public boolean hasKey(int wantedTicket) {
        return ticket == wantedTicket;
    }

    /**
     * Перевіряє повний ключ round-trip: номер квитанції і дату прийому
     * (номер може повторитися, якщо квитанції нумеруються щодня з одиниці).
     *
     * @param wantedTicket номер квитанції
     * @param wantedDate   дата прийому
     * @return {@code true}, якщо збігаються обидва поля
     */
    public boolean hasKey(int wantedTicket, LocalDate wantedDate) {
        return ticket == wantedTicket && acceptedOn.equals(wantedDate);
    }

    /**
     * Повертає дату прийому.
     *
     * @return дата
     */
    public LocalDate acceptedOn() {
        return acceptedOn;
    }

    /**
     * Повертає номер квитанції.
     *
     * @return ключ
     */
    public int ticket() {
        return ticket;
    }

    /**
     * Повертає клієнта.
     *
     * @return ім'я
     */
    public String client() {
        return client;
    }

    /**
     * Повертає вартість.
     *
     * @return гривні
     */
    public double price() {
        return price;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RepairRecord that)) {
            return false;
        }
        return ticket == that.ticket && Double.compare(hours, that.hours) == 0
                && Double.compare(price, that.price) == 0 && client.equals(that.client)
                && device.equals(that.device) && kind == that.kind && acceptedOn.equals(that.acceptedOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticket, client, device, kind, acceptedOn, hours, price);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "RepairRecord[%d, %s, %s, %s, %s, %.2f, %.2f]",
                ticket, client, device, kind, acceptedOn, hours, price);
    }

    private static int checkTicket(int ticket) {
        if (ticket <= 0) {
            throw new IllegalArgumentException("квитанція має бути більшою за 0");
        }
        return ticket;
    }

    private static String checkText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("порожнє поле " + name);
        }
        return value;
    }

    private static double checkHours(double hours) {
        if (!Double.isFinite(hours) || hours <= 0.0) {
            throw new IllegalArgumentException("години мають бути скінченним числом більшим за 0");
        }
        return hours;
    }

    private static double checkPrice(double price) {
        if (!Double.isFinite(price) || price < 0.0) {
            throw new IllegalArgumentException("вартість має бути скінченним числом не меншим за 0");
        }
        return price;
    }

    private static LocalDate parseDate(String text) throws DataStorageException {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new DataStorageException("Некоректна дата в полі «прийнято»: " + text, e);
        }
    }

    private static double parseDouble(String text, String column) throws DataStorageException {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new DataStorageException("Некоректне число в полі «" + column + "»: " + text, e);
        }
    }

    private static int parseInt(String text) throws DataStorageException {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new DataStorageException("Некоректне число в полі «квитанція»: " + text, e);
        }
    }

    private static DeviceKind parseKind(String text) throws DataStorageException {
        try {
            return DeviceKind.valueOf(text);
        } catch (IllegalArgumentException e) {
            throw new DataStorageException("Невідомий тип пристрою: " + text, e);
        }
    }
}
