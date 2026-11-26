package ua.lpnu.kzp;

import java.util.Objects;

/**
 * Гарантійний ремонт: роботи оплачує гарантія, тому остаточна вартість для клієнта — 0 грн.
 *
 * <p>Власне поле — строк гарантії в місяцях (від 1 до 36, за замовчуванням 12). Вартість робіт
 * {@link #price()} зберігається для обліку майстерні, але клієнт її не сплачує.</p>
 */
public final class WarrantyRepair extends RepairOrder {

    /** Строк гарантії за замовчуванням, місяців. */
    public static final int DEFAULT_WARRANTY_MONTHS = 12;

    /** Найбільший строк гарантії, місяців. */
    public static final int MAX_WARRANTY_MONTHS = 36;

    private final int warrantyMonths;

    /**
     * Створює гарантійну заявку: {@code super(...)} перевіряє спільну частину, потім перевіряється власне поле.
     *
     * @param client         клієнт
     * @param device         пристрій
     * @param diagnosis      діагноз
     * @param hours          тривалість, год
     * @param price          вартість робіт, грн
     * @param warrantyMonths строк гарантії, місяців; у межах [1; 36]
     * @throws IllegalArgumentException якщо значення порушують інваріанти
     */
    public WarrantyRepair(String client, String device, String diagnosis,
                          double hours, double price, int warrantyMonths) {
        super(client, device, diagnosis, hours, price, RepairKind.WARRANTY);
        if (warrantyMonths < 1 || warrantyMonths > MAX_WARRANTY_MONTHS) {
            throw new IllegalArgumentException("warrantyMonths має бути в межах [1; 36]");
        }
        this.warrantyMonths = warrantyMonths;
    }

    /**
     * Повертає строк гарантії.
     *
     * @return місяців
     */
    public int warrantyMonths() {
        return warrantyMonths;
    }

    /**
     * Гарантія покриває всю вартість робіт (знижка 100 %).
     *
     * @return 0.00 грн
     */
    @Override
    public double finalCost() {
        return 0.0;
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other)
                && other instanceof WarrantyRepair that
                && warrantyMonths == that.warrantyMonths;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), warrantyMonths);
    }
}
