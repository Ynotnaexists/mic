package ynotnaexists.moveandchat.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static ynotnaexists.moveandchat.ModState.commandMovementEnabled;

@Environment(EnvType.CLIENT)
@Mixin(EditBox.class)
public abstract class EditBoxMixin { // <-- removed implements IChatScreen
    // So that text isn't selected while command movement is enabled and shift is down
    @ModifyArg(
            method = "moveCursor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;moveCursorTo(IZ)V"),
            index = 1 // index 1 = the boolean hasShiftDown argument
    )
    private boolean onMoveCursor(boolean hasShiftDown) {
        return !commandMovementEnabled && hasShiftDown;
    }
}