package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import ua.lpnu.kzp.storage.DataStorageException;

/** Перевіряє, що вимірювання часу виконується (без порівняння швидкості — воно залежить від машини). */
class ExportTimingTest {

    @Test
    void measureReturnsPositiveTimes() throws DataStorageException {
        ExportTiming.Result r = ExportTiming.measure();
        assertTrue(r.scanNanos() > 0 && r.cacheNanos() > 0 && r.exportNanos() > 0);
    }
}
