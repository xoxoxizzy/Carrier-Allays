package net.mcmatcha.carrierallay.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import net.mcmatcha.carrierallay.CarrierAllaysPlugin;
import net.mcmatcha.carrierallay.config.CarrierAllaysPluginConfig;
import net.mcmatcha.carrierallay.managers.CarrierAllayManager;
import net.mcmatcha.carrierallay.models.CarrierAllayStatus;

public class AllayDeliveryScheduledTask implements Consumer<BukkitTask> {

    // The carrier item comes from the bundled datapack recipe, so returning
    // the item only works when that recipe is actually loaded
    private static final NamespacedKey CARRIER_RECIPE_KEY = new NamespacedKey("carrier_allays", "crafting/carrier_allay");

    private List<CarrierAllayManager> carrierAllays;
    private static final Logger LOGGER = CarrierAllaysPlugin.INSTANCE.getLogger();
    private static final CarrierAllaysPluginConfig CONFIG = CarrierAllaysPlugin.CONFIG_INSTANCE;

    // How long the allay visibly flies away before the delivery teleport
    private static final long FLY_AWAY_MILLIS = CONFIG.getFlyAwayMillis();
    private static final double CARRIER_ALLAY_SPEED = CONFIG.getCarrierAllaySpeed();

    // When each too-far allay should teleport, per allay (weakly held so
    // entries go byebye with the entity)
    private final Map<Allay, Long> flyAwayTeleportAt = new WeakHashMap<>();

    public AllayDeliveryScheduledTask() {

    }

    @Override
    public void accept(BukkitTask t) {

        // GET EVERY CARRIER ALLAY
        Set<Allay> allays = CarrierAllaysPlugin.ACTIVE_CARRIERS_STORE.getActiveCarrierAllays();

        List<CarrierAllayManager> managers = new ArrayList<>();

        for (Allay allay : allays) {
            CarrierAllayManager manager = new CarrierAllayManager(allay);
            if (manager.getData() != null)
                managers.add(manager);
        }

        this.carrierAllays = managers;

        for (CarrierAllayManager carrierAllay : carrierAllays) {

            switch (carrierAllay.getStatus()) {

                case COMPLETE:
                    completeLoop(t, carrierAllay);
                    break;
                case DELIVERY:
                    deliveryLoop(t, carrierAllay);
                    break;
                case RETURNING:
                    returningLoop(t, carrierAllay);
                    break;
                case WAITING:
                    waitingLoop(t, carrierAllay);
                    break;
                default:
                    t.cancel();
                    break;

            }

        }

    }

    private void completeLoop(BukkitTask t, CarrierAllayManager carrierAllay) {
        
        // Drop anything still held
        carrierAllay.DropItem();

  
        CarrierAllaysPlugin.ACTIVE_CARRIERS_STORE.RemoveCarrierAllay(carrierAllay.getData().carrier());

        if (CONFIG.getMatchaFlavoured() && Bukkit.getRecipe(CARRIER_RECIPE_KEY) != null) {
            carrierAllay.returnToPlayer();
            return;
        }

        if (CONFIG.getMatchaFlavoured())
            LOGGER.warning("matcha-flavoured is enabled but the carrier allay recipe is not loaded; respawning a wild allay instead.");

        // Respawn a wild allay: the vanilla "liked player" memory (the sender
        // who handed over the item) has no API to clear and would keep the
        // allay following the sender forever
        carrierAllay.respawnAsWildAllay();
    }

    private void deliveryLoop(BukkitTask t, CarrierAllayManager carrierAllay) {

        Player target = carrierAllay.getTargetPlayer();

        // Wait for the target to come back online instead of crashing
        if (target == null || !target.isOnline() || target.isDead())
            return;

        // When the target is too far away to fly to, let the allay visibly
        // fly away for a moment before teleporting near the target
        if (carrierAllay.isCarrierAllayTargetTooFar()) {
            long now = System.currentTimeMillis();
            Long teleportAt = flyAwayTeleportAt.get(carrierAllay.getAllay());

            if (teleportAt == null) {
                flyAwayTeleportAt.put(carrierAllay.getAllay(), now + FLY_AWAY_MILLIS);
                carrierAllay.flyOffToward(target, CARRIER_ALLAY_SPEED);
                return;
            }

            if (now < teleportAt) {
                carrierAllay.flyOffToward(target, CARRIER_ALLAY_SPEED);
                return;
            }

            flyAwayTeleportAt.remove(carrierAllay.getAllay());

            if (!carrierAllay.teleportNear(target))
                return;

            carrierAllay.notifyPlayer(carrierAllay.getTargetPlayer(), "A carrier allay approaches from a long distance...");
        }

        carrierAllay.pathfindTo(target, CARRIER_ALLAY_SPEED);

        // Once close, drop the item and then switch to returning mode
        if (carrierAllay.isCarrierAllayNearTarget()) {
            carrierAllay.DropItem();
            carrierAllay.notifyPlayer(target, "Your delivery from " + carrierAllay.getSenderName() + " has arrived!");
            carrierAllay.notifyPlayer(carrierAllay.getSenderPlayer(), "Your package was delivered to " + carrierAllay.getTargetName());
            carrierAllay.setStatus(CarrierAllayStatus.RETURNING);
        }
    }

    private void returningLoop(BukkitTask t, CarrierAllayManager carrierAllay) {

        Player sender = carrierAllay.getSenderPlayer();

        // Wait for the sender to come back online instead of crashing
        if (sender == null || !sender.isOnline() || sender.isDead())
            return;

        // When the sender is too far away to fly to, let the allay visibly
        // fly away for a moment before teleporting near the sender
        if (carrierAllay.isCarrierAllaySenderTooFar()) {
            long now = System.currentTimeMillis();
            Long teleportAt = flyAwayTeleportAt.get(carrierAllay.getAllay());

            if (teleportAt == null) {
                flyAwayTeleportAt.put(carrierAllay.getAllay(), now + FLY_AWAY_MILLIS);
                carrierAllay.flyOffToward(sender, CARRIER_ALLAY_SPEED);
                return;
            }

            if (now < teleportAt) {
                carrierAllay.flyOffToward(sender, CARRIER_ALLAY_SPEED);
                return;
            }

            flyAwayTeleportAt.remove(carrierAllay.getAllay());

            if (!carrierAllay.teleportNear(sender))
                return;
        }

        carrierAllay.pathfindTo(sender, CARRIER_ALLAY_SPEED);

        // Once you are close goto the complete loop
        if (carrierAllay.isCarrierAllayNearSender()) {
            carrierAllay.setStatus(CarrierAllayStatus.COMPLETE);
            completeLoop(t, carrierAllay);
        }
    }

    private void waitingLoop(BukkitTask t, CarrierAllayManager carrierAllay) {

        Player sender = carrierAllay.getSenderPlayer();

        // Wait for the sender to come back online instead of crashing
        if (sender == null || !sender.isOnline() || sender.isDead())
            return;

        // Wait for sender to give item
        // Follow the player
        carrierAllay.hoverNear(sender, CARRIER_ALLAY_SPEED);

        // When we are given an item, switch to delivery loop
        if (!carrierAllay.HasItem())
        {
            return; // do nothing
        }

        carrierAllay.setStatus(CarrierAllayStatus.DELIVERY);

    }

}
