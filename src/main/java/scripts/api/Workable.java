package scripts.api;

import org.tribot.api.General;
import org.tribot.api.util.abc.preferences.WalkingPreference;
import org.tribot.api2007.*;
import org.tribot.api2007.Objects;
import org.tribot.api2007.types.*;
import scripts.api.antiban.AntiBan;
import scripts.dax_api.api_lib.DaxWalker;
import scripts.dax_api.api_lib.models.RunescapeBank;
import scripts.dax_api.shared.helpers.BankHelper;
import scripts.dax_api.walker_engine.WalkingCondition;

import java.util.*;

/**
 * The workable interface contains critical methods and constants
 * that assist in development, debugging and planning.
 * Essentially this class provides assistance the other classes must have to function correctly.
 */

public interface Workable {

    // the oak fee to actually make oak planks
    int OAK_FEE = 250;

    // gold id constant
    int GOLD = 995;

    // knife id constant
    int KNIFE = 946;

    // axe id constants
    int[] AXES = Constants.IDs.Items.hatchets;
    int CRYSTAL_AXE_ACTIVE = 23673;
    int CRYSTAL_AXE_INACTIVE = 23675;
    int INFERNAL_AXE_INACTIVE = 13242;
    int INFERNAL_AXE_ACTIVE = 13241;
    int DRAGON_AXE = 6739;
    int RUNE_AXE = 1359;
    int ADAMANT_AXE = 1357;
    int MITHRIL_AXE = 1355;
    int BLACK_AXE = 1361;
    int STEEL_AXE = 1353;
    int IRON_AXE = 1349;
    int BRONZE_AXE = 1351;

    /**
     * @return All plank's currently inside the player's inventory
     */
    static RSItem[] getAllPlanks() {
        return Inventory.find(rsItem -> {
            RSItemDefinition itemDefinition = rsItem.getDefinition();
            return itemDefinition != null && itemDefinition.getName().toLowerCase().contains("plank");
        });
    }

    /**
     * @return All gold currently inside the player's inventory
     */
    static RSItem[] getAllGold() {
        return Inventory.find(rsItem -> {
            RSItemDefinition itemDefinition = rsItem.getDefinition();
            return itemDefinition != null && itemDefinition.getName().toLowerCase().contains("coins");
        });
    }

    /**
     * @return All logs currently inside the player's inventory
     */
    static RSItem[] getAllLogs() {
        return Inventory.find(rsItem -> {
            RSItemDefinition itemDefinition = rsItem.getDefinition();
            return itemDefinition != null && itemDefinition.getName().toLowerCase().contains("log");
        });
    }

    /**
     * @return All bows currently inside the player's inventory
     */
    static RSItem[] getAllBows() {
        return Inventory.find(rsItem -> {
            RSItemDefinition itemDefinition = rsItem.getDefinition();
            return itemDefinition != null && itemDefinition.getName().toLowerCase().contains("bow");
        });
    }

    /**
     * @return All arrow shafts currently inside the player's inventory
     */
    static RSItem[] getAllArrowShafts() {
        return Inventory.find(rsItem -> {
            RSItemDefinition itemDefinition = rsItem.getDefinition();
            return itemDefinition != null && itemDefinition.getName().toLowerCase().contains("arrow shaft");
        });
    }

    /**
     * Get all the axes in the game.
     *
     * @return All axes
     */
    static int[] completeAxes() {
        List<Integer> temp = new LinkedList<>(Arrays.asList(
                CRYSTAL_AXE_ACTIVE,
                CRYSTAL_AXE_INACTIVE,
                INFERNAL_AXE_ACTIVE,
                INFERNAL_AXE_INACTIVE)
        );

        Arrays.stream(AXES)
                .forEach(temp::add);

        return temp.stream()
                .mapToInt(Integer::intValue)
                .toArray();
    }

    /**
     * Check if player is animating.
     *
     * @return True when the player is cutting; false otherwise.
     */
    static boolean isWorking() {
        return Player.getAnimation() != -1;
    }

    /**
     * Return player special attack state
     *
     * @return Current special attack percent
     */
    static int getSpecialAttack() {
        return (Game.getSettingsArray()[300] / 10);
    }

    /**
     * Return player currently equipped axe if "special" attack usable
     *
     * @return True if player axe is dragon/infernal/crystal; false otherwise.
     */
    static boolean isSpecialAxeEquipped() {
        if (!isAxeEquipped()) {
            return false;
        }

        final RSItem axe = Equipment.getItem(Equipment.SLOTS.WEAPON);

        if (axe != null) {
            final int axeID = axe.getID();
            if (axeID > 0) {
                switch (axeID) {
                    case INFERNAL_AXE_ACTIVE:
                    case INFERNAL_AXE_INACTIVE:
                    case CRYSTAL_AXE_ACTIVE:
                    case CRYSTAL_AXE_INACTIVE:
                    case DRAGON_AXE: {
                        return Equipment.isEquipped(axeID);
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks if player is near a tree
     *
     * @return True if their is a nearest object; false otherwise.
     */
    static boolean nearObjects(int distance, String object) {
        if (object != null) {
            final RSObject[] objects = Objects.findNearest(distance, object);
            final int playerPlane = Player.getPosition().getPlane();
            if (objects.length > 0) {
                for (final RSObject o : objects) {
                    if (o.getPosition().getPlane() == playerPlane) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks entire RSTile[] array for matching object name
     *
     * @param tiles The tiles that need to be checked for matching objects
     * @return True if object exists : false otherwise.
     */
    static boolean objectsExist(RSTile[] tiles, String objectName) {
        if (tiles != null & objectName != null) {
            if (tiles.length > 0 && !objectName.isEmpty()) {
                for (final RSTile tile : tiles) {
                    if (Objects.isAt(tile, objectName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Find all tiles that have an object in correlation to trees
     *
     * @param treeArea The area containing the trees
     * @param treeName The tree name to find
     * @return All tiles belonging to the tree name
     */
    static RSTile[] discoverTreeTiles(RSTile[] treeArea, String treeName) {
        List<RSTile> treeList = new ArrayList<>();

        if (treeArea != null && treeName != null) {
            if (treeArea.length > 0 && !treeName.isEmpty()) {
                for (RSTile tile : treeArea) {
                    if (Objects.isAt(tile, treeName)) {
                        treeList.add(tile);
                    }
                }

                Collections.shuffle(treeList);
            }
        }

        return new ArrayList<>(treeList).toArray(RSTile[]::new);
    }

    static boolean walkToTileA(RSTile tile, int distance) {
        final RSTile[] path = Walking.generateStraightPath(tile);
        final int playerDistance = Player.getPosition().distanceTo(tile);
        final WalkingPreference walkingPreference = AntiBan.generateWalkingPreference(playerDistance);

        Walking.setControlClick(true);

        if (walkingPreference.equals(WalkingPreference.MINIMAP)) {
            return Walking.walkPath(path, () -> {
                RSTile playerPos = Player.getPosition();
                boolean run = AntiBan.activateRun();
                return playerPos.distanceTo(tile) < distance;
            }, 1000);
        } else {
            return screenWalkToTile(tile);
        }
    }

    static boolean walkToTile(RSTile tile) {
        final int playerDistance = Player.getPosition().distanceTo(tile);
        final WalkingPreference walkingPreference = AntiBan.generateWalkingPreference(playerDistance);

        if (walkingPreference.equals(WalkingPreference.MINIMAP)) {
            return DaxWalker.walkTo(tile, () -> {
                boolean run = AntiBan.activateRun();
                if (Player.getPosition().distanceTo(tile) < 5) {
                    return WalkingCondition.State.EXIT_OUT_WALKER_SUCCESS;
                } else {
                    return WalkingCondition.State.CONTINUE_WALKER;
                }
            });
        } else {
            return screenWalkToTile(tile);
        }
    }

    static boolean walkToBank(RunescapeBank bank) {
        final RSTile bankTile = bank.getPosition();
        final int playerDistance = Player.getPosition().distanceTo(bankTile);
        final WalkingPreference walkingPreference = AntiBan.generateWalkingPreference(playerDistance);

        if (walkingPreference.equals(WalkingPreference.MINIMAP)) {
            return DaxWalker.walkToBank(bank, () -> {
                boolean run = AntiBan.activateRun();
                if (Player.getPosition().distanceTo(bankTile) < 5) {
                    return WalkingCondition.State.EXIT_OUT_WALKER_SUCCESS;
                } else {
                    return WalkingCondition.State.CONTINUE_WALKER;
                }
            });
        } else {
            return screenWalkToTile(bankTile);
        }
    }

    static boolean screenWalkToTile(RSTile tile) {
        final RSTile[] walkingPath = Walking.generateStraightScreenPath(tile);
        return Walking.walkScreenPath(walkingPath);
    }

    static boolean isInLocation(Task task, RSPlayer player) {
        final RSTile playerTile = player.getPosition();
        final int playerPlane = playerTile.getPlane();

        if (task != null) {
            final RSTile[] locationTiles = task.getActualLocation().getRSArea().getAllTiles();
            for (final RSTile tile : locationTiles) {
                if (tile.distanceTo(playerTile) <= 3 && playerPlane == tile.getPlane()) {
                    return true;
                }
            }
        }

        return false;
    }

    // a method that returns true if we have an axe in the inventory
    static boolean inventoryContainsAxe() {
        final int[] allAxes = completeAxes();

        for (final int axe : allAxes) {
            if (Inventory.find(axe).length > 0) {
                return true;
            }
        }

        return false;
    }

    static boolean inventoryContainsKnife() {
        return Inventory.find(KNIFE).length > 0;
    }

    static boolean inventoryContainsGold() {
        return getAllGold().length > 0;
    }

    // a method that returns true if we have an axe equipped
    static boolean isAxeEquipped() {
        final int[] allAxes = completeAxes();

        for (final int axe : allAxes) {
            if (Equipment.isEquipped(axe)) {
                return true;
            }
        }

        return false;
    }

    // a method that maps all of the axes to their correspondent woodcutting level
    static HashMap<Integer, Integer> getMappedWCLevels() {
        HashMap<Integer, Integer> mappedWCLevels = new LinkedHashMap<>();
        mappedWCLevels.put(CRYSTAL_AXE_ACTIVE, 71);
        mappedWCLevels.put(CRYSTAL_AXE_INACTIVE, 71);
        mappedWCLevels.put(INFERNAL_AXE_INACTIVE, 61);
        mappedWCLevels.put(INFERNAL_AXE_ACTIVE, 61);
        mappedWCLevels.put(DRAGON_AXE, 61);
        mappedWCLevels.put(RUNE_AXE, 41);
        mappedWCLevels.put(ADAMANT_AXE, 31);
        mappedWCLevels.put(MITHRIL_AXE, 21);
        mappedWCLevels.put(BLACK_AXE, 11);
        mappedWCLevels.put(STEEL_AXE, 6);
        mappedWCLevels.put(IRON_AXE, 1);
        mappedWCLevels.put(BRONZE_AXE, 1);
        return mappedWCLevels;
    }

    // a method that maps all of the axes to their correspondent attack level
    static HashMap<Integer, Integer> getMappedATTLevels() {
        HashMap<Integer, Integer> mappedATTLevels = new LinkedHashMap<>();
        mappedATTLevels.put(CRYSTAL_AXE_ACTIVE, 70);
        mappedATTLevels.put(CRYSTAL_AXE_INACTIVE, 70);
        mappedATTLevels.put(INFERNAL_AXE_INACTIVE, 60);
        mappedATTLevels.put(INFERNAL_AXE_ACTIVE, 60);
        mappedATTLevels.put(DRAGON_AXE, 60);
        mappedATTLevels.put(RUNE_AXE, 40);
        mappedATTLevels.put(ADAMANT_AXE, 30);
        mappedATTLevels.put(MITHRIL_AXE, 20);
        mappedATTLevels.put(BLACK_AXE, 10);
        mappedATTLevels.put(STEEL_AXE, 5);
        mappedATTLevels.put(IRON_AXE, 1);
        mappedATTLevels.put(BRONZE_AXE, 1);
        return mappedATTLevels;
    }

    static void optimizeGame(RSTile tile) {
        optimizeGame();
        if (isWorking() && Camera.getCameraAngle() < 65) {
            tile.adjustCameraTo();
            Camera.setCameraAngle(General.random(85, 100));
        }
    }

    static void optimizeGame() {
        if (!GameTab.getOpen().equals(GameTab.TABS.INVENTORY)) {
            GameTab.open(GameTab.TABS.INVENTORY);
        }
    }

    default HashMap<String, Integer> getMappedBowLevels() {
        final HashMap<String, Integer> map = new LinkedHashMap<>();
        map.putIfAbsent("Shortbow", 5);
        map.putIfAbsent("Longbow", 10);
        map.putIfAbsent("Oak shortbow", 20);
        map.putIfAbsent("Oak longbow", 25);
        map.putIfAbsent("Willow shortbow", 35);
        map.putIfAbsent("Willow longbow", 40);
        map.putIfAbsent("Maple shortbow", 50);
        map.putIfAbsent("Maple longbow", 55);
        map.putIfAbsent("Yew shortbow", 65);
        map.putIfAbsent("Yew longbow", 70);
        map.putIfAbsent("Magic shortbow", 80);
        map.putIfAbsent("Magic longbow", 85);
        map.putIfAbsent("Redwood shield", 92);
        return map;
    }

    default HashMap<String, Integer> getMappedArrowLevels() {
        final HashMap<String, Integer> map = new LinkedHashMap<>();
        map.putIfAbsent("15 arrow shafts", 1);
        map.putIfAbsent("30 arrow shafts", 15);
        map.putIfAbsent("45 arrow shafts", 30);
        map.putIfAbsent("60 arrow shafts", 45);
        map.putIfAbsent("75 arrow shafts", 60);
        map.putIfAbsent("90 arrow shafts", 75);
        map.putIfAbsent("105 arrow shafts", 90);
        return map;
    }

}
