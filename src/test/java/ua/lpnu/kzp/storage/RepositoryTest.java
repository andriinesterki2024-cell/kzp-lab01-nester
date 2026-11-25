package ua.lpnu.kzp.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

/** Перевіряє {@link Repository}: порядок, незмінний знімок, пошук, null. */
class RepositoryTest {

    @Test
    void addKeepsInsertionOrder() {
        Repository<String> repo = new Repository<>();
        repo.add("в");
        repo.add("а");
        repo.addAll(List.of("б"));
        assertEquals(List.of("в", "а", "б"), repo.all());
        assertEquals(3, repo.size());
    }

    @Test
    void emptyRepository() {
        Repository<Integer> repo = new Repository<>();
        assertTrue(repo.all().isEmpty());
        assertTrue(repo.find(x -> true).isEmpty());
        assertEquals(0, repo.size());
    }

    @Test
    void snapshotIsUnmodifiable() {
        Repository<String> repo = new Repository<>();
        repo.add("x");
        List<String> snapshot = repo.all();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("y"));
    }

    @Test
    void laterAddsDoNotChangeSnapshot() {
        Repository<String> repo = new Repository<>();
        repo.add("x");
        List<String> snapshot = repo.all();
        repo.add("y");
        assertEquals(List.of("x"), snapshot);
    }

    @Test
    void findByPredicateOfSupertype() {
        Repository<Integer> repo = new Repository<>();
        repo.addAll(List.of(1, 20, 3, 40));
        Predicate<Number> big = n -> n.doubleValue() > 10;
        assertEquals(List.of(20, 40), repo.find(big));
    }

    @Test
    void nullIsRejected() {
        Repository<String> repo = new Repository<>();
        assertThrows(NullPointerException.class, () -> repo.add(null));
        assertThrows(NullPointerException.class, () -> repo.find(null));
    }
}
