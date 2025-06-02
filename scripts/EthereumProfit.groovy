ethereumProfit = new double[LL]

for (int i = 1; i < LL; i++) {
    ethereumProfit[i] = (ethereumDollar[i] - ethereumDollar[i-1]) * ETHquantity[i]
}