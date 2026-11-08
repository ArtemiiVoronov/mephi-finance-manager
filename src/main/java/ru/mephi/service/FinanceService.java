package ru.mephi.service;

import ru.mephi.exception.ValidationException;
import ru.mephi.model.Transaction;
import ru.mephi.model.Wallet;
import ru.mephi.repository.DataStorage;

import java.math.BigDecimal;

public class FinanceService {
    private final Wallet wallet;
    private final DataStorage storage;

    public FinanceService(Wallet wallet, DataStorage storage) {
        this.wallet = wallet;
        this.storage = storage;
    }

    public void addTransaction(Transaction.Type type, String category, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Сумма должна быть больше нуля.");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Категория не может быть пустой.");
        }

        wallet.addTransaction(new Transaction(type, category, amount));

        if (type == Transaction.Type.EXPENSE) {
            checkBudget(category);
        }
    }

    public void setBudget(String category, BigDecimal limit) {
        if (limit.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Бюджет не может быть отрицательным.");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Категория не может быть пустой.");
        }
        wallet.setBudget(category, limit);
    }

    public void editBudget(String category, BigDecimal newLimit) {
        if (!wallet.getBudgets().containsKey(category)) {
            throw new ValidationException("Бюджет для данной категории не найден.");
        }
        if (newLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Бюджет не может быть отрицательным.");
        }
        wallet.setBudget(category, newLimit);
    }

    public void transfer(String toLogin, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Сумма перевода должна быть положительной.");
        }
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new ValidationException("Недостаточно средств для перевода.");
        }

        Wallet toWallet = storage.getWallets().get(toLogin);
        if (toWallet == null) {
            throw new ValidationException("Получатель не найден.");
        }

        wallet.addTransaction(new Transaction(Transaction.Type.EXPENSE, "Перевод", amount));
        toWallet.addTransaction(new Transaction(Transaction.Type.INCOME, "Перевод", amount));
    }

    private void checkBudget(String category) {
        BigDecimal limit = wallet.getBudgets().get(category);
        if (limit == null) {
            return;
        }

        BigDecimal spent = wallet.getTransactions().stream()
                .filter(t -> t.getType() == Transaction.Type.EXPENSE && t.getCategory().equals(category))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (spent.compareTo(limit) > 0) {
            System.out.println("ВНИМАНИЕ! Вы превысили бюджет по категории '" + category + "'!");
        } else if (spent.compareTo(limit.multiply(new BigDecimal("0.8"))) >= 0) {
            System.out.println("ПРЕДУПРЕЖДЕНИЕ: Вы потратили более 80% бюджета на '" + category + "'.");
        }
    }

    public Wallet getWallet() {
        return wallet;
    }
}