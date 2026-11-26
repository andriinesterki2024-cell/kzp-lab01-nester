package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

/** Перевіряє enum {@link RepairKind}: фіксований набір, підписи, розбір і порівняння через ==. */
class RepairKindTest {

    @Test
    void fixedSetOfKindsInDeclarationOrder() {
        assertArrayEquals(new RepairKind[] {RepairKind.WARRANTY, RepairKind.PAID}, RepairKind.values());
    }

    @Test
    void labelsForReport() {
        assertEquals("гарантійний", RepairKind.WARRANTY.label());
        assertEquals("платний", RepairKind.PAID.label());
    }

    @ParameterizedTest
    @CsvSource({"WARRANTY,WARRANTY", "warranty,WARRANTY", " Paid ,PAID", "гарантійний,WARRANTY", "ПЛАТНИЙ,PAID"})
    void parseAcceptsNamesAndLabelsIgnoringCase(String text, RepairKind expected) {
        // Константа enum існує в одному екземплярі, тому порівнюємо посилання.
        assertSame(expected, RepairKind.parse(text));
        assertTrue(RepairKind.parse(text) == expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "ОРЕНДА", "free"})
    void parseRejectsUnknownOrBlank(String text) {
        assertThrows(IllegalArgumentException.class, () -> RepairKind.parse(text));
    }

    @Test
    void parseRejectsNull() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> RepairKind.parse(null));
        assertEquals("порожнє поле kind (вид)", e.getMessage());
    }
}
