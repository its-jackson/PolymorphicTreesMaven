package scripts.api;

/**
 * Purpose of class: Control the player's gold for planking.
 */

public class Gold {
    private static String goldRegex = "";
    private static int goldSpentTotal = 0;
    private static int goldTotalBank = -1;

    private static final int factor_thousand = 1000;
    private static final int factor_million = 1000000;
    private static final int max_gold = 2147000000;

    // private constructor, cannot instantiate class
    private Gold() {
    }

    /**
     * Calculate the amount of gold.
     * Used for validation purposes.
     *
     * @return Actual amount of gold to be withdrawn from bank; zero otherwise.
     */
    public static int calculateActualGoldRegex(String gold) {
        gold = gold
                .trim()
                .toLowerCase();

        if (gold.isEmpty() || gold.isBlank()) {
            return 0;
        }

        int actualGold = 0;

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

        if (actualGold <= getMaxGold() && actualGold > 0) {
            return actualGold;
        }

        return 0;
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

    public static int getMaxGold() {
        return max_gold;
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
        if (Gold.goldSpentTotal == 0) {
            Gold.goldSpentTotal = goldSpentTotal;
        } else {
            Gold.goldSpentTotal += goldSpentTotal;
        }
    }

    public static void resetGoldSpentTotal() {
        setGoldSpentTotal(0);
    }

    public static void resetGoldTotalBank() {
        setGoldTotalBank(-1);
    }
}
