package ua.lpnu.kzp;

import java.util.Locale;

/**
 * Опис умов заявки через вичерпний {@code switch} зі зіставленням зі зразком (завдання із зірочкою).
 *
 * <p>Гілки {@code default} немає: ієрархія {@link RepairOrder} запечатана, тому компілятор сам
 * перевіряє, що оброблено всі дозволені підтипи. Остаточна вартість тут не обчислюється — вона
 * лишається в поліморфному методі {@link RepairOrder#finalCost()}; switch лише показує власні поля підтипу.</p>
 */
public final class RepairDescriber {

    /* Забороняє створення екземплярів службового класу. */
    private RepairDescriber() {
    }

    /**
     * Описує умови заявки її власними полями.
     *
     * @param order заявка будь-якого дозволеного підтипу
     * @return опис українською
     */
    public static String describe(RepairOrder order) {
        return switch (order) {
            case WarrantyRepair warranty -> String.format(Locale.ROOT,
                    "гарантія %d міс., клієнт не сплачує роботи", warranty.warrantyMonths());
            case PaidRepair paid -> String.format(Locale.ROOT,
                    "знижка %.0f%% від %.2f грн", paid.discountRate() * 100.0, PaidRepair.DISCOUNT_THRESHOLD);
        };
    }
}
