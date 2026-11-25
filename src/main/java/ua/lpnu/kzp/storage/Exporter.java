package ua.lpnu.kzp.storage;

import java.util.List;

/**
 * Спільний контракт форматів експорту (завдання із зірочкою): CSV і JSON використовують
 * ті самі анотовані поля.
 *
 * @param <T> тип записів
 */
@FunctionalInterface
public interface Exporter<T> {

    /**
     * Перетворює записи на текст формату.
     *
     * @param type  клас записів (потрібен і для порожнього списку)
     * @param items записи
     * @return текст експорту
     * @throws DataStorageException якщо поля неможливо прочитати
     */
    String export(Class<T> type, List<? extends T> items) throws DataStorageException;
}
