package net.mcmatcha.carrierallay.config;

import com.crimsonwarpedcraft.cwcommons.config.Config;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CarrierAllaysPluginConfig implements Config {
    
    /**
     * Determines how long an allay flys away before teleporting near a player that is too far away to delivery normally.
     * Default: 10s
     */ 
    @JsonProperty("allay-flyaway-milliseconds")
    private long flyAwayMillis = 10000L;

    /**
     * Determines how close an allay needs to be to the player to complete the delivery.
     * Default: 4 blocks
     * This value is in the distance squared format not blocks.
     */
    @JsonProperty("near-distance")
    private double nearDistance = 16d;

    /***
     * Determines how far a player needs to be before deciding to teleport instead of flying directly.
     * Default: 150 blocks
     * This value is in the dinstance squared format not blocks.
     */
    @JsonProperty("teleport-distance")
    private double teleportDistance = 22500d;

    /**
     * Determines the minimum amount of blocks from the player the allay should teleport to.
     * Default: 45 blocks
     * This value is in blocks not distance squared.
     */ 
    @JsonProperty("teleport-min-offset")
    private double teleportMinOffset = 45d;
    
    /**
     * Determines the maximum amount of blocks from the player the allay should teleport to.
     * Default: 55 blocks
     * This value is in blocks not distance squared.
     */
    @JsonProperty("teleport-max-offset")
    private double teleportMaxOffset = 55d;

    /**
     * Determines how fast carrier allays move
     * Default: 1
     */
    @JsonProperty("carrier-allay-speed")
    private double carrierAllaySpeed = 1.5d;

    /**
     * Determines how far the player can drift from the current path goal before the allay repaths.
     * Default: 4 blocks
     * This value is in the distance squared format not blocks.
     */
    @JsonProperty("repath-distance")
    private double repathDistance = 16d;

    /**
     * Determines how far ahead of the allay the fly-away waypoint is placed.
     * Default: 16 blocks
     * This value is in blocks not distance squared.
     */
    @JsonProperty("fly-off-waypoint-distance")
    private double flyOffWaypointDistance = 16d;

    /**
     * Determines how far in front of the player the allay hovers while waiting.
     * Default: 2.5 blocks
     * This value is in blocks not distance squared.
     */
    @JsonProperty("hover-distance")
    private double hoverDistance = 2.5d;

    /**
     * Determines how close the allay needs to be to its hover spot to hold still.
     * Default: 2 blocks
     * This value is in the distance squared format not blocks.
     */
    @JsonProperty("hover-tolerance")
    private double hoverTolerance = 4d;

    /**
     * Enables "matcha flavoured" mode. Tweaks how you spawn carriers, requires the matcha flavoured datapack installed.
     */
    @JsonProperty("matcha-flavoured")
    private boolean matchaFlavoured = false;

    @JsonProperty("mf-datapack-filename")
    private String matchaFlavouredDatapackFilename = "matcha-flavoured";

    /**
     * Determines how long a player must wait after sending a carrier before they can send another one.
     * Default: 3m
     */
    @JsonProperty("rename-cooldown-milliseconds")
    private long renameCooldownMillis = 180000L;

    public String getMatchaFlavouredDatapackFilename() {
        return matchaFlavouredDatapackFilename;
    }
    
    public boolean getMatchaFlavoured() {
        return matchaFlavoured;
    }

    public long getRenameCooldownMillis() {
        return renameCooldownMillis;
    }

    public long getFlyAwayMillis() {
        return flyAwayMillis;
    }

    public double getNearDistance() {
        return nearDistance;
    }

    public double getTeleportDistance() {
        return teleportDistance;
    }

    public double getTeleportMaxOffset() {
        return teleportMaxOffset;
    }
    
    public double getTeleportMinOffset() {
        return teleportMinOffset;
    }

    public double getCarrierAllaySpeed() {
        return carrierAllaySpeed;
    }

    public double getRepathDistance() {
        return repathDistance;
    }

    public double getFlyOffWaypointDistance() {
        return flyOffWaypointDistance;
    }

    public double getHoverDistance() {
        return hoverDistance;
    }

    public double getHoverTolerance() {
        return hoverTolerance;
    }

}
