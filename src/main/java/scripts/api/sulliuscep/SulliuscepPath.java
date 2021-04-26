package scripts.api.sulliuscep;

import org.tribot.api2007.types.RSTile;

public class SulliuscepPath {
    private final RSTile start_position;
    private final RSTile sulliuscep_position;

    private RSTile[] absolutePath;
    private RSTile[] rotationPath;

    public SulliuscepPath(RSTile start_position, RSTile sulliuscep_position) {
        this.start_position = start_position;
        this.sulliuscep_position = sulliuscep_position;
        if (this.start_position != null && this.sulliuscep_position != null) {
            final SulliuscepEngine sulliuscep_engine = new SulliuscepEngine();
            // if player distance to previous sulliuscep is less than the next sulliuscep then proceed
            //  to walk the rotation path; else walk absolute path.
            //
            //
            if (this.sulliuscep_position.equals(sulliuscep_engine.getSulliuscepTile1())) {
                this.absolutePath = SulliuscepPaths.PRE_ROTATION_1;
            } else if (this.sulliuscep_position.equals(sulliuscep_engine.getSulliuscepTile2())) {
                this.absolutePath = SulliuscepPaths.PRE_ROTATION_2;
                this.rotationPath = SulliuscepPaths.ROTATION_1;
            } else if (this.sulliuscep_position.equals(sulliuscep_engine.getSulliuscepTile3())) {
                this.absolutePath = SulliuscepPaths.PRE_ROTATION_3;
                this.rotationPath = SulliuscepPaths.ROTATION_2;
            } else if (this.sulliuscep_position.equals(sulliuscep_engine.getSulliuscepTile4())) {
                this.absolutePath = SulliuscepPaths.PRE_ROTATION_4;
                this.rotationPath = SulliuscepPaths.ROTATION_3;
            } else if (this.sulliuscep_position.equals(sulliuscep_engine.getSulliuscepTile5())) {
                this.absolutePath = SulliuscepPaths.PRE_ROTATION_5;
                this.rotationPath = SulliuscepPaths.ROTATION_4;
            } else {
                this.absolutePath = SulliuscepPaths.PRE_ROTATION_6;
                this.rotationPath = SulliuscepPaths.ROTATION_5;
            }
        }
    }

    public RSTile[] getRotationPath() {
        return rotationPath;
    }

    public RSTile[] getAbsolutePath() {
        return absolutePath;
    }

    public RSTile getStartPosition() {
        return start_position;
    }

    public RSTile getSulliuscepPosition() {
        return sulliuscep_position;
    }

}
