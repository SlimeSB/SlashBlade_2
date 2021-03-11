package mods.flammpfeil.slashblade.util;

import com.google.common.collect.Lists;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.ability.ArrowReflector;
import mods.flammpfeil.slashblade.entity.EntitySlashEffect;
import mods.flammpfeil.slashblade.entity.IShootable;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;
import java.util.function.Consumer;

public class AttackManager {
    static public void areaAttack(LivingEntity playerIn, Consumer<LivingEntity> beforeHit){
        areaAttack(playerIn, beforeHit, 1.0f, true, true, false);
    }

    static public void doSlash(LivingEntity playerIn, float roll) {
        doSlash(playerIn,roll, false);
    }
    static public void doSlash(LivingEntity playerIn, float roll, boolean mute) {
        doSlash(playerIn,roll, mute, false);
    }
    static public void doSlash(LivingEntity playerIn, float roll, boolean mute, boolean critical) {
        doSlash(playerIn,roll,  mute, critical, 1.0);
    }
    static public void doSlash(LivingEntity playerIn, float roll, boolean mute, boolean critical, double damage) {
        doSlash(playerIn,roll, Vector3d.ZERO, mute, critical, damage);
    }
    static public void doSlash(LivingEntity playerIn, float roll, Vector3d centerOffset, boolean mute, boolean critical, double damage) {
        doSlash(playerIn,roll, centerOffset, mute, critical, damage, KnockBacks.cancel);
    }
    static public void doSlash(LivingEntity playerIn, float roll, Vector3d centerOffset, boolean mute, boolean critical, double damage, KnockBacks knockback) {

        int colorCode = playerIn.getHeldItemMainhand().getCapability(ItemSlashBlade.BLADESTATE)
                .map(state->state.getColorCode())
                .orElseGet(()->0xFFFFFF);

        doSlash(playerIn,roll,colorCode, centerOffset, mute, critical, damage, knockback);
    }
    static public void doSlash(LivingEntity playerIn, float roll, int colorCode, Vector3d centerOffset, boolean mute, boolean critical, double damage, KnockBacks knockback) {

        Vector3d pos = playerIn.getPositionVec()
                .add(0.0D, (double)playerIn.getEyeHeight() * 0.75D, 0.0D)
                .add(playerIn.getLookVec().scale(0.3f));

        pos = pos.add(getVectorForRotation( -90.0F, playerIn.getYaw(0)).scale(centerOffset.y))
                .add(getVectorForRotation( 0, playerIn.getYaw(0) + 90).scale(centerOffset.z))
                .add(playerIn.getLookVec().scale(centerOffset.z));

        EntitySlashEffect jc = new EntitySlashEffect(SlashBlade.RegistryEvents.SlashEffect, playerIn.world);
        jc.setPosition(pos.x ,pos.y, pos.z);
        jc.setShooter(playerIn);

        jc.setRotationRoll(roll);
        jc.rotationYaw = playerIn.rotationYaw;
        jc.rotationPitch = 0;

        jc.setColor(colorCode);

        jc.setMute(mute);
        jc.setIsCritical(critical);

        jc.setDamage(damage);

        jc.setKnockBack(knockback);

        playerIn.world.addEntity(jc);

    }

    static public Vector3d getVectorForRotation(float pitch, float yaw) {
        float f = pitch * ((float)Math.PI / 180F);
        float f1 = -yaw * ((float)Math.PI / 180F);
        float f2 = MathHelper.cos(f1);
        float f3 = MathHelper.sin(f1);
        float f4 = MathHelper.cos(f);
        float f5 = MathHelper.sin(f);
        return new Vector3d((double)(f3 * f4), (double)(-f5), (double)(f2 * f4));
    }

    static public List<Entity> areaAttack(LivingEntity playerIn, Consumer<LivingEntity> beforeHit, float ratio, boolean forceHit, boolean resetHit , boolean mute) {
        return areaAttack(playerIn, beforeHit, ratio, forceHit, resetHit, mute,null);
    }
    static public List<Entity> areaAttack(LivingEntity playerIn, Consumer<LivingEntity> beforeHit, float ratio, boolean forceHit, boolean resetHit , boolean mute, List<Entity> exclude) {
        List<Entity> founds = Lists.newArrayList();
        float modifiedRatio = (1.0F + EnchantmentHelper.getSweepingDamageRatio(playerIn) * 0.5f) * ratio;
        AttributeModifier am = new AttributeModifier("SweepingDamageRatio", modifiedRatio, AttributeModifier.Operation.MULTIPLY_BASE);

        if (!playerIn.world.isRemote()) {
            try {
                playerIn.getAttribute(Attributes.ATTACK_DAMAGE).applyNonPersistentModifier(am);

                founds = TargetSelector.getTargettableEntitiesWithinAABB(playerIn.world,
                        TargetSelector.getResolvedReach(playerIn),
                        playerIn);

                if(exclude != null)
                    founds.removeAll(exclude);

                for (Entity entity : founds) {
                    if(entity instanceof LivingEntity)
                        beforeHit.accept((LivingEntity)entity);

                    doMeleeAttack(playerIn, entity, forceHit, resetHit);
                }

            } finally {
                playerIn.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(am);
            }
        }

        if(!mute)
            playerIn.world.playSound((PlayerEntity)null, playerIn.getPosX(), playerIn.getPosY(), playerIn.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.5F, 0.4F / (playerIn.getRNG().nextFloat() * 0.4F + 0.8F));

        return founds;
    }

    static public <E extends Entity & IShootable> List<Entity> areaAttack(E owner, Consumer<LivingEntity> beforeHit, double reach, boolean forceHit, boolean resetHit) {
        return areaAttack(owner, beforeHit, reach, forceHit, resetHit, null);
    }
    static public <E extends Entity & IShootable> List<Entity> areaAttack(E owner, Consumer<LivingEntity> beforeHit, double reach, boolean forceHit, boolean resetHit, List<Entity> exclude) {
        List<Entity> founds = Lists.newArrayList();

        AxisAlignedBB bb = owner.getBoundingBox();
        //bb = bb.grow(3.0D, 3D, 3.0D);

        if (!owner.world.isRemote()) {

            founds = TargetSelector.getTargettableEntitiesWithinAABB(owner.world,
                    reach,
                    owner);

            if(exclude != null)
                founds.removeAll(exclude);

            for (Entity entity : founds) {

                if(entity instanceof LivingEntity)
                    beforeHit.accept((LivingEntity)entity);

                float baseAmount = (float) owner.getDamage();
                doAttackWith(DamageSource.causeIndirectMagicDamage(owner, owner.getShooter()), baseAmount,entity, forceHit, resetHit);
            }
        }

        return founds;
    }

    static public void doManagedAttack(Consumer<Entity> attack, Entity target, boolean forceHit, boolean resetHit){
        if(forceHit)
            target.hurtResistantTime = 0;

        attack.accept(target);

        if(resetHit)
            target.hurtResistantTime = 0;
    }

    static public void doAttackWith(DamageSource src, float amount , Entity target, boolean forceHit, boolean resetHit){
        doManagedAttack((t)->{
            t.attackEntityFrom(src, amount);
        },target, forceHit, resetHit);
    }

    static public void doMeleeAttack(LivingEntity attacker, Entity target, boolean forceHit, boolean resetHit){
        if (attacker instanceof PlayerEntity) {
            doManagedAttack((t)->{
                attacker.getHeldItemMainhand().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state) -> {
                    state.setOnClick(true);
                    ((PlayerEntity) attacker).attackTargetEntityWithCurrentItem(t);
                    state.setOnClick(false);
                });
            },target, forceHit, resetHit);
        }else{
            float baseAmount = (float) attacker.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
            doAttackWith(DamageSource.causeMobDamage(attacker), baseAmount, target, forceHit, resetHit);
        }

        ArrowReflector.doReflect(target, attacker);
    }
}
