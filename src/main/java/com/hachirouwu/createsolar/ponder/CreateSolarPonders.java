package com.hachirouwu.createsolar.ponder;

import com.hachirouwu.createsolar.CreateSolar;
import com.hachirouwu.createsolar.ModItems;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class CreateSolarPonders {
    public static final ResourceLocation SOLAR = CreateSolar.rl("solar");

    private CreateSolarPonders() {
    }

    public static void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<DeferredHolder<Item, BlockItem>> h = helper.withKeyFunction(DeferredHolder::getId);

        h.registerTag(SOLAR)
                .addToIndex()
                .item(ModItems.SOLAR_PANEL.get(), true, false)
                .title("Solar")
                .description("FE generation from sunlight")
                .register();

        h.addToTag(SOLAR)
                .add(ModItems.SOLAR_PANEL);
    }

    public static void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<DeferredHolder<Item, BlockItem>> h = helper.withKeyFunction(DeferredHolder::getId);

        h.addStoryBoard(ModItems.SOLAR_PANEL, "solar_panel", SolarPanelPonderScenes::solarPanel, SOLAR);
    }
}
