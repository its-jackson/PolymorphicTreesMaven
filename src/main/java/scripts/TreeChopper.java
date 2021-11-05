package scripts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.tribot.api.util.Screenshots;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Login;
import org.tribot.api2007.util.ThreadSettings;
import org.tribot.script.interfaces.*;
import scripts.api.*;
import scripts.api.antiban.AntiBan;
import scripts.api.antiban.Fatigue;
import scripts.dax_api.api_lib.DaxWalker;
import scripts.dax_api.api_lib.models.DaxCredentials;
import scripts.dax_api.api_lib.models.DaxCredentialsProvider;

import org.tribot.api.Timing;
import org.tribot.api2007.Skills;

import org.tribot.api.General;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import scripts.fluffeeapi.ArgumentUtilities;
import scripts.fluffeeapi.Utilities;
import scripts.nodes.woodcutting.*;

import scripts.polyGui.GUIFX;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * Purpose of class: Main class (Script); execute scripts.nodes.
 * Author: Jackson Johnson (Polymorphic~TRiBot)
 * Date: Aug 30th, 2020
 */

@ScriptManifest(
        authors = {"Polymorphic"},
        category = "Woodcutting",
        name = "Polymorphic Auto Woodcutter",
        version = 1.07,
        description = "# Polymorphic's Auto Woodcutter v1.07\n",
        gameMode = 1)

public class TreeChopper extends Script implements
        Arguments,
        Painting,
        MessageListening07,
        Starting,
        Ending,
        Breaking {

    volatile boolean runScript = true;

    private boolean hasArguments;

    public static int logCount;
    public static int levelCount;

    private GUIFX guifx;
    private URL fxml;

    private int startXP = 0;
    private boolean gameOptimizeComplete;

    private Fatigue workerFatigue;

    private final List<Node> node_list = new ArrayList<>();
    private final Image img = getImage("https://jacksonjohnson.ca/polywoodcutter/paint.png");
    private final RenderingHints aa = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    private final Font font = new Font("Verdana", Font.BOLD, 12);
    private final Font font_status = new Font("Verdana", Font.PLAIN, 12);
    private final Font font_level = new Font("Verdana", Font.PLAIN, 10);
    private final Font font_progress_bar = new Font("Verdana", Font.PLAIN, 24);
    private final long start_time = System.currentTimeMillis();

    private final Color mouse_colour = new Color(0, 153, 76); // custom green
    private final Color paint_main_colour = new Color(0, 100, 0); // dark green
    private final Color paint_secondary_colour = new Color(0, 0, 0); // black

    @Override
    public void run() {
        if (!hasArguments) {
            try {
                setFxml(new URL("https://jacksonjohnson.ca/gui/polywoodcutter.fxml"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            setGuifx(new GUIFX(fxml));

            getGuifx().show();

            while (getGuifx().isOpen()) {
                sleep(500);
            }

            System.out.println("Gui Completed.\n Initializing Script");
        }

        DaxWalker.setCredentials(new DaxCredentialsProvider() {
            @Override
            public DaxCredentials getDaxCredentials() {
                return new DaxCredentials("sub_JK3knXqxVGZtGR", "74aa47de-1cb1-4ee1-a8c9-5bae53c70b22");
            }
        });

        System.out.println("Planking Gold - " + Gold.getGoldRegex());

        General.useAntiBanCompliance(true);
        System.out.println("ABC2 - " + General.useAntiBanCompliance());

        AntiBan.setMicroSleep(Globals.isAntiBanMicroSleep());
        System.out.println("AFK Micro Sleep - " + Globals.isAntiBanMicroSleep());

        AntiBan.setHumanFatigue(Globals.isHumanFatigue());
        System.out.println("Human Fatigue - " + Globals.isHumanFatigue());

        AntiBan.setPrintDebug(true);
        System.out.println("Print Debug - " + AntiBan.getPrintDebug());

        while (isRunScript()) {
            sleep(100, 300);
            if (Login.getLoginState() == Login.STATE.INGAME && Globals.isStart()) {
                if (!isGameOptimizeComplete()) {
                    Collections.addAll(
                            getNodeList(),
                            new Chop(),
                            new Plank(),
                            new FetchAxe(),
                            new FetchGold(),
                            new FetchKnife(),
                            new SpecialAttack(),
                            new BirdNest(),
                            new LogDisposal(),
                            new WorldHop(),
                            new Walk()
                    );
                    setStartXP(Skills.getXP(Skills.SKILLS.WOODCUTTING));
                    Camera.setCameraAngle(100);
                    Workable.optimizeGame();
                    //Utilities.toggleRoofs(true);
                    ThreadSettings.get().setClickingAPIUseDynamic(true);
                    // set worker fatigue
                    setWorkerFatigue(new Fatigue());
                    // print all subset elements
                    getWorkerFatigue()
                            .getFatigueMultipleSubset()
                            .forEach(General::println);
                    // set the current multiple
                    getWorkerFatigue().setCurrentFatigueMultiple(getWorkerFatigue().getFatigueMultipleSubset().iterator().next());
                    // set global multiple
                    Globals.setCurrentFatigueMultiple(getWorkerFatigue().getCurrentFatigueMultiple());
                    // set worker state
                    setGameOptimizeComplete(true);
                }
                do {
                    for (final Task task : Globals.getTasks()) {
                        // tell user task is complete
                        General.println("New task! " + task.toString().toLowerCase());
                        // reset task gold
                        Gold.resetGoldSpentTotal();
                        Gold.resetGoldTotalBank();
                        // reset working objects
                        Globals.setCurrentWorkingTree(null);
                        Globals.setNextWorkingTree(null);
                        // reset the start time for each task
                        if (task.getTime() != null) {
                            task.getTime().setStartTime(System.currentTimeMillis());
                        }
                        // switch the location for each task
                        switch (task.getActualLocation()) {
                            case EDGEVILLE_YEWS:
                                Globals.setTreeFactor(20);
                                break;
                            case SEERS_VILLAGE_MAGICS:
                            case VARROCK_WEST_OAKS:
                            case VARROCK_WEST_TREES:
                            case SEERS_VILLAGE_MAPLES:
                                Globals.setTreeFactor(15);
                                break;
                            case SORCERERS_TOWER:
                            case VARROCK_PALACE_OAKS:
                            case VARROCK_PALACE_YEWS:
                            case REDWOOD_NORTH:
                            case REDWOOD_SOUTH:
                            case REDWOOD_NORTH_UPPER_LEVEL:
                            case REDWOOD_SOUTH_UPPER_LEVEL:
                                Globals.setTreeFactor(10);
                                break;
                        }
                        // continue looping each task and node until task is complete
                        while (!task.isValidated()) {
                            for (final Node node : getNodeList()) {
                                if (node.validate(task)) {
                                    node.getWorker().setCompleteWorkerState();
                                    if (getWorkerFatigue().shouldIncrementFatigue(AntiBan.getABCCount())) {
                                        Globals.setCurrentFatigueMultiple(getWorkerFatigue().getCurrentFatigueMultiple());
                                        General.println(getWorkerFatigue().toString().toLowerCase());
                                        General.println("[Worker] New fatigue level: " + Globals.getCurrentFatigueMultiple());
                                    }
                                    General.println("Current multiple: " + Globals.getCurrentFatigueMultiple());
                                    General.println(node.getWorker().toString().toLowerCase());
                                    node.execute(task);
                                    if (node instanceof Chop) {
                                        // TODO
                                    }
                                }
                                sleep(General.random(100, 300)); // time in between executing nodes
                            }
                        }
                        // tell user task is complete
                        General.println(task.toString().toLowerCase());
                        General.println("Task complete. Please be patient");
                    }
                    // once all tasks complete, shuffle and repeat
                    if (Globals.isOnRepeatShuffle()) {
                        Collections.shuffle(Globals.getTasks());
                        General.println("Shuffling tasks");
                    }
                }
                while (Globals.isOnRepeatShuffle() || Globals.isOnRepeat());
                // if not on repeat, throw RunTime exception, game over!
                end();
            }
        }
    }

    /**
     * Paint/gg
     *
     * @param graphics Draw the gg.
     */
    @Override
    public void onPaint(Graphics graphics) {
        Graphics2D gg = (Graphics2D) graphics;
        gg.setRenderingHints(getAa());

        long runTime = Timing.timeFromMark(getStartTime());
        long logsPerHour = (long) (getLogCount() * (3600000 / (double) runTime));
        int actualLevel = Skills.getActualLevel(Skills.SKILLS.WOODCUTTING);
        int gainedXP = Skills.getXP(Skills.SKILLS.WOODCUTTING) - getStartXP();
        long xpPerHour = (long) (gainedXP * (3600000 / (double) runTime));
        int percentToNextLevel = Skills.getPercentToNextLevel(Skills.SKILLS.WOODCUTTING);

        if (Globals.getCurrentWorkingTree() != null) {
            graphics.setColor(getMouseColour());
            graphics.drawPolygon(Globals.getCurrentWorkingTree().getModel().getEnclosedArea());
        }
        if (Globals.getNextWorkingTree() != null) {
            graphics.setColor(Color.GREEN);
            graphics.drawPolygon(Globals.getNextWorkingTree().getModel().getEnclosedArea());
        }

        gg.drawImage(getImg(), -1, 311, null);
        gg.setFont(getFont());
        gg.setColor(getPaintMainColour());
        gg.fillRect(240, 408, percentToNextLevel * 250 / 100, 25);
        gg.drawRect(240, 408, 250, 25);

        gg.setFont(getFontProgressBar());
        gg.setColor(getPaintSecondaryColour());

        if (actualLevel >= 99) {
            String name = General.getTRiBotUsername();
            gg.setColor(getPaintMainColour());
            gg.fillRect(240, 408, 250, 25);
            gg.setColor(getPaintSecondaryColour());
            gg.setFont(getFontStatus());
            gg.drawString("Master " + name, 300, 425);
        } else {
            gg.drawString(percentToNextLevel + "% TL", 325, 430);
        }

        gg.setFont(getFont());
        gg.setColor(getPaintMainColour()); // white font colour
        gg.drawString("Time Running:", 15, 375); // runtime
        gg.drawString("Logs Chopped:", 15, 390); // log count
        gg.drawString("Logs/Hour:", 15, 405); // logs hr
        gg.drawString("XP Gained:", 15, 420); // gained xp
        gg.drawString("XP/Hour:", 15, 435); // gained xp
        gg.drawString("Status:", 15, 450); // script state
        gg.drawString("Version: 1.07", 15, 465); // script state

        gg.setFont(getFontStatus());
        gg.setColor(getPaintSecondaryColour()); // white font colour
        gg.drawString(Timing.msToString(runTime), 120, 376); // runtime
        gg.drawString((getLogCount()) + " Logs", 120, 391); // log count
        gg.drawString(logsPerHour + " Logs/Hour", 120, 406); // logs hr
        gg.drawString(gainedXP + " XP", 120, 421); // gained xp
        gg.drawString(xpPerHour + " XP/Hour", 120, 436); // gained xp hour
        gg.drawString(Globals.getState(), 120, 451); // script state

        gg.setFont(getFontLevel());
        gg.drawString("Currently Level " + actualLevel + ", " + getLevelCount() + " Levels Gained.", 265, 465);
    }

    private void end() {
        setRunScript(false);
        throw new RuntimeException("Script over! " + General.getTRiBotUsername() + " thanks for using my script!");
    }

    /**
     * Return an image from the internet.
     *
     * @param url The address belonging to the image.
     * @return The image; otherwise null.
     */
    private Image getImage(String url) {
        try {
            return ImageIO.read(new URL(url));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Count the amount of logs collected.
     *
     * @param message The message that is received for keeping count
     */
    @Override
    public void serverMessageReceived(String message) {
        message = message.toLowerCase();

        if (message.contains("you get some")) {
            incrementLogCount();
            AntiBan.incrementResourcesWon();
        }

        if (message.contains("congratulations")) {
            incrementLevelCount();
            if (Screenshots.take(true))
                General.println("Congratulations on leveling up, screenshot taken");
        }
    }

    @Override
    public void onStart() {
        // create anti-ban instance
        AntiBan.create();
    }

    @Override
    public void onEnd() {
        // close/destroy anti-ban instance
        AntiBan.destroy();
        // call end method
        end();
    }

    @Override
    public void onBreakStart(long l) {
        Globals.setCurrentWorkingTree(null);
        Globals.setState("Taking break " + Timing.msToString(l));
    }

    @Override
    public void onBreakEnd() {
        Globals.setState("Break complete");
    }

    @Override
    public void passArguments(HashMap<String, String> hashMap) {
        HashMap<String, String> parsedArguments = ArgumentUtilities.get(hashMap);

        if (parsedArguments.size() > 0) {
            if (parsedArguments.containsKey("goldPerTask")) {
                Gold.setGoldRegex(parsedArguments.get("goldPerTask"));
                System.out.println("Gold Per Task - " + Gold.getGoldRegex());
            } else if (parsedArguments.containsKey("useAllGold")) {
                Globals.setUseAllGold(true);
                Globals.setUseGoldPerTask(false);
                System.out.println("Use All Gold - " + Globals.isUseAllGold());
            }

            if (parsedArguments.containsKey("repeat")) {
                Globals.setOnRepeat(Boolean.parseBoolean(parsedArguments.get("repeat")));
                System.out.println("Repeat - " + Globals.isOnRepeat());
            } else if (parsedArguments.containsKey("repeatShuffle")) {
                Globals.setOnRepeatShuffle(Boolean.parseBoolean(parsedArguments.get("repeatShuffle")));
                System.out.println("Repeat Shuffle - " + Globals.isOnRepeatShuffle());
            }

            if (parsedArguments.containsKey("useInfernalAxe")) {
                Globals.setUpgradeAxe(false);
                Globals.setSpecialAxe(Boolean.parseBoolean(parsedArguments.get("useInfernalAxe")));
                System.out.println("Use Infernal Axe - " + Globals.isSpecialAxe());
            }

            if (parsedArguments.containsKey("birdNest")) {
                Globals.setPickUpBirdNest(Boolean.parseBoolean(parsedArguments.get("birdNest")));
                System.out.println("Bird Nests - " + Globals.isPickUpBirdNest());
            }

            if (parsedArguments.containsKey("worldHopNoTrees")) {
                Globals.setWorldHopNoTreesAvailable(Boolean.parseBoolean(parsedArguments.get("worldHopNoTrees")));
                System.out.println("World Hop Trees - " + Globals.isWorldHopNoTreesAvailable());
            }

            if (parsedArguments.containsKey("worldHopPlayers")) {
                Globals.setWorldHop(Boolean.parseBoolean(parsedArguments.get("worldHopPlayers")));
                System.out.println("World Hop Players - " + Globals.isWorldHop());
            }

            if (parsedArguments.containsKey("afkMicroSleep")) {
                Globals.setAntiBanMicroSleep(Boolean.parseBoolean(parsedArguments.get("afkMicroSleep")));
                System.out.println("AFK Micro Sleep - " + Globals.isAntiBanMicroSleep());
            }

            if (parsedArguments.containsKey("replicateFatigue")) {
                Globals.setHumanFatigue(Boolean.parseBoolean(parsedArguments.get("replicateFatigue")));
                System.out.println("Replicate Fatigue - " + Globals.isHumanFatigue());
            }


            if (parsedArguments.containsKey("worldHopFactor")) {
                try {
                    int integer = Integer.parseInt(parsedArguments.get("worldHopFactor"));
                    Globals.setWorldHopFactor(integer);
                    System.out.println("World Hop Factor - " + Globals.getWorldHopFactor());
                } catch (NumberFormatException ex) {
                    System.out.println("Incorrect number format.");
                }
            }

            if (parsedArguments.containsKey("settingsFile")) {
                File settingsFile = new File(Utilities.getPolymorphicScriptsDirectory() + parsedArguments.get("settingsFile"));
                System.out.println(Utilities.getPolymorphicScriptsDirectory() + parsedArguments.get("settingsFile"));
                if (settingsFile.exists() && !settingsFile.isDirectory()) {
                    parseJson(settingsFile);
                    this.hasArguments = true;
                    Globals.setStart(true);
                }
            }
        }
    }

    public void parseJson(File file) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        try {
            FileReader fileReader = new FileReader(file);
            Task[] tasks = gson.fromJson(fileReader, Task[].class);
            for (Task t : tasks) {
                System.out.println(t);
                Globals.getTasks().add(t);
            }
        } catch (FileNotFoundException exception) {
            System.out.println(exception.getLocalizedMessage());
        }
    }

    public boolean isRunScript() {
        return runScript;
    }

    public void setRunScript(boolean runScript) {
        this.runScript = runScript;
    }

    public static int getLogCount() {
        return logCount;
    }

    public static void setLogCount(int logCount) {
        TreeChopper.logCount = logCount;
    }

    public static int getLevelCount() {
        return levelCount;
    }

    public static void setLevelCount(int levelCount) {
        TreeChopper.levelCount = levelCount;
    }

    public GUIFX getGuifx() {
        return guifx;
    }

    public void setGuifx(GUIFX guifx) {
        this.guifx = guifx;
    }

    public URL getFxml() {
        return fxml;
    }

    public void setFxml(URL fxml) {
        this.fxml = fxml;
    }

    public int getStartXP() {
        return startXP;
    }

    public void setStartXP(int startXP) {
        this.startXP = startXP;
    }

    public Fatigue getWorkerFatigue() {
        return workerFatigue;
    }

    public void setWorkerFatigue(Fatigue workerFatigue) {
        this.workerFatigue = workerFatigue;
    }

    public boolean isGameOptimizeComplete() {
        return gameOptimizeComplete;
    }

    public void setGameOptimizeComplete(boolean gameOptimizeComplete) {
        this.gameOptimizeComplete = gameOptimizeComplete;
    }

    public List<Node> getNodeList() {
        return node_list;
    }

    public Image getImg() {
        return img;
    }

    public RenderingHints getAa() {
        return aa;
    }

    public Font getFont() {
        return font;
    }

    public Font getFontStatus() {
        return font_status;
    }

    public Font getFontLevel() {
        return font_level;
    }

    public Font getFontProgressBar() {
        return font_progress_bar;
    }

    public long getStartTime() {
        return start_time;
    }

    public Color getMouseColour() {
        return mouse_colour;
    }

    public Color getPaintMainColour() {
        return paint_main_colour;
    }

    public Color getPaintSecondaryColour() {
        return paint_secondary_colour;
    }

    public void incrementLogCount() {
        logCount++;
    }

    public void incrementLevelCount() {
        levelCount++;
    }
}
