package scripts.api;

import org.tribot.api2007.types.RSItem;
import scripts.dax_api.api_lib.models.RunescapeBank;

/**
 * Purpose of class: The task class is used to determine the core functionality of the script.
 *                      Such as witch tree to chop, where to chop, and how to dispose the logs.
 */

public class Task {

    // not-dependable
    private String tree;
    private String location;
    private String logOption;
    private int untilLevel;
    private TimeElapse timer;

    // dependable (location and tree)
    private RunescapeBank bankLocation;
    private Location actualLocation;

    public Task() {}

    public Task(String tree, String location, String logOption, int untilLevel, TimeElapse timer) {
        this.tree = tree;
        this.location = location;
        this.logOption = logOption;
        this.untilLevel = untilLevel;
        this.timer = timer;
        setCompleteTask(this.location, this.tree);
    }

    public Task(String tree, String location, String logOption, int untilLevel) {
        this.tree = tree;
        this.location = location;
        this.logOption = logOption;
        this.untilLevel = untilLevel;
        setCompleteTask(this.location, this.tree);
    }

    public Task(String tree, String location, String logOption, TimeElapse timer) {
        this.tree = tree;
        this.location = location;
        this.logOption = logOption;
        this.timer = timer;
        setCompleteTask(this.location, this.tree);
    }

    public boolean isValidated() {
        if ("plank-bank".equalsIgnoreCase(getLogOption()) && Globals.isUseGoldPerTask()) {
            return reachedGoldLimit() || reachedAllGold() || reachedLevel() || reachedTime();
        }
        if ("plank-bank".equalsIgnoreCase(getLogOption()) && Globals.isUseAllGold()) {
            return reachedAllGold() || reachedLevel() || reachedTime();
        }
        return reachedLevel() || reachedTime();
    }

    private boolean reachedAllGold() {
        RSItem[] goldArray = Workable.getAllGold();

        if (goldArray.length > 0) {
            int currentInventoryGold = goldArray[0].getStack();
            return Gold.getGoldTotalBank() == 0 && currentInventoryGold < Workable.OAK_FEE;
        }

        return Workable.getAllGold().length == 0 && Gold.getGoldTotalBank() == 0;
    }

    private boolean reachedGoldLimit() {
        return Gold.getGoldSpentTotal() >= Gold.calculateActualGoldRegex(Gold.getGoldRegex());
    }

    private boolean reachedTime() {
        return getTime() != null && getTime().isValidated();
    }

    private boolean reachedLevel() {
        if (getUntilLevel() == 0 || getUntilLevel() < 0) {
            return false;
        }
        return Progressive.generateWoodcuttingLevel() >= getUntilLevel();
    }

    public boolean shouldPlankThenBank() {
        return getLogOption().toLowerCase().contentEquals("plank-bank");
    }

    public boolean shouldFletchThenBank() {
        return getLogOption().toLowerCase().contentEquals("fletch-bank");
    }

    public boolean shouldFletchThenDrop() {
        return getLogOption().toLowerCase().contentEquals("fletch-drop");
    }

    public boolean shouldBank() {
        return getLogOption().toLowerCase().contentEquals("bank");
    }

    public boolean shouldDrop() {
        return getLogOption().toLowerCase().contentEquals("drop");
    }

    public boolean shouldPickupNest() {
        return Globals.isPickUpBirdNest();
    }

    public boolean shouldUpgradeAxe() {
        return Globals.isUpgradeAxe();
    }

    public boolean shouldWorldHop() {
        return Globals.isWorldHop();
    }

    public void setCompleteTask(String location, String tree) {
        final String complete_location =
                location.concat(" ")
                        .concat(tree)
                        .toLowerCase();

        switch (complete_location) {
            case "seers' village magic":
            case "seers' village magic tree": {
                this.setActualLocation(Location.SEERS_VILLAGE_MAGICS);
                this.setTree("Magic tree");
                this.setBankLocation(RunescapeBank.CAMELOT);
            }
            break;
            case "north upper level redwood": {
                this.setActualLocation(Location.REDWOOD_NORTH_UPPER_LEVEL);
                this.setBankLocation(RunescapeBank.WOODCUTTING_GUILD);
            }
            break;
            case "north lower level redwood": {
                this.setActualLocation(Location.REDWOOD_NORTH);
                this.setBankLocation(RunescapeBank.WOODCUTTING_GUILD);
            }
            break;
            case "south lower level redwood": {
                this.setActualLocation(Location.REDWOOD_SOUTH);
                this.setBankLocation(RunescapeBank.WOODCUTTING_GUILD);
            }
            break;
            case "south upper level redwood": {
                this.setActualLocation(Location.REDWOOD_SOUTH_UPPER_LEVEL);
                this.setBankLocation(RunescapeBank.WOODCUTTING_GUILD);
            }
            break;
            case "woodcutting guild oak": {
                this.setActualLocation(Location.WOODCUTTING_GUILD_OAKS);
                this.setBankLocation(RunescapeBank.WOODCUTTING_GUILD);
            }
            break;
            case "woodcutting guild maple":
            case "woodcutting guild maple tree": {
                this.setActualLocation(Location.WOODCUTTING_GUILD_MAPLES);
                this.setTree("Maple tree");
                this.setBankLocation(RunescapeBank.WOODCUTTING_GUILD);
            }
            break;
            case "woodcutting guild willow": {
                this.setActualLocation(Location.WOODCUTTING_GUILD_WILLOWS);
                this.setBankLocation(RunescapeBank.WOODCUTTING_GUILD);
            }
            break;
            case "woodcutting guild magic":
            case "woodcutting guild magic tree": {
                this.setActualLocation(Location.WOODCUTTING_GUILD_MAGICS);
                this.setTree("Magic tree");
                this.setBankLocation(RunescapeBank.WOODCUTTING_GUILD);
            }
            break;
            case "woodcutting guild yew": {
                this.setActualLocation(Location.WOODCUTTING_GUILD_YEWS);
                this.setBankLocation(RunescapeBank.WOODCUTTING_GUILD);
            }
            break;
            case "draynor yew": {
                this.setActualLocation(Location.DRAYNOR_YEWS);
                this.setBankLocation(RunescapeBank.DRAYNOR);
            }
            break;
            case "draynor willow": {
                this.setActualLocation(Location.DRAYNOR_WILLOWS);
                this.setBankLocation(RunescapeBank.DRAYNOR);
            }
            break;
            case "falador yew": {
                this.setActualLocation(Location.FALADOR_YEWS);
                this.setBankLocation(RunescapeBank.FALADOR_EAST);
            }
            break;
            case "edgeville yew": {
                this.setActualLocation(Location.EDGEVILLE_YEWS);
                this.setBankLocation(RunescapeBank.EDGEVILLE);
            }
            break;
            case "varrock west tree": {
                this.setActualLocation(Location.VARROCK_WEST_TREES);
                this.setBankLocation(RunescapeBank.VARROCK_WEST);
            }
            break;
            case "varrock west oak": {
                this.setActualLocation(Location.VARROCK_WEST_OAKS);
                this.setBankLocation(RunescapeBank.VARROCK_WEST);
            }
            break;
            case "varrock palace oak": {
                this.setActualLocation(Location.VARROCK_PALACE_OAKS);
                this.setBankLocation(RunescapeBank.GRAND_EXCHANGE);
            }
            break;
            case "varrock palace yew": {
                this.setActualLocation(Location.VARROCK_PALACE_YEWS);
                this.setBankLocation(RunescapeBank.GRAND_EXCHANGE);
            }
            break;
            case "port sarim willow": {
                this.setActualLocation(Location.PORT_SARIM_WILLOWS);
                this.setBankLocation(RunescapeBank.DRAYNOR);
            }
            break;
            case "isle of souls teak":
            case "isle of souls mahogany": {
                this.setActualLocation(Location.ISLE_OF_SOULS);
                this.setBankLocation(RunescapeBank.ISLE_OF_SOULS);
            }
            break;
            case "falador east oak": {
                this.setActualLocation(Location.FALADOR_EAST_OAKS);
                this.setBankLocation(RunescapeBank.FALADOR_EAST);
            }
            break;
            case "catherby yew": {
                this.setActualLocation(Location.CATHERBY_YEWS);
                this.setBankLocation(RunescapeBank.CATHERBY);
            }
            break;
            case "catherby willow": {
                this.setActualLocation(Location.CATHERBY_WILLOWS);
                this.setBankLocation(RunescapeBank.CATHERBY);
            }
            break;
            case "seers' village yew": {
                this.setActualLocation(Location.SEERS_VILLAGE_YEWS);
                this.setBankLocation(RunescapeBank.CAMELOT);
            }
            break;
            case "seers' village willow": {
                this.setActualLocation(Location.SEERS_VILLAGE_WILLOWS);
                this.setBankLocation(RunescapeBank.CAMELOT);
            }
            break;
            case "seers' village maple":
            case "seers' village maple tree": {
                this.setActualLocation(Location.SEERS_VILLAGE_MAPLES);
                this.setTree("Maple tree");
                this.setBankLocation(RunescapeBank.CAMELOT);
            }
            break;
            case "lumbridge castle tree": {
                this.setActualLocation(Location.LUMBRIDGE_CASTLE_TREES);
                this.setBankLocation(RunescapeBank.LUMBRIDGE_TOP);
            }
            break;
            case "sorcerer's tower magic":
            case "sorcerer's tower magic tree": {
                this.setActualLocation(Location.SORCERERS_TOWER);
                this.setBankLocation(RunescapeBank.CAMELOT);
                this.setTree("Magic tree");
            }
            case "grand exchange tree": {
                this.setActualLocation(Location.GRAND_EXCHANGE_TREES);
                this.setBankLocation(RunescapeBank.GRAND_EXCHANGE);
            }
            break;
            case "seers' village trees": {
                this.setActualLocation(Location.SEERS_VILLAGE_TREES);
                this.setBankLocation(RunescapeBank.CAMELOT);
            }
            break;
        }
    }

    public String getTree() {
        return tree;
    }

    public void setTree(String tree) {
        this.tree = tree;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLogOption() {
        return logOption;
    }

    public void setLogOption(String logOption) {
        this.logOption = logOption;
    }

    public int getUntilLevel() {
        return untilLevel;
    }

    public void setUntilLevel(int untilLevel) {
        this.untilLevel = untilLevel;
    }

    public Location getActualLocation() {
        return actualLocation;
    }

    public void setActualLocation(Location actualLocation) {
        this.actualLocation = actualLocation;
    }

    public RunescapeBank getBankLocation() {
        return bankLocation;
    }

    public void setBankLocation(RunescapeBank bankLocation) {
        this.bankLocation = bankLocation;
    }

    public TimeElapse getTime() {
        return timer;
    }

    public void setTime(TimeElapse timer) {
        this.timer = timer;
    }

    @Override
    public String toString() {
        return "Task{" +
                "tree='" + tree + '\'' +
                ", location='" + location + '\'' +
                ", logOption='" + logOption + '\'' +
                ", untilLevel=" + untilLevel +
                ", timer=" + timer +
                ", bankLocation=" + bankLocation +
                ", actualLocation=" + actualLocation +
                '}';
    }
}
