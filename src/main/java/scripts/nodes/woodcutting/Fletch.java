package scripts.nodes.woodcutting;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Inventory;
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
 * Jackson Johnson (Polymorphic)
 */

public class Fletch extends Node implements Workable {
    private final long start_time = System.currentTimeMillis();

    private final HashMap<String, Integer> map_bows = getMappedBowLevels();
    private final HashMap<String, Integer> map_arrows = getMappedArrowLevels();

    private final Bank bank_node = new Bank();
    private final Drop drop_node = new Drop();

    @Override
    public void execute(Task task) {
        debug("Sleeping " + Workable.sleep(Globals.getWaitTimes(), AntiBan.getHumanFatigue()));

        // fetch all logs inside the player's inventory
        final RSItem[] logs = Workable.getAllLogs();

        // calculate best fletching option such as shortbow or longbow etc
        final String best_fletching_option = calculateBestFletchingOption(getWorker().getPlayerFletchingLevel(), logs);

        if (best_fletching_option != null) {
            // if interface is already open, click the optimal fletching option
            final boolean first_click_result = clickFletchingOption(best_fletching_option);
            // if the interface wasn't open, then proceed to run
            if (!first_click_result) {
                // use the knife on log
                debug("Utilizing knife");
                if (useKnifeOnLog(logs)) {
                    General.sleep(1000, 1200);
                    // select the best fletching option
                    final boolean final_click_result = clickFletchingOption(best_fletching_option);
                    if (final_click_result) {
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

        switch (task.getLogOption().toLowerCase()) {
            case "fletch-bank" -> {
                if (!(Workable.getAllLogs().length > 0)) {
                    // no logs inside inventory, then bank
                    debug("Fletching complete");
                    if (getBankNode().validate(task)) {
                        getBankNode().execute(task);
                    }
                }
            }
            case "fletch-drop" -> {
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
                String fletchingOption = calculateBestFletchingOption(Progressive.generateFletchingLevel(), Workable.getAllLogs());
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
            Arrays.stream(logs)
                    .findAny()
                    .ifPresent(InteractionHelper::click);
            return true;
        } else {
            return false;
        }
    }

    private String calculateBestFletchingOption(int fletchingLevel, RSItem[] logs) {
        String option = null;

        String[] greyList;

        if (logs != null && logs.length > 0 && fletchingLevel > 0) {
            final String log_name = Arrays.stream(logs)
                    .map(RSItem::getDefinition)
                    .filter(Objects::nonNull)
                    .map(RSItemDefinition::getName)
                    .findFirst()
                    .orElse("");

            switch (log_name.toLowerCase()) {
                case "logs" -> {
                    greyList = new String[]{"Longbow", "Shortbow", "15 arrow shafts"};

                    final Optional<String> longbow_key = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[0]))
                            .findFirst();

                    final Optional<String> shortbow_key = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[1]))
                            .findFirst();

                    final Optional<String> arrow_shaft_key = getMapArrows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[2]))
                            .findFirst();

                    if (longbow_key.isPresent() && shortbow_key.isPresent() && arrow_shaft_key.isPresent()) {
                        if (fletchingLevel >= getMapBows().get(longbow_key.get())) {
                            option = longbow_key.get();
                            return option;
                        } else if (fletchingLevel >= getMapBows().get(shortbow_key.get())) {
                            option = shortbow_key.get();
                            return option;
                        } else if (fletchingLevel >= getMapArrows().get(arrow_shaft_key.get())) {
                            option = arrow_shaft_key.get();
                            return option;
                        } else {
                            return option;
                        }
                    }
                }
                case "oak logs" -> {
                    greyList = new String[]{"Oak longbow", "Oak shortbow", "30 arrow shafts"};

                    final Optional<String> longbow_key = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[0]))
                            .findFirst();

                    final Optional<String> shortbow_key = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[1]))
                            .findFirst();

                    final Optional<String> arrow_shaft_key = getMapArrows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[2]))
                            .findFirst();

                    if (longbow_key.isPresent() && shortbow_key.isPresent() && arrow_shaft_key.isPresent()) {
                        if (fletchingLevel >= getMapBows().get(longbow_key.get())) {
                            option = longbow_key.get();
                            return option;
                        } else if (fletchingLevel >= getMapBows().get(shortbow_key.get())) {
                            option = shortbow_key.get();
                            return option;
                        } else if (fletchingLevel >= getMapArrows().get(arrow_shaft_key.get())) {
                            option = arrow_shaft_key.get();
                            return option;
                        } else {
                            return option;
                        }
                    }

                }
                case "willow logs" -> {
                    greyList = new String[]{"Willow longbow", "Willow shortbow", "45 arrow shafts"};

                    final Optional<String> longbow_key = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[0]))
                            .findFirst();

                    final Optional<String> shortbow_key = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[1]))
                            .findFirst();

                    final Optional<String> arrow_shaft_key = getMapArrows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[2]))
                            .findFirst();

                    if (longbow_key.isPresent() && shortbow_key.isPresent() && arrow_shaft_key.isPresent()) {
                        if (fletchingLevel >= getMapBows().get(longbow_key.get())) {
                            option = longbow_key.get();
                            return option;
                        } else if (fletchingLevel >= getMapBows().get(shortbow_key.get())) {
                            option = shortbow_key.get();
                            return option;
                        } else if (fletchingLevel >= getMapArrows().get(arrow_shaft_key.get())) {
                            option = arrow_shaft_key.get();
                            return option;
                        } else {
                            return option;
                        }
                    }
                }
                case "maple logs" -> {
                    greyList = new String[]{"Maple longbow", "Maple shortbow", "60 arrow shafts"};

                    final Optional<String> longbow_key = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[0]))
                            .findFirst();

                    final Optional<String> shortbow_key = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[1]))
                            .findFirst();

                    final Optional<String> arrow_shaft_key = getMapArrows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[2]))
                            .findFirst();

                    if (longbow_key.isPresent() && shortbow_key.isPresent() && arrow_shaft_key.isPresent()) {
                        if (fletchingLevel >= getMapBows().get(longbow_key.get())) {
                            option = longbow_key.get();
                            return option;
                        } else if (fletchingLevel >= getMapBows().get(shortbow_key.get())) {
                            option = shortbow_key.get();
                            return option;
                        } else if (fletchingLevel >= getMapArrows().get(arrow_shaft_key.get())) {
                            option = arrow_shaft_key.get();
                            return option;
                        } else {
                            return option;
                        }
                    }

                }
                case "yew logs" -> {
                    greyList = new String[]{"Yew longbow", "Yew shortbow", "75 arrow shafts"};

                    final Optional<String> longbow_key = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[0]))
                            .findFirst();

                    final Optional<String> shortbow_key = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[1]))
                            .findFirst();

                    final Optional<String> arrow_shaft_key = getMapArrows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[2]))
                            .findFirst();

                    if (longbow_key.isPresent() && shortbow_key.isPresent() && arrow_shaft_key.isPresent()) {
                        if (fletchingLevel >= getMapBows().get(longbow_key.get())) {
                            option = longbow_key.get();
                            return option;
                        } else if (fletchingLevel >= getMapBows().get(shortbow_key.get())) {
                            option = shortbow_key.get();
                            return option;
                        } else if (fletchingLevel >= getMapArrows().get(arrow_shaft_key.get())) {
                            option = arrow_shaft_key.get();
                            return option;
                        } else {
                            return option;
                        }
                    }
                }
                case "magic logs" -> {
                    greyList = new String[]{"Magic longbow", "Magic shortbow", "90 arrow shafts"};

                    final Optional<String> longbow_key = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[0]))
                            .findFirst();

                    final Optional<String> shortbow_key = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[1]))
                            .findFirst();

                    final Optional<String> arrow_shaft_key = getMapArrows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[2]))
                            .findFirst();

                    if (longbow_key.isPresent() && shortbow_key.isPresent() && arrow_shaft_key.isPresent()) {
                        if (fletchingLevel >= getMapBows().get(longbow_key.get())) {
                            option = longbow_key.get();
                            return option;
                        } else if (fletchingLevel >= getMapBows().get(shortbow_key.get())) {
                            option = shortbow_key.get();
                            return option;
                        } else if (fletchingLevel >= getMapArrows().get(arrow_shaft_key.get())) {
                            option = arrow_shaft_key.get();
                            return option;
                        } else {
                            return option;
                        }
                    }
                }
                case "redwood logs" -> {
                    greyList = new String[]{"Redwood longbow", "Redwood shortbow", "105 arrow shafts"};

                    final Optional<String> longbow_key = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[0]))
                            .findFirst();

                    final Optional<String> shortbow_key = getMapBows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[1]))
                            .findFirst();

                    final Optional<String> arrow_shaft_key = getMapArrows()
                            .keySet()
                            .stream()
                            .filter(s -> s.equals(greyList[2]))
                            .findFirst();

                    if (longbow_key.isPresent() && shortbow_key.isPresent() && arrow_shaft_key.isPresent()) {
                        if (fletchingLevel >= getMapBows().get(longbow_key.get())) {
                            option = longbow_key.get();
                            return option;
                        } else if (fletchingLevel >= getMapBows().get(shortbow_key.get())) {
                            option = shortbow_key.get();
                            return option;
                        } else if (fletchingLevel >= getMapArrows().get(arrow_shaft_key.get())) {
                            option = arrow_shaft_key.get();
                            return option;
                        } else {
                            return option;
                        }
                    }
                }
                case "teak logs" -> {
                    greyList = new String[]{"Teak stock"};
                    if (fletchingLevel >= 46) {
                        option = greyList[0];
                        return option;
                    }
                }
                case "mahogany logs" -> {
                    greyList =  new String[]{"Mahogany stock"};
                    if (fletchingLevel >= 61) {
                        option = greyList[0];
                        return option;
                    }
                }
            }
        }

        return option;
    }

    private boolean clickFletchingOption(String fletchingResult) {
        final RSInterfaceMaster master_interface = Interfaces.get(270);

        boolean clickResult = false;

        if (master_interface != null) {
            final Optional<RSInterfaceChild> fletch_option_interface = Arrays.stream(master_interface.getChildren())
                    .filter(rsInterfaceChild -> rsInterfaceChild.getComponentName().contains(fletchingResult))
                    .findFirst();

            if (fletch_option_interface.isPresent()) {
                clickResult = fletch_option_interface
                        .map(rsInterfaceChild -> rsInterfaceChild.click("Make"))
                        .orElse(false);
            }
        }

        return Timing.waitCondition(Workable::isWorking, General.random(1200, 2000)) || clickResult;
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
        return map_bows;
    }

    public HashMap<String, Integer> getMapArrows() {
        return map_arrows;
    }

    public Bank getBankNode() {
        return bank_node;
    }

    public Drop getDropNode() {
        return drop_node;
    }

    public long getStartTime() {
        return start_time;
    }
}
