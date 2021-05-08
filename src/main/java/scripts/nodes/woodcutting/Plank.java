package scripts.nodes.woodcutting;

import org.tribot.api.General;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.*;
import scripts.api.*;
import scripts.dax_api.walker_engine.interaction_handling.NPCInteraction;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

public class Plank extends Node {
    // cache the player's amount of gold before planking, currently inside the inventory
    private int playerStartGold;

    // cache the player's amount of gold after planking, currently inside the inventory
    private int playerEndGold;

    // cache the player's gold spent per planking trip
    private int playerGoldSpent;

    // filter for finding any sawmill operator
    private static Predicate<RSNPC> sawmill_operator_npc_filter() {
        return rsnpc -> rsnpc.getName().toLowerCase().contains("sawmill operator");
    }

    private final Walk walk_node = new Walk();

    @Override
    public void execute(Task task) {
        Workable.sleep(Globals.waitTimes, Globals.humanFatigue);

        // set the players gold currently inside inventory
        setPlayerStartGold(Workable.getAllGold()[0].getDefinition().getValue());

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
                debug("Planking complete");
            }

            // validate first, gold might be zero from buying planks previously.
                // set the players end gold after buying planks.
            if (Workable.getAllGold().length > 0 ) {
                // end gold
                setPlayerEndGold(Workable.getAllGold()[0].getDefinition().getValue());
                // actual gold difference afterward
                    // could be zero if failed planking. because startGold = endGold.
                setPlayerGoldSpent(getPlayerStartGold() - getPlayerEndGold());
            } else {
                // no gold inside inventory after buying planks, must of spent all the remaining gold
                setPlayerGoldSpent(getPlayerStartGold());
                setPlayerEndGold(0);
            }

            if (getPlayerGoldSpent() > 0) {
                Gold.setGoldSpentTotal(getPlayerGoldSpent());
            }
        } else {
            debug("Couldn't plank. No oak logs");
        }

    }

    @Override
    public boolean validate(Task task) {
        return task.shouldPlankThenBank()
                && shouldMakePlank(task);
    }

    @Override
    public void debug(String status) {
        Globals.STATE = (status);
        General.println("[Plank Control] ".concat(status));
    }

    public static boolean isAtSawmill() {
        final RSNPC[] sawmill_NPCS = NPCs.findNearest(sawmill_operator_npc_filter());

        if (sawmill_NPCS.length == 0 ) {
            return false;
        }

        final RSNPC sawmill_NPC = sawmill_NPCS[0];

        return Player.getPosition().distanceTo(sawmill_NPC) < 7;
    }

    private boolean openBuyPlankInterface() {
        return NPCInteraction.clickNpc(sawmill_operator_npc_filter(), "Buy-plank");
    }

    private String calculatePlankOption(RSItem[] logs) {
        String option = null;

        if (Arrays
                .stream(logs)
                .anyMatch(rsItem -> rsItem.getDefinition().getName()
                .toLowerCase()
                .contains("oak"))
        ) {
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

        //final int player_gold_limit = Gold.calculateActualGoldRegex(Gold.getGoldRegex());

        //final int player_gold_spent = Gold.getGoldSpentTotal();

        if (task.shouldPlankThenBank() && Inventory.isFull()) {
            if (isAtSawmill() && Workable.inventoryContainsGold()) {
                final RSItem inventory_gold = Workable.getAllGold()[0];
                final int gold_value = inventory_gold.getDefinition().getValue();
                // additional validation if using specific amount of gold per task
                // additional validation if use all of the player's gold until dissipated.
                return gold_value >= 250 && !task.isValidated();
            }
        }
        return false;
    }

    public int getPlayerStartGold() {
        return playerStartGold;
    }

    public void setPlayerStartGold(int playerStartGold) {
        this.playerStartGold = playerStartGold;
    }

    public int getPlayerEndGold() {
        return playerEndGold;
    }

    public void setPlayerEndGold(int playerEndGold) {
        this.playerEndGold = playerEndGold;
    }

    public int getPlayerGoldSpent() {
        return playerGoldSpent;
    }

    public void setPlayerGoldSpent(int playerGoldSpent) {
        this.playerGoldSpent = playerGoldSpent;
    }
}
