package ynotnaexists.mic.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ynotnaexists.mic.MIC;

@Environment(EnvType.CLIENT)
@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow private boolean cursorLocked;
    @Shadow private double x;
    @Shadow private double y;
    @Shadow @Final private MinecraftClient client;

    /**
     * This intercepts the lockCursor method to prevent it from
     * closing the ChatScreen, which would create a NullPointerException
     */
    @Inject(
        method = "lockCursor",
        at = @At("HEAD"),
        cancellable = true
    )
    private void addWalkingInCommandSupport(CallbackInfo ci) {
        if (MIC.enabled()) {
            this.cursorLocked = true;
            this.x = this.client.getWindow().getWidth() / 2;
            this.y = this.client.getWindow().getHeight() / 2;
            InputUtil.setCursorParameters(this.client.getWindow().getHandle(), InputUtil.GLFW_CURSOR_DISABLED, this.x, this.y);
            ci.cancel();
        }
    }
}
