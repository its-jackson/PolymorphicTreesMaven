package scripts.nodes.woodcutting;

import org.tribot.api2007.Player;
import scripts.api.Node;
import org.tribot.api.General;
import org.tribot.api2007.GroundItems;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.types.RSGroundItem;
import scripts.api.Globals;
import scripts.api.Task;
import scripts.api.Workable;
import scripts.api.antiban.AntiBan;
import scripts.dax_api.walker_engine.interaction_handling.InteractionHelper;

/**
 * Purpose of class: Pick up the ground item "Bird nest".
 * Author: Jackson Johnson (Polymorphic~TRiBot)
 * Date: Aug 30th, 2020
 *
 *
 */

public class BirdNest extends Node {

    private static final  String[] BIRD_NEST_NAMES = {
            "Bird nest",
            "Clue nest (beginner)",
            "Clue nest (easy)",
            "Clue nest (medium)",
            "Clue nest (hard)",
            "Clue nest (elite)"
    };

    @Override
    public void execute(Task task) {
        debug("Sleeping " + Workable.sleep(Globals.getWaitTimes(), AntiBan.getHumanFatigue()));

        debug("Bird nest found");

        final RSGroundItem[] bird_nests = GroundItems.findNearest(BIRD_NEST_NAMES);

        if (bird_nests.length > 0) {
            final RSGroundItem bird_nest = bird_nests[0];

            if (bird_nest != null && Player.getPosition().distanceTo(bird_nest) < 5) {
                boolean result = InteractionHelper.focusCamera(bird_nest);

                if (InteractionHelper.click(bird_nest, "Take")) {
                    final String bird_nest_name = bird_nest.getDefinition().getName().toLowerCase();
                    debug("Taking " + bird_nest_name);
                }
            }
        }
    }

    @Override
    public boolean validate(Task task) {
        return task.shouldPickupNest() && !Inventory.isFull() && exists();
    }

    @Override
    public void debug(String status) {
        String format = ("[Nest Control] ");
        Globals.setState(status);
        General.println(format.concat(status));
    }

    public static boolean exists() {
        return GroundItems.findNearest(BIRD_NEST_NAMES).length > 0;
    }
}


