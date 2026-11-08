package ru.mephi.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wallet {
    private List<Transaction> transactions;
    private Map<String, BigDecimal> budgets;

    public Wallet() {
        this.transactions = new ArrayList<>();
        this.budgets = new HashMap<>();
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public Map<String, BigDecimal> getBudgets() {
        return budgets;
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public void setBudget(String category, BigDecimal limit) {
        budgets.put(category, limit);
    }

    public BigDecimal getBalance() {
        BigDecimal income = transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expense = transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return income.subtract(expense);
    }
}