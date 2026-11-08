package ru.mephi.ui;

import ru.mephi.exception.ValidationException;
import ru.mephi.model.Transaction;
import ru.mephi.model.User;
import ru.mephi.repository.DataStorage;
import ru.mephi.service.AuthService;
import ru.mephi.service.FinanceService;
import ru.mephi.service.ReportService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private final Scanner scanner;
    private final DataStorage storage;
    private final AuthService authService;
    private User currentUser;
    private FinanceService financeService;
    private ReportService reportService;

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
        this.storage = new DataStorage();
        this.authService = new AuthService(storage);
    }

    public void start() {
        storage.loadData();
        System.out.println("=== Добро пожаловать в систему управления личными финансами ===");

        while (true) {
            if (currentUser == null) {
                showAuthMenu();
            } else {
                showMainMenu();
            }
        }
    }

    private void showAuthMenu() {
        System.out.println("\n--- Меню авторизации ---");
        System.out.println("1. Войти");
        System.out.println("2. Зарегистрироваться");
        System.out.println("0. Выход");
        System.out.print("Выберите действие: ");

        String choice = scanner.nextLine();
        try {
            if ("1".equals(choice)) {
                login();
            } else if ("2".equals(choice)) {
                register();
            } else if ("0".equals(choice)) {
                storage.saveData();
                System.out.println("Данные сохранены. До свидания!");
                System.exit(0);
            } else {
                System.out.println("Неверная команда. Попробуйте снова.");
            }
        } catch (ValidationException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void showMainMenu() {
        System.out.println("\n--- Главное меню [" + currentUser.getLogin() + "] ---");
        System.out.println("1. Добавить доход");
        System.out.println("2. Добавить расход");
        System.out.println("3. Установить бюджет");
        System.out.println("4. Редактировать бюджет");
        System.out.println("5. Показать статистику");
        System.out.println("6. Фильтр по категориям");
        System.out.println("7. Перевод средств");
        System.out.println("8. Экспорт в CSV");
        System.out.println("9. Помощь");
        System.out.println("0. Выйти из аккаунта");
        System.out.print("Выберите действие: ");

        String choice = scanner.nextLine();
        try {
            switch (choice) {
                case "1": handleTransaction(Transaction.Type.INCOME); break;
                case "2": handleTransaction(Transaction.Type.EXPENSE); break;
                case "3": handleSetBudget(); break;
                case "4": handleEditBudget(); break;
                case "5": handleShowStats(); break;
                case "6": handleFilter(); break;
                case "7": handleTransfer(); break;
                case "8": reportService.exportToFile("report.csv"); break;
                case "9": showHelp(); break;
                case "0":
                    currentUser = null;
                    financeService = null;
                    reportService = null;
                    System.out.println("Вы вышли из аккаунта.");
                    break;
                default: System.out.println("Неверная команда.");
            }
        } catch (ValidationException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: Введите корректное числовое значение.");
        } catch (Exception e) {
            System.out.println("Непредвиденная ошибка: " + e.getMessage());
        }
    }

    private void login() {
        System.out.print("Логин: ");
        String login = scanner.nextLine();
        System.out.print("Пароль: ");
        String password = scanner.nextLine();

        currentUser = authService.login(login, password);
        financeService = new FinanceService(storage.getWallets().get(currentUser.getLogin()), storage);
        reportService = new ReportService(financeService.getWallet());
        System.out.println("Успешный вход.");
    }

    private void register() {
        System.out.print("Придумайте логин: ");
        String login = scanner.nextLine();
        System.out.print("Придумайте пароль: ");
        String password = scanner.nextLine();

        authService.register(login, password);
        System.out.println("Регистрация успешна. Теперь вы можете войти.");
    }

    private void handleTransaction(Transaction.Type type) {
        System.out.print("Введите категорию: ");
        String category = scanner.nextLine();
        System.out.print("Введите сумму: ");
        BigDecimal amount = new BigDecimal(scanner.nextLine());

        financeService.addTransaction(type, category, amount);
        System.out.println("Операция добавлена.");
    }

    private void handleSetBudget() {
        System.out.print("Введите категорию: ");
        String category = scanner.nextLine();
        System.out.print("Введите лимит бюджета: ");
        BigDecimal limit = new BigDecimal(scanner.nextLine());

        financeService.setBudget(category, limit);
        System.out.println("Бюджет установлен.");
    }

    private void handleEditBudget() {
        System.out.print("Введите существующую категорию: ");
        String category = scanner.nextLine();
        System.out.print("Введите новый лимит: ");
        BigDecimal newLimit = new BigDecimal(scanner.nextLine());

        financeService.editBudget(category, newLimit);
        System.out.println("Бюджет обновлен.");
    }

    private void handleShowStats() {
        reportService.printTotal();
        reportService.printByCategory();
        reportService.printBudgets();
    }

    private void handleFilter() {
        System.out.print("Введите категории через запятую: ");
        String input = scanner.nextLine();
        List<String> categories = Arrays.asList(input.split("\\s*,\\s*"));
        reportService.printFiltered(categories);
    }

    private void handleTransfer() {
        System.out.print("Введите логин получателя: ");
        String toLogin = scanner.nextLine();
        System.out.print("Введите сумму перевода: ");
        BigDecimal amount = new BigDecimal(scanner.nextLine());

        financeService.transfer(toLogin, amount);
        System.out.println("Перевод успешно выполнен.");
    }

    private void showHelp() {
        System.out.println("\n--- Справка ---");
        System.out.println("1. Добавить доход - вносит поступление денег.");
        System.out.println("2. Добавить расход - вносит трату.");
        System.out.println("3. Установить бюджет - задает лимит для новой категории.");
        System.out.println("4. Редактировать бюджет - меняет лимит для существующей.");
        System.out.println("5. Показать статистику - общие суммы и остатки по бюджетам.");
        System.out.println("6. Фильтр по категориям - показывает суммы по выбранным категориям.");
        System.out.println("7. Перевод средств - переводит деньги другому пользователю.");
        System.out.println("8. Экспорт в CSV - сохраняет все операции в файл report.csv.");
        System.out.println("9. Помощь - выводит эту справку.");
        System.out.println("0. Выйти из аккаунта - возврат в меню авторизации.");
    }
}