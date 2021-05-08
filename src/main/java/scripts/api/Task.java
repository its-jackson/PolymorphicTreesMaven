package scripts.api;

import scripts.dax_api.api_lib.models.RunescapeBank;

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

    public Task() {
    }

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
        if ("plank-bank".equalsIgnoreCase(getLogOption()) && Globals.useGoldPerTask) {
            return reachedGoldLimit()
                    || reachedLevel()
                    || reachedTime()
                    ;
        }
        if ("plank-bank".equalsIgnoreCase(getLogOption()) && Globals.useAllGold) {
            return reachedAllGold()
                    || reachedLevel()
                    || reachedTime()
                    ;
        }
        return reachedLevel()
                || reachedTime()
                ;
    }

    private boolean reachedAllGold() {
        return Workable.getAllGold().length == 0 && Gold.getGoldTotalBank() != -1 && Gold.getGoldTotalBank() < 250;
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
        return Globals.pickUpBirdNest;
    }

    public boolean shouldUpgradeAxe() {
        return Globals.upgradeAxe;
    }

    public boolean shouldWorldHop() {
        return Globals.worldHop;
    }

    public void setCompleteTask(String location, String tree) {
        final String complete_location =
                location
                        .concat(" ")
                        .concat(tree)
                        .toLowerCase();

        switch (complete_location) {
            case "seers' village magic", "seers' village magic tree" -> {
                this.setActualLocation(Location.SEERS_VILLAGE_MAGICS);
                this.setTree("Magic tree");
                this.setBankLocation(RunescapeBank.CAMELOT);
            }
            case "north upper level redwood" -> {
                this.setActualLocation(Location.REDWOOD_NORTH_UPPER_LEVEL);
                this.setBankLocation(RunescapeBank.WOODCUTTING_GUILD);
            }
            case "north lower level redwood" -> {
                this.setActualLocation(Location.REDWOOD_NORTH);
                this.setBankLocation(RunescapeBank.WOODCUTTING_GUILD);
            }
            case "south lower level redwood" -> {
                this.setActualLocation(Location.REDWOOD_SOUTH);
                this.setBankLocation(RunescapeBank.WOODCUTTING_GUILD);
            }
            case "south upper level redwood" -> {
                this.setActualLocation(Location.REDWOOD_SOUTH_UPPER_LEVEL);
                this.setBankLocation(RunescapeBank.WOODCUTTING_GUILD);
            }
            case "woodcutting guild oak" -> {
                this.setActualLocation(Location.WOODCUTTING_GUILD_OAKS);
                this.setBankLocation(RunescapeBank.WOODCUTTING_GUILD);
            }
            case "woodcutting guild maple", "woodcutting guild maple tree" -> {
                this.setActualLocation(Location.WOODCUTTING_GUILD_MAPLES);
                this.setTree("Maple tree");
                this.setBankLocation(RunescapeBank.WOODCUTTING_GUILD);
            }
            case "woodcutting guild willow" -> {
                this.setActualLocation(Location.WOODCUTTING_GUILD_WILLOWS);
                this.setBankLocation(RunescapeBank.WOODCUTTING_GUILD);
            }
            case "woodcutting guild magic", "woodcutting guild magic tree" -> {
                this.setActualLocation(Location.WOODCUTTING_GUILD_MAGICS);
                this.setTree("Magic tree");
                this.setBankLocation(RunescapeBank.WOODCUTTING_GUILD);
            }
            case "woodcutting guild yew" -> {
                this.setActualLocation(Location.WOODCUTTING_GUILD_YEWS);
                this.setBankLocation(RunescapeBank.WOODCUTTING_GUILD);
            }
            case "draynor yew" -> {
                this.setActualLocation(Location.DRAYNOR_YEWS);
                this.setBankLocation(RunescapeBank.DRAYNOR);
            }
            case "draynor willow" -> {
                this.setActualLocation(Location.DRAYNOR_WILLOWS);
                this.setBankLocation(RunescapeBank.DRAYNOR);
            }
            case "falador yew" -> {
                this.setActualLocation(Location.FALADOR_YEWS);
                this.setBankLocation(RunescapeBank.FALADOR_EAST);
            }
            case "edgeville yew" -> {
                this.setActualLocation(Location.EDGEVILLE_YEWS);
                this.setBankLocation(RunescapeBank.EDGEVILLE);
            }
            case "varrock west tree" -> {
                this.setActualLocation(Location.VARROCK_WEST_TREES);
                this.setBankLocation(RunescapeBank.VARROCK_WEST);
            }
            case "varrock west oak" -> {
                this.setActualLocation(Location.VARROCK_WEST_OAKS);
                this.setBankLocation(RunescapeBank.VARROCK_WEST);
            }
            case "varrock palace oak" -> {
                this.setActualLocation(Location.VARROCK_PALACE_OAKS);
                this.setBankLocation(RunescapeBank.GRAND_EXCHANGE);
            }
            case "varrock palace yew" -> {
                this.setActualLocation(Location.VARROCK_PALACE_YEWS);
                this.setBankLocation(RunescapeBank.GRAND_EXCHANGE);
            }
            case "port sarim willow" -> {
                this.setActualLocation(Location.PORT_SARIM_WILLOWS);
                this.setBankLocation(RunescapeBank.DRAYNOR);
            }
            case "isle of souls teak", "isle of souls mahogany" -> {
                this.setActualLocation(Location.ISLE_OF_SOULS);
                this.setBankLocation(RunescapeBank.ISLE_OF_SOULS);
            }
            case "falador east oak" -> {
                this.setActualLocation(Location.FALADOR_EAST_OAKS);
                this.setBankLocation(RunescapeBank.FALADOR_EAST);
            }
            case "catherby yew" -> {
                this.setActualLocation(Location.CATHERBY_YEWS);
                this.setBankLocation(RunescapeBank.CATHERBY);
            }
            case "catherby willow" -> {
                this.setActualLocation(Location.CATHERBY_WILLOWS);
                this.setBankLocation(RunescapeBank.CATHERBY);
            }
            case "seers' village yew" -> {
                this.setActualLocation(Location.SEERS_VILLAGE_YEWS);
                this.setBankLocation(RunescapeBank.CAMELOT);
            }
            case "seers' village willow" -> {
                this.setActualLocation(Location.SEERS_VILLAGE_WILLOWS);
                this.setBankLocation(RunescapeBank.CAMELOT);
            }
            case "seers' village maple", "seers' village maple tree" -> {
                this.setActualLocation(Location.SEERS_VILLAGE_MAPLES);
                this.setTree("Maple tree");
                this.setBankLocation(RunescapeBank.CAMELOT);
            }
            case "tar swamp sulliuscep" -> {
                this.setActualLocation(Location.TAR_SWAMP);
                this.setBankLocation(RunescapeBank.FOSSIL_ISLAND);
            }
            case "sorcerer's tower magic", "sorcerer's tower magic tree" -> {
                this.actualLocation = Location.SORCERERS_TOWER;
                this.bankLocation = RunescapeBank.CAMELOT;
                this.tree = "Magic tree";
            }
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
        return this.tree + "--" + this.location + "--" + this.logOption + "--" + this.untilLevel + "--" + this.actualLocation + "--" + this.timer;
    }
}
