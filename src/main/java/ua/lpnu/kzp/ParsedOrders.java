package ua.lpnu.kzp;

import java.util.List;

/**
 * Результат розбору файла: коректні заявки різних підтипів і повідомлення про хибні рядки.
 *
 * @param orders коректні заявки у порядку файла
 * @param errors повідомлення «Рядок N: причина»
 */
public record ParsedOrders(List<RepairOrder> orders, List<String> errors) {

    /**
     * Робить незмінні копії списків, щоб результат не можна було змінити ззовні.
     */
    public ParsedOrders {
        orders = List.copyOf(orders);
        errors = List.copyOf(errors);
    }
}
