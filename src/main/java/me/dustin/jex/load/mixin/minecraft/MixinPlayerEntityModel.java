package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventInitPlayerModel;
import me.dustin.jex.event.render.EventPlayerEntityGetBodyParts;
import me.dustin.jex.event.render.EventPlayerEntityTexturedModelData;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(PlayerEntityModel.class)
public class MixinPlayerEntityModel<T extends LivingEntity> extends BipedEntityModel<T> {

    public MixinPlayerEntityModel(ModelPart root) {
        super(root);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void initClass(ModelPart root, boolean thinArms, CallbackInfo ci) {
        new EventInitPlayerModel(root).run();
    }

    @Inject(method = "getTexturedModelData", at = @At("RETURN"))
    private static void getTexturedModelData(Dilation dilation, boolean slim, CallbackInfoReturnable<ModelData> cir) {
        new EventPlayerEntityTexturedModelData(cir.getReturnValue(), dilation).run();
    }

    @Inject(method = "getBodyParts", at = @At("RETURN"), cancellable = true)
    public void getBodyParts(CallbackInfoReturnable<Iterable<ModelPart>> cir) {
        Iterable<ModelPart> parts = cir.getReturnValue();
        ArrayList<ModelPart> partArrayList = new ArrayList<>();
        parts.forEach(partArrayList::add);
        EventPlayerEntityGetBodyParts eventPlayerEntityGetBodyParts = new EventPlayerEntityGetBodyParts((PlayerEntityModel<?>)(Object)this, partArrayList).run();
        cir.setReturnValue(eventPlayerEntityGetBodyParts.getBodyParts());
    }
}
