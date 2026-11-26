package ua.lpnu.kzp;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import ua.lpnu.kzp.storage.Repository;

/**
 * Точка входу консольної програми «Ремонтна майстерня» (варіант 16).
 *
 * <p>Програма читає записи про ремонти з CSV-файла, перевіряє їх, обчислює
 * показники варіанта та виводить один і той самий звіт у консоль і файл.</p>
 */
public final class Main {

    /** Шлях до вхідного файла за замовчуванням. */
    static final Path DEFAULT_INPUT = Path.of("data", "input.csv");

    /** Шлях до файла звіту за замовчуванням. */
    static final Path DEFAULT_OUTPUT = Path.of("out", "report.txt");

    /** Код завершення: успіх. */
    static final int EXIT_OK = 0;

    /** Код завершення: помилка аргументів або введення-виведення. */
    static final int EXIT_ERROR = 1;

    /** Код завершення: у файлі немає жодного коректного запису. */
    static final int EXIT_NO_DATA = 2;

    /* Забороняє створення екземплярів службового класу. */
    private Main() {
    }

    /**
     * Запускає програму з аргументами командного рядка.
     *
     * @param args аргументи: {@code --help}, {@code --version},
     *             {@code --input <файл>}, {@code --output <файл>}
     */
    public static void main(String[] args) {
        int code = run(args, System.out, System.err);
        if (code != EXIT_OK) {
            System.exit(code);
        }
    }

    /**
     * Виконує всю логіку програми; винесено з {@code main}, щоб її можна було тестувати.
     *
     * @param args аргументи командного рядка
     * @param out  потік для звичайного виводу
     * @param err  потік для повідомлень про помилки
     * @return код завершення програми
     */
    static int run(String[] args, PrintStream out, PrintStream err) {
        Path input = DEFAULT_INPUT;
        Path output = DEFAULT_OUTPUT;
        int top = RepairQueries.DEFAULT_TOP;
        String findClient = null;
        Path exportCsv = null;
        Path exportJson = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--help" -> {
                    out.print(usage());
                    return EXIT_OK;
                }
                case "--version" -> {
                    out.printf("%s%n", versionText());
                    return EXIT_OK;
                }
                case "--input", "--output" -> {
                    // Після ключа обов'язково має йти шлях.
                    if (i + 1 >= args.length) {
                        err.printf("Помилка: після %s потрібно вказати шлях до файла%n", arg);
                        return EXIT_ERROR;
                    }
                    Path value = Path.of(args[++i]);
                    if ("--input".equals(arg)) {
                        input = value;
                    } else {
                        output = value;
                    }
                }
                case "--export", "--export-json" -> {
                    if (i + 1 >= args.length) {
                        err.printf("Помилка: після %s потрібно вказати шлях до файла%n", arg);
                        return EXIT_ERROR;
                    }
                    Path value = Path.of(args[++i]);
                    if ("--export".equals(arg)) {
                        exportCsv = value;
                    } else {
                        exportJson = value;
                    }
                }
                case "--top", "--find-client" -> {
                    if (i + 1 >= args.length) {
                        err.printf("Помилка: після %s потрібно вказати значення%n", arg);
                        return EXIT_ERROR;
                    }
                    String value = args[++i];
                    if ("--top".equals(arg)) {
                        try {
                            top = parseTop(value);
                        } catch (IllegalArgumentException e) {
                            err.printf("Помилка: %s%n", e.getMessage());
                            return EXIT_ERROR;
                        }
                    } else {
                        findClient = value;
                    }
                }
                default -> {
                    err.printf("Помилка: невідомий аргумент %s%n", arg);
                    err.print(usage());
                    return EXIT_ERROR;
                }
            }
        }

        List<String> lines;
        try {
            lines = FileReport.readLines(input);
        } catch (NoSuchFileException e) {
            err.printf("Помилка: вхідний файл не знайдено: %s%n", input);
            return EXIT_ERROR;
        } catch (IOException e) {
            err.printf("Помилка читання файла %s: %s%n", input, e.getMessage());
            return EXIT_ERROR;
        }

        // Читання → перевірка й обчислення → форматування: кожен крок в окремому класі.
        ParsedOrders parsed = RepairAnalyzer.parse(lines);
        // ЛР5: коректні заявки зберігаються в параметризованому сховищі, звіти працюють зі знімком.
        Repository<RepairOrder> repository = new Repository<>();
        repository.addAll(parsed.orders());
        List<RepairOrder> orders = repository.all();
        RepairSummary summary = RepairAnalyzer.summarize(orders, parsed.errors());
        String report = ReportFormatter.format(summary);
        out.print(report);
        // Поліморфні показники друкуються лише в консоль: файл звіту не змінюється.
        out.print(PolymorphicReport.format(orders));
        out.print(StreamReport.format(orders, top, findClient));
        if (exportCsv != null || exportJson != null) {
            out.print(ExportReport.run(orders, exportCsv, exportJson));
        }

        try {
            FileReport.writeReport(output, report);
            out.printf("Звіт збережено у файл: %s%n", output);
        } catch (IOException e) {
            err.printf("Помилка запису звіту %s: %s%n", output, e.getMessage());
            return EXIT_ERROR;
        }

        return summary.validCount() == 0 ? EXIT_NO_DATA : EXIT_OK;
    }

    /**
     * Формує текст довідки.
     *
     * @return довідка про параметри запуску
     */
    static String usage() {
        return String.format(
                "Використання: java -jar lab01-5.0.0.jar [--help] [--version] "
                        + "[--input <файл>] [--output <файл>]%n"
                        + "  --help           показати цю довідку%n"
                        + "  --version        показати версію програми та номер збірки%n"
                        + "  --input <файл>   вхідний CSV (за замовчуванням data/input.csv)%n"
                        + "  --output <файл>  файл звіту (за замовчуванням out/report.txt)%n"
                        + "  --top <N>        кількість найдорожчих заявок (за замовчуванням 5)%n"
                        + "  --find-client <ім'я> знайти першу заявку клієнта%n"
                        + "  --export <файл>  експорт заявок у CSV і перевірка round-trip%n"
                        + "  --export-json <файл> експорт заявок у JSON%n"
                        + "Формат рядка: client;device;diagnosis;hours;price[;kind[;date[;status]]]%n"
                        + "  kind = WARRANTY або PAID, date = YYYY-MM-DD, status = new, in_progress або done%n");
    }

    /**
     * Перевіряє значення N для top-N.
     *
     * @param text текст аргументу
     * @return N, не від'ємне
     * @throws IllegalArgumentException якщо текст не є цілим числом або N від'ємне
     */
    static int parseTop(String text) {
        int value;
        try {
            value = Integer.parseInt(text.strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("--top має бути цілим числом", e);
        }
        if (value < 0) {
            throw new IllegalArgumentException("--top не може бути від'ємним");
        }
        return value;
    }

    /**
     * Повертає версію програми та номер CI-збірки з ресурсу {@code version.properties}.
     *
     * @return рядок на зразок {@code lab01 1.0.0 (build 12)}
     */
    static String versionText() {
        Properties props = new Properties();
        try (InputStream in = Main.class.getResourceAsStream("/version.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            // Відсутність ресурсу не є критичною: нижче підставляються значення за замовчуванням.
            props.clear();
        }
        String version = props.getProperty("version", "unknown");
        String build = props.getProperty("build", "local");
        return String.format("lab01 %s (build %s)", version, build);
    }
}
