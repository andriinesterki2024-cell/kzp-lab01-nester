package ua.lpnu.kzp.storage;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Знаходить рефлексією поля з {@link CsvColumn} у класі та його предках і читає їхні значення.
 *
 * <p>Клас не знає жодного предметного типу. Поля сортуються за Java-іменами, бо JVM не
 * гарантує порядок {@code getDeclaredFields()}, — тож порядок стовпців детермінований.
 * Список полів для класу кешується (завдання із зірочкою).</p>
 */
public final class AnnotatedFields {

    private static final Map<Class<?>, List<Field>> CACHE = new ConcurrentHashMap<>();

    /* Забороняє створення екземплярів службового класу. */
    private AnnotatedFields() {
    }

    /**
     * Повертає анотовані поля класу (з кешу, якщо клас уже оброблявся).
     *
     * @param type клас записів
     * @return незмінний список полів, відсортованих за іменами
     * @throws DataStorageException якщо полів немає або доступ до поля заборонено
     */
    public static List<Field> of(Class<?> type) throws DataStorageException {
        Objects.requireNonNull(type, "type");
        List<Field> cached = CACHE.get(type);
        if (cached != null) {
            return cached;
        }
        List<Field> fields = scan(type);
        // Якщо інший потік встиг покласти список раніше, повертаємо саме його.
        List<Field> previous = CACHE.putIfAbsent(type, fields);
        return previous != null ? previous : fields;
    }

    /**
     * Знаходить поля без кешу — для порівняння часу з кешованою версією.
     *
     * @param type клас записів
     * @return незмінний список полів, відсортованих за іменами
     * @throws DataStorageException якщо полів немає або доступ до поля заборонено
     */
    public static List<Field> scan(Class<?> type) throws DataStorageException {
        List<Field> fields = new ArrayList<>();
        // Обхід ланцюжка успадкування до Object: успадковані анотовані поля теж експортуються.
        for (Class<?> current = type; current != null && current != Object.class;
                current = current.getSuperclass()) {
            Arrays.stream(current.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(CsvColumn.class))
                    .forEach(fields::add);
        }
        if (fields.isEmpty()) {
            throw new DataStorageException("Клас " + type.getSimpleName() + " не має полів з @CsvColumn");
        }
        fields.sort(Comparator.comparing(Field::getName));
        for (Field field : fields) {
            if (!field.trySetAccessible()) {
                throw new DataStorageException("Немає доступу до поля " + field.getName());
            }
        }
        return List.copyOf(fields);
    }

    /**
     * Повертає назви стовпців у порядку полів.
     *
     * @param type клас записів
     * @return назви з анотацій
     * @throws DataStorageException якщо полів немає або доступ заборонено
     */
    public static List<String> columns(Class<?> type) throws DataStorageException {
        return of(type).stream().map(field -> field.getAnnotation(CsvColumn.class).value()).toList();
    }

    /**
     * Читає значення поля об'єкта.
     *
     * @param field  поле, отримане з {@link #of}
     * @param target об'єкт
     * @return значення поля
     * @throws DataStorageException якщо значення неможливо прочитати
     */
    public static Object read(Field field, Object target) throws DataStorageException {
        try {
            return field.get(target);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new DataStorageException("Не вдалося прочитати поле " + field.getName(), e);
        }
    }
}
