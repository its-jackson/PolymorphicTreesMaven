package scripts.api.sulliuscep;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Player;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.api2007.util.DPathNavigator;
import scripts.api.Location;

import java.util.*;
import java.util.function.Predicate;

public class SulliuscepHelper {
    public final static Predicate<RSObject> thick_vines_filter =
            Filters.Objects.nameContains("vines", "vine")
                    .and(Filters.Objects.actionsEquals("Chop"));

    public final static Predicate<RSObject> vines_filter =
            Filters.Objects.nameEquals("Vines")
                    .and(Filters.Objects.actionsContains("Clear"));

    public final static Predicate<RSObject> sulliuscep_filter =
            Filters.Objects.nameEquals("Sulliuscep")
                    .and(Filters.Objects.actionsContains("Cut"));

    private SulliuscepHelper() {
    }

    public static boolean isThickVines(){
        return Arrays.stream(Location.TAR_SWAMP.getRSArea().getAllTiles())
                .anyMatch(rsTile -> Objects.isAt(rsTile, thick_vines_filter));
    }

    public static boolean isVines(){
        return Arrays.stream(Location.TAR_SWAMP.getRSArea().getAllTiles())
                .anyMatch(rsTile -> Objects.isAt(rsTile, vines_filter));
    }

    public static boolean isSulliuscep(){
        return Arrays.stream(Location.TAR_SWAMP.getRSArea().getAllTiles())
                .anyMatch(rsTile -> Objects.isAt(rsTile, sulliuscep_filter));
    }

    public static boolean isAtSulliuscepPosition(RSTile sulliuscepPosition){
        return Player.getPosition().distanceTo(sulliuscepPosition) < 3;
    }

    public static RSObject[] generateThickVines(RSTile thickVinesTile){
        return isThickVines() ? Objects.getAt(thickVinesTile, thick_vines_filter) : null;
    }

    public static RSObject[] generateVines(RSTile vinesTile){
        return isVines() ? Objects.getAt(vinesTile, vines_filter) : null;
    }

    public static RSObject[] generateSulliuscepObject(){
        return Objects.getAt(Arrays.stream(Location.TAR_SWAMP.getRSArea().getAllTiles())
                        .filter(rsTile -> Objects.isAt(rsTile, sulliuscep_filter))
                        .findAny()
                        .orElseThrow());
    }

    public static SulliuscepPath generateSulliuscepPath(RSTile playerPosition, RSTile sulliuscepPosition){
        return new SulliuscepPath(playerPosition, sulliuscepPosition);
    }

    public static boolean walkSulliuscepPath(SulliuscepPath path, boolean isRotation) {
        final DPathNavigator d_path_navigator = new DPathNavigator();

        final RSTile start_position = path.getStartPosition();
        final RSTile sulliuscep_position = path.getSulliuscepPosition();

        final RSTile[] absolute_path = path.getAbsolutePath();
        final RSTile[] rotation_path = path.getRotationPath();

        RSTile lastTile = null;

        if (isRotation) {
            for (RSTile tile : rotation_path) {
                boolean result = d_path_navigator.traverse(tile);
                if (result){
                    boolean time_result = Timing.waitCondition(() -> {
                        General.sleep(200,300);

                        return Player.getPosition().distanceTo(tile) <= 2;
                    }, General.random(2000,4000));
                    if (time_result) {
                        lastTile = tile;
                    }
                }
            }
        } else {
            for (RSTile tile : absolute_path) {
                boolean result = d_path_navigator.traverse(tile);
                if (result) {
                    boolean time_result = Timing.waitCondition(() -> {
                        General.sleep(200,300);
                        return Player.getPosition().distanceTo(tile) <= 2;
                    }, General.random(2000,4000));
                    if (time_result) {
                        lastTile = tile;
                    }
                }
            }
        }

        return lastTile != null && Player.getPosition().distanceTo(lastTile) <=2 ||
                Player.getPosition().distanceTo(sulliuscep_position) <= 2 && Player.getPosition() != start_position;
    }

}
