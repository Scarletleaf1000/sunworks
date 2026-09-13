package me.scarletleaf1000.sunworks.compat.ponder.scenes;

import me.scarletleaf1000.sunworks.compat.ponder.util.PonderScene;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;

public class SolarPanelScenes extends PonderScene {

    public static void intro(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("solar_panel_intro", "Using Solar Panels");
        setupScene(5, scene);

        scene.idle(10);

        var panelPos = util.grid().at(2, 1, 2);
        var panelSelection = util.select().position(panelPos);

        reveal(scene, panelSelection, Direction.UP);
        narrateAbove(scene, util, "The solar panel generates FE from the sun.", panelPos);
        narrateAbove(scene, util, "During the day it can generate much more power than at night.", panelPos);
    }

    public static void powerOutput(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("solar_panel_power_output", "Outputting FE from Solar Panels");
        setupScene(5, scene);
        scene.idle(10);

        var panelPos = util.grid().at(3, 2, 1);
        var panelSelection = util.select().position(panelPos);

        reveal(scene, panelSelection, Direction.UP);
        narrateAbove(scene, util, "You will need to extract power from the solar panel.", panelPos);
        narrateAbove(scene, util, "Solar panels can only output power on the bottom.", panelPos);
    }

}
