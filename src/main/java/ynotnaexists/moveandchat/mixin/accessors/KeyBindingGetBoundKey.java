package ynotnaexists.moveandchat.mixin.accessors;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(KeyMapping.class)
public interface KeyBindingGetBoundKey {
	@Accessor("key")
	InputConstants.Key getBoundKey();
}