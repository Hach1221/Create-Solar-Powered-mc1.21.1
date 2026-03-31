package com.hachirouwu.createsolar;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, CreateSolar.MOD_ID);

    public static final DeferredHolder<Item, BlockItem> SOLAR_PANEL = ITEMS.register("solar_panel",
            () -> new BlockItem(ModBlocks.SOLAR_PANEL.get(), new Item.Properties()));
}
