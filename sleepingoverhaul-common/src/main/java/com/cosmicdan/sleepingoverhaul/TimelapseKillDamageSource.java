package com.cosmicdan.sleepingoverhaul;

import net.minecraft.world.damagesource.DamageSource;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class TimelapseKillDamageSource extends DamageSource {
    public static final String MSG_ID = "sleepingoverhaul2.timelapseKill";

    public TimelapseKillDamageSource() {
        super(MSG_ID);
        bypassArmor();
    }
}
