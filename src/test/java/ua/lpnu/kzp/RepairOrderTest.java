package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Перевіряє конструктор, fromCsv, toString і будівник класу {@link RepairOrder}. */
class RepairOrderTest {

    private static final double EPS = 0.0001;

    @Test
    void constructorStoresValidValues() {
        // Arrange + Act
        RepairOrder repair = new RepairOrder("Бондар Марія", "Пральна машина Bosch", "Заміна підшипника", 4.5, 2750.0);
        // Assert
        assertEquals("Бондар Марія", repair.client());
        assertEquals("Пральна машина Bosch", repair.device());
        assertEquals("Заміна підшипника", repair.diagnosis());
        assertEquals(4.5, repair.hours(), EPS);
        assertEquals(2750.0, repair.price(), EPS);
    }

    @Test
    void constructorAcceptsBoundaryValues() {
        // Граничні коректні значення: безкоштовний ремонт і дуже коротка робота.
        RepairOrder repair = new RepairOrder("Клієнт", "Пристрій", "Гарантія", 0.01, 0.0);
        assertEquals(0.0, repair.price(), EPS);
        assertEquals(0.01, repair.hours(), EPS);
    }

    @Test
    void constructorStripsSpaces() {
        RepairOrder repair = new RepairOrder("  Іван  ", " Ноутбук ", " Ремонт ", 1.0, 10.0);
        assertEquals("Іван", repair.client());
        assertEquals("Ноутбук", repair.device());
    }

    @Test
    void constructorRejectsNullAndBlankText() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new RepairOrder(null, "Ноутбук", "Ремонт", 1.0, 10.0));
        assertEquals("порожнє поле client (клієнт)", e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> new RepairOrder("Іван", "   ", "Ремонт", 1.0, 10.0));
        assertThrows(IllegalArgumentException.class, () -> new RepairOrder("Іван", "Ноутбук", "", 1.0, 10.0));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY})
    void constructorRejectsInvalidHours(double hours) {
        assertThrows(IllegalArgumentException.class, () -> new RepairOrder("Іван", "Ноутбук", "Ремонт", hours, 10.0));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.01, -500.0, Double.NaN, Double.NEGATIVE_INFINITY})
    void constructorRejectsInvalidPrice(double price) {
        assertThrows(IllegalArgumentException.class, () -> new RepairOrder("Іван", "Ноутбук", "Ремонт", 1.0, price));
    }

    @Test
    void fromCsvParsesValidLine() {
        RepairOrder repair = RepairOrder.fromCsv("Коваль Оксана;Смартфон Samsung A54;Заміна екрана;2.0;3200.00");
        assertEquals(new RepairOrder("Коваль Оксана", "Смартфон Samsung A54", "Заміна екрана", 2.0, 3200.0), repair);
    }

    @Test
    void fromCsvKeepsLab01ErrorMessages() {
        assertEquals("порожній рядок", message(""));
        assertEquals("очікується 5 полів, отримано 4", message("Савчук Наталія;Навушники JBL;Пайка роз'єму;0.75"));
        assertEquals("порожнє поле diagnosis (діагноз)", message("Ткаченко Ірина;Телевізор LG;;2.0;1500.00"));
        assertEquals("поле hours має нечислове значення \"два\"",
                message("Кравченко Юрій;Мікрохвильова піч;Заміна магнетрона;два;900.00"));
        assertEquals("price не може бути від'ємним", message("Олійник Віктор;Ноутбук Asus;Заміна клавіатури;1.0;-500.00"));
        assertEquals("поле hours має недопустиме значення \"NaN\"", message("Тест;Ноутбук;Ремонт;NaN;100.00"));
    }

    @Test
    void toStringUsesDotAndTwoDecimals() {
        RepairOrder repair = new RepairOrder("Іван", "Ноутбук", "Ремонт", 1.5, 650.0);
        assertEquals("RepairOrder[client=Іван, device=Ноутбук, diagnosis=Ремонт, hours=1.50, price=650.00]",
                repair.toString());
    }

    @Test
    void hoursPriceReturnsRecord() {
        RepairOrder repair = new RepairOrder("Іван", "Ноутбук", "Ремонт", 2.0, 300.0);
        assertEquals(new HoursPrice(2.0, 300.0), repair.hoursPrice());
    }

    @Test
    void builderCreatesSameObjectAsConstructor() {
        RepairOrder built = RepairOrder.builder()
                .client("Іваненко Петро").device("Ноутбук Lenovo").diagnosis("Заміна термопасти")
                .hours(1.5).price(650.0)
                .build();
        assertEquals(new RepairOrder("Іваненко Петро", "Ноутбук Lenovo", "Заміна термопасти", 1.5, 650.0), built);
    }

    @Test
    void builderDoesNotBypassValidation() {
        // Незадана ціна лишається NaN і відхиляється конструктором.
        assertThrows(IllegalArgumentException.class,
                () -> RepairOrder.builder().client("Іван").device("Ноутбук").diagnosis("Ремонт").hours(1.0).build());
        assertThrows(IllegalArgumentException.class,
                () -> RepairOrder.builder().client(" ").device("Ноутбук").diagnosis("Ремонт").hours(1.0).price(1.0).build());
    }

    @Test
    void reusingBuilderDoesNotChangeBuiltObject() {
        RepairOrder.Builder builder = RepairOrder.builder().client("Іван").device("Ноутбук").diagnosis("Ремонт")
                .hours(1.0).price(100.0);
        RepairOrder first = builder.build();
        RepairOrder second = builder.price(200.0).build();
        assertEquals(100.0, first.price(), EPS);
        assertNotEquals(first, second);
    }

    private static String message(String line) {
        return assertThrows(IllegalArgumentException.class, () -> RepairOrder.fromCsv(line)).getMessage();
    }
}
