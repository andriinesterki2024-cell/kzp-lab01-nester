package ua.lpnu.kzp.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Параметризоване сховище в пам'яті.
 *
 * <p>Тип елементів {@code T} задається під час створення: {@code Repository<Repair>}
 * приймає лише ремонти, і компілятор перевіряє це без приведень типів і raw types.</p>
 *
 * @param <T> тип елементів сховища
 */
public final class Repository<T> {

    private final List<T> items = new ArrayList<>();

    /**
     * Створює порожнє сховище.
     */
    public Repository() {
        // Список ініціалізовано в оголошенні поля.
    }

    /**
     * Додає елемент у кінець сховища.
     *
     * @param item елемент, не {@code null}
     * @throws NullPointerException якщо {@code item == null}
     */
    public void add(T item) {
        items.add(Objects.requireNonNull(item, "item"));
    }

    /**
     * Додає всі елементи в тому самому порядку.
     *
     * @param newItems елементи, жоден не {@code null}
     */
    public void addAll(List<? extends T> newItems) {
        newItems.forEach(this::add);
    }

    /**
     * Повертає незмінний знімок вмісту: зовнішній код не може змінити сховище
     * в обхід {@link #add}, а пізніші додавання не змінюють уже отриманий знімок.
     *
     * @return незмінна копія елементів у порядку додавання
     */
    public List<T> all() {
        return List.copyOf(items);
    }

    /**
     * Шукає елементи за умовою. {@code Predicate<? super T>} дозволяє передати умову,
     * написану для {@code T} або будь-якого його супертипу.
     *
     * @param condition умова відбору
     * @return незмінний список елементів, що задовольняють умову, у порядку додавання
     */
    public List<T> find(Predicate<? super T> condition) {
        Objects.requireNonNull(condition, "condition");
        return items.stream().filter(condition).toList();
    }

    /**
     * Повертає кількість елементів.
     *
     * @return розмір сховища
     */
    public int size() {
        return items.size();
    }
}
