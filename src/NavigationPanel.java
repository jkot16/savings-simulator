import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class NavigationPanel extends RoundedPanel {
    public NavigationPanel(ActionListener simulationListener, ActionListener scriptsListener, ActionListener chartsListener, ActionListener logsListener, ActionListener settingsListener) {
        super(30);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(220, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setOpaque(false);

        add(createNavigationMenu("Simulation", new String[]{"Select model", "Load data", "Run model", "Get results"},
                "resources/icons/play_circle_outline_24dp_5F6368.png", simulationListener));

        add(Box.createVerticalStrut(10));

        add(createNavigationMenu("Scripts", new String[]{"Write script", "Open script"},
                "resources/icons/terminal_24dp_5F6368.png", scriptsListener));

        add(Box.createVerticalStrut(10));

        add(createNavigationMenu("Charts", new String[]{"Linear chart", "Area chart"},
                "resources/icons/bar_chart_24dp_5F6368.png", chartsListener));

        add(Box.createVerticalStrut(10));

        add(createNavigationMenu("Logs", new String[]{"History"},
                "resources/icons/manage_search_24dp_5F6368 (1).png", logsListener));

        add(Box.createVerticalStrut(10));

        add(createNavigationMenu("Settings", new String[]{},
                "resources/icons/settings_24dp_5F6368.png", settingsListener));

        add(Box.createVerticalGlue());
    }

    private JPanel createNavigationMenu(String title, String[] options, String iconPath, ActionListener listener) {
        JPanel dropdownPanel = new JPanel();
        dropdownPanel.setLayout(new BoxLayout(dropdownPanel, BoxLayout.Y_AXIS));
        dropdownPanel.setOpaque(false);

        ImageIcon icon = loadIconWithScaling(iconPath, 30, 28);

        RoundedButton mainButton = new RoundedButton(title, true);
        mainButton.setIcon(icon);
        mainButton.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel optionsPanel = createMenuOptionsPanel(options, listener);
        optionsPanel.setVisible(false);
        mainButton.addActionListener(e -> updatePanelVisibility(optionsPanel));

        dropdownPanel.add(mainButton);
        dropdownPanel.add(optionsPanel);

        return dropdownPanel;
    }

    private JPanel createMenuOptionsPanel(String[] options, ActionListener listener) {
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setOpaque(false);

        for (String option : options) {
            RoundedButton button = new RoundedButton(option, false);
            button.setFont(new Font("Tahoma", Font.BOLD, 13));
            button.addActionListener(listener);
            optionsPanel.add(button);
            optionsPanel.add(Box.createVerticalStrut(5));
        }

        return optionsPanel;
    }


    private ImageIcon loadIconWithScaling(String path, int width, int height) {
        ImageIcon rawIcon = new ImageIcon(path);
        Image scaledImage = rawIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    private void updatePanelVisibility(JPanel panel) {
        panel.setVisible(!panel.isVisible());
        panel.getParent().revalidate();
        panel.getParent().repaint();
    }
}