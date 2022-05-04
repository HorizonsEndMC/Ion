package net.horizonsend.ion.ores

@Suppress("unused")
internal enum class OrePlacementConfig(
	internal val options: Map<Ore, Int>
) {
	Chandra(
		mapOf(
			Ore.Titanium to 3
		)
	),
	Ilius(
		mapOf(
			Ore.Iron to 2
		)
	),
	Luxiterna(
		mapOf(
			Ore.Uranium to 1,
			Ore.Chetherite to 2,
		)
	),
	Herdoli(
		mapOf(
			Ore.Copper to 3
		)
	),
	Rubaciea(
		mapOf(
			Ore.Redstone to 3,
			Ore.Quartz to 2
		)
	),
	Aret(
		mapOf(
			Ore.Iron to 3,
			Ore.Titanium to 2
		)
	),
	Aerach(
		mapOf(
			Ore.Coal to 2
		)
	),
	Vask(
		mapOf(
			Ore.Copper to 2
		)
	),
	Gahara(
		mapOf(
			Ore.Gold to 3,
			Ore.Emerald to 2
		)
	),
	Isik(
		mapOf(
			Ore.Netherite to 1,
			Ore.Aluminium to 2
		)
	),
	Chimgara(
		mapOf(
			Ore.Redstone to 2
		)
	),
	Damkoth(
		mapOf(
			Ore.Chetherite to 3
		)
	),
	Krio(
		mapOf(
			Ore.Diamond to 3,
			Ore.Lapis to 2
		)
	),
	Qatra(
		mapOf(
			Ore.Coal to 3,
			Ore.Gold to 2
		)
	),
	Kovfefe(
		mapOf(
			Ore.Aluminium to 3
		)
	),
	Iioda(
		mapOf(
			Ore.Quartz to 2,
			Ore.Diamond to 2
		)
	),
	Turms(
		mapOf(
			Ore.Lapis to 3,
			Ore.Emerald to 2
		)
	);
}