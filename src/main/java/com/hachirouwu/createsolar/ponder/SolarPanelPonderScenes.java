package com.hachirouwu.createsolar.ponder;

import com.hachirouwu.createsolar.ModBlocks;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class SolarPanelPonderScenes {

    private SolarPanelPonderScenes() {
    }

    /**
     * Single-scene storyboard (Create Addition “alternator” style). If you export a Ponder template from
     * in-game, place it at {@code assets/createsolar/ponder/solar_panel.nbt}; this code still runs for text
     * and can place the panel when cells start empty.
     */
    public static void solarPanel(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("solar_panel", "Solar Panel");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos panelPos = util.grid().at(2, 1, 2);

        // Place only the solar panel
        scene.world().setBlock(panelPos, ModBlocks.SOLAR_PANEL.get().defaultBlockState(), false);

        // Show the panel block
        scene.world().showSection(util.select().position(panelPos), Direction.DOWN);

        scene.idle(10);
        scene.overlay().showText(50)
                .text("createsolar.ponder.solar_panel.text_1")
                .placeNearTarget()
                .pointAt(util.vector().topOf(panelPos));
        scene.idle(60);

        scene.overlay().showText(50)
                .text("createsolar.ponder.solar_panel.text_2")
                .placeNearTarget()
                .pointAt(util.vector().topOf(panelPos));
        scene.idle(60);

        scene.overlay().showText(50)
                .text("createsolar.ponder.solar_panel.text_3")
                .placeNearTarget()
                .pointAt(util.vector().topOf(panelPos));
        scene.idle(60);

        // Show the surrounding panels during the final text
        scene.overlay().showText(50)
                .text("createsolar.ponder.solar_panel.text_4")
                .placeNearTarget()
                .pointAt(util.vector().topOf(panelPos));

        // Define offsets for the 8 adjacent panels (horizontal only, y=0)
        int[][] offsets = {
            {-1, 0, -1}, {0, 0, -1}, {1, 0, -1},
            {-1, 0,  0},             {1, 0,  0},
            {-1, 0,  1}, {0, 0,  1}, {1, 0,  1}
        };

        // Place and show each surrounding panel with a small delay
        for (int i = 0; i < offsets.length; i++) {
            BlockPos offsetPos = panelPos.offset(offsets[i][0], offsets[i][1], offsets[i][2]);
            scene.world().setBlock(offsetPos, ModBlocks.SOLAR_PANEL.get().defaultBlockState(), false);
            scene.world().showSection(util.select().position(offsetPos), Direction.DOWN);
            scene.idle(5); // slight stagger
        }

        scene.idle(40); // let the text linger after all panels appear
        scene.markAsFinished();
    }
}