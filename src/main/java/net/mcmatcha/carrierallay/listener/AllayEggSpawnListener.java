package net.mcmatcha.carrierallay.listener;

import org.bukkit.entity.Allay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;

import net.mcmatcha.carrierallay.models.CarrierAllayData;

/**
 *
 * @author Copyright (c) xoxoxizzy. All Rights Reserved.
 */

public class AllayEggSpawnListener implements Listener {

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        CreatureSpawnEvent.SpawnReason spawnReason = event.getSpawnReason();
        if (spawnReason != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
            && spawnReason != CreatureSpawnEvent.SpawnReason.DISPENSE_EGG)
            return; // do nothing

        if (!(event.getEntity() instanceof Allay allay))
            return; // do nothing

        allay.getPersistentDataContainer().set(
            CarrierAllayData.SHOULD_RETURN_KEY,
            PersistentDataType.BYTE,
            (byte) 1
        );
    }

}
