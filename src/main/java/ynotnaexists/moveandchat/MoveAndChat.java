package ynotnaexists.moveandchat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ynotnaexists.moveandchat.ModState.commandMovementEnabled;

public class MoveAndChat implements ClientModInitializer {
    public static final String MOD_ID = "moveandchat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        KeyMappingHelper.registerKeyMapping(commandMovementKey);
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player != null && client.player.isDeadOrDying()) commandMovementEnabled = false;
            if (commandMovementEnabled || !(client.screen instanceof ChatScreen)) return;
            KeyMapping.resetToggleKeys();
        });
    }

    public static final KeyMapping commandMovementKey = new KeyMapping(
        "key.walkingincommand.toggle_command_movement",
        GLFW.GLFW_KEY_LEFT_CONTROL,
        KeyMapping.Category.MOVEMENT
    );
}