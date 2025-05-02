import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class RecipeScreen extends JPanel {
    private DefaultTableModel model;
    private String currentCategory;
    private final String[] foodTypes = {"All", "Milk", "Eggs", "Bread", "Fish", "Fruits", "Vegetables", "Dairy", "Meat", "Grains"};

    public RecipeScreen(String category) {
        setLayout(null);
        setBackground(Color.decode("#F4F8D3"));

        currentCategory = category;  // Store the current category

        JLabel titleLabel = new JLabel("Recipes for: " + category);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBounds(60, 30, 600, 40);
        add(titleLabel);

        String[] columnNames = {"Title", "URL"};
        model = new DefaultTableModel(columnNames, 0);

        loadRecipes(category);  // Load initial recipes

        JTable table = new JTable(model);

        table.setDefaultEditor(Object.class, null);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(60, 100, 800, 300);
        add(scrollPane);

        // Open URL on double-click
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {  // Double-click
                    JTable sourceTable = (JTable) e.getSource();
                    int column = sourceTable.columnAtPoint(e.getPoint()); // Get the clicked column
                    int row = sourceTable.rowAtPoint(e.getPoint()); // Get the clicked row
                    if (column == 1 && row != -1) {  // Check if the URL column (index 1) is clicked
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

        addRecipeButton.addActionListener(e -> {
            openAddRecipeDialog();
        });

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

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:SpoilMoniDB.db");
             PreparedStatement stmt = conn.prepareStatement("SELECT title, url FROM recipes WHERE item_categ = ?")) {
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String title = rs.getString("title");
                String url = rs.getString("url");
                model.addRow(new Object[]{title, url});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAddRecipeDialog() {
        // Create ComboBox for categories from ViewStocks
        JComboBox<String> categoryComboBox = new JComboBox<>(foodTypes);
        categoryComboBox.setSelectedItem(currentCategory);  // Set the current category as default

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
                // Insert into database
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:SpoilMoniDB.db");
                     PreparedStatement stmt = conn.prepareStatement("INSERT INTO recipes (title, url, item_categ) VALUES (?, ?, ?)")) {
                    stmt.setString(1, title);
                    stmt.setString(2, url);
                    stmt.setString(3, selectedCategory);
                    stmt.executeUpdate();

                    // Reload recipes to update the table
                    loadRecipes(selectedCategory);

                    JOptionPane.showMessageDialog(this, "Recipe added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
