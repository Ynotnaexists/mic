package ynotnaexists.moveandchat.mixin.accessors;

import net.minecraft.client.gui.components.CommandSuggestions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandSuggestions.SuggestionsList.class)
public interface GetSuggestionListSelectionLocation {
    @Accessor("current")
    int getSelectionLocation();
}
