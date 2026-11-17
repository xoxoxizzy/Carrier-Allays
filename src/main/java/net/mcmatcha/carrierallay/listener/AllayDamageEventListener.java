package net.mcmatcha.carrierallay.listener;

import org.bukkit.entity.Allay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import net.mcmatcha.carrierallay.models.CarrierAllayData;

/**
 * Makes carriers IMMORTAL
 *
 * @author Copyright (c) xoxoxizzy. All Rights Reserved.
 */

public class AllayDamageEventListener implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Allay allay))
            return; // do nothing

        if (CarrierAllayData.loadFromAllayPDC(allay) == null)
            return; // do nothing: not a carrier

        event.setCancelled(true);
    }

}
