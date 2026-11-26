package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Перевіряє record {@link HoursPrice}: компоненти, equals, правила та форматування. */
class HoursPriceTest {

    @Test
    void accessorsEqualsAndHashCode() {
        HoursPrice a = new HoursPrice(4.5, 2750.0);
        HoursPrice b = new HoursPrice(4.5, 2750.0);
        assertEquals(4.5, a.hours(), 0.0001);
        assertEquals(2750.0, a.price(), 0.0001);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hourlyRateDividesPriceByHours() {
        assertEquals(1600.0, new HoursPrice(2.0, 3200.0).hourlyRate(), 0.0001);
        assertEquals(0.0, new HoursPrice(1.0, 0.0).hourlyRate(), 0.0001);
    }

    @Test
    void rejectsInvalidValues() {
        assertEquals("hours має бути більшим за 0",
                assertThrows(IllegalArgumentException.class, () -> new HoursPrice(0.0, 10.0)).getMessage());
        assertEquals("price не може бути від'ємним",
                assertThrows(IllegalArgumentException.class, () -> new HoursPrice(1.0, -1.0)).getMessage());
        assertEquals("поле price має недопустиме значення \"Infinity\"",
                assertThrows(IllegalArgumentException.class,
                        () -> new HoursPrice(1.0, Double.POSITIVE_INFINITY)).getMessage());
    }

    @Test
    void toStringIsLocaleIndependent() {
        assertEquals("4.50 год / 2750.00 грн", new HoursPrice(4.5, 2750.0).toString());
    }
}
