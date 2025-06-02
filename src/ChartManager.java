import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import org.jfree.chart.plot.PlotOrientation;

public class ChartManager {
    private HistoryWindow historyWindow;

    public ChartManager(HistoryWindow historyWindow) {
        this.historyWindow = historyWindow;
    }

    public void generateChart(Controller.ModelVariables modelVar, String chartType) {
        if (chartType.equals("LINE") && !modelVar.names.contains("totalSavings")) {
            showMissingDataWarning("totalSavings");
            return;
        }

        if (chartType.equals("AREA") && !hasRequiredAreaChartData(modelVar)) {
            showMissingDataWarning("required variables for Area chart");
            return;
        }

        JFreeChart chart = createChart(modelVar, chartType);
        if (chart != null) {
            displayChart(chart, chartType);
        } else {
            historyWindow.appendLog("ERROR", "Failed to create the " + chartType + " Chart.");
            JOptionPane.showMessageDialog(null, "Failed to create the " + chartType + " Chart.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean hasRequiredAreaChartData(Controller.ModelVariables modelVar) {
        String[] requiredVars = {"monthlyIncome", "savingFraction", "ethereumDollar", "ETHquantity"};
        for (String var : requiredVars) {
            if (!modelVar.names.contains(var)) return false;
        }
        return true;
    }

    private void showMissingDataWarning(String missingData) {
        historyWindow.appendLog("WARNING", "'" + missingData + "' variable not found. Please load data and run the model first.");
        JOptionPane.showMessageDialog(null, "'" + missingData + "' variable not found. Please load data and run the model first.", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JFreeChart createChart(Controller.ModelVariables modelVar, String chartType) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        double[] totalSavings = getDoubleArray(modelVar, "totalSavings");
        double[] monthlyIncome = getDoubleArray(modelVar, "monthlyIncome");
        double[] savingFraction = getDoubleArray(modelVar, "savingFraction");
        double[] ethereumDollar = getDoubleArray(modelVar, "ethereumDollar");
        double[] ETHquantity = getDoubleArray(modelVar, "ETHquantity");

        double cumulativeIncome = 0;
        double cumulativeEthProfit = 0;

        for (int i = 0; i < modelVar.periodCount; i++) {
            String formattedPeriod = formatPeriodLabel(modelVar.periodLabels.get(i));

            if (chartType.equals("LINE")) {
                dataset.addValue(totalSavings[i], "Total Savings ($)", formattedPeriod);
            } else if (chartType.equals("AREA")) {
                dataset.addValue(totalSavings[i], "Total Savings ($)", formattedPeriod);
                double income = monthlyIncome[i] * savingFraction[i];
                cumulativeIncome += income;
                dataset.addValue(cumulativeIncome, "Income($)", formattedPeriod);
                double ethProfit = i == 0 ? 0 : (ethereumDollar[i] - ethereumDollar[i - 1]) * ETHquantity[i];
                cumulativeEthProfit += ethProfit;
                dataset.addValue(cumulativeEthProfit, "ETH($)", formattedPeriod);
            }
        }

        JFreeChart chart;
        if (chartType.equals("LINE")) {
            chart = ChartFactory.createLineChart(
                    "Total Savings Over Time",
                    "Period",
                    "Amount ($)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );
            customizeLineChart(chart);
        } else {
            chart = ChartFactory.createAreaChart(
                    "Total Savings Over Time",
                    "Period",
                    "Amount ($)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );
            customizeAreaChart(chart);
        }

        historyWindow.appendLog("INFO", "Model has been created.");
        return chart;
    }

    private double[] getDoubleArray(Controller.ModelVariables modelVar, String varName) {
        int index = modelVar.names.indexOf(varName);
        if (index == -1) {
            return new double[0];
        }
        return (double[]) modelVar.values.get(index);
    }


    private String formatPeriodLabel(String original) {
        String[] parts = original.split("_");
        if (parts.length == 2) {
            return parts[1] + "/" + parts[0].substring(2);
        }
        return original;
    }

    private void customizeLineChart(JFreeChart chart) {
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.getDomainAxis().setCategoryLabelPositions(
                org.jfree.chart.axis.CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0)
        );
        plot.getDomainAxis().setTickLabelFont(new Font("Arial", Font.PLAIN, 14));
        plot.getRangeAxis().setTickLabelFont(new Font("Arial", Font.PLAIN, 14));

        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(75, 0, 130));
        renderer.setSeriesStroke(0, new BasicStroke(5f));
        renderer.setSeriesOutlinePaint(0, new Color(0, 0, 0, 50));
        renderer.setSeriesOutlineStroke(0, new BasicStroke(5.0f));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new RoundRectangle2D.Double(-4, -4, 8, 8, 4, 4));
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelFont(new Font("Arial", Font.PLAIN, 12));
        renderer.setDefaultItemLabelPaint(Color.BLACK);

        plot.setRenderer(renderer);
        chart.getLegend().setItemFont(new Font("Arial", Font.PLAIN, 14));
    }

    private void customizeAreaChart(JFreeChart chart) {
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.getDomainAxis().setCategoryLabelPositions(
                org.jfree.chart.axis.CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0)
        );
        plot.getDomainAxis().setTickLabelFont(new Font("Arial", Font.PLAIN, 14));
        plot.getRangeAxis().setTickLabelFont(new Font("Arial", Font.PLAIN, 14));

        AreaRenderer renderer = new AreaRenderer();
        renderer.setSeriesPaint(0, new Color(100, 149, 237, 128));
        renderer.setSeriesPaint(1, new Color(60, 179, 113, 128));
        renderer.setSeriesPaint(2, new Color(255, 99, 71, 128));
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesStroke(1, new BasicStroke(2.0f));
        renderer.setSeriesStroke(2, new BasicStroke(2.0f));

        plot.setRenderer(renderer);
        chart.getLegend().setItemFont(new Font("Arial", Font.PLAIN, 14));
    }

    private void displayChart(JFreeChart chart, String chartType) {
        String title = chartType.equals("LINE") ? "Total Savings Over Time - Line Chart" : "Total Savings Over Time - Area Chart";
        JFrame chartFrame = new JFrame(title);
        chartFrame.setSize(800, 600);
        chartFrame.setLocationRelativeTo(null);
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartFrame.add(chartPanel);
        chartFrame.setVisible(true);

        historyWindow.appendLog("INFO", chartType.equals("LINE") ? "Line chart generated and displayed." : "Area chart generated and displayed.");
    }
}