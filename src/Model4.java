public class Model4 {
    @Bind private int LL;
    @Bind private double[] inflow;
    @Bind private double[] outflow;
    @Bind private double[] balance;

    private HistoryWindow historyWindow;

    public Model4(HistoryWindow historyWindow) {
        this.historyWindow = historyWindow;
    }

    public Model4() {
    }

    public void run() {
        balance = new double[LL];
        for (int t = 0; t < LL; t++) {
            balance[t] = inflow[t] - outflow[t];
        }
    }
}
