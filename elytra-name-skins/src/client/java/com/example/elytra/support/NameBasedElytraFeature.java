package client.java.com.example.elytra.support;

import client.java.com.example.elytra.support.NameSkinResolver;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class NameBasedElytraFeature<T extends LivingEntity, M extends EntityModel<T>> extends ElytraFeatureRenderer<T, M> {
    public NameBasedElytraFeature(FeatureRendererContext<T, M> context, net.minecraft.client.render.entity.model.EntityModelLoader modelLoader) {
        super(context, modelLoader);
    }

    @Override
    protected Identifier getElytraTexture(ItemStack stack, T entity) {
        if (stack != null && stack.isOf(Items.ELYTRA) && stack.hasCustomName()) {
            String name = stack.getName().getString();
            String skin = NameSkinResolver.resolve(name);
            if (skin != null && !skin.isEmpty()) {
                return new Identifier("minecraft", "textures/optifine/cit/elytra/model/" + skin + ".png");
            }
        }
        return super.getElytraTexture(stack, entity);
    }
}
