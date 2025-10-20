#!/usr/bin/env python3
"""
rebuild_pack.py

Uso básico:
    python rebuild_pack.py --src "C:/minhas_skins" --out "./MeuPack"

O que faz (modo padrão from-images):
    - Varre recursivamente --src procurando arquivos de imagem (.png, .jpg, .jpeg, .webp)
    - Para cada skin, cria a estrutura CIT correta para Elytra, compatível com OptiFine e Fabric (CIT Resewn):
            out/assets/minecraft/optifine/cit/elytra/icon/<nome>.png
            out/assets/minecraft/optifine/cit/elytra/model/<nome>.png
            out/assets/minecraft/optifine/cit/elytra/properties/<nome>_icon.properties
            out/assets/minecraft/optifine/cit/elytra/properties/<nome>_model.properties
        (Opcional) espelha a mesma estrutura para:
            out/assets/citresewn/cit/elytra/...

    - As regras de CIT usam trigger por nome do item (renomear Elytra no anvil):
            nbt.display.Name=ipattern:*<Nome Da Skin>*

Modo reorganize-pack:
    - Se você já tem um pack (com pastas icon/model/properties), pode rodar:
            python rebuild_pack.py --src "./Elytras+" --out "./Elytras+_fixed" --mode reorganize-pack
        que normaliza a estrutura, corrige caminhos de texture e (opcionalmente) cria cópia CIT Resewn.

Observações:
    - Não é necessário criar models JSON para este caso; CIT usa diretamente as texturas.
    - Este script não altera seus arquivos originais; ele escreve em --out.
"""
import argparse
import os
import shutil
import json
import re
from pathlib import Path
from datetime import datetime

IMG_EXTS = {'.png', '.jpg', '.jpeg', '.webp'}

def sanitize(name: str) -> str:
    # remove espaços e caracteres estranhos, lowercase
    s = name.strip()
    s = s.replace(' ', '_')
    s = re.sub(r'[^A-Za-z0-9_\-]', '', s)
    s = s.lower()
    if s == '':
        s = 'texture'
    return s

def ensure_dir(p: Path):
    if not p.exists():
        p.mkdir(parents=True, exist_ok=True)

def make_pack_mcmeta(out_dir: Path, description="Rebuilt resource pack", pack_format: int | None = None):
    meta = {
        "pack": {
            # Se não informado, usa um valor padrão razoável; ajuste via --pack-format para sua versão.
            "pack_format": pack_format if pack_format is not None else 8,
            "description": description
        }
    }
    path = out_dir / 'pack.mcmeta'
    if path.exists():
        print(f"pack.mcmeta já existe em {path}, não sobrescrevendo.")
        return
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(meta, f, indent=2, ensure_ascii=False)
    print(f"Criei pack.mcmeta em {path}")

def to_display_name(name: str) -> str:
    """Converte "citrus_elytra" -> "Citrus Elytra" para usar no ipattern."""
    parts = re.split(r"[_\-]+", name)
    parts = [p for p in parts if p]
    if not parts:
        return "Elytra"
    return " ".join(w.capitalize() for w in parts)

def write_properties(path: Path, lines: list[str]):
    ensure_dir(path.parent)
    with open(path, 'w', encoding='utf-8') as f:
        f.write("\n".join(lines) + "\n")

def make_properties_pair(out_props_dir: Path, skin_name: str, display_name: str):
    """Cria os dois .properties (item + elytra) seguindo o padrão do pack."""
    # Arquivo para o item (ícone no inventário)
    icon_props = [
        "# Auto-gerado pelo rebuild_pack.py",
        "type=item",
        "items=elytra",
        "matchItems=elytra",
        f"texture=optifine/cit/elytra/icon/{skin_name}.png",
        f"nbt.display.Name=ipattern:*{display_name}*",
        "",
        "# " + datetime.utcnow().isoformat() + "Z",
    ]
    # Arquivo para a elytra equipada no jogador
    model_props = [
        "# Auto-gerado pelo rebuild_pack.py",
        "type=elytra",
        "items=elytra",
        "matchItems=elytra",
        f"texture=optifine/cit/elytra/model/{skin_name}.png",
        f"nbt.display.Name=ipattern:*{display_name}*",
        "",
        "# " + datetime.utcnow().isoformat() + "Z",
    ]

    write_properties(out_props_dir / f"{skin_name}_icon.properties", icon_props)
    write_properties(out_props_dir / f"{skin_name}_model.properties", model_props)

def mirror_to_citresewn(out_root: Path):
    """Espelha o conteúdo de assets/minecraft/optifine/cit para assets/citresewn/cit.
    Mantém os mesmos .properties (referenciando caminho optifine), o que funciona pois
    as texturas também serão copiadas para optifine/."""
    mc_root = out_root / "assets" / "minecraft" / "optifine" / "cit"
    cit_root = out_root / "assets" / "citresewn" / "cit"
    if not mc_root.exists():
        return
    for src_dir, _, files in os.walk(mc_root):
        rel = Path(src_dir).relative_to(mc_root)
        dst_dir = cit_root / rel
        ensure_dir(Path(dst_dir))
        for fn in files:
            sp = Path(src_dir) / fn
            dp = dst_dir / fn
            # Não sobrescreve se já existe o mesmo arquivo
            if not dp.exists():
                shutil.copy2(sp, dp)

def copy_image(srcp: Path, dstp: Path):
    if dstp.exists():
        i = 1
        while True:
            alt = dstp.with_name(f"{dstp.stem}_{i}{dstp.suffix}")
            if not alt.exists():
                shutil.copy2(srcp, alt)
                print(f"Arquivo existe -> copiado como {alt}")
                return alt
            i += 1
    else:
        ensure_dir(dstp.parent)
        shutil.copy2(srcp, dstp)
        return dstp

def handle_from_images(src: Path, out: Path, make_citresewn: bool, pack_format: int | None = None):
    """Constrói a estrutura CIT a partir de um diretório de imagens soltas."""
    out_optifine_icon = out / "assets" / "minecraft" / "optifine" / "cit" / "elytra" / "icon"
    out_optifine_model = out / "assets" / "minecraft" / "optifine" / "cit" / "elytra" / "model"
    out_optifine_props = out / "assets" / "minecraft" / "optifine" / "cit" / "elytra" / "properties"

    for p in [out_optifine_icon, out_optifine_model, out_optifine_props]:
        ensure_dir(p)

    found = 0
    for root, _, files in os.walk(src):
        for fname in files:
            ext = Path(fname).suffix.lower()
            if ext not in IMG_EXTS:
                continue
            found += 1
            src_f = Path(root) / fname
            base = Path(fname).stem
            name = sanitize(base)
            display = to_display_name(name)

            # Heurística: se o nome termina com _icon ou _model, manda cada um pro seu lugar;
            # caso contrário, copia a mesma imagem para ambos.
            if name.endswith("_icon"):
                core = name[:-5]
                copy_image(src_f, out_optifine_icon / f"{core}.png")
                make_properties_pair(out_optifine_props, core, to_display_name(core))
            elif name.endswith("_model"):
                core = name[:-6]
                copy_image(src_f, out_optifine_model / f"{core}.png")
                make_properties_pair(out_optifine_props, core, to_display_name(core))
            else:
                copy_image(src_f, out_optifine_icon / f"{name}.png")
                copy_image(src_f, out_optifine_model / f"{name}.png")
                make_properties_pair(out_optifine_props, name, display)

    if found == 0:
        print("Nenhuma imagem encontrada em", src)
        return 0

    make_pack_mcmeta(out, description=f"Rebuilt pack from {src}", pack_format=pack_format)
    if make_citresewn:
        mirror_to_citresewn(out)
    print(f"Processados {found} arquivos. Estrutura criada em: {out.resolve()}")
    return found

def handle_reorganize_pack(src: Path, out: Path, make_citresewn: bool, pack_format: int | None = None):
    """Reorganiza um pack existente (Elytras+) para o layout esperado e copia para out."""
    # Copia tudo que já existe
    if out.exists():
        print("Diretório de saída já existe:", out)
    ensure_dir(out)

    # Copiar pack.mcmeta se houver
    pm = src / "pack.mcmeta"
    if pm.exists():
        shutil.copy2(pm, out / "pack.mcmeta")

    # Antes de mais nada, tente copiar as pastas de texturas e props existentes
    # origem típica: assets/minecraft/optifine/cit/elytra/{icon,model,properties}
    base_in = src / "assets" / "minecraft" / "optifine" / "cit" / "elytra"
    out_icon = out / "assets" / "minecraft" / "optifine" / "cit" / "elytra" / "icon"
    out_model = out / "assets" / "minecraft" / "optifine" / "cit" / "elytra" / "model"
    out_props = out / "assets" / "minecraft" / "optifine" / "cit" / "elytra" / "properties"
    for d in [out_icon, out_model, out_props]:
        ensure_dir(d)

    if base_in.exists():
        for sub in ["icon", "model", "properties"]:
            sdir = base_in / sub
            if sdir.exists():
                for root, _, files in os.walk(sdir):
                    rel = Path(root).relative_to(base_in / sub)
                    target_dir = (out_icon if sub == "icon" else out_model if sub == "model" else out_props) / rel
                    ensure_dir(target_dir)
                    for fn in files:
                        shutil.copy2(Path(root) / fn, target_dir / fn)

    # Corrigir/certificar que cada skin tem seus dois .properties e caminhos
    # Vamos varrer os .properties na saída e ajustar
    adjusted = 0
    for root, _, files in os.walk(out_props):
        for fn in files:
            if not fn.endswith(".properties"):
                continue
            p = Path(root) / fn
            with open(p, 'r', encoding='utf-8') as f:
                content = f.read()
            orig = content

            # Garanta matchItems=elytra além de items=elytra
            if "matchItems=elytra" not in content:
                content += "\nmatchItems=elytra\n"

            # Corrija o caminho de textura conforme o type
            # Se type=elytra => model; se type=item => icon
            type_elytra = re.search(r"^type\s*=\s*elytra\s*$", content, re.MULTILINE) is not None
            type_item = re.search(r"^type\s*=\s*item\s*$", content, re.MULTILINE) is not None

            name_guess = None
            m = re.search(r"texture\s*=\s*.*?/(icon|model)/([^\n\r\.]+)", content)
            if m:
                name_guess = m.group(2)
            else:
                # tenta pelo próprio arquivo
                stem = Path(fn).stem
                name_guess = stem.replace("_icon", "").replace("_model", "")

            if type_elytra:
                content = re.sub(r"^texture\s*=.*$", f"texture=optifine/cit/elytra/model/{name_guess}.png", content, flags=re.MULTILINE)
            if type_item:
                content = re.sub(r"^texture\s*=.*$", f"texture=optifine/cit/elytra/icon/{name_guess}.png", content, flags=re.MULTILINE)

            if content != orig:
                with open(p, 'w', encoding='utf-8') as f:
                    f.write(content if content.endswith("\n") else content + "\n")
                adjusted += 1

    print(f"Ajustados {adjusted} arquivos .properties")

    # Espelha para citresewn, se solicitado
    if make_citresewn:
        mirror_to_citresewn(out)

    make_pack_mcmeta(out, description=f"Reorganized from {src}", pack_format=pack_format)
    print(f"Reorganização concluída em: {out.resolve()}")
    return True

def main(src: Path, out: Path, mode: str, make_citresewn: bool, pack_format: int | None = None):
    if not src.exists():
        print("Diretório de origem não existe:", src)
        return
    if mode == "reorganize-pack":
        handle_reorganize_pack(src, out, make_citresewn, pack_format)
    else:
        handle_from_images(src, out, make_citresewn, pack_format)

if __name__ == '__main__':
    ap = argparse.ArgumentParser(description="Cria/organiza um resource pack de Elytras (CIT) compatível com OptiFine e Fabric (CIT Resewn)")
    ap.add_argument("--src", required=True, help="Diretório de origem (imagens soltas OU pack existente)")
    ap.add_argument("--out", required=True, help="Diretório de saída (novo resourcepack)")
    ap.add_argument("--mode", choices=["from-images", "reorganize-pack"], default="from-images", help="Modo de operação")
    ap.add_argument("--citresewn", action="store_true", help="Espelhar para assets/citresewn/cit (recomendado para Fabric)")
    ap.add_argument("--pack-format", type=int, default=None, help="Força pack_format no pack.mcmeta gerado (não sobrescreve se já existir)")
    args = ap.parse_args()

    src = Path(args.src).expanduser().resolve()
    out = Path(args.out).expanduser().resolve()
    main(src, out, args.mode, args.citresewn, args.pack_format)
