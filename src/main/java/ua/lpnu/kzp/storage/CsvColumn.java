package ua.lpnu.kzp.storage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Позначає поле, яке експортується в CSV/JSON, і задає назву стовпця.
 *
 * <p>{@code RUNTIME} залишає анотацію доступною для рефлексії під час виконання,
 * {@code FIELD} дозволяє ставити її лише над полями. Сама анотація нічого не виконує —
 * її читає {@link AnnotatedFields}.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CsvColumn {

    /**
     * Назва стовпця в заголовку CSV і ключ у JSON.
     *
     * @return назва стовпця
     */
    String value();
}
