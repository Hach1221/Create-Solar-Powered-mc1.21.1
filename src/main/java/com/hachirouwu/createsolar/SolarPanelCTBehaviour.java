package com.hachirouwu.createsolar;

import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SolarPanelCTBehaviour extends ConnectedTextureBehaviour.Base {

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
        return switch (direction) {
            case UP -> CreateSolarSpriteShifts.SOLAR_PANEL_TOP;
            case DOWN -> CreateSolarSpriteShifts.SOLAR_PANEL_BOTTOM;
            default -> CreateSolarSpriteShifts.SOLAR_PANEL_SIDE;
        };
    }
}
