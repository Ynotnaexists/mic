package ynotnaexists.mic.mixin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.argument.CoordinateArgument;
import net.minecraft.server.command.FillCommand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ynotnaexists.mic.MIC;
import ynotnaexists.mic.mixin.accessors.SuggestionWindowGetSelection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Mixin(ChatInputSuggestor.class)
public abstract class ChatInputSuggestorMixin {
    @Shadow @Nullable private ChatInputSuggestor.@Nullable SuggestionWindow window;
    @Unique private int selection;
    @Final @Shadow TextFieldWidget textField;


    @Shadow protected abstract void showCommandSuggestions();

    @Shadow private @Nullable ParseResults<ClientCommandSource> parse;

    @Inject(method = "refresh", at = @At("HEAD"))
    private void onRefresh(CallbackInfo ci) {
        if (MIC.enabled() && this.window != null) {
            selection = ((SuggestionWindowGetSelection) this.window).getSelection();
        }
    }

    @Inject(method = "show", at = @At("TAIL"))
    public void onRefreshEnd(CallbackInfo ci) {
        this.window.scroll(selection);
    }

    @Inject(method = "sortSuggestions", at = @At("HEAD"), cancellable = true)
    public void MICSortSuggestions(Suggestions suggestions, CallbackInfoReturnable<List<Suggestion>> cir) {

//        suggestions.getList().forEach(suggestion -> MIC.LOGGER.info("sugestion type {} ", suggestion.getClass()));
    }

    // Prevents the mouse making selection changes
    @Inject(method = "tryRenderWindow", at = @At("HEAD"), cancellable = true)
    public void preventUnwantedMouseMovement(DrawContext context, int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (MIC.enabled() && this.window != null) {
            this.window.render(context, 0, 100);
            cir.setReturnValue(true);
        }
    }
}