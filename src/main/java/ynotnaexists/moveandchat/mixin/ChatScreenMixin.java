package ynotnaexists.moveandchat.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ynotnaexists.moveandchat.MoveAndChat;
import ynotnaexists.moveandchat.mixin.accessors.GetCommandSuggestionsSuggestionList;
import ynotnaexists.moveandchat.mixin.accessors.KeyBindingGetBoundKey;
import ynotnaexists.moveandchat.mixin.accessors.ScreenGetMinecraftClient;

import static ynotnaexists.moveandchat.ModState.commandMovementEnabled;

@Environment(EnvType.CLIENT)
@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Shadow protected EditBox input;
    @Shadow CommandSuggestions commandSuggestions;
    @Shadow public abstract void handleChatInput(String chatText, boolean addToHistory);
    @Shadow public abstract void onClose();

    @Inject(
            method = "keyPressed",
            at = @At("HEAD"),
            cancellable = true
    )
    private void movementToggle(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = ((ScreenGetMinecraftClient) this).getMinecraftClient();
        CommandSuggestions.SuggestionsList list = ((GetCommandSuggestionsSuggestionList) this.commandSuggestions).getSuggestionsList();
        int keyCode = event.hashCode();

        MoveAndChat.LOGGER.info("Key press detected");

        if (keyCode == ((KeyBindingGetBoundKey) MoveAndChat.commandMovementKey).getBoundKey().hashCode()) {
            MoveAndChat.LOGGER.info("Key press is command movement key");
            if (commandMovementEnabled) {
                commandMovementEnabled = false;
                client.mouseHandler.releaseMouse();
                this.input.setFocused(true);
                this.input.setEditable(true);
                cir.setReturnValue(true);
            } else {
                commandMovementEnabled = true;
                MoveAndChat.LOGGER.info("hi");
                client.mouseHandler.grabMouse();
                this.input.setFocused(false);
                this.input.setEditable(false);
                cir.setReturnValue(true);
            }
        }
        if (commandMovementEnabled) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_TAB:
                    if (list == null) return;
                    commandMovementEnabled =false;
                    list.useSuggestion();
                    client.mouseHandler.releaseMouse();
                    this.input.setFocused(true);
                    this.input.setEditable(true);
                    cir.setReturnValue(true);
                    return;

                case GLFW.GLFW_KEY_ESCAPE:
                    commandMovementEnabled =false;
                    this.onClose();
                    cir.setReturnValue(true);

                case GLFW.GLFW_KEY_ENTER:
                    if (list == null) return;
                    commandMovementEnabled =false;
                    list.useSuggestion();
                    this.handleChatInput(this.input.getValue(), true);
                    this.input.setValue("");
                    this.onClose();
                    cir.setReturnValue(true);
            }
        }
    }
}