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
	val isWarship: Boolean
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
		isWarship = false
	),
	SPACE_STATION(
		displayName = "Space Station",
		minSize = 48000,
		maxSize = 500000,
		minLevel = 100,
		containerPercent = 0.05,
		crateLimitMultiplier = 0.01,
		sneakFlyAccelDistance = 0,
		maxSneakFlyAccel = 0,
		interdictionRange = 10,
		hyperspaceRangeMultiplier = 1.0,
		menuItemMaterial = Material.DIAMOND,
		isWarship = false
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
		isWarship = true
	),
	GUNSHIP(
		displayName = "Gunship",
		minSize = 500,
		maxSize = 2000,
		minLevel = 12,
		containerPercent = 0.025,
		crateLimitMultiplier = 0.5,
		sneakFlyAccelDistance = 5,
		maxSneakFlyAccel = 2,
		interdictionRange = 800,
		hyperspaceRangeMultiplier = 2.1,
		menuItemMaterial = Material.IRON_INGOT,
		isWarship = true
	),
	CORVETTE(
		displayName = "Corvette",
		minSize = 2000,
		maxSize = 4000,
		minLevel = 24,
		containerPercent = 0.025,
		crateLimitMultiplier = 0.5,
		sneakFlyAccelDistance = 6,
		maxSneakFlyAccel = 2,
		interdictionRange = 1200,
		hyperspaceRangeMultiplier = 2.2,
		menuItemMaterial = Material.IRON_BLOCK,
		isWarship = true
	),
	FRIGATE(
		displayName = "Frigate",
		minSize = 4000,
		maxSize = 8000,
		minLevel = 36,
		containerPercent = 0.025,
		crateLimitMultiplier = 0.5,
		sneakFlyAccelDistance = 6,
		maxSneakFlyAccel = 2,
		interdictionRange = 1600,
		hyperspaceRangeMultiplier = 2.3,
		menuItemMaterial = Material.LAPIS_BLOCK,
		isWarship = true
	),
	DESTROYER(
		displayName = "Destroyer",
		minSize = 8000,
		maxSize = 12000,
		minLevel = 48,
		containerPercent = 0.025,
		crateLimitMultiplier = 0.5,
		sneakFlyAccelDistance = 5,
		maxSneakFlyAccel = 3,
		interdictionRange = 2000,
		hyperspaceRangeMultiplier = 2.4,
		menuItemMaterial = Material.GOLD_BLOCK,
		isWarship = true
	),
	INTERDICTOR(
		displayName = "Interdictor",
		minSize = 14000,
		maxSize = 22000,
		minLevel = 50,
		containerPercent = 0.015,
		crateLimitMultiplier = 0.25,
		sneakFlyAccelDistance = 1,
		maxSneakFlyAccel = 1,
		interdictionRange = 4000,
		hyperspaceRangeMultiplier = 3.0,
		menuItemMaterial = Material.PURPLE_DYE,
		isWarship = true
	),
	BATTLECRUISER(
		displayName = "Battlecruiser",
		minSize = 12000,
		maxSize = 20000,
		minLevel = 60,
		containerPercent = 0.025,
		crateLimitMultiplier = 0.5,
		sneakFlyAccelDistance = 5,
		maxSneakFlyAccel = 5,
		interdictionRange = 2400,
		hyperspaceRangeMultiplier = 2.5,
		menuItemMaterial = Material.DIAMOND_BLOCK,
		isWarship = true
	),
	BATTLESHIP(
		displayName = "Battleship",
		minSize = 20000,
		maxSize = 32000,
		minLevel = 72,
		containerPercent = 0.025,
		crateLimitMultiplier = 0.5,
		sneakFlyAccelDistance = 5,
		maxSneakFlyAccel = 3,
		interdictionRange = 2800,
		hyperspaceRangeMultiplier = 2.6,
		menuItemMaterial = Material.MAGMA_BLOCK,
		isWarship = true
	),
	DREADNOUGHT(
		displayName = "Dreadnought",
		minSize = 32000,
		maxSize = 48000,
		minLevel = 84,
		containerPercent = 0.025,
		crateLimitMultiplier = 0.5,
		sneakFlyAccelDistance = 10,
		maxSneakFlyAccel = 2,
		interdictionRange = 3200,
		hyperspaceRangeMultiplier = 2.7,
		menuItemMaterial = Material.EMERALD_BLOCK,
		isWarship = true
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
		isWarship = false
	),
	TRANSPORT(
		displayName = "Transport",
		minSize = 500,
		maxSize = 2000,
		minLevel = 12,
		containerPercent = 0.045,
		crateLimitMultiplier = 1.0,
		sneakFlyAccelDistance = 10,
		maxSneakFlyAccel = 3,
		interdictionRange = 600,
		hyperspaceRangeMultiplier = 1.1,
		menuItemMaterial = Material.PRISMARINE_CRYSTALS,
		isWarship = false
	),
	LIGHT_FREIGHTER(
		displayName = "Light Freighter",
		minSize = 2000,
		maxSize = 4000,
		minLevel = 24,
		containerPercent = 0.045,
		crateLimitMultiplier = 1.0,
		sneakFlyAccelDistance = 10,
		maxSneakFlyAccel = 3,
		interdictionRange = 900,
		hyperspaceRangeMultiplier = 1.2,
		menuItemMaterial = Material.PRISMARINE_SLAB,
		isWarship = false
	),
	MEDIUM_FREIGHTER(
		displayName = "Medium Freighter",
		minSize = 4000,
		maxSize = 8000,
		minLevel = 36,
		containerPercent = 0.045,
		crateLimitMultiplier = 1.0,
		sneakFlyAccelDistance = 10,
		maxSneakFlyAccel = 3,
		interdictionRange = 1200,
		hyperspaceRangeMultiplier = 1.3,
		menuItemMaterial = Material.PRISMARINE_STAIRS,
		isWarship = false
	),
	BULK_FREIGHTER(
		displayName = "Bulk Freighter",
		minSize = 8000,
		maxSize = 12000,
		minLevel = 48,
		containerPercent = 0.045,
		crateLimitMultiplier = 1.0,
		sneakFlyAccelDistance = 10,
		maxSneakFlyAccel = 3,
		interdictionRange = 1500,
		hyperspaceRangeMultiplier = 1.4,
		menuItemMaterial = Material.PRISMARINE,
		isWarship = false
	),
	HEAVY_FREIGHTER(
		displayName = "Heavy Freighter",
		minSize = 12000,
		maxSize = 20000,
		minLevel = 60,
		containerPercent = 0.045,
		crateLimitMultiplier = 1.0,
		sneakFlyAccelDistance = 10,
		maxSneakFlyAccel = 3,
		interdictionRange = 1800,
		hyperspaceRangeMultiplier = 1.5,
		menuItemMaterial = Material.PRISMARINE_BRICKS,
		isWarship = false
	),
	BARGE(
		displayName = "Barge",
		minSize = 20000,
		maxSize = 32000,
		minLevel = 72,
		containerPercent = 0.045,
		crateLimitMultiplier = 1.0,
		sneakFlyAccelDistance = 10,
		maxSneakFlyAccel = 3,
		interdictionRange = 2100,
		hyperspaceRangeMultiplier = 1.6,
		menuItemMaterial = Material.DARK_PRISMARINE,
		isWarship = false
	),
	TANKER(
		displayName = "Tanker",
		minSize = 32000,
		maxSize = 48000,
		minLevel = 84,
		containerPercent = 0.045,
		crateLimitMultiplier = 1.0,
		sneakFlyAccelDistance = 10,
		maxSneakFlyAccel = 3,
		interdictionRange = 2400,
		hyperspaceRangeMultiplier = 1.7,
		menuItemMaterial = Material.GLOWSTONE,
		isWarship = false
	);


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
		player.hasPermission("starships.anyship") || Levels[player] >= minLevel

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
