package ua.lpnu.kzp;

import java.util.Locale;

/**
 * Допоміжний record варіанта 16 ({@code HoursPrice}): незмінна пара «години — ціна»,
 * яка перевіряє числові правила.
 *
 * <p>Це єдине місце, де описано правила для полів {@code hours} і {@code price}:
 * значення мають бути скінченними, {@code hours > 0}, {@code price >= 0}
 * (безкоштовний гарантійний ремонт допустимий).</p>
 *
 * @param hours тривалість ремонту в годинах
 * @param price вартість ремонту в гривнях
 */
public record HoursPrice(double hours, double price) {

    /**
     * Перевіряє компоненти під час створення record.
     *
     * @throws IllegalArgumentException якщо значення нескінченне, NaN або поза допустимим діапазоном
     */
    public HoursPrice {
        // Double.parseDouble приймає "NaN" та "Infinity", а порівняння з NaN завжди хибне,
        // тому скінченність перевіряється до перевірки діапазону.
        requireFinite(hours, "hours");
        requireFinite(price, "price");
        if (hours <= 0.0) {
            throw new IllegalArgumentException("hours має бути більшим за 0");
        }
        if (price < 0.0) {
            throw new IllegalArgumentException("price не може бути від'ємним");
        }
    }

    /**
     * Обчислює вартість однієї години роботи.
     *
     * @return {@code price / hours}; ділення на нуль неможливе, бо {@code hours > 0}
     */
    public double hourlyRate() {
        return price / hours;
    }

    /**
     * Повертає подання з двома знаками після крапки незалежно від локалі ОС.
     *
     * @return рядок на зразок {@code 4.50 год / 2750.00 грн}
     */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%.2f год / %.2f грн", hours, price);
    }

    private static void requireFinite(double value, String fieldName) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(String.format(
                    "поле %s має недопустиме значення \"%s\"", fieldName, value));
        }
    }
}
