import com.formdev.flatlaf.FlatDarculaLaf;
import javax.swing.table.DefaultTableCellRenderer;
import java.text.DecimalFormat;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.table.DefaultTableModel;


public class MainGUI extends JFrame {
    private HistoryWindow historyWindow;
    private ChartManager chartManager;
    private Controller controller;
    private JPanel mainPanel;
    private JTable resultsTable;
    private JScrollPane tableScrollPane;

    public MainGUI() {
        super("Savings Simulator v1.0");
        configureLookAndFeel();
        initializeMainComponents();
        setupMainWindow();
    }
    private void selectModel() {
        String[] availableModels = {"ModelSavings", "Model2", "Model3", "Model4"};

        String chosen = (String)JOptionPane.showInputDialog(
                this,
                "Select your model:",
                "Models",
                JOptionPane.PLAIN_MESSAGE,
                null,
                availableModels,
                availableModels[0]
        );


        if (chosen != null) {
            controller.setModel(chosen);
            resultsTable.setModel(new DefaultTableModel());
            updateResultsTable(controller.getModelVariables(), false);

            historyWindow.appendLog("INFO", "Model changed to: " + chosen);
            JOptionPane.showMessageDialog(
                    this,
                    "Model changed to: " + chosen,
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
    private JPanel buildMainScreenPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        NavigationPanel navigationPanel = new NavigationPanel(
                e -> onMainOptionClick(e),
                e -> onScriptOptionClick(e),
                e -> onChartOptionClick(e),
                e -> openHistoryWindow(),
                e -> showSettings());

        JPanel navigationWrapper = new JPanel(new BorderLayout());
        navigationWrapper.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        navigationWrapper.setOpaque(false);
        navigationWrapper.add(navigationPanel, BorderLayout.CENTER);

        mainPanel.add(navigationWrapper, BorderLayout.WEST);

        resultsTable = new JTable();
        tableScrollPane = new JScrollPane(resultsTable);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        return mainPanel;
    }

    private void initializeMainComponents() {
        historyWindow = new HistoryWindow();
        controller = new Controller("ModelSavings", historyWindow);
        chartManager = new ChartManager(historyWindow);
        mainPanel = buildMainScreenPanel();
        add(mainPanel);
    }

    private void setupMainWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (Exception e) {
            e.printStackTrace();
            historyWindow.appendLog("ERROR", "Failed to set Look and Feel: " + e.getMessage());
        }
    }

    private void onMainOptionClick(ActionEvent e) {
        String command = ((JButton)e.getSource()).getText();
        switch (command) {
            case "Select model":
                selectModel();
                break;
            case "Load data":
                loadData();
                break;
            case "Run model":
                runModel();
                break;
            case "Get results":
                getResults();
                break;
        }
    }


    private void onScriptOptionClick(ActionEvent e) {
        String command = ((JButton)e.getSource()).getText();
        switch (command) {
            case "Write script":
                new ScriptEditorWindow(controller, this,historyWindow);
                break;
            case "Open script":
                executeScriptFromFile();
                break;
        }
    }

    private void onChartOptionClick(ActionEvent e) {
        String command = ((JButton)e.getSource()).getText();
        Controller.ModelVariables modelVar = controller.getModelVariables();
        switch (command) {
            case "Linear chart":
                chartManager.generateChart(modelVar, "LINE");
                break;
            case "Area chart":
                chartManager.generateChart(modelVar, "AREA");
                break;
        }
    }

    private void loadData() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            resultsTable.setModel(new DefaultTableModel());
            controller.readDataFrom(file.getAbsolutePath());
            historyWindow.appendLog("INFO", "Data loaded successfully from: " + file.getName());
            JOptionPane.showMessageDialog(this, "Data loaded successfully from: " + file.getName());
        }
    }

    private void runModel() {
        controller.runModel();
        updateResultsTable(controller.getModelVariables(), false);
    }

    private void getResults() {
        String tsv = controller.getResultsAsTsv();
        JTextArea textArea = createReadOnlyTextArea(tsv);
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(600, 300));

        historyWindow.appendLog("INFO", "Displayed combined results as TSV.");
        JOptionPane.showMessageDialog(this, scroll, "Combined Results TSV", JOptionPane.INFORMATION_MESSAGE);
    }

    private void executeScriptFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open and Execute Script File");
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                controller.runScriptFromFile(file.getAbsolutePath());
                historyWindow.appendLog("INFO", "Script executed successfully from file: " + file.getName());
                JOptionPane.showMessageDialog(this, "Script executed successfully from: " + file.getName(), "Success", JOptionPane.INFORMATION_MESSAGE);

                refreshResultsTable();
            } catch (Exception ex) {
                historyWindow.appendLog("ERROR", "Failed to execute script: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Failed to execute script: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showSettings() {
        JOptionPane.showMessageDialog(this, "Settings clicked. No options for now:) ");
    }

    private JTextArea createReadOnlyTextArea(String text) {
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setCaretPosition(0);
        return textArea;
    }

    private void openHistoryWindow() {
        if (historyWindow != null) {
            historyWindow.setVisible(true);
            historyWindow.toFront();
        }
    }

    private void updateResultsTable(Controller.ModelVariables modelVar, boolean isScript) {
        historyWindow.appendLog("INFO", "Generating TSV with BoundVariables: " + modelVar.names);
        String[] columnNames = generateColumnNames(modelVar);
        Object[][] tableData = generateTableData(modelVar);

        resultsTable.setModel(new DefaultTableModel(tableData, columnNames));
        formatResultsTable();
        historyWindow.appendLog("INFO", "Table has been updated.");
    }

    public void refreshResultsTable() {
        updateResultsTable(controller.getModelVariables(), false);
    }

    private String[] generateColumnNames(Controller.ModelVariables modelVar) {
        if (modelVar == null || modelVar.names.isEmpty()) {
            return new String[]{"LATA"};
        }
        String[] columnNames = new String[1 + modelVar.names.size()];
        columnNames[0] = "LATA";
        for (int i = 0; i < modelVar.names.size(); i++) {
            String varName = modelVar.names.get(i);
            columnNames[i + 1] = varName;
        }

        return columnNames;
    }


    private Object[][] generateTableData(Controller.ModelVariables modelVar) {
        int rowCount = modelVar.periodCount;
        int colCount = modelVar.names.size();
        Object[][] tableData = new Object[rowCount][colCount + 1];

        for (int r = 0; r < rowCount; r++) {
            tableData[r][0] = modelVar.periodLabels.get(r);
        }

        DecimalFormat df = new DecimalFormat("#,##0");

        for (int varIndex = 0; varIndex < modelVar.names.size(); varIndex++) {
            String varName = modelVar.names.get(varIndex);
            Object val = modelVar.values.get(varIndex);
            for (int r = 0; r < rowCount; r++) {
                tableData[r][varIndex + 1] = formatTableValue(varName, val, r);
            }
        }

        return tableData;
    }

    private Object formatTableValue(String varName, Object val, int rowIndex) {
        double[] arr = (double[]) val;
        double value = arr[Math.min(rowIndex, arr.length - 1)];
        if (varName.equals("savingFraction")) {
            return formatDouble(value * 100) + "%";
        }

        return formatDouble(value);
    }



    private String formatDouble(double value) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(value);
    }


    private void formatResultsTable() {
        resultsTable.getTableHeader().setFont(new Font("Courier New", Font.BOLD, 16));
        resultsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        resultsTable.setRowHeight(24);

        resultsTable.setGridColor(Color.GRAY);
        resultsTable.setSelectionBackground(new Color(255, 254, 254, 87));
        resultsTable.setSelectionForeground(Color.WHITE);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i = 1; i < resultsTable.getColumnCount(); i++) {
            resultsTable.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        }

        historyWindow.appendLog("INFO", "Table has been updated.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI());
    }
}