package scripts;

import org.tribot.api.util.Screenshots;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Login;
import org.tribot.api2007.util.ThreadSettings;
import org.tribot.script.interfaces.*;
import scripts.api.*;
import scripts.api.antiban.AntiBan;
import scripts.dax_api.api_lib.DaxWalker;
import scripts.dax_api.api_lib.models.DaxCredentials;
import scripts.dax_api.api_lib.models.DaxCredentialsProvider;

import org.tribot.api.Timing;
import org.tribot.api2007.Skills;

import org.tribot.api.General;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;

import scripts.nodes.woodcutting.*;

import scripts.polyGui.GUIFX;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
        version = 1.05,
        description = Globals.desc,
        gameMode = 1)

public class TreeChopper extends Script implements
        Arguments,
        Painting,
        MessageListening07,
        Starting,
        Ending,
        Breaking {

    volatile boolean runScript = true;

    private final List<Node> node_list = new ArrayList<>();

    private final FindObject object_finder = new FindObject();

    private GUIFX guifx;
    private URL fxml;

    private final Image IMG = getImage("https://jacksonjohnson.ca/polywoodcutter/paint.png");
    private final RenderingHints aa = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    private final Font FONT = new Font("Verdana", Font.BOLD, 12);
    private final Font FONT_STATUS = new Font("Verdana", Font.PLAIN, 12);
    private final Font FONT_LEVEL = new Font("Verdana", Font.PLAIN, 10);
    private final Font FONT_PROGRESS_BAR = new Font("Verdana", Font.PLAIN, 24);
    private final long START_TIME = System.currentTimeMillis();

    private final Color mouse_colour = new Color(0, 153, 76); // custom green
    private final Color paint_main_colour = new Color(0, 100, 0); // dark green
    private final Color paint_secondary_colour = new Color(0, 0, 0); // black

    private int startXP = 0;
    private boolean gameOptimizeComplete;

    public static int logCount;
    public static int levelCount;

    @Override
    public void run() {
        try {
            fxml = new URL("https://jacksonjohnson.ca/gui/test.fxml");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        guifx = new GUIFX(fxml);
        guifx.show();

        while (guifx.isOpen()) {
            sleep(500);
        }

        System.out.println("Gui Completed.\n Initializing Script");

        DaxWalker.setCredentials(new DaxCredentialsProvider() {
            @Override
            public DaxCredentials getDaxCredentials() {
                return new DaxCredentials("sub_JK3knXqxVGZtGR", "74aa47de-1cb1-4ee1-a8c9-5bae53c70b22");
            }
        });

        General.useAntiBanCompliance(true);
        System.out.println("ABC2 - " + General.useAntiBanCompliance());

        AntiBan.setMicroSleep(Globals.antiBanMicroSleep);
        System.out.println("AFK Micro Sleep - " + Globals.antiBanMicroSleep);

        AntiBan.setHumanFatigue(Globals.humanFatigue);
        System.out.println("Human Fatigue - " + Globals.humanFatigue);

        AntiBan.setPrintDebug(true);
        System.out.println("Print Debug - " + AntiBan.getPrintDebug());

        Collections.addAll(
                node_list,
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

        while (runScript) {
            sleep(100, 300);
            if (Login.getLoginState() == Login.STATE.INGAME && Globals.START) {
                if (!gameOptimizeComplete) {
                    startXP = Skills.getXP(Skills.SKILLS.WOODCUTTING);
                    Camera.setCameraAngle(100);
                    Workable.optimizeGame();
                    ThreadSettings.get().setClickingAPIUseDynamic(true);
                    runObjectFinder(object_finder);
                    gameOptimizeComplete = true;
                }
                do {
                    for (final Task task : Globals.tasks) {
                        // tell user task is complete
                        General.println("New task! " + task.toString().toLowerCase());
                        // reset task gold
                        Gold.resetGoldSpentTotal();
                        Gold.resetGoldTotalBank();
                        // reset working objects
                        Globals.objectsNear = null;
                        Globals.currentWorkingTree = null;
                        Globals.nextWorkingTree = null;
                        // reset the start time for each task
                        if (task.getTime() != null) {
                            task.getTime().setStartTime(System.currentTimeMillis());
                        }
                        // set the object finder for each task
                        object_finder.setTask(task);
                        // switch the location for each task
                        switch (task.getActualLocation()) {
                            case EDGEVILLE_YEWS -> Globals.treeFactor = 20;
                            case SEERS_VILLAGE_MAGICS,
                                    VARROCK_WEST_OAKS,
                                    VARROCK_WEST_TREES,
                                    SEERS_VILLAGE_MAPLES -> Globals.treeFactor = 15;
                            case SORCERERS_TOWER,
                                    VARROCK_PALACE_OAKS,
                                    VARROCK_PALACE_YEWS,
                                    REDWOOD_NORTH,
                                    REDWOOD_SOUTH,
                                    REDWOOD_NORTH_UPPER_LEVEL,
                                    REDWOOD_SOUTH_UPPER_LEVEL -> Globals.treeFactor = 10;
                        }
                        // continue looping each task and node until task is complete
                        while (!task.isValidated()) {
                            for (final Node node : node_list) {
                                if (node.validate(task)) {
                                    node.execute(task);
                                    if (node instanceof Chop) {
                                        // TODO
                                        // destroy tree objects
                                        Globals.nextWorkingTree = null;
                                        Globals.objectsNear = null;
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
                    if (Globals.onRepeatShuffle) {
                        Collections.shuffle(Globals.tasks);
                    }
                }
                while (Globals.onRepeatShuffle || Globals.onRepeat);
                // if not on repeat, throw RunTime exception, game over!
                if (Globals.dontRepeat) {
                    end();
                }
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
        gg.setRenderingHints(aa);

        long runTime = Timing.timeFromMark(START_TIME);
        long logsPerHour = (long) (logCount * (3600000 / (double) runTime));
        int actualLevel = Skills.getActualLevel(Skills.SKILLS.WOODCUTTING);
        int gainedXP = Skills.getXP(Skills.SKILLS.WOODCUTTING) - startXP;
        long xpPerHour = (long) (gainedXP * (3600000 / (double) runTime));
        int percentToNextLevel = Skills.getPercentToNextLevel(Skills.SKILLS.WOODCUTTING);

        if (Globals.currentWorkingTree != null) {
            graphics.setColor(mouse_colour);
            graphics.drawPolygon(Globals.currentWorkingTree.getModel().getEnclosedArea());
        }
        if (Globals.nextWorkingTree != null) {
            graphics.setColor(Color.GREEN);
            graphics.drawPolygon(Globals.nextWorkingTree.getModel().getEnclosedArea());
        }

        gg.drawImage(IMG, 0, 311, null);
        gg.setFont(FONT);
        gg.setColor(paint_main_colour);
        gg.fillRect(240, 408, percentToNextLevel * 250 / 100, 25);
        gg.drawRect(240, 408, 250, 25);

        gg.setFont(FONT_PROGRESS_BAR);
        gg.setColor(paint_secondary_colour);

        if (actualLevel >= 99) {
            String name = General.getTRiBotUsername();
            gg.setColor(paint_main_colour);
            gg.fillRect(240, 408, 250, 25);
            gg.setColor(paint_secondary_colour);
            gg.setFont(FONT_STATUS);
            gg.drawString("Master " + name, 300, 425);
        } else {
            gg.drawString(percentToNextLevel + "% TL", 325, 430);
        }

        gg.setFont(FONT);
        gg.setColor(paint_main_colour); // white font colour
        gg.drawString("Time Running:", 15, 375); // runtime
        gg.drawString("Logs Chopped:", 15, 390); // log count
        gg.drawString("Logs/Hour:", 15, 405); // logs hr
        gg.drawString("XP Gained:", 15, 420); // gained xp
        gg.drawString("XP/Hour:", 15, 435); // gained xp
        gg.drawString("Status:", 15, 450); // script state

        gg.setFont(FONT_STATUS);
        gg.setColor(paint_secondary_colour); // white font colour
        gg.drawString(Timing.msToString(runTime), 120, 376); // runtime
        gg.drawString((logCount) + " Logs", 120, 391); // log count
        gg.drawString(logsPerHour + " Logs/Hour", 120, 406); // logs hr
        gg.drawString(gainedXP + " XP", 120, 421); // gained xp
        gg.drawString(xpPerHour + " XP/Hour", 120, 436); // gained xp hour
        gg.drawString(Globals.STATE, 120, 451); // script state

        gg.setFont(FONT_LEVEL);
        gg.drawString("Currently Level " + actualLevel + ", " + levelCount + " Levels Gained.", 265, 465);
    }

    private void end() {
        this.runScript = false;
        throw new RuntimeException("Game Over! " + General.getTRiBotUsername() + " thanks for playing!");
    }

    private void runObjectFinder(FindObject runnable) {
        new Thread(runnable).start();
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

        String substr = message.substring(0, 12);

        if (substr.equals("you get some") && message.endsWith("logs.")) {
            logCount++;
            AntiBan.incrementResourcesWon();
        }
        if (message.contains("congratulations")) {
            levelCount++;
            if (Screenshots.take(true))
                General.println("Congratulations on leveling up, screenshot taken");
        }
    }

    @Override
    public void onStart() {
        // create anti-ban instance
        AntiBan.create();

        // generate fatigue system variables
        Globals.var1000 = General.random(400, 600);
        Globals.var1001 = General.random(800, 1000);
        Globals.var1002 = General.random(1200, 1400);
    }

    @Override
    public void onEnd() {
        AntiBan.destroy();
    }

    @Override
    public void onBreakStart(long l) {
        Globals.objectsNear = null;
        Globals.currentWorkingTree = null;
        Globals.nextWorkingTree = null;
        Globals.STATE = ("Taking break " + Timing.msToString(l));
    }

    @Override
    public void onBreakEnd() {
        Globals.STATE = ("Break complete");
    }

    @Override
    public void passArguments(HashMap<String, String> hashMap) {

    }
}
