package net.mcmatcha.carrierallay.managers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Allay;

import net.mcmatcha.carrierallay.models.CarrierAllayData;

/**
 * Previously, the plugin would scan for every entity and find all alays to see which ones are carriers.
 * This is innefficient. This plugin should keep track of which allays are actually carriers, so the loop can only
 * run on them.
 * CarrierAllaysStore
 */
public class CarrierAllaysStore {
    
    private final Set<UUID> activeCarrierAllays;

    public CarrierAllaysStore() {
        activeCarrierAllays = new HashSet<>();
    }

    public void AddCarrierAllay(UUID uuid) {
        activeCarrierAllays.add(uuid);
    }

    public void RemoveCarrierAllay(UUID uuid) {
        activeCarrierAllays.remove(uuid);
    }

    public Set<Allay> getActiveCarrierAllays() {
        Set<Allay> allays = new HashSet<>();

        for (UUID uuid: this.activeCarrierAllays) {
            Allay allay = (Allay)Bukkit.getEntity(uuid);
            if ( allay == null ) continue;
            allays.add(allay);
        }

        return allays;
    }

    public void initActiveCarrierAllays(Server server) {

        for (World world : server.getWorlds()) {
            for (Allay allay : world.getEntitiesByClass(Allay.class)) {
                if (CarrierAllayData.loadFromAllayPDC(allay) != null)
                    AddCarrierAllay(allay.getUniqueId());
            }
        }
    }

}
