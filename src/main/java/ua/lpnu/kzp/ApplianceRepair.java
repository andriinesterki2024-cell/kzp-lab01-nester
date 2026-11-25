package ua.lpnu.kzp;

import java.util.Objects;

/**
 * Ремонт побутової (великогабаритної) техніки з виїздом майстра до клієнта.
 *
 * <p>До ціни ремонту додається плата за виїзд {@code callOutFee}
 * (за замовчуванням 200 грн); гарантія — 90 днів.</p>
 */
public final class ApplianceRepair extends Repair {

    /** Плата за виїзд майстра за замовчуванням, грн. */
    public static final double DEFAULT_CALL_OUT_FEE = 200.0;

    /** Гарантійний строк для побутової техніки, днів. */
    public static final int WARRANTY_DAYS = 90;

    private final double callOutFee;

    /**
     * Створює ремонт побутової техніки: спочатку {@code super(...)} перевіряє
     * спільну частину, потім перевіряється власне поле.
     *
     * @param client     клієнт
     * @param device     пристрій
     * @param diagnosis  діагноз
     * @param hours      тривалість, год
     * @param price      вартість ремонту, грн
     * @param callOutFee плата за виїзд, грн; скінченна і не менша за 0
     * @throws IllegalArgumentException якщо значення порушують інваріанти
     */
    public ApplianceRepair(String client, String device, String diagnosis,
                           double hours, double price, double callOutFee) {
        super(client, device, diagnosis, hours, price);
        if (!Double.isFinite(callOutFee) || callOutFee < 0.0) {
            throw new IllegalArgumentException("callOutFee має бути скінченним і не від'ємним");
        }
        this.callOutFee = callOutFee;
    }

    /**
     * Повертає плату за виїзд майстра.
     *
     * @return гривні
     */
    public double callOutFee() {
        return callOutFee;
    }

    /**
     * До ціни ремонту додається плата за виїзд майстра.
     *
     * @return {@code price() + callOutFee}
     */
    @Override
    public double totalCost() {
        return price() + callOutFee;
    }

    @Override
    public int warrantyDays() {
        return WARRANTY_DAYS;
    }

    @Override
    public String tierLabel() {
        return "Побутова техніка";
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other)
                && other instanceof ApplianceRepair that
                && Double.compare(callOutFee, that.callOutFee) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), callOutFee);
    }
}
