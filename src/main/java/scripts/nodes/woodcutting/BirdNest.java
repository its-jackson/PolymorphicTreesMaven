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
import scripts.dax_api.walker_engine.interaction_handling.InteractionHelper;

/**
 * Purpose of class: Pick up the ground item "Bird nest".
 * Author: Jackson Johnson (Polymorphic~TRiBot)
 * Date: Aug 30th, 2020
 */

public class BirdNest extends Node {
    final static private String[] bird_nest_names = {
            "Bird nest",
            "Clue nest (beginner)",
            "Clue nest (easy)",
            "Clue nest (medium)",
            "Clue nest (hard)",
            "Clue nest (elite)"
    };

    @Override
    public void execute(Task task) {
        Workable.sleep(Globals.waitTimes, Globals.humanFatigue);

        debug("Bird nest found");

        final RSGroundItem[] bird_nests = GroundItems.findNearest(bird_nest_names);

        if (bird_nests.length > 0) {
            final RSGroundItem bird_nest = bird_nests[0];

            if (bird_nest != null && Player.getPosition().distanceTo(bird_nest) < 5) {
                boolean result = InteractionHelper.focusCamera(bird_nest);

                if (InteractionHelper.click(bird_nest, "Take")) {
                    final String bird_nest_name = bird_nest.getDefinition().getName().toLowerCase();
                    debug("Taking " + bird_nest_name);
                    Globals.birdNestCount++;
                }
            }
        }
    }

    @Override
    public boolean validate(Task task) {
        return !Inventory.isFull()
                && Globals.objectsNear != null
                && Globals.objectsNear.length > 0
                && exists()
                && task.shouldPickupNest();
    }

    @Override
    public void debug(String status) {
        String format = ("[Nest Control] ");
        Globals.STATE = (status);
        General.println(format.concat(status));
    }

    public static boolean exists() {
        return GroundItems.findNearest(bird_nest_names).length > 0;
    }

}


