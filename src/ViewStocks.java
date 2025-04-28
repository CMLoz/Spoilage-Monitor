import javax.swing.*;
import java.awt.*;

public class ViewStocks extends JPanel {
    public ViewStocks() {
        setLayout(null); // if you prefer absolute layout
        setBackground(Color.decode("#F4F8D3"));

        JLabel stocksLabel = new JLabel("Your Inventory");
        stocksLabel.setFont(new Font("Arial", Font.BOLD, 50));
        stocksLabel.setBounds(350, 50, 400, 60);
        add(stocksLabel);

        String[] foodTypes = {"All", "Fruits", "Vegetables", "Dairy", "Meat", "Grains"};
        JComboBox<String> foodTypeComboBox = new JComboBox<>(foodTypes);
        foodTypeComboBox.setSelectedIndex(0);
        foodTypeComboBox.setBounds(470, 150, 190, 25);
        add(foodTypeComboBox);

        JLabel productsLabel = new JLabel("Products:");
        productsLabel.setFont(new Font("Arial", Font.BOLD, 20));
        productsLabel.setBounds(370, 150, 200, 25);
        add(productsLabel);

        JButton backButton = new JButton("Back");
        backButton.setBounds(60, 450, 200, 50);
        styleButton(backButton);
        add(backButton);



        // Store action listener in a method or allow it to be set externally
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
}
