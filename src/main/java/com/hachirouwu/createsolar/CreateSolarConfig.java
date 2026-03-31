package com.hachirouwu.createsolar;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class CreateSolarConfig {
    public static final ModConfigSpec SERVER_SPEC;
    public static final ModConfigSpec.IntValue MAX_OUTPUT;
    public static final ModConfigSpec.IntValue UPDATE_INTERVAL;
    public static final ModConfigSpec.IntValue MAX_ENERGY_STORED;

    public static final ModConfigSpec.BooleanValue ALTITUDE_DEPENDENCE_ENABLED;
    public static final ModConfigSpec.IntValue ALTITUDE_MIN_Y;
    public static final ModConfigSpec.IntValue ALTITUDE_MAX_Y;
    public static final ModConfigSpec.DoubleValue ALTITUDE_MIN_FACTOR;
    public static final ModConfigSpec.DoubleValue ALTITUDE_MAX_FACTOR;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Solar Panel settings");

        MAX_OUTPUT = builder
                .comment("Maximum FE generated in one update interval at full sunlight (default 250 = 12.5 FE/T).")
                .defineInRange("max_output", 250, 1, 500);

        UPDATE_INTERVAL = builder
                .comment("How many ticks between energy updates (default 20 = 1 second).")
                .defineInRange("update_interval", 20, 1, 100);

        MAX_ENERGY_STORED = builder
                .comment("Internal energy buffer capacity (FE). Default 1000.")
                .defineInRange("max_energy_stored", 1000, 1, 1000000);

        builder.comment("Altitude Dependence settings (disabled by default)");
        ALTITUDE_DEPENDENCE_ENABLED = builder
                .comment("Whether solar panel output depends on altitude. Default false.")
                .define("altitude_dependence_enabled", false);

        ALTITUDE_MIN_Y = builder
                .comment("Minimum Y level for altitude calculation (lowest altitude, default -64).")
                .defineInRange("altitude_min_y", -64, -64, 320);

        ALTITUDE_MAX_Y = builder
                .comment("Maximum Y level for altitude calculation (highest altitude, default 200).")
                .defineInRange("altitude_max_y", 200, -64, 320);

        ALTITUDE_MIN_FACTOR = builder
                .comment("Output multiplier at minimum altitude (default 0.5 = 50% efficiency).")
                .defineInRange("altitude_min_factor", 0.5, 0.0, 1.0);

        ALTITUDE_MAX_FACTOR = builder
                .comment("Output multiplier at maximum altitude (default 1.0 = 100% efficiency).")
                .defineInRange("altitude_max_factor", 1.0, 0.0, 1.0);

        SERVER_SPEC = builder.build();
    }

    public static void register() {
        ModContainer container = ModLoadingContext.get().getActiveContainer();
        container.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
    }
}
