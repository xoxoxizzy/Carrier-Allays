package net.mcmatcha.carrierallay.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Storage for COOLDOWNS
 */
public class CarrierCooldownStore {

    private final Map<UUID, Long> lastCarrierAt;
    private final long cooldownMillis;

    public CarrierCooldownStore(long cooldownMillis) {
        this.lastCarrierAt = new HashMap<>();
        this.cooldownMillis = cooldownMillis;
    }

    public void markNow(UUID playerId) {
        this.lastCarrierAt.put(playerId, System.currentTimeMillis());
    }

    public boolean isOnCooldown(UUID playerId) {
        return secondsLeft(playerId) > 0;
    }

    public long secondsLeft(UUID playerId) {
        Long lastAt = this.lastCarrierAt.get(playerId);

        if (lastAt == null)
            return 0;

        long millisLeft = lastAt + this.cooldownMillis - System.currentTimeMillis();
        if (millisLeft <= 0)
            return 0;

        return (millisLeft + 999) / 1000;
    }

}
