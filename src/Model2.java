public class Model2 {
    @Bind private int LL;
    @Bind private double[] capital;
    @Bind private double[] growthRate;
    @Bind private double[] result;

    private HistoryWindow historyWindow;

    public Model2(HistoryWindow historyWindow) {
        this.historyWindow = historyWindow;
    }

    public Model2() {
    }

    public void run() {
        result = new double[LL];
        for (int t = 0; t < LL; t++) {
            result[t] = capital[t] * growthRate[t];
        }
    }
}
