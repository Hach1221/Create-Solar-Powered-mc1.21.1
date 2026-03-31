package com.hachirouwu.createsolar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.lang.reflect.Method;
import java.util.Locale;

@EventBusSubscriber(modid = CreateSolar.MOD_ID, value = Dist.CLIENT)
public class ClientIntegration {
    private static final String GOGGLES_CLASS_API = "com.simibubi.create.api.equipment.goggles.GogglesItem";
    private static final String GOGGLES_CLASS_CONTENT = "com.simibubi.create.content.equipment.goggles.GogglesItem";

    private static boolean createChecked = false;
    private static boolean createPresent = false;
    private static String gogglesClassName;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (!createChecked) {
            gogglesClassName = null;
            for (String name : new String[] {GOGGLES_CLASS_API, GOGGLES_CLASS_CONTENT}) {
                try {
                    Class.forName(name);
                    gogglesClassName = name;
                    break;
                } catch (ClassNotFoundException ignored) {
                }
            }
            createPresent = gogglesClassName != null;
            createChecked = true;
        }

        if (!createPresent || gogglesClassName == null) return;

        try {
            Class<?> gogglesClass = Class.forName(gogglesClassName);
            Method isWearing = gogglesClass.getMethod("isWearing", Player.class);
            boolean wearing = (boolean) isWearing.invoke(null, mc.player);
            if (!wearing) return;
        } catch (Exception e) {
            return;
        }

        HitResult hit = mc.hitResult;
        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            BlockEntity be = mc.level.getBlockEntity(pos);
            if (be instanceof SolarPanelBlockEntity panel) {
                int stored = panel.getEnergyStorage().getEnergyStored();
                int max = panel.getEnergyStorage().getMaxEnergyStored();
                int outputPerInterval = panel.getCurrentOutput();
                int interval = CreateSolarConfig.UPDATE_INTERVAL.get();
                double fePerTick = interval > 0 ? (double) outputPerInterval / interval : 0.0;
                String text = String.format(Locale.ROOT, "Solar Panel: %d/%d FE | Output: %.2f FE/t", stored, max, fePerTick);
                Font font = mc.font;
                int x = 5;
                int y = 5;
                event.getGuiGraphics().drawString(font, text, x, y, 0xFFFFFF, true);
            }
        }
    }
}
