package ru.mephi.service;

import ru.mephi.exception.ValidationException;
import ru.mephi.model.User;
import ru.mephi.model.Wallet;
import ru.mephi.repository.DataStorage;

public class AuthService {
    private final DataStorage storage;

    public AuthService(DataStorage storage) {
        this.storage = storage;
    }

    public User login(String login, String password) {
        if (login == null || login.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new ValidationException("Логин и пароль не могут быть пустыми.");
        }

        User user = storage.getUsers().get(login);
        if (user == null || !user.getPassword().equals(password)) {
            throw new ValidationException("Неверный логин или пароль.");
        }

        return user;
    }

    public void register(String login, String password) {
        if (login == null || login.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new ValidationException("Логин и пароль не могут быть пустыми.");
        }
        if (password.length() < 4) {
            throw new ValidationException("Пароль должен содержать минимум 4 символа.");
        }
        if (storage.getUsers().containsKey(login)) {
            throw new ValidationException("Пользователь с таким логином уже существует.");
        }

        storage.getUsers().put(login, new User(login, password));
        storage.getWallets().put(login, new Wallet());
    }
}