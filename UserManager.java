package server;



import java.io.*;
import java.util.*;

public class UserManager {
    private static final String FILE_NAME = "users.csv";
    private Map<String, String> users;

    public UserManager() {
        users = loadUsersFromFile();
    }

    private Map<String, String> loadUsersFromFile() {
        Map<String, String> map = new HashMap<>();
        File file = new File(FILE_NAME);
        if (!file.exists()) return map;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    map.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    private void saveUserToFile(String username, String password) {
        try (FileWriter fw = new FileWriter(FILE_NAME, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(username + "," + password);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean addUser(String username, String password) {
        if (users.containsKey(username)) return false;
        users.put(username, password);
        saveUserToFile(username, password);
        return true;
    }

    public synchronized boolean validateUser(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(password);
    }
}

