package net.starlegacy.feature.gas.collectionfactors;

import java.util.Arrays;
import org.bukkit.Location;

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
