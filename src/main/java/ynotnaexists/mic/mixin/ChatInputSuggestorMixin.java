package ynotnaexists.mic.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ynotnaexists.mic.MIC;
import ynotnaexists.mic.mixin.accessors.SuggestionWindowGetSelection;

@Mixin(ChatInputSuggestor.class)
public abstract class ChatInputSuggestorMixin {
    @Shadow @Nullable private ChatInputSuggestor.@Nullable SuggestionWindow window;
    @Unique private int selection;

    @Inject(method = "refresh", at = @At("HEAD"))
    private void onRefresh(CallbackInfo ci) {
        if (MIC.enabled() && this.window != null) {
            selection = ((SuggestionWindowGetSelection) this.window).getSelection();
        }
    }

    @Inject(method = "show", at = @At("TAIL"))
    public void onRefreshEnd(CallbackInfo ci) {
        this.window.scroll(selection);
    }

    @Inject(method = "tryRenderWindow", at = @At("HEAD"), cancellable = true)
    public void preventUnwantedMouseMovement(DrawContext context, int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (MIC.enabled() && this.window != null) {
            this.window.render(context, 0, 100);
            cir.setReturnValue(true);
        }
    }
}