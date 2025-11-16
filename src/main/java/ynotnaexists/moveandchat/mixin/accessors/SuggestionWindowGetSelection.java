package ynotnaexists.moveandchat.mixin.accessors;

import net.minecraft.client.gui.screen.ChatInputSuggestor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatInputSuggestor.SuggestionWindow.class)
public interface SuggestionWindowGetSelection {
    @Accessor("selection")
    int getSelection();
}
