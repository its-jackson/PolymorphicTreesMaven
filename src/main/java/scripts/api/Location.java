package scripts.api;

import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSTile;

/**
 * This enumeration contains all the woodcutting locations.
 */

public enum Location {
    ISLE_OF_SOULS(new RSArea(
            new RSTile[] {
                    new RSTile(2208, 2981, 0),
                    new RSTile(2210, 3001, 0),
                    new RSTile(2197, 3003, 0),
                    new RSTile(2186, 2998, 0),
                    new RSTile(2177, 2989, 0),
                    new RSTile(2180, 2980, 0)
            }
    )),
    ISLE_OF_SOULS_COMPLETE(
            new RSArea(
                    new RSTile(2074, 3009, 0),
                    new RSTile(2345, 2763, 0))),
    SEERS_VILLAGE_MAGICS(new RSArea(
            new RSTile[] {
                    new RSTile(2687, 3428, 0),
                    new RSTile(2696, 3418, 0),
                    new RSTile(2701, 3420, 0),
                    new RSTile(2695, 3429, 0)
            }
    )),
    REDWOOD_NORTH(new RSArea(new RSTile(1566, 3496, 1), new RSTile(1574, 3489, 1))),
    REDWOOD_NORTH_UPPER_LEVEL(new RSArea(new RSTile(1566, 3496, 2), new RSTile(1574, 3489, 2))),
    REDWOOD_SOUTH(new RSArea(new RSTile(1567, 3485, 1), new RSTile(1573, 3480, 1))),
    REDWOOD_SOUTH_UPPER_LEVEL(new RSArea(new RSTile(1567, 3485, 2), new RSTile(1573, 3480, 2))),
    WOODCUTTING_GUILD_MAGICS (new RSArea(new RSTile(1577, 3493, 0),
            new RSTile(1581, 3480, 0))),
    WOODCUTTING_GUILD_YEWS (new RSArea(new RSTile(1597, 3482, 0), new RSTile(1588, 3498, 0))),
    DRAYNOR_YEWS (new RSArea(new RSTile(3144, 3258, 0), new RSTile(3189, 3217, 0))),
    DRAYNOR_WILLOWS (new RSArea(
            new RSTile[]{
                    new RSTile(3087, 3225, 0),
                    new RSTile(3093, 3227, 0),
                    new RSTile(3088, 3237, 0),
                    new RSTile(3082, 3239, 0)
            }
    )),
    WOODCUTTING_GUILD_MAPLES (new RSArea(
            new RSTile[]{
                    new RSTile(1608, 3498, 0),
                    new RSTile(1625, 3497, 0),
                    new RSTile(1619, 3486, 0),
                    new RSTile(1609, 3489, 0)
            }
    )),
    WOODCUTTING_GUILD_WILLOWS (new RSArea(
            new RSTile[]{
                    new RSTile(1644, 3503, 0),
                    new RSTile(1626, 3503, 0),
                    new RSTile(1628, 3493, 0),
                    new RSTile(1641, 3493, 0)
            }
    )),
    WOODCUTTING_GUILD_OAKS (new RSArea(new RSTile(1612, 3515, 0), new RSTile(1653, 3505, 0))),
    VARROCK_WEST_TREES (new RSArea(new RSTile(3158, 3416, 0), new RSTile(3172, 3370, 0))),
    VARROCK_WEST_OAKS (new RSArea(
            new RSTile[] {
                    new RSTile(3169, 3422, 0),
                    new RSTile(3158, 3416, 0),
                    new RSTile(3167, 3412, 0)
            }
    )),
    VARROCK_PALACE_YEWS (new RSArea(
            new RSTile[] {
                    new RSTile(3207, 3498, 0),
                    new RSTile(3201, 3506, 0),
                    new RSTile(3223, 3507, 0),
                    new RSTile(3226, 3499, 0)
            }
    )),
    VARROCK_PALACE_OAKS (new RSArea(
            new RSTile[] {
                    new RSTile(3193, 3464, 0),
                    new RSTile(3187, 3459, 0),
                    new RSTile(3198, 3454, 0)
            }
    )),
    FALADOR_YEWS (new RSArea(
            new RSTile[] {
                    new RSTile(3043, 3323, 0),
                    new RSTile(2990, 3314, 0),
                    new RSTile(2992, 3307, 0),
                    new RSTile(3050, 3319, 0)
            }
    )),
    EDGEVILLE_YEWS (new RSArea(new RSTile(3086, 3478, 0), new RSTile(3088, 3471, 0))),
    FALADOR_EAST_OAKS (new RSArea(new RSTile(3051, 3342, 0), new RSTile(3057, 3333, 0))),
    CATHERBY_YEWS (new RSArea(
            new RSTile[] {
                    new RSTile(2767, 3424, 0),
                    new RSTile(2767, 3435, 0),
                    new RSTile(2749, 3435, 0),
                    new RSTile(2755, 3426, 0)
            }
    )),
    CATHERBY_WILLOWS (new RSArea(
            new RSTile[] {
                    new RSTile(2788, 3431, 0),
                    new RSTile(2779, 3428, 0),
                    new RSTile(2782, 3423, 0),
                    new RSTile(2787, 3427, 0)
            }
    )),
    SEERS_VILLAGE_YEWS (new RSArea(
            new RSTile[] {
                    new RSTile(2705, 3466, 0),
                    new RSTile(2705, 3458, 0),
                    new RSTile(2716, 3458, 0),
                    new RSTile(2719, 3462, 0)
            }
    )),
    SEERS_VILLAGE_WILLOWS (new RSArea(
            new RSTile[] {
                    new RSTile(2710, 3515, 0),
                    new RSTile(2715, 3508, 0),
                    new RSTile(2712, 3506, 0),
                    new RSTile(2706, 3513, 0)
            }
    )),
    SEERS_VILLAGE_MAPLES (new RSArea(new RSTile(2720, 3502, 0), new RSTile(2733, 3499, 0))),
    TAR_SWAMP (new RSArea(
            new RSTile[] {
                    new RSTile(3688, 3727, 0),
                    new RSTile(3687, 3806, 0),
                    new RSTile(3673, 3809, 0),
                    new RSTile(3658, 3802, 0),
                    new RSTile(3668, 3727, 0)
            }
    )),
    GRAND_EXCHANGE_TREES (new RSArea(
            new RSTile[] {
                    new RSTile(3151, 3447, 0),
                    new RSTile(3173, 3452, 0),
                    new RSTile(3170, 3457, 0),
                    new RSTile(3159, 3460, 0),
                    new RSTile(3157, 3466, 0),
                    new RSTile(3150, 3465, 0)
            }
    )),
    SEERS_VILLAGE_TREES (new RSArea(
            new RSTile[] {
                    new RSTile(2703, 3502, 0),
                    new RSTile(2716, 3506, 0),
                    new RSTile(2718, 3501, 0),
                    new RSTile(2715, 3498, 0)
            }
    )),
    SORCERERS_TOWER (new RSArea(new RSTile(2700, 3399, 0), new RSTile(2705, 3397, 0))),
    PORT_SARIM_WILLOWS (new RSArea(new RSTile(3056, 3255, 0), new RSTile(3063, 3250, 0))),
    LUMBRIDGE_CASTLE_TREES (new RSArea(new RSTile(3166, 3239, 0), new RSTile(3196, 3211, 0)));

    private final RSArea location;

    Location(RSArea location) {
        this.location = location;
    }

    public RSArea getRSArea() {
        return this.location;
    }
}
