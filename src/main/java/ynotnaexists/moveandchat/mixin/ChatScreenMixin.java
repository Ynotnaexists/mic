package ynotnaexists.moveandchat.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ynotnaexists.moveandchat.MoveAndChat;
import ynotnaexists.moveandchat.mixin.accessors.GetCommandSuggestionsSuggestionList;
import ynotnaexists.moveandchat.mixin.accessors.ScreenGetMinecraftClient;

import static ynotnaexists.moveandchat.ModState.commandMovementEnabled;

@Environment(EnvType.CLIENT)
@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Shadow protected EditBox input;
    @Shadow private CommandSuggestions commandSuggestions;
    @Shadow public abstract void handleChatInput(String chatText, boolean addToHistory);
    @Shadow public abstract void onClose();
    @Shadow public abstract void insertText(String text, boolean replace);

    @Inject(
            method = "keyPressed",
            at = @At("HEAD"),
            cancellable = true
    )
    private void movementToggle(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {

        // Set command movement
        if (MoveAndChat.commandMovementKey.matches(event)) {
            if (commandMovementEnabled) {
                this.move_and_chat$diableCommandMovement();
                cir.setReturnValue(true);
            } else {
                this.move_and_chat$enableCommandMovement();
                cir.setReturnValue(true);
            }
        }

        // Handle command movement inputs
        if (!commandMovementEnabled) return;

        if (event.isEscape()) {
            this.move_and_chat$diableCommandMovement();
            this.onClose();
            cir.setReturnValue(true);
        }

        CommandSuggestions.SuggestionsList list = ((GetCommandSuggestionsSuggestionList) this.commandSuggestions).getSuggestionsList();
        // If there are no suggestions
        if (list == null) return;

        if (event.isCycleFocus()) {
            if (!event.hasAltDown()) {
                this.move_and_chat$diableCommandMovement();
            }
            list.useSuggestion();
            this.insertText(" ", false);
            cir.setReturnValue(true);

        } else if (event.isConfirmation()) {
            this.move_and_chat$diableCommandMovement();
            list.useSuggestion();
            this.handleChatInput(this.input.getValue(), true);
            this.input.setValue("");
            this.onClose();
            cir.setReturnValue(true);
        }
    }
    @Unique
    private void move_and_chat$diableCommandMovement() {
        Minecraft minecraft = ((ScreenGetMinecraftClient) this).getMinecraftClient();
        commandMovementEnabled = false;
        minecraft.mouseHandler.releaseMouse();
        this.input.setFocused(true);
        this.input.setEditable(true);
    }
    @Unique
    private void move_and_chat$enableCommandMovement() {
        Minecraft minecraft = ((ScreenGetMinecraftClient) this).getMinecraftClient();
        commandMovementEnabled = true;
        minecraft.mouseHandler.grabMouse();
        this.input.setFocused(false);
        this.input.setEditable(false);
    }
}