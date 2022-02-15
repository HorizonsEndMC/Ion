package net.starlegacy.feature.gas;

import net.starlegacy.feature.gas.collectionfactors.CollectionFactor;
import net.starlegacy.feature.misc.CustomItem;
import net.starlegacy.feature.misc.CustomItems;
import org.bukkit.Location;

import java.util.List;

public class Gas {
//	HELIUM("gas_helium") {
//		@Override
//		public boolean isAvailable(Location location) {
//			return new RandomFactor(0.05f).factor(location) && new AtmosphereHeightFactor(70, 0).factor(location);
//		}
//	},
//	OXYGEN("gas_oxygen") {
//		@Override
//		public boolean isAvailable(Location location) {
//			return new RandomFactor(0.1f).factor(location) && new AtmosphereHeightFactor(200, 60).factor(location);
//		}
//	},
//	HYDROGREN("gas_hydrogen") {
//		@Override
//		public boolean isAvailable(Location location) {
//			return (new RandomFactor(0.08f).factor(location) && new AtmosphereHeightFactor(255, 120).factor(location))
//					|| (new SpaceOnlyFactor().factor(location)
//							&& new DistanceFactor(new Location(location.getWorld(), 0, 128, 0), 1500, 10)
//									.factor(location));
//		}
//	},
//	NITROGEN("gas_nitrogen") {
//		@Override
//		public boolean isAvailable(Location location) {
//			return (new RandomFactor(0.07f).factor(location) && new AtmosphereHeightFactor(255, 120).factor(location))
//					|| (new SpaceOnlyFactor().factor(location)
//							&& new DistanceFactor(new Location(location.getWorld(), 0, 128, 0), 1500, 1)
//									.factor(location));
//		}
//	},
//	CARBON_DIOXIDE("gas_carbon_dioxide") {
//		@Override
//		public boolean isAvailable(Location location) {
//			return new RandomFactor(0.02f).factor(location) && new AtmosphereHeightFactor(150, 30).factor(location);
//		}
//	};


    private String itemId, name;

    private List<List<CollectionFactor>> factors;

    public Gas(String name, String itemId, List<List<CollectionFactor>> factors) {
        this.name = name;
        this.itemId = itemId;
        this.factors = factors;
    }

    public boolean isAvailable(Location location) {
        return factors.stream().anyMatch(f -> f.stream().allMatch(i -> i.factor(location)));
    }

    public String getItemId() {
        return itemId;
    }


    public CustomItem getItem() {
        return CustomItems.INSTANCE.get(getItemId());
    }

    public List<List<CollectionFactor>> getFactors() {
        return factors;
    }

    public String getName() {
        return name;
    }
}
