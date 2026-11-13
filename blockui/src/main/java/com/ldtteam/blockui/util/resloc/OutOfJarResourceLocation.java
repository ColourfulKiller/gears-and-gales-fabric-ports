package com.ldtteam.blockui.util.resloc;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.jetbrains.annotations.Nullable;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class OutOfJarResourceLocation extends Identifier
{
    private final Path nioPath;

    protected OutOfJarResourceLocation(final String namespace, final Path path, @Nullable final String pathString)
    {
        super(namespace,
            pathString != null ? pathString : path.toString().toLowerCase().replace('\\', '/').replaceAll("[^a-z0-9/._-]", "_"));
        this.nioPath = path;
    }

    public static OutOfJarResourceLocation of(final String namespace, final Path path)
    {
        return new OutOfJarResourceLocation(namespace, path, null);
    }

    @SuppressWarnings("resource")
    public static OutOfJarResourceLocation ofMinecraftFolder(final String namespace, final String... parts)
    {
        Path path = Minecraft.getInstance().gameDirectory.toPath().resolve(namespace);
        for (final String part : parts)
        {
            path = path.resolve(part);
        }
        return of(namespace, path);
    }

    /**
     * Fabric 1.21.11 port: skins are ClientAsset textures now and their cache file is no longer exposed, so this
     * returns the skin's texture id (already loaded by vanilla) rather than an out-of-jar copy.
     *
     * @param textureSelector null for {@code PlayerSkin#body()}, or {@code PlayerSkin#cape()} / {@code PlayerSkin#elytra()} -
     *                        both cape and elytra may complete with null
     */
    public static CompletableFuture<Identifier> ofMinecraftSkin(final Minecraft minecraft,
        final GameProfile gameProfile,
        @Nullable final Function<PlayerSkin, ClientAsset.Texture> textureSelector)
    {
        return minecraft.getSkinManager().get(gameProfile).thenApply(optionalSkin -> {
            if (optionalSkin.isEmpty())
            {
                return null;
            }
            final PlayerSkin skin = optionalSkin.get();
            final ClientAsset.Texture texture = textureSelector == null ? skin.body() : textureSelector.apply(skin);
            return texture == null ? null : texture.texturePath();
        });
    }

    public Path getNioPath()
    {
        return nioPath;
    }

    public static boolean fileExists(final Identifier resLoc, final ResourceManager fallbackManager)
    {
        if (resLoc instanceof final OutOfJarResourceLocation nioResLoc)
        {
            return Files.exists(nioResLoc.nioPath);
        }
        return fallbackManager.getResource(resLoc).isPresent();
    }

    public static Resource getResourceHandle(final Identifier resLoc, final ResourceManager fallbackManager) throws IOException
    {
        if (resLoc instanceof final OutOfJarResourceLocation nioResLoc)
        {
            return fileExists(nioResLoc.withSuffix(".mcmeta"), fallbackManager) ?
                new OutOfJarResource(nioResLoc, FallbackResourceManager.convertToMetadata(() -> Files.newInputStream(nioResLoc.getNioPath()))) :
                new OutOfJarResource(nioResLoc);
        }
        return fallbackManager.getResource(resLoc).orElseThrow(() -> new FileNotFoundException("File not found: " + resLoc));
    }

    public static InputStream openStream(final Identifier resLoc, final ResourceManager fallbackManager) throws IOException
    {
        if (resLoc instanceof final OutOfJarResourceLocation nioResLoc)
        {
            return Files.newInputStream(nioResLoc.nioPath);
        }
        return fallbackManager.open(resLoc);
    }

    public static BufferedReader openReader(final Identifier resLoc, final ResourceManager fallbackManager) throws IOException
    {
        if (resLoc instanceof final OutOfJarResourceLocation nioResLoc)
        {
            return Files.newBufferedReader(nioResLoc.nioPath);
        }
        return fallbackManager.openAsReader(resLoc);
    }

    /**
     * @deprecated unsafe, use proper path methods!
     */
    @Override
    @Deprecated(forRemoval = false)
    public Identifier withPath(final String path)
    {
        return of(getNamespace(), Path.of(path));
    }

    /**
     * @deprecated unsafe, use proper path methods!
     */
    @Override
    @Deprecated(forRemoval = false)
    public Identifier withPath(final UnaryOperator<String> op)
    {
        return of(getNamespace(), Path.of(op.apply(nioPath.toString())));
    }

    /**
     * With path prefix (prefix + current path)
     */
    @Override
    public Identifier withPrefix(final String prefix)
    {
        return of(getNamespace(), Path.of(prefix).resolve(nioPath));
    }

    /**
     * With both path and file suffix.
     * <p>
     * Tries to behave as vanilla method, so {@code withSuffix(".foo")} adds file suffix, unlike {@code path.resolve(".foo")} which
     * would add file ".foo" in subdirectory. To get same behaviour as {@link Path#resolve(Path)} add '/' to start of parameter
     */
    @Override
    public Identifier withSuffix(final String suffix)
    {
        // in nio resolveSibling(...) = parent + path.of(...) so theoretically should resolve both correctly
        return of(getNamespace(), nioPath.resolveSibling(nioPath.getFileName().toString() + suffix));
    }

    @Override
    public int compareTo(final Identifier o)
    {
        if (o instanceof final OutOfJarResourceLocation nioResLoc)
        {
            final int ret = this.nioPath.compareTo(nioResLoc.nioPath);
            return ret != 0 ? ret : this.getNamespace().compareTo(nioResLoc.getNamespace());
        }
        return super.compareTo(o);
    }

    @Override
    public int hashCode()
    {
        return 31 * this.getNamespace().hashCode() + this.nioPath.hashCode();
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof final OutOfJarResourceLocation nioResLoc)
        {
            return this.getNamespace().equals(nioResLoc.getNamespace()) && this.nioPath.equals(nioResLoc.nioPath);
        }
        return false;
    }

    public static class OutOfJarResource extends Resource
    {
        private final OutOfJarResourceLocation resLoc;

        public OutOfJarResource(final OutOfJarResourceLocation resLoc, final IoSupplier<ResourceMetadata> metadataSupplier)
        {
            super(null, () -> Files.newInputStream(resLoc.getNioPath()), metadataSupplier);
            this.resLoc = resLoc;
        }

        public OutOfJarResource(final OutOfJarResourceLocation resLoc)
        {
            super(null, () -> Files.newInputStream(resLoc.getNioPath()));
            this.resLoc = resLoc;
        }

        @Override
        public PackResources source()
        {
            // currently only used at one place, so no-op should not crash
            return null;
        }

        @Override
        public String sourcePackId()
        {
            return "blockui out-of-jar resource: " + resLoc;
        }
    }
}
