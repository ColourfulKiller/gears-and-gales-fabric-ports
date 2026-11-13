package com.ldtteam.fabric.event.bus;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Minimal NeoForge-style event bus: {@link SubscribeEvent} methods (static ones via {@link #register(Class)}, instance
 * ones via {@link #register(Object)}), dispatched in {@link EventPriority} order to listeners of the event's class and
 * its superclasses. Canceled events skip later listeners unless they set receiveCanceled.
 */
public class EventBus implements IEventBus
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private record Listener(Object owner, EventPriority priority, boolean receiveCanceled, Consumer<Event> handler) {}

    private final Map<Class<?>, List<Listener>> listeners = new ConcurrentHashMap<>();

    @Override
    public void register(final Object target)
    {
        final boolean isStatic = target instanceof Class<?>;
        final Class<?> type = isStatic ? (Class<?>) target : target.getClass();
        for (final Method method : allMethods(type))
        {
            final SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);
            if (annotation == null || Modifier.isStatic(method.getModifiers()) != isStatic || method.getParameterCount() != 1
                  || !Event.class.isAssignableFrom(method.getParameterTypes()[0]))
            {
                continue;
            }
            try
            {
                method.setAccessible(true);
                MethodHandle handle = MethodHandles.lookup().unreflect(method);
                if (!isStatic)
                {
                    handle = handle.bindTo(target);
                }
                final MethodHandle bound = handle;
                add(method.getParameterTypes()[0], new Listener(target, annotation.priority(), annotation.receiveCanceled(), event -> {
                    try
                    {
                        bound.invoke(event);
                    }
                    catch (final RuntimeException | Error e)
                    {
                        throw e;
                    }
                    catch (final Throwable e)
                    {
                        throw new RuntimeException(e);
                    }
                }));
            }
            catch (final IllegalAccessException e)
            {
                LOGGER.error("Cannot register event handler {}", method, e);
            }
        }
    }

    private static List<Method> allMethods(final Class<?> type)
    {
        final List<Method> methods = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass())
        {
            for (final Method m : c.getDeclaredMethods())
            {
                methods.add(m);
            }
        }
        return methods;
    }

    @Override
    public void unregister(final Object target)
    {
        listeners.values().forEach(list -> list.removeIf(l -> l.owner() == target));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> void addListener(final Class<T> eventType, final Consumer<T> listener)
    {
        addListener(EventPriority.NORMAL, false, eventType, listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> void addListener(final EventPriority priority, final boolean receiveCanceled, final Class<T> eventType, final Consumer<T> listener)
    {
        add(eventType, new Listener(listener, priority, receiveCanceled, event -> listener.accept((T) event)));
    }

    private void add(final Class<?> eventType, final Listener listener)
    {
        final List<Listener> list = listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>());
        list.add(listener);
        list.sort(Comparator.comparing(Listener::priority));
    }

    /** Whether anything listens for this event type (lets the bridge skip building events nobody wants). */
    public boolean hasListeners(final Class<? extends Event> eventType)
    {
        for (Class<?> c = eventType; c != null && Event.class.isAssignableFrom(c); c = c.getSuperclass())
        {
            final List<Listener> list = listeners.get(c);
            if (list != null && !list.isEmpty())
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public <T extends Event> T post(final T event)
    {
        final List<Listener> all = new ArrayList<>();
        for (Class<?> c = event.getClass(); c != null && Event.class.isAssignableFrom(c); c = c.getSuperclass())
        {
            final List<Listener> list = listeners.get(c);
            if (list != null)
            {
                all.addAll(list);
            }
        }
        all.sort(Comparator.comparing(Listener::priority));
        for (final Listener listener : all)
        {
            if (event.isCanceled() && !listener.receiveCanceled())
            {
                continue;
            }
            try
            {
                listener.handler().accept(event);
            }
            catch (final RuntimeException e)
            {
                LOGGER.error("Exception in event handler {} for {}", listener.owner(), event.getClass().getSimpleName(), e);
            }
        }
        return event;
    }
}