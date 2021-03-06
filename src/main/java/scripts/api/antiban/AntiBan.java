package scripts.api.antiban;

import org.tribot.api.Clicking;
import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api.interfaces.Clickable;
import org.tribot.api.interfaces.Positionable;
import org.tribot.api.util.abc.ABCProperties;
import org.tribot.api.util.abc.ABCUtil;
import org.tribot.api.util.abc.preferences.OpenBankPreference;
import org.tribot.api.util.abc.preferences.WalkingPreference;
import org.tribot.api2007.*;
import org.tribot.api2007.types.RSObject;
import scripts.api.Globals;

import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.TimeUnit;

/**
 * The AntiBan class provides an easy way to implement Anti-ban Compliance 2.0
 * into any script.
 *
 * @author Adapted from Starfox
 * https://github.com/frankkeuning/chopper/blob/master/AntiBan.java
 *
 * @Version 4.0 (1/24/2016 3:37 PM GTM+0)
 *
 * @Version 4.1 (12/13/2020 9:10 PM EST) - Polymorphic
 *  --Replaced all deprecated code with updated code.
 *
 * @Version 4.2 (01/01/2021 4:20 PM EST) - Polymorphic
 * -- Updated checkXP method
 * -- If checkXP is called then game tab will reset to inventory once checkXP is finished executing.
 *
 * @Version 4.3 (01/14/2021 3:25 PM EST) - Polymorphic
 * -- Added ABCCount for every ant-iban task performed
 */

public final class AntiBan {

    /**
     * The object that stores the seeds
     */
    private static ABCUtil abc;

    /**
     * The boolean flag that determines whether or not to print debug information
     */
    private static boolean print_debug;

    /**
     * The boolean flag that determines whether or not to afk micro sleep
     */
    private static boolean micro_sleep;

    /**
     * The boolean flag that determines whether or not to afk micro sleep
     */
    private static boolean human_fatigue;

    /**
     * The amount of resources you have won
     */
    private static int resources_won;

    /**
     * The amount of resources you have lost
     */
    private static int resources_lost;

    /**
     * The % run energy to activate run at
     */
    private static int run_at;

    /**
     * The % hp to eat food at
     */
    private static int eat_at;

    /**
     * The bool that determines whether or not we should be hovering.
     */
    private static boolean should_hover;

    /**
     * The bool that determines whether or not we should be opening the menu.
     */
    private static boolean should_open_menu;

    /**
     * The time stamp at which we were last under attack.
     */
    private static long last_under_attack_time;

    /**
     * The bool that determines whether or not we should reaction sleep
     */
    private static boolean enableReactionSleep;

    /**
     * The amount of times abc has performed a check
     */
    private static int abc_count;

    /**
     * The start time
     */
    private static long abc_start_time;

    /**
     * The afk timer
     */
    private static long abc_afk_timer;

    /**
     * Static initialization block. By default, the use of general anti-ban
     * compliance is set to be true.
     */
    static {
        abc = new ABCUtil();
        abc_count = 0;
        abc_start_time = System.currentTimeMillis();
        abc_afk_timer = General.randomLong(300000, 1200000); // 5-20 minutes
        print_debug = false;
        micro_sleep = false;
        human_fatigue = false;
        resources_won = 0;
        resources_lost = 0;
        run_at = abc.generateRunActivation();
        eat_at = abc.generateEatAtHP();
        should_hover = abc.shouldHover();
        should_open_menu = abc.shouldOpenMenu() && abc.shouldHover();
        last_under_attack_time = 0;
        enableReactionSleep = true;
        General.useAntiBanCompliance(true);
    }

    /**
     * Prevent instantiation of this class
     */
    private AntiBan(){}

    /**
     * Destroys the current instance of ABCUtil and stops all anti-ban threads.
     * Call this at the end of your script
     */
    public static void destroy() {
        abc.close();
        abc = null;
    }

    /**
     * Gets the ABCCount int
     *
     * @return The ABCCount int
     */
    public static int getABCCount() {
        return abc_count;
    }

    /**
     * Set the ABCCount int
     *
     * @param abc_count The int to set
     */
    public static void setABCCount(int abc_count) {
        AntiBan.abc_count = abc_count;
    }

    public static void incrementABCCount() {
        abc_count++;
    }

    /**
     * Creates a new instance of ABCUtil and sets the instance to be equal to
     * the current ABCUtil.
     */
    public static void create() {
        abc = new ABCUtil();
    }

    /**
     * Gets the ABCUtil object.
     *
     * @Return The ABCUtil object.
     */
    public static ABCUtil getABCUtil() {
        return abc;
    }

    /**
     * Gets the energy % to run at
     *
     * @return The energy % to run at
     */
    public static int getRunAt() {
        return run_at;
    }

    /**
     * Gets the % hp to eat at
     *
     * @return The hitpoints % to eat at
     */
    public static int getEatAt() {
        return eat_at;
    }

    /**
     * Gets the bool to hover or not a certain entity
     *
     * @return True if should hover, false otherwise
     */
    public static boolean getShouldHover() {
        return should_hover;
    }

    /**
     * Gets the bool should open menu
     *
     * @return True if should open menu, false otherwise
     */
    public static boolean getShouldOpenMenu() {
        return should_open_menu;
    }

    /**
     * Gets the last time the character was under attack, in milliseconds
     *
     * @return The last time under attack
     */
    public static long getLastUnderAttackTime() {
        return last_under_attack_time;
    }

    /**
     * Gets the properties for ABCUtil.
     *
     * @Return The properties.
     */
    public static ABCProperties getProperties() {
        return getABCUtil().getProperties();
    }

    /**
     * Gets the waiting time for the next action we want to perform.
     *
     * @Return The waiting time.
     */
    public static int getWaitingTime() {
        return getProperties().getWaitingTime();
    }

    /**
     * Gets the reaction time that we should sleep for before performing our
     * next action. Examples:
     * <ul>
     * <li>Reacting to when our character stops fishing. The response time will
     * be used before we move on to the next fishing spot, or before we walk to
     * the bank.</li>
     * <li>Reacting to when our character stops mining. The response time will
     * be used before we move on to the next rock, or before we walk to the
     * bank.</li>
     * <li>Reacting to when our character kills our target NPC. The response
     * time will be used before we attack our next target, or before we walk to
     * the bank.</li>
     * </ul>
     *
     * @Return The reaction time.
     */
    public static int getReactionTime() {
        return getABCUtil().generateReactionTime();
    }

    /**
     * Sets the print_debug bool to be equal to the specified bool. By calling
     * this method and providing a true value, other methods in this class will
     * start printing debug information into the system print stream when they
     * are executed.
     *
     * @param state The bool to set.
     */
    public static void setPrintDebug(boolean state) {
        print_debug = state;
    }

    public static boolean getPrintDebug() {
        return print_debug;
    }

    /**
     * Sets the micro_sleep bool to be equal to the specified bool. By calling
     * this method and providing a true value, other methods in this class will
     * start afk micro sleeping when they are executed.
     *
     * @param state The bool to set.
     */
    public static void setMicroSleep(boolean state) {
        micro_sleep = state;
    }

    public static boolean getMicroSleep() {
        return micro_sleep;
    }

    public static boolean getHumanFatigue() {
        return human_fatigue;
    }

    public static void setHumanFatigue(boolean state) {
        human_fatigue = state;
    }

    /**
     * Gets the amount of resources won.
     *
     * @Return The amount of resources won.
     */
    public static int getResourcesWon() {
        return resources_won;
    }

    /**
     * Gets the amount of resources lost.
     *
     * @Return The amount of recourses lost.
     */
    public static int getResourcesLost() {
        return resources_lost;
    }

    /**
     * Sets the amount of resources won to the specified amount.
     *
     * @param amount The amount to set.
     */
    public static void setResourcesWon(int amount) {
        resources_won = amount;
    }

    /**
     * Sets the amount of resources lost to the specified amount.
     *
     * @param amount The amount to set.
     */
    public static void setResourcesLost(int amount) {
        resources_lost = amount;
    }

    /**
     * Increments the amount of resources won by 1.
     */
    public static void incrementResourcesWon() {
        resources_won++;
    }

    /**
     * Increments the amount of resources lost by 1.
     */
    public static void incrementResourcesLost() {
        resources_lost++;
    }

    /**
     * Sets the last_under_attack_time to be equal to the specified time stamp.
     *
     * @param time_stamp The time stamp.
     */
    public static void setLastUnderAttackTime(long time_stamp) {
        last_under_attack_time = time_stamp;
    }

    public static long getAbcStartTime() {
        return abc_start_time;
    }

    public static void setAbcStartTime(long abc_start_time) {
        AntiBan.abc_start_time = abc_start_time;
    }

    public static long getAbcAfkTimer() {
        return abc_afk_timer;
    }

    public static void setAbcAfkTimer(long abc_afk_timer) {
        AntiBan.abc_afk_timer = abc_afk_timer;
    }

    private static int average(List<Integer> times) {
        final OptionalDouble stream = times
                .stream()
                .mapToInt(Integer::intValue)
                .average();

        return stream.isPresent() ? (int) stream.getAsDouble() : 0;
    }

    public static void sleep(List<Integer> waitTimes, boolean humanFatigue) {
        int reactionTime;

        if (waitTimes.isEmpty()) {
            AntiBan.generateTrackers(General.random(800, 1400), false);
        } else {
            AntiBan.generateTrackers(average(waitTimes), false);
        }

        if (humanFatigue) {
            reactionTime = (int) (AntiBan.getReactionTime() * Globals.getCurrentFatigueMultiple());
        } else {
            reactionTime = AntiBan.getReactionTime();
        }
        debug(String.format("Sleeping %sms", reactionTime));
        waitTimes.add(reactionTime);
        AntiBan.sleepReactionTime(reactionTime);
    }

    /**
     * Sleeps for the reaction time generated by ABCUtil. Note that this method
     * uses a special sleeping method from ABCUtil that allows the ABC2
     * background thread to interrupt the sleep when needed.
     */
    public static void sleepReactionTime(final int time) {
        if (!enableReactionSleep) {
            return;
        }
        try {
            getABCUtil().sleep(time);
            incrementABCCount();
        } catch (InterruptedException e) {
            debug("Sleep has been skipped");
        }
    }

    /**
     * Generates the trackers for ABCUtil. Call this only after successfully
     * completing an action that has a dynamic wait time for the next action.
     *
     * @param estimated_wait
     *            The estimated wait time (in milliseconds) before the next
     *            action occurs.
     * @param fixed_wait
     *            True if estimated wait is fixed, false otherwise
     */
    public static void generateTrackers(int estimated_wait, boolean fixed_wait) {
        final ABCProperties properties = getProperties();

        properties.setHovering(should_hover);
        properties.setMenuOpen(should_open_menu);
        properties.setWaitingFixed(fixed_wait);
        properties.setWaitingTime(estimated_wait);

        properties.setUnderAttack(Combat.isUnderAttack() || (Timing.currentTimeMillis() - last_under_attack_time < 2000));

        getABCUtil().generateTrackers();
    }

    /**
     * Resets the should_hover bool to match the ABCUtil value. This method
     * should be called after successfully clicking an entity.
     */
    public static void resetShouldHover() {
        should_hover = getABCUtil().shouldHover();
    }

    /**
     * Resets the should_open_menu bool to match the ABCUtil value. This method
     * should be called after successfully clicking an entity.
     */
    public static void resetShouldOpenMenu() {
        should_open_menu = getABCUtil().shouldOpenMenu() && getABCUtil().shouldHover();
    }

    /**
     * Randomly moves the camera. Happens only if the time tracker for camera
     * movement is ready.
     *
     * @Return True if the action was performed, false otherwise.
     */
    public static boolean moveCamera() {
        if (getABCUtil().shouldRotateCamera()) {
            incrementABCCount();
            if (print_debug) {
                debug("Rotated camera");
            }
            getABCUtil().rotateCamera();
            return true;
        }
        return false;
    }

    /**
     * Checks the exp of the skill being trained. Happens only if the time
     * tracker for checking exp is ready.
     *
     * @Return True if the exp was checked, false otherwise.
     */
    public static boolean checkXp() {
        if (getABCUtil().shouldCheckXP()) {
            incrementABCCount();
            if (print_debug) {
                debug("Checked xp");
            }
            getABCUtil().checkXP();
            General.sleep(1000, 7000);
            // reset tab to inventory once xp is checked.
            if (!GameTab.getOpen().equals(GameTab.TABS.INVENTORY)) {
                debug("Resetting game tab");
                GameTab.open(GameTab.TABS.INVENTORY);
            }
            return true;
        }
        return false;
    }

    /**
     * Picks up the mouse. Happens only if the time tracker for picking up the
     * mouse is ready.
     *
     * @Return True if the mouse was picked up, false otherwise.
     */
    public static boolean pickUpMouse() {
        if (getABCUtil().shouldPickupMouse()) {
            incrementABCCount();
            if (print_debug) {
                debug("Picked up mouse");
            }
            getABCUtil().pickupMouse();
            return true;
        }
        return false;
    }

    /**
     * Navigates the mouse off game window and mimics de-focusing the window.
     * Happens only if the time tracker for leaving the game is ready.
     *
     * @Return True if the mouse left the game window, false otherwise.
     */
    public static boolean leaveGame() {
        if (getABCUtil().shouldLeaveGame()) {
            incrementABCCount();
            if (print_debug) {
                debug("Left game");
            }
            getABCUtil().leaveGame();
            return true;
        }
        return false;
    }

    /**
     * Examines an entity near your player. Happens only if the time tracker for
     * examining an entity is ready.
     *
     * @Return True if an entity was examined, false otherwise.
     */
    public static boolean examineEntity() {
        if (getABCUtil().shouldExamineEntity()) {
            incrementABCCount();
            if (print_debug) {
                debug("Examined entity");
            }
            getABCUtil().examineEntity();
            return true;
        }
        return false;
    }

    /**
     * Right clicks the mouse. Happens only if the time tracker for right
     * clicking the mouse is ready.
     *
     * @Return True if a random spot was right clicked, false otherwise.
     */
    public static boolean rightClick() {
        if (getABCUtil().shouldRightClick()) {
            incrementABCCount();
            if (print_debug) {
                debug("Right clicked");
            }
            getABCUtil().rightClick();
            return true;
        }
        return false;
    }

    /**
     * Moves the mouse. Happens only if the time tracker for moving the mouse is
     * ready.
     *
     * @Return True if the mouse was moved to a random point, false otherwise.
     */
    public static boolean mouseMovement() {
        if (getABCUtil().shouldMoveMouse()) {
            incrementABCCount();
            if (print_debug) {
                debug("Mouse moved");
            }
            getABCUtil().moveMouse();
            return true;
        }
        return false;
    }

    /**
     * Opens up a game tab. Happens only if the time tracker for tab checking is
     * ready.
     *
     * @Return True if the combat tab was checked, false otherwise.
     */
    public static boolean checkTabs() {
        if (getABCUtil().shouldCheckTabs()) {
            incrementABCCount();
            if (print_debug) {
                debug("Tab checked");
            }
            getABCUtil().checkTabs();
            General.sleep(1000, 7000);
            // reset tab to inventory once xp is checked.
            if (!GameTab.getOpen().equals(GameTab.TABS.INVENTORY)) {
                debug("Resetting game tab");
                GameTab.open(GameTab.TABS.INVENTORY);
            }
            return true;
        }
        return false;
    }

    public static boolean moveToAnticipated() {
        if (getABCUtil().shouldMoveToAnticipated()) {
            incrementABCCount();
            if (print_debug) {
                debug("Moved to anticipated");
            }
            getABCUtil().moveMouse();
            return true;
        }
        return false;
    }

    public static boolean afkMicroSleep(Long afk_timer) {
        if (micro_sleep) {
            incrementABCCount();
            final long timer_run = Timing.timeFromMark(getAbcStartTime());
            final long eight_minutes_cut = 450000;
            final int sleep_magic = 40 * getReactionTime();
            if (timer_run >= afk_timer) {
                if (!(sleep_magic >= eight_minutes_cut)) {
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(sleep_magic);
                    debug("AFK " + sleep_magic + " ms");
                    debug("AFK " + minutes + " minutes");
                    Mouse.leaveGame();
                    sleepReactionTime(sleep_magic);
                    setAbcStartTime(System.currentTimeMillis());
                    setAbcAfkTimer(General.randomLong(300000, 1200000));
                    return true;
                } else {
                    debug("AFK has been skipped");
                    setAbcStartTime(System.currentTimeMillis());
                    setAbcAfkTimer(General.randomLong(300000, 1200000));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks all of the actions that are perform with the time tracker; if any
     * are ready, they will be performed.
     */
    public static void timedActions() {
        moveCamera();
        checkXp();
        checkTabs();
        pickUpMouse();
        leaveGame();
        examineEntity();
        rightClick();
        mouseMovement();
        afkMicroSleep(AntiBan.getAbcAfkTimer());
    }

    public static void checkAntiBanTask(RSObject[] objects, RSObject object) {
        shouldHoverObject(objects);
        shouldExamineObject(object);
        timedActions();
    }

    public static void shouldHoverObject(RSObject[] objects) {
        if (getShouldHover()) {
            hoverEntity(objects);
            resetShouldHover();
        }
    }

    public static void shouldExamineObject(RSObject object) {
        if (getShouldOpenMenu()) {
            if (DynamicClicking.clickRSObject(object, 3)) {
                Timing.waitCondition(() -> {
                    General.sleep(20, 60);
                    return ChooseOption.isOpen();
                }, General.random(1200, 2000));
            }
            resetShouldOpenMenu();
        }
    }

    /**
     * Gets the next target that should be interacted with from the specified
     * list of targets.
     *
     * @param targets
     *            The targets to choose from.
     * @param <T>
     *            The generic type.
     * @Return The target to interact with.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Positionable> T selectNextTarget(Positionable[] targets) {
        return (T) getABCUtil().selectNextTarget(targets);
    }

    /**
     * Activates run. No action is taken if run is already enabled or the
     * current run energy is less than the value returned by ABCUtil.
     *
     * @Return True if run was enabled, false otherwise.
     */
    public static boolean activateRun() {
        if (Game.getRunEnergy() >= getRunAt() && !Game.isRunOn()) {
            Options.setRunEnabled(true);
            if (Options.setRunEnabled(true)) {
                incrementABCCount();
                if (print_debug) {
                    debug("Turned run on at " + Game.getRunEnergy() + "%");
                }
                run_at = getABCUtil().generateRunActivation();
                return true;
            }
        }
        return false;
    }

    public static boolean doEat(String option, String name) {
        if (Player.getRSPlayer().getHealthPercent() <= getEatAt()) {
            return eat(option, name);
        }
        return false;
    }

    /**
     * Eats/drinks an item in your inventory with the specified name if your
     * current HP percent is less than or equal to the value generated by
     * ABCUtil. Note that if there is any delay/lag that is longer than 3000
     * milliseconds between the time the food/drink was clicked and when your
     * players HP is changed the tracker will not be reset and you will have to
     * reset it manually.
     *
     * @param option
     *            The option to click the food/drink with (this is normally
     *            "Eat" or "Drink"). Input an empty string to have the method
     *            attempt to find the correct option automatically. Note that
     *            this is not guaranteed to execute properly if an empty string
     *            is inputted.
     * @param name
     *            The name of the food or drink.
     * @Return True if the food/drink was successfully eaten/drank, false
     *         otherwise.
     * @see(java.lang.String, org.tribot.api2007.types.RSItem)
     */
    public static boolean eat(String option, final String name) {
        return eat(option, Inventory.getCount(name));
    }

    /**
     * Eats/drinks an item in your inventory with the specified ID if your
     * current HP percent is less than or equal to the value generated by
     * ABCUtil. Note that if there is any delay/lag that is longer than 3000
     * milliseconds between the time the food/drink was clicked and when your
     * players HP is changed the tracker will not be reset and you will have to
     * reset it manually.
     *
     * @param option
     *            The option to click the food/drink with (this is normally
     *            "Eat" or "Drink"). Input an empty string to have the method
     *            attempt to find the correct option automatically. Note that
     *            this is not guaranteed to execute properly if an empty string
     *            is inputted.
     * @param id
     *            The ID of the food or drink.
     * @Return True if the food/drink was successfully eaten/drank, false
     *         otherwise.
     * @seet(java.lang.String, org.tribot.api2007.types.RSItem)
     */
    public static boolean eat(String option, final int id) {
        return eat(option, Inventory.getCount(id));
    }

    /**
     * Checks to see if the player should switch resources. Note that this
     * method will only return correctly if you have been tracking the resources
     * you have won and lost. Note also that you must create the check time in
     * your script and reset it accordingly. e.g. to check if you should switch
     * resources, you should check the following condition:
     * <code>Timing.currentTimeMillis() >= check_time && AntiBan.shouldSwitchResources()</code>
     *
     * @param player_count
     *            The amount of players gathering resources near you.
     * @Return True if your player should switch resources, false otherwise.
     */
    public static boolean shouldSwitchResources(int player_count) {
        double win_percent = ((double) (resources_won + resources_lost) / (double) resources_won);
        return win_percent < 50.0 && getABCUtil().shouldSwitchResources(player_count);
    }

    /**
     * Sleeps the current thread for the item interaction delay time. This
     * method should be called directly after interacting with an item in your
     * players inventory.
     */
    public static void waitItemInteractionDelay() {
        General.sleep(25, 75);
    }

    /**
     * Sleeps the current thread for the item interaction delay time multiplied
     * by the specified number of iterations. This method can be used to sleep
     * between certain actions that do not have a designated method already
     * assigned to them such as casting spells or clicking interfaces.
     * <p/>
     * This method does not guarantee a static sleep time each iteration.
     *
     * @param iterations
     *            How many times to sleep the item interaction delay time.
     * @see #waitItemInteractionDelay()
     */
    public static final void waitItemInteractionDelay(int iterations) {
        for (int i = 0; i < iterations; i++) {
            waitItemInteractionDelay();
        }
    }

    /**
     * Hovers the entity if applicable.
     *
     * Note that you <i>must</i> reset the tracker yourself after the current
     * Object interaction is finished.
     */
    public static boolean hoverEntity(Clickable[] b) {
        if (should_hover) {
            incrementABCCount();
            if (print_debug) {
                debug("Hovering entity");
            }
            Clicking.hover(b);
            return true;
        }
        return false;
    }

    /**
     * Enable or disable reaction sleeps
     *
     * @param state
     *            The new state
     */
    public static void setEnableReactionSleep(boolean state) {
        enableReactionSleep = state;
    }

    /**
     *
     */
    public static Inventory.DROPPING_METHOD generateDroppingPreference() {
        int code = General.random(1, 2);
        return code == 1 ? Inventory.DROPPING_METHOD.RIGHT_CLICK : Inventory.DROPPING_METHOD.SHIFT;
    }

    /**
     *
     */
    public static Inventory.DROPPING_PATTERN generateDroppingPattern() {
        int code = General.random(1, 4);
        switch (code) {
            case 2: {
                return Inventory.DROPPING_PATTERN.TOP_TO_BOTTOM;
            }
            case 3: {
                return Inventory.DROPPING_PATTERN.TOP_TO_BOTTOM_ZIGZAG;
            }
            case 4: {
                return Inventory.DROPPING_PATTERN.ZIGZAG;
            }
            default: {
                return Inventory.DROPPING_PATTERN.LEFT_TO_RIGHT;
            }
        }
    }

    /**
     * Returns the special attack preference of the player's profile. MAIN_HUD or COMBAT_TAB
     * or both
     *
     * @return SpecialAttackPreference Special attack preference
     */
    public static SpecialAttackPreference generateSpecialAttackPreference() {
        int code = General.random(1, 2);
        return code == 1 ? SpecialAttackPreference.MAIN_HUD : SpecialAttackPreference.COMBAT_TAB;
    }

    /**
     * Returns the walking preference of the player's profile. SCREEN or MINIMAP
     * or both
     *
     * @return WalkingPreference Walking preference
     */
    public static WalkingPreference generateWalkingPreference(int distance) {
        return getABCUtil().generateWalkingPreference(distance);
    }

    /**
     * Returns the bank preference of the player's profile. BANKER or BOOTH
     *
     * @return OpenBankPreference Banking preference
     */
    public static OpenBankPreference generateOpenBankPreference() {
        return getABCUtil().generateOpenBankPreference();
    }

    /**
     * Sends the specified message to the system print stream with the [ABC2]
     * tag.
     *
     * @param message
     *            The message to print.
     */
    private static void debug(Object message) {
        Globals.setState((String) message);
        General.println("[ABC2] " + message);
    }
}
