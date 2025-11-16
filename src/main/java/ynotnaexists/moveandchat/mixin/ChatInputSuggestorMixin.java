package ynotnaexists.moveandchat.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ynotnaexists.moveandchat.MoveAndChat;
import ynotnaexists.moveandchat.mixin.accessors.SuggestionWindowGetSelection;

import java.util.*;

@Mixin(ChatInputSuggestor.class)
public abstract class ChatInputSuggestorMixin {
    @Shadow @Nullable private ChatInputSuggestor.@Nullable SuggestionWindow window;
    @Unique private int selection;
    @Final @Shadow TextFieldWidget textField;


    @Shadow protected abstract void showCommandSuggestions();

    @Shadow private @Nullable ParseResults<ClientCommandSource> parse;

    @Inject(method = "refresh", at = @At("HEAD"))
    private void onRefresh(CallbackInfo ci) {
        if (MoveAndChat.enabled() && this.window != null) {
            selection = ((SuggestionWindowGetSelection) this.window).getSelection();
        }
    }

    @Inject(method = "show", at = @At("TAIL"), cancellable = true)
    public void onRefreshEnd(CallbackInfo cir) {
        if (window == null) cir.cancel();
        this.window.scroll(selection);
    }

    @Inject(method = "sortSuggestions", at = @At("HEAD"), cancellable = true)
    public void analyzeSuggestionType(Suggestions suggestions, CallbackInfoReturnable<List<Suggestion>> cir) {
        if (this.parse == null || !MoveAndChat.enabled() || MinecraftClient.getInstance().world == null) return;
        List<ParsedCommandNode<ClientCommandSource>> nodes = this.parse.getContext().getNodes();
        if (nodes.isEmpty()) return;

        List<Suggestion> modifiedSuggestions = new ArrayList<>(suggestions.getList());

        // Get all nodes (parsed and being suggested)

        // Check for future nodes to change suggestions of
        CommandNode<ClientCommandSource> child = nodes.getLast()
                .getNode()
                .getChildren()
                .stream()
                .filter(ArgumentCommandNode.class::isInstance)
                .findFirst()
                .orElse(null);
        if (!(child instanceof ArgumentCommandNode<?, ?>)) cir.cancel();

        ArgumentCommandNode<?, ?> argNode = (ArgumentCommandNode<?, ?>) child;
        var argumentType = argNode.getType();
        MoveAndChat.LOGGER.debug("Next argument type: {}", argumentType.getClass().getName());

        if (argumentType instanceof BlockPosArgumentType) {
            MoveAndChat.LOGGER.debug("Detected BlockPos suggestions");
            if (modifiedSuggestions.size() <= 3) cir.cancel();
            Collections.swap(modifiedSuggestions, 0, 2);

        } else if (argumentType instanceof EntityArgumentType) {
            // Gets if there is a UUID suggestion based on if an entity with that uuid exists
            ClientWorld world = MinecraftClient.getInstance().world;
            Suggestion uuidSuggestion = modifiedSuggestions.stream()
                    .filter(s -> {
                        // For each suggestion check if UUID is valid
                        try {
                            UUID uuid = UUID.fromString(s.getText());
                            return world.getEntity(uuid) != null;
                        } catch (IllegalArgumentException e) {
                            return false;
                        }
                    })
                    .findFirst()
                    .orElse(null);

            if (uuidSuggestion != null) {
                modifiedSuggestions.remove(uuidSuggestion);
                modifiedSuggestions.addFirst(uuidSuggestion);
            }
        }

        cir.setReturnValue(modifiedSuggestions);
    }

    // Prevents the mouse making selection changes
    @Inject(method = "tryRenderWindow", at = @At("HEAD"), cancellable = true)
    public void preventUnwantedMouseMovement(DrawContext context, int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (MoveAndChat.enabled() && this.window != null) {
            this.window.render(context, 0, 100);
            cir.setReturnValue(true);
        }
    }
}