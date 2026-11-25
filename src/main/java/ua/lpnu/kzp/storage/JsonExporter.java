package ua.lpnu.kzp.storage;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Другий формат експорту (завдання із зірочкою): масив JSON-об'єктів з тими самими
 * анотованими полями, що й у CSV. Числа й логічні значення пишуться без лапок.
 */
public final class JsonExporter {

    /* Забороняє створення екземплярів службового класу. */
    private JsonExporter() {
    }

    /**
     * Формує JSON-масив.
     *
     * @param type  клас записів
     * @param items записи
     * @param <T>   тип записів
     * @return текст JSON
     * @throws DataStorageException якщо поля неможливо прочитати
     */
    public static <T> String toJson(Class<T> type, List<? extends T> items) throws DataStorageException {
        List<Field> fields = AnnotatedFields.of(type);
        String nl = System.lineSeparator();
        List<String> objects = new ArrayList<>();
        for (T item : items) {
            List<String> pairs = new ArrayList<>();
            for (Field field : fields) {
                Object value = AnnotatedFields.read(field, item);
                pairs.add(quote(field.getAnnotation(CsvColumn.class).value()) + ": " + literal(value));
            }
            objects.add("  {" + String.join(", ", pairs) + "}");
        }
        return objects.isEmpty() ? "[]" + nl : "[" + nl + String.join("," + nl, objects) + nl + "]" + nl;
    }

    /**
     * Записує JSON у файл з явним UTF-8.
     *
     * @param path  шлях
     * @param type  клас записів
     * @param items записи
     * @param <T>   тип записів
     * @throws DataStorageException якщо експорт або запис не вдалися
     */
    public static <T> void write(Path path, Class<T> type, List<? extends T> items) throws DataStorageException {
        String json = toJson(type, items);
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DataStorageException("Не вдалося записати JSON: " + path, e);
        }
    }

    private static String literal(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        return quote(value.toString());
    }

    private static String quote(String text) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.append('"').toString();
    }
}
