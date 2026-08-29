package me.scarletleaf1000.sunworks.compat.ponder.scenes;

import me.scarletleaf1000.sunworks.compat.ponder.util.PonderScene;
import me.scarletleaf1000.sunworks.item.ModItems;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;


public class HelioreceiverScenes extends PonderScene {

    public static void linking(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("helioreceiver_linking", "Linking Helioreceivers");
        setupScene(5, scene);

        scene.idle(10);

        var recieverPos = util.grid().at(3, 0, 3);
        var cablePos = util.grid().at(3, 2,3);
        var recieverSelection = util.select().fromTo(cablePos, recieverPos);
        var reflectorPos = util.grid().at(1, 1, 1);
        var reflectorSelection = util.select().position(reflectorPos);

        reveal(scene, recieverSelection, Direction.UP);
        narrateAbove(scene, util, "The helioreceiver absorbs solar power reflected from reflection panels to generate FE.", recieverPos);

        reveal(scene, reflectorSelection, Direction.DOWN);
        narrateAbove(scene, util, "The reflector panel reflects sunlight at a helioreceiver.", reflectorPos);

        showShiftClickWithItemAt(scene, util, reflectorPos, ModItems.SOLAR_WRENCH.get().getDefaultInstance());
        scene.idle(30);
        showShiftClickWithItemAt(scene, util, recieverPos, ModItems.SOLAR_WRENCH.get().getDefaultInstance());
        scene.idle(20);
        narrateAbove(scene, util, "A solar wrench can be used to link a reflector to a receiver.", reflectorPos);
        scene.idle(20);
        narrateAbove(scene, util, "The helioreceiver generates 60 FE/t for every connected panel.", recieverPos);
    }
}
