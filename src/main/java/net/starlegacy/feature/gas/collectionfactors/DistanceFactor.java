package net.starlegacy.feature.gas.collectionfactors;

import org.bukkit.Location;

public class DistanceFactor extends CollectionFactor {

    private Location origin;
    private double maxDistance;
    private float multiplier;

    public DistanceFactor(Location origin, double maxDistance, float multiplier) {
        this.origin = origin;
        this.maxDistance = maxDistance;
        this.multiplier = multiplier;
    }

    @Override
    public boolean factor(Location location) {
        if (!origin.getWorld().getName().equals(location.getWorld().getName()))
            return false;
        double distance = location.distance(origin);
        if (distance < 1) return true;
        if (distance > maxDistance)
            return false;
        return new RandomFactor(multiplier / (float) distance).factor(location);
    }

}
