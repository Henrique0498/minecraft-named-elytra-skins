package client.java.com.example.elytra.mixin;

import com.example.elytra.support.NameSkinResolver;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ElytraFeatureRenderer.class)
public abstract class ElytraFeatureRendererMixin<T extends LivingEntity> {

    @Inject(method = "getElytraTexture", at = @At("HEAD"), cancellable = true)
    private void elytraNameSkins$overrideTexture(ItemStack stack, T entity, CallbackInfoReturnable<Identifier> cir) {
        if (stack != null && stack.isOf(Items.ELYTRA)) {
            String name = stack.getName().getString();
            String skin = NameSkinResolver.resolve(name);
            if (skin != null && !skin.isEmpty()) {
                cir.setReturnValue(Identifier.of("minecraft", "textures/optifine/cit/elytra/model/" + skin + ".png"));
            }
        }
    }
}
