package net.mcmatcha.carrierallay.managers;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.util.Vector;

import com.destroystokyo.paper.entity.Pathfinder;

import net.kyori.adventure.text.Component;
import net.mcmatcha.carrierallay.CarrierAllaysPlugin;
import net.mcmatcha.carrierallay.models.CarrierAllayData;
import net.mcmatcha.carrierallay.models.CarrierAllayStatus;

public class CarrierAllayManager {

    // Delivery counts as arrived within 4 blocks
    private static final double NEAR_DISTANCE_SQUARED = CarrierAllaysPlugin.CONFIG_INSTANCE.getNearDistance();

    // Beyond 150 blocks we teleport instead of flying
    private static final double TELEPORT_DISTANCE_SQUARED =  CarrierAllaysPlugin.CONFIG_INSTANCE.getTeleportDistance();

    // Teleports land this many blocks from the player (squared: 2025-3025), at
    // a random angle, far enough out that you don't see the allay pop in
    private static final double TELEPORT_MIN_OFFSET =  CarrierAllaysPlugin.CONFIG_INSTANCE.getTeleportMinOffset();
    private static final double TELEPORT_MAX_OFFSET =  CarrierAllaysPlugin.CONFIG_INSTANCE.getTeleportMaxOffset();

    private static final double REPATH_DISTANCE_SQUARED = CarrierAllaysPlugin.CONFIG_INSTANCE.getRepathDistance();
    private static final double FLY_OFF_WAYPOINT_DISTANCE = CarrierAllaysPlugin.CONFIG_INSTANCE.getFlyOffWaypointDistance();

    private static final double HOVER_DISTANCE = CarrierAllaysPlugin.CONFIG_INSTANCE.getHoverDistance();
    private static final double HOVER_TOLERANCE_SQUARED = CarrierAllaysPlugin.CONFIG_INSTANCE.getHoverTolerance();

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

    public void pathfindTo(Player player, double speed) {
        Location destination = player.getLocation();
        Pathfinder.PathResult path = getPathfinder().getCurrentPath();

        if (path != null
                && path.getNextPoint() != null
                && path.getFinalPoint() != null
                && path.getFinalPoint().distanceSquared(destination) < REPATH_DISTANCE_SQUARED)
            return;

        getPathfinder().moveTo(destination, speed);
    }

    public void returnToPlayer() {
        World world = allay.getWorld();
        Player sender = getSenderPlayer();

        world.spawnParticle(Particle.ENCHANT, allay.getLocation(), 30);
        world.playSound(allay.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, 1.0f, 1.0f);

        // Give the player back the carrier item.
        ItemStack carrierItem = getCarrierItem();

        // Sender went offline: leave the item where the allay stood
        if (sender == null || !sender.isOnline()) {
            world.dropItemNaturally(allay.getLocation(), carrierItem);
            allay.remove();
            return;
        }

        sender.give(carrierItem);
        allay.remove();
    }

    private ItemStack getCarrierItem() {
        Recipe recipe = Bukkit.getRecipe(new NamespacedKey("carrier_allays", "crafting/carrier_allay"));
        return ((CraftingRecipe) recipe).getResult();
    }

    public void flyOffToward(Player player, double speed) {
        Location from = getAllay().getLocation();
        Vector direction = player.getLocation().toVector().subtract(from.toVector());

        if (direction.lengthSquared() < 0.01d)
            direction = new Vector(0, 1, 0);

        double distance = Math.min(direction.length(), FLY_OFF_WAYPOINT_DISTANCE);
        getPathfinder().moveTo(from.add(direction.normalize().multiply(distance)), speed);
    }

    public void hoverNear(Player player, double speed) {
        if (getAllay().getWorld() != player.getWorld())
            return;

        Location hoverLocation = getHoverSpot(player);

        if (getAllay().getLocation().distanceSquared(hoverLocation) <= HOVER_TOLERANCE_SQUARED) {
            getPathfinder().stopPathfinding();
            return;
        }

        getPathfinder().moveTo(hoverLocation, speed);
    }

    private Location getHoverSpot(Player player) {
        Location playerLocation = player.getLocation();
        Vector facing = playerLocation.getDirection().setY(0d);

        if (facing.lengthSquared() < 0.01d)
            facing = new Vector(0d, 0d, 1d);

        Location hoverLocation = playerLocation.clone().add(facing.normalize().multiply(HOVER_DISTANCE));
        hoverLocation.setY(playerLocation.getY() + 1.25d);
        return hoverLocation;
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

    public boolean teleportNear(Player player) {
        World world = player.getWorld();

        for (int i = 0; i < 4; i++) {
            double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2d);
            double offset = TELEPORT_MIN_OFFSET
                + ThreadLocalRandom.current().nextDouble(TELEPORT_MAX_OFFSET - TELEPORT_MIN_OFFSET);

            Location candidate = player.getLocation().clone().add(Math.cos(angle) * offset, 0d, Math.sin(angle) * offset);

            if (!world.isChunkLoaded(candidate.getBlockX() >> 4, candidate.getBlockZ() >> 4))
                continue;

            getAllay().teleport(findSafeSpot(candidate));
            return true;
        }

        return false;
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
