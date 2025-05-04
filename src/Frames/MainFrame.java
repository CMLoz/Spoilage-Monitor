package Frames;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.HashMap;

public class MainFrame extends JFrame {
    BorderLayout layout = new BorderLayout(10, 10);
    GridBagLayout gbLayout = new GridBagLayout();

    JPanel mainPanel = new JPanel();
    JPanel sidePanel = new JPanel();

    JLabel title = new JLabel("Spoilage Monitor", SwingConstants.CENTER);
    JLabel nearLabel = new JLabel("Spoilage Warning", SwingConstants.CENTER);

    JTable spoilTable = null;

    JButton checkButton = new JButton("View Stocks");
    JButton recipeButton = new JButton("View Recipes");
    JButton exitButton = new JButton("Exit Application");

    public MainFrame(){
        setSize(1024, 720);
        setLocationRelativeTo(null);
        setLayout(gbLayout);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Color.decode("#F4F8D3"));

        initComp();

        setVisible(true);
    }

    private void initComp(){
        mainPanel.setBackground(Color.decode("#F4F8D3"));
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.black));

        sidePanel.setBackground(Color.decode("#F4F8D3"));
        sidePanel.setBorder(BorderFactory.createLineBorder(Color.black));

        title.setFont(new Font("Arial", Font.BOLD, 50));
        add(title, BorderLayout.NORTH);

        nearLabel.setBounds(650, 150, 500, 40);
        nearLabel.setFont(new Font("Arial", Font.BOLD, 27));
        mainPanel.add(nearLabel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        sidePanel.add(Box.createVerticalGlue());
        styleButton(checkButton);
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.add(checkButton);

        sidePanel.add(Box.createVerticalGlue());
        styleButton(recipeButton);
        sidePanel.add(recipeButton);

        sidePanel.add(Box.createVerticalGlue());
        styleButton(exitButton);
        sidePanel.add(exitButton);
        add(sidePanel, BorderLayout.WEST);
        sidePanel.add(Box.createVerticalGlue());
    }

    private static void styleButton(JButton button) {
        button.setBackground(Color.decode("#A6D6D6"));
        button.setForeground(Color.decode("#141414"));
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setSize(300, 50);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
    }


}
