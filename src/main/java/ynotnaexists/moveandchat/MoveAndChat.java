package ynotnaexists.moveandchat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ynotnaexists.moveandchat.mixin.accessors.ChatScreenGetInputSuggestor;

public class MoveAndChat implements ClientModInitializer {
    public static final String MOD_ID = "moveandchat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static boolean commandMovementEnabled;

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(commandMovementKey);
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player != null && client.player.isDead()) setEnabled(false);
            if (!MoveAndChat.enabled() || !(client.currentScreen instanceof ChatScreen)) return;
            KeyBinding.updatePressedStates();
            ((ChatScreenGetInputSuggestor) client.currentScreen).getChatInputSuggestor().refresh();
        });
    }

    public static void setEnabled(boolean enabled) {
        commandMovementEnabled = enabled;
    }

    public static boolean enabled() {
        return commandMovementEnabled;
    }

    public static final KeyBinding commandMovementKey = new KeyBinding(
        "key.walkingincommand.toggle_command_movement",
        GLFW.GLFW_KEY_LEFT_CONTROL,
        KeyBinding.Category.MOVEMENT
    );
}