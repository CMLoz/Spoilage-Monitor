package Frames;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;

public class StocksFrame extends JFrame {

    private JPanel mainPanel;

    private JTable itemsTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    private final String[] foodTypes = {"All", "Milk", "Eggs", "Bread", "Fish", "Fruits", "Vegetables", "Dairy", "Meat", "Grains"};

    private JButton backButton;
    private JButton removeButton;
    private JButton addButton;
    JComboBox<String> foodTypeComboBox;

    public StocksFrame(JFrame mainFrame){
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mainFrame.setVisible(true);
                dispose();
            }
            @Override
            public void windowClosed(WindowEvent e){
                mainFrame.setVisible(true);
                dispose();
            }
        });

        setSize(1024, 720);

        initComponents(mainFrame);
        setActionListeners();

        add(mainPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents(JFrame mainFrame){
        mainPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw background images (sticker-like images)
                drawBackgroundImages(g);
            }
        };
        mainPanel.setLayout(new MigLayout("fill, insets 10", "[grow 4][grow 3][grow 3]", "[grow 1, fill][grow 1, fill][grow 5, fill][grow 3, fill]"));
        mainPanel.setBackground(Color.decode("#F4F8D3"));

        JLabel stocksLabel = new JLabel("Your Inventory", JLabel.CENTER);
        stocksLabel.setFont(new Font("Arial", Font.BOLD, 50));
        mainPanel.add(stocksLabel, "cell 0 0 3 1, grow, dock north");

        foodTypeComboBox = new JComboBox<>(foodTypes);
        foodTypeComboBox.setSelectedIndex(0);
        foodTypeComboBox.setBounds(470, 150, 190, 25);
        mainPanel.add(foodTypeComboBox, "cell 1 1, growx");

        JLabel productsLabel = new JLabel("Products:", JLabel.RIGHT);
        productsLabel.setFont(new Font("Arial", Font.BOLD, 20));
        productsLabel.setBounds(370, 150, 200, 25);
        mainPanel.add(productsLabel, "cell 0 1, growx");

        addButton = new JButton("Add New Item");
        addButton.setBounds(420, 420, 200, 40);
        styleButton(addButton);
        mainPanel.add(addButton, "cell 1 3, growx");

        removeButton = new JButton("Remove Item");
        removeButton.setBounds(650, 420, 200, 40);
        styleButton(removeButton);
        mainPanel.add(removeButton, "cell 2 3, growx");

        backButton = new JButton("Back");
        backButton.setBounds(60, 450, 200, 50);
        styleButton(backButton);
        mainPanel.add(backButton, "cell 0 3, growx");

        itemsTable = new JTable();
        tableModel = new DefaultTableModel(new String[]{"Item ID", "Category", "Item Name", "Quantity", "Date Added"}, 0);
        itemsTable.setModel(tableModel);
        scrollPane = new JScrollPane(itemsTable);
        scrollPane.setBounds(50, 200, 900, 200);
        mainPanel.add(scrollPane, "cell 0 2 3 1, growx");

        loadItemsFromDatabase("SpoilMoniDB.db", "All");
    }

    private void setActionListeners(){
        backButton.addActionListener(e -> {
            dispose();
        });

        foodTypeComboBox.addActionListener(e -> {
            String selectedCategory = (String) foodTypeComboBox.getSelectedItem();
            loadItemsFromDatabase("SpoilMoniDB.db", selectedCategory);
        });

        addButton.addActionListener(e -> {
            String itemName = JOptionPane.showInputDialog(this, "Enter item name:");
            if (itemName != null && !itemName.trim().isEmpty()) {
                JComboBox<String> categoryComboBox = new JComboBox<>(java.util.Arrays.copyOfRange(foodTypes, 1, foodTypes.length)); // exclude "All"
                int result = JOptionPane.showConfirmDialog(this, categoryComboBox, "Select Category", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String category = (String) categoryComboBox.getSelectedItem();
                    String quantityStr = JOptionPane.showInputDialog(this, "Enter quantity:");
                    try {
                        int quantity = Integer.parseInt(quantityStr);
                        String dateAdded = JOptionPane.showInputDialog(this, "Enter date added (YYYY-MM-DD):");
                        if (dateAdded != null && !dateAdded.trim().isEmpty()) {
                            addItemToDatabase(itemName, category, quantity, dateAdded);
                            JOptionPane.showMessageDialog(this, "Item added successfully!");
                            loadItemsFromDatabase("SpoilMoniDB.db", (String) foodTypeComboBox.getSelectedItem()); // Refresh
                        } else {
                            JOptionPane.showMessageDialog(this, "Invalid date input.");
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid quantity input.");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Item name cannot be empty.");
            }
        });

        removeButton.addActionListener(e -> {
            int selectedRow = itemsTable.getSelectedRow();
            if (selectedRow != -1) {
                int itemId = (int) itemsTable.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to remove the item?", "Remove Item",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    removeItemFromDatabase(itemId);
                    JOptionPane.showMessageDialog(this, "Item removed successfully!");
                    loadItemsFromDatabase("SpoilMoniDB.db", (String) foodTypeComboBox.getSelectedItem()); // Refresh
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select an item to remove.");
            }
        });

    }

    private void addItemToDatabase(String itemName, String category, int quantity, String dateAdded) {
        String url = "jdbc:sqlite:SpoilMoniDB.db";
        String insertSQL = "INSERT INTO items (item_categ, item_name, quantity, date_added) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setString(1, category);
            pstmt.setString(2, itemName);
            pstmt.setInt(3, quantity);
            pstmt.setString(4, dateAdded);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeItemFromDatabase(int itemId) {
        String url = "jdbc:sqlite:SpoilMoniDB.db";
        String deleteSQL = "DELETE FROM items WHERE item_ID = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {

            pstmt.setInt(1, itemId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadItemsFromDatabase(String dbPath, String category) {
        tableModel.setRowCount(0); // Clear table

        String sql = "SELECT item_ID, item_categ, item_name, quantity, date_added FROM items";
        boolean filter = !category.equals("All");

        if (filter) {
            sql += " WHERE item_categ = ?";
        }

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (filter) {
                pstmt.setString(1, category);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int itemId = rs.getInt("item_ID");
                String categ = rs.getString("item_categ");
                String itemName = rs.getString("item_name");
                int quantity = rs.getInt("quantity");
                String dateAdded = rs.getString("date_added");

                tableModel.addRow(new Object[]{itemId, categ, itemName, quantity, dateAdded});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void styleButton(JButton button) {
        button.setBackground(Color.decode("#A6D6D6"));
        button.setForeground(Color.decode("#141414"));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
    }

    private static void drawBackgroundImages(Graphics g) {

        ImageIcon mushroomIcon = new ImageIcon("photos/mushrooms.png");
        ImageIcon milkIcon = new ImageIcon("photos/glass-of-milk.png");
        ImageIcon breadIcon = new ImageIcon("photos/bread.png");
        ImageIcon milkCartonIcon = new ImageIcon("photos/milk-carton.png");
        ImageIcon carrotsIcon = new ImageIcon("photos/carrots.png");
        ImageIcon avocadoIcon = new ImageIcon("photos/avocado.png");
        ImageIcon friedEggIcon = new ImageIcon("photos/fried-egg.png");

        Image mushroomImg = mushroomIcon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
        mushroomIcon = new ImageIcon(mushroomImg);

        Image milkImg = milkIcon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
        milkIcon = new ImageIcon(milkImg);

        Image breadImg = breadIcon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
        breadIcon = new ImageIcon(breadImg);

        Image milkCartonImg = milkCartonIcon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
        milkCartonIcon = new ImageIcon(milkCartonImg);

        Image carrotsImg = carrotsIcon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
        carrotsIcon = new ImageIcon(carrotsImg);

        Image avocadoImg = avocadoIcon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
        avocadoIcon = new ImageIcon(avocadoImg);

        Image friedEggImg = friedEggIcon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
        friedEggIcon = new ImageIcon(friedEggImg);

        g.drawImage(mushroomIcon.getImage(), 70, 50, null);
        g.drawImage(milkIcon.getImage(), 50, 350, null);
        g.drawImage(breadIcon.getImage(), 300, 140, null);
        g.drawImage(milkCartonIcon.getImage(), 400, 280, null);
        g.drawImage(carrotsIcon.getImage(), 600, 400, null);
        g.drawImage(avocadoIcon.getImage(), 780, 10, null);
        g.drawImage(friedEggIcon.getImage(), 900, 300, null);
    }

}
