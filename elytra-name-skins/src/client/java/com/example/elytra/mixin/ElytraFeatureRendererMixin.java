package com.example.elytra.mixin;

import com.example.elytra.support.NameSkinResolver;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ElytraFeatureRenderer.class)
public abstract class ElytraFeatureRendererMixin {

    // 1.21.8: ElytraFeatureRenderer has private static Identifier getTexture(BipedEntityRenderState state)
    @Inject(
        method = "getTexture(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)Lnet/minecraft/util/Identifier;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void elytraNameSkins$overrideTexture(BipedEntityRenderState state, CallbackInfoReturnable<Identifier> cir) {
        if (state == null) return;
        ItemStack stack = state.equippedChestStack;
        if (stack != null && stack.isOf(Items.ELYTRA)) {
            String name = stack.getName().getString();
            String skin = NameSkinResolver.resolve(name);
            if (skin != null && !skin.isEmpty()) {
                Identifier id = Identifier.of("minecraft", "textures/optifine/cit/elytra/model/" + skin + ".png");
                System.out.println("[ElytraNameSkins] Elytra detected; name='" + name + "' -> skin='" + skin + "' path=" + id);
                cir.setReturnValue(id);
            } else {
                System.out.println("[ElytraNameSkins] Elytra detected; name='" + name + "' -> no matching skin, using vanilla");
            }
        }
    }
}
