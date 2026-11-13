package com.ldtteam.fabric;

import net.minecraft.network.chat.Component;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 1.21.5 changed Item.appendHoverText from filling a List&lt;Component&gt; to calling a Consumer&lt;Component&gt;. This
 * list forwards every add() to the consumer, so ported tooltip code can keep its List-based body. Reading back what
 * was added works too (it keeps a copy).
 */
public final class TooltipList extends AbstractList<Component>
{
    private final Consumer<Component> consumer;
    private final List<Component> added = new ArrayList<>();

    private TooltipList(final Consumer<Component> consumer)
    {
        this.consumer = consumer;
    }

    public static List<Component> of(final Consumer<Component> consumer)
    {
        return new TooltipList(consumer);
    }

    @Override
    public boolean add(final Component component)
    {
        consumer.accept(component);
        return added.add(component);
    }

    @Override
    public Component get(final int index)
    {
        return added.get(index);
    }

    @Override
    public int size()
    {
        return added.size();
    }
}
