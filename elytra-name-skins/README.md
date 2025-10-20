# Elytra Name Skins (Fabric 1.21.8)

Pequeno mod Fabric que aplica texturas de Elytra com base no nome do item (display name). Se a Elytra for renomeada contendo um termo configurado, o mod escolhe a textura correspondente no resource pack.

- Versão alvo: 1.21.8 (Fabric)
- Requer: Fabric Loader + Fabric API
- Não precisa de CIT / OptiFine

## Como funciona

- O mod intercepta a renderização da Elytra (no jogador) e, se a Elytra renomeada corresponder a um padrão, procura uma textura em:
  - `minecraft:textures/optifine/cit/elytra/model/<skin>.png`
- O mapeamento de padrões é lido de `config/elytra_name_skins.json` (gerado automaticamente na primeira execução). Você pode editar para adicionar mais nomes/skins.

## Instalação

1. Copie o `.jar` para a pasta `mods` do seu perfil Fabric.
2. Mantenha seu resource pack com as texturas em `assets/minecraft/textures/optifine/cit/elytra/model/`.
3. Inicie o jogo.

## Config padrão (exemplo)

```json
{
  "mappings": [
    { "match": "*Veteran*", "skin": "veteran_cape" },
    { "match": "*Rainbow*", "skin": "rainbow" }
  ],
  "fallback": ""
}
```

- `match`: ipattern simples (asteriscos como coringas, case-insensitive).
- `skin`: nome do arquivo de textura (sem .png) em `minecraft/textures/optifine/cit/elytra/model/`.
- `fallback`: vazio = deixa a textura vanilla caso não haja match; ou defina um skin padrão.

## Observações

- Este mod afeta apenas a textura renderizada no corpo do jogador (elytra equipada). Ícone no inventário não é alterado.
- Se quiser que o ícone também mude por nome, existe a possibilidade de extensão futura.
