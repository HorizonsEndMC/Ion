package net.starlegacy.feature.gas.collectionfactors;

import org.bukkit.Location;

import java.util.Arrays;

public class WorldLimitFactor extends CollectionFactor {

    private String[] enabledWorlds;

    public WorldLimitFactor(String[] enabledWorlds) {
        this.enabledWorlds = enabledWorlds;
    }

    @Override
    public boolean factor(Location location) {
        return Arrays.stream(enabledWorlds).anyMatch(location.getWorld().getName()::equalsIgnoreCase);
    }
}
