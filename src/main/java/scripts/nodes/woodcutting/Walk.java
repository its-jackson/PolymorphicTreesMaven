package scripts.nodes.woodcutting;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.*;
import org.tribot.api2007.Objects;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSPlayer;
import org.tribot.api2007.types.RSTile;
import scripts.api.*;
import scripts.api.antiban.AntiBan;
import scripts.dax_api.shared.helpers.BankHelper;
import scripts.dax_api.walker_engine.interaction_handling.InteractionHelper;

import java.util.Arrays;

public class Walk extends Node {
    private boolean walkToBankController;
    private boolean walkToTreeController;
    private boolean walkToSawmillController;
    private boolean walkToWoodcuttingGuildAlternativeBankController;

    private final RSTile rope_ladder_tile_north = new RSTile(1575, 3493, 0);
    private final RSTile rope_ladder_tile_north_inside = new RSTile(1575, 3493, 1);

    private final RSTile rope_ladder_tile_south = new RSTile(1575, 3483, 0);
    private final RSTile rope_ladder_tile_south_inside = new RSTile(1575, 3483, 1);

    private final RSTile walking_tile_south = new RSTile(1576, 3483, 0);
    private final RSTile walking_tile_north = new RSTile(1576, 3493, 0);

    private final RSTile sawmill_operator_woodcutting_guild_location = new RSTile(1625, 3500, 0);

    @Override
    public void execute(Task task) {
        Workable.sleep(Globals.waitTimes, Globals.humanFatigue);

        getWalking(
                isWalkToBankController(),
                isWalkToTreeController(),
                isWalkToSawmillController(),
                isWalkToWoodcuttingGuildAlternativeBankController(),
                task
        );
    }

    @Override
    public boolean validate(Task task) {
        return shouldGetWalking(task);
    }

    @Override
    public void debug(String status) {
        String format = "[Walking Control] ";
        Globals.STATE = (status);
        General.println(format.concat(status));
    }

    private boolean shouldRetrieveKnife(Task task) {
        if (task.shouldFletchThenBank() || task.shouldFletchThenDrop()) {
            if (Workable.inventoryContainsKnife()) {
                return false;
            }
            if (!Workable.inventoryContainsKnife() && !BankHelper.isInBank()) {
                // walk to bank, get a knife
                setWalkToBankController(true);
                return true;
            }
        }

        return false;
    }

    private boolean shouldRetrieveGold(Task task) {
        if (task.shouldPlankThenBank()) {
            // walk to bank, get gold
            if (!Workable.inventoryContainsGold()) {
                if (!task.isValidated() && !BankHelper.isInBank()) {
                    // walk to bank, get gold now, no gold in the inventory, haven't surpassed the task gold.
                    setWalkToBankController(true);
                    return true;
                }
            }
        }

        return false;
    }

    private boolean shouldWalkToSawmillLocation(Task task) {
        // walk to sawmill
        if (task.shouldPlankThenBank() && Inventory.isFull()
                && !Plank.isAtSawmill()
                && Workable.inventoryContainsGold()
                && Workable.getAllLogs().length > 0) {
            long count = Arrays
                    .stream(Workable.getAllLogs())
                    .filter(rsItem -> rsItem.getDefinition().getName().toLowerCase().contains("oak"))
                    .count();
            if (count > 0) {
                if (!task.isValidated()) {
                    // walk to sawmill
                    setWalkToSawmillController(true);
                    return true;
                }
            }
        }

        return false;
    }

    // walk to bank alternative woodcutting bank deposit box--
    // if task should plank then bank
    // && if inventory is full
    // && player is not in alternative bank
    // && player has oak planks
    // && player has gold greater than or equal to 250
    // && no oak logs OR no oak logs equal to the amount of inventory gold that can be utilized
    // how this works
    // --- a method called calculatePlankGold() will multiply 250 by the amount of logs
    // which will determine how many planks could be made.
    // return value can determine the functionality:
    // is less than the inventory gold, cant make planks
    // is greater than or equal to the inventory gold, walk to alt bank
    private boolean shouldWalkToSawmillBankA(Task task) {
        if (!task.isValidated()) {
            if (task.shouldPlankThenBank() && !Bank.isInWoodcuttingGuildAlternativeBank()) {
                if (Inventory.isFull() && Workable.inventoryContainsGold()) {
                    if (Workable.getAllPlanks().length > 0 && Plank.calculateOakPlankGold(Workable.getAllLogs()) == -1) {
                        setWalkToWoodcuttingGuildAlternativeBankController(true);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean shouldWalkToBank(Task task) {
        if (Inventory.isFull()
                && !BankHelper.isInBank()
                && task.shouldFletchThenBank()) {
            // walk to bank
            setWalkToBankController(true);
            return true;
        }
        if ((Inventory.find("Arrow shaft").length > 0)
                && Inventory.isFull()
                && Workable.getAllLogs().length == 0
                && task.shouldFletchThenBank()) {
            // walk to bank
            setWalkToBankController(true);
            return true;
        }
        if (Inventory.isFull()
                && !BankHelper.isInBank()
                && task.shouldBank()) {
            // walk to bank
            setWalkToBankController(true);
            return true;
        }

        return false;
    }

    private boolean shouldWalkToTrees(Task task) {
        if (task.shouldPlankThenBank()) {
            if (!Inventory.isFull() && !Workable.isInLocation(task, Player.getRSPlayer()) && Workable.inventoryContainsGold() && !task.isValidated()) {
                // walk to tree area
                setWalkToTreeController(true);
                return true;
            }
        } else {
            if (!Inventory.isFull()
                    //&& !Workable.isWorking()
                    && !Workable.isInLocation(task, Player.getRSPlayer())) {
                // walk to tree area
                setWalkToTreeController(true);
                return true;
            }

            if (!Inventory.isFull()
                    && !Workable.isWorking()
                    && Workable.isInLocation(task, Player.getRSPlayer())
                    && !Workable.nearObjects(Globals.treeFactor, task.getTree())) {
                // walk to future tree
                setWalkToTreeController(true);
                return true;
            }
        }

        return false;
    }

    private boolean shouldGetWalking(Task task) {
        if (!Player.isMoving()) {
            // before doing anything, player must have an axe on their person;
            // else go get an axe.
            if (Workable.inventoryContainsAxe() || Workable.isAxeEquipped()) {
                // get a knife if suppose to utilize knife during task.
                if (shouldRetrieveKnife(task)) {
                    return true;
                }
                // get gold for task
                if (shouldRetrieveGold(task)) {
                    return true;
                }
                // walk to sawmill woodcutting guild if task says to
                if (shouldWalkToSawmillLocation(task)) {
                    return true;
                }
                // && !task.isValidated
                if (shouldWalkToSawmillBankA(task)) {
                    return true;
                }
                // walk to the bank
                if (shouldWalkToBank(task)) {
                    return true;
                }
                // walk to trees
                if (shouldWalkToTrees(task)) {
                    return true;
                }
            }
        }
        // no axe on player, and not in bank, - return to bank and fetch an axe
        return !Workable.inventoryContainsAxe()
                && !Workable.isAxeEquipped()
                && !BankHelper.isInBank();
    }

    private void getWalking(
            boolean walkToBankController,
            boolean walkToTreeController,
            boolean walkToSawmillController,
            boolean walkToWoodcuttingGuildAlternativeBankController,
            Task task) {
        switch (task.getLogOption().toLowerCase()) {
            default -> {
                if (walkToBankController) {
                    debug("Walking to bank");
                    walkToBank(task);
                } else if (walkToTreeController) {
                    debug("Walking to trees");
                    walkToTrees(task);
                } else if (walkToSawmillController) {
                    debug("Walking to sawmill");
                    walkToSawmill(task);
                } else if (walkToWoodcuttingGuildAlternativeBankController) {
                    debug("Walking alternative bank");
                    walkToWoodcuttingGuildAlternativeBank(task);
                } else {
                    debug("Retrieving axe");
                    walkToBank(task);
                }
            }
        }
        setWalkToBankController(false);
        setWalkToTreeController(false);
        setWalkToSawmillController(false);
        setWalkToWoodcuttingGuildAlternativeBankController(false);
    }

    private void walkToWoodcuttingGuildAlternativeBank(Task task) {
        final RSPlayer player = Player.getRSPlayer();
        final RSTile player_tile = player.getPosition();
        final int player_plane = player_tile.getPlane();

        switch (task.getActualLocation()) {
            case WOODCUTTING_GUILD_OAKS -> {
                switch (player_plane) {
                    case 0 -> walkToWoodcuttingGuildAlternativeBankLocation(Bank.getSawmillWoodcuttingGuildAlternativeBank());
                    case 1 -> {
                        final RSObject[] ladder_object = Objects.getAt(this.rope_ladder_tile_north_inside);
                        walkToRedwoodLadderInside(player_tile, ladder_object, rope_ladder_tile_north);
                    }
                    case 2 -> enterCaveNorthUpperLevel(player_tile, player_plane);
                }
            }
        }
    }

    private void walkToSawmill(Task task) {
        final RSPlayer player = Player.getRSPlayer();
        final RSTile player_tile = player.getPosition();
        final int player_plane = player_tile.getPlane();

        switch (task.getActualLocation()) {
            case WOODCUTTING_GUILD_OAKS -> {
                switch (player_plane) {
                    case 0 -> {
                        walkToSawmillLocation(this.sawmill_operator_woodcutting_guild_location);
                    }
                    case 1 -> {
                        final RSObject[] ladder_object = Objects.getAt(this.rope_ladder_tile_north_inside);
                        walkToRedwoodLadderInside(player_tile, ladder_object, rope_ladder_tile_north);
                    }
                    case 2 -> enterCaveNorthUpperLevel(player_tile, player_plane);
                }
            }
        }
    }

    private void walkToTrees(Task task) {
        final RSTile destination = task.getActualLocation().getRSArea().getRandomTile();
        final RSTile[] woodcutting_location_tiles = task.getActualLocation().getRSArea().getAllTiles();

        final RSPlayer player = Player.getRSPlayer();
        final RSTile player_tile = player.getPosition();
        final int player_plane = player_tile.getPlane();

        switch (task.getActualLocation()) {
            case TAR_SWAMP: {

            }
            break;
            case REDWOOD_SOUTH: {
                switch (player_plane) {
                    case 0: {
                        walkToRedwoodLadderOutside(this.walking_tile_south, this.rope_ladder_tile_south, player_tile);
                    }
                    break;
                    case 1: {
                        walkToNextObject(destination, woodcutting_location_tiles, player, task);
                    }
                    break;
                    case 2: {
                        // in case we are in plane 2
                        enterCaveSouthUpperLevel(player_tile, player_plane);
                    }
                }
            }
            break;
            case REDWOOD_NORTH: {
                switch (player_plane) {
                    case 0: {
                        walkToRedwoodLadderOutside(this.walking_tile_north, this.rope_ladder_tile_north, player_tile);
                    }
                    break;
                    case 1: {
                        walkToNextObject(destination, woodcutting_location_tiles, player, task);

                    }
                    break;
                    case 2: {
                        // in case we are in plane 2
                        enterCaveNorthUpperLevel(player_tile, player_plane);
                    }
                }
            }
            break;
            case REDWOOD_SOUTH_UPPER_LEVEL: {
                switch (player_plane) {
                    case 0: {
                        walkToRedwoodLadderOutside(this.walking_tile_south, this.rope_ladder_tile_south, player_tile);
                    }
                    break;
                    case 1: {
                        enterCaveSouthLowerLevel(player_tile);
                    }
                    break;
                    case 2: {
                        walkToNextObject(destination, woodcutting_location_tiles, player, task);
                    }
                }
            }
            break;
            case REDWOOD_NORTH_UPPER_LEVEL: {
                switch (player_plane) {
                    case 0: {
                        walkToRedwoodLadderOutside(this.walking_tile_north, this.rope_ladder_tile_north, player_tile);
                    }
                    break;
                    case 1: {
                        enterCaveNorthLowerLevel(player_tile);
                    }
                    break;
                    case 2: {
                        walkToNextObject(destination, woodcutting_location_tiles, player, task);
                    }
                }
            }
            break;
            case ISLE_OF_SOULS: {
                switch (player_plane) {
                    case 0: {
                        if (Location.ISLE_OF_SOULS_COMPLETE.getRSArea().contains(player_tile)) {
                            walkToNextObject(destination, woodcutting_location_tiles, player, task);
                        } else {
                            walkToSoulWarsPortalEdgeVille(player_tile);
                        }
                    }
                    break;
                    case 1: {
                        final RSObject[] ladder_object = Objects.getAt(this.rope_ladder_tile_north_inside);
                        walkToRedwoodLadderInside(player_tile, ladder_object, rope_ladder_tile_north);
                    }
                    break;
                    case 2: {
                        enterCaveNorthUpperLevel(player_tile, player_plane);
                    }
                    break;
                }
            }
            default: {
                switch (player_plane) {
                    case 0 -> walkToNextObject(destination, woodcutting_location_tiles, player, task);
                    case 1 -> {
                        final RSObject[] ladder_object = Objects.getAt(this.rope_ladder_tile_north_inside);
                        walkToRedwoodLadderInside(player_tile, ladder_object, rope_ladder_tile_north);
                    }
                    case 2 -> enterCaveNorthUpperLevel(player_tile, player_plane);
                }
            }
        }
    }

    private void walkToNextObject(
            RSTile destination,
            RSTile[] woodcutting_location_tiles,
            RSPlayer player,
            Task task) {
        if (Workable.objectsExist(woodcutting_location_tiles, task.getTree())) {
            // a method that returns a list of all tiles that trees belong to
            final RSTile[] tree_tiles = Workable.discoverTreeTiles(woodcutting_location_tiles, task.getTree());
            final RSTile next_tree = AntiBan.selectNextTarget(tree_tiles);
            // walk to next tree
            if (!Workable.walkToTile(next_tree)) {
                Workable.walkToTileA(next_tree, 5);
            }
        } else {
            // walk to working location if player isn't there or if no trees are in the location
            if (destination != null && !Workable.isInLocation(task, player)) {
                Workable.walkToTile(destination);
            }
        }
    }

    private void walkToSawmillLocation(RSTile walking_tile) {
        if (!Plank.isAtSawmill()) {
            if (!Workable.walkToTile(walking_tile)) {
                Workable.walkToTileA(walking_tile, 5);
            }
        }
    }

    private void walkToWoodcuttingGuildAlternativeBankLocation(RSTile walking_tile) {
        if (!Bank.isInWoodcuttingGuildAlternativeBank()) {
            if (!Workable.walkToTile(walking_tile)) {
                Workable.walkToTileA(walking_tile, 5);
            }
        }
    }

    private void walkToRedwoodLadderOutside(RSTile walking_tile, RSTile rope_ladder_tile, RSTile player_tile) {
        if (player_tile.distanceTo(walking_tile) > 5) {
            if (!Workable.walkToTile(walking_tile)) {
                Workable.walkToTileA(walking_tile, 5);
            }
        }

        RSObject[] ladderObject = Objects.getAt(rope_ladder_tile);

        if (ladderObject.length > 0 && ladderObject[0].isOnScreen()) {
            if (!InteractionHelper.click(ladderObject[0], "Climb-up")) {
                InteractionHelper.focusCamera(ladderObject[0]);
            }
        }
    }

    private void walkToBank(Task task) {
        final RSPlayer player = Player.getRSPlayer();
        final RSTile player_tile = player.getPosition();
        final int player_plane = player_tile.getPlane();

        switch (task.getActualLocation()) {
            case REDWOOD_SOUTH, REDWOOD_SOUTH_UPPER_LEVEL -> {
                switch (player_plane) {
                    case 0 -> {
                        if (!Workable.walkToBank(task.getBankLocation())) {
                            Workable.walkToTileA(task.getBankLocation().getPosition(), 15);
                        }
                    }
                    case 1 -> {
                        final RSObject[] ladder_object = Objects.getAt(this.rope_ladder_tile_south_inside);
                        walkToRedwoodLadderInside(player_tile, ladder_object, rope_ladder_tile_south);
                    }
                    case 2 -> // call this in case of miss click while cutting below plane 2
                            enterCaveSouthUpperLevel(player_tile, player_plane);
                }
            }
            case REDWOOD_NORTH, REDWOOD_NORTH_UPPER_LEVEL -> {
                switch (player_plane) {
                    case 0 -> {
                        if (!Workable.walkToBank(task.getBankLocation())) {
                            Workable.walkToTileA(task.getBankLocation().getPosition(), 15);
                        }
                    }
                    case 1 -> {
                        final RSObject[] ladder_object = Objects.getAt(this.rope_ladder_tile_north_inside);
                        walkToRedwoodLadderInside(player_tile, ladder_object, rope_ladder_tile_north);
                    }
                    case 2 -> {
                        enterCaveNorthUpperLevel(player_tile, player_plane);
                    }
                }
            }
            default -> {
                switch (player_plane) {
                    case 0 -> {
                        if (!Workable.walkToBank(task.getBankLocation())) {
                            Workable.walkToTileA(task.getBankLocation().getPosition(), 5);
                        }
                    }
                    case 1 -> {
                        final RSObject[] ladder_object = Objects.getAt(this.rope_ladder_tile_north_inside);
                        walkToRedwoodLadderInside(player_tile, ladder_object, rope_ladder_tile_north);
                    }
                    case 2 -> {
                        enterCaveNorthUpperLevel(player_tile, player_plane);
                    }
                }
            }
        }
    }

    private void enterCaveSouthUpperLevel(RSTile playerTile, int playerPlane) {
        if (playerPlane == 2) {
            final RSTile cave_tile_south_upper_level = new RSTile(1570, 3484, 2);
            RSObject[] caveObjectSouth = Objects.getAt(cave_tile_south_upper_level);

            if (caveObjectSouth.length > 0) {
                final boolean camera_result = InteractionHelper.focusCamera(caveObjectSouth[0]);

                if (InteractionHelper.click(caveObjectSouth[0], "Enter")) {
                    Timing.waitCondition(() -> {
                        final RSTile cave_entrance_plane_1_0 = new RSTile(1571, 3486, 1);
                        final RSTile cave_entrance_plane_1_1 = new RSTile(1570, 3486, 1);
                        return playerTile.equals(cave_entrance_plane_1_0) || playerTile.equals(cave_entrance_plane_1_1);
                    }, General.random(4000, 5000));
                }
            }
        }
    }

    private void enterCaveNorthUpperLevel(RSTile playerTile, int playerPlane) {
        if (playerPlane == 2) {
            final RSTile cave_tile_north_upper_level = new RSTile(1570, 3490, 2);
            RSObject[] caveObjectNorth = Objects.getAt(cave_tile_north_upper_level);

            if (caveObjectNorth.length > 0) {
                final boolean camera_result = InteractionHelper.focusCamera(caveObjectNorth[0]);

                if (InteractionHelper.click(caveObjectNorth[0], "Enter")) {
                    Timing.waitCondition(() -> {
                        final RSTile cave_entrance_plane_2_0 = new RSTile(1571, 3489, 1);
                        final RSTile cave_entrance_plane_2_1 = new RSTile(1570, 3489, 1);
                        return playerTile.equals(cave_entrance_plane_2_0) || playerTile.equals(cave_entrance_plane_2_1);
                    }, General.random(4000, 5000));
                }
            }
        }
    }

    private void enterCaveSouthLowerLevel(RSTile playerTile) {
        final RSTile cave_tile_south_lower_level = new RSTile(1570, 3484, 1);
        RSObject[] caveObjectSouth = Objects.getAt(cave_tile_south_lower_level);

        if (caveObjectSouth.length > 0) {
            final boolean camera_result = InteractionHelper.focusCamera(caveObjectSouth[0]);

            if (InteractionHelper.click(caveObjectSouth[0], "Enter")) {
                Timing.waitCondition(() -> {
                    final RSTile cave_entrance_plane_1_0 = new RSTile(1571, 3486, 2);
                    final RSTile cave_entrance_plane_1_1 = new RSTile(1570, 3486, 2);
                    return playerTile.equals(cave_entrance_plane_1_0) || playerTile.equals(cave_entrance_plane_1_1);
                }, General.random(4000, 8000));
            }
        }
    }

    private void enterCaveNorthLowerLevel(RSTile playerTile) {
        final RSTile cave_tile_north_lower_level = new RSTile(1570, 3490, 1);

        RSObject[] caveObjectNorth = Objects.getAt(cave_tile_north_lower_level);

        if (caveObjectNorth.length > 0) {
            final boolean camera_result = InteractionHelper.focusCamera(caveObjectNorth[0]);

            if (InteractionHelper.click(caveObjectNorth[0], "Enter")) {
                Timing.waitCondition(() -> {
                    final RSTile cave_entrance_plane_1_0 = new RSTile(1571, 3489, 2);
                    final RSTile cave_entrance_plane_1_1 = new RSTile(1570, 3489, 2);
                    return playerTile.equals(cave_entrance_plane_1_0) || playerTile.equals(cave_entrance_plane_1_1);
                }, General.random(4000, 8000));
            }

        }
    }

    private void walkToRedwoodLadderInside(RSTile playerTile, RSObject[] ladder_object, RSTile ropeTile) {
        if (ladder_object.length > 0) {
            InteractionHelper.focusCamera(ladder_object[0]);
            if (InteractionHelper.click(ladder_object[0], "Climb-down")) {
                Timing.waitCondition(() -> playerTile == ropeTile, General.random(4000, 5000));
            }
        }
    }

    private void walkToSoulWarsPortalEdgeVille(RSTile playerTile) {
        RSTile soul_wars_portal_tile = new RSTile(3082, 3473, 0);

        if (playerTile.distanceTo(soul_wars_portal_tile) > 5) {
            Workable.walkToTile(soul_wars_portal_tile);
        }

        RSObject[] portals = Objects.getAt(soul_wars_portal_tile);

        if (portals.length > 0) {
            if (portals[0].isOnScreen() && portals[0].isClickable()) {
                if (!DynamicClicking.clickRSObject(portals[0], "Enter")) {
                    portals[0].adjustCameraTo();
                }
            } else {
                int optimal = Camera.getOptimalAngleForPositionable(portals[0]);
                Camera.setRotationMethod(Camera.getRotationMethod());
                Camera.setCameraRotation(Camera.getCameraRotation());
                Camera.setCameraAngle(optimal);
            }
        }
    }

    public boolean isWalkToBankController() {
        return walkToBankController;
    }

    public void setWalkToBankController(boolean walkToBankController) {
        this.walkToBankController = walkToBankController;
    }

    public boolean isWalkToTreeController() {
        return walkToTreeController;
    }

    public void setWalkToTreeController(boolean walkToTreeController) {
        this.walkToTreeController = walkToTreeController;
    }

    public boolean isWalkToSawmillController() {
        return walkToSawmillController;
    }

    public void setWalkToSawmillController(boolean walkToSawmillController) {
        this.walkToSawmillController = walkToSawmillController;
    }

    public boolean isWalkToWoodcuttingGuildAlternativeBankController() {
        return walkToWoodcuttingGuildAlternativeBankController;
    }

    public void setWalkToWoodcuttingGuildAlternativeBankController(boolean walkToWoodcuttingGuildAlternativeBankController) {
        this.walkToWoodcuttingGuildAlternativeBankController = walkToWoodcuttingGuildAlternativeBankController;
    }
}

