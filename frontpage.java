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
        // Replace "logo.png" with your actual image file path
        ImageIcon logoIcon = new ImageIcon("bf.png");
        JLabel logoLabel = new JLabel(logoIcon, JLabel.CENTER);
        add(logoLabel, BorderLayout.CENTER);

        // ===== Right Top Login Button =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false); // Transparent panel

        loginBtn = new JButton("Login");
        loginBtn.addActionListener(this);

        // Put login button on the right side of top panel
        topPanel.add(loginBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginBtn) {
            JOptionPane.showMessageDialog(this, "Opening Login Page...");
            // Open your login JFrame here
        }
    }

    public static void main(String[] args) {
        new frontpage();
    }
}

