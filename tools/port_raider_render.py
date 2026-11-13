"""MineColonies 1.21.11: raider models / renderers onto RaiderRenderState + AbstractRaiderRenderer.

    python -I ports/tools/port_raider_render.py <src/main/java dir>

Models: HumanoidModel<AbstractEntityMinecoloniesMonster> -> HumanoidModel<RaiderRenderState>; the culture base models
(AmazonModel, EgyptianModel) lose their type parameter; setupAnim(entity, 5 floats) -> setupAnim(RaiderRenderState)
with the five floats re-declared from the state.
Renderers: the six Abstract<Culture> renderers become thin subclasses of AbstractRaiderRenderer; the concrete
renderers' getTextureLocation(entity) take the state. Idempotent.
"""
import re
import sys
from pathlib import Path

ROOT = Path(sys.argv[1])
STATE_IMPORT = "import com.minecolonies.api.client.render.modeltype.RaiderRenderState;"
CULTURES = ["Amazon", "Barbarian", "DrownedPirate", "Egyptian", "Norsemen", "Pirate"]


def add_import(t, imp):
    if imp in t:
        return t
    return re.sub(r"(package [\w.]+;\r?\n)", r"\1\n" + imp + "\n", t, count=1)


def port_model(t):
    t = re.sub(r"public class (AmazonModel|EgyptianModel)<T extends AbstractEntityMinecoloniesMonster> extends HumanoidModel<AbstractEntityMinecoloniesMonster>",
               r"public class \1 extends HumanoidModel<RaiderRenderState>", t)
    t = t.replace("extends HumanoidModel<AbstractEntityMinecoloniesMonster>", "extends HumanoidModel<RaiderRenderState>")
    t = re.sub(r"extends (AmazonModel|EgyptianModel)<\w+>", r"extends \1", t)
    m = re.search(r"setupAnim\((?:@NotNull )?(?:final )?AbstractEntityMinecoloniesMonster (\w+), (?:final )?float (\w+), (?:final )?float (\w+), "
                  r"(?:final )?float (\w+), (?:final )?float (\w+), (?:final )?float (\w+)\)", t)
    if m:
        state, ls, lsa, age, yaw, pitch = m.groups()
        t = t[:m.start()] + f"setupAnim(final RaiderRenderState {state})" + t[m.end():]
        sup = re.search(rf"super\.setupAnim\({state}(?:, \w+){{5}}\);", t)
        if sup:
            decl = (f"super.setupAnim({state});\n"
                    f"        final float {ls} = {state}.walkAnimationPos;\n"
                    f"        final float {lsa} = {state}.walkAnimationSpeed;\n"
                    f"        final float {age} = {state}.ageInTicks;\n"
                    f"        final float {yaw} = {state}.yRot;\n"
                    f"        final float {pitch} = {state}.xRot;")
            t = t[:sup.start()] + decl + t[sup.end():]
    return add_import(t, STATE_IMPORT) if "RaiderRenderState" in t and "package com.minecolonies.api.client.render.modeltype;" not in t else t


def abstract_renderer(culture, pkg):
    return f"""package {pkg};

import com.minecolonies.api.client.render.modeltype.RaiderRenderState;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMonster;
import com.minecolonies.core.client.render.mobs.AbstractRaiderRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Abstract for rendering {culture} raiders (Fabric 1.21.11: everything shared lives in AbstractRaiderRenderer).
 */
public abstract class AbstractRenderer{culture}<T extends AbstractEntityMinecoloniesMonster, M extends HumanoidModel<RaiderRenderState>> extends AbstractRaiderRenderer<T, M>
{{
    public AbstractRenderer{culture}(final EntityRendererProvider.Context context, final M modelBipedIn, final float shadowSize)
    {{
        super(context, modelBipedIn, shadowSize);
    }}
}}
"""


def port_renderer(t):
    t = t.replace("HumanoidModel<AbstractEntityMinecoloniesMonster>", "HumanoidModel<RaiderRenderState>")
    t = re.sub(r"getTextureLocation\((?:@NotNull )?(?:final )?AbstractEntityMinecoloniesMonster (\w+)\)", r"getTextureLocation(final RaiderRenderState \1)", t)
    t = re.sub(r"ModelLayers\.PLAYER_OUTER_ARMOR\b", "ModelLayers.PLAYER_ARMOR.chest()", t)
    t = re.sub(r"ModelLayers\.PLAYER_INNER_ARMOR\b", "ModelLayers.PLAYER_ARMOR.legs()", t)
    return add_import(t, STATE_IMPORT) if "RaiderRenderState" in t else t


n = 0
for f in ROOT.rglob("*.java"):
    t = f.read_text(encoding="utf-8")
    o = t
    m = re.fullmatch(r"AbstractRenderer(\w+)\.java", f.name)
    if m and m.group(1) in CULTURES:
        pkg = re.search(r"package ([\w.]+);", t).group(1)
        t = abstract_renderer(m.group(1), pkg)
    elif re.search(r"extends AbstractRenderer(" + "|".join(CULTURES) + r")<", t):
        t = port_renderer(t)
    elif "HumanoidModel<AbstractEntityMinecoloniesMonster>" in t or re.search(r"extends (AmazonModel|EgyptianModel|NorsemenModel)\b", t):
        t = port_model(t)
    if t != o:
        f.write_text(t, encoding="utf-8")
        n += 1
print(n, "files")
