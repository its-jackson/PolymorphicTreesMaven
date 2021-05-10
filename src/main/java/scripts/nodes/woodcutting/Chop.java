package scripts.nodes.woodcutting;

import org.tribot.api2007.types.*;
import scripts.api.*;
import org.tribot.api2007.*;
import org.tribot.api.General;
import org.tribot.api.Timing;
import scripts.api.antiban.AntiBan;
import scripts.dax_api.walker_engine.interaction_handling.InteractionHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Purpose of class: Cut trees when inside the woodcutting location
 * Author: Jackson Johnson (Polymorphic~TRiBot)
 * Date: Aug 30th, 2020
 */

public class Chop extends Node {
    private final long start_time = System.currentTimeMillis();
    private final WorldHop world_hop_node = new WorldHop();
    private final SpecialAttack special_attack_node = new SpecialAttack();
    private final BirdNest bird_nest_node = new BirdNest();

    private Worker worker;
    private RSObject[] trees;
    private RSObject tree;
    private RSObject nextTree;

    private static Predicate<RSObject> filter_upper_level(Task task) {
        return rsObject -> rsObject.getPosition().getPlane() == 2
                && rsObject.getDefinition().getName().contains(task.getTree())
                && rsObject.getDefinition().getActions().length > 0;
    }

    private static Predicate<RSObject> filter_lower_level(Task task) {
        return rsObject -> rsObject.getPosition().getPlane() == 1
                && rsObject.getDefinition().getName().contains(task.getTree())
                && rsObject.getDefinition().getActions().length > 0;
    }

    @Override
    public void execute(Task task) {
        Workable.sleep(Globals.waitTimes, Globals.humanFatigue);

        // set the worker
        worker = new Worker(
                Progressive.generateAttackLevel(),
                Progressive.generateWoodcuttingLevel(),
                Progressive.generateFiremakingLevel(),
                Progressive.generateAgilityLevel(),
                Progressive.songOfElvesCompletable(),
                Progressive.isMember()
        );

        // filter the trees
        switch (task.getActualLocation()) {
            case REDWOOD_NORTH, REDWOOD_SOUTH -> trees = Arrays.stream(Globals.objectsNear)
                    .filter(filter_lower_level(task))
                    .toArray(RSObject[]::new);
            case REDWOOD_SOUTH_UPPER_LEVEL, REDWOOD_NORTH_UPPER_LEVEL -> trees = Arrays.stream(Globals.objectsNear)
                    .filter(filter_upper_level(task))
                    .toArray(RSObject[]::new);
            case WOODCUTTING_GUILD_OAKS -> trees = Arrays.stream(Globals.objectsNear).filter(rsObject -> task.getActualLocation().getRSArea().contains(rsObject.getPosition())).toArray(RSObject[]::new);
            case ISLE_OF_SOULS, SORCERERS_TOWER -> trees = Globals.objectsNear;
            default -> trees = reachableTrees(Globals.objectsNear);
        }

        // proceed to chop down the tree
        if (trees != null && trees.length > 0) {
            debug("Locating " + task.getTree().toLowerCase());
            setTree(AntiBan.selectNextTarget(trees));
            Globals.currentWorkingTree = getTree();
            for (RSObject tree : trees) {
                if (!tree.equals(getTree())) {
                    setNextTree(tree);
                    break;
                }
            }
            // check if player can equip the axe in the inventory
            if (Workable.inventoryContainsAxe()) {
                final List<RSItem> inventory_list = Inventory.getAllList();
                int inventoryAxeId = 0;
                String axeName = "";
                for (final RSItem inventory_item : inventory_list) {
                    final RSItemDefinition item_definition = inventory_item.getDefinition();
                    for (final int axe_id : Workable.completeAxes()) {
                        final int inventory_item_id = item_definition.getID();
                        if (axe_id == inventory_item_id) {
                            inventoryAxeId = inventory_item_id;
                            axeName = item_definition.getName().toLowerCase();
                            break;
                        }
                    }
                    // found axe, lets get the hell out of here
                    if (inventoryAxeId > 0) {
                        break;
                    }
                }
                // attempt to equip axe, if value assigned
                if (inventoryAxeId > 0) {
                    if (FetchAxe.equipAxe(inventoryAxeId, worker.getPlayerAttackLevel(),
                            worker.getPlayerFiremakingLevel(), worker.getPlayerAgilityLevel())) {
                        debug("Equipped " + axeName);
                    }
                }
            }
            // chop down the tree now
            final boolean chopping_result = chopTree(tree);
            if (chopping_result) {
                // perform loop while chopping down tree
                debug("Clicked " + task.getTree().toLowerCase());
                completeChoppingTask(trees, tree, task);
            }
            // destroy working tree object once tree dead or inventory full
            Globals.currentWorkingTree = null;
        } else {
            debug("No valid trees");
        }
        // chop the next tree (short circuit)
        if (getNextTree() != null && Objects.isAt(getNextTree().getPosition(), task.getTree()) && !Inventory.isFull() && !task.isValidated()) {
            Globals.nextWorkingTree = getNextTree();

            Workable.sleep(Globals.waitTimes, Globals.humanFatigue);

            debug("Switching " + task.getTree().toLowerCase());

            final boolean next_chopping_result = chopTree(getNextTree());

            if (next_chopping_result) {
                completeChoppingTask(trees, nextTree, task);
            }
        }
        // when no longer chopping (end of node execution). Generate the trackers for the next node
        AntiBan.generateTrackers((int) (System.currentTimeMillis() - getStartTime()), false);
    }

    @Override
    public boolean validate(Task task) {
        return shouldChop(task);
    }

    @Override
    public void debug(String status) {
        String format = ("[Chop Control] ");
        Globals.STATE = (status);
        General.println(format.concat(status));
    }

    /**
     * Perform the necessary tasks while chopping.
     *
     * @param trees for performing tasks.
     */
    private void completeChoppingTask(RSObject[] trees, RSObject tree, Task task) {
        final RSTile tree_tile = tree.getPosition().clone();

        switch (task.getActualLocation()) {
            case REDWOOD_NORTH, REDWOOD_SOUTH, REDWOOD_NORTH_UPPER_LEVEL, REDWOOD_SOUTH_UPPER_LEVEL: {
                int optimal = Camera.getOptimalAngleForPositionable(tree);
                if (Camera.getCameraAngle() != optimal) {
                    final boolean tree_adjust_result = tree.adjustCameraTo();
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
            General.sleep(1000, 1800);
            debug("Chopping " + task.getTree().toLowerCase());
            if (task.isValidated()) {
                debug("Task complete");
                break;
            }
            Workable.optimizeGame();
            General.sleep(200, 400);
            // found bird nest on ground
            // leave loop
            if (shouldPickupBirdNestNow(task)) {
                getBirdNestNode().debug("Bird nest complete");
            }
            General.sleep(200, 400);
            // special attack boost ready
            // leave loop
            if (shouldUtilizeSpecialAttackNow(task)) {
                getSpecialAttackNode().debug("Special ability complete");
            }
            General.sleep(200, 400);
            // time to world hop, too many players
            if (shouldWorldHopNow(task)) {
                getWorldHopNode().debug("World hop complete");
            }
            General.sleep(200, 400);
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
                            .collect(Collectors.toList()).toArray(RSObject[]::new);
        }
        return null;
    }

    private boolean chopTree(RSObject tree) {
        final boolean focus_tree_result = InteractionHelper.focusCamera(tree);

        if (InteractionHelper.click(tree, "Chop down", "Cut")) {
            // We clicked the tree. Let's first wait to stop chopping for 1-1.2
            // seconds just in case we moved on to this tree while still performing
            // the chopping animation.
            final boolean time_result = Timing.waitCondition(() -> {
                General.sleep(200, 300);
                return !Workable.isWorking();
            }, General.random(1000, 1200));
        }
        // wait until we are cutting before timout
        return Timing.waitCondition(() -> {
            General.sleep(200, 300);
            return Workable.isWorking();
        }, General.random(5000, 7000));
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
        final String log_disposal_option = task.getLogOption().toLowerCase();

        switch (log_disposal_option) {
            case "fletch-bank", "fletch-drop" -> {
                return !Inventory.isFull() && Globals.objectsNear != null && Globals.objectsNear.length > 0
                        && Workable.inventoryContainsKnife()
                        && Workable.inventoryContainsAxe()
                        && Workable.isInLocation(task, player)

                        ||

                        !Inventory.isFull() && Globals.objectsNear != null && Globals.objectsNear.length > 0
                                && Workable.inventoryContainsKnife()
                                && Workable.isAxeEquipped()
                                && Workable.isInLocation(task, player)
                        ;
            }
            default -> {
                return !Inventory.isFull() && Globals.objectsNear != null && Globals.objectsNear.length > 0
                        && Workable.inventoryContainsAxe()
                        && Workable.isInLocation(task, player)

                        ||

                        !Inventory.isFull() && Globals.objectsNear != null && Globals.objectsNear.length > 0
                                && Workable.isInLocation(task, player)
                                && Workable.isAxeEquipped()
                        ;
            }
        }
    }

    public long getStartTime() {
        return start_time;
    }

    public WorldHop getWorldHopNode() {
        return world_hop_node;
    }

    public SpecialAttack getSpecialAttackNode() {
        return special_attack_node;
    }

    public BirdNest getBirdNestNode() {
        return bird_nest_node;
    }

    public RSObject getTree() {
        return tree;
    }

    public void setTree(RSObject tree) {
        this.tree = tree;
    }

    public RSObject getNextTree() {
        return nextTree;
    }

    public void setNextTree(RSObject nextTree) {
        this.nextTree = nextTree;
    }
}