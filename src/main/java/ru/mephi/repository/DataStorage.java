package ru.mephi.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ru.mephi.model.User;
import ru.mephi.model.Wallet;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class DataStorage {
    private Map<String, User> users;
    private Map<String, Wallet> wallets;
    private final String FILE_PATH = "data.json";
    private final Gson gson;

    public DataStorage() {
        this.users = new HashMap<>();
        this.wallets = new HashMap<>();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public Map<String, Wallet> getWallets() {
        return wallets;
    }

    public void saveData() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            Map<String, Object> data = new HashMap<>();
            data.put("users", users);
            data.put("wallets", wallets);
            gson.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения данных: " + e.getMessage());
        }
    }

    public void loadData() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return;
        }
        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> data = gson.fromJson(reader, type);
            if (data != null) {
                if (data.containsKey("users")) {
                    String usersJson = gson.toJson(data.get("users"));
                    users = gson.fromJson(usersJson, new TypeToken<Map<String, User>>(){}.getType());
                }
                if (data.containsKey("wallets")) {
                    String walletsJson = gson.toJson(data.get("wallets"));
                    wallets = gson.fromJson(walletsJson, new TypeToken<Map<String, Wallet>>(){}.getType());
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки данных: " + e.getMessage());
        }
    }
}