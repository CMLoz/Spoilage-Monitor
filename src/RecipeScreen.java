import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.awt.event.*;
import java.net.URI;

public class RecipeScreen extends JPanel {
    private DefaultTableModel model;
    private String currentCategory;
    private final String[] foodTypes = {"All", "Milk", "Eggs", "Bread", "Fish", "Fruits", "Vegetables", "Dairy", "Meat", "Grains"};
    private JComboBox<String> categoryDropdown;

    public RecipeScreen(String category) {
        setLayout(null);
        setBackground(Color.decode("#F4F8D3"));

        currentCategory = category;

        JLabel titleLabel = new JLabel("Recipes");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBounds(60, 30, 300, 40);
        add(titleLabel);

        // Filter
        categoryDropdown = new JComboBox<>(foodTypes);
        categoryDropdown.setSelectedItem(category);
        categoryDropdown.setBounds(380, 35, 200, 25);
        add(categoryDropdown);

        categoryDropdown.addActionListener(e -> {
            currentCategory = categoryDropdown.getSelectedItem().toString();
            loadRecipes(currentCategory);
        });

        String[] columnNames = {"Title", "URL"};
        model = new DefaultTableModel(columnNames, 0);

        loadRecipes(currentCategory);

        JTable table = new JTable(model);
        table.setDefaultEditor(Object.class, null);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(100, 100, 800, 300);
        add(scrollPane);

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

        JButton addRecipeButton = new JButton("Add Recipe");
        addRecipeButton.setBounds(600, 450, 200, 50);
        styleButton(addRecipeButton);
        add(addRecipeButton);

        addRecipeButton.addActionListener(e -> openAddRecipeDialog());

        JButton backButton = new JButton("Back");
        backButton.setBounds(60, 450, 200, 50);
        styleButton(backButton);
        add(backButton);

        backButton.addActionListener(e -> {
            Container parent = this.getParent();
            if (parent instanceof JPanel && parent.getLayout() instanceof CardLayout) {
                CardLayout cl = (CardLayout) parent.getLayout();
                cl.show(parent, "Home");
            }
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

    // Override paintComponent to draw background images
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

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