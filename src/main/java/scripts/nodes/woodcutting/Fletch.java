package scripts.nodes.woodcutting;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Skills;
import org.tribot.api2007.types.RSInterfaceChild;
import org.tribot.api2007.types.RSInterfaceMaster;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSItemDefinition;
import scripts.api.*;
import scripts.api.antiban.AntiBan;
import scripts.dax_api.shared.helpers.BankHelper;
import scripts.dax_api.walker_engine.interaction_handling.InteractionHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

/**
 * Purpose of class: Fletch the task's logs into the best viable fletching option that is available to the player.
 * Jackson Johnson (Polymorphic)
 *
 * Updated 11/04/2021 - Added null safe checks to all methods and cached all return values.
 *
 * Updated 11/05/2021 - Changed naming convention for final variables.
 */

public class Fletch extends Node implements Workable {

    private final HashMap<String, Integer> bowLevels = getMappedBowLevels();
    private final HashMap<String, Integer> arrowLevels = getMappedArrowLevels();

    private final Bank bankNode = new Bank();
    private final Drop dropNode = new Drop();

    @Override
    public void execute(Task task) {
        final long startTime = System.currentTimeMillis();

        debug("Sleeping " + Workable.sleep(Globals.getWaitTimes(), AntiBan.getHumanFatigue()));

        // fetch all logs inside the player's inventory
        final RSItem[] logs = Workable.getAllLogs();

        // calculate best fletching option such as shortbow or longbow etc
        final String calculateBestFletchingOption = calculateBestFletchingOption(Skills.SKILLS.FLETCHING.getActualLevel(), logs);

        if (calculateBestFletchingOption != null) {
            // if interface is already open, click the optimal fletching option
            final boolean firstClickResult = clickFletchingOption(calculateBestFletchingOption);
            // if the interface wasn't open, then proceed to run
            if (!firstClickResult) {
                // use the knife on log
                debug("Utilizing knife");
                if (useKnifeOnLog(logs)) {
                    General.sleep(1000, 1200);
                    // select the best fletching option
                    final boolean finalClickResult = clickFletchingOption(calculateBestFletchingOption);
                    if (finalClickResult) {
                        completeFletchingTask(task);
                    }
                }
            } else {
                completeFletchingTask(task);
            }
        } else {
            debug("Invalid level: " + getWorker().getPlayerFletchingLevel());
            // bank the logs because the player's fletching level isn't
            //  high enough to fletch the logs in the inventory
            if (task.shouldFletchThenBank()) {
                if (getBankNode().validate(task)) {
                    getBankNode().execute(task);
                }
            } else {
                if (getDropNode().validate(task)) {
                    getDropNode().execute(task);
                }
            }
        }

        AntiBan.generateTrackers((int) (System.currentTimeMillis() - startTime), false);

        switch (task.getLogOption().toLowerCase()) {
            case "fletch-bank": {
                if (!(Workable.getAllLogs().length > 0)) {
                    // no logs inside inventory, then bank
                    debug("Fletching complete");
                    if (getBankNode().validate(task)) {
                        getBankNode().execute(task);
                    }
                }
            }
            break;
            case "fletch-drop": {
                if (!(Workable.getAllLogs().length > 0)) {
                    // no logs inside inventory, then drop
                    debug("Fletching complete");
                    if (getDropNode().validate(task)) {
                        getDropNode().execute(task);
                    }
                }
            }
        }
    }

    @Override
    public boolean validate(Task task) {
        if (Inventory.isFull() && Workable.inventoryContainsKnife()) {
            if (task.shouldFletchThenBank()) {
                final String fletchingOption = calculateBestFletchingOption(Progressive.generateFletchingLevel(), Workable.getAllLogs());
                if (fletchingOption != null && fletchingOption.contains("arrow shafts")) {
                    return true;
                }
                if (BankHelper.isInBank()) {
                    return true;
                }
            }
            return task.shouldFletchThenDrop();
        }
        return false;
    }

    @Override
    public void debug(String status) {
        String format = ("[Fletch Control] ");
        Globals.setState(status);
        General.println(format.concat(status));
    }

    private boolean useKnifeOnLog(RSItem[] logs) {
        final RSItem[] knives = Inventory.find(Workable.KNIFE);

        if (!(logs.length > 0 && knives.length > 0)) {
            return false;
        }

        if (InteractionHelper.click(knives[0], "Use")) {
            // if knife click result successful, click a log
            General.sleep(200, 400);
            final Optional<RSItem> log = Arrays.stream(logs)
                    .findAny();
            // a log will always be present no matter what, so we don't have to check if present.
            return InteractionHelper.click(log.get());
        } else {
            return false;
        }
    }

    private String calculateBestFletchingOption(int fletchingLevel, RSItem[] logs) {
        String option;
        String[] greyList;

        if (logs != null && logs.length > 0 && fletchingLevel > 0) {
            final String log_name = Arrays.stream(logs)
                    .map(RSItem::getDefinition)
                    .filter(Objects::nonNull)
                    .map(RSItemDefinition::getName)
                    .findFirst()
                    .orElse("");

            switch (log_name.toLowerCase()) {
                case "logs": {
                    greyList = new String[]{"Longbow", "Shortbow", "15 arrow shafts"};

                    final Optional<String> longbowKey = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[0]))
                            .findFirst();

                    final Optional<String> shortbowKey = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[1]))
                            .findFirst();

                    final Optional<String> arrowShaftKey = getMapArrows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[2]))
                            .findFirst();

                    if (longbowKey.isPresent() && shortbowKey.isPresent() && arrowShaftKey.isPresent()) {
                        if (fletchingLevel >= getMapBows().get(longbowKey.get())) {
                            option = longbowKey.get();
                            return option;
                        } else if (fletchingLevel >= getMapBows().get(shortbowKey.get())) {
                            option = shortbowKey.get();
                            return option;
                        } else if (fletchingLevel >= getMapArrows().get(arrowShaftKey.get())) {
                            option = arrowShaftKey.get();
                            return option;
                        } else {
                            return null;
                        }
                    }
                }
                break;
                case "oak logs": {
                    greyList = new String[]{"Oak longbow", "Oak shortbow", "30 arrow shafts"};

                    final Optional<String> longbowKey = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[0]))
                            .findFirst();

                    final Optional<String> shortbowKey = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[1]))
                            .findFirst();

                    final Optional<String> arrowShaftKey = getMapArrows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[2]))
                            .findFirst();

                    if (longbowKey.isPresent() && shortbowKey.isPresent() && arrowShaftKey.isPresent()) {
                        if (fletchingLevel >= getMapBows().get(longbowKey.get())) {
                            option = longbowKey.get();
                            return option;
                        } else if (fletchingLevel >= getMapBows().get(shortbowKey.get())) {
                            option = shortbowKey.get();
                            return option;
                        } else if (fletchingLevel >= getMapArrows().get(arrowShaftKey.get())) {
                            option = arrowShaftKey.get();
                            return option;
                        } else {
                            return null;
                        }
                    }
                }
                break;
                case "willow logs": {
                    greyList = new String[]{"Willow longbow", "Willow shortbow", "45 arrow shafts"};

                    final Optional<String> longbowKey = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[0]))
                            .findFirst();

                    final Optional<String> shortbowKey = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[1]))
                            .findFirst();

                    final Optional<String> arrowShaftKey = getMapArrows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[2]))
                            .findFirst();

                    if (longbowKey.isPresent() && shortbowKey.isPresent() && arrowShaftKey.isPresent()) {
                        if (fletchingLevel >= getMapBows().get(longbowKey.get())) {
                            option = longbowKey.get();
                            return option;
                        } else if (fletchingLevel >= getMapBows().get(shortbowKey.get())) {
                            option = shortbowKey.get();
                            return option;
                        } else if (fletchingLevel >= getMapArrows().get(arrowShaftKey.get())) {
                            option = arrowShaftKey.get();
                            return option;
                        } else {
                            return null;
                        }
                    }
                }
                break;
                case "maple logs": {
                    greyList = new String[]{"Maple longbow", "Maple shortbow", "60 arrow shafts"};

                    final Optional<String> longbowKey = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[0]))
                            .findFirst();

                    final Optional<String> shortbowKey = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[1]))
                            .findFirst();

                    final Optional<String> arrowShaftKey = getMapArrows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[2]))
                            .findFirst();

                    if (longbowKey.isPresent() && shortbowKey.isPresent() && arrowShaftKey.isPresent()) {
                        if (fletchingLevel >= getMapBows().get(longbowKey.get())) {
                            option = longbowKey.get();
                            return option;
                        } else if (fletchingLevel >= getMapBows().get(shortbowKey.get())) {
                            option = shortbowKey.get();
                            return option;
                        } else if (fletchingLevel >= getMapArrows().get(arrowShaftKey.get())) {
                            option = arrowShaftKey.get();
                            return option;
                        } else {
                            return null;
                        }
                    }
                }
                break;
                case "yew logs": {
                    greyList = new String[]{"Yew longbow", "Yew shortbow", "75 arrow shafts"};

                    final Optional<String> longbowKey = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[0]))
                            .findFirst();

                    final Optional<String> shortbowKey = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[1]))
                            .findFirst();

                    final Optional<String> arrowShaftKey = getMapArrows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[2]))
                            .findFirst();

                    if (longbowKey.isPresent() && shortbowKey.isPresent() && arrowShaftKey.isPresent()) {
                        if (fletchingLevel >= getMapBows().get(longbowKey.get())) {
                            option = longbowKey.get();
                            return option;
                        } else if (fletchingLevel >= getMapBows().get(shortbowKey.get())) {
                            option = shortbowKey.get();
                            return option;
                        } else if (fletchingLevel >= getMapArrows().get(arrowShaftKey.get())) {
                            option = arrowShaftKey.get();
                            return option;
                        } else {
                            return null;
                        }
                    }
                }
                break;
                case "magic logs": {
                    greyList = new String[]{"Magic longbow", "Magic shortbow", "90 arrow shafts"};

                    final Optional<String> longbowKey = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[0]))
                            .findFirst();

                    final Optional<String> shortbowKey = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[1]))
                            .findFirst();

                    final Optional<String> arrowShaftKey = getMapArrows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[2]))
                            .findFirst();

                    if (longbowKey.isPresent() && shortbowKey.isPresent() && arrowShaftKey.isPresent()) {
                        if (fletchingLevel >= getMapBows().get(longbowKey.get())) {
                            option = longbowKey.get();
                            return option;
                        } else if (fletchingLevel >= getMapBows().get(shortbowKey.get())) {
                            option = shortbowKey.get();
                            return option;
                        } else if (fletchingLevel >= getMapArrows().get(arrowShaftKey.get())) {
                            option = arrowShaftKey.get();
                            return option;
                        } else {
                            return null;
                        }
                    }
                }
                break;
                case "redwood logs": {
                    greyList = new String[]{"Redwood shield", "105 arrow shafts"};

                    final Optional<String> shieldKey = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[0]))
                            .findFirst();

                    final Optional<String> arrowShaftKey = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[1]))
                            .findFirst();

                    if (shieldKey.isPresent() && arrowShaftKey.isPresent()) {
                        if (fletchingLevel >= getMapBows().get(shieldKey.get())) {
                            option = shieldKey.get();
                            return option;
                        } else if (fletchingLevel >= getMapBows().get(arrowShaftKey.get())) {
                            option = arrowShaftKey.get();
                            return option;
                        } else {
                            return null;
                        }
                    }
                }
                break;
                case "teak logs": {
                    greyList = new String[]{"Teak stock"};
                    if (fletchingLevel >= 46) {
                        option = greyList[0];
                        return option;
                    }
                }
                break;
                case "mahogany logs": {
                    greyList =  new String[]{"Mahogany stock"};
                    if (fletchingLevel >= 61) {
                        option = greyList[0];
                        return option;
                    }
                }
                break;
            }
        }

        return null;
    }

    private boolean clickFletchingOption(String fletchingResult) {
        final RSInterfaceMaster masterInterface = Interfaces.get(270);

        boolean clickResult = false;

        if (masterInterface != null) {
            final Optional<RSInterfaceChild> fletchOptionInterface = Arrays.stream(masterInterface.getChildren())
                    .filter(rsInterfaceChild -> rsInterfaceChild.getComponentName().contains(fletchingResult))
                    .findFirst();

            if (fletchOptionInterface.isPresent()) {
                clickResult = fletchOptionInterface
                        .map(rsInterfaceChild -> rsInterfaceChild.click("Make"))
                        .orElse(false);
            }
        }

        return Timing.waitCondition(Workable::isWorking, General.random(1200, 2000)) && clickResult;
    }

    private void completeFletchingTask(Task task) {
        while (Workable.isWorking()) {
            General.sleep(1200, 1600);
            debug("Fletching " + task.getTree().toLowerCase() + " logs");
            AntiBan.timedActions();
            if (task.isValidated()) {
                debug("Task complete");
                break;
            }
        }
    }

    public HashMap<String, Integer> getMapBows() {
        return bowLevels;
    }

    public HashMap<String, Integer> getMapArrows() {
        return arrowLevels;
    }

    public Bank getBankNode() {
        return bankNode;
    }

    public Drop getDropNode() {
        return dropNode;
    }
}
