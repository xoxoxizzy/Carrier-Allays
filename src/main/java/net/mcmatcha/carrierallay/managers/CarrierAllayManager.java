package net.mcmatcha.carrierallay.managers;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.destroystokyo.paper.entity.Pathfinder;

import net.kyori.adventure.text.Component;
import net.mcmatcha.carrierallay.models.CarrierAllayData;
import net.mcmatcha.carrierallay.models.CarrierAllayStatus;

public class CarrierAllayManager {

    // Delivery counts as arrived within 4 blocks
    private static final double NEAR_DISTANCE_SQUARED = 16d;

    // Beyond 150 blocks we teleport instead of flying
    private static final double TELEPORT_DISTANCE_SQUARED = 22500d;

    // Teleports land this many blocks from the player (squared: 2025-3025), at
    // a random angle, far enough out that you don't see the allay pop in
    private static final double TELEPORT_MIN_OFFSET = 45d;
    private static final double TELEPORT_MAX_OFFSET = 55d;

    private CarrierAllayData carrierAllayData;
    private final Allay allay;

    public CarrierAllayManager(Allay allay) {
        this.carrierAllayData = CarrierAllayData.loadFromAllayPDC(allay);
        this.allay = allay;
    }

    public void setStatus(CarrierAllayStatus newStatus) {
        this.carrierAllayData = this.carrierAllayData.withStatus(newStatus);
        this.carrierAllayData.saveToAllayPDC(allay);
    }

    public CarrierAllayStatus getStatus() {
        return this.carrierAllayData.currentStatus();
    }

    public Allay getAllay() {
        return this.allay;
    }

    public void clearPDC() {
        this.allay.getPersistentDataContainer().remove(CarrierAllayData.SENDER_KEY);
        this.allay.getPersistentDataContainer().remove(CarrierAllayData.STATUS_KEY);
        this.allay.getPersistentDataContainer().remove(CarrierAllayData.TARGET_KEY);
    }

    public Player getTargetPlayer() {
        return Bukkit.getPlayer(this.carrierAllayData.target());
    }

    public Player getSenderPlayer() {
        return Bukkit.getPlayer(this.carrierAllayData.sender());
    }

    public CarrierAllayData getData() {
        return this.carrierAllayData;
    }

    public boolean HasItem() {
        Allay allay = this.getAllay();
        EntityEquipment equipment = allay.getEquipment();
        if (equipment != null) {
            ItemStack handItem = equipment.getItemInMainHand();
            return (!handItem.getType().isAir());
        }
        return false;
    }

    public void DropItem() {

        Allay allay = this.getAllay();
        World world = allay.getWorld();
        Location loc = allay.getLocation();

        EntityEquipment equipment = allay.getEquipment();

        if (equipment != null) {
            ItemStack handItem = equipment.getItemInMainHand();
            if (!handItem.getType().isAir()) {
                Item droppedItem = world.dropItemNaturally(loc, handItem);
                // Give the delivery a head start so the allay does not
                // instantly pick it back up
                droppedItem.setPickupDelay(60);
                equipment.setItemInMainHand(null);
            }
        }

    }

    public Pathfinder getPathfinder() {
        return this.allay.getPathfinder();
    }

    public boolean isCarrierAllayTooFarFromPlayer(Player player) {
        Location carrierLocation = getAllay().getLocation();
        Location targetLocation = player.getLocation();

        return carrierLocation.distanceSquared(targetLocation) > TELEPORT_DISTANCE_SQUARED;
        
    }

    public boolean isCarrierAllayTargetTooFar() {
        return isCarrierAllayTooFarFromPlayer(getTargetPlayer());
    }

    public boolean isCarrierAllaySenderTooFar() {
        return isCarrierAllayTooFarFromPlayer(getSenderPlayer());
    }

    public String getTargetName() {
        return getPlayerName(getTargetPlayer());
    }

    public String getSenderName() {
        return getPlayerName(getSenderPlayer());
    }

    private String getPlayerName(Player player) {
        return player.getName();
    }

    public void teleportNear(Player player) {
        Location destination = player.getLocation().clone();
        double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2d);
        double offset = TELEPORT_MIN_OFFSET
            + ThreadLocalRandom.current().nextDouble(TELEPORT_MAX_OFFSET - TELEPORT_MIN_OFFSET);

        destination.add(Math.cos(angle) * offset, 0d, Math.sin(angle) * offset);
        destination = findSafeSpot(destination);

        getAllay().teleport(destination);
    }

    private Location findSafeSpot(Location center) {
        World world = center.getWorld();
        int x = center.getBlockX();
        int z = center.getBlockZ();

        for (int dy = 2; dy >= -4; dy--) {
            Block spot = world.getBlockAt(x, center.getBlockY() + dy, z);
            Block above = spot.getRelative(BlockFace.UP);
            Block below = spot.getRelative(BlockFace.DOWN);

            if (!spot.isPassable() || !above.isPassable())
                continue;
            if (spot.isLiquid() || above.isLiquid() || below.isLiquid())
                continue;

            return spot.getLocation().add(0.5d, 0d, 0.5d);
        }

        // No air pocket nearby: the surface of this column is always safe
        return world.getHighestBlockAt(x, z).getLocation().add(0.5d, 1.1d, 0.5d);
    }

    public boolean isCarrierAllayNearPlayer(Player player) {
        Location carrierLocation = getAllay().getLocation();
        Location playerLocation = player.getLocation();

        return carrierLocation.distanceSquared(playerLocation) <= NEAR_DISTANCE_SQUARED;
    }

    public boolean isCarrierAllayNearTarget() {
        return isCarrierAllayNearPlayer(getTargetPlayer());
    }

    public boolean isCarrierAllayNearSender() {
        return isCarrierAllayNearPlayer(getSenderPlayer());
    }

    public void respawnAsWildAllay() {
        Location location = findSafeSpot(this.allay.getLocation().clone());
        World world = this.allay.getWorld();

        this.allay.remove();
        world.spawn(location, Allay.class);
    }

    public void notifyPlayer(Player player, String textContent) {

        player.playSound(
            player.getLocation(), 
            Sound.ENTITY_PLAYER_LEVELUP, 
            SoundCategory.MASTER, 
            1.0f, 0.5f
        );

        if (textContent != null)
            player.sendMessage(Component.text(textContent));

    }





}
