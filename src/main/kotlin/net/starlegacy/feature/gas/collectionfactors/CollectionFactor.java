package net.starlegacy.feature.gas.collectionfactors;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public abstract class CollectionFactor {

    public static List<CollectionFactor> collectionSetFromString(String string) {
        List<CollectionFactor> collectionFactors = new ArrayList<>();
        for (String text : string.split(";")) collectionFactors.add(valueOf(text));
        return collectionFactors;
    }

    private static CollectionFactor valueOf(String text) {
        String[] params = text.split(":");
        switch (params[0].toLowerCase()) {
            case "atmosphereheight":
                return new AtmosphereHeightFactor(Integer.parseInt(params[1]), Integer.parseInt(params[2]));
            case "distance":
                String[] locationParams = params[1].split(",");
                return new DistanceFactor(
                        new Location(Bukkit.getWorld(locationParams[0]), Integer.parseInt(locationParams[1]),
                                     Integer.parseInt(locationParams[2]), Integer.parseInt(locationParams[3])),
                        Integer.parseInt(params[2]), Float.parseFloat(params[3]));
            case "random":
                return new RandomFactor(Float.parseFloat(params[1]));
            case "hyperspaceonly":
                return new HyperspaceOnlyFactor();
            case "spaceonly":
                return new SpaceOnlyFactor();
            case "worldlimit":
                return new WorldLimitFactor(params[1].split(","));
            case "worldchance":
                return new WorldChanceFactor(Float.parseFloat(params[1]), params[2]);
            case "skylight":
            case "outdoors":
                return new OutdoorsFactor();
        }
        return new RandomFactor(1.0f);
    }

    public abstract boolean factor(Location location);
}
