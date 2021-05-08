package scripts.api;

import org.tribot.api.General;

/**
 * Purpose of class: Retrieve the amount of gold or all gold from the bank, when we run out of gold.
 * The player can either start with any amount of gold in the inventory
 * or start without gold.
 */

public class Gold extends Node {
    private static String goldRegex = "";
    private static int goldSpentTotal = 0;
    private static int goldTotalBank = -1;

    private static final int factor_thousand = 1000;
    private static final int factor_million = 1000000;
    private static final int max = 2147000000;

    @Override
    public void execute(Task task) {

    }

    @Override
    public boolean validate(Task task) {
        return false;
    }

    @Override
    public void debug(String status) {
        String format = ("[Gold Control] ");
        Globals.STATE = (status);
        General.println(format.concat(status));
    }

    /**
     * Calculate the amount of gold.
     * Used for validation purposes.
     *
     * @return Actual amount of gold to be withdrawn from bank; zero otherwise.
     */
    public static int calculateActualGoldRegex(String gold) {
        gold =
                gold
                        .trim()
                        .toLowerCase();

        int actualGold = 0;

        if (gold.isEmpty() || gold.isBlank()) {
            return 0;
        }

        if (gold.matches("\\d+k")) {
            // parse in thousand (only numbers with "k" at the end)
            StringBuilder sb = new StringBuilder(gold);
            sb.deleteCharAt(sb.indexOf("k"));
            actualGold = (Integer.parseInt(sb.toString()) * getFactorThousand());
        } else if (gold.matches("\\d+m")) {
            // parse in thousand (only numbers with "k" at the end)
            StringBuilder sb = new StringBuilder(gold);
            sb.deleteCharAt(sb.indexOf("m"));
            actualGold = (Integer.parseInt(sb.toString()) * getFactorMillion());
        } else if (gold.matches("\\d+")) {
            // parse actual integer from string if correct format (only numbers)
            actualGold = Integer.parseInt(gold);
        }

        if (actualGold <= max && actualGold > 0) {
            return actualGold;
        }

        return 0;
    }

    private boolean shouldFetchGold(Task task) {

        return false;
    }

    public static int getGoldTotalBank() {
        return goldTotalBank;
    }

    public static void setGoldTotalBank(int goldTotalBank) {
        Gold.goldTotalBank = goldTotalBank;
    }

    public static int getFactorThousand() {
        return factor_thousand;
    }

    public static int getFactorMillion() {
        return factor_million;
    }

    public static int getMax() {
        return max;
    }

    public static String getGoldRegex() {
        return goldRegex;
    }

    public static void setGoldRegex(String goldRegex) {
        Gold.goldRegex = goldRegex;
    }

    public static int getGoldSpentTotal() {
        return goldSpentTotal;
    }

    public static void setGoldSpentTotal(int goldSpentTotal) {
        Gold.goldSpentTotal += goldSpentTotal;
    }

    public static void resetGoldSpentTotal() {
        setGoldSpentTotal(0);
    }
}
