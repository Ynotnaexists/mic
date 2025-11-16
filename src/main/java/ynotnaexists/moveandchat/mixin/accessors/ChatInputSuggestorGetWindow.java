package ynotnaexists.moveandchat.mixin.accessors;

import net.minecraft.client.gui.screen.ChatInputSuggestor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatInputSuggestor.class)
public interface ChatInputSuggestorGetWindow {
    @Accessor("window")
    ChatInputSuggestor.SuggestionWindow getWindow();
}
