package com.hachirouwu.createsolar;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;

public class CreateSolarSpriteShifts {
    public static final CTSpriteShiftEntry SOLAR_PANEL_TOP =
            CTSpriteShifter.getCT(
                    AllCTTypes.ROOF,
                    CreateSolar.rl("block/solar_panel_top"),
                    CreateSolar.rl("block/solar_panel_top_connected_sheet")
            );

    // Placeholder mappings for side and bottom
    public static final CTSpriteShiftEntry SOLAR_PANEL_SIDE =
            CTSpriteShifter.getCT(
                    AllCTTypes.ROOF,
                    CreateSolar.rl("block/solar_panel_side"),
                    CreateSolar.rl("block/solar_panel_top_connected_sheet")
            );

    public static final CTSpriteShiftEntry SOLAR_PANEL_BOTTOM =
            CTSpriteShifter.getCT(
                    AllCTTypes.ROOF,
                    CreateSolar.rl("block/solar_panel_bottom"),
                    CreateSolar.rl("block/solar_panel_top_connected_sheet") 
            );
}
