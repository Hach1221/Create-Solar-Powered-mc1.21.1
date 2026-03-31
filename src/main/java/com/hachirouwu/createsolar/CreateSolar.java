package com.hachirouwu.createsolar;

import com.hachirouwu.createsolar.ponder.CreateSolarPonderPlugin;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(CreateSolar.MOD_ID)
public class CreateSolar {
    public static final String MOD_ID = "createsolar";

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public CreateSolar(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        CreateSolarConfig.register();

        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::clientSetup);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            ModBlockEntities.SOLAR_PANEL.get(),
            (be, side) -> ((SolarPanelBlockEntity) be).getEnergyStorage()
        );
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().toString().equals("create:base")) {
            event.accept(ModItems.SOLAR_PANEL.get());
        }
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        CreateSolarConnectedTextures.register();
        PonderIndex.addPlugin(new CreateSolarPonderPlugin());
    }
}
