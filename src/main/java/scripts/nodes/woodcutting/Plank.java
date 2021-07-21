package scripts.nodes.woodcutting;

import org.tribot.api.General;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.*;
import scripts.api.*;
import scripts.api.antiban.AntiBan;
import scripts.dax_api.walker_engine.interaction_handling.NPCInteraction;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Purpose of class: Will perform the planking option by right clicking the sawmill operator.
 * Author: Jackson (Polymorphic~TRiBot)
 * Date:
 * Time:
 */

public class Plank extends Node {

    // filter for finding any sawmill operator
    private static Predicate<RSNPC> sawmill_operator_npc_filter() {
        return rsnpc -> rsnpc.getName().toLowerCase().contains("sawmill operator");
    }

    private final Walk walk_node = new Walk();

    @Override
    public void execute(Task task) {
        debug("Sleeping " + Workable.sleep(Globals.getWaitTimes(), AntiBan.getHumanFatigue()));

        final int plank_gold = Plank.calculateOakPlankGold(Workable.getAllLogs());

        if (plank_gold != -1) {
            // set the players gold currently inside inventory
            debug("Start gold = " + plank_gold);
        }

        // fetch all logs inside the player's inventory
        final RSItem[] logs = Workable.getAllLogs();

        // find the plank option such as oak plank
        final String plank_option = calculatePlankOption(logs);

        if (plank_option != null) {
            // if the plank interface is open, then click plank option
            final boolean first_click_result = clickPlankOption(plank_option);
            // if failed to click plank option, then proceed to run
            if (!first_click_result) {
                // open plank interface
                debug("Utilizing sawmill operator");
                if (openBuyPlankInterface()) {
                    General.sleep(1200, 1400);
                    // now select the plank option
                    final boolean final_click_result = clickPlankOption(plank_option);
                    if (final_click_result) {
                        debug("Planking complete");
                    }
                }
            } else {
                debug("Planking complete!");
            }
            General.sleep(1000,3000);
            //
            final int plank_gold_end = Plank.calculateOakPlankGold(Workable.getAllLogs());
            // if their are oak logs leftover check the plank gold
            if (plank_gold_end != -1 && plank_gold != -1) {
                // set the amount of gold that wasnt spent
                Gold.setGoldSpentTotal(plank_gold - plank_gold_end);
            } else {
                Gold.setGoldSpentTotal(plank_gold);
            }

            debug("Gold spent = " + Gold.getGoldSpentTotal());
        } else {
            debug("Planking incomplete");
        }
    }

    @Override
    public boolean validate(Task task) {
        return task.shouldPlankThenBank()
                && shouldMakePlank(task);
    }

    @Override
    public void debug(String status) {
        Globals.setState(status);
        General.println("[Plank Control] ".concat(status));
    }

    /**
     * Method determines if player can actually make oak planks.
     *
     * @param logs The logs to work with, oak logs specifically.
     * @return True if the player has the correct amount of inventory gold to make oak planks
     * equal to the amount of oak logs in the inventory, otherwise; false.
     */
    public static boolean canActuallyMakeOakPlank(RSItem[] logs) {
        // no logs, or oak logs, or logs null, leave now
        if (logs == null || logs.length == 0) {
            return false;
        }

        final int plank_gold = calculateOakPlankGold(logs);

        RSItem[] goldArray = Workable.getAllGold();

        int currentInventoryGold = 0;

        if (goldArray.length > 0) {
            currentInventoryGold = goldArray[0].getStack();
        }

        return plank_gold != -1 && currentInventoryGold >= Workable.OAK_FEE;
    }

    /**
     * Method calculates how many oak planks could be made
     * Multiply factor of 250 by oak log inventory count.
     *
     * @param logs To perform stream manipulation.
     * @return The unsigned integer of the gold required to make oak planks based on inventory count of logs;
     * otherwise return -1 if the inventory doesn't contain any logs or the logs aren't type Oak or logs null.
     */
    public static int calculateOakPlankGold(RSItem[] logs) {
        // no logs or logs null, leave now return -1
        if (logs == null || logs.length == 0) {
            return -1;
        }

        final boolean is_oak_log = Arrays
                .stream(logs)
                .anyMatch(rsItem -> rsItem
                        .getDefinition()
                        .getName()
                        .toLowerCase()
                        .contains("oak"));

        // no oak logs inside the inventory, leave now return -1
        if (!is_oak_log) {
            return -1;
        }

        // return the all oak logs in the inventory and multiply the count by 250
        return (int) (Arrays.stream(logs)
                .filter(rsItem -> rsItem
                        .getDefinition()
                        .getName()
                        .toLowerCase()
                        .contains("oak")).count() * Workable.OAK_FEE);
    }

    public static boolean isAtSawmill() {
        final RSNPC[] sawmill_npcs = NPCs.findNearest(sawmill_operator_npc_filter());

        if (sawmill_npcs.length == 0) {
            return false;
        }

        final RSNPC sawmill_NPC = sawmill_npcs[0];

        return Player.getPosition().distanceTo(sawmill_NPC) < 7;
    }

    private boolean openBuyPlankInterface() {
        return NPCInteraction.clickNpc(sawmill_operator_npc_filter(), "Buy-plank");
    }

    private String calculatePlankOption(RSItem[] logs) {
        String option = null;

        if (Arrays.stream(logs)
                .anyMatch(rsItem -> rsItem.getDefinition().getName().toLowerCase().contains("oak"))) {
            option = "Oak - 250gp";
        }

        return option;
    }

    private boolean clickPlankOption(String option) {
        final RSInterfaceMaster master_interface = Interfaces.get(270);

        boolean clickResult = false;

        if (master_interface != null) {
            final Optional<RSInterfaceChild> fletch_option_interface = Arrays.stream(master_interface.getChildren())
                    .filter(rsInterfaceChild -> rsInterfaceChild.getComponentName().contains(option))
                    .findFirst();

            if (fletch_option_interface.isPresent()) {
                clickResult = fletch_option_interface
                        .map(rsInterfaceChild -> rsInterfaceChild.click("Make"))
                        .orElse(false);
            }
        }

        return clickResult;
    }

    private boolean shouldMakePlank(Task task) {
        // goldSpent has to be less than or equal to playerChoiceGold for the plank node to execute
        // if using goldPerTask.
        // once the goldSpent has reached the actual gold limit, then the task is complete, can't make planks.
        // if using all gold then doesn't matter if goldSpent has reached the limited.
        if (task.shouldPlankThenBank() && Inventory.isFull()) {
            if (isAtSawmill() && Workable.inventoryContainsGold()) {
                return !task.isValidated() && canActuallyMakeOakPlank(Workable.getAllLogs());
            }
        }
        return false;
    }
}
