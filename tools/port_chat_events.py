"""1.21.5: ClickEvent / HoverEvent became sealed interfaces with one record per action.

    python -I ports/tools/port_chat_events.py <src/main/java dir>

new HoverEvent(HoverEvent.Action.SHOW_TEXT, x)    -> new HoverEvent.ShowText(x)
new ClickEvent(ClickEvent.Action.RUN_COMMAND, x)  -> new ClickEvent.RunCommand(x)    (also SUGGEST_COMMAND, COPY_TO_CLIPBOARD,
OPEN_URL -> new ClickEvent.OpenUrl(java.net.URI.create(x)), OPEN_FILE, CHANGE_PAGE -> Integer.parseInt). Idempotent.
"""
import re
import sys
from pathlib import Path

CLICK = {"RUN_COMMAND": ("RunCommand", "{}"), "SUGGEST_COMMAND": ("SuggestCommand", "{}"),
         "COPY_TO_CLIPBOARD": ("CopyToClipboard", "{}"), "OPEN_URL": ("OpenUrl", "java.net.URI.create({})"),
         "OPEN_FILE": ("OpenFile", "{}"), "CHANGE_PAGE": ("ChangePage", "Integer.parseInt({})")}
HOVER = {"SHOW_TEXT": "ShowText", "SHOW_ITEM": "ShowItem", "SHOW_ENTITY": "ShowEntity"}


def close_paren(t, i):
    depth = 0
    while i < len(t):
        depth += t[i] == "("
        depth -= t[i] == ")"
        if depth == 0:
            return i
        i += 1
    return -1


def rewrite(t):
    pat = re.compile(r"new (Click|Hover)Event\(\s*(?:(?:net\.minecraft\.network\.chat\.)?(?:Click|Hover)Event\.)?Action\.(\w+)\s*,")
    out, pos = [], 0
    while True:
        m = pat.search(t, pos)
        if not m:
            out.append(t[pos:])
            return "".join(out)
        kind, action = m.group(1), m.group(2)
        open_idx = t.index("(", m.start())
        end = close_paren(t, open_idx)
        arg = t[m.end():end].strip()
        if kind == "Hover":
            new = f"new HoverEvent.{HOVER[action]}({arg})"
        else:
            rec, wrap = CLICK[action]
            new = f"new ClickEvent.{rec}({wrap.format(arg)})"
        out.append(t[pos:m.start()] + new)
        pos = end + 1


n = 0
for f in Path(sys.argv[1]).rglob("*.java"):
    t = f.read_text(encoding="utf-8")
    if "Event.Action." not in t and "Action.SHOW_TEXT" not in t:
        continue
    new = rewrite(t)
    if new != t:
        f.write_text(new, encoding="utf-8")
        n += 1
print(n, "files")
