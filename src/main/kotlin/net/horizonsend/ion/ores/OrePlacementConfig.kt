package net.horizonsend.ion.ores

import java.util.EnumSet
import net.horizonsend.ion.ores.Ore.Aluminium
import net.horizonsend.ion.ores.Ore.Chetherite
import net.horizonsend.ion.ores.Ore.Coal
import net.horizonsend.ion.ores.Ore.Copper
import net.horizonsend.ion.ores.Ore.Diamond
import net.horizonsend.ion.ores.Ore.Emerald
import net.horizonsend.ion.ores.Ore.Gold
import net.horizonsend.ion.ores.Ore.Iron
import net.horizonsend.ion.ores.Ore.Lapis
import net.horizonsend.ion.ores.Ore.Netherite
import net.horizonsend.ion.ores.Ore.Redstone
import net.horizonsend.ion.ores.Ore.Titanium
import net.horizonsend.ion.ores.Ore.Uranium
import org.bukkit.Material
import org.bukkit.Material.ANDESITE
import org.bukkit.Material.BLACKSTONE
import org.bukkit.Material.CALCITE
import org.bukkit.Material.COBBLED_DEEPSLATE
import org.bukkit.Material.DEEPSLATE
import org.bukkit.Material.DIORITE
import org.bukkit.Material.GRANITE
import org.bukkit.Material.GRAVEL
import org.bukkit.Material.LIGHT_GRAY_TERRACOTTA
import org.bukkit.Material.NETHERRACK
import org.bukkit.Material.ORANGE_TERRACOTTA
import org.bukkit.Material.PACKED_ICE
import org.bukkit.Material.POLISHED_GRANITE
import org.bukkit.Material.PRISMARINE
import org.bukkit.Material.RED_CONCRETE
import org.bukkit.Material.RED_TERRACOTTA
import org.bukkit.Material.STONE
import org.bukkit.Material.TUFF

@Suppress("unused")
internal enum class OrePlacementConfig(internal val groundMaterial: EnumSet<Material>, internal val options: Map<Ore, Double>) {
	Chandra(
		setOf(STONE, TUFF, BLACKSTONE, COBBLED_DEEPSLATE).toCollection(EnumSet.noneOf(Material::class.java)),
		mapOf(
			Titanium to 1.0,
			Iron to 0.3
		)
	),
	Ilius(
		setOf(STONE, DEEPSLATE).toCollection(EnumSet.noneOf(Material::class.java)),
		mapOf(
			Aluminium to 0.3,
			Coal to 0.3
		)
	),
	Damkoth(
		setOf(DEEPSLATE).toCollection(EnumSet.noneOf(Material::class.java)),
		mapOf(
			Chetherite to 1.0,
			Emerald to 0.3
		)
	),
	Herdoli(
		setOf(STONE, GRANITE, ANDESITE, DIORITE).toCollection(EnumSet.noneOf(Material::class.java)),
		mapOf(
			Copper to 1.0,
			Iron to 0.3
		)
	),
	Rubaciea(
		setOf(NETHERRACK, RED_TERRACOTTA, RED_CONCRETE).toCollection(EnumSet.noneOf(Material::class.java)),
		mapOf(
			Redstone to 1.0,
			Lapis to 0.3
		)
	),
	Isik(
		setOf(STONE, GRANITE, ANDESITE, DIORITE).toCollection(EnumSet.noneOf(Material::class.java)),
		mapOf(
			Netherite to 0.3,
			Emerald to 0.3
		)
	),
	Chimgara(
		setOf(STONE, GRANITE, ANDESITE, DIORITE).toCollection(EnumSet.noneOf(Material::class.java)),
		mapOf(
			Redstone to 0.3,
			Lapis to 0.3
		)
	),
	Vask(
		setOf(STONE, POLISHED_GRANITE, ORANGE_TERRACOTTA).toCollection(EnumSet.noneOf(Material::class.java)),
		mapOf(
			Copper to 0.3,
			Coal to 0.3
		)
	),
	Krio(
		setOf(STONE, GRANITE, ANDESITE, DIORITE).toCollection(EnumSet.noneOf(Material::class.java)),
		mapOf(
			Diamond to 1.0,
			Gold to 0.3
		)
	),
	Aret(
		setOf(STONE, GRANITE, ANDESITE, DIORITE).toCollection(EnumSet.noneOf(Material::class.java)),
		mapOf(
			Iron to 1.0,
			Diamond to 0.3
		)
	),
	Aerach(
		setOf(STONE, GRANITE, ANDESITE, DIORITE).toCollection(EnumSet.noneOf(Material::class.java)),
		mapOf(
			Aluminium to 0.3,
			Coal to 0.3
		)
	),
	Luxiterna(
		setOf(DIORITE, CALCITE).toCollection(EnumSet.noneOf(Material::class.java)),
		mapOf(
			Uranium to 0.6,
			Chetherite to 0.3,
		)
	),
	Gahara(
		setOf(STONE, GRANITE, ANDESITE, DIORITE, LIGHT_GRAY_TERRACOTTA, GRAVEL, PRISMARINE, PACKED_ICE).toCollection(EnumSet.noneOf(Material::class.java)),
		mapOf(
			Gold to 1.0,
			Titanium to 0.3
		)
	);
}