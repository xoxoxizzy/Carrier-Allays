package net.mcmatcha.carrierallay.listener;

import java.util.List;

import org.bukkit.Server;
import org.bukkit.entity.Allay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.papermc.paper.event.player.PlayerNameEntityEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.mcmatcha.carrierallay.CarrierAllaysPlugin;
import net.mcmatcha.carrierallay.models.CarrierAllayData;
import net.mcmatcha.carrierallay.models.CarrierAllayStatus;

/**
 * Listens for when a player renames an entity.
 * 
 * @author Copyright (c) xoxoxizzy. All Rights Reserved.
 */

public class AllayEntityRenameListener implements Listener {

    private final Server minecraftServer;

    public AllayEntityRenameListener(CarrierAllaysPlugin minecraftPlugin) {
        this.minecraftServer = minecraftPlugin.getServer();
    }

    @EventHandler
    public void  onEntityEvent(PlayerNameEntityEvent nameEntityEvent) {

        // Get the sneder, entity and the new name tagged
        LivingEntity entity = nameEntityEvent.getEntity();
        TextComponent nameTextComponent = (TextComponent)nameEntityEvent.getName();
        String name = nameTextComponent.content();
        Player senderPlayer = nameEntityEvent.getPlayer();

        // Guard that the entity is an allay
        if (!entity.getType().equals(EntityType.ALLAY))
            return; // do nothing

        // Guard that the name is a player in the server
        List<Player> matchedPlayers = minecraftServer.matchPlayer(name);
        
        if (matchedPlayers.isEmpty())
            return; // do nothing

        Player targetPlayer = matchedPlayers.get(0);

        // Perhaps guard if the player is too far??

        // Cooldown so players cannot spam carriers
        long secondsLeft = CarrierAllaysPlugin.CARRIER_COOLDOWNS_STORE.secondsLeft(senderPlayer.getUniqueId());

        if (secondsLeft > 0) {
            senderPlayer.sendMessage(Component.text("You can send another carrier in " + secondsLeft + "s"));
            nameEntityEvent.setCancelled(true);
            entity.customName(null);
            return;
        }

        CarrierAllaysPlugin.CARRIER_COOLDOWNS_STORE.markNow(senderPlayer.getUniqueId());

        // Create our carrier allay data and store it :3
        CarrierAllayData newData = new CarrierAllayData(
            senderPlayer.getUniqueId(),
            targetPlayer.getUniqueId(),
            entity.getUniqueId(),
            CarrierAllayData.isSpawnedIn((Allay)entity),
            CarrierAllayStatus.WAITING
        );

        newData.saveToAllayPDC((Allay)entity);
        CarrierAllaysPlugin.ACTIVE_CARRIERS_STORE.AddCarrierAllay(newData.carrier());
        
    }


}