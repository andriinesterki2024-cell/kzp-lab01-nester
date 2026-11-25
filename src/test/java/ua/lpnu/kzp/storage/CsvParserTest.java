package ua.lpnu.kzp.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Перевіряє машину станів CSV: лапки, коми, перенесення рядка, помилки формату. */
class CsvParserTest {

    @Test
    void simpleRecords() throws DataStorageException {
        assertEquals(List.of(List.of("a", "b"), List.of("c", "d")), CsvParser.parseDocument("a,b\nc,d\n"));
    }

    @Test
    void lastLineWithoutNewline() throws DataStorageException {
        assertEquals(List.of(List.of("a", "b"), List.of("c", "d")), CsvParser.parseDocument("a,b\nc,d"));
    }

    @Test
    void allLineSeparatorsAreAccepted() throws DataStorageException {
        List<List<String>> expected = List.of(List.of("1"), List.of("2"), List.of("3"));
        assertEquals(expected, CsvParser.parseDocument("1\r\n2\r3\n"));
    }

    @Test
    void commaInsideQuotesIsData() throws DataStorageException {
        assertEquals(List.of(List.of("Петренко, Іван", "5")), CsvParser.parseDocument("\"Петренко, Іван\",5\n"));
    }

    @Test
    void doubledQuoteBecomesQuote() throws DataStorageException {
        assertEquals(List.of(List.of("О\"Ніл")), CsvParser.parseDocument("\"О\"\"Ніл\"\n"));
    }

    @Test
    void newlineInsideQuotesIsOneRecord() throws DataStorageException {
        List<List<String>> rows = CsvParser.parseDocument("\"два\nрядки\",x\r\nnext,y\r\n");
        assertEquals(2, rows.size());
        assertEquals("два\nрядки", rows.get(0).get(0));
    }

    @Test
    void emptyFieldsArePreserved() throws DataStorageException {
        assertEquals(List.of(List.of("", "", "")), CsvParser.parseDocument(",,\n"));
        assertEquals(List.of(List.of("a", "")), CsvParser.parseDocument("a,\n"));
    }

    @Test
    void emptyDocumentHasNoRecords() throws DataStorageException {
        assertTrue(CsvParser.parseDocument("").isEmpty());
    }

    @Test
    void unclosedQuoteIsAnError() {
        DataStorageException e = assertThrows(DataStorageException.class,
                () -> CsvParser.parseDocument("a,b\n\"незакрито,c\n"));
        assertEquals("CSV-запис 2: незакриті лапки", e.getMessage());
    }

    @Test
    void textAfterClosingQuoteIsAnError() {
        DataStorageException e = assertThrows(DataStorageException.class,
                () -> CsvParser.parseDocument("\"a\"b,c\n"));
        assertTrue(e.getMessage().contains("після закриваючих лапок"));
    }
}
