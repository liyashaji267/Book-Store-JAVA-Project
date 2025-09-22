import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class frontpage extends JFrame implements ActionListener {
    
    JButton loginBtn;

    public frontpage() {
        // Frame settings
        setTitle("Front Page");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ===== Center Logo =====
        // Make sure BF.png is in the project folder or give absolute path
        ImageIcon logoIcon = new ImageIcon("BF.png");
        JLabel logoLabel = new JLabel(logoIcon, JLabel.CENTER);
        add(logoLabel, BorderLayout.CENTER);

        // ===== Right Top Login Button =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        loginBtn = new JButton("Login");
        loginBtn.addActionListener(this);
        topPanel.add(loginBtn, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginBtn) {
            dispose(); // ✅ Close frontpage
            BookstoreLogin.main(null); // ✅ Open Login Page
        }
    }

    public static void main(String[] args) {
        new frontpage(); // ✅ Start with frontpage
    }
}
