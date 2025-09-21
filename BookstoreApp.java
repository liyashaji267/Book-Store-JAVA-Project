import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Swing-based Bookstore Management System (Frontend Only)
 * Features:
 *  - Add / Update / Delete book inventory
 *  - Search by Title/Author/Edition/Publisher/ISBN
 *  - Show price (store & substore)
 *  - Show rack/location for current store books
 *  - Substore lookup & reserve simulation
 *  - Billing/cart system with membership discount & manual discount
 *  - Remove sold books from stock after checkout
 *  - Bills history (with optional customer name if consent given)
*/

public class BookstoreApp extends JFrame {

    private DefaultTableModel inventoryModel;
    private DefaultTableModel substoreModel;
    private DefaultTableModel cartModel;
    private DefaultTableModel billsModel;

    private java.util.List<Book> inventory = new ArrayList<>();
    private java.util.List<Book> substore = new ArrayList<>();
    private java.util.List<Bill> bills = new ArrayList<>();

    private JTable inventoryTable, substoreTable, cartTable, billsTable;
    private JTextField searchField, scanField;
    private JComboBox<String> searchByCombo;

    private int nextBookId = 1001;
    private int nextBillId = 1;

    public BookstoreApp() {
        super("Bookstore Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        initSampleData();
        initUI();
    }

    private void initSampleData() {
        try (Connection con = DBUtil.getConnection();
         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery("SELECT * FROM books")) {

            while (rs.next()) {
                inventory.add(new Book(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("edition"),
                    rs.getString("publisher"),
                    rs.getString("isbn"),
                    rs.getDouble("price"),
                    rs.getString("location"),
                    rs.getInt("stock")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Could not load books from database.");
        }
    }


    private void initUI() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Inventory", createInventoryPanel());
        tabs.addTab("Substore Lookup", createSubstorePanel());
        tabs.addTab("Billing", createBillingPanel());
        tabs.addTab("Bills History", createBillsPanel());
        getContentPane().add(tabs);
    }

    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(30);
        searchByCombo = new JComboBox<>(new String[]{"Title", "Author", "Edition", "Publisher", "ISBN"});
        JButton searchBtn = new JButton("Search");
        JButton resetBtn = new JButton("Reset");

        top.add(new JLabel("Search:")); top.add(searchField); top.add(searchByCombo);
        top.add(searchBtn); top.add(resetBtn);
        top.add(Box.createHorizontalStrut(20));
        top.add(new JLabel("Scan ISBN:"));
        scanField = new JTextField(15);
        JButton scanBtn = new JButton("Scan");
        top.add(scanField); top.add(scanBtn);
        panel.add(top, BorderLayout.NORTH);

        String[] invCols = {"ID", "Title", "Author", "Edition", "Publisher", "ISBN", "Price", "Location", "Stock"};
        inventoryModel = new DefaultTableModel(invCols, 0) { public boolean isCellEditable(int r,int c){return false;} };
        inventoryTable = new JTable(inventoryModel);
        refreshInventoryTable();
        panel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        bottom.add(addBtn); bottom.add(editBtn); bottom.add(deleteBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> applyInventorySearch());
        resetBtn.addActionListener(e -> { searchField.setText(""); refreshInventoryTable(); });
        scanBtn.addActionListener(e -> simulateScan());
        addBtn.addActionListener(e -> showAddEditDialog(null));
        editBtn.addActionListener(e -> {
            int row = inventoryTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select a book to edit."); return; }
            int id = (int) inventoryModel.getValueAt(row, 0);
            showAddEditDialog(findBookInInventoryById(id));
        });
        deleteBtn.addActionListener(e -> {
            int row = inventoryTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select a book to delete."); return; }
            int id = (int) inventoryModel.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "Delete this book?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                removeBookFromInventoryById(id); refreshInventoryTable();
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyInventorySearch(); }
            public void removeUpdate(DocumentEvent e) { applyInventorySearch(); }
            public void changedUpdate(DocumentEvent e) { applyInventorySearch(); }
        });

        return panel;
    }

    private JPanel createSubstorePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
            String[] cols = {"ID","Title","Author","Edition","Publisher","ISBN","Price","Stock"};
            substoreModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
            substoreTable = new JTable(substoreModel);
            refreshSubstoreTable();

            panel.add(new JScrollPane(substoreTable), BorderLayout.CENTER);

            // ✅ Mini-cart for ordered books
            String[] cartCols = {"Title","Qty","Price","Total"};
            DefaultTableModel orderCartModel = new DefaultTableModel(cartCols, 0) { public boolean isCellEditable(int r,int c){return false;} };
            JTable orderCartTable = new JTable(orderCartModel);

            JPanel bottom = new JPanel(new BorderLayout());
            bottom.add(new JScrollPane(orderCartTable), BorderLayout.CENTER);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton addToOrderBtn = new JButton("Add to Order");
            JButton checkoutBtn = new JButton("Checkout Order");
            btnPanel.add(addToOrderBtn);
            btnPanel.add(checkoutBtn);

            bottom.add(btnPanel, BorderLayout.SOUTH);
            panel.add(bottom, BorderLayout.SOUTH);

            // ✅ Add selected book to order cart
            addToOrderBtn.addActionListener(e -> {
                int row = substoreTable.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(this, "Select a book to add to order.");
                    return;
                }
                String title = (String) substoreModel.getValueAt(row, 1);
                double price = (double) substoreModel.getValueAt(row, 6);
        
                // Check if already in cart -> just increment qty
                boolean found = false;
                for (int i = 0; i < orderCartModel.getRowCount(); i++) {
                    if (orderCartModel.getValueAt(i, 0).equals(title)) {
                        int qty = (int) orderCartModel.getValueAt(i, 1);
                        qty++;
                        orderCartModel.setValueAt(qty, i, 1);
                        orderCartModel.setValueAt(qty * price, i, 3);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    orderCartModel.addRow(new Object[]{title, 1, price, price});
                }
            });

            // ✅ Checkout: Calculate total & open PaymentOpt
            checkoutBtn.addActionListener(e -> {
                if (orderCartModel.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(this, "No books in order cart.");
                    return;
                }

                double total = 0;
                for (int i = 0; i < orderCartModel.getRowCount(); i++) {
                    total += (double) orderCartModel.getValueAt(i, 3);
                }

                final double finalTotal = total;
                PaymentOpt payWindow = new PaymentOpt(finalTotal, () -> {
                    JOptionPane.showMessageDialog(this, "Order placed successfully!");
                    orderCartModel.setRowCount(0); // ✅ Clear cart after success
                });

                payWindow.setVisible(true);
            });

            return panel;
        }

            private JPanel createBillingPanel() {
                JPanel panel = new JPanel(new BorderLayout(8,8));
                JPanel left = new JPanel(new BorderLayout());
                left.setPreferredSize(new Dimension(550, 500));
                JTable pickTable = new JTable(inventoryModel);
                pickTable.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            int r = pickTable.getSelectedRow();
                            if (r != -1) addToCart(findBookInInventoryById((int) inventoryModel.getValueAt(r, 0)));
                        }
                    }
                });
                left.add(new JScrollPane(pickTable), BorderLayout.CENTER);
                panel.add(left, BorderLayout.WEST);

                JPanel right = new JPanel(new BorderLayout());
                String[] cartCols = {"ID", "Title", "Qty", "Unit Price", "Total"};
                cartModel = new DefaultTableModel(cartCols,0){ public boolean isCellEditable(int r,int c){return false;} };
                cartTable = new JTable(cartModel);
                right.add(new JScrollPane(cartTable), BorderLayout.CENTER);
                panel.add(right, BorderLayout.EAST);

                JButton checkoutBtn = new JButton("Checkout");
                checkoutBtn.addActionListener(e -> checkoutCart());
                right.add(checkoutBtn, BorderLayout.SOUTH);

                return panel;
            }

    private JPanel createBillsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"Bill ID","Date","Amount","Customer"};
        billsModel = new DefaultTableModel(cols,0) { public boolean isCellEditable(int r,int c){return false;} };
        billsTable = new JTable(billsModel);
        refreshBillsTable();
        panel.add(new JScrollPane(billsTable), BorderLayout.CENTER);
        return panel;
    }

    private void refreshInventoryTable() {
        inventoryModel.setRowCount(0);
        for (Book b : inventory)
            inventoryModel.addRow(new Object[]{b.id,b.title,b.author,b.edition,b.publisher,b.isbn,b.price,b.location,b.stock});
    }

    private void refreshSubstoreTable() {
        substoreModel.setRowCount(0);
        for (Book b : substore)
            substoreModel.addRow(new Object[]{b.id,b.title,b.author,b.edition,b.publisher,b.isbn,b.price,b.stock});
    }

    private void refreshBillsTable() {
        billsModel.setRowCount(0);
        for (Bill b : bills) {
            billsModel.addRow(new Object[]{
                    b.id,
                    new SimpleDateFormat("yyyy-MM-dd HH:mm").format(b.date),
                    b.amount,
                    (b.customer.isEmpty() ? "N/A" : b.customer)
            });
        }
    }

    private void applyInventorySearch() {
        String q = searchField.getText().trim().toLowerCase();
        String by = (String) searchByCombo.getSelectedItem();
        inventoryModel.setRowCount(0);
        for (Book b : inventory) {
            boolean match = switch (by) {
                case "Title" -> b.title.toLowerCase().contains(q);
                case "Author" -> b.author.toLowerCase().contains(q);
                case "Edition" -> b.edition.toLowerCase().contains(q);
                case "Publisher" -> b.publisher.toLowerCase().contains(q);
                case "ISBN" -> b.isbn.toLowerCase().contains(q);
                default -> false;
            };
            if (q.isEmpty() || match)
                inventoryModel.addRow(new Object[]{b.id,b.title,b.author,b.edition,b.publisher,b.isbn,b.price,b.location,b.stock});
        }
    }

    private void simulateScan() {
        String isbn = scanField.getText().trim();
        if (isbn.isEmpty()) return;
        Book b = findBookInInventoryByISBN(isbn);
        JOptionPane.showMessageDialog(this, b != null ?
                "Found in store: " + b.title + " — Location: " + b.location + " — Price: " + b.price + " — Stock: " + b.stock :
                "Book not found in current store.");
    }

    private void showAddEditDialog(Book existing) {
        JTextField titleF = new JTextField(existing==null?"":existing.title);
        JTextField authorF = new JTextField(existing==null?"":existing.author);
        JTextField editionF = new JTextField(existing==null?"":existing.edition);
        JTextField publisherF = new JTextField(existing==null?"":existing.publisher);
        JTextField isbnF = new JTextField(existing==null?"":existing.isbn);
        JTextField priceF = new JTextField(existing==null?"":String.valueOf(existing.price));
        JTextField locF = new JTextField(existing==null?"":existing.location);
        JTextField stockF = new JTextField(existing==null?"":String.valueOf(existing.stock));

        JPanel p = new JPanel(new GridLayout(0,2,4,4));
        p.add(new JLabel("Title:")); p.add(titleF);
        p.add(new JLabel("Author:")); p.add(authorF);
        p.add(new JLabel("Edition:")); p.add(editionF);
        p.add(new JLabel("Publisher:")); p.add(publisherF);
        p.add(new JLabel("ISBN:")); p.add(isbnF);
        p.add(new JLabel("Price:")); p.add(priceF);
        p.add(new JLabel("Location:")); p.add(locF);
        p.add(new JLabel("Stock:")); p.add(stockF);

        if (JOptionPane.showConfirmDialog(this,p,existing==null?"Add Book":"Edit Book",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION) return;
        try {
            if (existing==null)
                inventory.add(new Book(nextBookId++,titleF.getText(),authorF.getText(),editionF.getText(),
                        publisherF.getText(),isbnF.getText(),Double.parseDouble(priceF.getText()),
                        locF.getText(),Integer.parseInt(stockF.getText())));
            else {
                existing.title=titleF.getText(); existing.author=authorF.getText();
                existing.edition=editionF.getText(); existing.publisher=publisherF.getText();
                existing.isbn=isbnF.getText(); existing.price=Double.parseDouble(priceF.getText());
                existing.location=locF.getText(); existing.stock=Integer.parseInt(stockF.getText());
            }
            refreshInventoryTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,"Invalid input: "+ex.getMessage());
        }
    }

    private void addToCart(Book b) {
        if (b == null) return;
        if (b.stock <= 0) {
            JOptionPane.showMessageDialog(this,
                    "This book is out of stock and cannot be purchased.",
                    "Out of Stock",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            int id = (int) cartModel.getValueAt(i, 0);
            if (id == b.id) {
                int qty = (int) cartModel.getValueAt(i, 2);
                if (qty + 1 > b.stock) {
                    JOptionPane.showMessageDialog(this,
                            "Only " + b.stock + " copies available!",
                            "Stock Limit Reached",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                qty++;
                cartModel.setValueAt(qty, i, 2);
                cartModel.setValueAt(qty * b.price, i, 4);
                return;
            }
        }
        cartModel.addRow(new Object[]{b.id, b.title, 1, b.price, b.price});
    }

    private void checkoutCart() {
    if (cartModel.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, "Cart is empty!");
        return;
    }

    double totalCalc = 0;
    for (int i = 0; i < cartModel.getRowCount(); i++) {
        totalCalc += (double) cartModel.getValueAt(i, 4);
    }
    final double total = totalCalc;

    String customerName = JOptionPane.showInputDialog(this, "Enter customer name (or leave blank):");

    // ✅ Show Payment Window
    PaymentOpt payWindow = new PaymentOpt(total, () -> {
        // Deduct stock & create bill
        Bill bill = new Bill(nextBillId++, new Date(), total, (customerName == null ? "" : customerName));
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            int bookId = (int) cartModel.getValueAt(i, 0);
            String title = (String) cartModel.getValueAt(i, 1);
            int qty = (int) cartModel.getValueAt(i, 2);
            double unitPrice = (double) cartModel.getValueAt(i, 3);

            Book b = findBookInInventoryById(bookId);
            if (b != null) b.stock = Math.max(0, b.stock - qty);

            bill.items.add(new BillItem(bookId, title, qty, unitPrice));
        }
        bills.add(bill);
        refreshBillsTable();
        cartModel.setRowCount(0);
        refreshInventoryTable();
    });

    payWindow.setVisible(true);
}


    private Book findBookInInventoryById(int id) {
        for (Book b : inventory) if (b.id == id) return b;
        return null;
    }
    private Book findBookInInventoryByISBN(String isbn) {
        for (Book b : inventory) if (b.isbn.equalsIgnoreCase(isbn)) return b;
        return null;
    }
    private void removeBookFromInventoryById(int id) {
        inventory.removeIf(b -> b.id == id);
    }

    // === INNER CLASSES ===
    static class Book {
        int id; String title, author, edition, publisher, isbn, location;
        int stock;
        double price;
        Book(int id,String t,String a,String e,String p,String i,double pr,String l,int s){
            this.id=id; this.title=t; this.author=a; this.edition=e;
            this.publisher=p; this.isbn=i; this.price=pr; this.location=l;
            this.stock=s;
        }
    }
    static class Bill {
        int id; Date date; double amount; String customer;
        java.util.List<BillItem> items=new ArrayList<>();
        Bill(int id,Date d,double a,String c){
            this.id=id; this.date=d; this.amount=a; this.customer=c;
        }
    }
    static class BillItem {
        int bookId; String title; int qty; double unitPrice;
        BillItem(int id,String t,int q,double u){
            bookId=id; title=t; qty=q; unitPrice=u;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BookstoreApp().setVisible(true));
    }
}
