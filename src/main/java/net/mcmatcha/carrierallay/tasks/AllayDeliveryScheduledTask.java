package net.mcmatcha.carrierallay.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import net.mcmatcha.carrierallay.CarrierAllaysPlugin;
import net.mcmatcha.carrierallay.managers.CarrierAllayManager;
import net.mcmatcha.carrierallay.models.CarrierAllayStatus;

public class AllayDeliveryScheduledTask implements Consumer<BukkitTask> {

    private List<CarrierAllayManager> carrierAllays;
    private static Logger LOGGER = CarrierAllaysPlugin.INSTANCE.getLogger();

    // How long the allay visibly flies away before the delivery teleport
    private static final long FLY_AWAY_MILLIS = CarrierAllaysPlugin.CONFIG_INSTANCE.getFlyAwayMillis();
    private static final double CARRIER_ALLAY_SPEED = CarrierAllaysPlugin.CONFIG_INSTANCE.getCarrierAllaySpeed();

    // When each too-far allay should teleport, per allay (weakly held so
    // entries go byebye with the entity)
    private final Map<Allay, Long> flyAwayTeleportAt = new WeakHashMap<>();

    public AllayDeliveryScheduledTask() {

    }

    public List<Allay> getAllLoadedAllays() {
        List<Allay> allays = new ArrayList<>();

        for (World world : Bukkit.getWorlds()) {
            allays.addAll(world.getEntitiesByClass(Allay.class));
        }

        return allays;
    }

    @Override
    public void accept(BukkitTask t) {

        // GET EVERY ALLAY
        List<Allay> allays = getAllLoadedAllays();

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
                carrierAllay.getPathfinder().moveTo(target);
                return;
            }

            if (now < teleportAt) {
                carrierAllay.getPathfinder().moveTo(target);
                return;
            }

            flyAwayTeleportAt.remove(carrierAllay.getAllay());
            carrierAllay.teleportNear(target);
            carrierAllay.notifyPlayer(carrierAllay.getTargetPlayer(), "A carrier allay approaches from a long distance...");
        }

        //continouously pathfind to the target until you are close
        carrierAllay.getPathfinder().moveTo(target, CARRIER_ALLAY_SPEED);

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
                carrierAllay.getPathfinder().moveTo(sender);
                return;
            }

            if (now < teleportAt) {
                carrierAllay.getPathfinder().moveTo(sender);
                return;
            }

            flyAwayTeleportAt.remove(carrierAllay.getAllay());
            carrierAllay.teleportNear(sender);
        }

        // Continouously pathfind to the sender until you are close
        carrierAllay.getPathfinder().moveTo(sender, CARRIER_ALLAY_SPEED);

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
        carrierAllay.getPathfinder().moveTo(sender, CARRIER_ALLAY_SPEED);

        // When we are given an item, switch to delivery loop
        if (!carrierAllay.HasItem())
        {
            return; // do nothing
        }

        carrierAllay.setStatus(CarrierAllayStatus.DELIVERY);

    }

}
