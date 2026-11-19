package net.mcmatcha.carrierallay.listener;

import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.mcmatcha.carrierallay.CarrierAllaysPlugin;

/**
 * Listens for when a player uses egg
 *
 * @author Copyright (c) xoxoxizzy. All Rights Reserved.
 */

public class AllayEggUseListener implements Listener {

    private static final NamespacedKey CARRIER_ITEM_MODEL = new NamespacedKey("carrier_allays", "carrier_allay_egg");

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        CarrierAllaysPlugin.INSTANCE.getLogger().log(Level.FINEST, "==Player Interact event==");

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return; // do nothing

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.ALLAY_SPAWN_EGG)
            return; // do nothing

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasItemModel() || !CARRIER_ITEM_MODEL.equals(meta.getItemModel()))
            return; // do nothing: not the carrier egg

        Player player = event.getPlayer();
        long secondsLeft = CarrierAllaysPlugin.CARRIER_COOLDOWNS_STORE.secondsLeft(player.getUniqueId());

        if (secondsLeft <= 0)
            return; // do nothing

        event.setCancelled(true);
        player.sendMessage(Component.text("You can spawn another carrier in " + secondsLeft + "s"));
    }

}
