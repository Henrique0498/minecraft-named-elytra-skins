# Elytras+ (OptiFine + Fabric/CIT Resewn)

Este repositório contém um script para reorganizar e/ou construir um resource pack de Elytras com CIT compatível com OptiFine e Fabric (via CIT Resewn).

## Requisitos

- Python 3.9+
- Para usar no Fabric: instale o mod [CIT Resewn].

## Estrutura gerada

O script garante o layout padrão para CIT:

```
assets/minecraft/optifine/cit/elytra/
  icon/        # ícones do item no inventário
  model/       # textura aplicada à Elytra equipada
  properties/  # regras .properties (item e elytra)
```

Opcionalmente, a mesma árvore é espelhada para:

```
assets/citresewn/cit/elytra/ ...
```

## Uso

### 1) Reorganizar um pack já existente (recomendado para packs prontos)

Exemplo usando o próprio Elytras+ como origem e gerando `Elytras+_fixed`:

```powershell
python ".\rebuild_pack.py" --src "." --out "..\Elytras+_fixed" --mode reorganize-pack --citresewn --pack-format 18
```

Notas:

- `--citresewn` cria também `assets/citresewn/cit`, útil no Fabric.
- `--pack-format` controla o valor do pack_format no pack.mcmeta gerado quando ele não existe. Se o arquivo já existir, o script não sobrescreve.

### 2) Criar um pack a partir de imagens soltas

Se você só tem PNGs com as texturas, use:

```powershell
python ".\rebuild_pack.py" --src "C:\MinhasSkins" --out "C:\MeuPack" --mode from-images --citresewn --pack-format 18
```

Heurística de nomes:

- se o arquivo termina em `_icon.png`, ele vai para `icon/<nome>.png`;
- se termina em `_model.png`, vai para `model/<nome>.png`;
- caso contrário, a mesma imagem é copiada para icon e model.

## Como trocar a skin no jogo

- Ative o resource pack nas opções do Minecraft.
- Renomeie sua Elytra no bigorna para conter o nome/parte do nome definido na regra (ex.: "Citrus", "Rainbow", etc.).
- As regras usam `nbt.display.Name=ipattern:*Nome*`.

## Dicas de compatibilidade

- As regras incluem `items=elytra` e `matchItems=elytra` para melhor compatibilidade.
- CIT Resewn normalmente aceita `assets/minecraft/optifine/cit`. O espelho em `assets/citresewn/cit` é um plus.
- Ajuste o `pack_format` conforme sua versão do jogo (ex.: 18 para 1.20.5+). Se seu `pack.mcmeta` já existir, o script não altera.

## Problemas comuns

- A skin não troca: verifique o nome exato usado no bigorna e a regra `.properties` correspondente em `properties/`.
- No Fabric, certifique-se de que o mod CIT Resewn está instalado e carregado.

[CIT Resewn]: https://modrinth.com/mod/cit-resewn
