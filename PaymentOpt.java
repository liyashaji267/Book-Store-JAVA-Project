import javax.swing.*;
import java.awt.*;

public class PaymentOpt extends JFrame {
    private double totalAmount;
    private Runnable onPaymentSuccess;

    public PaymentOpt(double totalAmount, Runnable onPaymentSuccess) {
        this.totalAmount = totalAmount;
        this.onPaymentSuccess = onPaymentSuccess;

        setTitle("Payment Options");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel amountLabel = new JLabel("Total Amount: ₹" + totalAmount, SwingConstants.CENTER);
        amountLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(amountLabel, BorderLayout.NORTH);

        JPanel options = new JPanel(new GridLayout(3, 1, 10, 10));
        JButton upiBtn = new JButton("Pay via UPI");
        JButton netBtn = new JButton("Pay via Net Banking");
        JButton cashBtn = new JButton("Pay via Cash");

        options.add(upiBtn);
        options.add(netBtn);
        options.add(cashBtn);
        panel.add(options, BorderLayout.CENTER);

        // ✅ Create bottom panel for buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton printBtn = new JButton("Print Bill");
        printBtn.setEnabled(false); // Enabled only after successful payment

        JButton closeBtn = new JButton("Close"); // ✅ new button to close without printing

        bottomPanel.add(printBtn);
        bottomPanel.add(closeBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Payment buttons still use printBtn for enabling after success
        upiBtn.addActionListener(e -> handlePayment("UPI", printBtn));
        netBtn.addActionListener(e -> handlePayment("Net Banking", printBtn));
        cashBtn.addActionListener(e -> handlePayment("Cash", printBtn));

        // ✅ Print & close
        printBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "🖨 Printing Bill...");
            this.dispose(); // ✅ close after printing
        });

        // ✅ Close button: just close window without printing
        closeBtn.addActionListener(e -> this.dispose());

        add(panel);
    }
    
    private void handlePayment(String method, JButton printBtn) {
        JOptionPane.showMessageDialog(this,
                method + " Payment Successful!\nAmount Paid: ₹" + totalAmount,
                "Payment Success",
                JOptionPane.INFORMATION_MESSAGE);

        printBtn.setEnabled(true);

        if (onPaymentSuccess != null) {
            onPaymentSuccess.run();
        }
    }
}
