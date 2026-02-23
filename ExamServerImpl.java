package server;

import common.ExamInterface;
import common.Question;
import common.User;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * Exam RMI service implementation.
 * - Loads/saves questions from questions.txt (| delimiter)
 * - Loads users from users.txt (username,password)
 * - Supports both legacy getQuestions() (String[]) and object getQuestions1() (Question)
 */
public class ExamServerImpl extends UnicastRemoteObject implements ExamInterface {
    private int examTime = 120; // default 120s

    private Map<String, String> users; // username -> password
    private List<Question> questions;   
    private static final long serialVersionUID = 1L;

    private static final String QUESTIONS_FILE = "questions.txt";
    private static final String USER_FILE = "users.txt";

    public ExamServerImpl() throws RemoteException {
        super();
        users = new HashMap<>();
        questions = new ArrayList<>();

        // Load data from files
        loadUsersFromFile();
        loadQuestionsFromFile();

        System.out.println("Users loaded: " + users);
        System.out.println("Questions loaded: " + questions.size());

        // Default users if needed
        if (!users.containsKey("admin")) users.put("admin", "123");
        if (!users.containsKey("student")) users.put("student", "abc");
    }
    
    



    private void loadQuestionsFromFile() {
        File f = new File(QUESTIONS_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            questions.clear();
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
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(QUESTIONS_FILE))) {
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

    @Override
    public synchronized void addQuestion(Question question) throws RemoteException {
        questions.add(question);
        saveQuestionsToFile();
    }

    @Override
    public synchronized void editQuestion(int index, Question newQuestion) throws RemoteException {
        if (index >= 0 && index < questions.size()) {
            questions.set(index, newQuestion);
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

    @Override
    public synchronized List<Question> getQuestions1() throws RemoteException {
        loadQuestionsFromFile(); // always refresh
        return new ArrayList<>(questions);
    }

    @Override
    public synchronized void addQuestion(String[] questionData) throws RemoteException {
        if (questionData == null || questionData.length < 6) return;
        Question q = new Question(
            questionData[0].trim(),
            questionData[1].trim(),
            questionData[2].trim(),
            questionData[3].trim(),
            questionData[4].trim(),
            questionData[5].trim()
        );
        addQuestion(q); // reuse object method
    }

    @Override
    public synchronized int submitAnswers(List<String> answers) throws RemoteException {
        loadQuestionsFromFile();
        if (answers == null) return 0;
        int n = Math.min(answers.size(), questions.size());
        int score = 0;
        for (int i = 0; i < n; i++) {
            String userAns = answers.get(i);
            String correct = questions.get(i).getCorrectAnswer();
            if (userAns != null && userAns.equalsIgnoreCase(correct)) score++;
        }
        return score;
    }

    @Override
    public List<String[]> getQuestions() throws RemoteException {
        loadQuestionsFromFile();
        List<String[]> out = new ArrayList<>();
        for (Question q : questions) {
            out.add(new String[]{
                q.getQuestionText(),
                q.getOptionA(),
                q.getOptionB(),
                q.getOptionC(),
                q.getOptionD(),
                q.getCorrectAnswer()
            });
        }
        return out;
    }

    // =================== USERS ===================
    private void loadUsersFromFile() {
        File file = new File(USER_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 2) {
                    users.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void saveUsersToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                bw.write(entry.getKey() + "|" + entry.getValue());
                bw.newLine();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public synchronized boolean addUser(String username, String password) throws RemoteException {
        if (username == null || password == null) return false;

        username = username.trim();
        password = password.trim();

        if (users.containsKey(username)) return false;

        users.put(username, password);
        saveUsersToFile();
        return true;
    }

    @Override
    public boolean login(String username, String password) throws RemoteException {
        loadUsersFromFile(); // refresh from file
        if (username == null || password == null) return false;
        username = username.trim();
        password = password.trim();
        return users.containsKey(username) && users.get(username).equals(password);
    }

    @Override
    public synchronized void editUser(String oldUsername, User u) throws RemoteException {
        if (users.containsKey(oldUsername)) {
            users.remove(oldUsername);
            users.put(u.getUsername(), u.getPassword());
            saveUsersToFile();
        }
    }
    @Override
    public synchronized int getExamTime() throws RemoteException {
        return examTime;
    }

    @Override
    public synchronized void setExamTime(int seconds) throws RemoteException {
        this.examTime = seconds;
        System.out.println("Exam time updated to: " + seconds + " seconds");
    }


    @Override
    public List<User> searchUsers(String keyword) throws RemoteException {
        List<User> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : users.entrySet()) {
            if (keyword == null || entry.getKey().toLowerCase().contains(keyword.toLowerCase())) {
                list.add(new User(entry.getKey(), entry.getValue()));
            }
        }
        return list;
    }

    @Override
    public List<String[]> getUsers() throws RemoteException {
        List<String[]> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : users.entrySet()) {
            list.add(new String[]{entry.getKey(), entry.getValue()});
        }
        return list;
    }

	
}
