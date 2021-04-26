package scripts.api.sulliuscep;

import org.tribot.api2007.types.RSTile;
import scripts.dax_api.walker_engine.Loggable;

public class SulliuscepEngine implements Loggable {
    private RSTile startingPosition;

    public static SulliuscepEngine instance;

    private final RSTile sulliuscep_tile_6 = new RSTile(3678, 3806, 0);
    private final RSTile sulliuscep_tile_5 = new RSTile(3663, 3802, 0);
    private final RSTile sulliuscep_tile_4 = new RSTile(3663, 3781, 0);
    private final RSTile sulliuscep_tile_3 = new RSTile(3683, 3775, 0);
    private final RSTile sulliuscep_tile_2 = new RSTile(3678, 3733, 0);
    private final RSTile sulliuscep_tile_1 = new RSTile(3683, 3758, 0);

    // clear vines tile (Rake)
    private final RSTile vines_tile = new RSTile(3675, 3771, 0);

    // NOTE: Some Thick vines are also called "Thick vine"
    // this tile contains an object called "Thick vine" instead of "Thick vines" (Chop)
    private final RSTile thick_vines_tile_6 = new RSTile(3672, 3801, 0);
    private final RSTile thick_vines_tile_5 = new RSTile(3670, 3792, 0);
    private final RSTile thick_vines_tile_4 = new RSTile(3672, 3780, 0);
    private final RSTile thick_vines_tile_3 = new RSTile(3672, 3764, 0);
    private final RSTile thick_vines_tile_2 = new RSTile(3671, 3760, 0);
    private final RSTile thick_vines_tile_1 = new RSTile(3669, 3746, 0);

    private final RSTile thick_vine_tile_spawn = new RSTile(3678, 3743, 0);

    public SulliuscepEngine() {
    }

    public SulliuscepEngine(RSTile startingPosition) {
        this.startingPosition = startingPosition;
    }

    public static SulliuscepEngine getInstance() {
        return instance != null ? instance : (instance = new SulliuscepEngine());
    }

    public RSTile getStartingPosition() {
        return startingPosition;
    }

    public void setStartingPosition(RSTile startingPosition) {
        this.startingPosition = startingPosition;
    }

    public RSTile getSulliuscepTile6() {
        return sulliuscep_tile_6;
    }

    public RSTile getSulliuscepTile5() {
        return sulliuscep_tile_5;
    }

    public RSTile getSulliuscepTile4() {
        return sulliuscep_tile_4;
    }

    public RSTile getSulliuscepTile3() {
        return sulliuscep_tile_3;
    }

    public RSTile getSulliuscepTile2() {
        return sulliuscep_tile_2;
    }

    public RSTile getSulliuscepTile1() {
        return sulliuscep_tile_1;
    }

    public RSTile getVinesTile() {
        return vines_tile;
    }

    public RSTile getThickVinesTile6() {
        return thick_vines_tile_6;
    }

    public RSTile getThickVinesTile5() {
        return thick_vines_tile_5;
    }

    public RSTile getThickVinesTile4() {
        return thick_vines_tile_4;
    }

    public RSTile getThickVinesTile3() {
        return thick_vines_tile_3;
    }

    public RSTile getThickVinesTile2() {
        return thick_vines_tile_2;
    }

    public RSTile getThickVinesTile1() {
        return thick_vines_tile_1;
    }

    public RSTile getThickVineTileSpawn() {
        return thick_vine_tile_spawn;
    }

    public RSTile[] getAllSulliuscepTiles() {
        return new RSTile[] {
                this.sulliuscep_tile_1,
                this.sulliuscep_tile_2,
                this.sulliuscep_tile_3,
                this.sulliuscep_tile_4,
                this.sulliuscep_tile_5,
                this.sulliuscep_tile_6
        };
    }

    @Override
    public String getName() {
        return "Sulliuscep";
    }

    @Override
    public void log(CharSequence debug) {
        Loggable.super.log(debug);
    }

    @Override
    public void log(Level level, CharSequence debug) {
        Loggable.super.log(level, debug);
    }
}
