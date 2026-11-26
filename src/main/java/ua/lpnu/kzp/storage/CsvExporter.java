package ua.lpnu.kzp.storage;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Універсальний рефлексивний CSV-експортер для будь-якого плоского класу з полями {@link CsvColumn}.
 *
 * <p>Не містить назв предметних класів. Роздільник полів — кома; поле з комою, лапкою або
 * перенесенням рядка береться в лапки, а лапка всередині подвоюється. Записи розділяються
 * системним роздільником рядків; {@link CsvParser} приймає {@code \n}, {@code \r\n} і {@code \r}.</p>
 */
public final class CsvExporter {

    /* Забороняє створення екземплярів службового класу. */
    private CsvExporter() {
    }

    /**
     * Екранує одне значення за правилами CSV.
     *
     * @param value значення (null стає порожнім полем)
     * @return текст поля
     */
    public static String escape(Object value) {
        String text = Objects.toString(value, "");
        boolean quoted = text.contains(",") || text.contains("\"")
                || text.contains("\r") || text.contains("\n");
        return quoted ? "\"" + text.replace("\"", "\"\"") + "\"" : text;
    }

    /**
     * Формує CSV-документ із заголовком.
     *
     * @param type  клас записів; потрібен окремо, щоб побудувати заголовок і для порожнього списку
     * @param items записи
     * @param <T>   тип записів
     * @return текст CSV
     * @throws DataStorageException якщо клас не має анотованих полів або поле неможливо прочитати
     */
    public static <T> String toCsv(Class<T> type, List<? extends T> items) throws DataStorageException {
        List<Field> fields = AnnotatedFields.of(type);
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        List<String> header = new ArrayList<>();
        for (Field field : fields) {
            header.add(escape(field.getAnnotation(CsvColumn.class).value()));
        }
        sb.append(String.join(",", header)).append(nl);
        for (T item : items) {
            List<String> cells = new ArrayList<>();
            for (Field field : fields) {
                cells.add(escape(AnnotatedFields.read(field, item)));
            }
            sb.append(String.join(",", cells)).append(nl);
        }
        return sb.toString();
    }

    /**
     * Записує CSV у файл через {@link Files} з явним UTF-8; старий вміст обрізається.
     *
     * @param path  шлях до файла
     * @param type  клас записів
     * @param items записи
     * @param <T>   тип записів
     * @throws DataStorageException якщо експорт або запис не вдалися (IOException стає причиною)
     */
    public static <T> void write(Path path, Class<T> type, List<? extends T> items) throws DataStorageException {
        String csv = toCsv(type, items);
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, csv, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new DataStorageException("Не вдалося записати CSV: " + path, e);
        }
    }
}
