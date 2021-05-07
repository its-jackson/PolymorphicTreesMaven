package scripts.nodes.woodcutting;

import org.tribot.api.General;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.*;
import scripts.api.Globals;
import scripts.api.Node;
import scripts.api.Task;
import scripts.api.Workable;
import scripts.dax_api.walker_engine.interaction_handling.NPCInteraction;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

public class Plank extends Node {

    private static Predicate<RSNPC> sawmill_operator_npc_filter() {
        return rsnpc -> rsnpc.getName().toLowerCase().contains("sawmill operator");
    }

    @Override
    public void execute(Task task) {
        Workable.sleep(Globals.waitTimes, Globals.humanFatigue);

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
        }
    }

    @Override
    public boolean validate(Task task) {
        return task.shouldPlankThenBank()
                && Inventory.isFull()
                && isAtSawmill()
                && Workable.inventoryContainsGold();
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

        if (Arrays.stream(logs)
                .anyMatch(rsItem -> rsItem.getDefinition().getName()
                .toLowerCase()
                .contains("oak"))) {
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
        int playerGoldChoice = Gold.calculateActualGold(Gold.gold);

        if (task.shouldPlankThenBank() && Inventory.isFull()) {
            if (isAtSawmill() && Workable.inventoryContainsGold() && !Globals.useAllGold) {
                final RSItem inventory_gold = Workable.getAllGold()[0];
                final int gold_value = inventory_gold.getDefinition().getValue();
                if (gold_value >= 250 && gold_value <= playerGoldChoice) {
                    return true;
                }
            }
        }
        return false;
    }

}
