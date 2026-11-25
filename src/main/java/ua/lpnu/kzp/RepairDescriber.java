package ua.lpnu.kzp;

import java.util.Locale;

/**
 * Текстовий опис правила підтипу через вичерпний {@code switch} зі зіставленням зі зразком.
 *
 * <p>Гілки {@code default} немає: ієрархія {@link Repair} запечатана, тому компілятор
 * сам перевіряє, що оброблено всі дозволені підтипи. Обчислення вартості тут не
 * дублюється — воно лишається в поліморфному методі {@link Repair#totalCost()}.</p>
 */
public final class RepairDescriber {

    /* Забороняє створення екземплярів службового класу. */
    private RepairDescriber() {
    }

    /**
     * Описує правило, за яким підтип формує вартість і гарантію.
     *
     * @param repair запис будь-якого дозволеного підтипу
     * @return опис правила українською
     */
    public static String describe(Repair repair) {
        return switch (repair) {
            case GadgetRepair g -> String.format(Locale.ROOT,
                    "ремонт у майстерні, гарантія %d днів", g.warrantyDays());
            case ApplianceRepair a -> String.format(Locale.ROOT,
                    "виїзд майстра %.2f грн, гарантія %d днів", a.callOutFee(), a.warrantyDays());
        };
    }
}
