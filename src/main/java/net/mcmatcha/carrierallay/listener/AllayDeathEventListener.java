package net.mcmatcha.carrierallay.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import net.mcmatcha.carrierallay.CarrierAllaysPlugin;

public class AllayDeathEventListener implements Listener {
    
    @EventHandler 
    public void onEntityDeath(EntityDeathEvent event) {
        // Get the entoity 
        LivingEntity entity = event.getEntity();
        EntityType entityType = event.getEntityType();

        // If its an allay
        if (entityType == EntityType.ALLAY)
            // remove from carriers list
            CarrierAllaysPlugin.ACTIVE_CARRIERS_STORE.RemoveCarrierAllay(entity.getUniqueId());
    }
    
}
