import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class RoundedButton extends JButton {
    private boolean hovered = false;
    private final int cornerRadius = 20;
    private final boolean isMainButton;

    public RoundedButton(String text, boolean isMainButton) {
        super(text);
        this.isMainButton = isMainButton;
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setForeground(Color.WHITE);
        setFont(isMainButton ? new Font("Tahoma", Font.BOLD, 20) : new Font("Tahoma", Font.PLAIN, 14));
        setOpaque(false);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, isMainButton ? 60 : 50));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (hovered) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(60, 60, 60));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.dispose();
        }
        super.paintComponent(g);
    }
}