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
 */

public class SpecialAttack extends Node {

    @Override
    public void execute(Task task) {
        debug("Sleeping " + Workable.sleep(Globals.getWaitTimes(), AntiBan.getHumanFatigue()));

        final boolean is_resizable = General.isClientResizable();
        debug("Client resizable " + is_resizable);

        final SpecialAttackPreference special_attack_preference = AntiBan.generateSpecialAttackPreference();
        debug("Special preference " + special_attack_preference.toString().toLowerCase());

        final RSInterfaceChild special_box = generateSpecialBox(special_attack_preference);

        if (special_box != null) {
            debug("Special attack boosting");
            final boolean result = performSpecialAttack(special_attack_preference, new Rectangle(special_box.getAbsoluteBounds()));
            if (result) {
                Timing.waitCondition(() -> !Workable.isWorking(), General.random(1200, 2000));
                Timing.waitCondition(Workable::isWorking, General.random(2200, 3300));
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
            case MAIN_HUD -> {
                Mouse.moveBox(rectangle);
                General.sleep(200, 400);
                Mouse.click(0);
                General.sleep(200, 400);
                return true;
            }
            case COMBAT_TAB -> {
                openCombatTab();
                General.sleep(400, 800);
                Mouse.moveBox(rectangle);
                General.sleep(200, 400);
                Mouse.click(0);
                General.sleep(200, 400);
                return true;
            }
        }
        return false;
    }

    private boolean openCombatTab() {
        if (GameTab.getOpen().equals(GameTab.TABS.COMBAT)) {
            return false;
        }
        return GameTab.open(GameTab.TABS.COMBAT);
    }

    private static boolean shouldUtilize(Task task) {
        return Workable.getSpecialAttack() == 100
                && !Inventory.isFull()
                && Workable.isSpecialAxeEquipped()
                && Workable.nearObjects(Globals.getTreeFactor(), task.getTree())
                && Workable.isInLocation(task, Player.getRSPlayer())
                && Inventory.getAll().length < 26;
    }

}
