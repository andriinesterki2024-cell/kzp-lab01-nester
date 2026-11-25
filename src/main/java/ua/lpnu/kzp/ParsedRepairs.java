package ua.lpnu.kzp;

import java.util.List;

/**
 * Результат розбору файла: коректні записи (різних підтипів) і повідомлення про хибні рядки.
 *
 * @param repairs коректні записи у порядку файла
 * @param errors  повідомлення «Рядок N: причина»
 */
public record ParsedRepairs(List<Repair> repairs, List<String> errors) {

    /**
     * Робить незмінні копії списків, щоб результат не можна було змінити ззовні.
     */
    public ParsedRepairs {
        repairs = List.copyOf(repairs);
        errors = List.copyOf(errors);
    }
}
