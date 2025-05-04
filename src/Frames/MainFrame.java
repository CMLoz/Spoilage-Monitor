package Frames;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

public class MainFrame extends JFrame {
    JPanel mainPanel;
    JPanel sidePanel;

    JLabel title = new JLabel("Spoilage Monitor", SwingConstants.CENTER);
    JLabel nearLabel = new JLabel("Spoilage Warning", SwingConstants.CENTER);

    JTable spoilTable = null;

    JButton checkButton = new JButton("View Stocks");
    JButton recipeButton = new JButton("View Recipes");
    JButton exitButton = new JButton("Exit Application");

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


    public MainFrame(){
        createConnection();

        setSize(1024, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Color.decode("#F4F8D3"));
        setTitle("Spoilage Monitor");

        // Auto-refresh spoil table every 10 seconds
        Timer timer = new Timer(10000, e -> {
            JTable updatedTable = createSpoilTableFromDB("SpoilMoniDB.db");
            spoilTable.setModel(updatedTable.getModel());
        });
        timer.start();

        setLayout(new MigLayout("fill, insets 0","[grow 3][grow 7]","[fill]"));

        initComp();
        addActionListeners();
        setLocationRelativeTo(null);
        setVisible(true);


    }

    private void initComp(){
        mainPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw background images (sticker-like images)
                drawBackgroundImages(g);
            }
        };
        sidePanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw background images (sticker-like images)
                drawBackgroundImages(g);
            }
        };
        sidePanel.setLayout(new MigLayout("insets 0 30 0 30, fill", "","push[fill]push[fill]push[fill]push"));


        mainPanel.setBackground(Color.decode("#F4F8D3"));
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        mainPanel.setLayout(new MigLayout("fill, insets 0", "[fill, grow][center]", "[grow]"));

        sidePanel.setBackground(Color.decode("#F4F8D3"));
        sidePanel.setBorder(BorderFactory.createLineBorder(Color.black));

        title.setFont(new Font("Arial", Font.BOLD, 50));
        title.setBorder(BorderFactory.createLineBorder(Color.black));
        title.setOpaque(true);
        title.setBackground(Color.decode("#F4F8D3"));
        add(title, "cell 0 0 5 2, grow, dock north");


        add(sidePanel, "cell 0 2, grow");

        sidePanel.add(checkButton, "height 15%, growy, growx, wrap");
        styleButton(checkButton, sidePanel);
        sidePanel.add(recipeButton, "height 15%, growy, growx, wrap");
        styleButton(recipeButton, sidePanel);
        sidePanel.add(exitButton, "height 15%, growy, growx, wrap");
        styleButton(exitButton, sidePanel);

        sidePanel.add(Box.createVerticalGlue());

        nearLabel.setBounds(650, 150, 500, 40);
        nearLabel.setFont(new Font("Arial", Font.BOLD, 27));
        spoilTable = createSpoilTableFromDB("SpoilMoniDB.db");
        mainPanel.add(nearLabel, "cell 0 0, growx");

        JScrollPane spoilscrollPane = new JScrollPane(spoilTable);
        spoilscrollPane.setBounds(580, 200, 360, 250);
        mainPanel.add(spoilscrollPane, "cell 0 1, align center");


        add(mainPanel, "cell 1 2, grow, push");

    }

    private void addActionListeners(){
        // View Stocks button
        checkButton.addActionListener(e -> {
            StocksFrame viewStocksPanel = new StocksFrame(this);
            setVisible(false);
        });

        // Recipe button
//        recipeButton.addActionListener(e -> {
//            String category = selectedCategory[0] == null ? "All" : selectedCategory[0];
//            RecipeScreen recipeScreen = new RecipeScreen(category);
//            cardPanel.add(recipeScreen, "RecipeScreen");
//            cardLayout.show(cardPanel, "RecipeScreen");
//        });

        // Exit button
        exitButton.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Exit Confirmation", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
    }

    private void createConnection(){
        // Create the table in the database
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    private static void styleButton(JButton button, JPanel parentPanel) {
        button.setBackground(Color.decode("#A6D6D6"));
        button.setForeground(Color.decode("#141414"));
        button.setFont(new Font("Arial", Font.BOLD, 20));
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
