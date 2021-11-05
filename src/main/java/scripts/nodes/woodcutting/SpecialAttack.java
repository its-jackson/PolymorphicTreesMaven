package scripts.nodes.woodcutting;

import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api2007.types.RSInterfaceChild;
import scripts.api.*;
import org.tribot.api.General;
import org.tribot.api2007.*;
import scripts.api.antiban.AntiBan;
import scripts.api.antiban.SpecialAttackPreference;

import java.awt.*;

/**
 * Purpose of class: Click axe special ability.
 * Author: Jackson (Polymorphic~TRiBot)
 * Date: Aug 30th, 2020
 *
 * Updated 11/04/2021 - Added null safe checks to all methods and cached all return values.
 *
 * Updated 11/05/2021 - Changed naming convention for final variables.
 */

public class SpecialAttack extends Node {

    @Override
    public void execute(Task task) {
        debug("Sleeping " + Workable.sleep(Globals.getWaitTimes(), AntiBan.getHumanFatigue()));

        final SpecialAttackPreference specialAttackPreference = AntiBan.generateSpecialAttackPreference();
        debug("Special preference " + specialAttackPreference.toString().toLowerCase());

        final RSInterfaceChild specialBox = generateSpecialBox(specialAttackPreference);

        if (specialBox != null) {
            debug("Special attack boosting");
            final boolean result = performSpecialAttack(specialAttackPreference, new Rectangle(specialBox.getAbsoluteBounds()));
            if (result) {
                debug("Clicked special attack");
                final boolean firstWaitResult = Timing.waitCondition(() -> !Workable.isWorking(), General.random(1200, 2000));
                if (firstWaitResult) {
                    debug("Player is not animating");
                }
                final boolean lastWaitResult = Timing.waitCondition(Workable::isWorking, General.random(3000, 4000));
                if (lastWaitResult) {
                    debug("Player is animating");
                }
            }
        }
    }

    @Override
    public boolean validate(Task task) {
        return shouldUtilize(task) && Progressive.isMember();
    }

    @Override
    public void debug(String status) {
        String format = ("[Special Control] ");
        Globals.setState(status);
        General.println(format.concat(status));
    }

    /**
     * Generate the actual interface to perform on
     * @param preference The special attack preference for determining the interface to generate
     * @return Special attack interface box
     */
    private RSInterfaceChild generateSpecialBox(SpecialAttackPreference preference) {
        return preference.equals(SpecialAttackPreference.COMBAT_TAB) ? Interfaces.get(593, 40) : Interfaces.get(160, 30);
    }

    /**
     * Click the special attack ability
     *
     * @param option Choose which method to execute for clicking special attack
     * @return True if we were successful at clicking the special attack; false otherwise
     */
    private boolean performSpecialAttack(SpecialAttackPreference option, Rectangle rectangle) {
        switch (option) {
            case MAIN_HUD : {
                Mouse.moveBox(rectangle);
                General.sleep(200, 400);
                Mouse.click(0);
                General.sleep(200, 400);
                return true;
            }
            case COMBAT_TAB : {
                if (openCombatTab()) {
                    General.sleep(400, 800);
                    Mouse.moveBox(rectangle);
                    General.sleep(200, 400);
                    Mouse.click(0);
                    General.sleep(200, 400);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean openCombatTab() {
        if (GameTab.getOpen().equals(GameTab.TABS.COMBAT)) {
            return true;
        }

        return GameTab.open(GameTab.TABS.COMBAT);
    }

    private static boolean shouldUtilize(Task task) {
        return Workable.getSpecialAttack() == 100
                && !Inventory.isFull()
                && Workable.isSpecialAxeEquipped()
                && Workable.nearObjects(Globals.getTreeFactor(), task.getTree())
                && Workable.isInLocation(task, Player.getRSPlayer())
                ;
    }

}
