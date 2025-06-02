import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
public class ScriptEditorWindow extends JFrame {
    private Controller controller;
    private HistoryWindow historyWindow;
    private MainGUI mainGUI;
    private RSyntaxTextArea scriptEditor;
    private JButton loadScriptButton;
    private JButton saveScriptButton;
    private JButton runScriptButton;
    private JList<String> variablesList;

    public ScriptEditorWindow(Controller controller, MainGUI mainGUI, HistoryWindow historyWindow) {
        super("Script Editor");
        this.controller = controller;
        this.historyWindow = historyWindow;
        this.mainGUI = mainGUI;
        initializeUIComponents();
        setupMainFrame();
    }

    private void initializeUIComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel topPanel = createTopControlPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);
        scriptEditor = createScriptEditorArea();
        RTextScrollPane editorScrollPane = new RTextScrollPane(scriptEditor);
        mainPanel.add(editorScrollPane, BorderLayout.CENTER);
        JPanel variablesPanel = createVariablesPanel();
        mainPanel.add(variablesPanel, BorderLayout.EAST);
        add(mainPanel);
    }

    private JPanel createTopControlPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        loadScriptButton = new JButton("Load Script");
        saveScriptButton = new JButton("Save script");
        runScriptButton = new JButton("Run script");
        loadScriptButton.addActionListener(e -> loadScriptFromFile());
        saveScriptButton.addActionListener(e -> saveScriptToFile());
        runScriptButton.addActionListener(e -> runScript());
        topPanel.add(loadScriptButton);
        topPanel.add(saveScriptButton);
        topPanel.add(runScriptButton);
        return topPanel;
    }

    private RSyntaxTextArea createScriptEditorArea() {
        RSyntaxTextArea editor = new RSyntaxTextArea(20, 60);
        editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
        editor.setCodeFoldingEnabled(true);

        try {
            Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
            theme.apply(editor);
        } catch (IOException e) {
            e.printStackTrace();
            historyWindow.appendLog("ERROR", "Failed to load theme for script editor: " + e.getMessage());
        }
        return editor;
    }

    private JPanel createVariablesPanel() {
        JPanel variablesPanel = new JPanel(new BorderLayout());
        variablesPanel.setPreferredSize(new Dimension(200, 0));
        variablesPanel.setBorder(BorderFactory.createTitledBorder("Available Variables"));

        Controller.ModelVariables modelVar = controller.getModelVariables();
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (int i = 0; i < modelVar.names.size(); i++) {
            String varName = modelVar.names.get(i);
            listModel.addElement(varName);
        }
        variablesList = new JList<>(listModel);

        variablesList.setFont(new Font("Arial", Font.PLAIN, 12));
        variablesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScrollPane = new JScrollPane(variablesList);
        variablesPanel.add(listScrollPane, BorderLayout.CENTER);

        return variablesPanel;
    }

    private void setupMainFrame() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadScriptFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load Script File");
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder scriptContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    scriptContent.append(line).append("\n");
                }
                scriptEditor.setText(scriptContent.toString());
                historyWindow.appendLog("INFO", "Script loaded from file: " + file.getName());
                JOptionPane.showMessageDialog(this, "Script loaded successfully from: " + file.getName(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                historyWindow.appendLog("ERROR", "Failed to load script: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Failed to load script.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveScriptToFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Script File");
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(scriptEditor.getText());
                historyWindow.appendLog("INFO", "Script saved to file: " + file.getName());
                JOptionPane.showMessageDialog(this, "Script saved successfully to: " + file.getName(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
                historyWindow.appendLog("ERROR", "Failed to save script: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Failed to save script.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void runScript() {
        String scriptContent = scriptEditor.getText().trim();
        if (scriptContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Script is empty. Please write or load a script.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            controller.runScript(scriptContent);
            historyWindow.appendLog("INFO", "Script executed successfully.");
            JOptionPane.showMessageDialog(this, "Script executed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

            mainGUI.refreshResultsTable();

        } catch (Exception e) {
            e.printStackTrace();
            historyWindow.appendLog("ERROR", "Failed to execute script: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to execute script.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}