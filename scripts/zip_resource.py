#!/usr/bin/env python3
"""
zip_resource.py

Cria um .zip com o conteúdo de ./resource (sem incluir a pasta "resource" na raiz do zip)
e salva em ./dist/<nome>.zip.

Por que importa: alguns servidores/leitores esperam que o zip tenha "pack.mcmeta" e "assets/"
diretamente na raiz, e não dentro de uma pasta extra.

Uso:
  python scripts/zip_resource.py                   # gera dist/elytras-plus.zip a partir de ./resource
  python scripts/zip_resource.py --name meu_pack   # gera dist/meu_pack.zip
  python scripts/zip_resource.py --src ./resource --dist ./dist --name elytras-plus

Requisitos: Python 3.8+
"""
import argparse
import os
from pathlib import Path
import zipfile
import sys

REPO_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_SRC = REPO_ROOT / "resource"
DEFAULT_DIST = REPO_ROOT / "dist"
DEFAULT_NAME = "elytras-plus"

IGNORES = {
    ".DS_Store",
    "Thumbs.db",
}


def build_zip(src: Path, dist_dir: Path, name: str) -> Path:
    if not src.exists() or not src.is_dir():
        raise FileNotFoundError(f"Diretório de resource não encontrado: {src}")

    dist_dir.mkdir(parents=True, exist_ok=True)
    out_zip = dist_dir / f"{name}.zip"

    with zipfile.ZipFile(out_zip, mode="w", compression=zipfile.ZIP_DEFLATED) as zf:
        for root, _, files in os.walk(src):
            root_p = Path(root)
            for fn in files:
                if fn in IGNORES:
                    continue
                fp = root_p / fn
                # arcname relativo ao diretório 'src' para NÃO incluir a pasta 'resource' no zip
                arcname = fp.relative_to(src)
                zf.write(fp, arcname)

    return out_zip


def main():
    ap = argparse.ArgumentParser(description="Compacta o conteúdo de ./resource em ./dist/<nome>.zip (sem a pasta 'resource' na raiz)")
    ap.add_argument("--src", default=str(DEFAULT_SRC), help="Diretório de origem do resource pack (padrão: ./resource)")
    ap.add_argument("--dist", default=str(DEFAULT_DIST), help="Diretório de saída para o zip (padrão: ./dist)")
    ap.add_argument("--name", default=DEFAULT_NAME, help="Nome do arquivo zip gerado (sem .zip)")
    args = ap.parse_args()

    src = Path(args.src).expanduser().resolve()
    dist_dir = Path(args.dist).expanduser().resolve()
    name = args.name.strip() or DEFAULT_NAME

    try:
        out = build_zip(src, dist_dir, name)
    except Exception as e:
        print(f"Erro ao gerar zip: {e}", file=sys.stderr)
        sys.exit(1)

    print(f"Zip gerado: {out}")
    # Dica: verificar se a raiz está correta
    if not (src / "pack.mcmeta").exists():
        print("Aviso: pack.mcmeta não encontrado em resource/. O pack pode não ser reconhecido pelo jogo.")


if __name__ == "__main__":
    main()
