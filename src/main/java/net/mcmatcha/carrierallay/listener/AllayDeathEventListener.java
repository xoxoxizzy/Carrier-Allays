package net.mcmatcha.carrierallay.listener;

import java.util.logging.Level;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import net.mcmatcha.carrierallay.CarrierAllaysPlugin;

/**
 * Listens for when an allay dies
 * 
 * @author Copyright (c) xoxoxizzy. All Rights Reserved.
 */
public class AllayDeathEventListener implements Listener {
    
    @EventHandler 
    public void onEntityDeath(EntityDeathEvent event) {

        CarrierAllaysPlugin.INSTANCE.getLogger().log(Level.FINEST, "==Entity death event==");

        // Get the entoity 
        LivingEntity entity = event.getEntity();
        EntityType entityType = event.getEntityType();

        // If its an allay
        if (entityType == EntityType.ALLAY)
            // remove from carriers list
            CarrierAllaysPlugin.ACTIVE_CARRIERS_STORE.RemoveCarrierAllay(entity.getUniqueId());
    }
    
}
