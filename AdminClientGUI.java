package client;

import common.User;
import common.Question;
import common.Activity;
import common.ExamInterface;
import server.AdminService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.rmi.RemoteException;

public class AdminClientGUI extends JFrame {
	private AdminService adminService; // RMI stub for admin service

    private AdminService service;
    private String adminUsername = "admin";
    private int timeLeft; // remaining seconds
    private ScheduledExecutorService scheduler; // timer scheduler
    private DefaultTableModel userTableModel;
    private JTable userTable;
    private JTextField txtUserSearch;

  

    private DefaultTableModel questionTableModel;
    private JTable questionTable;
    private ExamInterface examService; // class-level variable

    // Timer components
    private JLabel lblTimer;
    private JButton btnStartTimer, btnStopTimer, btnResetTimer;
    private JTextField txtSetMinutes, txtSetSeconds;
    private ScheduledExecutorService timerScheduler;
    private int timeLeftSeconds = 0;

    public AdminClientGUI() {
        try {
           
        	service = (AdminService) Naming.lookup("rmi://localhost:1098/AdminService");
        	  adminService = service; // same RMI stub

              // ExamInterface (for timer)
              examService = (ExamInterface) Naming.lookup("rmi://localhost:1099/ExamService");
        
        
        } catch(Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,"Cannot connect to AdminService");
            System.exit(0);
        }

        setTitle("Admin Panel");
        setSize(1300,700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // ===== TIMER CONTROL PANEL =====
        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,10));
        timerPanel.setBackground(new Color(245,245,245));

        txtSetMinutes = new JTextField("0",3);
        txtSetSeconds = new JTextField("0",3);
        btnStartTimer = new JButton("Start");
        btnStopTimer = new JButton("Stop");
        btnResetTimer = new JButton("Reset");
        lblTimer = new JLabel("Time Left: 00:00");
        lblTimer.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        styleButton(btnStartTimer,new Color(46,204,113));
        styleButton(btnStopTimer,new Color(231,76,60));
        styleButton(btnResetTimer,new Color(52,152,219));

        timerPanel.add(new JLabel("Set Time:"));
        timerPanel.add(txtSetMinutes);
        timerPanel.add(new JLabel(":"));
        timerPanel.add(txtSetSeconds);
        timerPanel.add(btnStartTimer);
        timerPanel.add(btnStopTimer);
        timerPanel.add(btnResetTimer);
        timerPanel.add(lblTimer);

        add(timerPanel, BorderLayout.NORTH);

        // ===== TABBED PANE =====
        JTabbedPane tabbedPane = new JTabbedPane();

        // USERS TAB
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(250,250,250));

        JPanel topSearch = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
        topSearch.setBackground(new Color(245,245,245));
        topSearch.add(new JLabel("Search User:"));
        txtUserSearch = new JTextField(20);
        JButton btnSearch = new JButton("Search");
        JButton btnRefreshUser = new JButton("Refresh");
        styleButton(btnSearch, new Color(52,152,219));
        styleButton(btnRefreshUser, new Color(46,204,113));
        topSearch.add(txtUserSearch); topSearch.add(btnSearch); topSearch.add(btnRefreshUser);
        userPanel.add(topSearch, BorderLayout.NORTH);

        String[] userCols = {"Username","Password"};
        
        
        userTableModel = new DefaultTableModel(userCols,0);
        userTable = new JTable(userTableModel);
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userTable.setRowHeight(25);
        JScrollPane userScroll = new JScrollPane(userTable);
        userPanel.add(userScroll, BorderLayout.CENTER);

        JTableHeader header = userTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setBackground(new Color(220,220,220));
        header.setForeground(Color.BLACK);

        userScroll.setColumnHeaderView(header); 

     
        JPanel userBtnPanel = new JPanel();
        userBtnPanel.setBackground(new Color(245,245,245));
        JButton btnAddUser = new JButton("Add User");
        JButton btnEditUser = new JButton("Edit User");
        JButton btnDeleteUser = new JButton("Delete User");
        JButton btnResetPass = new JButton("Reset Password");
        JButton btnShowHistory = new JButton("Activity History");
        styleButton(btnAddUser, new Color(100,149,237));
        styleButton(btnEditUser, new Color(100,149,237));
        styleButton(btnDeleteUser, new Color(100,149,237));
        styleButton(btnResetPass, new Color(100,149,237));
        styleButton(btnShowHistory, new Color(100,149,237));
        userBtnPanel.add(btnAddUser); userBtnPanel.add(btnEditUser);
        userBtnPanel.add(btnDeleteUser); userBtnPanel.add(btnResetPass); userBtnPanel.add(btnShowHistory);
        userPanel.add(userBtnPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Users", userPanel);

        // QUESTIONS TAB
        JPanel qPanel = new JPanel(new BorderLayout());
        qPanel.setBackground(new Color(250,250,250));

        String[] qCols = {"No","Question","A","B","C","D","Correct"};
        questionTableModel = new DefaultTableModel(qCols,0);
        questionTable = new JTable(questionTableModel);
        questionTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        questionTable.setRowHeight(25);

      
        
        JTableHeader qHeader = questionTable.getTableHeader();
        qHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));  
        qHeader.setBackground(new Color(220,220,220));         
        qHeader.setForeground(Color.BLACK); 
        
        JScrollPane qScroll = new JScrollPane(questionTable);
        qScroll.setColumnHeaderView(qHeader);
        
        qPanel.add(qScroll, BorderLayout.CENTER);
        
        qScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        qScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        JPanel qBtnPanel = new JPanel();
        qBtnPanel.setBackground(new Color(245,245,245));
        JButton btnAddQ = new JButton("Add Question");
        JButton btnEditQ = new JButton("Edit Question");
        JButton btnDelQ = new JButton("Delete Question");
        JButton btnRefreshQ = new JButton("Refresh");
        styleButton(btnAddQ,new Color(52,152,219));
        styleButton(btnEditQ,new Color(52,152,219));
        styleButton(btnDelQ,new Color(52,152,219));
        styleButton(btnRefreshQ,new Color(52,152,219));
        qBtnPanel.add(btnAddQ); qBtnPanel.add(btnEditQ); qBtnPanel.add(btnDelQ); qBtnPanel.add(btnRefreshQ);
        qPanel.add(qBtnPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Questions", qPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // ===== ACTION LISTENERS =====
        btnStartTimer.addActionListener(e -> {
            try {
                // Admin set time from txtSetMinutes / txtSetSeconds
                int min = Integer.parseInt(txtSetMinutes.getText().trim());
                int sec = Integer.parseInt(txtSetSeconds.getText().trim());
                int totalSeconds = min*60 + sec;

                // Update server
                examService.setExamTime(totalSeconds);

                // Start timer with server value
                startTimer(totalSeconds);
            } catch(Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to start timer: " + ex.getMessage());
            }
        });
        btnStopTimer.addActionListener(e -> {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdownNow();   
                lblTimer.setText("Stopped");
            }
        });
        btnResetTimer.addActionListener(e -> {
            try {
                if (scheduler != null && !scheduler.isShutdown()) {
                    scheduler.shutdownNow();
                }

                int serverTime = examService.getExamTime();

                timeLeft = serverTime;

                int m = timeLeft / 60;
                int s = timeLeft % 60;
                lblTimer.setText(String.format("Time Left: %02d:%02d", m, s));

                txtSetMinutes.setText(String.valueOf(timeLeft / 60));
                txtSetSeconds.setText(String.valueOf(timeLeft % 60));

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to reset timer: " + ex.getMessage());
            }
        });



        btnAddUser.addActionListener(e -> addUser());
        btnEditUser.addActionListener(e -> editUser());
        btnDeleteUser.addActionListener(e -> deleteUser());
        btnResetPass.addActionListener(e -> resetPassword());
        btnSearch.addActionListener(e -> searchUser());
        btnRefreshUser.addActionListener(e -> refreshUserList());
        btnShowHistory.addActionListener(e -> showHistoryTable());

        btnAddQ.addActionListener(e -> addQuestion());
        btnEditQ.addActionListener(e -> editQuestion());
        btnDelQ.addActionListener(e -> deleteQuestion());
        btnRefreshQ.addActionListener(e -> refreshQuestionList());
        JTextField txtMinutes = new JTextField(5); // user input field for minutes
        JButton btnSetTime = new JButton("Set Exam Time");

        btnSetTime.addActionListener(e -> {
            try {
                int seconds = Integer.parseInt(txtMinutes.getText().trim()) * 60;
                examService.setExamTime(seconds); // Server set
                JOptionPane.showMessageDialog(this, "Exam time set to " + seconds + " seconds");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to set exam time: " + ex.getMessage());
            }
        });



        refreshUserList();
        refreshQuestionList();
        setVisible(true);
    }

    // ===== BUTTON STYLE =====
    private void styleButton(JButton btn, Color bg){
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(5,15,5,15));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt){ btn.setBackground(bg.darker()); }
            public void mouseExited(java.awt.event.MouseEvent evt){ btn.setBackground(bg); }
        });
    }

   

    private void stopTimer(){
        if(timerScheduler!=null) timerScheduler.shutdownNow();
    }

    private void resetTimer() {
        try {
            // Stop existing timer
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdownNow();
            }

            // Get latest exam time from server
            int serverTime = examService.getExamTime();
            timeLeft = serverTime;

            // Update label
            int m = timeLeft / 60;
            int s = timeLeft % 60;
            lblTimer.setText(String.format("Time Left: %02d:%02d", m, s));

            // Update input fields to match server time
            txtSetMinutes.setText(String.valueOf(m));
            txtSetSeconds.setText(String.valueOf(s));

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to reset timer: " + e.getMessage());
        }
    }


    public static void main(String[] args){
        SwingUtilities.invokeLater(AdminClientGUI::new);
    }

    // ================== USERS METHODS ==================
    private void refreshUserList() {
        try {
            userTableModel.setRowCount(0);
            for(User u: service.getAllUsers()){
                userTableModel.addRow(new Object[]{u.getUsername(), u.getPassword()});
            }
        } catch(Exception e){ e.printStackTrace(); }
    }
    
    private void addUser() {
        try {
            // Text fields
            JTextField usernameField = new JTextField(15);
            JPasswordField passwordField = new JPasswordField(15);

            // Labels
            JLabel usernameLabel = new JLabel("Username:");
            usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            JLabel passwordLabel = new JLabel("Password :");
            passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

            // Panel with BoxLayout
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(new Color(245, 245, 245));
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            // Username row
            JPanel userRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            userRow.setBackground(new Color(245, 245, 245));
            userRow.add(usernameLabel);
            userRow.add(usernameField);

            // Password row
            JPanel passRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            passRow.setBackground(new Color(245, 245, 245));
            passRow.add(passwordLabel);
            passRow.add(passwordField);

            panel.add(userRow);
            panel.add(passRow);

            // Show dialog
            int res = JOptionPane.showConfirmDialog(this, panel, "Add User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) return;

            // Add user logic
            if(service.addUser(usernameField.getText().trim(), new String(passwordField.getPassword()).trim())){
                refreshUserList();
                service.logActivity(new Activity(adminUsername,"Added user: "+usernameField.getText()));
                JOptionPane.showMessageDialog(this,"User added Successfully!");
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }



    private void editUser(){
        String selected = getSelectedUser(); 
        if(selected==null){ JOptionPane.showMessageDialog(this,"Select a user to edit."); return; }
        try{
            String newUsername = JOptionPane.showInputDialog("New username:",selected);
            if(newUsername==null||newUsername.trim().isEmpty()) return;
            String newPassword = JOptionPane.showInputDialog("New password:");
            if(newPassword==null||newPassword.trim().isEmpty()) return;

            User u = new User(newUsername.trim(), newPassword.trim());
            service.editUser(selected, u); // selected = old username

            service.logActivity(new Activity(adminUsername,"Edited user "+selected+" to "+newUsername));
            refreshUserList();
            JOptionPane.showMessageDialog(this,"User edited successfully.");
        }catch(Exception e){ e.printStackTrace(); JOptionPane.showMessageDialog(this,"Failed to edit user."); }
    }



    private void deleteUser() {
        String selected = getSelectedUser();
        if(selected==null){ JOptionPane.showMessageDialog(this,"Select user"); return; }
        int confirm = JOptionPane.showConfirmDialog(this,"Delete "+selected+"?");
        if(confirm!=JOptionPane.YES_OPTION) return;
        try {
            service.deleteUser(selected);
            refreshUserList();
            service.logActivity(new Activity(adminUsername,"Deleted user: "+selected));
        } catch(Exception e){ e.printStackTrace(); }
    }

    private void resetPassword() {
        String selected = getSelectedUser();
        if(selected==null){ JOptionPane.showMessageDialog(this,"Select user"); return; }
        JTextField pass = new JTextField();
        JPanel panel = new JPanel(new GridLayout(1,2,10,10));
        panel.setBackground(new Color(245,245,245));
        panel.add(new JLabel("New Password:")); panel.add(pass);
        int res = JOptionPane.showConfirmDialog(this,panel,"Reset Password",JOptionPane.OK_CANCEL_OPTION);
        if(res!=JOptionPane.OK_OPTION) return;
        try{
            service.resetPassword(selected,pass.getText());
            refreshUserList();
            service.logActivity(new Activity(adminUsername,"Reset password for user: "+selected));
        } catch(Exception e){ e.printStackTrace(); }
    }

    private void searchUser() {
        try{
            String keyword = txtUserSearch.getText().trim();
            if(keyword.isEmpty()){ refreshUserList(); return; }
            List<User> filtered = service.getAllUsers().stream()
                    .filter(u -> u.getUsername().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
            userTableModel.setRowCount(0);
            for(User u: filtered) userTableModel.addRow(new Object[]{u.getUsername(), u.getPassword()});
        } catch(Exception e){ e.printStackTrace(); }
    }

    private String getSelectedUser(){
        int row = userTable.getSelectedRow();
        if(row>=0) return (String) userTableModel.getValueAt(row,0);
        return null;
    }

    // ================== QUESTIONS METHODS ==================
    private void refreshQuestionList() {
        try{
            questionTableModel.setRowCount(0);
            List<Question> qs = service.getAllQuestions();
            int i=1;
            for(Question q: qs){
                questionTableModel.addRow(new Object[]{i++,q.getQuestionText(),q.getOptionA(),q.getOptionB(),q.getOptionC(),q.getOptionD(),q.getCorrectAnswer()});
            }
        } catch(Exception e){ e.printStackTrace(); }
    }

    private void addQuestion() {
        try {
            JTextArea txtQuestion = new JTextArea(5, 40);
            txtQuestion.setLineWrap(true);
            txtQuestion.setWrapStyleWord(true);
            txtQuestion.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            JScrollPane questionScroll = new JScrollPane(txtQuestion);

            JTextField txtA = new JTextField(30);
            JTextField txtB = new JTextField(30);
            JTextField txtC = new JTextField(30);
            JTextField txtD = new JTextField(30);
            txtA.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txtB.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txtC.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txtD.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            JComboBox<String> correct = new JComboBox<>(new String[]{"A","B","C","D"});
            correct.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(new Color(245,245,245));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8,8,8,8);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Question:"), gbc);
            gbc.gridx = 1; gbc.gridy = 0; panel.add(questionScroll, gbc);
            gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Option A:"), gbc);
            gbc.gridx = 1; gbc.gridy = 1; panel.add(txtA, gbc);
            gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Option B:"), gbc);
            gbc.gridx = 1; gbc.gridy = 2; panel.add(txtB, gbc);
            gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Option C:"), gbc);
            gbc.gridx = 1; gbc.gridy = 3; panel.add(txtC, gbc);
            gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Option D:"), gbc);
            gbc.gridx = 1; gbc.gridy = 4; panel.add(txtD, gbc);
            gbc.gridx = 0; gbc.gridy = 5; panel.add(new JLabel("Correct Answer:"), gbc);
            gbc.gridx = 1; gbc.gridy = 5; panel.add(correct, gbc);

            int res = JOptionPane.showConfirmDialog(this,panel,"Add Question",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
            if(res!=JOptionPane.OK_OPTION) return;

            Question q = new Question(
                    txtQuestion.getText().trim(),
                    txtA.getText().trim(),
                    txtB.getText().trim(),
                    txtC.getText().trim(),
                    txtD.getText().trim(),
                    (String) correct.getSelectedItem()
            );

            service.addQuestion(q);
            refreshQuestionList();
            service.logActivity(new Activity(adminUsername,"Added question: "+txtQuestion.getText()));
            JOptionPane.showMessageDialog(this,"Question added successfully!");

        } catch(Exception e){ e.printStackTrace(); }
    }
 // ===== TIMER METHODS =====
    private void startTimer(int serverTimeInSeconds) {
        stopTimer(); 

        this.timeLeft = serverTimeInSeconds; // Admin panel set time

        try {
            // 1. Admin panel read user input
        	int min = Integer.parseInt(txtSetMinutes.getText().trim());
        	int sec = Integer.parseInt(txtSetSeconds.getText().trim());
        	int clientTime = min * 60 + sec;
        	int serverTime = examService.getExamTime();
        	timeLeft = (serverTime > 0) ? serverTime : clientTime;

        	 scheduler = Executors.newSingleThreadScheduledExecutor();
        	    scheduler.scheduleAtFixedRate(() -> SwingUtilities.invokeLater(() -> {
        	        if (timeLeft <= 0) {
        	            lblTimer.setText("Time Left: 00:00");
        	            scheduler.shutdown();
        	            saveAnswer();
        	            showResult();
        	        } else {
        	            int m = timeLeft / 60;
        	            int s = timeLeft % 60;
        	            lblTimer.setText(String.format("Time Left: %02d:%02d", m, s));
        	            timeLeft--;
        	        }
        	    }), 0, 1, TimeUnit.SECONDS);

        
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to start timer: " + e.getMessage());
        }
    }

   


    private void editQuestion() {
        int row = questionTable.getSelectedRow();
        if(row<0){ JOptionPane.showMessageDialog(this,"Select question"); return; }
        try{
            Question oldQ = service.getAllQuestions().get(row);

            JTextArea txtQuestion = new JTextArea(oldQ.getQuestionText(),5,40);
            txtQuestion.setLineWrap(true); txtQuestion.setWrapStyleWord(true);
            txtQuestion.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            JScrollPane questionScroll = new JScrollPane(txtQuestion);

            JTextField txtA = new JTextField(oldQ.getOptionA());
            JTextField txtB = new JTextField(oldQ.getOptionB());
            JTextField txtC = new JTextField(oldQ.getOptionC());
            JTextField txtD = new JTextField(oldQ.getOptionD());
            txtA.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txtB.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txtC.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txtD.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            JComboBox<String> correct = new JComboBox<>(new String[]{"A","B","C","D"});
            correct.setSelectedItem(oldQ.getCorrectAnswer());
            correct.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(new Color(245,245,245));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8,8,8,8);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Question:"), gbc);
            gbc.gridx = 1; gbc.gridy = 0; panel.add(questionScroll, gbc);
            gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Option A:"), gbc);
            gbc.gridx = 1; gbc.gridy = 1; panel.add(txtA, gbc);
            gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Option B:"), gbc);
            gbc.gridx = 1; gbc.gridy = 2; panel.add(txtB, gbc);
            gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Option C:"), gbc);
            gbc.gridx = 1; gbc.gridy = 3; panel.add(txtC, gbc);
            gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Option D:"), gbc);
            gbc.gridx = 1; gbc.gridy = 4; panel.add(txtD, gbc);
            gbc.gridx = 0; gbc.gridy = 5; panel.add(new JLabel("Correct Answer:"), gbc);
            gbc.gridx = 1; gbc.gridy = 5; panel.add(correct, gbc);

            int res = JOptionPane.showConfirmDialog(this,panel,"Edit Question",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
            if(res!=JOptionPane.OK_OPTION) return;

            Question q = new Question(
                    txtQuestion.getText().trim(),
                    txtA.getText().trim(),
                    txtB.getText().trim(),
                    txtC.getText().trim(),
                    txtD.getText().trim(),
                    (String) correct.getSelectedItem()
            );

            service.editQuestion(row,q);
            refreshQuestionList();
            service.logActivity(new Activity(adminUsername,"Edited question: "+txtQuestion.getText()));

        } catch(Exception e){ e.printStackTrace(); }
    }

    private void deleteQuestion() {
        int row = questionTable.getSelectedRow();
        if(row<0){ JOptionPane.showMessageDialog(this,"Select question"); return; }
        int confirm = JOptionPane.showConfirmDialog(this,"Delete selected question?");
        if(confirm!=JOptionPane.YES_OPTION) return;
        try{
            Question q = service.getAllQuestions().get(row);
            service.deleteQuestion(row);
            refreshQuestionList();
            service.logActivity(new Activity(adminUsername,"Deleted question: "+q.getQuestionText()));
        } catch(Exception e){ e.printStackTrace(); }
    }

    private void saveAnswer() {
    }

    private void showResult() {
    }

    // ================== ACTIVITY HISTORY ==================
    private void showHistoryTable() {
        try {
            List<Activity> acts = service.getAllActivities();
            if (acts.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No activity records");
                return;
            }

            String[] cols = {"Timestamp", "Admin", "Action"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);

            JTable table = new JTable(model) {
                public boolean isCellEditable(int row, int col) { return false; }

                // Alternate row colors
                public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                    Component c = super.prepareRenderer(renderer, row, column);
                    if (!isRowSelected(row)) {
                        if (row % 2 == 0) c.setBackground(new Color(245, 245, 245));
                        else c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    } else {
                        c.setBackground(new Color(100, 149, 237));
                        c.setForeground(Color.WHITE);
                    }
                    return c;
                }
            };

            table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            table.setRowHeight(28);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));

            // Highlight Action column
            table.getColumnModel().getColumn(2).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                               boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    c.setForeground(new Color(220, 20, 60)); // crimson red
                    return c;
                }
            });

            for (Activity a : acts) {
                model.addRow(new Object[]{a.getTimestamp(), a.getAdminName(), a.getAction()});
            }

            // Search / filter box
            JTextField txtSearch = new JTextField(20);
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);
            txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                private void filter() {
                    String text = txtSearch.getText().trim();
                    if (text.isEmpty()) sorter.setRowFilter(null);
                    else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }

                public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            });

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.add(new JLabel("Search:"));
            topPanel.add(txtSearch);

            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(1000, 500));

            JFrame frame = new JFrame("Activity History");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.add(topPanel, BorderLayout.NORTH);
            frame.add(scroll, BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(this);
            frame.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startExam() {
        try {
            // RMI lookup
            ExamInterface examService = (ExamInterface) Naming.lookup("rmi://localhost:1099/ExamService");

            int timeLeft = examService.getExamTime(); 

            // Timer start
            startTimer(timeLeft); // startTimer(seconds) 
        } catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Failed to fetch exam time from server.");
        }
    }

    }