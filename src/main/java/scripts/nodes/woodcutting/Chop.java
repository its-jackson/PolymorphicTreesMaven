package scripts.nodes.woodcutting;

import org.tribot.api2007.types.*;
import scripts.api.*;
import org.tribot.api2007.*;
import org.tribot.api.General;
import org.tribot.api.Timing;
import scripts.api.antiban.AntiBan;
import scripts.dax_api.walker_engine.interaction_handling.InteractionHelper;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Purpose of class: Chop down trees while inside the active task's woodcutting location.
 * Author: Jackson Johnson (Polymorphic~TRiBot)
 * Date: Aug 30th, 2020
 *
 * Updated 11/04/2021 - Added null safe checks to all methods and cached all return values.
 *
 * Updated 11/05/2021 - Changed naming convention for final variables.
 *
 */

public class Chop extends Node {

    private final WorldHop worldHop = new WorldHop();
    private final SpecialAttack specialAttackNode = new SpecialAttack();
    private final BirdNest birdNestNode = new BirdNest();

    private RSObject[] trees;
    private RSObject tree;

    // filter redwood upper level predicate
    // constant
    private static Predicate<RSObject> filter_upper_level_redwood(Task task) {
        return rsObject -> {
            RSObjectDefinition definition = rsObject.getDefinition();
            if (definition == null) {
                return false;
            }
            return rsObject.getPosition().getPlane() == 2
                    && definition.getName().contains(task.getTree())
                    && definition.getActions().length > 0;
        };
    }

    // filter redwood lower level predicate
    // constant
    private static Predicate<RSObject> filter_lower_level_redwood(Task task) {
        return rsObject -> {
            RSObjectDefinition definition = rsObject.getDefinition();
            if (definition == null) {
                return false;
            }
            return rsObject.getPosition().getPlane() == 1
                    && definition.getName().contains(task.getTree())
                    && definition.getActions().length > 0;
        };
    }

    @Override
    public void execute(Task task) {
        final long startTime = System.currentTimeMillis();

        AntiBan.sleep(Globals.getWaitTimes(), AntiBan.getHumanFatigue());

        setTrees(Objects.findNearest(Globals.getTreeFactor(), task.getTree()));

        // filter the trees for the tasks location
        switch (task.getActualLocation()) {
            case REDWOOD_SOUTH:
            case REDWOOD_NORTH: {
                setTrees(Arrays.stream(getTrees())
                        .filter(filter_lower_level_redwood(task))
                        .toArray(RSObject[]::new));
            }
            break;
            case REDWOOD_SOUTH_UPPER_LEVEL:
            case REDWOOD_NORTH_UPPER_LEVEL: {
                setTrees(Arrays.stream(getTrees())
                        .filter(filter_upper_level_redwood(task))
                        .toArray(RSObject[]::new));
            }
            break;
            case WOODCUTTING_GUILD_OAKS: {
                setTrees(Arrays.stream(getTrees())
                        .filter(rsObject -> task.getActualLocation().getRSArea().contains(rsObject.getPosition()))
                        .toArray(RSObject[]::new));
            }
            break;
            case ISLE_OF_SOULS:
            case SORCERERS_TOWER: {
                setTrees(getTrees());
            }
            break;
            default: {
                setTrees(reachableTrees(getTrees()));
            }
        }

        // proceed to chop down the tree
        if (getTrees().length > 0) {
            debug("Locating " + task.getTree().toLowerCase());

            setTree(AntiBan.selectNextTarget(getTrees()));

            Globals.setCurrentWorkingTree(getTree());

            // check if player can equip the axe in the inventory
            if (Workable.inventoryContainsAxe()) {
                final List<RSItem> inventoryList = Inventory.getAllList();
                int inventoryAxeId = 0;
                String axeName = "";
                if (inventoryList.size() > 0) {
                    for (final RSItem inventory_item : inventoryList) {
                        final RSItemDefinition itemDefinition = inventory_item.getDefinition();
                        if (itemDefinition != null) {
                            final int inventoryItemId = itemDefinition.getID();
                            for (final int axeId : Workable.completeAxes()) {
                                if (axeId == inventoryItemId) {
                                    inventoryAxeId = inventoryItemId;
                                    axeName = itemDefinition.getName().toLowerCase();
                                    break;
                                }
                            }
                        }
                        // found axe, lets get the hell out of here
                        if (inventoryAxeId > 0) {
                            break;
                        }
                    }
                }
                // attempt to equip axe, if value assigned
                if (inventoryAxeId > 0) {
                    if (FetchAxe.equipAxe(
                            inventoryAxeId,
                            getWorker().getPlayerAttackLevel(),
                            getWorker().getPlayerFiremakingLevel(),
                            getWorker().getPlayerAgilityLevel())) {
                        debug("Equipped " + axeName);
                    }
                }
            }
            // chop down the tree now
            final boolean choppingResult = chopTree(getTree());
            if (choppingResult) {
                // perform loop while chopping down tree
                debug("Clicked " + task.getTree().toLowerCase());
                completeChoppingTask(getTrees(), getTree(), task);
            }
            // destroy working tree object once tree dead or inventory full
            Globals.setCurrentWorkingTree(null);
        } else {
            debug("No valid trees discovered");
        }

        // when no longer chopping (end of node execution). Generate the trackers for the next node
        AntiBan.generateTrackers((int) (System.currentTimeMillis() - startTime), false);
    }

    @Override
    public boolean validate(Task task) {
        return shouldChop(task);
    }

    @Override
    public void debug(String status) {
        String format = ("[Chop Control] ");
        Globals.setState(status);
        General.println(format.concat(status));
    }

    /**
     * Perform the necessary tasks while chopping.
     *
     * @param trees for performing tasks.
     */
    private void completeChoppingTask(RSObject[] trees, RSObject tree, Task task) {
        switch (task.getActualLocation()) {
            case REDWOOD_NORTH:
            case REDWOOD_SOUTH:
            case REDWOOD_NORTH_UPPER_LEVEL:
            case REDWOOD_SOUTH_UPPER_LEVEL: {
                int optimal = Camera.getOptimalAngleForPositionable(tree);
                if (Camera.getCameraAngle() != optimal) {
                    final boolean treeAdjustResult = tree.adjustCameraTo();
                    if (treeAdjustResult) {
                        debug("Adjusted camera successfully");
                    } else {
                        debug("Adjusted camera unsuccessful");
                    }
                    Camera.setRotationMethod(Camera.getRotationMethod());
                    Camera.setCameraAngle(optimal);
                }
            }
            break;
            default: {
                Workable.optimizeGame(tree.getPosition());
            }
        }
        // perform tasks while chopping down tree...
        // Objects.isAt(tree_tile, task.getTree()
        while (Workable.isWorking()) {
            General.sleep(1000, 1500);
            debug("Chopping " + task.getTree().toLowerCase());
            if (task.isValidated()) {
                debug("Task complete");
                break;
            }
            Workable.optimizeGame();
            // found bird nest on ground
            // leave loop
            if (shouldPickupBirdNestNow(task)) {
                getBirdNestNode().debug("Bird nest complete");
                General.sleep(200, 400);
            }
            // special attack boost ready
            // leave loop
            if (shouldUtilizeSpecialAttackNow(task)) {
                getSpecialAttackNode().debug("Special ability complete");
                General.sleep(200, 400);
            }
            // time to world hop, too many players
            if (shouldWorldHopNow(task)) {
                getWorldHopNode().debug("World change complete");
                General.sleep(200, 400);
            }
            // let ABC2 determine the next anti-ban task/action
            AntiBan.checkAntiBanTask(trees, tree);
        }
    }

    private boolean isTreeReachable(RSObject tree) {
        return PathFinding.canReach(tree.getPosition().clone(), true);
    }

    private RSObject[] reachableTrees(RSObject[] trees) {
        if (trees != null) {
            return Arrays.stream(trees)
                    .filter(this::isTreeReachable)
                    .toArray(RSObject[]::new);
        }

        return new RSObject[]{};
    }

    private boolean chopTree(RSObject tree) {
        final boolean focusTreeResult = InteractionHelper.focusCamera(tree);

        if (focusTreeResult) {
            debug("Focused tree successful");
        }

        if (InteractionHelper.click(tree, "Chop down", "Cut")) {
            // We clicked the tree. Let's first wait to stop chopping for 1-1.2
            // seconds just in case we moved on to this tree while still performing
            // the chopping animation.
            // wait until we are cutting before timout
            return Timing.waitCondition(Workable::isWorking, General.random(5000, 6000));
        }

        return false;
    }

    private boolean executeNode(Task task, Node node) {
        if (node.validate(task)) {
            node.execute(task);
            return true;
        }
        return false;
    }

    private boolean shouldWorldHopNow(Task task) {
        return executeNode(task, getWorldHopNode());
    }

    private boolean shouldUtilizeSpecialAttackNow(Task task) {
        return executeNode(task, getSpecialAttackNode());
    }

    private boolean shouldPickupBirdNestNow(Task task) {
        return executeNode(task, getBirdNestNode());
    }

    private boolean shouldChop(Task task) {
        final RSPlayer player = Player.getRSPlayer();
        final String logDisposalOption = task.getLogOption().toLowerCase();

        if (!Inventory.isFull()) {
            if (Workable.nearObjects(Globals.getTreeFactor(), task.getTree()) && Workable.isInLocation(task, player)) {
                if (Workable.inventoryContainsAxe() || Workable.isAxeEquipped()) {
                    // return based on log disposal, inventory must have knife to return true for fletch
                    switch (logDisposalOption) {
                        case "fletch-bank":
                        case "fletch-drop": {
                            return Workable.inventoryContainsKnife();
                        }
                        default: {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public WorldHop getWorldHopNode() {
        return worldHop;
    }

    public SpecialAttack getSpecialAttackNode() {
        return specialAttackNode;
    }

    public BirdNest getBirdNestNode() {
        return birdNestNode;
    }

    public RSObject[] getTrees() {
        return trees;
    }

    public void setTrees(RSObject[] trees) {
        this.trees = trees;
    }

    public RSObject getTree() {
        return tree;
    }

    public void setTree(RSObject tree) {
        this.tree = tree;
    }
}