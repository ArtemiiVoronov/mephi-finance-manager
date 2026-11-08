package ru.mephi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mephi.exception.ValidationException;
import ru.mephi.model.Transaction;
import ru.mephi.model.User;
import ru.mephi.model.Wallet;
import ru.mephi.repository.DataStorage;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FinanceServiceTest {
    private DataStorage storage;
    private FinanceService financeService;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        storage = new DataStorage();
        wallet = new Wallet();
        storage.getUsers().put("testUser", new User("testUser", "pass"));
        storage.getWallets().put("testUser", wallet);
        financeService = new FinanceService(wallet, storage);
    }

    @Test
    void testAddIncome() {
        financeService.addTransaction(Transaction.Type.INCOME, "Зарплата", new BigDecimal("1000"));
        assertEquals(1, wallet.getTransactions().size());
        assertEquals(new BigDecimal("1000"), wallet.getBalance());
    }

    @Test
    void testAddExpenseExceedsBudget() {
        financeService.setBudget("Еда", new BigDecimal("500"));
        financeService.addTransaction(Transaction.Type.EXPENSE, "Еда", new BigDecimal("600"));
        assertEquals(new BigDecimal("-600"), wallet.getBalance());
    }

    @Test
    void testNegativeAmountThrowsException() {
        assertThrows(ValidationException.class, () -> {
            financeService.addTransaction(Transaction.Type.INCOME, "Зарплата", new BigDecimal("-100"));
        });
    }

    @Test
    void testTransferBetweenWallets() {
        storage.getWallets().put("targetUser", new Wallet());
        financeService.addTransaction(Transaction.Type.INCOME, "Зарплата", new BigDecimal("1000"));

        financeService.transfer("targetUser", new BigDecimal("500"));

        assertEquals(new BigDecimal("500"), wallet.getBalance());
        assertEquals(new BigDecimal("500"), storage.getWallets().get("targetUser").getBalance());
    }

    @Test
    void testEditBudget() {
        financeService.setBudget("Еда", new BigDecimal("1000"));
        financeService.editBudget("Еда", new BigDecimal("2000"));
        assertEquals(new BigDecimal("2000"), wallet.getBudgets().get("Еда"));
    }
}