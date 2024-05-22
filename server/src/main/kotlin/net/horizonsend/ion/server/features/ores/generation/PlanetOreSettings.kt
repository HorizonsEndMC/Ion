package net.horizonsend.ion.server.features.ores.generation

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.ores.storage.Ore
import net.horizonsend.ion.server.miscellaneous.utils.enumSetOf
import org.bukkit.Material
import org.bukkit.World
import java.util.EnumSet
import kotlin.math.PI

/**
 * A planet's ore settings
 *
 * @param dataVersion The current version of ore generation for this planet
 *
 * @param groundMaterials Materials that may be replaced by the ore
 * @param ores A list of ores that may appear on the planet
 **/
enum class PlanetOreSettings(
	val planetName: String,
	val dataVersion: Int,
	val groundMaterials: EnumSet<Material>,
	val ores: List<OreSetting>
) {
	CHANDRA(
		"Chandra",
		12,
		enumSetOf(
			Material.STONE,
			Material.TUFF,
			Material.BLACKSTONE,
			Material.COBBLED_DEEPSLATE
		),
		listOf(
			OreSetting(Ore.TITANIUM, 3),
			OreSetting(Ore.ALUMINIUM, 2)
		)
	),
	ILIUS(
		"Ilius",
		12,
		enumSetOf(
			Material.STONE,
			Material.DEEPSLATE
		),
		listOf(
			OreSetting(Ore.IRON, 2)
		)
	),
	LUXITERNA(
		"Luxiterna",
		12,
		enumSetOf(
			Material.DIORITE,
			Material.CALCITE
		),
		listOf(
			OreSetting(Ore.URANIUM, 2),
			OreSetting(Ore.CHETHERITE, 2)
		)
	),
	HERDOLI(
		"Herdoli",
		12,
		enumSetOf(
			Material.STONE,
			Material.GRANITE,
			Material.ANDESITE,
			Material.DIORITE
		),
		listOf(
			OreSetting(Ore.COPPER, 3)
		)
	),
	RUBACIEA(
		"Rubaciea",
		12,
		enumSetOf(
			Material.NETHERRACK,
			Material.RED_TERRACOTTA,
			Material.RED_CONCRETE
		),
		listOf(
			OreSetting(Ore.REDSTONE, 3),
			OreSetting(Ore.QUARTZ, 2)
		)
	),
	ARET(
		"Aret",
		12,
		enumSetOf(
			Material.STONE,
			Material.GRANITE,
			Material.ANDESITE,
			Material.DIORITE
		),
		listOf(
			OreSetting(Ore.IRON, 3),
			OreSetting(Ore.TITANIUM, 2)
		)
	),
	AERACH(
		"Aerach",
		12,
		enumSetOf(
			Material.STONE,
			Material.GRANITE,
			Material.ANDESITE,
			Material.DIORITE
		),
		listOf(
			OreSetting(Ore.CHETHERITE, 2),
			OreSetting(Ore.COAL, 2)
		)
	),
	VASK(
		"Vask",
		12,
		enumSetOf(
			Material.STONE,
			Material.POLISHED_GRANITE,
			Material.ORANGE_TERRACOTTA
		),
		listOf(
			OreSetting(Ore.COPPER, 2),
			OreSetting(Ore.REDSTONE, 2)
		)
	),
	GAHARA(
		"Gahara",
		12,
		enumSetOf(
			Material.STONE,
			Material.GRANITE,
			Material.ANDESITE,
			Material.DIORITE,
			Material.LIGHT_GRAY_TERRACOTTA,
			Material.GRAVEL,
			Material.PRISMARINE,
			Material.PACKED_ICE
		),
		listOf(
			OreSetting(Ore.GOLD, 3),
			OreSetting(Ore.EMERALD, 2)
		)
	),
	ISIK(
		"Isik",
		12,
		enumSetOf(
			Material.STONE,
			Material.GRANITE,
			Material.ANDESITE,
			Material.DIORITE
		),
		listOf(
			OreSetting(Ore.NETHERITE, 2),
			OreSetting(Ore.GOLD, 2)
		)
	),
	CHIMGARA(
		"Chimgara",
		12,
		enumSetOf(
			Material.STONE,
			Material.GRANITE,
			Material.ANDESITE,
			Material.DIORITE
		),
		listOf(
			OreSetting(Ore.REDSTONE, 2),
			OreSetting(Ore.ALUMINIUM, 2)
		)
	),
	DAMKOTH(
		"Damkoth",
		12,
		enumSetOf(
			Material.DEEPSLATE
		),
		listOf(
			OreSetting(Ore.CHETHERITE, 3)
		)
	),
	KRIO(
		"Krio",
		12,
		enumSetOf(
			Material.STONE,
			Material.GRANITE,
			Material.ANDESITE,
			Material.DIORITE
		),
		listOf(
			OreSetting(Ore.DIAMOND, 3),
			OreSetting(Ore.LAPIS, 2)
		)
	),
	QATRA(
		"Qatra",
		12,
		enumSetOf(
			Material.STONE,
			Material.GRANITE,
			Material.ANDESITE,
			Material.DIORITE
		),
		listOf(
			OreSetting(Ore.URANIUM, 2),
			OreSetting(Ore.ALUMINIUM, 2)
		)
	),
	KOVFEFE(
		"Kovfefe",
		12,
		enumSetOf(
			Material.STONE,
			Material.GRANITE,
			Material.ANDESITE,
			Material.DIORITE,
			Material.END_STONE
		),
		listOf(
			OreSetting(Ore.ALUMINIUM, 3)
		)
	),
	LIODA(
		"Lioda",
		13,
		enumSetOf(
			Material.DIORITE,
			Material.CALCITE,
			Material.DEEPSLATE,
			Material.STONE
		),
		listOf(
			OreSetting(Ore.QUARTZ, 1),
			OreSetting(Ore.DIAMOND, 1)
		)
	),
	TURMS(
		"Turms",
		12,
		enumSetOf(
			Material.STONE,
			Material.GRANITE,
			Material.ANDESITE,
			Material.DIORITE
		),
		listOf(
			OreSetting(Ore.LAPIS, 3),
			OreSetting(Ore.EMERALD, 2)
		)
	),
	EDEN(
		"Ilius_horizonsend_eden",
		12,
		enumSetOf(
			Material.STONE,
			Material.DEEPSLATE,
			Material.POLISHED_DEEPSLATE,
			Material.COBBLED_DEEPSLATE
		),
		listOf(
			OreSetting(Ore.IRON, 2),
			OreSetting(Ore.CHETHERITE, 2)
		)
	);

	/**
	 * Represents an ore that may be generated on a planet
	 *
	 * @param ore The ore that can be generated
	 * @param stars The number of stars this ore has //TODO explain that better
	 **/
	@Serializable
	data class OreSetting(
		val ore: Ore,
		val stars: Int,
		val blobSizeMin: Int = 3,
		val blobSizeMax: Int = 5
	) {
		/**
		 * An estimate of the average volume of each ore blob
		 **/
		fun getVolume(): Double = (4.0 / 3.0) * (blobSizeMax * blobSizeMax * blobSizeMax) * PI
	}

	fun getWorld() = IonServer.server.getWorld(planetName)

	companion object {
		private val byPlanet = mutableMapOf<World, PlanetOreSettings?>()

		operator fun get(world: World) = byPlanet.getOrPut(world) {
			entries.firstOrNull { it.getWorld()?.uid == world.uid }
		}
	}
}
