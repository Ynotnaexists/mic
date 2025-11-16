package ynotnaexists.moveandchat.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ynotnaexists.moveandchat.MoveAndChat;

@Environment(EnvType.CLIENT)
@Mixin(TextFieldWidget.class)
public abstract class TextFieldMixin {

    @Shadow public abstract void setCursor(int cursor, boolean shiftKeyPressed);

    @Shadow protected abstract int getCursorPosWithOffset(int offset);


    // Prevents highlighting text accidentally while holding shift to crouch/fly
    @Inject(
            method = "moveCursor",
            at = @At("HEAD"),
            cancellable = true
    ) public void onMoveCursor(int offset, boolean shiftKeyPressed, CallbackInfo ci) {
        if (MoveAndChat.enabled()) {
            this.setCursor(this.getCursorPosWithOffset(offset), false);
            ci.cancel();
        }
    }
}
