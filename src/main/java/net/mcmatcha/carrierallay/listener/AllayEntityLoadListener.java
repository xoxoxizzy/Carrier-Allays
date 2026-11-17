package net.mcmatcha.carrierallay.listener;

import org.bukkit.entity.Allay;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;

import net.mcmatcha.carrierallay.CarrierAllaysPlugin;
import net.mcmatcha.carrierallay.models.CarrierAllayData;

public class AllayEntityLoadListener implements Listener {

    @EventHandler 
    public void onEntityLoad(EntitiesLoadEvent event) {
        for (Entity entity: event.getEntities()) {
            if (entity instanceof Allay allay && CarrierAllayData.loadFromAllayPDC(allay) != null) {
                CarrierAllaysPlugin.ACTIVE_CARRIERS_STORE.AddCarrierAllay(allay.getUniqueId());
            }
        }
    }
    
}
