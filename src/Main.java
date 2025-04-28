import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class Main {
    public static void main(String[] args) {

        // Main application frame
        JFrame mainFrame = new JFrame("Spoilage Monitor");
        mainFrame.setSize(1024, 600);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CardLayout cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);

        // Main screen panel
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.decode("#F4F8D3"));

        JLabel title = new JLabel("Spoilage Monitor");
        title.setBounds(320, 40, 500, 60);
        title.setFont(new Font("Arial", Font.BOLD, 50));

        JLabel nearLabel = new JLabel("Spoilage Warning");
        nearLabel.setBounds(650, 150, 500, 40);
        nearLabel.setFont(new Font("Arial", Font.BOLD, 27));

        String[] spoilColumnNames = {"Item", "Quantity", "Expires In (Days)"};
        Object[][] spoilData = {
                {"Milk", 2, "5 days"},
                {"Eggs", 12, "6 days"},
                {"Bread", 1, "2 days"}
        };

        JTable spoilTable = new JTable(spoilData, spoilColumnNames);

        JScrollPane spoilscrollPane = new JScrollPane(spoilTable);
        spoilscrollPane.setBounds(580, 200, 360, 250);

        JButton checkButton = new JButton("View Stocks");
        checkButton.setBounds(150, 200, 300, 50);
        styleButton(checkButton);

        JButton recipeButton = new JButton("View Recipes");
        recipeButton.setBounds(150, 300, 300, 50);
        recipeButton.setEnabled(false);
        styleButton(recipeButton);

        JButton exitButton = new JButton("Exit Application");
        exitButton.setBounds(150, 400, 300, 50);
        styleButton(exitButton);

        ViewStocks viewStocksPanel = new ViewStocks();

        cardPanel.add(mainPanel, "Home");
        cardPanel.add(viewStocksPanel, "ViewStocks");

        exitButton.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(mainFrame, "Are you sure you want to exit?", "Exit Confirmation", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        spoilTable.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e) {
               int selectedRow = spoilTable.getSelectedRow();
               if (selectedRow != -1 ) {
                   String itemName = spoilTable.getValueAt(selectedRow, 0).toString();
                   recipeButton.setText("Check " + itemName.toLowerCase() + " recipes");
                   recipeButton.setEnabled(true);
               }
            }
        });

        checkButton.addActionListener(e -> cardLayout.show(cardPanel, "ViewStocks"));

        mainPanel.setLayout(null);
        mainFrame.add(cardPanel);

        mainPanel.add(title);
        mainPanel.add(nearLabel);
        mainPanel.add(checkButton);
        mainPanel.add(exitButton);
        mainPanel.add(recipeButton);
        mainPanel.add(spoilscrollPane);
        mainFrame.setVisible(true);
    }

    private static void styleButton(JButton button) {
        button.setBackground(Color.decode("#A6D6D6"));
        button.setForeground(Color.decode("#141414"));
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
    }

}