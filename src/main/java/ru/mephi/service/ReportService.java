package ru.mephi.service;

import ru.mephi.model.Transaction;
import ru.mephi.model.Wallet;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportService {
    private final Wallet wallet;

    public ReportService(Wallet wallet) {
        this.wallet = wallet;
    }

    public void printTotal() {
        System.out.println("Общий доход: " + calculateTotal(Transaction.Type.INCOME, null));
        System.out.println("Общий расход: " + calculateTotal(Transaction.Type.EXPENSE, null));
    }

    public void printByCategory() {
        System.out.println("--- Доходы по категориям ---");
        printCategoryStats(Transaction.Type.INCOME);
        System.out.println("--- Расходы по категориям ---");
        printCategoryStats(Transaction.Type.EXPENSE);
    }

    public void printBudgets() {
        System.out.println("--- Бюджеты ---");
        if (wallet.getBudgets().isEmpty()) {
            System.out.println("Бюджеты не установлены.");
            return;
        }

        wallet.getBudgets().forEach((category, limit) -> {
            BigDecimal spent = wallet.getTransactions().stream()
                    .filter(t -> t.getType() == Transaction.Type.EXPENSE && t.getCategory().equals(category))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal remaining = limit.subtract(spent);
            System.out.println(category + ": Лимит " + limit + ", Остаток: " + remaining);
        });
    }

    public void printFiltered(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException("Список категорий пуст.");
        }

        boolean hasData = wallet.getTransactions().stream()
                .anyMatch(t -> categories.contains(t.getCategory()));

        if (!hasData) {
            System.out.println("По выбранным категориям нет данных.");
            return;
        }

        System.out.println("--- Статистика по выбранным категориям ---");
        System.out.println("Доходы: " + calculateTotal(Transaction.Type.INCOME, categories));
        System.out.println("Расходы: " + calculateTotal(Transaction.Type.EXPENSE, categories));
    }

    private BigDecimal calculateTotal(Transaction.Type type, List<String> categories) {
        return wallet.getTransactions().stream()
                .filter(t -> t.getType() == type)
                .filter(t -> categories == null || categories.contains(t.getCategory()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void printCategoryStats(Transaction.Type type) {
        Map<String, BigDecimal> stats = wallet.getTransactions().stream()
                .filter(t -> t.getType() == type)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        if (stats.isEmpty()) {
            System.out.println("Нет данных.");
        } else {
            stats.forEach((category, sum) -> System.out.println(category + ": " + sum));
        }
    }

    public void exportToFile(String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("Type,Category,Amount,Date\n");
            for (Transaction t : wallet.getTransactions()) {
                writer.write(t.getType() + "," + t.getCategory() + "," + t.getAmount() + "," + t.getDate() + "\n");
            }
            System.out.println("Данные успешно экспортированы в файл: " + filename);
        } catch (IOException e) {
            System.out.println("Ошибка при экспорте данных: " + e.getMessage());
        }
    }
}