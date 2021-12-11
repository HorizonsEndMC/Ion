package net.starlegacy.feature.gas.collectionfactors;

import org.bukkit.Location;

public class RandomFactor extends CollectionFactor {

    private float chance;

    public RandomFactor(float chance) {
        this.chance = chance;
    }

    @Override
    public boolean factor(Location location) {
        return Math.random() <= chance;
    }
}
