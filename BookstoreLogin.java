import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class BookstoreLogin {

    private static HashMap<String, String> users = new HashMap<>(); // username -> password

    public static void main(String[] args) {
        users.put("admin", "1234"); // default admin user
        SwingUtilities.invokeLater(BookstoreLogin::showLoginScreen);
    }

    private static void showLoginScreen() {
        JFrame loginFrame = new JFrame("Bookstore Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(400, 250);
        loginFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField(15);
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField(15);

        JButton loginButton = new JButton("Login");
        JButton signupButton = new JButton("Sign Up");
        JButton exitButton = new JButton("Exit");

        gbc.gridx = 0; gbc.gridy = 0; panel.add(userLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(userField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(passLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(passField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(loginButton, gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(signupButton, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panel.add(exitButton, gbc);

        loginFrame.add(panel);

        loginButton.addActionListener(_ -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());

            if (users.containsKey(username) && users.get(username).equals(password)) {
                loginFrame.dispose();
                BookstoreApp.main(null);
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        signupButton.addActionListener(_ -> showSignUpDialog(loginFrame));

        exitButton.addActionListener(_ -> System.exit(0));

        loginFrame.setVisible(true);
    }

    private static void showSignUpDialog(JFrame parent) {
        JTextField newUserField = new JTextField();
        JPasswordField newPassField = new JPasswordField();

        JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
        p.add(new JLabel("New Username:")); p.add(newUserField);
        p.add(new JLabel("New Password:")); p.add(newPassField);

        int result = JOptionPane.showConfirmDialog(parent, p, "Sign Up", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String newUser = newUserField.getText().trim();
            String newPass = new String(newPassField.getPassword());

            if (newUser.isEmpty() || newPass.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Username and password cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (users.containsKey(newUser)) {
                JOptionPane.showMessageDialog(parent, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            users.put(newUser, newPass);
            JOptionPane.showMessageDialog(parent, "User registered successfully! You can now login.");
        }
    }
}