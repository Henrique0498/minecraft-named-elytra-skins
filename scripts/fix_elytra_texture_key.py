#!/usr/bin/env python3
import sys, re
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
# Preferir a nova pasta 'resource' se existir
PACK_ROOT = (REPO_ROOT/"resource") if (REPO_ROOT/"resource").exists() else REPO_ROOT
changed = 0

prop_roots = [
    PACK_ROOT/"assets"/"minecraft"/"optifine"/"cit",
    PACK_ROOT/"assets"/"minecraft"/"citresewn"/"cit",
]

def process_file(p: Path):
    global changed
    try:
        txt = p.read_text(encoding='utf-8')
    except Exception:
        return
    # only handle type=elytra
    if not re.search(r"^type\s*=\s*elytra\s*$", txt, flags=re.MULTILINE):
        return
    # find existing texture line
    m = re.search(r"^texture\s*=\s*(.+)$", txt, flags=re.MULTILINE)
    if not m:
        return
    texture_val = m.group(1).strip()
    # if already has texture.elytra, skip
    if re.search(r"^texture\.elytra\s*=\s*.+$", txt, flags=re.MULTILINE):
        return
    # insert texture.elytra after texture line
    new_txt = re.sub(r"^texture\s*=\s*.+$", lambda _:
                     f"texture={texture_val}\ntexture.elytra={texture_val}",
                     txt, count=1, flags=re.MULTILINE)
    if new_txt != txt:
        p.write_text(new_txt, encoding='utf-8')
        changed += 1

for root in prop_roots:
    if not root.exists():
        continue
    for p in root.rglob("*.properties"):
        process_file(p)

print(f"Updated {changed} properties files with texture.elytra")
