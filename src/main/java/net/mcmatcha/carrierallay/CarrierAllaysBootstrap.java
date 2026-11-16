package net.mcmatcha.carrierallay;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private void saveDefaultConfig(BootstrapContext context) {
        Path dataDir = context.getDataDirectory();
        Path configFile = dataDir.resolve("config.yml");

        if (Files.notExists(configFile)) {
            try {
                Files.createDirectories(dataDir);
                try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                    if (in != null) {
                        Files.copy(in, configFile);
                    } else {
                        context.getLogger().warn("Default config.yml not found in JAR resources.");
                    }
                }
            } catch (IOException e) {
                context.getLogger().error("Failed to save default config.yml", e);
            }
        }
    }

    @Override
    public void bootstrap(BootstrapContext context) {

        // Save default config if not exists
        saveDefaultConfig(context);

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
                
                String matchaFlavouredDatapackFilename = config.getMatchaFlavouredDatapackFilename();
                Map<String, DiscoveredDatapack> discoveredDatapacks = event.registrar().getDiscoveredPacks();

                // Guard if the file is not congiured
                if (matchaFlavouredDatapackFilename == null) {
                    logger.error("The matcha flavoured datapack filename is not configured please, set it up to use matcha-flavoured mode.");
                    return;
                }
    
                // Guard that the configured file name exists.
                if (!discoveredDatapacks.containsKey("file/"+matchaFlavouredDatapackFilename)) {
                    logger.error("The configured matcha flavoured datapack filename could not be found. Please configure the exact file name of the datapack folder.");
                    return;
                }

                // include the carrier allays datapack bundled in the plugin jar
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
