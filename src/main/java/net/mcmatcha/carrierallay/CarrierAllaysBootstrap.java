package net.mcmatcha.carrierallay;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;

import com.crimsonwarpedcraft.cwcommons.config.bukkit.BukkitConfigManagerBuilder;

import io.papermc.paper.datapack.DiscoveredDatapack;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.mcmatcha.carrierallay.config.CarrierAllaysPluginConfig;

public final class CarrierAllaysBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {

        // Load the config file
        try {
            File dataDirectory = context.getDataDirectory().toFile();
            
            CarrierAllaysPlugin.CONFIG_INSTANCE = new BukkitConfigManagerBuilder()
                .build()
                .load(new File(dataDirectory, "config.yml"), CarrierAllaysPluginConfig.class);

        } catch (IOException | IllegalStateException e) {
            context.getLogger().error("Failed to load config: " + e.getMessage());
            return;
        }

        CarrierAllaysPluginConfig config = CarrierAllaysPlugin.CONFIG_INSTANCE;
        ComponentLogger logger = context.getLogger();

        // Discover matcha datapack

        if (config.getMatchaFlavoured()) {
            context.getLifecycleManager().registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY, event -> {
                
                // Guard if the file is not congiured
                if (config.getMatchaFlavouredDatapackFilename() == null) {
                    logger.error("The matcha flavoured datapack filename is not configured please, set it up to use matcha-flavoured mode.");
                    return;
                }
                
                Map<String, DiscoveredDatapack> discoveredDatapacks =  event.registrar().getDiscoveredPacks();
                String matchaFlavouredDatapackFilename = config.getMatchaFlavouredDatapackFilename();

                // Guard that the configured file name exists.
                if (!discoveredDatapacks.containsKey("file/"+matchaFlavouredDatapackFilename)) {
                    logger.error("The configured matcha flavoured datapack filename could not be found. Please configure the exact file name of the datapack folder.");
                    return;
                }

                try {
                    final URI datapack_uri = this.getClass().getResource("/carrier_allays_data").toURI();
                    event.registrar().discoverPack(datapack_uri, "provided_carrier_allays_data");

                } catch (URISyntaxException | IOException | NullPointerException e) {
                    logger.error("Could not find the provided carrier allays datapack. Aborting...");
                    throw new RuntimeException(e);
                }
            
            });
        }
    }

    @Override
    public JavaPlugin createPlugin(final PluginProviderContext context) {
        return new CarrierAllaysPlugin();
    }

}
