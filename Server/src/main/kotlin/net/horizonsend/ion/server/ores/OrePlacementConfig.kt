package net.horizonsend.ion.server.ores

import java.util.EnumSet
import net.horizonsend.ion.common.utilities.enumSetOf
import org.bukkit.Material

/*
 TODO: This should be loaded from a configuration file.
*/

@Suppress("unused")
enum class OrePlacementConfig(
	val groundMaterial: EnumSet<Material> = enumSetOf(),
	val options: Map<Ore, Int> = mapOf(),
	val currentOreVersion: Int = 9
) {
	Chandra(
		enumSetOf(
			Material.STONE,
			Material.TUFF,
			Material.BLACKSTONE,
			Material.COBBLED_DEEPSLATE
		),
		mapOf(
			Ore.Titanium to 3
		)
	),
	Ilius(
		enumSetOf(
			Material.STONE,
			Material.DEEPSLATE
		),
		mapOf(
			Ore.Iron to 2
		)
	),
	Luxiterna(
		enumSetOf(
			Material.DIORITE,
			Material.CALCITE
		),
		mapOf(
			Ore.Uranium to 2,
			Ore.Chetherite to 2,
		)
	),
	Herdoli(
		enumSetOf(
			Material.STONE,
			Material.GRANITE,
			Material.ANDESITE,
			Material.DIORITE
		),
		mapOf(
			Ore.Copper to 3
		)
	),
	Rubaciea(
		enumSetOf(
			Material.NETHERRACK,
			Material.RED_TERRACOTTA,
			Material.RED_CONCRETE
		),
		mapOf(
			Ore.Redstone to 3,
			Ore.Quartz to 2
		)
	),
	Aret(
		enumSetOf(
			Material.STONE,
			Material.GRANITE,
			Material.ANDESITE,
			Material.DIORITE
		),
		mapOf(
			Ore.Iron to 3,
			Ore.Titanium to 2
		)
	),
	Aerach(
		enumSetOf(
			Material.STONE,
			Material.GRANITE,
			Material.ANDESITE,
			Material.DIORITE
		),
		mapOf(
			Ore.Coal to 2
		)
	),
	Vask(
		enumSetOf(
			Material.STONE,
			Material.POLISHED_GRANITE,
			Material.ORANGE_TERRACOTTA
		),
		mapOf(
			Ore.Copper to 2
		)
	),
	Gahara(
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
		mapOf(
			Ore.Gold to 3,
			Ore.Emerald to 2
		)
	),
	Isik(
		enumSetOf(
			Material.STONE,
			Material.GRANITE,
			Material.ANDESITE,
			Material.DIORITE
		),
		mapOf(
			Ore.Netherite to 1,
			Ore.Aluminium to 2
		)
	),
	Chimgara(
		enumSetOf(
			Material.STONE,
			Material.GRANITE,
			Material.ANDESITE,
			Material.DIORITE
		),
		mapOf(
			Ore.Redstone to 2
		)
	),
	Damkoth(
		enumSetOf(
			Material.DEEPSLATE
		),
		mapOf(
			Ore.Chetherite to 3
		)
	),
	Krio(
		enumSetOf(
			Material.STONE,
			Material.GRANITE,
			Material.ANDESITE,
			Material.DIORITE
		),
		mapOf(
			Ore.Diamond to 3,
			Ore.Lapis to 2
		)
	),
	Qatra(
		enumSetOf(
			Material.STONE,
			Material.GRANITE,
			Material.ANDESITE,
			Material.DIORITE
		),
		mapOf(
			Ore.Coal to 3,
			Ore.Gold to 2
		)
	),
	Kovfefe(
		enumSetOf(
			Material.STONE,
			Material.GRANITE,
			Material.ANDESITE,
			Material.DIORITE,
			Material.END_STONE
		),
		mapOf(
			Ore.Aluminium to 3
		)
	),
	Lioda(
		enumSetOf(
			Material.DIORITE,
			Material.CALCITE,
			Material.DEEPSLATE,
			Material.STONE
		),
		mapOf(
			Ore.Quartz to 2,
			Ore.Diamond to 2
		)
	),
	Turms(
		enumSetOf(
			Material.STONE,
			Material.GRANITE,
			Material.ANDESITE,
			Material.DIORITE
		),
		mapOf(
			Ore.Lapis to 3,
			Ore.Emerald to 2
		)
	)
}