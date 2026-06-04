package ynotnaexists.moveandchat.mixin.accessors;

import net.minecraft.client.gui.components.CommandSuggestions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandSuggestions.class)
public interface GetCommandSuggestionsSuggestionList {
    @Accessor("suggestions")
    CommandSuggestions.SuggestionsList getSuggestionsList();
}
