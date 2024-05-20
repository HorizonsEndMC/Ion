package net.horizonsend.ion.server.miscellaneous.registrations

import net.horizonsend.ion.server.features.ores.OldOreData
import net.horizonsend.ion.server.miscellaneous.utils.enumSetOf
import org.bukkit.Material
import java.util.EnumSet

/*
TODO: This should be loaded from a configuration file.
*/

const val algorithmVersion = 11

@Suppress("unused")
enum class OrePlacementConfig(
	val groundMaterial: EnumSet<Material> = enumSetOf(),
	val options: Map<OldOreData, Int> = mapOf(),
	val configVersion: Int = 0
) {
	Chandra(
		enumSetOf(
			Material.STONE,
			Material.TUFF,
			Material.BLACKSTONE,
			Material.COBBLED_DEEPSLATE
		),
		mapOf(
			OldOreData.Titanium to 3,
			OldOreData.Aluminium to 2
		)
	),
	Ilius(
		enumSetOf(
			Material.STONE,
			Material.DEEPSLATE
		),
		mapOf(
			OldOreData.Iron to 2
		)
	),
	Luxiterna(
		enumSetOf(
			Material.DIORITE,
			Material.CALCITE
		),
		mapOf(
			OldOreData.Uranium to 2,
			OldOreData.Chetherite to 2
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
			OldOreData.Copper to 3
		)
	),
	Rubaciea(
		enumSetOf(
			Material.NETHERRACK,
			Material.RED_TERRACOTTA,
			Material.RED_CONCRETE
		),
		mapOf(
			OldOreData.Redstone to 3,
			OldOreData.Quartz to 2
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
			OldOreData.Iron to 3,
			OldOreData.Titanium to 2
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
			OldOreData.Chetherite to 2,
			OldOreData.Coal to 2
		)
	),
	Vask(
		enumSetOf(
			Material.STONE,
			Material.POLISHED_GRANITE,
			Material.ORANGE_TERRACOTTA
		),
		mapOf(
			OldOreData.Copper to 2,
			OldOreData.Redstone to 2
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
			OldOreData.Gold to 3,
			OldOreData.Emerald to 2
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
			OldOreData.Netherite to 2,
			OldOreData.Gold to 2
		),
		configVersion = 1
	),
	Chimgara(
		enumSetOf(
			Material.STONE,
			Material.GRANITE,
			Material.ANDESITE,
			Material.DIORITE
		),
		mapOf(
			OldOreData.Redstone to 2
		)
	),
	Damkoth(
		enumSetOf(
			Material.DEEPSLATE
		),
		mapOf(
			OldOreData.Chetherite to 3
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
			OldOreData.Diamond to 3,
			OldOreData.Lapis to 2
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
			OldOreData.Uranium to 2,
			OldOreData.Gold to 2
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
			OldOreData.Aluminium to 3
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
			OldOreData.Quartz to 2,
			OldOreData.Diamond to 2
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
			OldOreData.Lapis to 3,
			OldOreData.Emerald to 2
		)
	),
	Ilius_horizonsend_eden(
		enumSetOf(
			Material.STONE,
			Material.DEEPSLATE,
			Material.POLISHED_DEEPSLATE,
			Material.COBBLED_DEEPSLATE
		),
		mapOf(
			OldOreData.Iron to 2,
			OldOreData.Chetherite to 2
		)
	);

	val currentOreVersion: Int get() = algorithmVersion + configVersion
}
