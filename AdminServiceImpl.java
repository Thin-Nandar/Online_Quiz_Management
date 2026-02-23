package server;

import common.User;
import common.Question;
import common.Activity;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class AdminServiceImpl extends UnicastRemoteObject implements AdminService {

    private static final String QUESTION_FILE = "questions.txt";
    private static final String USER_FILE = "users.txt";
    private static final String ACTIVITY_FILE = "activities.txt";

    private List<User> users;
    private List<Activity> activities;
    private List<Question> questions;
    private int examTime = 60; // default 60 seconds


    public AdminServiceImpl() throws RemoteException {
        users = new ArrayList<>();
        activities = new ArrayList<>();
        questions = new ArrayList<>();

        loadUsersFromFile();
        loadQuestionsFromFile();
        loadActivitiesFromFile();
    }

    @Override
    public void setExamTime(int seconds) throws RemoteException {
        this.examTime = seconds;
    }

    @Override
    public int getExamTime() throws RemoteException {
        return this.examTime;
    }


    // =================== USER METHODS ===================
    @Override
    public synchronized boolean addUser(String username, String password) throws RemoteException {
        if (username == null || password == null) return false;
        username = username.trim();
        password = password.trim();

        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) return false; // already exists
        }

        users.add(new User(username, password));
        saveUsersToFile();
        return true;
    }

    @Override
    public synchronized void editUser(String oldUsername, User newUser) throws RemoteException {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equals(oldUsername)) {
                users.set(i, newUser);
                saveUsersToFile();
                return;
            }
        }
    }

    @Override
    public synchronized void deleteUser(String username) throws RemoteException {
        users.removeIf(u -> u.getUsername().equals(username));
        saveUsersToFile();
    }

    @Override
    public synchronized void resetPassword(String username, String newPass) throws RemoteException {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                u.setPassword(newPass);
                saveUsersToFile();
                return;
            }
        }
    }

    @Override
    public List<User> getAllUsers() throws RemoteException {
        loadUsersFromFile();
        return new ArrayList<>(users);
    }

    @Override
    public List<User> searchUsers(String keyword) throws RemoteException {
        return users.stream()
                .filter(u -> u.getUsername().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean login(String username, String password) throws RemoteException {
        loadUsersFromFile(); // reload to reflect any file changes
        for (User u : users) {
            if (u.getUsername().equals(username) && u.getPassword().equals(password)) return true;
        }
        return false;
    }

    private void loadUsersFromFile() {
        users.clear();
        File file = new File(USER_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 2) users.add(new User(parts[0].trim(), parts[1].trim()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveUsersToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (User u : users) {
                bw.write(u.getUsername() + "|" + u.getPassword());
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =================== QUESTION METHODS ===================
    @Override
    public synchronized void addQuestion(Question q) throws RemoteException {
        if (q != null) {
            questions.add(q);
            saveQuestionsToFile();
        }
    }

    @Override
    public synchronized void editQuestion(int index, Question q) throws RemoteException {
        if (index >= 0 && index < questions.size()) {
            questions.set(index, q);
            saveQuestionsToFile();
        }
    }

    @Override
    public synchronized void deleteQuestion(int index) throws RemoteException {
        if (index >= 0 && index < questions.size()) {
            questions.remove(index);
            saveQuestionsToFile();
        }
    }

    public List<Question> getAllQuestions() throws RemoteException {
        loadQuestionsFromFile();
        return new ArrayList<>(questions);
    }

    private void loadQuestionsFromFile() {
        questions.clear();
        File file = new File(QUESTION_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6) {
                    questions.add(new Question(
                            parts[0].trim(),
                            parts[1].trim(),
                            parts[2].trim(),
                            parts[3].trim(),
                            parts[4].trim(),
                            parts[5].trim()
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveQuestionsToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(QUESTION_FILE))) {
            for (Question q : questions) {
                bw.write(String.join("|",
                        q.getQuestionText(),
                        q.getOptionA(),
                        q.getOptionB(),
                        q.getOptionC(),
                        q.getOptionD(),
                        q.getCorrectAnswer()));
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =================== ACTIVITY METHODS ===================
    @Override
    public void logActivity(Activity act) throws RemoteException {
        if (act == null) return;
        activities.add(act);
        saveActivitiesToFile();
    }

    @Override
    public List<Activity> getAllActivities() throws RemoteException {
        return new ArrayList<>(activities);
    }
    

    private void loadActivitiesFromFile() {
        activities.clear();
        File file = new File(ACTIVITY_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                activities.add(Activity.fromString(line));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveActivitiesToFile() {
        if (activities.isEmpty()) return;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ACTIVITY_FILE))) {
            for (Activity a : activities) {
                bw.write(a.toString());
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
