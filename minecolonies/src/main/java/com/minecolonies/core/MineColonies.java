package com.minecolonies.core;

import com.ldtteam.common.config.Configurations;
import com.ldtteam.common.language.LanguageHandler;
import com.ldtteam.fabric.event.NeoForge;
import com.ldtteam.fabric.event.TagsUpdatedEvent;
import com.ldtteam.fabric.event.bus.EventPriority;
import com.ldtteam.fabric.event.bus.IEventBus;
import com.ldtteam.fabric.event.bus.SubscribeEvent;
import com.ldtteam.fabric.event.entity.EntityAttributeCreationEvent;
import com.ldtteam.fabric.event.lifecycle.FMLCommonSetupEvent;
import com.ldtteam.fabric.event.lifecycle.FMLLoadCompleteEvent;
import com.ldtteam.structurize.api.TagManager;
import com.ldtteam.structurize.storage.SurvivalBlueprintHandlers;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.configuration.ClientConfiguration;
import com.minecolonies.api.configuration.CommonConfiguration;
import com.minecolonies.api.configuration.ServerConfiguration;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesRaider;
import com.minecolonies.api.entity.mobs.RaiderMobUtils;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.items.component.ModDataComponents;
import com.minecolonies.api.loot.ModLootConditions;
import com.minecolonies.api.sounds.ModSoundEvents;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.IItemHandlerCapProvider;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.SchematicTagConstants;
import com.minecolonies.apiimp.ClientMinecoloniesAPIImpl;
import com.minecolonies.apiimp.CommonMinecoloniesAPIImpl;
import com.minecolonies.apiimp.initializer.*;
import com.minecolonies.core.blocks.BlockDecorationController;
import com.minecolonies.core.blocks.BlockPlantationField;
import com.minecolonies.core.blocks.huts.BlockHutGateHouse;
import com.minecolonies.core.blocks.huts.BlockHutMiner;
import com.minecolonies.core.blocks.huts.BlockHutSchool;
import com.minecolonies.core.colony.crafting.CustomRecipeManagerMessage;
import com.minecolonies.core.colony.requestsystem.init.RequestSystemInitializer;
import com.minecolonies.core.colony.requestsystem.init.StandardFactoryControllerInitializer;
import com.minecolonies.core.commands.arguments.ModArgumentTypes;
import com.minecolonies.core.debug.messages.DebugEnableMessage;
import com.minecolonies.core.debug.messages.DebugEnablePathfindingMessage;
import com.minecolonies.core.debug.messages.DebugOutputMessage;
import com.minecolonies.core.debug.messages.QueryCitizenAIHistoryMessage;
import com.minecolonies.core.entity.mobs.EntityMercenary;
import com.minecolonies.core.event.*;
import com.minecolonies.core.network.messages.PermissionsMessage;
import com.minecolonies.core.network.messages.client.*;
import com.minecolonies.core.network.messages.client.colony.*;
import com.minecolonies.core.network.messages.server.*;
import com.minecolonies.core.network.messages.server.colony.*;
import com.minecolonies.core.network.messages.server.colony.building.*;
import com.minecolonies.core.network.messages.server.colony.building.builder.BuilderSelectWorkOrderMessage;
import com.minecolonies.core.network.messages.server.colony.building.enchanter.EnchanterWorkerSetMessage;
import com.minecolonies.core.network.messages.server.colony.building.fields.AssignFieldMessage;
import com.minecolonies.core.network.messages.server.colony.building.fields.AssignmentModeMessage;
import com.minecolonies.core.network.messages.server.colony.building.fields.FarmFieldPlotResizeMessage;
import com.minecolonies.core.network.messages.server.colony.building.fields.FarmFieldUpdateSeedMessage;
import com.minecolonies.core.network.messages.server.colony.building.guard.GuardSetMinePosMessage;
import com.minecolonies.core.network.messages.server.colony.building.home.AssignUnassignMessage;
import com.minecolonies.core.network.messages.server.colony.building.miner.MinerRepairLevelMessage;
import com.minecolonies.core.network.messages.server.colony.building.miner.MinerSetLevelMessage;
import com.minecolonies.core.network.messages.server.colony.building.postbox.PostBoxRequestMessage;
import com.minecolonies.core.network.messages.server.colony.building.university.TryResearchMessage;
import com.minecolonies.core.network.messages.server.colony.building.warehouse.SortBuildingMessage;
import com.minecolonies.core.network.messages.server.colony.building.warehouse.UpgradeWarehouseMessage;
import com.minecolonies.core.network.messages.server.colony.building.worker.*;
import com.minecolonies.core.network.messages.server.colony.citizen.*;
import com.minecolonies.core.placementhandlers.PlacementHandlerInitializer;
import com.minecolonies.core.placementhandlers.main.SuppliesHandler;
import com.minecolonies.core.placementhandlers.main.SurvivalHandler;
import com.minecolonies.core.research.GlobalResearchTreeMessage;
import com.minecolonies.core.structures.MineColoniesStructures;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.minecraft.world.entity.animal.equine.Horse;
import org.jetbrains.annotations.NotNull;
import static com.minecolonies.api.util.constant.SchematicTagConstants.*;
import com.ldtteam.fabric.Env;
import com.ldtteam.fabric.event.NeoForge;
import com.ldtteam.fabric.event.AddReloadListenerEvent;
import com.ldtteam.fabric.event.TagsUpdatedEvent;
import com.ldtteam.fabric.event.bus.EventBus;
import com.ldtteam.fabric.event.bus.SubscribeEvent;
import com.ldtteam.fabric.event.entity.EntityAttributeCreationEvent;
import com.ldtteam.fabric.items.ItemHandlers;
import com.ldtteam.fabric.registry.DeferredRegister;
import com.ldtteam.fabric.registry.ModRegistration;
import net.fabricmc.api.ModInitializer;

import java.util.List;

/**
 * Fabric main entrypoint (was the NeoForge @Mod constructor and its mod-bus handlers; the original is kept as
 * neoforge-only/MineColonies.java.neoforge). Registration runs through {@link ModRegistration} on MineColonies' own
 * mod bus; game events go to {@link NeoForge#EVENT_BUS}, fired by com.ldtteam.fabric.event.EventBridge.
 */
public class MineColonies implements ModInitializer
{
    private static Configurations<ClientConfiguration, ServerConfiguration, CommonConfiguration> config;

    /** MineColonies' mod bus (registration-time events). */
    public static final EventBus MOD_BUS = new EventBus();

    @Override
    public void onInitialize()
    {
        if (!com.ldtteam.fabric.ModInit.begin("minecolonies", "main"))
        {
            return;
        }
        com.ldtteam.fabric.ModInit.ensure("blockui", "main");
        com.ldtteam.fabric.ModInit.ensure("domum_ornamentum", "main");
        com.ldtteam.fabric.ModInit.ensure("structurize", "main");
        com.ldtteam.fabric.ModInit.ensure("multipiston", "main");
        LanguageHandler.loadLangPath("assets/minecolonies/lang/%s.json");
        config = new Configurations<>(Constants.MOD_ID, ClientConfiguration::new, ServerConfiguration::new, CommonConfiguration::new);

        MinecoloniesAPIProxy.getInstance().setApiInstance(Env.isClient() ? new ClientMinecoloniesAPIImpl() : new CommonMinecoloniesAPIImpl());
        MinecoloniesAPIProxy.getInstance().onRegistryNewRegistry();

        MOD_BUS.register(EntityInitializer.class);
        MOD_BUS.register(ModBlocksInitializer.class);
        MOD_BUS.register(ModItemsInitializer.class);
        MOD_BUS.register(ModContainerInitializers.class);
        MOD_BUS.register(ModParticleTypesInitializer.CommonRegistration.class);
        MOD_BUS.register(MineColonies.class);

        final List<DeferredRegister<?>> registers = List.of(
            TileEntityInitializer.BLOCK_ENTITIES,
            AdvancementTriggers.DEFERRED_REGISTER,
            ModContainerInitializers.CONTAINERS,
            ModBuildingsInitializer.DEFERRED_REGISTER,
            ModBuildingExtensionsInitializer.DEFERRED_REGISTER,
            ModGuardTypesInitializer.DEFERRED_REGISTER,
            ModColonyEventDescriptionTypeInitializer.DEFERRED_REGISTER,
            ModResearchRequirementInitializer.DEFERRED_REGISTER,
            ModRecipeSerializerInitializer.RECIPE_SERIALIZER,
            ModRecipeSerializerInitializer.RECIPE_TYPES,
            ModColonyEventTypeInitializer.DEFERRED_REGISTER,
            ModCraftingTypesInitializer.DEFERRED_REGISTER,
            ModJobsInitializer.DEFERRED_REGISTER,
            ModRecipeTypesInitializer.DEFERRED_REGISTER,
            RaiderMobUtils.ATTRIBUTES,
            ModSoundEvents.SOUND_EVENTS,
            ModInteractionsInitializer.DEFERRED_REGISTER,
            ModResearchEffectInitializer.DEFERRED_REGISTER,
            ModLootConditions.DEFERRED_REGISTER,
            ModEquipmentTypes.DEFERRED_REGISTER,
            ModQuestInitializer.DEFERRED_REGISTER_OBJECTIVE,
            ModQuestInitializer.DEFERRED_REGISTER_TRIGGER,
            ModQuestInitializer.DEFERRED_REGISTER_REWARD,
            ModQuestInitializer.DEFERRED_REGISTER_ANSWER_RESULT,
            ModHappinessFactorTypeInitializer.DEFERRED_REGISTER_HAPPINESS_FACTOR,
            ModHappinessFactorTypeInitializer.DEFERRED_REGISTER_HAPPINESS_FUNCTION,
            ModDataComponents.REGISTRY,
            ModCreativeTabs.TAB_REG,
            MineColoniesStructures.DEFERRED_REGISTRY_STRUCTURE,
            MineColoniesStructures.DEFERRED_REGISTRY_STRUCTURE);
        ModIngredientTypeInitializer.register();
        ModArgumentTypes.init();
        // 1.21.2 stopped sending recipes to clients; the GUIs look up crafting / smelting / MineColonies recipes client-side.
        for (final net.minecraft.world.item.crafting.RecipeSerializer<?> serializer : java.util.List.of(net.minecraft.world.item.crafting.RecipeSerializer.SHAPED_RECIPE,
          net.minecraft.world.item.crafting.RecipeSerializer.SHAPELESS_RECIPE, net.minecraft.world.item.crafting.RecipeSerializer.SMELTING_RECIPE))
        {
            net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization.synchronizeRecipeSerializer(serializer);
        }
        // registers the colony chunk ticket type while the built-in registries are still open
        java.util.Objects.requireNonNull(com.minecolonies.api.util.constant.ColonyConstants.KEEP_LOADED_TYPE);
        ModRegistration.run(MOD_BUS, registers);
        com.ldtteam.fabric.data.Compostables.registerBundled(Constants.MOD_ID);
        MOD_BUS.post(new EntityAttributeCreationEvent());

        // network (client receivers in MineColoniesClient)
        registerMessages();

        // game bus
        NeoForge.EVENT_BUS.addListener(TagsUpdatedEvent.class, event -> ModTags.tagsLoaded = true);
        NeoForge.EVENT_BUS.register(EventHandler.class);
        NeoForge.EVENT_BUS.register(FMLEventHandler.class);
        NeoForge.EVENT_BUS.register(DataPackSyncEventHandler.ServerEvents.class);
        NeoForge.EVENT_BUS.register(QuestObjectiveEventHandler.class);
        NeoForge.EVENT_BUS.register(com.minecolonies.core.entity.other.cavalry.CavalryHorseDebug.class);
        NeoForge.EVENT_BUS.post(new AddReloadListenerEvent(Constants.MOD_ID));

        // item handler "capabilities"
        ItemHandlers.registerEntity(ModEntities.CITIZEN, IItemHandlerCapProvider::getItemHandlerCap);
        ItemHandlers.registerEntity(ModEntities.VISITOR, IItemHandlerCapProvider::getItemHandlerCap);
        ItemHandlers.registerBlockEntity(MinecoloniesTileEntities.BUILDING.get(), IItemHandlerCapProvider::getItemHandlerCap);
        ItemHandlers.registerBlockEntity(MinecoloniesTileEntities.ENCHANTER.get(), IItemHandlerCapProvider::getItemHandlerCap);
        ItemHandlers.registerBlockEntity(MinecoloniesTileEntities.RACK.get(), IItemHandlerCapProvider::getItemHandlerCap);
        ItemHandlers.registerBlockEntity(MinecoloniesTileEntities.GRAVE.get(), IItemHandlerCapProvider::getItemHandlerCap);
        ItemHandlers.registerBlockEntity(MinecoloniesTileEntities.WAREHOUSE.get(), IItemHandlerCapProvider::getItemHandlerCap);
        ItemHandlers.registerBlockEntity(MinecoloniesTileEntities.STASH.get(), IItemHandlerCapProvider::getItemHandlerCap);

        InteractionValidatorInitializer.init();
        SurvivalBlueprintHandlers.registerHandler(new SurvivalHandler());
        SurvivalBlueprintHandlers.registerHandler(new SuppliesHandler());

        // was FMLCommonSetupEvent + FMLLoadCompleteEvent
        StandardFactoryControllerInitializer.onPreInit();
        ModLootConditions.init();
        ModTags.init();
        // tool tiers are read from item tags, which are only bound once a datapack has loaded
        net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> ModEquipmentTypes.initRegisterEquipmentTiers());
        PlacementHandlerInitializer.initHandlers();
        RequestSystemInitializer.onPostInit();

        logIncompatibilities();
    }

    @SubscribeEvent
    public static void createEntityAttribute(final EntityAttributeCreationEvent event)
    {
        event.put(ModEntities.CITIZEN, AbstractEntityCitizen.getDefaultAttributes().build());
        event.put(ModEntities.VISITOR, AbstractEntityCitizen.getDefaultAttributes().build());
        event.put(ModEntities.MERCENARY, EntityMercenary.getDefaultAttributes().build());
        event.put(ModEntities.BARBARIAN, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.ARCHERBARBARIAN, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CHIEFBARBARIAN, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.PHARAO, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.MUMMY, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.ARCHERMUMMY, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.PIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.ARCHERPIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CHIEFPIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.AMAZON, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.AMAZONSPEARMAN, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.AMAZONCHIEF, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.NORSEMEN_ARCHER, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.NORSEMEN_CHIEF, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.SHIELDMAIDEN, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.DROWNED_PIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.DROWNED_ARCHERPIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.DROWNED_CHIEFPIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());

        event.put(ModEntities.CAMP_BARBARIAN, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_ARCHERBARBARIAN, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_CHIEFBARBARIAN, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_PIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_ARCHERPIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_CHIEFPIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_PHARAO, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_MUMMY, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_ARCHERMUMMY, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_AMAZON, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_AMAZONSPEARMAN, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_AMAZONCHIEF, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_NORSEMEN_ARCHER, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_NORSEMEN_CHIEF, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_SHIELDMAIDEN, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_DROWNED_PIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_DROWNED_ARCHERPIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_DROWNED_CHIEFPIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAVALRY_HORSE, Horse.createBaseHorseAttributes().build());
    }

    public static Configurations<ClientConfiguration, ServerConfiguration, CommonConfiguration> getConfig()
    {
        return config;
    }

    private static void registerMessages()
    {
        ColonyViewMessage.TYPE.register();
        ColonyViewCitizenViewMessage.TYPE.register();
        ColonyViewRemoveCitizenMessage.TYPE.register();
        ColonyViewBuildingViewMessage.TYPE.register();
        ColonyViewRemoveBuildingMessage.TYPE.register();
        ColonyViewBuildingExtensionsUpdateMessage.TYPE.register();
        PermissionsMessage.View.TYPE.register();
        ColonyViewWorkOrderMessage.TYPE.register();
        ColonyViewRemoveWorkOrderMessage.TYPE.register();
        ColonyViewResearchManagerViewMessage.TYPE.register();
        PermissionsMessage.Permission.TYPE.register();
        PermissionsMessage.AddPlayer.TYPE.register();
        PermissionsMessage.RemovePlayer.TYPE.register();
        PermissionsMessage.ChangePlayerRank.TYPE.register();
        PermissionsMessage.AddPlayerOrFakePlayer.TYPE.register();
        PermissionsMessage.AddRank.TYPE.register();
        PermissionsMessage.RemoveRank.TYPE.register();
        PermissionsMessage.EditRankType.TYPE.register();
        BuildRequestMessage.TYPE.register();
        OpenInventoryMessage.TYPE.register();
        TownHallRenameMessage.TYPE.register();
        MinerSetLevelMessage.TYPE.register();
        RecallCitizenMessage.TYPE.register();
        HireFireMessage.TYPE.register();
        WorkOrderChangeMessage.TYPE.register();
        AssignFieldMessage.TYPE.register();
        AssignmentModeMessage.TYPE.register();
        GuardSetMinePosMessage.TYPE.register();
        RecallCitizenHutMessage.TYPE.register();
        TransferItemsRequestMessage.TYPE.register();
        MarkBuildingDirtyMessage.TYPE.register();
        ChangeFreeToInteractBlockMessage.TYPE.register();
        CreateColonyMessage.TYPE.register();
        ColonyDeleteOwnMessage.TYPE.register();
        ColonyViewRemoveMessage.TYPE.register();
        GiveToolMessage.TYPE.register();
        GetColonyInfoMessage.TYPE.register();
        MarkStoryReadOnItem.TYPE.register();
        PickupBlockMessage.TYPE.register();
        ColonyAbandonOwnMessage.TYPE.register();
        AssignUnassignMessage.TYPE.register();
        OpenCraftingGUIMessage.TYPE.register();
        AddRemoveRecipeMessage.TYPE.register();
        ChangeRecipePriorityMessage.TYPE.register();
        ChangeDeliveryPriorityMessage.TYPE.register();
        ForcePickupMessage.TYPE.register();
        UpgradeWarehouseMessage.TYPE.register();
        TransferItemsToCitizenRequestMessage.TYPE.register();
        UpdateRequestStateMessage.TYPE.register();
        BuildingSetStyleMessage.TYPE.register();
        RecallSingleCitizenMessage.TYPE.register();
        AssignFilterableItemMessage.TYPE.register();
        TeamColonyColorChangeMessage.TYPE.register();
        ColonyFlagChangeMessage.TYPE.register();
        ColonyStructureStyleMessage.TYPE.register();
        PauseCitizenMessage.TYPE.register();
        RestartCitizenMessage.TYPE.register();
        SortBuildingMessage.TYPE.register();
        PostBoxRequestMessage.TYPE.register();
        HireMercenaryMessage.TYPE.register();
        HutRenameMessage.TYPE.register();
        BuildingHiringModeMessage.TYPE.register();
        DecorationBuildRequestMessage.TYPE.register();
        DirectPlaceMessage.TYPE.register();
        TeleportToColonyMessage.TYPE.register();
        EnchanterWorkerSetMessage.TYPE.register();
        InteractionResponse.TYPE.register();
        TryResearchMessage.TYPE.register();
        HireSpiesMessage.TYPE.register();
        AddMinimumStockToBuildingModuleMessage.TYPE.register();
        RemoveMinimumStockFromBuildingModuleMessage.TYPE.register();
        FarmFieldPlotResizeMessage.TYPE.register();
        FarmFieldUpdateSeedMessage.TYPE.register();
        AdjustSkillCitizenMessage.TYPE.register();
        BuilderSelectWorkOrderMessage.TYPE.register();
        TriggerSettingMessage.TYPE.register();
        AssignFilterableEntityMessage.TYPE.register();
        BuildPickUpMessage.TYPE.register();
        SwitchBuildingWithToolMessage.TYPE.register();
        ColonyTextureStyleMessage.TYPE.register();
        MinerRepairLevelMessage.TYPE.register();
        PlantationFieldBuildRequestMessage.TYPE.register();
        PlayerAssistantBuildRequestMessage.TYPE.register();
        ResetFilterableItemMessage.TYPE.register();
        CourierHiringModeMessage.TYPE.register();
        QuarryHiringModeMessage.TYPE.register();
        ToggleRecipeMessage.TYPE.register();
        ColonyNameStyleMessage.TYPE.register();
        InteractionClose.TYPE.register();
        AlterRestaurantMenuItemMessage.TYPE.register();
        TriggerConnectionEventMessage.TYPE.register();
        QueryCitizenAIHistoryMessage.TYPE.register();
        DebugEnablePathfindingMessage.TYPE.register();
        BlockParticleEffectMessage.TYPE.register();
        CompostParticleMessage.TYPE.register();
        ItemParticleEffectMessage.TYPE.register();
        LocalizedParticleEffectMessage.TYPE.register();
        OpenSuggestionWindowMessage.TYPE.register();
        UpdateClientWithCompatibilityMessage.TYPE.register();
        CircleParticleEffectMessage.TYPE.register();
        StreamParticleEffectMessage.TYPE.register();
        SleepingParticleMessage.TYPE.register();
        VanillaParticleMessage.TYPE.register();
        StopMusicMessage.TYPE.register();
        PlayAudioMessage.TYPE.register();
        PlayMusicAtPosMessage.TYPE.register();
        ColonyVisitorViewDataMessage.TYPE.register();
        ColonyViewAnimalViewDataMessage.TYPE.register();
        SyncPathMessage.TYPE.register();
        SyncPathReachedMessage.TYPE.register();
        ReactivateBuildingMessage.TYPE.register();
        PlaySoundForCitizenMessage.TYPE.register();
        OpenDecoBuildWindowMessage.TYPE.register();
        OpenPlantationFieldBuildWindowMessage.TYPE.register();
        SaveStructureNBTMessage.TYPE.register();
        GlobalQuestSyncMessage.TYPE.register();
        GlobalDiseaseSyncMessage.TYPE.register();
        DebugEnableMessage.TYPE.register();
        DebugOutputMessage.TYPE.register();
        OpenBuildingUIMessage.TYPE.register();
        OpenCantFoundColonyWarningMessage.TYPE.register();
        OpenColonyFoundingCovenantMessage.TYPE.register();
        OpenDeleteAbandonColonyMessage.TYPE.register();
        OpenReactivateColonyMessage.TYPE.register();
        TransferRecipeCraftingTeachingMessage.TYPE.register();
        OpenGuiWindowTriggerMessage.TYPE.register();
        ClickGuiButtonTriggerMessage.TYPE.register();
        RemoveFromRallyingListMessage.TYPE.register();
        ToggleBannerRallyGuardsMessage.TYPE.register();
        GlobalResearchTreeMessage.TYPE.register();
        CustomRecipeManagerMessage.TYPE.register();
        SwitchRecipeCraftingTeachingMessage.TYPE.register();
        ColonyListMessage.TYPE.register();
        ResourceScrollSaveWarehouseSnapshotMessage.TYPE.register();
        ItemSettingMessage.TYPE.register();
    }

    /** Client receivers, from MineColoniesClient. */
    public static void registerClientMessages()
    {
        ColonyViewMessage.TYPE.registerClient();
        ColonyViewCitizenViewMessage.TYPE.registerClient();
        ColonyViewRemoveCitizenMessage.TYPE.registerClient();
        ColonyViewBuildingViewMessage.TYPE.registerClient();
        ColonyViewRemoveBuildingMessage.TYPE.registerClient();
        ColonyViewBuildingExtensionsUpdateMessage.TYPE.registerClient();
        PermissionsMessage.View.TYPE.registerClient();
        ColonyViewWorkOrderMessage.TYPE.registerClient();
        ColonyViewRemoveWorkOrderMessage.TYPE.registerClient();
        ColonyViewResearchManagerViewMessage.TYPE.registerClient();
        PermissionsMessage.Permission.TYPE.registerClient();
        PermissionsMessage.AddPlayer.TYPE.registerClient();
        PermissionsMessage.RemovePlayer.TYPE.registerClient();
        PermissionsMessage.ChangePlayerRank.TYPE.registerClient();
        PermissionsMessage.AddPlayerOrFakePlayer.TYPE.registerClient();
        PermissionsMessage.AddRank.TYPE.registerClient();
        PermissionsMessage.RemoveRank.TYPE.registerClient();
        PermissionsMessage.EditRankType.TYPE.registerClient();
        BuildRequestMessage.TYPE.registerClient();
        OpenInventoryMessage.TYPE.registerClient();
        TownHallRenameMessage.TYPE.registerClient();
        MinerSetLevelMessage.TYPE.registerClient();
        RecallCitizenMessage.TYPE.registerClient();
        HireFireMessage.TYPE.registerClient();
        WorkOrderChangeMessage.TYPE.registerClient();
        AssignFieldMessage.TYPE.registerClient();
        AssignmentModeMessage.TYPE.registerClient();
        GuardSetMinePosMessage.TYPE.registerClient();
        RecallCitizenHutMessage.TYPE.registerClient();
        TransferItemsRequestMessage.TYPE.registerClient();
        MarkBuildingDirtyMessage.TYPE.registerClient();
        ChangeFreeToInteractBlockMessage.TYPE.registerClient();
        CreateColonyMessage.TYPE.registerClient();
        ColonyDeleteOwnMessage.TYPE.registerClient();
        ColonyViewRemoveMessage.TYPE.registerClient();
        GiveToolMessage.TYPE.registerClient();
        GetColonyInfoMessage.TYPE.registerClient();
        MarkStoryReadOnItem.TYPE.registerClient();
        PickupBlockMessage.TYPE.registerClient();
        ColonyAbandonOwnMessage.TYPE.registerClient();
        AssignUnassignMessage.TYPE.registerClient();
        OpenCraftingGUIMessage.TYPE.registerClient();
        AddRemoveRecipeMessage.TYPE.registerClient();
        ChangeRecipePriorityMessage.TYPE.registerClient();
        ChangeDeliveryPriorityMessage.TYPE.registerClient();
        ForcePickupMessage.TYPE.registerClient();
        UpgradeWarehouseMessage.TYPE.registerClient();
        TransferItemsToCitizenRequestMessage.TYPE.registerClient();
        UpdateRequestStateMessage.TYPE.registerClient();
        BuildingSetStyleMessage.TYPE.registerClient();
        RecallSingleCitizenMessage.TYPE.registerClient();
        AssignFilterableItemMessage.TYPE.registerClient();
        TeamColonyColorChangeMessage.TYPE.registerClient();
        ColonyFlagChangeMessage.TYPE.registerClient();
        ColonyStructureStyleMessage.TYPE.registerClient();
        PauseCitizenMessage.TYPE.registerClient();
        RestartCitizenMessage.TYPE.registerClient();
        SortBuildingMessage.TYPE.registerClient();
        PostBoxRequestMessage.TYPE.registerClient();
        HireMercenaryMessage.TYPE.registerClient();
        HutRenameMessage.TYPE.registerClient();
        BuildingHiringModeMessage.TYPE.registerClient();
        DecorationBuildRequestMessage.TYPE.registerClient();
        DirectPlaceMessage.TYPE.registerClient();
        TeleportToColonyMessage.TYPE.registerClient();
        EnchanterWorkerSetMessage.TYPE.registerClient();
        InteractionResponse.TYPE.registerClient();
        TryResearchMessage.TYPE.registerClient();
        HireSpiesMessage.TYPE.registerClient();
        AddMinimumStockToBuildingModuleMessage.TYPE.registerClient();
        RemoveMinimumStockFromBuildingModuleMessage.TYPE.registerClient();
        FarmFieldPlotResizeMessage.TYPE.registerClient();
        FarmFieldUpdateSeedMessage.TYPE.registerClient();
        AdjustSkillCitizenMessage.TYPE.registerClient();
        BuilderSelectWorkOrderMessage.TYPE.registerClient();
        TriggerSettingMessage.TYPE.registerClient();
        AssignFilterableEntityMessage.TYPE.registerClient();
        BuildPickUpMessage.TYPE.registerClient();
        SwitchBuildingWithToolMessage.TYPE.registerClient();
        ColonyTextureStyleMessage.TYPE.registerClient();
        MinerRepairLevelMessage.TYPE.registerClient();
        PlantationFieldBuildRequestMessage.TYPE.registerClient();
        PlayerAssistantBuildRequestMessage.TYPE.registerClient();
        ResetFilterableItemMessage.TYPE.registerClient();
        CourierHiringModeMessage.TYPE.registerClient();
        QuarryHiringModeMessage.TYPE.registerClient();
        ToggleRecipeMessage.TYPE.registerClient();
        ColonyNameStyleMessage.TYPE.registerClient();
        InteractionClose.TYPE.registerClient();
        AlterRestaurantMenuItemMessage.TYPE.registerClient();
        TriggerConnectionEventMessage.TYPE.registerClient();
        QueryCitizenAIHistoryMessage.TYPE.registerClient();
        DebugEnablePathfindingMessage.TYPE.registerClient();
        BlockParticleEffectMessage.TYPE.registerClient();
        CompostParticleMessage.TYPE.registerClient();
        ItemParticleEffectMessage.TYPE.registerClient();
        LocalizedParticleEffectMessage.TYPE.registerClient();
        OpenSuggestionWindowMessage.TYPE.registerClient();
        UpdateClientWithCompatibilityMessage.TYPE.registerClient();
        CircleParticleEffectMessage.TYPE.registerClient();
        StreamParticleEffectMessage.TYPE.registerClient();
        SleepingParticleMessage.TYPE.registerClient();
        VanillaParticleMessage.TYPE.registerClient();
        StopMusicMessage.TYPE.registerClient();
        PlayAudioMessage.TYPE.registerClient();
        PlayMusicAtPosMessage.TYPE.registerClient();
        ColonyVisitorViewDataMessage.TYPE.registerClient();
        ColonyViewAnimalViewDataMessage.TYPE.registerClient();
        SyncPathMessage.TYPE.registerClient();
        SyncPathReachedMessage.TYPE.registerClient();
        ReactivateBuildingMessage.TYPE.registerClient();
        PlaySoundForCitizenMessage.TYPE.registerClient();
        OpenDecoBuildWindowMessage.TYPE.registerClient();
        OpenPlantationFieldBuildWindowMessage.TYPE.registerClient();
        SaveStructureNBTMessage.TYPE.registerClient();
        GlobalQuestSyncMessage.TYPE.registerClient();
        GlobalDiseaseSyncMessage.TYPE.registerClient();
        DebugEnableMessage.TYPE.registerClient();
        DebugOutputMessage.TYPE.registerClient();
        OpenBuildingUIMessage.TYPE.registerClient();
        OpenCantFoundColonyWarningMessage.TYPE.registerClient();
        OpenColonyFoundingCovenantMessage.TYPE.registerClient();
        OpenDeleteAbandonColonyMessage.TYPE.registerClient();
        OpenReactivateColonyMessage.TYPE.registerClient();
        TransferRecipeCraftingTeachingMessage.TYPE.registerClient();
        OpenGuiWindowTriggerMessage.TYPE.registerClient();
        ClickGuiButtonTriggerMessage.TYPE.registerClient();
        RemoveFromRallyingListMessage.TYPE.registerClient();
        ToggleBannerRallyGuardsMessage.TYPE.registerClient();
        GlobalResearchTreeMessage.TYPE.registerClient();
        CustomRecipeManagerMessage.TYPE.registerClient();
        SwitchRecipeCraftingTeachingMessage.TYPE.registerClient();
        ColonyListMessage.TYPE.registerClient();
        ResourceScrollSaveWarehouseSnapshotMessage.TYPE.registerClient();
        ItemSettingMessage.TYPE.registerClient();
    }

    /** Blueprint tag options offered by the tag tool (client). */
    public static void registerTagOptions()
    {
        TagManager.registerGlobalTagOption(TAG_WORK);
        TagManager.registerGlobalTagOption(TAG_SIT_IN);
        TagManager.registerGlobalTagOption(TAG_SIT_OUT);
        TagManager.registerGlobalTagOption(TAG_STAND_IN);
        TagManager.registerGlobalTagOption(TAG_STAND_OUT);
        TagManager.registerGlobalTagOption(TAG_SITTING);
        TagManager.registerGlobalTagOption(BUILDING_SIGN);
        TagManager.registerSpecificTagOption(TAG_GATE, b -> b instanceof BlockHutGateHouse);
        TagManager.registerSpecificTagOption(TAG_KNIGHT, b -> b instanceof BlockHutGateHouse);
        TagManager.registerSpecificTagOption(TAG_ARCHER, b -> b instanceof BlockHutGateHouse);
        TagManager.registerSpecificTagOption(TAG_COBBLE, b -> b instanceof BlockHutMiner);
        TagManager.registerSpecificTagOption(TAG_LADDER, b -> b instanceof BlockHutMiner);
        TagManager.registerSpecificTagOption(TAG_LEISURE, b -> b instanceof BlockDecorationController);
        TagManager.registerSpecificTagOption(TAG_SITTING, b -> b instanceof BlockHutSchool);
        for (final String fieldTag : SchematicTagConstants.getPlantationTags())
             {
                 TagManager.registerSpecificTagOption(fieldTag, b -> b instanceof BlockPlantationField);
             }
    }

    private static void logIncompatibilities()
    {
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("minecolonies_tweaks"))
        {
            Log.getLogger().warn("|======================================================================================================================================|");
            Log.getLogger().warn("|                                                                                                                                      |");
            Log.getLogger().warn("| Minecolonies has detected an addon mod that alters Minecolonies core code recklessly: 'Tweaks/Compatibility addon for Minecolonies'. |");
            Log.getLogger().warn("|          Please report any bugs or issues you find directly to the authors of this addon, as the Official Minecolonies Team          |");
            Log.getLogger().warn("|               will not be able to provide you any support with potential issues that will arise when using this addon.               |");
            Log.getLogger().warn("|                                                                                                                                      |");
            Log.getLogger().warn("|======================================================================================================================================|");
        }
    }
}
