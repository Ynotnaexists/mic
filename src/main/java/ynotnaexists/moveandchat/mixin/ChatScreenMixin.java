package ynotnaexists.moveandchat.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screens.ChatInputSuggestor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ynotnaexists.moveandchat.MoveAndChat;
import ynotnaexists.moveandchat.mixin.accessors.ChatInputSuggestorGetWindow;
import ynotnaexists.moveandchat.mixin.accessors.KeyBindingGetBoundKey;
import ynotnaexists.moveandchat.mixin.accessors.ScreenGetMinecraftClient;

@Environment(EnvType.CLIENT)
@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Shadow protected MultilineTextField chatField;
    @Shadow ChatInputSuggestor chatInputSuggestor;
    @Shadow public abstract void sendMessage(String chatText, boolean addToHistory);
    @Shadow public abstract void close();

    @Inject(
            method = "keyPressed",
            at = @At("HEAD"),
            cancellable = true
    )
    private void movementToggle(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = ((ScreenGetMinecraftClient) this).getMinecraftClient();
        int keyCode = input.getKeycode();

        if (keyCode == ((KeyBindingGetBoundKey) MoveAndChat.commandMovementKey).getBoundKey().getCode()) {
            if (MoveAndChat.enabled()) {
                MoveAndChat.setEnabled(false);
                client.mouse.unlockCursor();
                this.chatField.setFocused(true);
                this.chatField.setEditable(true);
                cir.setReturnValue(true);
            } else {
                MoveAndChat.setEnabled(true);
                client.mouse.lockCursor();
                this.chatField.setFocused(false);
                this.chatField.setEditable(false);
                cir.setReturnValue(true);
            }
        }
        if (MoveAndChat.enabled()) {
            if (input.isTab()) {
                ChatInputSuggestor.SuggestionWindow window = ((ChatInputSuggestorGetWindow) this.chatInputSuggestor).getWindow();

                if (window == null) return;
                MoveAndChat.setEnabled(false);
                window.complete();
                client.mouse.unlockCursor();
                this.chatField.setFocused(true);
                this.chatField.setEditable(true);
                cir.setReturnValue(true);
                return;
            }
            if (input.isEscape()) {
                MoveAndChat.setEnabled(false);
                this.close();
                cir.setReturnValue(true);
            }
            if (input.isEnter()) {
                ChatInputSuggestor.SuggestionWindow window = ((ChatInputSuggestorGetWindow) this.chatInputSuggestor).getWindow();

                if (window == null) return;
                MoveAndChat.setEnabled(false);
                window.complete();
                this.sendMessage(this.chatField.getText(), true);
                this.chatField.setText("");
                this.close();
                cir.setReturnValue(true);
            }
        }
    }
}
