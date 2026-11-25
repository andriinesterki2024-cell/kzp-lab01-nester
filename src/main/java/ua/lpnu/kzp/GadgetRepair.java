package ua.lpnu.kzp;

import java.util.Objects;

/**
 * Ремонт гаджета або офісної техніки, яку клієнт приносить до майстерні.
 *
 * <p>Вартість для клієнта дорівнює ціні ремонту; гарантійний строк задається
 * власним полем {@code warrantyDays} (за замовчуванням 30 днів).</p>
 */
public final class GadgetRepair extends Repair {

    /** Гарантійний строк за замовчуванням, днів. */
    public static final int DEFAULT_WARRANTY_DAYS = 30;

    /** Найбільший допустимий гарантійний строк, днів. */
    public static final int MAX_WARRANTY_DAYS = 365;

    private final int warrantyDays;

    /**
     * Створює ремонт гаджета: спочатку {@code super(...)} перевіряє спільну частину,
     * потім перевіряється власне поле.
     *
     * @param client       клієнт
     * @param device       пристрій
     * @param diagnosis    діагноз
     * @param hours        тривалість, год
     * @param price        вартість, грн
     * @param warrantyDays гарантія, днів; у межах [0; 365]
     * @throws IllegalArgumentException якщо значення порушують інваріанти
     */
    public GadgetRepair(String client, String device, String diagnosis,
                        double hours, double price, int warrantyDays) {
        super(client, device, diagnosis, hours, price);
        if (warrantyDays < 0 || warrantyDays > MAX_WARRANTY_DAYS) {
            throw new IllegalArgumentException("warrantyDays має бути в межах [0; 365]");
        }
        this.warrantyDays = warrantyDays;
    }

    /**
     * Для ремонту в майстерні клієнт сплачує лише ціну ремонту.
     *
     * @return {@code price()}
     */
    @Override
    public double totalCost() {
        return price();
    }

    @Override
    public int warrantyDays() {
        return warrantyDays;
    }

    @Override
    public String tierLabel() {
        return "Гаджети";
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other)
                && other instanceof GadgetRepair that
                && warrantyDays == that.warrantyDays;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), warrantyDays);
    }
}
