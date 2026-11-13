package com.ldtteam.structurize.event;

import com.ldtteam.structurize.network.messages.*;

/**
 * Mod lifecycle (was the NeoForge mod-bus subscriber). Data generation is parked in neoforge-only; its output ships in
 * src/main/resources. Language loading moved to the entrypoints.
 */
public class LifecycleSubscriber
{
    private LifecycleSubscriber()
    {
    }

    /** Payload types and server receivers; both sides, from the main entrypoint. */
    public static void onNetworkRegistry()
    {
        AbsorbBlockMessage.TYPE.register();
        AddRemoveTagMessage.TYPE.register();
        BlueprintSyncMessage.TYPE.register();
        BuildToolPlacementMessage.TYPE.register();
        ClientBlueprintRequestMessage.TYPE.register();
        FillTopPlaceholderMessage.TYPE.register();
        ItemMiddleMouseMessage.TYPE.register();
        NotifyClientAboutStructurePacksMessage.TYPE.register();
        NotifyServerAboutStructurePacksMessage.TYPE.register();
        OperationHistoryMessage.TYPE.register();
        RemoveBlockMessage.TYPE.register();
        RemoveEntityMessage.TYPE.register();
        ReplaceBlockMessage.TYPE.register();
        SaveScanMessage.TYPE.register();
        ScanOnServerMessage.TYPE.register();
        ScanToolTeleportMessage.TYPE.register();
        SetTagInTool.TYPE.register();
        ShowScanMessage.TYPE.register();
        SyncPreviewCacheToClient.TYPE.register();
        SyncPreviewCacheToServer.TYPE.register();
        SyncSettingsToServer.TYPE.register();
        TransferStructurePackToClient.TYPE.register();
        UndoRedoMessage.TYPE.register();
        UpdateClientRender.TYPE.register();
        UpdateScanToolMessage.TYPE.register();
    }

    /** Client receivers, from the client entrypoint. */
    public static void onClientNetworkRegistry()
    {
        AbsorbBlockMessage.TYPE.registerClient();
        AddRemoveTagMessage.TYPE.registerClient();
        BlueprintSyncMessage.TYPE.registerClient();
        BuildToolPlacementMessage.TYPE.registerClient();
        ClientBlueprintRequestMessage.TYPE.registerClient();
        FillTopPlaceholderMessage.TYPE.registerClient();
        ItemMiddleMouseMessage.TYPE.registerClient();
        NotifyClientAboutStructurePacksMessage.TYPE.registerClient();
        NotifyServerAboutStructurePacksMessage.TYPE.registerClient();
        OperationHistoryMessage.TYPE.registerClient();
        RemoveBlockMessage.TYPE.registerClient();
        RemoveEntityMessage.TYPE.registerClient();
        ReplaceBlockMessage.TYPE.registerClient();
        SaveScanMessage.TYPE.registerClient();
        ScanOnServerMessage.TYPE.registerClient();
        ScanToolTeleportMessage.TYPE.registerClient();
        SetTagInTool.TYPE.registerClient();
        ShowScanMessage.TYPE.registerClient();
        SyncPreviewCacheToClient.TYPE.registerClient();
        SyncPreviewCacheToServer.TYPE.registerClient();
        SyncSettingsToServer.TYPE.registerClient();
        TransferStructurePackToClient.TYPE.registerClient();
        UndoRedoMessage.TYPE.registerClient();
        UpdateClientRender.TYPE.registerClient();
        UpdateScanToolMessage.TYPE.registerClient();
    }
}