public class ModelSavings {
    @Bind private int LL;

    @Bind private double[] monthlyIncome;
    @Bind private double[] savingFraction;

    @Bind private double[] ethereumDollar;
    @Bind private double[] ETHquantity;

    @Bind private double[] bankDeposit;
    @Bind private double[] bankDepositRate;

    @Bind private double[] initialSavings;

    @Bind private double[] totalSavings;

    private HistoryWindow historyWindow;

    public ModelSavings(HistoryWindow historyWindow) {
        this.historyWindow = historyWindow;
        historyWindow.appendLog("INFO", "ModelSavings initialized.");
    }

    public void run() {
        if (LL <= 0) {
            historyWindow.appendLog("ERROR", "Invalid number of months (LL <= 0). Calculations cannot proceed.");
            return;
        }

        totalSavings = new double[LL];
        historyWindow.appendLog("INFO", "Model calculations started for " + LL + " months.");

        double previousEthPrice = 0;

        for (int i = 0; i < LL; i++) {
            double currentMonthlyIncome = (i < monthlyIncome.length) ? monthlyIncome[i] : monthlyIncome[monthlyIncome.length - 1];

            double savingRate = (i < savingFraction.length) ? savingFraction[i] : savingFraction[savingFraction.length - 1];

            double currentEthPrice = (i < ethereumDollar.length) ? ethereumDollar[i] : ethereumDollar[ethereumDollar.length - 1];

            double ethProfit;
            if (i == 0) {
                ethProfit = 0;
            } else {
                double previousPrice = (i - 1 < ethereumDollar.length) ? ethereumDollar[i - 1] : ethereumDollar[ethereumDollar.length - 1];
                ethProfit = (currentEthPrice - previousPrice) * ETHquantity[i];
            }

            double bankInterest = (i < bankDeposit.length && i < bankDepositRate.length) ? (bankDeposit[i] * bankDepositRate[i])
                    : (bankDeposit[bankDeposit.length - 1] * bankDepositRate[bankDepositRate.length - 1]);

            double initSavings = (i < initialSavings.length) ? initialSavings[i] : initialSavings[initialSavings.length - 1];
            double savedIncome = currentMonthlyIncome * savingRate;

            if (i == 0) {
                totalSavings[i] = savedIncome + ethProfit + bankInterest + initSavings;
                historyWindow.appendLog("INFO", String.format("Month %d: Initial savings set to %.2f", i + 1, totalSavings[i]));
            } else {
                totalSavings[i] = totalSavings[i - 1] + savedIncome + ethProfit + bankInterest + initSavings;
            }

            if (i == 0 || i == LL - 1) {
                historyWindow.appendLog("INFO", String.format("Month %d: Total Savings = %.2f", i + 1, totalSavings[i]));
            }

            previousEthPrice = currentEthPrice;
        }

        historyWindow.appendLog("INFO", "Model calculations completed successfully.");
    }
}