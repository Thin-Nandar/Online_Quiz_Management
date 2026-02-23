package client;

import java.rmi.Naming;
import javax.swing.*;
import common.ExamInterface;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class WelcomeForm extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton studentLoginBtn, adminLoginBtn;

    public WelcomeForm() {
        setTitle("🎓 Online Exam System");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 250, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12,12,12,12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("🎓 Welcome to Online Quiz System", JLabel.CENTER);
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        title.setForeground(new Color(30, 144, 255));
        gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2;
        panel.add(title, gbc);

        // Subtitle
        JLabel subtitle = new JLabel(" Please login to continue", JLabel.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.DARK_GRAY);
        gbc.gridy=1;
        panel.add(subtitle, gbc);

        // Username
        JLabel userLabel = new JLabel("👤 Username:");
        gbc.gridy=2; gbc.gridwidth=1; gbc.gridx=0;
        panel.add(userLabel, gbc);

        usernameField = new JTextField();
        gbc.gridx=1; panel.add(usernameField, gbc);

        // Password
        JLabel passLabel = new JLabel("🔑 Password:");
        gbc.gridx=0; gbc.gridy=3;
        panel.add(passLabel, gbc);

        passwordField = new JPasswordField();
        gbc.gridx=1; panel.add(passwordField, gbc);

        // Buttons
        studentLoginBtn = new JButton("✅ Student Login");
        studentLoginBtn.setBackground(new Color(0, 102, 204));
        studentLoginBtn.setForeground(Color.WHITE);
        studentLoginBtn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        gbc.gridx=0; gbc.gridy=4;
        panel.add(studentLoginBtn, gbc);

        adminLoginBtn = new JButton("🔑 Admin Login");
        adminLoginBtn.setBackground(new Color(46,204,113)); 
        adminLoginBtn.setForeground(Color.WHITE);
        adminLoginBtn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        gbc.gridx=1; gbc.gridy=4;
        panel.add(adminLoginBtn, gbc);


        // Action listeners
        studentLoginBtn.addActionListener(e -> studentLoginAction());
        adminLoginBtn.addActionListener(e -> adminLoginAction());

        add(panel);
    }

    // Method to style button with hover effect
    private void styleButton(JButton button, Color defaultColor, Color hoverColor) {
        button.setBackground(defaultColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(defaultColor.darker(), 2, true));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(defaultColor);
            }
        });
    }
    private void studentLoginAction() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if(username.isEmpty() || password.isEmpty()){
            JOptionPane.showMessageDialog(this,"⚠️ Enter username and password!");
            return;
        }

        try {
            ExamInterface service = (ExamInterface) Naming.lookup("rmi://127.0.0.1/ExamService");
            if(service.login(username, password)){
                JOptionPane.showMessageDialog(this,"✅ Student login successfully!");
                ExamClientGUI examClient = new ExamClientGUI(username, service, 0);
                examClient.setVisible(true);
                examClient.startExam();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,"❌ Invalid student credentials!");
            }
        } catch(Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,"🚫 Server connection failed.");
        }
    }

    private void adminLoginAction() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if(username.isEmpty() || password.isEmpty()){
            JOptionPane.showMessageDialog(this,"⚠️ Enter username and password!");
            return;
        }

        if(username.equalsIgnoreCase("admin") && password.equals("admin123")){
            JOptionPane.showMessageDialog(this,"✅ Admin login successfully!");
            AdminClientGUI adminGUI = new AdminClientGUI();
            adminGUI.setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,"❌ Invalid admin credentials!");
        }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new WelcomeForm().setVisible(true));
    }
}
