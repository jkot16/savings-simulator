import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import javax.swing.JOptionPane;
import java.io.BufferedReader;
public class Controller {
    private static final String FIELD_LL = "LL";
    private Binding binding;
    private GroovyShell groovy;
    private Map<String, double[]> scriptVariables;
    private Object modelInstance;
    private Class<?> modelClass;
    private int periodCount;
    private final List<String> periodLabels = new ArrayList<>();
    private final List<String> modelVariableNames = new ArrayList<>();
    private HistoryWindow historyWindow;

    public Controller(String modelName, HistoryWindow historyWindow) {
        this.historyWindow = historyWindow;
        try {
            modelClass = Class.forName(modelName);
            modelInstance = modelClass.getDeclaredConstructor(HistoryWindow.class).newInstance(historyWindow);
            binding = new Binding();
            groovy = new GroovyShell(binding);
            scriptVariables = new LinkedHashMap<String, double[]>();

            for (Field f : modelClass.getDeclaredFields()) {
                if (f.isAnnotationPresent(Bind.class) && !f.getName().equals(FIELD_LL)) {
                    modelVariableNames.add(f.getName());
                }
            }

            historyWindow.appendLog("INFO", "Controller initialized with model: " + modelName);
        } catch (Exception e) {
            historyWindow.appendLog("ERROR", "Failed to initialize Controller with model: " + modelName + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void reset() {
        periodLabels.clear();
        periodCount = 0;
        scriptVariables.clear();
        for (Field f : modelClass.getDeclaredFields()) {
            if (f.isAnnotationPresent(Bind.class)) {
                f.setAccessible(true);
                try {
                    if (f.getName().equals("LL")) {
                        f.setInt(modelInstance, 0);
                    } else if (f.getType() == double[].class) {
                        f.set(modelInstance, new double[0]);
                    }
                } catch (IllegalAccessException e) {
                    historyWindow.appendLog("ERROR", "Error resetting field '" + f.getName() + "': " + e.getMessage());
                }
            }
        }

        historyWindow.appendLog("INFO", "Controller has been reset.");
    }


    public Controller readDataFrom(String fname) {
        historyWindow.appendLog("INFO", "Attempting to read data from file: " + fname);
        reset();

        try (Scanner sc = new Scanner(new FileReader(fname))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }

                if (line.startsWith("LATA")) {
                    String[] tokens = line.split("\\s+");
                    for (int i = 1; i < tokens.length; i++) {
                        periodLabels.add(tokens[i]);
                    }
                    periodCount = periodLabels.size();

                    setFieldValue("LL", periodCount);
                    historyWindow.appendLog("INFO", "Set LL based on LATA -> LL=" + periodCount);
                } else {
                    String[] tokens = line.split("\\s+");
                    String varName = tokens[0];

                    List<Double> values = new ArrayList<>();
                    for (int i = 1; i < tokens.length; i++) {
                        try {
                            double d = Double.parseDouble(tokens[i]);
                            values.add(d);
                        } catch (NumberFormatException ex) {
                            historyWindow.appendLog("WARNING",
                                    "Ignored non-numeric value: " + tokens[i] + " for variable " + varName);
                        }
                    }

                    Field f = getBindFieldByName(varName);
                    if (f != null && f.getType() == double[].class) {
                        double[] arr = new double[periodCount];
                        for (int i = 0; i < periodCount; i++) {
                            if (i < values.size()) {
                                arr[i] = values.get(i);
                            } else {
                                arr[i] = values.isEmpty() ? 0.0 : values.get(values.size() - 1);
                            }
                        }
                        setFieldValue(varName, arr);
                        historyWindow.appendLog("INFO",
                                "Set " + varName + " = " + Arrays.toString(arr));
                    } else {
                        historyWindow.appendLog("WARNING", "Ignored variable or unsupported type: " + varName);
                    }
                }
            }
            historyWindow.appendLog("INFO", "Data loaded successfully from: " + fname);
        } catch (IOException e) {
            historyWindow.appendLog("ERROR",
                    "Error reading file: " + fname + " - " + e.getMessage());
        }
        return this;
    }


    public Controller runModel() {
        historyWindow.appendLog("INFO", "Initiating model run.");
        try {
            modelClass.getMethod("run").invoke(modelInstance);
            historyWindow.appendLog("INFO", "Model has been run successfully.");
        } catch (Exception e) {
            historyWindow.appendLog("ERROR", "Failed to run model - " + e.getMessage());
            e.printStackTrace();
        }
        return this;
    }

    public Controller runScript(String scriptCode) {
        historyWindow.appendLog("INFO", "Executing script.");
        try {
            setBindingVariables();

            Script script = groovy.parse(scriptCode);
            script.run();

            extractScriptVariables();
            historyWindow.appendLog("INFO", "Script has been executed successfully.");
        } catch (Exception e) {
            historyWindow.appendLog("ERROR", "Error executing script - " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error executing script: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return this;
    }

    private void setBindingVariables() throws IllegalAccessException {
        for (Field f : modelClass.getDeclaredFields()) {
            if (f.isAnnotationPresent(Bind.class)) {
                f.setAccessible(true);
                binding.setVariable(f.getName(), f.get(modelInstance));
            }
        }
    }

    private void extractScriptVariables() {
        for (Object key : binding.getVariables().keySet()) {
            String varName = (String) key;
            if (isScriptVariable(varName) && !isIgnoredVariable(varName) && !modelVariableNames.contains(varName)) {
                try {
                    double[] value = (double[]) binding.getVariable(varName);
                    scriptVariables.put(varName, value);
                    historyWindow.appendLog("INFO", "Script variable '" + varName + "' = " + Arrays.toString(value));
                } catch (ClassCastException e) {
                    historyWindow.appendLog("ERROR", "Variable '" + varName + "' is not of type double[].");
                    throw e;
                }
            }
        }

    }


    public Controller runScriptFromFile(String fname) {
        historyWindow.appendLog("INFO", "Executing script from file: " + fname);
        try {
            StringBuilder scriptBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(fname))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    scriptBuilder.append(line).append("\n");
                }
            }
            runScript(scriptBuilder.toString());
            historyWindow.appendLog("INFO", "Script executed from file: " + fname);
        } catch (IOException e) {
            historyWindow.appendLog("ERROR", "Error reading script file: " + fname + " - " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error reading script file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return this;
    }

    public ModelVariables getModelVariables() {
        ModelVariables modelVar = new ModelVariables();
        modelVar.periodCount = periodCount;
        modelVar.periodLabels.addAll(periodLabels);

        for (String varName : modelVariableNames) {
            try {
                Field f = modelClass.getDeclaredField(varName);
                f.setAccessible(true);
                double[] val = (double[])f.get(modelInstance);
                modelVar.names.add(varName);
                modelVar.values.add(val);
            } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
                historyWindow.appendLog("ERROR", "Error retrieving variable '" + varName + "': " + e.getMessage());
            }
        }

        for (Map.Entry<String, double[]> entry : scriptVariables.entrySet()) {
            modelVar.names.add(entry.getKey());
            modelVar.values.add(entry.getValue());
        }

        return modelVar;
    }
    public void setModel(String modelName) {
        try {
            this.modelClass = Class.forName(modelName);

            try {
                this.modelInstance = modelClass.getDeclaredConstructor(HistoryWindow.class).newInstance(historyWindow);
            } catch (NoSuchMethodException ex) {

                historyWindow.appendLog("WARNING",
                        "Model " + modelName + " has no (HistoryWindow) constructor.");
                this.modelInstance = modelClass.getDeclaredConstructor().newInstance();
            }


            binding = new Binding();
            groovy = new GroovyShell(binding);
            scriptVariables = new LinkedHashMap<>();

            modelVariableNames.clear();
            for (Field f : modelClass.getDeclaredFields()) {
                if (f.isAnnotationPresent(Bind.class) && !f.getName().equals(FIELD_LL)) {
                    modelVariableNames.add(f.getName());
                }
            }

            historyWindow.appendLog("INFO", "Controller re-initialized with model: " + modelName);
        } catch (Exception e) {
            historyWindow.appendLog("ERROR", "Failed to re-initialize Controller with model: " + modelName + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getResultsAsTsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("LATA");
        for (String lbl : periodLabels) {
            sb.append("\t").append(lbl);
        }
        sb.append("\n");

        for (Field f : modelClass.getDeclaredFields()) {
            if (f.isAnnotationPresent(Bind.class)) {
                f.setAccessible(true);
                String varName = f.getName();
                sb.append(varName);

                try {
                    if (varName.equals(FIELD_LL)) {
                        int llVal = f.getInt(modelInstance);
                        for (int i = 0; i < periodCount; i++) {
                            sb.append("\t").append(llVal);
                        }
                    } else if (f.getType() == double[].class) {
                        double[] arr = (double[]) f.get(modelInstance);
                        for (double d : arr) {
                            sb.append("\t").append(d);
                        }
                    }
                } catch (IllegalAccessException e) {
                    historyWindow.appendLog("ERROR", "Error reading field '" + varName + "': " + e.getMessage());
                }
                sb.append("\n");
            }
        }

        for (Map.Entry<String, double[]> entry : scriptVariables.entrySet()) {
            String varName = entry.getKey();
            sb.append(varName);

            double[] arr = entry.getValue();
            for (double d : arr) {
                sb.append("\t").append(d);
            }
            sb.append("\n");
        }
        historyWindow.appendLog("INFO", "Generated TSV results.");
        return sb.toString();
    }


    public static class ModelVariables {
        public int periodCount;
        public List<String> periodLabels = new ArrayList<>();
        public List<String> names = new ArrayList<>();
        public List<double[]> values = new ArrayList<>();
    }


    private Field getBindFieldByName(String name) {
        for (Field f : modelClass.getDeclaredFields()) {
            if (f.isAnnotationPresent(Bind.class) && f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }

    private void setFieldValue(String fieldName, Object val) {
        Field f = getBindFieldByName(fieldName);
        if (f == null) {
            historyWindow.appendLog("WARNING", "Field '" + fieldName + "' not found in the model.");
            return;
        }
        f.setAccessible(true);
        try {
            if (f.getName().equals(FIELD_LL)) {
                f.setInt(modelInstance, (Integer) val);
                historyWindow.appendLog("INFO", "Set LL to value: " + val);
            } else {
                f.set(modelInstance, (double[]) val);
                historyWindow.appendLog("INFO", "Set field '" + fieldName + "' to array of length " + ((double[]) val).length);
            }
        } catch (IllegalAccessException e) {
            historyWindow.appendLog("ERROR", "Error setting field value for '" + fieldName + "': " + e.getMessage());
        } catch (ClassCastException e) {
            historyWindow.appendLog("ERROR", "Type mismatch when setting field '" + fieldName + "': " + e.getMessage());
        }
    }


    private boolean isScriptVariable(String name) {
        return !name.equals("LL");
    }

    private boolean isIgnoredVariable(String name) {
        return name.length() == 1 && Character.isLowerCase(name.charAt(0));
    }
}