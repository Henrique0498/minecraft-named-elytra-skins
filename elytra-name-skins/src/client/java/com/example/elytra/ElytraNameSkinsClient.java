package com.example.elytra;

import com.example.elytra.support.NameSkinResolver;
import com.example.elytra.support.NameBasedElytraArmorRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.item.Items;

public class ElytraNameSkinsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Inicializa config default (gera arquivo se não existir)
        NameSkinResolver.resolve("__init__");
        // Registra o renderer de Elytra que escolhe textura por nome
        ArmorRenderer.register(new NameBasedElytraArmorRenderer(), Items.ELYTRA);
    }
}
