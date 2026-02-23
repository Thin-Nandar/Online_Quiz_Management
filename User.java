package common;

import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private String password;

    // Constructor for username + password
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getter & Setter
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
