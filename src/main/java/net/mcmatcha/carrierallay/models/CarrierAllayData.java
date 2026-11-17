package net.mcmatcha.carrierallay.models;

import java.util.UUID;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Allay;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.mcmatcha.carrierallay.CarrierAllaysPlugin;

/**
 * Stores the carrier allay's sender, target and related data
 * 
 * @author Copyright (c) xoxoxizzy All Rights Reserved.
 */
public record CarrierAllayData (
    UUID sender,
    UUID target,
    UUID carrier,
    boolean should_return,
    CarrierAllayStatus currentStatus
) {

    public static final NamespacedKey SENDER_KEY = new NamespacedKey(CarrierAllaysPlugin.INSTANCE, "sender");
    public static final NamespacedKey STATUS_KEY = new NamespacedKey(CarrierAllaysPlugin.INSTANCE, "status");
    public static final NamespacedKey TARGET_KEY = new NamespacedKey(CarrierAllaysPlugin.INSTANCE, "target");
    public static final NamespacedKey SHOULD_RETURN_KEY = new NamespacedKey(CarrierAllaysPlugin.INSTANCE, "should_return");

    public CarrierAllayData(UUID sender, UUID target, UUID carrier, boolean should_return, CarrierAllayStatus currentStatus) {
        this.sender = sender;
        this.target = target;
        this.carrier = carrier;
        this.should_return = should_return;
        this.currentStatus = currentStatus;
    }

    public CarrierAllayData(UUID sender, UUID target, UUID carrier, CarrierAllayStatus currentStatus) {
        this(sender, target, carrier, false, currentStatus);
    }

    public CarrierAllayData withStatus(CarrierAllayStatus status)  {
        return new CarrierAllayData(this.sender, this.target, this.carrier, this.should_return, status);
    }

    public static PersistentDataType<long[], UUID> UUID_TYPE = new PersistentDataType<long[], UUID>() {
        
        @Override 
        public  Class<long[]> getPrimitiveType() {
            return long[].class;
        }

        @Override 
        public Class<UUID> getComplexType() {
            return UUID.class;
        }

        @Override 
        public long [] toPrimitive(UUID complex, PersistentDataAdapterContext context) {
            return new long[] {
                complex.getMostSignificantBits(),
                complex.getLeastSignificantBits()
            };
        }

        @Override 
        public UUID fromPrimitive( long[] primitive, PersistentDataAdapterContext context) {
            return new UUID(primitive[0], primitive[1]);
        }

    };

    public static CarrierAllayData loadFromAllayPDC(Allay allay) {
        PersistentDataContainer pdc = allay.getPersistentDataContainer();
        UUID sender = pdc.get(SENDER_KEY, UUID_TYPE);
        UUID target = (UUID)pdc.get(TARGET_KEY, UUID_TYPE);
        Integer statusCode = pdc.get(STATUS_KEY, PersistentDataType.INTEGER);
        Byte shouldReturn = pdc.get(SHOULD_RETURN_KEY, PersistentDataType.BYTE);

        // Guard for empty pdc data
        if (sender == null || target == null || statusCode == null ) {
            return null;
        }

        return new CarrierAllayData(
            sender,
            target,
            allay.getUniqueId(),
            shouldReturn != null && shouldReturn != 0,
            CarrierAllayStatus.fromCode(statusCode)
        );
    }

    public void saveToAllayPDC(Allay allay) {
        PersistentDataContainer pdc = allay.getPersistentDataContainer();
        pdc.set(SENDER_KEY, UUID_TYPE, this.sender);
        pdc.set(TARGET_KEY, UUID_TYPE, this.target);
        pdc.set(STATUS_KEY, PersistentDataType.INTEGER, this.currentStatus.getCode());
        pdc.set(SHOULD_RETURN_KEY, PersistentDataType.BYTE, (byte)(this.should_return ? 1 : 0));
    }

    public static boolean isSpawnedIn(Allay allay) {
        Byte spawned = allay.getPersistentDataContainer().get(SHOULD_RETURN_KEY, PersistentDataType.BYTE);
        return spawned != null && spawned != 0;
    }

}
