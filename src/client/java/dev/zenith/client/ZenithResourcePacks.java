package dev.zenith.client;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Регистрирует встроенный в мод .zip-ресурспак (src/client/resources/resourcepacks/zenith-gui.zip)
 * как обычный "серверный" ресурспак Minecraft. После регистрации он появляется в списке паков
 * (Options -> Resource Packs) и им можно управлять как вручную, так и программно — см. GuiPackToggle.
 */
public final class ZenithResourcePacks {

    public static final String PACK_PATH = "zenith-gui";

    private ZenithResourcePacks() {
    }

    static void register() {
        var modContainer = FabricLoader.getInstance().getModContainer(ZenithClientClient.MOD_ID).orElseThrow();

        // Диагностика: видит ли ModContainer саму папку resourcepacks/zenith-gui на диске
        for (var rootPath : modContainer.getRootPaths()) {
            java.nio.file.Path candidate = rootPath.resolve("resourcepacks").resolve(PACK_PATH);
            System.out.println("[ZenithResourcePacks] Root path: " + rootPath
                    + " | resourcepacks/" + PACK_PATH + " exists: " + java.nio.file.Files.exists(candidate)
                    + " | pack.mcmeta exists: " + java.nio.file.Files.exists(candidate.resolve("pack.mcmeta")));
        }

        boolean registered = ResourceManagerHelper.registerBuiltinResourcePack(
                ResourceLocation.fromNamespaceAndPath(ZenithClientClient.MOD_ID, PACK_PATH),
                modContainer,
                Component.literal("Zenith GUI"),
                ResourcePackActivationType.DEFAULT_ENABLED
        );
        System.out.println("[ZenithResourcePacks] registerBuiltinResourcePack returned: " + registered);
    }

    public static boolean isEnabled() {
        String packId = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                ZenithClientClient.MOD_ID, PACK_PATH
        ).toString();
        return net.minecraft.client.Minecraft.getInstance()
                .getResourcePackRepository()
                .getSelectedIds()
                .contains(packId);
    }
}