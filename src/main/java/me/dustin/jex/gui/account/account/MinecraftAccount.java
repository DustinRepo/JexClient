package me.dustin.jex.gui.account.account;

public class MinecraftAccount {

    private String username, email, password;
    private boolean isCracked;

    public MinecraftAccount(String username) {
        this.username = username;
        this.email = "";
        this.password = "";
        this.isCracked = true;
    }

    public MinecraftAccount(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isCracked() {
        return isCracked;
    }

    public void setCracked(boolean cracked) {
        isCracked = cracked;
    }
}
