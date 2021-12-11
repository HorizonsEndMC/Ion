package net.starlegacy.feature.gas.collectionfactors;

import org.bukkit.Location;

public class WorldChanceFactor extends RandomFactor {

    String world;

    public WorldChanceFactor(float chance, String world) {
        super(chance);
        this.world = world;
    }

    @Override
    public boolean factor(Location location) {
        return super.factor(location) && location.getWorld().getName().equalsIgnoreCase(world);
    }
}
