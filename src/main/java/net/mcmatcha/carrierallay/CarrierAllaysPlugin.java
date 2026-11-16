package net.mcmatcha.carrierallay;

import org.bukkit.plugin.java.JavaPlugin;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;
import net.mcmatcha.carrierallay.config.CarrierAllaysPluginConfig;
import net.mcmatcha.carrierallay.listener.AllayDeathEventListener;
import net.mcmatcha.carrierallay.listener.AllayEntityLoadListener;
import net.mcmatcha.carrierallay.listener.AllayEntityRenameListener;
import net.mcmatcha.carrierallay.managers.CarrierAllaysStore;
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
  public static CarrierAllaysStore ACTIVE_CARRIERS_STORE;

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

    ACTIVE_CARRIERS_STORE = new CarrierAllaysStore();
    ACTIVE_CARRIERS_STORE.initActiveCarrierAllays(getServer());

    getServer().getPluginManager()
      .registerEvents(new AllayEntityRenameListener(this), this);
    getServer().getPluginManager()
      .registerEvents(new AllayDeathEventListener(), this);
    getServer().getPluginManager()
      .registerEvents(new AllayEntityLoadListener(), this);
  
    getServer().getScheduler().runTaskTimer(INSTANCE, new AllayDeliveryScheduledTask(), 0, 10);

  }

  @Override
  public void onDisable() {
    INSTANCE = null;
    CommandAPI.onDisable();
  }


}
