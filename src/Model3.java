public class Model3 {
    @Bind private int LL;
    @Bind private double[] debt;
    @Bind private double[] interest;
    @Bind private double[] newDebt;

    private HistoryWindow historyWindow;

    public Model3(HistoryWindow historyWindow) {
        this.historyWindow = historyWindow;
    }

    public Model3() {
    }

    public void run() {
        newDebt = new double[LL];
        for (int t = 0; t < LL; t++) {
            newDebt[t] = debt[t] * (1 + interest[t]);
        }
    }
}
