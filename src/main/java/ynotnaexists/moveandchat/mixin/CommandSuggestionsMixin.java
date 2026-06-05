package ynotnaexists.moveandchat.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ynotnaexists.moveandchat.MoveAndChat;

import java.util.*;

import static ynotnaexists.moveandchat.ModState.commandMovementEnabled;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {
    @Shadow @Nullable private CommandSuggestions.SuggestionsList suggestions;
    @Shadow @Nullable private ParseResults<ClientSuggestionProvider> currentParse;

    @Unique private int selection;

    @Inject(method = "showSuggestions", at = @At("TAIL"), cancellable = true)
    public void onRefreshEnd(CallbackInfo cir) {
        if (suggestions == null) cir.cancel();
        this.suggestions.cycle(selection);
    }

    @Inject(method = "sortSuggestions", at = @At("HEAD"), cancellable = true)
    public void analyzeSuggestionType(Suggestions suggestions, CallbackInfoReturnable<List<Suggestion>> cir) {
        List<ParsedCommandNode<ClientSuggestionProvider>> nodes = this.currentParse.getContext().getNodes();
        MoveAndChat.LOGGER.debug(String.format("""
            Parse: %s
            Enabled: %s
            Level: %s
            Nodes: %s
            """, this.currentParse, commandMovementEnabled, Minecraft.getInstance().level, nodes
        ));

        if (this.currentParse == null ||
            !commandMovementEnabled ||
            Minecraft.getInstance().level == null ||
            nodes.isEmpty()) return;
        List<Suggestion> modifiedSuggestions = new ArrayList<>(suggestions.getList());

        // Get all nodes (parsed and being suggested)
        // Check for future nodes to change suggestions of
        CommandNode<ClientSuggestionProvider> child = nodes.getLast()
                .getNode()
                .getChildren()
                .stream()
                .filter(ArgumentCommandNode.class::isInstance)
                .findFirst()
                .orElse(null);
        if (!(child instanceof ArgumentCommandNode<?, ?>)) cir.cancel();

        ArgumentCommandNode<?, ?> argNode = (ArgumentCommandNode<?, ?>) child;
        var argumentType = argNode.getType();
        MoveAndChat.LOGGER.info("Next argument type: {}", argumentType.getClass().getName());

        if (argumentType instanceof BlockPosArgument) {
            MoveAndChat.LOGGER.info("Detected BlockPos suggestions");
            if (modifiedSuggestions.size() <= 3) cir.cancel();
            Collections.swap(modifiedSuggestions, 0, 2);

        } else if (argumentType instanceof EntityArgument) {
            // Gets if there is a UUID suggestion based on if an entity with that uuid exists
            ClientLevel level = Minecraft.getInstance().level;
            Suggestion uuidSuggestion = modifiedSuggestions.stream()
                    .filter(s -> {
                        // For each suggestion check if UUID is valid
                        try {
                            UUID uuid = UUID.fromString(s.getText());
                            return level.getEntity(uuid) != null;
                        } catch (IllegalArgumentException e) {
                            return false;
                        }
                    })
                    .findFirst()
                    .orElse(null);

            // Bring to the front of the list
            if (uuidSuggestion != null) {
                modifiedSuggestions.remove(uuidSuggestion);
                modifiedSuggestions.addFirst(uuidSuggestion);
            }
        }

        cir.setReturnValue(modifiedSuggestions);
    }

    // Prevents the mouse making selection changes
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    public void preventUnwantedMouseMovement(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (commandMovementEnabled && this.suggestions != null) {
            this.suggestions.extractRenderState(graphics, 0, 100);
            ci.cancel();
        }
    }
}