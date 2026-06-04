package ynotnaexists.moveandchat.mixin.accessors;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(Screen.class)
public interface ScreenGetMinecraftClient {
    @Accessor("minecraft")
    Minecraft getMinecraftClient();
}