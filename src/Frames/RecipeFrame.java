package Frames;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.sql.*;

public class RecipeFrame extends JFrame {
    private DefaultTableModel model;
    private String currentCategory;
    private final String[] foodTypes = {"All", "Milk", "Eggs", "Bread", "Fish", "Fruits", "Vegetables", "Dairy", "Meat", "Grains"};
    private JComboBox<String> categoryDropdown;

    JPanel mainPanel;
    JLabel titleLabel = new JLabel("Recipes");

    JTable table;
    JScrollPane scrollPane;

    JButton addRecipeButton;
    JButton backButton;

    public RecipeFrame(JFrame mainFrame, String category) {
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
        currentCategory = category;

        setTitle("Spoilage Monitor");
        setSize(1024, 720);

        initComponents();
        setActionListeners();
        add(mainPanel, BorderLayout.CENTER);

        setLocationRelativeTo(mainFrame);
        setVisible(true);
    }

    private void initComponents(){
        mainPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBackgroundImages(g);
            }
        };
        mainPanel.setBackground(Color.decode("#F4F8D3"));
        mainPanel.setLayout(new MigLayout("fill, insets 0 10 10 10, debug", "[grow 0][grow 1][grow 1]", "[grow 1][grow 1][grow 1]"));

        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBounds(60, 30, 300, 40);
        mainPanel.add(titleLabel, "cell 0 0 1,alignx left");

        // Filter
        categoryDropdown = new JComboBox<>(foodTypes);
        categoryDropdown.setSelectedItem(currentCategory);
        categoryDropdown.setBounds(380, 35, 200, 25);
        mainPanel.add(categoryDropdown, "cell 1 0 3 1, alignx center");

        String[] columnNames = {"Title", "URL"};
        model = new DefaultTableModel(columnNames, 0);

        loadRecipes(currentCategory);

        table = new JTable(model);
        table.setDefaultEditor(Object.class, null);
        scrollPane = new JScrollPane(table);
        scrollPane.setBounds(100, 100, 800, 300);
        mainPanel.add(scrollPane, "cell 0 1 4 1, grow");

        addRecipeButton = new JButton("Add Recipe");
        addRecipeButton.setBounds(600, 450, 200, 50);
        styleButton(addRecipeButton);
        mainPanel.add(addRecipeButton,"cell 3 2 2 1, alignx right, grow");

        backButton = new JButton("Back");
        backButton.setBounds(60, 450, 200, 50);
        styleButton(backButton);
        mainPanel.add(backButton, "cell 0 2 2 1, alignx left, grow");

    }


    private void setActionListeners(){
        categoryDropdown.addActionListener(e -> {
            currentCategory = categoryDropdown.getSelectedItem().toString();
            loadRecipes(currentCategory);
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable sourceTable = (JTable) e.getSource();
                    int column = sourceTable.columnAtPoint(e.getPoint());
                    int row = sourceTable.rowAtPoint(e.getPoint());
                    if (column == 1 && row != -1) {
                        String url = sourceTable.getValueAt(row, column).toString();
                        try {
                            Desktop.getDesktop().browse(new URI(url));
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, "Failed to open link.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        addRecipeButton.addActionListener(e -> openAddRecipeDialog());

        backButton.addActionListener(e -> {
            dispose();
        });
    }

    private void styleButton(JButton button) {
        button.setBackground(Color.decode("#A6D6D6"));
        button.setForeground(Color.decode("#141414"));
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
    }

    private void loadRecipes(String category) {
        model.setRowCount(0);
        String query = "SELECT title, url FROM recipes";
        boolean hasCategoryFilter = !category.equals("All");

        if (hasCategoryFilter) {
            query += " WHERE item_categ = ?";
        }

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:SpoilMoniDB.db");
             PreparedStatement stmt = conn.prepareStatement(query)) {

            if (hasCategoryFilter) {
                stmt.setString(1, category);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("title"), rs.getString("url")});
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAddRecipeDialog() {
        JComboBox<String> categoryComboBox = new JComboBox<>(foodTypes);
        categoryComboBox.setSelectedItem(currentCategory);

        JTextField titleField = new JTextField();
        JTextField urlField = new JTextField();

        Object[] message = {
                "Category:", categoryComboBox,
                "Title:", titleField,
                "URL:", urlField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add New Recipe", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String selectedCategory = categoryComboBox.getSelectedItem().toString();
            String title = titleField.getText().trim();
            String url = urlField.getText().trim();

            if (title.isEmpty() || url.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Both Title and URL are required!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    new URI(url);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid URL format!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:SpoilMoniDB.db");
                     PreparedStatement stmt = conn.prepareStatement("INSERT INTO recipes (title, url, item_categ) VALUES (?, ?, ?)")) {
                    stmt.setString(1, title);
                    stmt.setString(2, url);
                    stmt.setString(3, selectedCategory);
                    stmt.executeUpdate();

                    loadRecipes(currentCategory); // Keep showing current filtered category
                    JOptionPane.showMessageDialog(this, "Recipe added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
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
