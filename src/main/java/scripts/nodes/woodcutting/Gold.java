package scripts.nodes.woodcutting;

import org.tribot.api.General;
import scripts.api.Globals;
import scripts.api.Node;
import scripts.api.Task;

/**
 * Purpose of class: Retrieve the declared amount of gold or all gold from the bank.
 * For plank-bank class. The player can either start with any amount of gold in the inventory
 * or start without gold.
 *
 */

public class Gold extends Node {
    public static String gold = "";

    public static final int factor_thousand = 1000;
    public static final int factor_million = 1000000;
    public static final int max = 2147000000;

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
     *
     * @return Actual amount of gold to be withdrawn from bank; zero otherwise.
     */
    public static int calculateActualGold(String gold) {
        gold = gold
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
            actualGold = (Integer.parseInt(sb.toString()) * factor_thousand);
        } else if (gold.matches("\\d+m")) {
            // parse in thousand (only numbers with "k" at the end)
            StringBuilder sb = new StringBuilder(gold);
            sb.deleteCharAt(sb.indexOf("m"));
            actualGold = (Integer.parseInt(sb.toString()) * factor_million);
        } else if (gold.matches("\\d+")) {
            // parse actual integer from string if correct format (only numbers)
            actualGold = Integer.parseInt(gold);
        }

        if (actualGold <= max && actualGold > 0) {
            return actualGold;
        }

        return 0;
    }

}
