import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HistoryWindow extends JFrame {
    private JTextPane logArea;
    private JScrollPane scrollPane;
    private final List<LogEntry> logEntries = new ArrayList<>();
    private JCheckBox infoCheckBox;
    private JCheckBox warningCheckBox;
    private JCheckBox errorCheckBox;
    private int lineCounter = 1;

    public HistoryWindow() {
        super("History");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoCheckBox = new JCheckBox("INFO", true);
        warningCheckBox = new JCheckBox("WARNING", true);
        errorCheckBox = new JCheckBox("ERROR", true);

        topPanel.add(infoCheckBox);
        topPanel.add(warningCheckBox);
        topPanel.add(errorCheckBox);

        infoCheckBox.addItemListener(e -> filterLogs());
        warningCheckBox.addItemListener(e -> filterLogs());
        errorCheckBox.addItemListener(e -> filterLogs());

        add(topPanel, BorderLayout.NORTH);

        logArea = new JTextPane();
        logArea.setEditable(false);
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.WHITE);
        logArea.setFont(new Font("Courier New", Font.PLAIN, 14));

        scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);
    }


    public void appendLog(String level, String message) {
        addLog(new LogEntry(level, message));
    }

    private void addLog(LogEntry logEntry) {
        logEntries.add(logEntry);
        if (isLevelEnabled(logEntry.level)) {
            appendToLogArea(logEntry);
        }
    }

    private boolean isLevelEnabled(String level) {
        switch (level) {
            case "INFO":
                return infoCheckBox.isSelected();
            case "WARNING":
                return warningCheckBox.isSelected();
            case "ERROR":
                return errorCheckBox.isSelected();
            default:
                return true;
        }
    }
    private void filterLogs() {
        SwingUtilities.invokeLater(() -> {
            logArea.setText("");
            lineCounter = 1;
            for (LogEntry entry : logEntries) {
                if (isLevelEnabled(entry.level)) {
                    appendToLogArea(entry);
                }
            }
        });
    }

    private void appendToLogArea(LogEntry logEntry) {
        try {
            StyledDocument doc = logArea.getStyledDocument();
            Style style = logArea.addStyle("Style_" + logEntry.level, null);
            switch (logEntry.level) {
                case "INFO":
                    StyleConstants.setForeground(style, Color.GREEN);
                    break;
                case "WARNING":
                    StyleConstants.setForeground(style, Color.YELLOW);
                    break;
                case "ERROR":
                    StyleConstants.setForeground(style, Color.RED);
                    break;
            }
            StyleConstants.setBold(style, true);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timestamp = logEntry.timestamp.format(dtf);
            String logText = String.format("[%d] [%s] [%s] %s%n", lineCounter++, timestamp, logEntry.level, logEntry.message);
            doc.insertString(doc.getLength(), logText, style);
            logArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    private static class LogEntry {
        String level;
        String message;
        LocalDateTime timestamp;

        LogEntry(String level, String message) {
            this.level = level;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }
    }
}