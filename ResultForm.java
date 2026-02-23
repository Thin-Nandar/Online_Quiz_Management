// ResultForm.java
package client;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;

// iText 5.x imports
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Image;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.draw.LineSeparator;

public class ResultForm extends JFrame {

    private String username;
    private int score;
    private int total;

    public ResultForm(String username, int score, int total, String[] questions,
                      String[] userAnswers, String[] correctAnswers) {

        this.username = username;
        this.score = score;
        this.total = total;

        setTitle("Exam Result");
        setSize(820, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10,10));
        getContentPane().setBackground(new Color(52, 152, 219));

        // Top Panel
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(245,245,245));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JLabel lblUser = new JLabel("Student Name: " + username);
        lblUser.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 24));
        lblUser.setForeground(new Color(44,62,80));
        lblUser.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblScore = new JLabel("Score: " + score + " / " + total);
        lblScore.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 24));
        lblScore.setForeground(new Color(39,174,96));
        lblScore.setAlignmentX(Component.CENTER_ALIGNMENT);


        JLabel lblPassFail = new JLabel(score >= total*0.5 ? "PASS ✔" : "FAIL ✘");
        lblPassFail.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 24));
        lblPassFail.setForeground(score >= total*0.5 ? new Color(39,174,96) : Color.RED);
        lblPassFail.setAlignmentX(Component.CENTER_ALIGNMENT);


        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(lblUser);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(lblScore);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(lblPassFail);

        topPanel.add(Box.createVerticalStrut(10));
        add(topPanel, BorderLayout.NORTH);

        // Center Panel (Questions & Answers)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(135,206,235));
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        for(int i=0;i<questions.length;i++){
            JPanel qPanel = new JPanel(new GridLayout(4,1));
            qPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY),
                    BorderFactory.createEmptyBorder(10,10,10,10)
            ));

            JLabel qLabel = new JLabel("Q"+(i+1)+": "+questions[i]);
            qLabel.setFont(new java.awt.Font("SansSerif",java.awt.Font.PLAIN,16));

            JLabel yourAnsLabel = new JLabel("Your Answer: "+ (userAnswers[i]==null?"No Answer":userAnswers[i]));
            yourAnsLabel.setFont(new java.awt.Font("SansSerif",java.awt.Font.PLAIN,16));

            JLabel correctAnsLabel = new JLabel("Correct Answer: "+correctAnswers[i]);
            correctAnsLabel.setFont(new java.awt.Font("SansSerif",java.awt.Font.PLAIN,16));

            boolean correct = userAnswers[i] != null && userAnswers[i].equalsIgnoreCase(correctAnswers[i]);
            JLabel resultLabel = new JLabel("Result: "+(correct?"✔ Correct":"✘ Wrong"));
            resultLabel.setFont(new java.awt.Font("SansSerif",Font.BOLD,16));
            resultLabel.setForeground(correct?new Color(39,174,96):Color.RED);

            qPanel.add(qLabel);
            qPanel.add(yourAnsLabel);
            qPanel.add(correctAnsLabel);
            qPanel.add(resultLabel);

            centerPanel.add(qPanel);
            centerPanel.add(Box.createVerticalStrut(5));
        }

        add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel (Buttons)
        JPanel bottomPanel = new JPanel();
        JButton btnExit = new JButton("Exit");
        btnExit.addActionListener(e -> System.exit(0));

        JButton btnCertificate = new JButton("Generate Certificate (PDF)");
        btnCertificate.addActionListener(e -> {
            if (score >= total * 0.5) {   // Only generate if PASS
                generateCertificatePDF();
            } else {
                JOptionPane.showMessageDialog(this,
                    "You did not pass the exam. Certificate will not be generated.",
                    "Cannot Generate Certificate",
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        bottomPanel.add(btnExit);
        bottomPanel.add(btnCertificate);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void generateCertificatePDF() {
        try {
            String userHome = System.getProperty("user.home");
            String folderPath = userHome + "/Desktop/Certificates";
            File folder = new File(folderPath);
            if (!folder.exists()) folder.mkdirs();

            String fileName = folderPath + "/" + username.replaceAll("\\s+","_") + "_Certificate.pdf";
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            PdfContentByte canvas = writer.getDirectContentUnder();

            // GOLD BORDER
            Rectangle border = new Rectangle(PageSize.A4);
            border.setBorder(Rectangle.BOX);
            border.setBorderWidth(5);
            border.setBorderColor(new BaseColor(212,175,55)); // gold
            canvas.rectangle(border);

            // Light gradient background (subtle)
            Rectangle bg = new Rectangle(PageSize.A4);
            bg.setBackgroundColor(new BaseColor(255, 253, 208));
            canvas.rectangle(bg);

//           

            // Logo
            try {
                Image logo = Image.getInstance(userHome + "/Desktop/logo.png");
                logo.scaleToFit(100, 100);
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);
                document.add(new Paragraph(" "));
            } catch (Exception e) {
                System.out.println("Logo not found, skipping.");
            }

            // Title
            Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 28, Font.BOLD, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("🎓 Certificate of Achievement", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Separator
            document.add(new Chunk(new LineSeparator(2f, 100f, new BaseColor(212,175,55), Element.ALIGN_CENTER, -2)));
            document.add(new Paragraph(" "));

            // Student Name
            Font nameFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLDITALIC, new BaseColor(255, 0, 0));
            Paragraph name = new Paragraph(username, nameFont);
            name.setAlignment(Element.ALIGN_CENTER);
            document.add(name);
            document.add(new Paragraph(" "));

            // Body
            Font bodyFont = new Font(Font.FontFamily.TIMES_ROMAN, 20, Font.BOLD, new BaseColor(0,51,102));
            Paragraph body = new Paragraph(
                    "For successfully completing the Online Exam.\n" +
//                    "Score: " + score + " : " + total + "\n" +
                    "We congratulate you on your achievement and encourage continuous learning.",
                    bodyFont
            );
            body.setAlignment(Element.ALIGN_CENTER);
            document.add(body);
            document.add(new Paragraph(" "));

            // Signature line
            document.add(new Paragraph("\n\n"));
            Font signFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
            Paragraph signature = new Paragraph("______________________________\nAuthorized Signature", signFont);
            signature.setAlignment(Element.ALIGN_RIGHT);
            document.add(signature);

            // Footer Date
            Font dateFont = new Font(Font.FontFamily.COURIER, 14, Font.ITALIC, BaseColor.BLACK);
            Paragraph date = new Paragraph("Issued on: " + java.time.LocalDate.now(), dateFont);
            date.setAlignment(Element.ALIGN_LEFT);
            document.add(date);

            document.close();

            JOptionPane.showMessageDialog(this,
                    "Certificate saved as: " + fileName,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error generating PDF: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
