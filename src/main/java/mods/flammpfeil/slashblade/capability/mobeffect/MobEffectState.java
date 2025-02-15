package mods.flammpfeil.slashblade.capability.mobeffect;

import com.google.common.collect.Sets;
import net.minecraft.potion.Effect;

import java.util.Optional;
import java.util.Set;

public class MobEffectState implements IMobEffectState {

    long stunTimeout = -1;
    long freezeTimeout = -1;

    int stunLimit = 200;
    int freezeLimit = 200;

    @Override
    public void setStunTimeOut(long timeout) {
        stunTimeout = timeout;
    }

    @Override
    public long getStunTimeOut() {
        return stunTimeout;
    }

    @Override
    public void setFreezeTimeOut(long timeout) {
        freezeTimeout = timeout;
    }

    @Override
    public long getFreezeTimeOut() {
        return freezeTimeout;
    }

    @Override
    public int getStunLimit() {
        return stunLimit;
    }

    @Override
    public void setStunLimit(int limit) {
        this.stunLimit = limit;
    }

    @Override
    public int getFreezeLimit() {
        return freezeLimit;
    }

    @Override
    public void setFreezeLimit(int limit) {
        this.freezeLimit = limit;
    }



    Optional<Long> UntouchableTimeout = Optional.empty();
    int untouchableLimit = 200;
    Set<Effect> effectSet = Sets.newHashSet();
    float storedHealth;
    boolean hasWorked;


    @Override
    public int getUntouchableLimit() {
        return untouchableLimit;
    }

    @Override
    public void setUntouchableLimit(int limit) {
        this.untouchableLimit = limit;
    }

    @Override
    public void setUntouchableTimeOut(Optional<Long> timeout) {
        this.UntouchableTimeout = timeout;
    }

    @Override
    public Optional<Long> getUntouchableTimeOut() {
        return this.UntouchableTimeout;
    }

    @Override
    public Set<Effect> getEffectSet() {
        return effectSet;
    }

    @Override
    public boolean hasUntouchableWorked() {
        return this.hasWorked;
    }

    @Override
    public void setUntouchableWorked(boolean b) {
        this.hasWorked = b;
    }

    @Override
    public float getStoredHealth() {
        return storedHealth;
    }

    @Override
    public void storeHealth(float health) {
        this.storedHealth = health;
    }

    Optional<Long> avoidCooldown = Optional.empty();
    int avoidCount = 0;

    @Override
    public Optional<Long> getAvoidCooldown() {
        return avoidCooldown;
    }

    @Override
    public int getAvoidCount() {
        return avoidCount;
    }

    @Override
    public void setAvoidCooldown(Optional<Long> time) {
        this.avoidCooldown = time;
    }

    @Override
    public void setAvoidCount(int value) {
        avoidCount = value;
    }
}
