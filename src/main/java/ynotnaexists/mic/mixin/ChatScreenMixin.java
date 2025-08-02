package ynotnaexists.mic.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ynotnaexists.mic.MIC;
import ynotnaexists.mic.mixin.accessors.ChatInputSuggestorGetWindow;
import ynotnaexists.mic.mixin.accessors.KeyBindingGetBoundKey;
import ynotnaexists.mic.mixin.accessors.ScreenGetMinecraftClient;

@Environment(EnvType.CLIENT)
@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Shadow protected TextFieldWidget chatField;
    @Shadow ChatInputSuggestor chatInputSuggestor;

    @Inject(
            method = "keyPressed",
            at = @At("HEAD"),
            cancellable = true
    )
    private void movementToggle(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = ((ScreenGetMinecraftClient) this).getMinecraftClient();
        if (keyCode == ((KeyBindingGetBoundKey) MIC.commandMovementKey).getBoundKey().getCode()) {
            if (MIC.enabled()) {
                MIC.setEnabled(false);
                client.mouse.unlockCursor();
                this.chatField.setFocused(true);
                this.chatField.setEditable(true);
                cir.setReturnValue(true);
            } else {
                MIC.setEnabled(true);
                client.mouse.lockCursor();
                this.chatField.setFocused(false);
                this.chatField.setEditable(false);
                cir.setReturnValue(true);
            }
        }
        if (MIC.enabled()) {
            if (keyCode == GLFW.GLFW_KEY_TAB) {
                ChatInputSuggestor.SuggestionWindow window = ((ChatInputSuggestorGetWindow) this.chatInputSuggestor).getWindow();
                if (window != null) {
                    MIC.setEnabled(false);
                    window.complete();
                    client.mouse.unlockCursor();
                    this.chatField.setFocused(true);
                    this.chatField.setEditable(true);
                    cir.setReturnValue(true);
                    return;
                }
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                MIC.setEnabled(false);
                client.setScreen(null);
                cir.setReturnValue(true);
            }
        }
    }
}
