package dev.awfuul.legacyarmorcompat;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

public class Legacyarmorcompat implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("LegacyArmorCompat");

    private static final Map<String, TextureMapping> ARMOR_TEXTURE_MAPPINGS = ImmutableMap.<String, TextureMapping>builder()
            .put("diamond", new TextureMapping(
                    "textures/entity/equipment/humanoid/diamond.png",
                    "textures/models/armor/diamond_layer_1.png"))
            .put("diamond_leggings", new TextureMapping(
                    "textures/entity/equipment/humanoid_leggings/diamond.png",
                    "textures/models/armor/diamond_layer_2.png"))
            .put("netherite", new TextureMapping(
                    "textures/entity/equipment/humanoid/netherite.png",
                    "textures/models/armor/netherite_layer_1.png"))
            .put("netherite_leggings", new TextureMapping(
                    "textures/entity/equipment/humanoid_leggings/netherite.png",
                    "textures/models/armor/netherite_layer_2.png"))
            .put("iron", new TextureMapping(
                    "textures/entity/equipment/humanoid/iron.png",
                    "textures/models/armor/iron_layer_1.png"))
            .put("iron_leggings", new TextureMapping(
                    "textures/entity/equipment/humanoid_leggings/iron.png",
                    "textures/models/armor/iron_layer_2.png"))
            .put("gold", new TextureMapping(
                    "textures/entity/equipment/humanoid/gold.png",
                    "textures/models/armor/gold_layer_1.png"))
            .put("gold_leggings", new TextureMapping(
                    "textures/entity/equipment/humanoid_leggings/gold.png",
                    "textures/models/armor/gold_layer_2.png"))
            .put("leather", new TextureMapping(
                    "textures/entity/equipment/humanoid/leather.png",
                    "textures/models/armor/leather_layer_1.png"))
            .put("leather_leggings", new TextureMapping(
                    "textures/entity/equipment/humanoid_leggings/leather.png",
                    "textures/models/armor/leather_layer_2.png"))
            .put("chainmail", new TextureMapping(
                    "textures/entity/equipment/humanoid/chainmail.png",
                    "textures/models/armor/chainmail_layer_1.png"))
            .put("chainmail_leggings", new TextureMapping(
                    "textures/entity/equipment/humanoid_leggings/chainmail.png",
                    "textures/models/armor/chainmail_layer_2.png"))
            .build();

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(new LegacyArmorReloadListener());
    }

    public static class TextureMapping {
        public final String newPath;
        public final String legacyPath;

        public TextureMapping(String newPath, String legacyPath) {
            this.newPath = newPath;
            this.legacyPath = legacyPath;
        }
    }

    public static class LegacyArmorReloadListener implements SimpleSynchronousResourceReloadListener {

        @Override
        public Identifier getFabricId() {
            return Identifier.of("legacyarmorcompat", "legacy_armor");
        }

        @Override
        public void reload(ResourceManager manager) {
            for (Map.Entry<String, TextureMapping> entry : ARMOR_TEXTURE_MAPPINGS.entrySet()) {
                TextureMapping mapping = entry.getValue();
                Identifier newId = Identifier.of("minecraft", mapping.newPath);
                Identifier legacyId = Identifier.of("minecraft", mapping.legacyPath);
                Optional<Resource> legacyRes = manager.getResource(legacyId);
                boolean usingLegacy = legacyRes.isPresent();
                Optional<Resource> textureRes = usingLegacy ? legacyRes : manager.getResource(newId);

                if (textureRes.isPresent()) {
                    if (usingLegacy) {
                        LOGGER.info("Overriding texture '{}' with legacy texture '{}' for key '{}'", mapping.newPath, mapping.legacyPath, entry.getKey());
                    } else {
                        LOGGER.info("Registering default texture '{}' for key '{}'", mapping.newPath, entry.getKey());
                    }
                    try (InputStream in = textureRes.get().getInputStream()) {
                        NativeImage image = NativeImage.read(in);
                        MinecraftClient.getInstance().getTextureManager().registerTexture(newId, new NativeImageBackedTexture(image)
                        );
                        LOGGER.info("Registered texture for key '{}'", entry.getKey());
                    } catch (IOException e) {
                        LOGGER.error("Failed to load texture for key '{}'", entry.getKey(), e);
                    }
                }
            }
        }
    }
}