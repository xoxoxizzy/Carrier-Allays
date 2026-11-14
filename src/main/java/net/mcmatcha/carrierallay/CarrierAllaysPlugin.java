package net.mcmatcha.carrierallay;

import java.io.File;
import java.io.IOException;

import org.bukkit.plugin.java.JavaPlugin;

import com.crimsonwarpedcraft.cwcommons.config.bukkit.BukkitConfigManagerBuilder;
import com.crimsonwarpedcraft.cwcommons.store.DataStore;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;
import net.mcmatcha.carrierallay.config.CarrierAllaysPluginConfig;
import net.mcmatcha.carrierallay.listener.EntityRenameListener;
import net.mcmatcha.carrierallay.tasks.AllayDeliveryScheduledTask;

/**
 * Created by Levi Muniz on 7/29/20.
 *
 * @author Copyright (c) Levi Muniz. All Rights Reserved.
 */
public class CarrierAllaysPlugin extends JavaPlugin {

  // Singleton
  public static CarrierAllaysPlugin INSTANCE;
  public static CarrierAllaysPluginConfig CONFIG_INSTANCE;

  public CarrierAllaysPlugin() {

  }

  @Override
  public void onLoad() {
    CommandAPI.onLoad(new CommandAPIPaperConfig(this));
  }

  @Override
  public void onEnable() {
    INSTANCE = this;
    CommandAPI.onEnable();
    saveDefaultConfig();

    try {
      CONFIG_INSTANCE = new BukkitConfigManagerBuilder()
        .build()
        .load(new File(getDataFolder(), "config.yml"), CarrierAllaysPluginConfig.class);
    } catch (IOException | IllegalStateException e) {
      getLogger().severe("Failed to load config: " + e.getMessage());
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
      
    getServer().getPluginManager()
      .registerEvents(new EntityRenameListener(this), this);
  
    getServer().getScheduler().runTaskTimer(INSTANCE, new AllayDeliveryScheduledTask(), 0, 10);

  }

  @Override
  public void onDisable() {
    INSTANCE = null;
    CommandAPI.onDisable();
  }


}
