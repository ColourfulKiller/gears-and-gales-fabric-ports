package com.ldtteam.fabric;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * NeoForge's menus-with-extra-data (IMenuTypeExtension#create + ServerPlayer#openMenu(provider, buf -> ...)) on top of
 * Fabric's extended menu types. The extra data travels as an opaque byte array, so the mod's buffer reading / writing
 * code is unchanged.
 */
public final class MenuHooks
{
    private MenuHooks()
    {
    }

    /** NeoForge's IContainerFactory. */
    @FunctionalInterface
    public interface ContainerFactory<T extends AbstractContainerMenu>
    {
        T create(int windowId, Inventory inventory, RegistryFriendlyByteBuf data);
    }

    /** Was {@code IMenuTypeExtension.create(factory)}. */
    public static <T extends AbstractContainerMenu> MenuType<T> create(final ContainerFactory<T> factory)
    {
        return new ExtendedScreenHandlerType<>((syncId, inventory, data) ->
            factory.create(syncId, inventory, new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data), inventory.player.registryAccess())),
            ByteBufCodecs.BYTE_ARRAY);
    }

    /** Was {@code serverPlayer.openMenu(provider, extraDataWriter)}. */
    public static void openMenu(final Player player, final MenuProvider provider, final Consumer<RegistryFriendlyByteBuf> extraDataWriter)
    {
        if (!(player instanceof final ServerPlayer serverPlayer))
        {
            return;
        }
        final RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), serverPlayer.registryAccess());
        extraDataWriter.accept(buf);
        final byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        buf.release();
        serverPlayer.openMenu(new ExtendedScreenHandlerFactory<byte[]>()
        {
            @Override
            public byte[] getScreenOpeningData(final ServerPlayer p)
            {
                return data;
            }

            @Override
            public Component getDisplayName()
            {
                return provider.getDisplayName();
            }

            @Override
            public @Nullable AbstractContainerMenu createMenu(final int id, final Inventory inventory, final Player p)
            {
                return provider.createMenu(id, inventory, p);
            }
        });
    }
}
