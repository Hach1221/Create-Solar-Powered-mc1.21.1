package com.hachirouwu.createsolar;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.block.connected.CTModel;

public class CreateSolarConnectedTextures {

    public static void register() {
        CreateClient.MODEL_SWAPPER.getCustomBlockModels()
                .register(
                        ModBlocks.SOLAR_PANEL.getId(),
                        model -> new CTModel(model, new SolarPanelCTBehaviour())
                );
    }
}