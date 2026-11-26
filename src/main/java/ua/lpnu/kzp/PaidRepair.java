package ua.lpnu.kzp;

import java.util.Objects;

/**
 * Платний ремонт: клієнт сплачує вартість робіт, а для дорогих ремонтів діє знижка.
 *
 * <p>Власне поле — частка знижки {@code discountRate} у межах [0; 0.5] (за замовчуванням 0.05),
 * яка застосовується, якщо вартість робіт не менша за {@value #DISCOUNT_THRESHOLD} грн.</p>
 */
public final class PaidRepair extends RepairOrder {

    /** Знижка за замовчуванням (5 %). */
    public static final double DEFAULT_DISCOUNT_RATE = 0.05;

    /** Найбільша допустима знижка (50 %). */
    public static final double MAX_DISCOUNT_RATE = 0.5;

    /** Вартість робіт, від якої діє знижка, грн. */
    public static final double DISCOUNT_THRESHOLD = 3000.0;

    private final double discountRate;

    /**
     * Створює платну заявку: {@code super(...)} перевіряє спільну частину, потім перевіряється власне поле.
     *
     * @param client       клієнт
     * @param device       пристрій
     * @param diagnosis    діагноз
     * @param hours        тривалість, год
     * @param price        вартість робіт, грн
     * @param discountRate частка знижки для дорогих ремонтів; скінченна, у межах [0; 0.5]
     * @throws IllegalArgumentException якщо значення порушують інваріанти
     */
    public PaidRepair(String client, String device, String diagnosis,
                      double hours, double price, double discountRate) {
        super(client, device, diagnosis, hours, price, RepairKind.PAID);
        if (!Double.isFinite(discountRate) || discountRate < 0.0 || discountRate > MAX_DISCOUNT_RATE) {
            throw new IllegalArgumentException("discountRate має бути в межах [0; 0.5]");
        }
        this.discountRate = discountRate;
    }

    /**
     * Повертає частку знижки.
     *
     * @return від 0 до 0.5
     */
    public double discountRate() {
        return discountRate;
    }

    /**
     * Вартість робіт; для ремонтів від {@value #DISCOUNT_THRESHOLD} грн — зі знижкою.
     *
     * @return сума до сплати, грн
     */
    @Override
    public double finalCost() {
        double rate = price() >= DISCOUNT_THRESHOLD ? discountRate : 0.0;
        return roundMoney(price() * (1.0 - rate));
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other)
                && other instanceof PaidRepair that
                && Double.compare(discountRate, that.discountRate) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), discountRate);
    }
}
