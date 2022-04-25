package net.starlegacy.feature.starship

import java.util.Locale
import net.starlegacy.feature.progression.Levels
import net.starlegacy.util.setDisplayNameAndGet
import net.starlegacy.util.setLoreAndGet
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

enum class StarshipType(
	val displayName: String,
	val minSize: Int,
	val maxSize: Int,
	val minLevel: Int,
	val containerPercent: Double,
	val crateLimitMultiplier: Double,
	val sneakFlyAccelDistance: Int,
	val maxSneakFlyAccel: Int,
	val interdictionRange: Int,
	val hyperspaceRangeMultiplier: Double,
	menuItemMaterial: Material,
	val isWarship: Boolean,
	val colour: String,
	val overridePermission: String
) {
	SPEEDER(
		displayName = "Speeder",
		minSize = 25,
		maxSize = 100,
		minLevel = 1,
		containerPercent = 0.025,
		crateLimitMultiplier = 0.25,
		sneakFlyAccelDistance = 5,
		maxSneakFlyAccel = 5,
		interdictionRange = 10,
		hyperspaceRangeMultiplier = 3.0,
		menuItemMaterial = Material.DEAD_BUSH,
		isWarship = false,
		colour = "#007100",
		overridePermission = "ion.ships.override.1"
	),
	STARFIGHTER(
		displayName = "Starfighter",
		minSize = 250,
		maxSize = 500,
		minLevel = 1,
		containerPercent = 0.025,
		crateLimitMultiplier = 0.5,
		sneakFlyAccelDistance = 4,
		maxSneakFlyAccel = 4,
		interdictionRange = 400,
		hyperspaceRangeMultiplier = 2.0,
		menuItemMaterial = Material.IRON_NUGGET,
		isWarship = true,
		colour = "#330000",
		overridePermission = "ion.ships.override.1"
	),
	GUNSHIP(
		displayName = "Gunship",
		minSize = 500,
		maxSize = 2000,
		minLevel = 10,
		containerPercent = 0.025,
		crateLimitMultiplier = 0.5,
		sneakFlyAccelDistance = 5,
		maxSneakFlyAccel = 2,
		interdictionRange = 800,
		hyperspaceRangeMultiplier = 2.1,
		menuItemMaterial = Material.IRON_INGOT,
		isWarship = true,
		colour = "#660000",
		overridePermission = "ion.ships.override.10"
	),
	CORVETTE(
		displayName = "Corvette",
		minSize = 2000,
		maxSize = 4000,
		minLevel = 20,
		containerPercent = 0.025,
		crateLimitMultiplier = 0.5,
		sneakFlyAccelDistance = 6,
		maxSneakFlyAccel = 2,
		interdictionRange = 1200,
		hyperspaceRangeMultiplier = 2.2,
		menuItemMaterial = Material.IRON_BLOCK,
		isWarship = true,
		colour = "#990000",
		overridePermission = "ion.ships.override.20"
	),
	FRIGATE(
		displayName = "Frigate",
		minSize = 4000,
		maxSize = 8000,
		minLevel = 40,
		containerPercent = 0.025,
		crateLimitMultiplier = 0.5,
		sneakFlyAccelDistance = 6,
		maxSneakFlyAccel = 2,
		interdictionRange = 1600,
		hyperspaceRangeMultiplier = 2.3,
		menuItemMaterial = Material.LAPIS_BLOCK,
		isWarship = true,
		colour = "#cc0000",
		overridePermission = "ion.ships.override.40"
	),
	DESTROYER(
		displayName = "Destroyer",
		minSize = 8000,
		maxSize = 12000,
		minLevel = 80,
		containerPercent = 0.025,
		crateLimitMultiplier = 0.5,
		sneakFlyAccelDistance = 5,
		maxSneakFlyAccel = 3,
		interdictionRange = 2000,
		hyperspaceRangeMultiplier = 2.4,
		menuItemMaterial = Material.GOLD_BLOCK,
		isWarship = true,
		colour = "#ff0000",
		overridePermission = "ion.ships.override.80"
	),
	SHUTTLE(
		displayName = "Shuttle",
		minSize = 100,
		maxSize = 500,
		minLevel = 1,
		containerPercent = 0.045,
		crateLimitMultiplier = 1.0,
		sneakFlyAccelDistance = 5,
		maxSneakFlyAccel = 2,
		interdictionRange = 300,
		hyperspaceRangeMultiplier = 1.0,
		menuItemMaterial = Material.PRISMARINE_SHARD,
		isWarship = false,
		colour = "#000033",
		overridePermission = "ion.ships.override.1"
	),
	TRANSPORT(
		displayName = "Transport",
		minSize = 500,
		maxSize = 2000,
		minLevel = 10,
		containerPercent = 0.045,
		crateLimitMultiplier = 1.0,
		sneakFlyAccelDistance = 10,
		maxSneakFlyAccel = 3,
		interdictionRange = 600,
		hyperspaceRangeMultiplier = 1.1,
		menuItemMaterial = Material.PRISMARINE_CRYSTALS,
		isWarship = false,
		colour = "#000066",
		overridePermission = "ion.ships.override.10"
	),
	LIGHT_FREIGHTER(
		displayName = "Light Freighter",
		minSize = 2000,
		maxSize = 4000,
		minLevel = 20,
		containerPercent = 0.045,
		crateLimitMultiplier = 1.0,
		sneakFlyAccelDistance = 10,
		maxSneakFlyAccel = 3,
		interdictionRange = 900,
		hyperspaceRangeMultiplier = 1.2,
		menuItemMaterial = Material.PRISMARINE_SLAB,
		isWarship = false,
		colour = "#000099",
		overridePermission = "ion.ships.override.20"
	),
	MEDIUM_FREIGHTER(
		displayName = "Medium Freighter",
		minSize = 4000,
		maxSize = 8000,
		minLevel = 40,
		containerPercent = 0.045,
		crateLimitMultiplier = 1.0,
		sneakFlyAccelDistance = 10,
		maxSneakFlyAccel = 3,
		interdictionRange = 1200,
		hyperspaceRangeMultiplier = 1.3,
		menuItemMaterial = Material.PRISMARINE_STAIRS,
		isWarship = false,
		colour = "#0000cc",
		overridePermission = "ion.ships.override.40"
	),
	HEAVY_FREIGHTER(
		displayName = "Heavy Freighter",
		minSize = 8000,
		maxSize = 12000,
		minLevel = 80,
		containerPercent = 0.045,
		crateLimitMultiplier = 1.0,
		sneakFlyAccelDistance = 10,
		maxSneakFlyAccel = 3,
		interdictionRange = 1500,
		hyperspaceRangeMultiplier = 1.4,
		menuItemMaterial = Material.PRISMARINE,
		isWarship = false,
		colour = "#0000ff",
		overridePermission = "ion.ships.override.80"
	),
	PLATFORM(
		displayName = "Platform",
		minSize = 25,
		maxSize = 100000,
		minLevel = 1,
		containerPercent = 0.0,
		crateLimitMultiplier = 0.0,
		sneakFlyAccelDistance = 0,
		maxSneakFlyAccel = 0,
		interdictionRange = 0,
		hyperspaceRangeMultiplier = 0.0,
		menuItemMaterial = Material.BEDROCK,
		isWarship = false,
		colour = "#ffffff",
		overridePermission = "ion.ships.override.1"
	);

	val formatted: String get() = "<$colour>$displayName</$colour>"

	val menuItem: ItemStack = ItemStack(menuItemMaterial)
		.setDisplayNameAndGet(displayName)
		.setLoreAndGet(
			listOf(
				"Min Block Count: $minSize",
				"Max Block Count: $maxSize",
				"Min Level: $minLevel",
				"Max Container:Total Blocks Ratio: $containerPercent",
				"Crate Limit Multiplier: $crateLimitMultiplier",
				"Sneak Fly Accel Distance: $sneakFlyAccelDistance",
				"Max Sneak Fly Accel: $maxSneakFlyAccel",
				"Interdiction Range: $interdictionRange",
				"Hyperspace Range Multiplier: $hyperspaceRangeMultiplier",
				"Warship: $isWarship"
			)
		)

	fun canUse(player: Player): Boolean =
		player.hasPermission("starships.anyship") || player.hasPermission(overridePermission) || Levels[player] >= minLevel

	companion object {
		private val stringMap = mutableMapOf<String, StarshipType>().apply {
			putAll(values().associateBy { it.name.lowercase(Locale.getDefault()) })
			putAll(values().associateBy { it.displayName.lowercase(Locale.getDefault()) })

			println(this)
		}

		fun getType(name: String): StarshipType? = stringMap[name.lowercase(Locale.getDefault())]

		fun getUnlockedTypes(player: Player): List<StarshipType> = values()
			.filter { it.canUse(player) }
			.sortedBy { it.minLevel }
	}
}
