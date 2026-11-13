package com.minecolonies.core.client.render.worldevent;

import net.minecraft.client.Minecraft;
import com.ldtteam.blockui.util.color.ColourARGB;
import com.ldtteam.blockui.util.color.ColourQuartet;
import com.ldtteam.blockui.util.color.IColour;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.util.WorldRenderMacros;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.claim.IChunkClaimData;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.util.MutableChunkPos;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class ColonyBorderRenderer
{
    /**
     * Fabric 1.21.11 port: VertexBuffer is gone, so the border lines are cached on the CPU (positions + colours) and
     * streamed into the lines render type each frame.
     */
    private static final class LineMesh
    {
        private final FloatArrayList positions = new FloatArrayList();
        private final IntArrayList   colours   = new IntArrayList();
        private IColour defaultColor = new ColourQuartet(255, 255, 255, 255);

        private LineMesh addVertex(final float x, final float y, final float z)
        {
            positions.add(x);
            positions.add(y);
            positions.add(z);
            return this;
        }

        private void setDefaultColor()
        {
            colours.add(defaultColor.argb());
        }

        private void close()
        {
        }

        private void emit(final Matrix4f pose, final VertexConsumer consumer)
        {
            for (int i = 0; i < colours.size(); i++)
            {
                consumer.addVertex(pose, positions.getFloat(i * 3), positions.getFloat(i * 3 + 1), positions.getFloat(i * 3 + 2)).setColor(colours.getInt(i)).setNormal(0, 1, 0).setLineWidth(1.0F);
            }
        }
    }

    private static final int RENDER_DIST_THRESHOLD = 3;
    private static final int CHUNK_SIZE = 16;
    private static final int PLAYER_CHUNK_STEP = CHUNK_SIZE / 4;

    private static LineMesh colonies           = null;
    private static LineMesh chunktickets       = null;
    private static ChunkPos                     lastPlayerChunkPos = null;
    private static IColonyView lastColony = null;

    static void render(final WorldEventContext ctx)
    {
        if (ctx.mainHandItem.getItem() != ModItems.buildTool.get() || !ctx.hasNearestColony())
        {
            return;
        }

        final ChunkPos playerChunkPos = new ChunkPos(ctx.clientPlayer.blockPosition());

        if (lastColony != ctx.nearestColony || !lastPlayerChunkPos.equals(playerChunkPos))
        {
            lastColony = ctx.nearestColony;
            lastPlayerChunkPos = playerChunkPos;

            final Map<ChunkPos, Integer> coloniesMap = new HashMap<>();
            final Map<ChunkPos, Integer> chunkticketsMap = new HashMap<>();
            final int nearestColonyId = ctx.nearestColony.getID();
            final int playerRenderDist = Math.max(ctx.clientRenderDist - RENDER_DIST_THRESHOLD, 2);
            final int range = Math.max(ctx.clientRenderDist, MineColonies.getConfig().getServer().maxColonySize.get());

            for (int chunkX = -range; chunkX <= range; chunkX++)
            {
                for (int chunkZ = -range; chunkZ <= range; chunkZ++)
                {
                    final LevelChunk chunk = ctx.clientLevel.getChunk(playerChunkPos.x + chunkX, playerChunkPos.z + chunkZ);
                    if (chunk.isEmpty()) { continue; }
                    final ChunkPos chunkPos = chunk.getPos();

                    final IChunkClaimData cap = IColonyManager.getInstance().getClaimData(ctx.nearestColony.getDimension(), chunkPos);;
                    if (cap != null)
                    {
                        coloniesMap.put(chunkPos, cap.getOwningColony());
                    }

                    if (ctx.nearestColony.getTicketedChunks().contains(chunkPos.toLong()))
                    {
                        chunkticketsMap.put(chunkPos, nearestColonyId);
                    }
                    else
                    {
                        chunkticketsMap.put(chunkPos, 0);
                    }
                }
            }

            if (colonies != null)
            {
                colonies.close();
            }
            if (chunktickets != null)
            {
                chunktickets.close();
            }
            colonies = draw(ctx, coloniesMap, nearestColonyId, playerChunkPos, playerRenderDist);
            chunktickets = draw(ctx, chunkticketsMap, nearestColonyId, playerChunkPos, playerRenderDist);
        }

        final LineMesh p = Minecraft.getInstance().hasControlDown() ? chunktickets : colonies;
        if (p == null)
        {
            return;
        }

        ctx.pushPoseCameraToPos(lastPlayerChunkPos.getWorldPosition());
        p.emit(ctx.poseStack.last().pose(), ctx.bufferSource.getBuffer(WorldRenderMacros.LINES));
        ctx.popPose();
    }

    private static LineMesh draw(final WorldEventContext ctx,
        final Map<ChunkPos, Integer> mapToDraw,
        final int playerColonyId,
        final ChunkPos playerChunkPos,
        final int playerRenderDist)
    {
        final MutableChunkPos mutableChunkPos = new MutableChunkPos(0, 0);
        final Map<Integer, IColour> colonyColours = new HashMap<>();
        final boolean useColonyColour = IMinecoloniesAPI.getInstance().getConfig().getClient().colonyteamborders.get();

        final LineMesh buf = new LineMesh();
        mapToDraw.forEach((chunkPos, colonyId) -> {
            if (colonyId == 0 || chunkPos.x <= playerChunkPos.x - playerRenderDist || chunkPos.x >= playerChunkPos.x + playerRenderDist
                || chunkPos.z <= playerChunkPos.z - playerRenderDist || chunkPos.z >= playerChunkPos.z + playerRenderDist)
            {
                return;
            }

            final boolean isPlayerChunkX = colonyId == playerColonyId && chunkPos.x == playerChunkPos.x;
            final boolean isPlayerChunkZ = colonyId == playerColonyId && chunkPos.z == playerChunkPos.z;
            final float minX = chunkPos.getMinBlockX() - playerChunkPos.getMinBlockX();
            final float maxX = chunkPos.getMaxBlockX() - playerChunkPos.getMinBlockX() + 1.0f;
            final float minZ = chunkPos.getMinBlockZ() - playerChunkPos.getMinBlockZ();
            final float maxZ = chunkPos.getMaxBlockZ() - playerChunkPos.getMinBlockZ() + 1.0f;
            final int minY = ctx.clientLevel.getMinY();
            final int maxY = (ctx.clientLevel.getMaxY() + 1);
            final int testedColonyId = colonyId;

            if (useColonyColour)
            {
                buf.defaultColor = colonyColours.computeIfAbsent(colonyId, id ->
                {
                    final IColonyView colony = IMinecoloniesAPI.getInstance().getColonyManager().getColonyView(id, ctx.clientLevel.dimension());
                    final ChatFormatting team = colony != null ? colony.getTeamColonyColor()
                            : id == playerColonyId ? ChatFormatting.WHITE : ChatFormatting.RED;
                    return new ColourARGB(team.getColor() | 0xff000000).asQuartet();
                });
            }
            else if (colonyId == playerColonyId)
            {
                buf.defaultColor = new ColourQuartet(255, 255, 255, 255);
            }
            else
            {
                buf.defaultColor = new ColourQuartet(255, 70, 70, 255);
            }

            mutableChunkPos.setX(chunkPos.x);
            mutableChunkPos.setZ(chunkPos.z - 1);
            final boolean north = mapToDraw.getOrDefault(mutableChunkPos, -1) != testedColonyId;

            mutableChunkPos.setZ(chunkPos.z + 1);
            final boolean south = mapToDraw.getOrDefault(mutableChunkPos, -1) != testedColonyId;

            mutableChunkPos.setX(chunkPos.x + 1);
            mutableChunkPos.setZ(chunkPos.z);
            final boolean east = mapToDraw.getOrDefault(mutableChunkPos, -1) != testedColonyId;

            mutableChunkPos.setX(chunkPos.x - 1);
            final boolean west = mapToDraw.getOrDefault(mutableChunkPos, -1) != testedColonyId;

            // vert lines
            if (north || west)
            {
                buf.addVertex(minX, minY, minZ).setDefaultColor();
                buf.addVertex(minX, maxY, minZ).setDefaultColor();
            }
            if (north || east)
            {
                buf.addVertex(maxX, minY, minZ).setDefaultColor();
                buf.addVertex(maxX, maxY, minZ).setDefaultColor();
            }
            if (south || west)
            {
                buf.addVertex(minX, minY, maxZ).setDefaultColor();
                buf.addVertex(minX, maxY, maxZ).setDefaultColor();
            }
            if (south || east)
            {
                buf.addVertex(maxX, minY, maxZ).setDefaultColor();
                buf.addVertex(maxX, maxY, maxZ).setDefaultColor();
            }

            // horizontal lines
            if (north)
            {
                if (isPlayerChunkX)
                {
                    for (int shift = PLAYER_CHUNK_STEP; shift < CHUNK_SIZE; shift += PLAYER_CHUNK_STEP)
                    {
                        buf.addVertex(minX + shift, minY, minZ).setDefaultColor();
                        buf.addVertex(minX + shift, maxY, minZ).setDefaultColor();
                    }
                    for (int y = minY + PLAYER_CHUNK_STEP; y < maxY; y += PLAYER_CHUNK_STEP)
                    {
                        buf.addVertex(minX, y, minZ).setDefaultColor();
                        buf.addVertex(maxX, y, minZ).setDefaultColor();
                    }
                }
                else
                {
                    for (int y = minY + CHUNK_SIZE; y < maxY; y += CHUNK_SIZE)
                    {
                        buf.addVertex(minX, y, minZ).setDefaultColor();
                        buf.addVertex(maxX, y, minZ).setDefaultColor();
                    }
                }
            }
            if (south)
            {
                if (isPlayerChunkX)
                {
                    for (int shift = PLAYER_CHUNK_STEP; shift < CHUNK_SIZE; shift += PLAYER_CHUNK_STEP)
                    {
                        buf.addVertex(minX + shift, minY, maxZ).setDefaultColor();
                        buf.addVertex(minX + shift, maxY, maxZ).setDefaultColor();
                    }
                    for (int y = minY + PLAYER_CHUNK_STEP; y < maxY; y += PLAYER_CHUNK_STEP)
                    {
                        buf.addVertex(minX, y, maxZ).setDefaultColor();
                        buf.addVertex(maxX, y, maxZ).setDefaultColor();
                    }
                }
                else
                {
                    for (int y = minY + CHUNK_SIZE; y < maxY; y += CHUNK_SIZE)
                    {
                        buf.addVertex(minX, y, maxZ).setDefaultColor();
                        buf.addVertex(maxX, y, maxZ).setDefaultColor();
                    }
                }
            }
            if (west)
            {
                if (isPlayerChunkZ)
                {
                    for (int shift = PLAYER_CHUNK_STEP; shift < CHUNK_SIZE; shift += PLAYER_CHUNK_STEP)
                    {
                        buf.addVertex(minX, minY, minZ + shift).setDefaultColor();
                        buf.addVertex(minX, maxY, minZ + shift).setDefaultColor();
                    }
                    for (int y = minY + PLAYER_CHUNK_STEP; y < maxY; y += PLAYER_CHUNK_STEP)
                    {
                        buf.addVertex(minX, y, minZ).setDefaultColor();
                        buf.addVertex(minX, y, maxZ).setDefaultColor();
                    }
                }
                else
                {
                    for (int y = minY + CHUNK_SIZE; y < maxY; y += CHUNK_SIZE)
                    {
                        buf.addVertex(minX, y, minZ).setDefaultColor();
                        buf.addVertex(minX, y, maxZ).setDefaultColor();
                    }
                }
            }
            if (east)
            {
                if (isPlayerChunkZ)
                {
                    for (int shift = PLAYER_CHUNK_STEP; shift < CHUNK_SIZE; shift += PLAYER_CHUNK_STEP)
                    {
                        buf.addVertex(maxX, minY, minZ + shift).setDefaultColor();
                        buf.addVertex(maxX, maxY, minZ + shift).setDefaultColor();
                    }
                    for (int y = minY + PLAYER_CHUNK_STEP; y < maxY; y += PLAYER_CHUNK_STEP)
                    {
                        buf.addVertex(maxX, y, minZ).setDefaultColor();
                        buf.addVertex(maxX, y, maxZ).setDefaultColor();
                    }
                }
                else
                {
                    for (int y = minY + CHUNK_SIZE; y < maxY; y += CHUNK_SIZE)
                    {
                        buf.addVertex(maxX, y, minZ).setDefaultColor();
                        buf.addVertex(maxX, y, maxZ).setDefaultColor();
                    }
                }
            }
        });

        return buf.colours.isEmpty() ? null : buf;
    }

    /**
     * Cleanup on logout.
     */
    public static void cleanup()
    {
        if (colonies != null)
        {
            colonies.close();
        }
        if (chunktickets != null)
        {
            chunktickets.close();
        }
        lastColony = null;
        lastPlayerChunkPos = null;
    }
}
