package com.ldtteam.fabric.event;

import com.ldtteam.fabric.event.bus.Event;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/** NeoForge's RegisterCommandsEvent, fired from Fabric's CommandRegistrationCallback. */
public class RegisterCommandsEvent extends Event
{
    private final CommandDispatcher<CommandSourceStack> dispatcher;
    private final Commands.CommandSelection selection;
    private final CommandBuildContext buildContext;

    public RegisterCommandsEvent(final CommandDispatcher<CommandSourceStack> dispatcher, final Commands.CommandSelection selection, final CommandBuildContext buildContext)
    {
        this.dispatcher = dispatcher;
        this.selection = selection;
        this.buildContext = buildContext;
    }

    public CommandDispatcher<CommandSourceStack> getDispatcher()
    {
        return dispatcher;
    }

    public Commands.CommandSelection getCommandSelection()
    {
        return selection;
    }

    public CommandBuildContext getBuildContext()
    {
        return buildContext;
    }
}