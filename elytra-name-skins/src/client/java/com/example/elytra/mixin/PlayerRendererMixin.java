package com.example.elytra.mixin;

import com.example.elytra.support.NameBasedElytraFeature;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerRendererMixin {
    @Shadow @Final private List<FeatureRenderer<?, ?>> features;

    @Inject(method = "<init>(Lnet/minecraft/client/render/entity/EntityRendererFactory$Context;Z)V", at = @At("TAIL"))
    private void elytraNameSkins$replaceElytra(EntityRendererFactory.Context context, boolean slim, CallbackInfo ci) {
        // Remover a Elytra vanilla
        Iterator<FeatureRenderer<?, ?>> it = features.iterator();
        while (it.hasNext()) {
            FeatureRenderer<?, ?> f = it.next();
            if (f instanceof ElytraFeatureRenderer<?, ?>) {
                it.remove();
                break;
            }
        }
        // Adicionar a nossa, com seleção por nome
        features.add(new NameBasedElytraFeature<>((PlayerEntityRenderer)(Object)this, context.getModelLoader()));
    }
}
