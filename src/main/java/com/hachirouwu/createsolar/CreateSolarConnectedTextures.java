package com.hachirouwu.createsolar;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.block.connected.CTModel;
import net.minecraft.core.registries.BuiltInRegistries;

public class CreateSolarConnectedTextures {

    public static void register() {
        CreateClient.MODEL_SWAPPER.getCustomBlockModels()
                .register(
                        BuiltInRegistries.BLOCK.getKey(ModBlocks.SOLAR_PANEL.get()),
                        model -> new CTModel(model, new SolarPanelCTBehaviour())
                );
    }
}
