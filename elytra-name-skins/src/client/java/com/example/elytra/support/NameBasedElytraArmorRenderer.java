package com.example.elytra.support;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class NameBasedElytraArmorRenderer implements ArmorRenderer {
    // In 1.21.8 ElytraEntityModel is non-generic
    private ElytraEntityModel elytraModel; // lazy init to avoid NPE during early client init

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack, BipedEntityRenderState state, EquipmentSlot slot, int light, BipedEntityModel<BipedEntityRenderState> contextModel) {
        if (!stack.isOf(Items.ELYTRA) || slot != EquipmentSlot.CHEST) return;

        String name = stack.getName().getString();
        String skin = NameSkinResolver.resolve(name);

        Identifier texture = (skin != null && !skin.isEmpty())
            ? Identifier.of("minecraft", "textures/optifine/cit/elytra/model/" + skin + ".png")
            : Identifier.of("minecraft", "textures/entity/elytra.png");

        // Lazy init model using local TexturedModelData to avoid relying on client model loader API differences
        if (this.elytraModel == null) {
            this.elytraModel = new ElytraEntityModel(ElytraEntityModel.getTexturedModelData().createModel());
        }

        matrices.push();
        VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(texture));
        this.elytraModel.render(matrices, vc, light, OverlayTexture.DEFAULT_UV);
        matrices.pop();
    }
}