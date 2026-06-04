package ynotnaexists.moveandchat.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ynotnaexists.moveandchat.ModState.commandMovementEnabled;

@Environment(EnvType.CLIENT)
@Mixin(MouseHandler.class)
public class MouseMixin {
    @Shadow private boolean mouseGrabbed;
    @Shadow private double xpos;
    @Shadow private double ypos;
    @Shadow @Final private Minecraft minecraft;

    /**
     * This intercepts the lockCursor method to prevent it from
     * closing the ChatScreen, which would create a NullPointerException
     */
    @Inject(
        method = "grabMouse",
        at = @At("HEAD"),
        cancellable = true
    )
    private void addWalkingInCommandSupport(CallbackInfo ci) {
        if (commandMovementEnabled) {
            this.mouseGrabbed = true;
            this.xpos = (double) this.minecraft.getWindow().getWidth() / 2;
            this.ypos = (double) this.minecraft.getWindow().getHeight() / 2;
            InputConstants.grabOrReleaseMouse(this.minecraft.getWindow(), InputConstants.CURSOR_DISABLED, this.xpos, this.ypos);
            ci.cancel();
        }
    }
}
