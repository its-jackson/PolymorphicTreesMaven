package scripts.dax_api.shared.helpers;

import org.tribot.api.interfaces.Positionable;
import org.tribot.api.util.abc.preferences.OpenBankPreference;
import org.tribot.api2007.*;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import scripts.api.antiban.AntiBan;
import scripts.dax_api.walker_engine.WaitFor;
import scripts.dax_api.walker_engine.interaction_handling.InteractionHelper;

import java.util.HashSet;
import java.util.function.Predicate;

public class BankHelper {
    private static final Predicate<RSObject> BANK_OBJECT_FILTER = Filters.Objects.nameContains("bank", "Bank", "Exchange" +
                    " booth", "chest").and(Filters.Objects.actionsContains("Bank", "Open bank", "Use", "deposit", "Deposit"));

    private static final Predicate<RSNPC> BANKER_NPC_FILTER = Filters.NPCs.nameContains("bank", "Bank");

    public static boolean isInBank() {
        return isInBank(Player.getPosition());
    }

    public static boolean isInBank(Positionable positionable) {
        if (isInNPCBank(positionable)) {
            return true;
        }

        RSObject[] bankObjects = Objects.findNearest(15, BANK_OBJECT_FILTER);

        if (bankObjects.length == 0) {
            return false;
        }

        RSObject bankObject = bankObjects[0];

        return positionable.getPosition().distanceTo(bankObject) < 7;
    }

    private static boolean isInNPCBank(Positionable positionable) {
        RSNPC[] bankNpcs = NPCs.findNearest(BANKER_NPC_FILTER);

        if (bankNpcs.length == 0) {
            return false;
        }

        RSNPC bankerNpc = bankNpcs[0];

        return positionable.getPosition().distanceTo(bankerNpc) < 7;
    }

    /**
     * @return whether if the action succeeded
     */
    public static boolean openBank() {
        return Banking.isBankScreenOpen() || InteractionHelper.click(InteractionHelper.getRSObject(BANK_OBJECT_FILTER), "Bank", "Open bank", "Use", "deposit", "Deposit");
    }

    /**
     * @return bank screen is open
     */
    public static boolean openBankAndWait() {
        final OpenBankPreference open_bank_preference = AntiBan.generateOpenBankPreference();

        if (Banking.isBankScreenOpen() || Banking.isDepositBoxOpen()) {
            return true;
        }
        //RSObject object = InteractionHelper.getRSObject(BANK_OBJECT_FILTER);
        RSObject[] bankObjects = Objects.findNearest(15, BANK_OBJECT_FILTER);
        RSNPC[] bankNpcs = NPCs.findNearest(BANKER_NPC_FILTER);

        if (bankObjects.length == 0 && bankNpcs.length == 0) {
            return false;
        }

        RSObject bankObject = AntiBan.selectNextTarget(bankObjects);

        RSNPC bankNpc = AntiBan.selectNextTarget(bankNpcs);

        if (bankNpc != null && PathFinding.canReach(bankNpc.getPosition(), true)) {
            if (open_bank_preference.equals(OpenBankPreference.BANKER)) {
                InteractionHelper.focusCamera(bankNpc);
                return InteractionHelper.click(bankNpc, "Bank", "Open bank") && waitForBankScreen(bankNpc);
            }
        }

        InteractionHelper.focusCamera(bankObject);

        return InteractionHelper.click(bankObject, "Bank", "Open bank", "Use", "deposit", "Deposit") && waitForBankScreen(bankObject);
    }

    public static HashSet<RSTile> getBuilding(Positionable positionable) {
        return computeBuilding(positionable, Game.getSceneFlags(), new HashSet<>());
    }

    private static HashSet<RSTile> computeBuilding(Positionable positionable, byte[][][] sceneFlags, HashSet<RSTile> tiles) {
        try {
            RSTile local = positionable.getPosition().toLocalTile();
            int localX = local.getX(), localY = local.getY(), localZ = local.getPlane();
            if (localX < 0 || localY < 0 || localZ < 0) {
                return tiles;
            }
            if (sceneFlags.length <= localZ || sceneFlags[localZ].length <= localX || sceneFlags[localZ][localX].length <= localY) { //Not within bounds
                return tiles;
            }
            if (sceneFlags[localZ][localX][localY] < 4) { //Not a building
                return tiles;
            }
            if (!tiles.add(local.toWorldTile())) { //Already computed
                return tiles;
            }
            computeBuilding(new RSTile(localX, localY + 1, localZ, RSTile.TYPES.LOCAL).toWorldTile(), sceneFlags, tiles);
            computeBuilding(new RSTile(localX + 1, localY, localZ, RSTile.TYPES.LOCAL).toWorldTile(), sceneFlags, tiles);
            computeBuilding(new RSTile(localX, localY - 1, localZ, RSTile.TYPES.LOCAL).toWorldTile(), sceneFlags, tiles);
            computeBuilding(new RSTile(localX - 1, localY, localZ, RSTile.TYPES.LOCAL).toWorldTile(), sceneFlags, tiles);
        } catch (ArrayIndexOutOfBoundsException e) {

        }
        return tiles;
    }

    private static boolean isInBuilding(RSTile localRSTile, byte[][][] sceneFlags) {
        return !(sceneFlags.length <= localRSTile.getPlane()
                || sceneFlags[localRSTile.getPlane()].length <= localRSTile.getX()
                || sceneFlags[localRSTile.getPlane()][localRSTile.getX()].length <= localRSTile.getY())
                && sceneFlags[localRSTile.getPlane()][localRSTile.getX()][localRSTile.getY()] >= 4;
    }

    private static boolean waitForBankScreen(RSObject object) {
        return WaitFor.condition(WaitFor.getMovementRandomSleep(object),
                ((WaitFor.Condition) () -> Banking.isBankScreenOpen() || Banking.isDepositBoxOpen() ? WaitFor.Return.SUCCESS :
                        WaitFor.Return.IGNORE).combine(WaitFor.getNotMovingCondition())) == WaitFor.Return.SUCCESS;
    }

    private static boolean waitForBankScreen(RSNPC npc) {
        return WaitFor.condition(WaitFor.getMovementRandomSleep(npc),
                ((WaitFor.Condition) () -> Banking.isBankScreenOpen() || Banking.isDepositBoxOpen() ? WaitFor.Return.SUCCESS :
                        WaitFor.Return.IGNORE).combine(WaitFor.getNotMovingCondition())) == WaitFor.Return.SUCCESS;
    }

}
