package scripts.nodes.woodcutting;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.*;
import org.tribot.api2007.Objects;
import org.tribot.api2007.types.*;
import scripts.api.*;
import scripts.api.antiban.AntiBan;
import scripts.dax_api.shared.helpers.BankHelper;
import scripts.dax_api.walker_engine.interaction_handling.InteractionHelper;

import java.util.Arrays;

/**
 * Purpose of class: Control the walking behaviour, dependable on active task.
 *
 * Updated 11/05/2021 - Added null safe checks to all methods and cached all return values.
 *                    - Changed naming convention for final variables.
 *                    - Changed sawmill and redwood tiles to static and final constants.
 */

public class Walk extends Node {

    private boolean walkToBankController;
    private boolean walkToTreeController;
    private boolean walkToSawmillController;
    private boolean walkToWoodcuttingGuildAlternativeBankController;

    private static final RSTile rope_ladder_tile_north = new RSTile(1575, 3493, 0);
    private static final RSTile rope_ladder_tile_north_inside = new RSTile(1575, 3493, 1);

    private static final RSTile rope_ladder_tile_south = new RSTile(1575, 3483, 0);
    private static final RSTile rope_ladder_tile_south_inside = new RSTile(1575, 3483, 1);

    private static final RSTile walking_tile_south = new RSTile(1576, 3483, 0);
    private static final RSTile walking_tile_north = new RSTile(1576, 3493, 0);

    private static final RSTile sawmill_operator_woodcutting_guild_location = new RSTile(1625, 3500, 0);

    @Override
    public void execute(Task task) {
        AntiBan.sleep(Globals.getWaitTimes(), AntiBan.getHumanFatigue());

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
        Globals.setState(status);
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
                if (!BankHelper.isInBank()) {
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

            // get all the oak logs inside the player's inventory
            final boolean hasOakLog = Arrays.stream(Workable.getAllLogs())
                    .map(RSItem::getDefinition) // map to the definition value
                    .filter(java.util.Objects::nonNull) // filter the null values
                    .map(RSItemDefinition::getName) // map the definitions name value
                    .anyMatch(rsItemName -> rsItemName.toLowerCase().contains("oak")); // find any oak logs, cache bool

            if (hasOakLog) {
                setWalkToSawmillController(true);
                return true;
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
    private boolean shouldWalkToSawmillBankAlternative(Task task) {
        if (task.shouldPlankThenBank() && !Bank.isInWoodcuttingGuildAlternativeBank()) {
            if (Inventory.isFull() && Workable.inventoryContainsGold()) {
                if (Workable.getAllPlanks().length > 0 && Plank.calculateOakPlankGold(Workable.getAllLogs()) == -1) {
                    setWalkToWoodcuttingGuildAlternativeBankController(true);
                    return true;
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
            if (!Inventory.isFull() && !Workable.isInLocation(task, Player.getRSPlayer()) && Workable.inventoryContainsGold()) {
                // walk to tree area
                setWalkToTreeController(true);
                return true;
            }
        } else {
            if (!Inventory.isFull() && !Workable.isInLocation(task, Player.getRSPlayer())) {
                // walk to tree area
                setWalkToTreeController(true);
                return true;
            }
            if (!Inventory.isFull()
                    && !Workable.isWorking()
                    && Workable.isInLocation(task, Player.getRSPlayer())
                    && !Workable.nearObjects(Globals.getTreeFactor(), task.getTree())) {
                // walk to future tree
                setWalkToTreeController(true);
                return true;
            }
        }

        return false;
    }

    private boolean shouldGetWalking(Task task) {
        if (!Player.isMoving() && !task.isValidated()) {
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
                if (shouldWalkToSawmillBankAlternative(task)) {
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
        return !Workable.inventoryContainsAxe() && !Workable.isAxeEquipped() && !BankHelper.isInBank();
    }

    private void getWalking(
            boolean walkToBankController,
            boolean walkToTreeController,
            boolean walkToSawmillController,
            boolean walkToWoodcuttingGuildAlternativeBankController,
            Task task
    ) {

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

        setWalkToBankController(false);
        setWalkToTreeController(false);
        setWalkToSawmillController(false);
        setWalkToWoodcuttingGuildAlternativeBankController(false);
    }

    private void walkToWoodcuttingGuildAlternativeBank(Task task) {
        final RSPlayer player = Player.getRSPlayer();
        final RSTile playerTile = player.getPosition();
        final int playerPlane = playerTile.getPlane();

        switch (task.getActualLocation()) {
            case WOODCUTTING_GUILD_OAKS: {
                switch (playerPlane) {
                    case 0:
                        walkToWoodcuttingGuildAlternativeBankLocation(Bank.getSawmillWoodcuttingGuildAlternativeBank());
                        break;
                    case 1: {
                        final RSObject[] ladder_object = Objects.getAt(rope_ladder_tile_north_inside);
                        walkToRedwoodLadderInside(playerTile, ladder_object, rope_ladder_tile_north);
                    }
                    break;
                    case 2:
                        enterCaveNorthUpperLevel(playerTile, playerPlane);
                        break;
                }
            }
        }
    }

    private void walkToSawmill(Task task) {
        final RSPlayer player = Player.getRSPlayer();
        final RSTile playerTile = player.getPosition();
        final int playerPlane = playerTile.getPlane();

        switch (task.getActualLocation()) {
            case WOODCUTTING_GUILD_OAKS: {
                switch (playerPlane) {
                    case 0: {
                        walkToSawmillLocation(sawmill_operator_woodcutting_guild_location);
                    }
                    break;
                    case 1: {
                        final RSObject[] ladder_object = Objects.getAt(rope_ladder_tile_north_inside);
                        walkToRedwoodLadderInside(playerTile, ladder_object, rope_ladder_tile_north);
                    }
                    break;
                    case 2: {
                        enterCaveNorthUpperLevel(playerTile, playerPlane);
                    }
                    break;
                }
            }
        }
    }

    private void walkToTrees(Task task) {
        final RSTile destination = task.getActualLocation().getRSArea().getRandomTile();
        final RSTile[] woodcuttingLocationTiles = task.getActualLocation().getRSArea().getAllTiles();

        final RSPlayer player = Player.getRSPlayer();
        final RSTile playerTile = player.getPosition();
        final int playerPlane = playerTile.getPlane();

        switch (task.getActualLocation()) {
            case TAR_SWAMP: {
                // TODO
            }
            break;
            case REDWOOD_SOUTH: {
                switch (playerPlane) {
                    case 0: {
                        walkToRedwoodLadderOutside(walking_tile_south, rope_ladder_tile_south, playerTile);
                    }
                    break;
                    case 1: {
                        walkToNextObject(destination, woodcuttingLocationTiles, player, task);
                    }
                    break;
                    case 2: {
                        // in case we are in plane 2
                        enterCaveSouthUpperLevel(playerTile, playerPlane);
                    }
                }
            }
            break;
            case REDWOOD_NORTH: {
                switch (playerPlane) {
                    case 0: {
                        walkToRedwoodLadderOutside(walking_tile_north, rope_ladder_tile_north, playerTile);
                    }
                    break;
                    case 1: {
                        walkToNextObject(destination, woodcuttingLocationTiles, player, task);

                    }
                    break;
                    case 2: {
                        // in case we are in plane 2
                        enterCaveNorthUpperLevel(playerTile, playerPlane);
                    }
                }
            }
            break;
            case REDWOOD_SOUTH_UPPER_LEVEL: {
                switch (playerPlane) {
                    case 0: {
                        walkToRedwoodLadderOutside(walking_tile_south, rope_ladder_tile_south, playerTile);
                    }
                    break;
                    case 1: {
                        enterCaveSouthLowerLevel(playerTile);
                    }
                    break;
                    case 2: {
                        walkToNextObject(destination, woodcuttingLocationTiles, player, task);
                    }
                }
            }
            break;
            case REDWOOD_NORTH_UPPER_LEVEL: {
                switch (playerPlane) {
                    case 0: {
                        walkToRedwoodLadderOutside(walking_tile_north, rope_ladder_tile_north, playerTile);
                    }
                    break;
                    case 1: {
                        enterCaveNorthLowerLevel(playerTile);
                    }
                    break;
                    case 2: {
                        walkToNextObject(destination, woodcuttingLocationTiles, player, task);
                    }
                }
            }
            break;
            case ISLE_OF_SOULS: {
                switch (playerPlane) {
                    case 0: {
                        if (Location.ISLE_OF_SOULS_COMPLETE.getRSArea().contains(playerTile)) {
                            walkToNextObject(destination, woodcuttingLocationTiles, player, task);
                        } else {
                            walkToSoulWarsPortalEdgeVille(playerTile);
                        }
                    }
                    break;
                    case 1: {
                        final RSObject[] ladder_object = Objects.getAt(rope_ladder_tile_north_inside);
                        walkToRedwoodLadderInside(playerTile, ladder_object, rope_ladder_tile_north);
                    }
                    break;
                    case 2: {
                        enterCaveNorthUpperLevel(playerTile, playerPlane);
                    }
                    break;
                }
            }
            default: {
                switch (playerPlane) {
                    case 0: {
                        walkToNextObject(destination, woodcuttingLocationTiles, player, task);
                    }
                    break;
                    case 1: {
                        final RSObject[] ladder_object = Objects.getAt(rope_ladder_tile_north_inside);
                        walkToRedwoodLadderInside(playerTile, ladder_object, rope_ladder_tile_north);
                    }
                    break;
                    case 2: {
                        enterCaveNorthUpperLevel(playerTile, playerPlane);
                    }
                    break;
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
            final RSTile[] treeTiles = Workable.discoverTreeTiles(woodcutting_location_tiles, task.getTree());
            final RSTile nextTree = AntiBan.selectNextTarget(treeTiles);
            // walk to next tree
            if (!Workable.walkToTile(nextTree)) {
                if (Workable.walkToTileA(nextTree, 5)) {
                    debug("Using alternative walker-engine");
                }
            }
        } else {
            // walk to working location if player isn't there or if no trees are in the location
            if (destination != null && !Workable.isInLocation(task, player)) {
                boolean walkResult = Workable.walkToTile(destination);
                if (!walkResult) {
                    if (Workable.walkToTileA(destination, 5)) {
                        debug("Using alternative walker-engine");
                    }
                }
            }
        }
    }

    private void walkToSawmillLocation(RSTile walking_tile) {
        if (!Plank.isAtSawmill()) {
            if (!Workable.walkToTile(walking_tile)) {
                if (Workable.walkToTileA(walking_tile, 5)) {
                    debug("Using alternative walker-engine");
                }
            }
        }
    }

    private void walkToWoodcuttingGuildAlternativeBankLocation(RSTile walking_tile) {
        if (!Bank.isInWoodcuttingGuildAlternativeBank()) {
            if (!Workable.walkToTile(walking_tile)) {
                if (Workable.walkToTileA(walking_tile, 5)) {
                    debug("Using alternative walker-engine");
                }
            }
        }
    }

    private void walkToRedwoodLadderOutside(RSTile walking_tile, RSTile rope_ladder_tile, RSTile player_tile) {
        if (player_tile.distanceTo(walking_tile) > 5) {
            if (!Workable.walkToTile(walking_tile)) {
                if (Workable.walkToTileA(walking_tile, 5)) {
                    debug("Using alternative walker-engine");
                }
            }
        }

        RSObject[] ladderObject = Objects.getAt(rope_ladder_tile);

        if (ladderObject.length > 0 && ladderObject[0].isOnScreen()) {
            if (!InteractionHelper.click(ladderObject[0], "Climb-up")) {
                if (InteractionHelper.focusCamera(ladderObject[0])) {
                    debug("Focused camera on ladder successful");
                }
            }
        }
    }

    private void walkToBank(Task task) {
        final RSPlayer player = Player.getRSPlayer();
        final RSTile playerTile = player.getPosition();
        final int playerPlane = playerTile.getPlane();

        switch (task.getActualLocation()) {
            case REDWOOD_SOUTH:
            case REDWOOD_SOUTH_UPPER_LEVEL: {
                switch (playerPlane) {
                    case 0: {
                        if (Workable.walkToBank(task.getBankLocation())) {
                            if (Workable.walkToTileA(task.getBankLocation().getPosition(), 15)) {
                                debug("Using alternative walker-engine");
                            }
                        }
                    }
                    break;
                    case 1: {
                        final RSObject[] ladder_object = Objects.getAt(rope_ladder_tile_south_inside);
                        walkToRedwoodLadderInside(playerTile, ladder_object, rope_ladder_tile_south);
                    }
                    break;
                    case 2: // call this in case of miss click while cutting below plane 2
                        enterCaveSouthUpperLevel(playerTile, playerPlane);
                }
            }
            case REDWOOD_NORTH:
            case REDWOOD_NORTH_UPPER_LEVEL: {
                switch (playerPlane) {
                    case 0: {
                        if (Workable.walkToBank(task.getBankLocation())) {
                            if (Workable.walkToTileA(task.getBankLocation().getPosition(), 15)) {
                                debug("Using alternative walker-engine");
                            }
                        }
                    }
                    break;
                    case 1: {
                        final RSObject[] ladder_object = Objects.getAt(rope_ladder_tile_north_inside);
                        walkToRedwoodLadderInside(playerTile, ladder_object, rope_ladder_tile_north);
                    }
                    break;
                    case 2: {
                        enterCaveNorthUpperLevel(playerTile, playerPlane);
                    }
                    break;
                }
            }
            default: {
                switch (playerPlane) {
                    case 0: {
                        if (Workable.walkToBank(task.getBankLocation())) {
                            if (Workable.walkToTileA(task.getBankLocation().getPosition(), 5)) {
                                debug("Using alternative walker-engine");
                            }
                        }
                    }
                    break;
                    case 1: {
                        final RSObject[] ladder_object = Objects.getAt(rope_ladder_tile_north_inside);
                        walkToRedwoodLadderInside(playerTile, ladder_object, rope_ladder_tile_north);
                    }
                    break;
                    case 2: {
                        enterCaveNorthUpperLevel(playerTile, playerPlane);
                    }
                    break;
                }
            }
        }
    }

    private void enterCaveSouthUpperLevel(RSTile playerTile, int playerPlane) {
        if (playerPlane == 2) {
            final RSTile caveTileSouthUpperLevel = new RSTile(1570, 3484, 2);
            final RSObject[] caveObjectSouth = Objects.getAt(caveTileSouthUpperLevel);

            if (caveObjectSouth.length > 0) {
                final boolean cameraResult = InteractionHelper.focusCamera(caveObjectSouth[0]);
                if (cameraResult) {
                    debug("Focused camera on cave object successful");
                }
                if (InteractionHelper.click(caveObjectSouth[0], "Enter")) {
                    final boolean waitResult = Timing.waitCondition(() -> {
                        final RSTile cave_entrance_plane_1_0 = new RSTile(1571, 3486, 1);
                        final RSTile cave_entrance_plane_1_1 = new RSTile(1570, 3486, 1);
                        return playerTile.equals(cave_entrance_plane_1_0) || playerTile.equals(cave_entrance_plane_1_1);
                    }, General.random(4000, 5000));
                    if (waitResult) {
                        debug("Wait result successful");
                    }
                }
            }
        }
    }

    private void enterCaveNorthUpperLevel(RSTile playerTile, int playerPlane) {
        if (playerPlane == 2) {
            final RSTile caveTileNorthUpperLevel = new RSTile(1570, 3490, 2);
            final RSObject[] caveObjectNorth = Objects.getAt(caveTileNorthUpperLevel);

            if (caveObjectNorth.length > 0) {
                final boolean cameraResult = InteractionHelper.focusCamera(caveObjectNorth[0]);
                if (cameraResult) {
                    debug("Focused camera on cave object successful");
                }
                if (InteractionHelper.click(caveObjectNorth[0], "Enter")) {
                    final boolean waitResult = Timing.waitCondition(() -> {
                        final RSTile cave_entrance_plane_2_0 = new RSTile(1571, 3489, 1);
                        final RSTile cave_entrance_plane_2_1 = new RSTile(1570, 3489, 1);
                        return playerTile.equals(cave_entrance_plane_2_0) || playerTile.equals(cave_entrance_plane_2_1);
                    }, General.random(4000, 5000));
                    if (waitResult) {
                        debug("Wait result successful");
                    }
                }
            }
        }
    }

    private void enterCaveSouthLowerLevel(RSTile playerTile) {
        final RSTile caveTileSouthLowerLevel = new RSTile(1570, 3484, 1);
        final RSObject[] caveObjectSouth = Objects.getAt(caveTileSouthLowerLevel);

        if (caveObjectSouth.length > 0) {
            final boolean cameraResult = InteractionHelper.focusCamera(caveObjectSouth[0]);
            if (cameraResult) {
                debug("Focused camera on cave object successful");
            }
            if (InteractionHelper.click(caveObjectSouth[0], "Enter")) {
                final boolean waitResult = Timing.waitCondition(() -> {
                    final RSTile cave_entrance_plane_1_0 = new RSTile(1571, 3486, 2);
                    final RSTile cave_entrance_plane_1_1 = new RSTile(1570, 3486, 2);
                    return playerTile.equals(cave_entrance_plane_1_0) || playerTile.equals(cave_entrance_plane_1_1);
                }, General.random(4000, 8000));
                if (waitResult) {
                    debug("Wait result successful");
                }
            }
        }
    }

    private void enterCaveNorthLowerLevel(RSTile playerTile) {
        final RSTile caveTileNorthLowerLevel = new RSTile(1570, 3490, 1);

        final RSObject[] caveObjectNorth = Objects.getAt(caveTileNorthLowerLevel);

        if (caveObjectNorth.length > 0) {
            final boolean cameraResult = InteractionHelper.focusCamera(caveObjectNorth[0]);
            if (cameraResult) {
                debug("Focused camera on cave object successful");
            }
            if (InteractionHelper.click(caveObjectNorth[0], "Enter")) {
                final boolean waitResult = Timing.waitCondition(() -> {
                    final RSTile cave_entrance_plane_1_0 = new RSTile(1571, 3489, 2);
                    final RSTile cave_entrance_plane_1_1 = new RSTile(1570, 3489, 2);
                    return playerTile.equals(cave_entrance_plane_1_0) || playerTile.equals(cave_entrance_plane_1_1);
                }, General.random(4000, 8000));
                if (waitResult) {
                    debug("Wait result successful");
                }
            }
        }
    }

    private void walkToRedwoodLadderInside(RSTile playerTile, RSObject[] ladder_object, RSTile ropeTile) {
        if (ladder_object.length > 0) {
            if (InteractionHelper.focusCamera(ladder_object[0])) {
                debug("Focused camera on ladder object successful");
            }
            if (InteractionHelper.click(ladder_object[0], "Climb-down")) {
                if (Timing.waitCondition(() -> playerTile == ropeTile, General.random(4000, 5000))) {
                    debug("Wait result successful");
                }
            }
        }
    }

    private void walkToSoulWarsPortalEdgeVille(RSTile playerTile) {
        final RSTile soulWarsPortalTile = new RSTile(3082, 3473, 0);

        if (playerTile.distanceTo(soulWarsPortalTile) > 5) {
            if (Workable.walkToTile(soulWarsPortalTile)) {
                debug("Walking to soul wars portal edgeville");
            }
        }

        final RSObject[] portals = Objects.getAt(soulWarsPortalTile);

        if (portals.length > 0) {
            if (portals[0].isOnScreen() && portals[0].isClickable()) {
                if (!DynamicClicking.clickRSObject(portals[0], "Enter")) {
                    if (portals[0].adjustCameraTo()) {
                        debug("Adjusted camera on portal successful");
                    }
                }
            } else {
                final int optimal = Camera.getOptimalAngleForPositionable(portals[0]);
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

