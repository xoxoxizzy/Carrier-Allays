package net.mcmatcha.carrierallay.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Custom text config
 *
 * placeholders:
 * {cooldown} - The amount of time left for the player's cooldown
 * {target} - The player who got sent a carrier
 * {sender} - The player who sent the carrier
 */
public class CarrierAllaysPluginTextConfig {

    /** Message sent to the target when a carrier approaches from a long distance. */
    @JsonProperty("allay-approach-world")
    private String allayApproachWorld = "A carrier allay approaches from a long distance...";

    /** Message sent to the target when a carrier approaches from another dimension. */
    @JsonProperty("allay-approach-dimensional")
    private String allayApproachDimensional = "A carrier allay approaches from another dimension...";

    /** Message sent to the sender when they rename an allay while on cooldown. */
    @JsonProperty("allay-rename-on-cooldown")
    private String allayRenameOnCooldown = "You can send another carrier in {cooldown}s";

    /** Message sent to the sender when they spawn a carrier allay while on cooldown. */
    @JsonProperty("allay-egg-spawn-on-cooldown")
    private String allayEggSpawnOnCooldown = "You can spawn another carrier in {cooldown}s";

    /** Message sent to the target once the delivery has arrived. */
    @JsonProperty("delivery-completed")
    private String deliveryCompleted = "Your delivery from {sender} has arrived!";

    /** Message sent to the sender once the delivery has arrived. */
    @JsonProperty("delivery-arrived")
    private String deliveryArrived = "Your package was delivered to {target}";

    public String getAllayApproachWorld() {
        return allayApproachWorld;
    }

    public String getAllayApproachDimensional() {
        return allayApproachDimensional;
    }

    public String getAllayRenameOnCooldown() {
        return allayRenameOnCooldown;
    }

    public String getAllayEggSpawnOnCooldown() {
        return allayEggSpawnOnCooldown;
    }

    public String getDeliveryCompleted() {
        return deliveryCompleted;
    }

    public String getDeliveryArrived() {
        return deliveryArrived;
    }

}
