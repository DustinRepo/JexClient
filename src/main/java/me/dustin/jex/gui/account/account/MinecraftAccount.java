package me.dustin.jex.gui.account.account;

public class MinecraftAccount {
    public String username;
    public int loginCount;
    public long lastUsed;
    protected String email, password;
    public static class MicrosoftAccount extends MinecraftAccount {
        private String accessToken;
        private String refreshToken;
        private String uuid;

        public MicrosoftAccount(String name, String email, String password, String token, String refresh, String uuid) {
            this.username = name;
            this.accessToken = token;
            this.email = email;
            this.password = password;
            this.refreshToken = refresh;
            this.uuid = uuid.replace("-", "");
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public String getUUID() {
            return uuid;
        }

        public void setUUID(String uuid) {
            this.uuid = uuid;
        }
    }

    public static class MojangAccount extends MinecraftAccount {
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
