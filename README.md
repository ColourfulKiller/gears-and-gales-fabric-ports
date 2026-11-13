# MineColonies for Fabric 1.21.11 (unofficial port)

An unofficial port of **MineColonies** and the four libraries it needs (**Structurize**, **BlockUI**, **Domum
Ornamentum**, **MultiPiston**) from NeoForge 1.21.1 to **Fabric 1.21.11**. It was made for the
Gears and Gales Fabric modpack, where it runs next to Create Fly and Shine: VFX 3.0.

All five mods are by [LDT Team](https://github.com/ldtteam) and licensed under the GPL-3.0. This port is GPL-3.0 too
(see `LICENSE` and each mod's own `LICENSE`). **This is not an official LDT Team release.** Please report bugs in this
repository, not to LDT Team.

| Mod | Upstream source | Version ported |
|---|---|---|
| MineColonies | [ldtteam/minecolonies](https://github.com/ldtteam/minecolonies) | tag `v1.21.1-1.1.1403` |
| Structurize | [ldtteam/Structurize](https://github.com/ldtteam/Structurize) | NeoForge 1.21.1 source |
| BlockUI | [ldtteam/BlockUI](https://github.com/ldtteam/BlockUI) | NeoForge 1.21.1 source |
| Domum Ornamentum | [ldtteam/Domum-Ornamentum](https://github.com/ldtteam/Domum-Ornamentum) | NeoForge 1.21.1 source |
| MultiPiston | [ldtteam/Piston-Unlimited](https://github.com/ldtteam/Piston-Unlimited) | tag `v1.21.1-1.2.58` |

## Requirements to play

Minecraft 1.21.11, Fabric Loader 0.20.1+, Fabric API, and
[Forge Config API Port](https://www.curseforge.com/minecraft/mc-mods/forge-config-api-port-fabric) 21.11.x. Install all
five jars; MineColonies needs the other four. Trinkets is optional (the Build Goggles can go in its face slot).

## Building from source

You need JDK 25 to run Gradle (the mods compile for Java 21; Gradle picks the JDK via
`gradle/gradle-daemon-jvm.properties`) and the Forge Config API Port jar
(`ForgeConfigAPIPort-v21.11.1-mc1.21.11-Fabric.jar`) in a local folder. Build in this order, because each mod compiles
against the previous ones' dev jars:

```
cd blockui      && gradlew build -PlibsDir=C:/path/to/jars
cd ../domum     && gradlew build -PlibsDir=C:/path/to/jars
cd ../structurize && gradlew build -PlibsDir=C:/path/to/jars
cd ../multipiston && gradlew build -PlibsDir=C:/path/to/jars
cd ../minecolonies && gradlew build -PlibsDir=C:/path/to/jars
```

The jars land in each mod's `build/libs` (use the one without `-sources`). The decompiled Minecraft source the notes
below mention (`mc-src-1.21.11`) is not in this repository; `gradlew genSources` recreates it.

The rest of this file is the porting log: what changed, the shared compatibility layer, and known gaps.

| Mod | Status |
|---|---|
| BlockUI | **done**: builds, loads on a Fabric server, passed in-game test (all 4 test windows). |
| Structurize | **done**: builds, server init passes, works in game. Blueprint preview renderer rewritten for 1.21.11 GPU pipelines (see below). Datagen + NeoForge-only client model code parked in `structurize/neoforge-only`; generated tags written by hand. |
| Domum Ornamentum | **done**: builds, server init passes, passed in-game test (cutter, retextured blocks and items). Datagen + JEI + old model classes parked in `domum/neoforge-only`. Retexturing rewritten for the Fabric renderer API (see below). Ghost placement preview deferred (eoforge-only/deferred`). |
| MultiPiston | **done**: builds, server init passes, works in game. Source is upstream `ldtteam/Piston-Unlimited` tag v1.21.1-1.2.58 (the mod's repo name). Small port: block/BE API changes, entrypoint, recipe + item definition. |
| MineColonies | **done**: builds, server init passes, ships in Gears and Gales Fabric 1.1.0 (2026-11-13); long play-testing still pending. Source = tag v1.21.1-1.1.1403. Compile errors 6,598 -> 0. Rendering moved to 1.21.11 render states (`CitizenRenderState` / `RaiderRenderState`, citizen + raider + mercenary + cavalry renderers, all block-entity renderers, spear special item model, banner picker, colony border lines on CPU-cached meshes); recipes via `RecipeHooks` (server-side) and Fabric recipe sync for client GUIs (shaped / shapeless / smelting); global loot modifiers + compostables data map via the shared layer; item definitions from `tools/gen_item_definitions_minecolonies.py` (goggles condition `minecolonies:goggles_disabled`); lang files extracted from the 1.21.1 release jar. Known gaps: JEI / JourneyMap / Tinkers / Dynamic Trees compat parked in eoforge-only/`; scarecrow lantern light level; shield-block damage split simplified; flag placeholder item in creative not drawn. |

## Building

Each port is a Fabric Loom 1.19.4 project (Mojang names). Gradle 9.9.1 runs on JDK 25; the mods compile for Java 21.

```
cd ports/blockui
gradlew build          # jar in build/libs
gradlew genSources     # decompiled Minecraft 1.21.11 (also extracted to ports/mc-src-1.21.11 for grepping)
```

The original NeoForge build files are kept next to each port as `*.neoforge`.

## Shared compatibility layer (`com.ldtteam.fabric`, inside BlockUI)

Replaces the NeoForge APIs the LDT mods share, so Structurize and MineColonies can port onto it:

| NeoForge | Fabric port |
|---|---|
| `FMLEnvironment.production` / `.dist.isClient()` | `Env.PRODUCTION` / `Env.isClient()` |
| `LogicalSidedProvider.WORKQUEUE...tell(...)` | `Env.runOnMainThread(...)` |
| `IPayloadContext` | etwork.PayloadContext` |
| `PacketDistributor.sendTo...` | etwork.Networking.sendTo...` |
| `PayloadRegistrar` | `PlayMessageType.register()` (both sides) + `registerClient()` (client) |
| `Minecraft.pushGuiLayer / popGuiLayer` | `ScreenLayers.push / pop` |
| `common.util.Lazy` | `Lazy` |
| `DeferredRegister` / `DeferredHolder` / `DeferredBlock` / `DeferredItem` | `registry.*` (register() from the main entrypoint; mixins fill in 1.21.2+ block/item ids) |
| IBlockExtension positional getSoundType / getExplosionResistance / getCloneItemStack | `block.PositionalBlockHooks` (explosion + pick-block wired by mixins; sound not called) |
| `appendHoverText(..., List<Component>, ...)` | new signature + `TooltipList` adapter (`tools/port_block_item_hooks.py`) |
| `client.model.data.ModelData` | `ModelData` (always `EMPTY`) |
| `ModConfigSpec` / config screen | unchanged API, provided by Forge Config API Port (`Configurations` now takes the mod id) |

## Porting scripts (`ports/tools`)

Run on a `src/main/java` tree, in this order. They only do rewrites that are safe everywhere and print what they did.

1. `port_mechanical.py`: `ResourceLocation` → `Identifier`, FMLEnvironment → `Env`, networking → shim.
2. `port_gui_pose.py`: GUI code from `PoseStack` to `Matrix3x2fStack`; prints every dropped z value to check.
   Pass `--skip` for code that really renders in 3D.
3. `port_api_fixes.py`: modifier keys moved to `Minecraft`, removed RenderSystem blend/depth calls,
   `setShaderColor` → `UiRenderMacros.setTint`, `Registry.get` → `getValue`.

4. `port_block_item_hooks.py`: NeoForge positional block hooks and the 1.21.5 tooltip signature.

Everything else is by hand, checked against `mc-src-1.21.11`.

## BlockUI: what changed, and known gaps

Rendering was rewritten for the 1.21.6+ GUI render-state system: `UiRenderMacros` and `BOGuiGraphics` record into
`GuiGraphics` instead of drawing with Tesselator / BufferUploader, `BOScreen` builds BlockUI's pixel-space transform
as the starting 2D matrix instead of replacing the projection, and scissors use `GuiGraphics.enableScissor`.
GUI sprites moved from per-mod atlases into the vanilla GUI atlas (`assets/minecraft/atlases/gui.json`).

Known gaps, each marked in the code:

- **Block-state icons** draw the block's item, not the exact block state + block entity + fluid. Needs a
  picture-in-picture renderer (Fabric `SpecialGuiElementRegistry`).
- **Item property overrides** (`ItemIconWithProperties`, e.g. a fixed compass angle) are parsed but not applied:
  vanilla's `ItemProperties` registry was removed in 1.21.4.
- **Animated out-of-jar textures** show their first frame.
- **World-space GUIs (hooks)** stay disabled, as they already were upstream on 1.21.1 ("need port fix").
- **Text linear filtering** for small scaled text (a NeoForge render option) is gone.
- **Layered windows** restore the screen beneath them and draw it underneath; NeoForge had real layers.

Small behaviour fix: config watchers now receive the real old value (the 1.21.1 code passed the new value twice).

## Domum Ornamentum: what changed, and known gaps

Retexturing: NeoForge used a custom geometry loader (`domum_ornamentum:materially_textured`) plus eoforge:composite`
models and ModelData. The Fabric port:

- **Models are plain vanilla JSON** (`tools/port_neoforge_models.py`): the marker loader is dropped, composites are
  flattened (children resolved through their parents, texture variables prefixed per child, elements concatenated).
- **Blocks:** a `ModelLoadingPlugin` wraps every `IMateriallyTexturedBlock` state model in
  `MateriallyTexturedBlockStateModel`, which reads the block entity's `MaterialTextureData` (Fabric render data) and
  swaps component sprites for the chosen block's face (`Retexturer`, same UV remap and `(stateId << 8) | tint` tint
  packing as the original). Results are cached per model part + texture data.
- **Items:** generated `assets/domum_ornamentum/items/*.json` (`tools/gen_item_definitions.py`) use the item model type
  `domum_ornamentum:materially_textured`; door / trapdoor / panel / post variants are a `select` on block_state `type`
  (the 1.21.1 item overrides). Item tints go through the layer tint array.
- **Recipes** converted to the 1.21.2 ingredient format (`tools/port_recipes_1_21_2.py`).

Known gaps: per-child `render_type` is gone (those blocks render in the translucent layer); block sounds aren't
position-aware; DynamicTimberFrameBlock's position-aware `rotate` isn't called by vanilla (Structurize will call it);
the pre-1.21 item NBT upgrade path is a no-op; the ghost placement preview is deferred.

## Structurize: what changed, and known gaps

- **Blueprint preview** (`client/BlueprintRenderer`): blocks are meshed once with Fabric's block renderer (so Domum
  blocks retexture) into persistent GPU buffers and drawn with the vanilla moving-block pipelines; the DynamicTransforms
  uniform carries the offset and the preview transparency. Block entities and entities are extracted to render states
  and submitted to vanilla's queue during `BEFORE_ENTITIES`.
- **Render stages**: `client/render/RenderLevelStage` replaces NeoForge's RenderLevelStageEvent (same getter names);
  Fabric's WorldRenderEvents fire the NeoForge stage names. MineColonies subscribes to `RenderLevelStage.EVENT`.
  `WorldRenderMacros` render types are RenderPipelines now.
- **Tag substitution block**: render-state block entity renderer + a `structurize:tag_substitution` special item model.
- Keys: NeoForge conflict contexts / modifiers are gone; the rotate keys require Shift in code.
- Data: recipes converted to 1.21.2 ingredients; tags the datagen would have produced are hand-written; DataVersion
  knows 1.21.2 – 1.21.11.

Known gaps: the hotbar "highlight tip" (scan tool slot, tag substitution content) has no Fabric hook; entity
automation item handlers (NeoForge `ENTITY_AUTOMATION`) aren't looked up.

## Shared layer additions for Structurize / MineColonies

`com.ldtteam.fabric.items` (IItemHandler, IItemHandlerModifiable, InvWrapper, ItemStackHandler, ItemHandlerHelper,
ItemHandlers: block lookup via Containers then Fabric's Transfer API), bt.NbtIO` (CompoundTag bridge for 1.21.6
ValueInput / ValueOutput, entity save/load), `util.TriPredicate`, `PlayerHooks.playNotifySound`,
`PositionalBlockHooks.rotate` / `rotateAt`. Scripts: `port_nbt_optional.py` (line-targeted Optional NBT getters),
`port_interaction_result.py`, `port_be_valueio.py`, `port_recipes_1_21_2.py`, `gen_item_definitions.py`,
`port_neoforge_models.py`; `port_mechanical.py` gained the matching import / rename rules.

## Shared event layer (for MineColonies)

`com.ldtteam.fabric.event`: NeoForge-shaped event classes (generated by `tools/gen_event_shims.py`), a reflection `@SubscribeEvent` bus (`NeoForge.EVENT_BUS`), and `EventBridge` / `ClientEventBridge` firing them from Fabric callbacks and the mixins in `ldtteam_events.mixins.json` (block place, farmland trample, entity join, game events, dimension travel, spawn checks, mob conversion, item toss / pickup, bow release, explosions, player ticks). Handlers port by import change only. Fabric can only allow/deny damage, so `LivingDamageEvent.Pre#setNewDamage(0)` denies it.

More scripts: `port_identifier_ctor.py`, `port_line_renames.py` (rename only where javac reports the method missing), `port_package_moves.py` (re-point imports of moved vanilla classes using the decompiled 1.21.11 source).

## Testing BlockUI in game

Copy `blockui/build/libs/blockui-fabric-1.21.11-fabric-0.1.0.jar` into the Gears and Gales Fabric instance's
`mods` folder (it needs Fabric API and Forge Config API Port, both in the pack). In a world, press
**Ctrl + Alt + Shift + X** for BlockUI's test windows. Check text, buttons, scrolling lists, tooltips, item and entity
icons, and the dropdowns. Note anything that looks wrong and the log lines around it.
