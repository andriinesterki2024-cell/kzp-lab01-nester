package ua.lpnu.kzp.storage;

/**
 * Перевірюваний виняток збереження даних: помилки файла, формату CSV, значень і рефлексії.
 *
 * <p>Повідомлення пояснює операцію, а {@link #getCause()} зберігає першопричину
 * ({@link java.io.IOException}, {@link NumberFormatException},
 * {@link java.time.format.DateTimeParseException} тощо).</p>
 */
public class DataStorageException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Створює виняток без першопричини.
     *
     * @param message опис помилки
     */
    public DataStorageException(String message) {
        super(message);
    }

    /**
     * Створює виняток зі збереженням першопричини.
     *
     * @param message опис операції, що не вдалася
     * @param cause   початкова технічна помилка
     */
    public DataStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
