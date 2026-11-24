package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Перевіряє розбір рядків і обчислення показників варіанта 16. */
class RepairAnalyzerTest {

    private static final double EPS = 0.0001;

    @Test
    void validRecordsAreSummed() {
        RepairSummary s = RepairAnalyzer.analyze(List.of(
                "Іваненко Петро;Ноутбук;Заміна термопасти;1.5;650.00",
                "Коваль Оксана;Смартфон;Заміна екрана;2.0;3200.00",
                "Бондар Марія;Пральна машина;Заміна підшипника;4.5;2750.00"));
        assertEquals(3, s.validCount());
        assertEquals(8.0, s.totalHours(), EPS);
        assertEquals(6600.0, s.totalRevenue(), EPS);
        assertEquals(4.5, s.longestHours(), EPS);
        assertEquals("Бондар Марія", s.longestClient());
        assertEquals("Пральна машина", s.longestDevice());
        assertTrue(s.errors().isEmpty());
    }

    @Test
    void emptyLineIsReportedWithNumber() {
        RepairSummary s = RepairAnalyzer.analyze(List.of(
                "Іваненко Петро;Ноутбук;Діагностика;1.0;300.00",
                "   "));
        assertEquals(1, s.validCount());
        assertEquals(List.of("Рядок 2: порожній рядок"), s.errors());
    }

    @Test
    void wrongFieldCountIsRejected() {
        RepairSummary s = RepairAnalyzer.analyze(List.of(
                "Савчук Наталія;Навушники;Пайка;0.75"));
        assertEquals(0, s.validCount());
        assertEquals("Рядок 1: очікується 5 полів, отримано 4", s.errors().get(0));
    }

    @Test
    void emptyTextFieldIsRejected() {
        RepairSummary s = RepairAnalyzer.analyze(List.of(
                "Ткаченко Ірина;Телевізор;;2.0;1500.00",
                ";Телевізор;Ремонт;2.0;1500.00"));
        assertEquals(0, s.validCount());
        assertEquals("Рядок 1: порожнє поле diagnosis (діагноз)", s.errors().get(0));
        assertEquals("Рядок 2: порожнє поле client (клієнт)", s.errors().get(1));
    }

    @Test
    void nonNumericValueIsRejected() {
        RepairSummary s = RepairAnalyzer.analyze(List.of(
                "Кравченко Юрій;Мікрохвильова піч;Заміна магнетрона;два;900.00",
                "Кравченко Юрій;Мікрохвильова піч;Заміна магнетрона;1,5;900.00"));
        assertEquals(0, s.validCount());
        assertEquals(2, s.errors().size());
        assertTrue(s.errors().get(0).contains("нечислове значення \"два\""));
    }

    @Test
    void negativeZeroNanAndInfinityAreRejected() {
        RepairSummary s = RepairAnalyzer.analyze(List.of(
                "Олійник Віктор;Ноутбук;Заміна клавіатури;1.0;-500.00",
                "Олійник Віктор;Ноутбук;Заміна клавіатури;0;500.00",
                "Олійник Віктор;Ноутбук;Заміна клавіатури;NaN;500.00",
                "Олійник Віктор;Ноутбук;Заміна клавіатури;1.0;Infinity"));
        assertEquals(0, s.validCount());
        assertEquals(4, s.errors().size());
        assertEquals(0.0, s.totalRevenue(), EPS);
    }

    @Test
    void invalidLinesDoNotChangeTotals() {
        RepairSummary s = RepairAnalyzer.analyze(List.of(
                "Мельник Олег;Планшет;Діагностика;0.5;300.00",
                "Мельник Олег;Планшет;Діагностика;-9;99999.00",
                "Лисенко Тарас;Консоль;Чищення від пилу;1.75;1100.00"));
        assertEquals(2, s.validCount());
        assertEquals(2.25, s.totalHours(), EPS);
        assertEquals(1400.0, s.totalRevenue(), EPS);
        assertEquals(1.75, s.longestHours(), EPS);
    }

    @Test
    void fileWithoutValidRecordsGivesZeroCount() {
        RepairSummary s = RepairAnalyzer.analyze(List.of("", "abc"));
        assertEquals(0, s.validCount());
        assertEquals(0.0, s.totalHours(), EPS);
        assertEquals(2, s.errors().size());
    }
}
