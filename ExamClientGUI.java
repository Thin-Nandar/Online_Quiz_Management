package client;

import common.ExamInterface;
import common.Question;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class ExamClientGUI extends JFrame {

    private String username;
    private ExamInterface examService;
    private List<Question> questionsList;
    private Map<Integer, String> userAnswers;
    private int currentQuestion = 0;

    private JTextArea questionArea;
    private JRadioButton[] options;
    private ButtonGroup optionGroup;
    private JButton btnNext, btnBack;
    private JLabel lblTimer;

    private ScheduledExecutorService scheduler;
    private int timeLeft = 60;

    public ExamClientGUI(String username, ExamInterface service, int examTime) throws Exception {
        this.username = username;
        this.examService = service;
        this.timeLeft = examService.getExamTime();

        setTitle("Welcome " + username);
        setSize(850, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        startExam();
    }

    private void initComponents() throws Exception {
        // ===== Main Panel =====
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 245, 245));
        add(mainPanel);

        // ===== Question Panel =====
        JPanel questionPanel = new JPanel(new BorderLayout());
        questionPanel.setBackground(Color.WHITE);
        questionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 3, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        questionArea = new JTextArea();
        questionArea.setEditable(false);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setFont(new Font("Segoe UI", Font.BOLD, 20));
        questionArea.setBackground(Color.WHITE);
        questionArea.setForeground(new Color(44, 62, 80));

        JScrollPane scrollQ = new JScrollPane(questionArea);
        scrollQ.setBorder(null);
        questionPanel.add(scrollQ, BorderLayout.CENTER);
        mainPanel.add(questionPanel, BorderLayout.NORTH);

        // ===== Options Panel =====
        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 15, 15));
        optionsPanel.setBackground(new Color(245, 245, 245));

        options = new JRadioButton[4];
        optionGroup = new ButtonGroup();
     // Soft pastel color for options
        Color softOptionColor = new Color(173, 216, 230); // Light Blue

        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton("Option " + (i + 1));
            options[i].setFont(new Font("Segoe UI", Font.PLAIN, 18));
            options[i].setBackground(softOptionColor);
            options[i].setForeground(Color.BLACK);
            options[i].setFocusPainted(false);
            options[i].setBorder(new EmptyBorder(8, 15, 8, 15));
            int index = i;
            options[i].addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    options[index].setBackground(softOptionColor.darker());
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    options[index].setBackground(softOptionColor);
                }
            });

            optionGroup.add(options[i]);
            optionsPanel.add(options[i]);}
        mainPanel.add(optionsPanel, BorderLayout.CENTER);

        // ===== Bottom Panel =====
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(245, 245, 245));

        JPanel navPanel = new JPanel();
        navPanel.setBackground(new Color(245, 245, 245));

        btnBack = createButton("Back", new Color(52, 152, 219));
        btnNext = createButton("Next", new Color(46, 204, 113));

        navPanel.add(btnBack);
        navPanel.add(Box.createHorizontalStrut(20));
        navPanel.add(btnNext);

        lblTimer = new JLabel("Time Left: " + timeLeft + " s");
        lblTimer.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTimer.setForeground(new Color(231, 76, 60));

        bottomPanel.add(navPanel, BorderLayout.WEST);
        bottomPanel.add(lblTimer, BorderLayout.EAST);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // ===== Load Questions =====
        questionsList = examService.getQuestions1();
        userAnswers = new HashMap<>();

        btnNext.addActionListener(e -> nextQuestion());
        btnBack.addActionListener(e -> previousQuestion());
    }

    private JButton createButton(String text, Color color){
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 25, 7, 25));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt){ btn.setBackground(color.darker()); }
            public void mouseExited(java.awt.event.MouseEvent evt){ btn.setBackground(color); }
        });
        return btn;
    }

    public void startExam() {
        if (questionsList == null || questionsList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No questions available!");
            return;
        }

        loadQuestion();

        try {
            timeLeft = examService.getExamTime(); // seconds
        } catch (RemoteException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Cannot get exam time from server.");
            return;
        }

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> SwingUtilities.invokeLater(() -> {
            if (timeLeft <= 0) {
                lblTimer.setText("Time Left: 00:00");
                scheduler.shutdown();
                saveAnswer();
                showResult();
            } else {
                int minutes = timeLeft / 60;
                int seconds = timeLeft % 60;
                lblTimer.setText(String.format("Time Left: %02d:%02d", minutes, seconds));
                timeLeft--;
            }
        }), 0, 1, TimeUnit.SECONDS);
    }

    private void loadQuestion() {
        optionGroup.clearSelection(); 
        Question q = questionsList.get(currentQuestion);
        questionArea.setText("Q"+(currentQuestion + 1) + ". " + q.getQuestionText());
        options[0].setText(q.getOptionA());
        options[1].setText(q.getOptionB());
        options[2].setText(q.getOptionC());
        options[3].setText(q.getOptionD());

        String saved = userAnswers.get(currentQuestion);
        if (saved != null) {
            switch(saved) {
                case "A": options[0].setSelected(true); break;
                case "B": options[1].setSelected(true); break;
                case "C": options[2].setSelected(true); break;
                case "D": options[3].setSelected(true); break;
            }
        }
    }



    private void saveAnswer() {
        if (options[0].isSelected()) userAnswers.put(currentQuestion, "A");
        else if (options[1].isSelected()) userAnswers.put(currentQuestion, "B");
        else if (options[2].isSelected()) userAnswers.put(currentQuestion, "C");
        else if (options[3].isSelected()) userAnswers.put(currentQuestion, "D");
    }

    private void nextQuestion() {
        saveAnswer();
        if (currentQuestion < questionsList.size() - 1) {
            currentQuestion++;
            loadQuestion();
        } else {
            scheduler.shutdown();
            showResult();
        }
    }

    private void previousQuestion() {
        saveAnswer();
        if (currentQuestion > 0) {
            currentQuestion--;
            loadQuestion();
        }
    }

    private void showResult() {
        int score = 0;
        String[] qTexts = new String[questionsList.size()];
        String[] userAns = new String[questionsList.size()];
        String[] correctAns = new String[questionsList.size()];

        for (int i = 0; i < questionsList.size(); i++) {
            Question q = questionsList.get(i);
            qTexts[i] = q.getQuestionText();
            userAns[i] = userAnswers.get(i);
            correctAns[i] = q.getCorrectAnswer();
            if (userAns[i] != null && userAns[i].equalsIgnoreCase(correctAns[i])) score++;
        }

        ResultForm resultForm = new ResultForm(username, score, questionsList.size(), qTexts, userAns, correctAns);
        resultForm.setVisible(true);
        dispose();
    }

    public static void main(String[] args){
        try{
            ExamInterface service = (ExamInterface) Naming.lookup("rmi://localhost/ExamService");
            new ExamClientGUI("student", service, 120).setVisible(true);
        } catch(Exception e){ e.printStackTrace(); }
    }
}
