package com.minecolonies.core.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.core.commands.arguments.ColonyIdArgument;
import com.minecolonies.core.commands.commandTypes.IMCColonyOfficerCommand;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.SortedArraySet;

import java.util.HashSet;
import java.util.Set;

import static com.minecolonies.core.commands.CommandArgumentNames.COLONYID_ARG;
import static com.minecolonies.core.commands.colonycommands.CommandColonyInfo.ID_TEXT;
import static com.minecolonies.core.commands.colonycommands.CommandColonyInfo.NAME_TEXT;

public class CommandColonyChunks implements IMCColonyOfficerCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final IColony colony = ColonyIdArgument.getColony(context, COLONYID_ARG);

        // Fabric 1.21.11: chunk tickets moved into the private TicketStorage; only the colony's own ticket type is reported.
        final StringBuilder ticketString = new StringBuilder("[").append(net.minecraft.core.registries.BuiltInRegistries.TICKET_TYPE.getKey(com.minecolonies.api.util.constant.ColonyConstants.KEEP_LOADED_TYPE)).append("]");

        context.getSource()
          .sendSuccess(() -> Component.literal(ID_TEXT)
            .append(Component.literal("" + colony.getID()).withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" " + NAME_TEXT))
            .append(Component.literal("" + colony.getName()).withStyle(ChatFormatting.YELLOW)), true);
        context.getSource().sendSuccess(() -> Component.literal("Loaded chunks:").append(Component.literal(" " + colony.getLoadedChunkCount()).withStyle(ChatFormatting.YELLOW)), true);
        context.getSource().sendSuccess(() -> Component.translatableEscape("Ticket types: ").append(Component.literal(ticketString.toString()).withStyle(ChatFormatting.YELLOW)), true);

        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "chunkstatus";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
          .then(IMCCommand.newArgument(COLONYID_ARG, ColonyIdArgument.id()).executes(this::checkPreConditionAndExecute));
    }
}
