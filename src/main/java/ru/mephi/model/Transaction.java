package ru.mephi.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    public enum Type { INCOME, EXPENSE }

    private Type type;
    private String category;
    private BigDecimal amount;
    private LocalDateTime date;

    public Transaction(Type type, String category, BigDecimal amount) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = LocalDateTime.now();
    }

    public Type getType() { return type; }
    public String getCategory() { return category; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getDate() { return date; }
}