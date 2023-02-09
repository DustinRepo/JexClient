package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventExplosionVelocity;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.player.EventPlayerVelocity;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import java.lang.Math;

public class AntiKnockback extends Feature {

    public Property<Mode> modeProperty = new Property.PropertyBuilder<Mode>(this.getClass())
            .name("Mode")
            .value(Mode.NORMAL)
            .build();
    public final Property<Boolean> velocity0Property = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("PlayerVelocity")
            .value(true)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.NORMAL)
            .build();
    public final Property<Integer> npXProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("PercentX")
            .value(0)
            .parent(velocity0Property)
            .depends(parent -> (boolean) parent.value())
            .min(0)
            .max(100)
            .inc(1)
            .build();
    public final Property<Integer> npYProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("PercentY")
            .value(0)
            .parent(velocity0Property)
            .depends(parent -> (boolean) parent.value())
            .min(0)
            .max(100)
            .inc(1)
            .build();
    public final Property<Integer> npZProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("PercentZ")
            .value(0)
            .parent(velocity0Property)
            .depends(parent -> (boolean) parent.value())
            .min(0)
            .max(100)
            .inc(1)
            .build();
    public final Property<Boolean> velocity1Property = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("ExplosionVelocity")
            .value(true)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.NORMAL)
            .build();
    public final Property<Integer> enpXProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("ExplosionX")
            .value(0)
            .parent(velocity1Property)
            .depends(parent -> (boolean) parent.value())
            .min(0)
            .max(100)
            .inc(1)
            .build();
    public final Property<Integer> enpYProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("ExplosionY")
            .value(0)
            .parent(velocity1Property)
            .depends(parent -> (boolean) parent.value())
            .min(0)
            .max(100)
            .inc(1)
            .build();
    public final Property<Integer> enpZProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("ExplosionZ")
            .value(0)
            .parent(velocity1Property)
            .depends(parent -> (boolean) parent.value())
            .min(0)
            .max(100)
            .inc(1)
            .build();
    //-------------------------------------------------------------------------------------------------------
    public final Property<Boolean> velocity2Property = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("ReversePlayerVelocity")
            .value(true)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.REVERSE)
            .build();
    public final Property<Integer> rpXProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("RPercentX")
            .value(-1)
            .parent(velocity2Property)
            .depends(parent -> (boolean) parent.value())
            .min(-1)
            .max(-100)
            .inc(-1)
            .build();
    public final Property<Integer> rpYProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("RPercentY")
            .value(-1)
            .parent(velocity2Property)
            .depends(parent -> (boolean) parent.value())
            .min(-1)
            .max(-100)
            .inc(-1)
            .build();
    public final Property<Integer> rpZProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("RPercentZ")
            .value(-1)
            .parent(velocity2Property)
            .depends(parent -> (boolean) parent.value())
            .min(-1)
            .max(-100)
            .inc(-1)
            .build();
    public final Property<Boolean> velocity3Property = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("ReverseExplosionVelocity")
            .value(true)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.REVERSE)
            .build();
    public final Property<Integer> erpXProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("RExplosionX")
            .value(-1)
            .parent(velocity3Property)
            .depends(parent -> (boolean) parent.value())
            .min(-1)
            .max(-100)
            .inc(-1)
            .build();
    public final Property<Integer> erpYProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("RExplosionY")
            .value(-1)
            .parent(velocity3Property)
            .depends(parent -> (boolean) parent.value())
            .min(-1)
            .max(-100)
            .inc(-1)
            .build();
    public final Property<Integer> erpZProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("RExplosionZ")
            .value(-1)
            .parent(velocity3Property)
            .depends(parent -> (boolean) parent.value())
            .min(-1)
            .max(-100)
            .inc(-1)
            .build();
    

    public AntiKnockback() {
        super(Category.COMBAT, "Changes knockback from the player.");
    }

    @EventPointer
    private final EventListener<EventExplosionVelocity> eventExplosionVelocityEventListener = new EventListener<>(event -> {
        if (modeProperty.value() == Mode.NORMAL) {
        float enpx = enpXProperty.value() / 100.0f;
        float enpy = enpYProperty.value() / 100.0f;
        float enpz = enpZProperty.value() / 100.0f;
        if (enpXProperty.value() == 0){
        if (enpYProperty.value() == 0){
        if (enpZProperty.value() == 0){
            event.cancel();
        }
        }
        }
        else {
            event.setMultX(enpx);
            event.setMultY(enpy);
            event.setMultZ(enpz);
        }
       }
        if (modeProperty.value() == Mode.REVERSE){
         float erpx = erpXProperty.value() / 100.0f;
        float erpy = erpYProperty.value() / 100.0f;
        float erpz = erpZProperty.value() / 100.0f;
        if (erpXProperty.value() == 0) {
        if (erpYProperty.value() == 0) {
        if (erpZProperty.value() == 0) {
            event.cancel();
         }
         }
         }
        else {
            event.setMultX(erpx);
            event.setMultY(erpy);
            event.setMultZ(erpz);
        }   
        }
    });

    @EventPointer
    private final EventListener<EventPlayerVelocity> eventPlayerVelocityEventListener = new EventListener<>(event -> {
        if (modeProperty.value() == Mode.NORMAL) {
        float npx = npXProperty.value() / 100.0f;
        float npy = npYProperty.value() / 100.0f;
        float npz = npZProperty.value() / 100.0f;
        if (npXProperty.value() == 0) {
        if (npYProperty.value() == 0) {
        if (npZProperty.value() == 0) {
            event.cancel();
         }
         }
         }
        else {
            event.setVelocityX((int)(event.getVelocityX() * npx));
            event.setVelocityY((int)(event.getVelocityY() * npy));
            event.setVelocityZ((int)(event.getVelocityZ() * npz));
        }
            
        }
      if (modeProperty.value() == Mode.REVERSE) { 
       float rpx = rpXProperty.value() / 100.0f;
        float rpy = rpYProperty.value() / 100.0f;
        float rpz = rpZProperty.value() / 100.0f;
        if (rpXProperty.value() == 0) {
        if (rpYProperty.value() == 0) {
        if (rpZProperty.value() == 0) {
            event.cancel();
         }
         }
         }
        else {
            event.setVelocityX((int)(event.getVelocityX() * rpx));
            event.setVelocityY((int)(event.getVelocityY() * rpy));
            event.setVelocityZ((int)(event.getVelocityZ() * rpz));
        } 
      }  
    });
        
     public enum Mode {
        NORMAL, REVERSE
    }
}
