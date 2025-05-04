import Frames.MainFrame;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import javax.swing.table.DefaultTableModel;

public class Main {
    public static void main(String[] args) {

        new MainFrame();
        String url = "jdbc:sqlite:SpoilMoniDB.db";
        final String[] selectedCategory = {null}; // null means "All"

        // Ensure the items table exists
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS items (
                item_ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                item_categ VARCHAR(255),
                item_name VARCHAR(255),
                quantity INTEGER,
                date_added TEXT NOT NULL
            );
        """;

        // Create the table in the database
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Main application frame
        JFrame mainFrame = new JFrame("Spoilage Monitor");
        mainFrame.setSize(1024, 600);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CardLayout cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);

        // Main screen panel
        JPanel mainPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw background images (sticker-like images)
                drawBackgroundImages(g);
            }
        };
        mainPanel.setBackground(Color.decode("#F4F8D3"));

        JLabel title = new JLabel("Spoilage Monitor");
        title.setBounds(320, 40, 500, 60);
        title.setFont(new Font("Arial", Font.BOLD, 50));

        JLabel nearLabel = new JLabel("Spoilage Warning");
        nearLabel.setBounds(650, 150, 500, 40);
        nearLabel.setFont(new Font("Arial", Font.BOLD, 27));

        JTable spoilTable = createSpoilTableFromDB("SpoilMoniDB.db");
        JScrollPane spoilscrollPane = new JScrollPane(spoilTable);
        spoilscrollPane.setBounds(580, 200, 360, 250);

        JButton checkButton = new JButton("View Stocks");
        checkButton.setBounds(150, 200, 300, 50);
        styleButton(checkButton);

        JButton recipeButton = new JButton("View Recipes");
        recipeButton.setBounds(150, 300, 300, 50);
        styleButton(recipeButton); // Always enabled

        JButton exitButton = new JButton("Exit Application");
        exitButton.setBounds(150, 400, 300, 50);
        styleButton(exitButton);

        // Add main panel to card layout
        cardPanel.add(mainPanel, "Home");
        mainFrame.add(cardPanel);

        // Table row click listener
        spoilTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int selectedRow = spoilTable.getSelectedRow();
                if (selectedRow != -1) {
                    String itemName = spoilTable.getValueAt(selectedRow, 0).toString();
                    recipeButton.setText("Check " + itemName.toLowerCase() + " recipes");
                    selectedCategory[0] = itemName;
                }
            }
        });

        // View Stocks button
        checkButton.addActionListener(e -> {
            ViewStocks viewStocksPanel = new ViewStocks();
            cardPanel.add(viewStocksPanel, "ViewStocks");
            cardLayout.show(cardPanel, "ViewStocks");
        });

        // Recipe button
        recipeButton.addActionListener(e -> {
            String category = selectedCategory[0] == null ? "All" : selectedCategory[0];
            RecipeScreen recipeScreen = new RecipeScreen(category);
            cardPanel.add(recipeScreen, "RecipeScreen");
            cardLayout.show(cardPanel, "RecipeScreen");
        });

        // Exit button
        exitButton.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(mainFrame, "Are you sure you want to exit?", "Exit Confirmation", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        // Auto-refresh spoil table every 10 seconds
        Timer timer = new Timer(10000, e -> {
            JTable updatedTable = createSpoilTableFromDB("SpoilMoniDB.db");
            spoilTable.setModel(updatedTable.getModel());
        });
        timer.start();

        // Add components to main panel
        mainPanel.add(title);
        mainPanel.add(nearLabel);
        mainPanel.add(spoilscrollPane);
        mainPanel.add(checkButton);
        mainPanel.add(recipeButton);
        mainPanel.add(exitButton);

        mainFrame.setVisible(true);
    }

    private static void styleButton(JButton button) {
        button.setBackground(Color.decode("#A6D6D6"));
        button.setForeground(Color.decode("#141414"));
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
    }

    private static JTable createSpoilTableFromDB(String dbPath) {
        String[] columnNames = {"Item", "Quantity", "Expires In (Days)"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        HashMap<String, Integer> shelfLifeMap = new HashMap<>();
        shelfLifeMap.put("Milk", 14);
        shelfLifeMap.put("Eggs", 14);
        shelfLifeMap.put("Bread", 3);
        shelfLifeMap.put("Meat", 15);
        shelfLifeMap.put("Fish", 10);
        shelfLifeMap.put("Vegetables", 7);
        shelfLifeMap.put("Fruits", 5);
        shelfLifeMap.put("Grains", 120);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT item_categ, quantity, date_added FROM items")) {

            while (rs.next()) {
                String item = rs.getString("item_categ");
                int quantity = rs.getInt("quantity");
                String dateAddedStr = rs.getString("date_added");

                LocalDate dateAdded = LocalDate.parse(dateAddedStr, formatter);
                int shelfLife = shelfLifeMap.getOrDefault(item, 3);
                LocalDate expiryDate = dateAdded.plusDays(shelfLife);

                long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);

                if (daysLeft >= 0 && daysLeft <= 2) {
                    String expiresIn = (daysLeft == 0) ? "Today" : daysLeft + " days";
                    model.addRow(new Object[]{item, quantity, expiresIn});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new JTable(model);
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
