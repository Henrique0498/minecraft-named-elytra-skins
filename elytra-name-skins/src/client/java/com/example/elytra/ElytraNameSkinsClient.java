package com.example.elytra;

import com.example.elytra.support.NameSkinResolver;
import net.fabricmc.api.ClientModInitializer;

public class ElytraNameSkinsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Inicializa config default (gera arquivo se não existir)
        NameSkinResolver.resolve("__init__");
        // A aplicação da textura é feita pelo ElytraFeatureRenderer (via mixin/subclasse).
        // Não registramos ArmorRenderer para Elytra para evitar renderização duplicada e sem animação.
    }
}
