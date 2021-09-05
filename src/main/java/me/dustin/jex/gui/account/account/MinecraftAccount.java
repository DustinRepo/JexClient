package me.dustin.jex.gui.account.account;

public class MinecraftAccount {
    public String username;
    public int loginCount;
    public long lastUsed;
    public static class MicrosoftAccount extends MinecraftAccount {
        public String accessToken;
        public String refreshToken;
        public String uuid;

        public MicrosoftAccount(String name, String token, String refresh, String uuid) {
            this.username = name;
            this.accessToken = token;
            this.refreshToken = refresh;
            this.uuid = uuid.replace("-", "");
        }
    }

    public static class MojangAccount extends MinecraftAccount {
        private String email, password;
        private boolean isCracked;

        public MojangAccount(String username) {
            this.username = username;
            this.email = "";
            this.password = "";
            this.isCracked = true;
        }

        public MojangAccount(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
