package me.dustin.jex.gui.account.account;

import java.util.ArrayList;

public enum MinecraftAccountManager {

    INSTANCE;

    private ArrayList<MinecraftAccount> minecraftAccounts = new ArrayList<>();

    public ArrayList<MinecraftAccount> getAccounts() {
        return minecraftAccounts;
    }

    public MinecraftAccount getAccount(String username) {
        for (MinecraftAccount account : getAccounts()) {
            if (account.getUsername().equalsIgnoreCase(username))
                return account;
        }
        return null;
    }

    public ArrayList<MinecraftAccount> accountsContainChars(String chars) {
        ArrayList<MinecraftAccount> accountList = new ArrayList<>();
        for (MinecraftAccount account : getAccounts()) {
            if (account.getUsername().toLowerCase().contains(chars.toLowerCase()))
                accountList.add(account);
        }
        return accountList;
    }
}
