package com.ldtteam.fabric;

/**
 * Stand-in for NeoForge's client ModelData (extra per-block data for block models). Fabric has no equivalent, so ported
 * code keeps passing this around and it is always {@link #EMPTY}.
 */
public final class ModelData
{
    public static final ModelData EMPTY = new ModelData();

    private ModelData() {}
}
