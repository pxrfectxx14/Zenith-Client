package dev.zenith.client.menu;

import dev.zenith.client.ZenithClientClient;
import dev.zenith.client.ZenithResourcePacks;
import dev.zenith.client.feature.Lang;
import dev.zenith.client.feature.Toggleable;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashSet;
import java.util.Set;

final class ResourcePackToggle implements Toggleable {

    private static final String PACK_ID = ResourceLocation.fromNamespaceAndPath(
            ZenithClientClient.MOD_ID, ZenithResourcePacks.PACK_PATH
    ).toString();

    @Override
    public String getDisplayName() {
        return Lang.current() == Lang.RU ? "Zenith GUI (ресурспак)" : "Zenith GUI (resource pack)";
    }

    @Override
    public boolean isEnabled() {
        return Minecraft.getInstance().getResourcePackRepository().getSelectedIds().contains(PACK_ID);
    }

    @Override
    public void setEnabled(boolean enabled) {
        var repository = Minecraft.getInstance().getResourcePackRepository();
        Set<String> selection = new LinkedHashSet<>(repository.getSelectedIds());

        if (enabled) {
            selection.add(PACK_ID);
        } else {
            selection.remove(PACK_ID);
        }

        repository.setSelected(selection);
        Minecraft.getInstance().reloadResourcePacks();
    }
}