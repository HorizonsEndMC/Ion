package net.horizonsend.ion.server.configuration

import com.sk89q.worldedit.extent.clipboard.Clipboard
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.horizonsend.ion.common.database.StarshipTypeDB
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ServerConfiguration.AsteroidConfig.Palette
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.WeightedRandomList
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.horizonsend.ion.server.miscellaneous.utils.readSchematic
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.util.Vector

@Serializable
data class ServerConfiguration(
	val serverName: String? = null,
	val crossServerDeathMessages: Boolean = false,
	val particleColourChoosingMoneyRequirement: Double? = 5.0,
	val beacons: List<HyperspaceBeacon> = listOf(),
	val spaceGenConfig: Map<String, AsteroidConfig> = mapOf(),
	val soldShips: List<Ship> = listOf(),
	val mobSpawns: Map<String, PlanetSpawnConfig> = mapOf(),
	val dutyModeMonitorWebhook: String? = null,
	val eventLoggerWebhook: String? = null,
) {
	/**
	 * @param baseAsteroidDensity: Roughly a base level of the number of asteroids per chunk
	 * @param maxAsteroidSize: Maximum Size for an Asteroid
	 * @param blockPalettes: list of Palettes use for the asteroid materials
	 * @param features List of AsteroidFeature
	 * @see Palette
	 */
	@Serializable
	data class AsteroidConfig(
		val baseAsteroidDensity: Double = 0.25,
		val maxAsteroidSize: Double = 14.0,
		val blockPalettes: ArrayList<Palette>,
		val features: List<AsteroidFeature>,
		val wreckClasses: ArrayList<WreckClass>,
		val wreckMultiplier: Double = 0.01
	) {
		/**
		 * @param weight: Number of rolls for this Palette
		 * @param materials: Map of Materials to their Weight
		 * @param ores:  list of Palettes used for ore placement
		 * @param oreRatio: Number of attempts to place an ore blob per chunk
		 *
		 * Each Palette is a set of materials, and their weights that might make up an asteroid. Asteroids may pick from a list of Palettes.
		 */
		@Serializable
		data class Palette(
			val weight: Int,
			val materials: List<PaletteEntry>,
			val ores: Set<Ore>,
			val oreRatio: Double = 0.25
		) {
			@Serializable
			data class PaletteEntry(
				val material: String,
				val weight: Int
			)

			fun getMaterial(material: String) = Bukkit.createBlockData(material)

			fun blockStateWeightedList(): WeightedRandomList<BlockState> {
				val list = WeightedRandomList<BlockState>()
				list.addMany(materials.map { getMaterial(it.material).nms to it.weight })

				return list
			}
		}

		/**
		 * @param material: String representation of the blockstate
		 * @param rolls: Number of rolls for this Palette
		 *
		 * Each Palette is a set of materials, and their weights that might make up an asteroid. Asteroids may pick from a list of Palettes.
		 */
		@Serializable
		data class Ore(
			val material: String, // Serialized BlockData
			val rolls: Int
		) {
			@Transient
			val blockData = Bukkit.createBlockData(this.material)

			@Transient
			val blockState = blockData.nms
		}

		/**
		 * Asteroid Feature
		 *
		 * All asteroid features are stored as tauruses, but the values can be manipulated to make spheres or other shapes.
		 * @param name Unused, for readability.
		 * @param baseDensity Asteroid density inside the tube
		 * @param tubeSize Distance from the center of the tube to the center of the taurus
		 * @param tubeRadius Radius of the tube
		 * @param origin Origin position
		 * @see Pos
		 * @see baseAsteroidDensity
		 */
		@Serializable
		data class AsteroidFeature(
			val name: String = "",
			val baseDensity: Double = 1.0,
			val tubeSize: Double = 0.0,
			val tubeRadius: Double = 0.0,
			val origin: Pos
		)

		/**
		 * @param wrecks: List of Wrecks
		 * @param weight: Weight of the wreck class
		 **/
		@Serializable
		data class WreckClass(
			val className: String,
			val wrecks: List<Wreck>,
			val weight: Int
		) {
			/**
			 * The Wreck data class contains its schematic, and weight
			 * @param wreckSchematicName: Name of the wreck schematic
			 * @param weight: Number of rolls for this wreck
			 * @param encounters: Map of possible scenarios to information about them
			 **/
			@Serializable
			data class Wreck(
				val wreckSchematicName: String,
				val weight: Int,
				val encounters: Map<String, Int>
			) {
				@Transient
				val encounterWeightedRandomList = WeightedRandomList<String>().apply {
					this.addMany(this@Wreck.encounters)
				}
			}

			@Transient
			val weightedWrecks = WeightedRandomList<Wreck>().apply {
				this.addMany(
					wrecks.associateWith { it.weight }
				)
			}
		}

		@Transient
		val weightedWreckList = WeightedRandomList<WeightedRandomList<WreckClass.Wreck>>().apply {
			this.addMany(
				wreckClasses.associate { wreckClass -> wreckClass.weightedWrecks to wreckClass.weight }
			)
		}

		fun paletteWeightedList(): WeightedRandomList<Pair<Int, WeightedRandomList<BlockState>>> {
			val list = WeightedRandomList<Pair<Int, WeightedRandomList<BlockState>>>()
			val transformed = blockPalettes.associate {
				(blockPalettes.indexOf(it) to it.blockStateWeightedList()) to it.weight
			}.toMap()

			list.addMany(transformed)

			return list
		}
	}

	@Serializable
	data class HyperspaceBeacon(
		val name: String,
		val radius: Double,
		val spaceLocation: Pos,
		val destination: Pos,
		val destinationName: String? = null,
		val exits: ArrayList<Pos>? = null,
		val prompt: String? = null
	)

	@Serializable
	data class Pos(
		val world: String,
		val x: Int,
		val y: Int,
		val z: Int
	) {
		fun bukkitWorld(): World = Bukkit.getWorld(world) ?: throw
		kotlin.NullPointerException("Could not find world $world")

		fun toVector(): Vector = Vector(x, y, z)

		fun toVec3i(): Vec3i = Vec3i(x, y, z)

		fun toLocation(): Location = Location(bukkitWorld(), x.toDouble(), y.toDouble(), z.toDouble())
	}

	/**
	 * @param cooldown in ms
	 **/
	@Serializable
	data class Ship(
		val price: Double,
		val displayName: String,
		val schematicName: String,
		val guiMaterial: Material,
		val cooldown: Long,
		val protectionCanBypass: Boolean,
		private val shipClass: StarshipTypeDB,
		val lore: List<String>
	) {
		@Transient
		val shipType: StarshipType = shipClass.actualType

		@Transient
		val schematicFile = IonServer.dataFolder.resolve("sold_ships").resolve("$schematicName.schem")

		fun schematic(): Clipboard = readSchematic(schematicFile)!!
	}

	@Serializable
	data class PlanetSpawnConfig(
		val mobs: List<Mob>
	) {
		@Serializable
		data class Mob(
			val weight: Int,
			val type: String,
			val namePool: Map<String, Int> = mapOf(),
			val onHand: DroppedItem? = null,
			val offHand: DroppedItem? = null,
			val helmet: DroppedItem? = null,
			val chestPlate: DroppedItem? = null,
			val leggings: DroppedItem? = null,
			val boots: DroppedItem? = null,
		) {
			@Transient
			val nameList: WeightedRandomList<String> = WeightedRandomList(namePool)

			fun getEntityType(): EntityType = EntityType.valueOf(type)
		}

		/**
		 * Uses bazaar strings for now
		 * Not the end of the world, but could be improved upon
		 **/
		@Serializable
		data class DroppedItem(
			val itemString: String,
			val amount: Int = 1,
			val dropChance: Float,
		)

		fun weightedList(): WeightedRandomList<Mob> {
			return WeightedRandomList(*mobs.map { it to it.weight }.toTypedArray())
		}
	}

	val spaceGenerationDefault = mapOf(
		"Asteri" to AsteroidConfig(
			baseAsteroidDensity = 0.0145,
			maxAsteroidSize = 60.0,
			blockPalettes = arrayListOf(
				ancientDebrisAsteroid(3, 0.0115),
				ironCoal(3, 0.0115),
				deepslateLapisCopper(3, 0.0115),
				netherQuartz(3, 0.0115),
				coal(3, 0.0115),
				copper(3, 0.0115),
				iron(3, 0.0115),
				emerald(3, 0.0115),
				uranium(3, 0.0115),
				gold(3, 0.0115),
				redstone(3, 0.0115),
				diamond(3, 0.0115),
				aluminumIron(3, 0.0115),
				titanium(3, 0.0115),
				lapis(3, 0.0115),
				cheth(3, 0.0115),
			),
			features = listOf(
				AsteroidConfig.AsteroidFeature(
					name = "Belt1",
					baseDensity = 0.0235,
					tubeSize = 15500.0,
					tubeRadius = 2000.0,
					origin = Pos(
						world = "Asteri",
						x = 30000,
						y = 100,
						z = 30000
					)
				),
				AsteroidConfig.AsteroidFeature(
					name = "Belt2",
					baseDensity = 0.0275,
					tubeSize = 6500.0,
					tubeRadius = 1500.0,
					origin = Pos(
						world = "Asteri",
						x = 30000,
						y = 100,
						z = 30000
					)
				)
			),
			wreckClasses = arrayListOf(),
			wreckMultiplier = 0.05,
		),
		"Regulus" to AsteroidConfig(
			baseAsteroidDensity = 0.0145,
			maxAsteroidSize = 60.0,
			blockPalettes = arrayListOf(
				ancientDebrisAsteroid(3, 0.0115),
				copperGold(3, 0.0115),
				netherQuartz(3, 0.0115),
				coal(3, 0.0115),
				copper(3, 0.0115),
				iron(3, 0.0115),
				emerald(3, 0.0115),
				uranium(3, 0.0115),
				gold(3, 0.0115),
				redstone(3, 0.0115),
				diamond(3, 0.0115),
				aluminum(3, 0.0115),
				titanium(3, 0.0115),
				lapis(3, 0.0115),
				cheth(3, 0.0115),
			),
			features = listOf(
				AsteroidConfig.AsteroidFeature(
					name = "Belt1",
					baseDensity = 0.0235,
					tubeSize = 23000.0,
					tubeRadius = 1500.0,
					origin = Pos(
						world = "Regulus",
						x = 30000,
						y = 100,
						z = 30000
					)
				),
				AsteroidConfig.AsteroidFeature(
					name = "Belt2",
					baseDensity = 0.0275,
					tubeSize = 6500.0,
					tubeRadius = 1250.0,
					origin = Pos(
						world = "Regulus",
						x = 30000,
						y = 100,
						z = 30000
					)
				)
			),
			wreckClasses = arrayListOf(),
			wreckMultiplier = 0.05,
		),
		"Sirius" to AsteroidConfig(
			baseAsteroidDensity = 0.0145,
			maxAsteroidSize = 60.0,
			blockPalettes = arrayListOf(
				ancientDebrisAsteroid(3, 0.0115),
				ironGoldRedstone(3, 0.0115),
				netherQuartz(3, 0.0115),
				coal(3, 0.0115),
				copper(3, 0.0115),
				iron(3, 0.0115),
				emerald(3, 0.0115),
				uranium(3, 0.0115),
				gold(3, 0.0115),
				redstone(3, 0.0115),
				diamond(3, 0.0115),
				aluminum(3, 0.0115),
				titanium(3, 0.0115),
				lapis(3, 0.0115),
				cheth(3, 0.0115),
			),
			features = listOf(
				AsteroidConfig.AsteroidFeature(
					name = "Belt1",
					baseDensity = 0.0235,
					tubeSize = 15500.0,
					tubeRadius = 2000.0,
					origin = Pos(
						world = "Sirius",
						x = 30000,
						y = 100,
						z = 30000
					)
				),
				AsteroidConfig.AsteroidFeature(
					name = "Belt2",
					baseDensity = 0.0275,
					tubeSize = 7000.0,
					tubeRadius = 1000.0,
					origin = Pos(
						world = "Sirius",
						x = 30000,
						y = 100,
						z = 30000
					)
				)
			),
			wreckClasses = arrayListOf(),
			wreckMultiplier = 0.05,
		),
		"Ilios" to AsteroidConfig(
			baseAsteroidDensity = 0.0145,
			maxAsteroidSize = 60.0,
			blockPalettes = arrayListOf(
				ancientDebrisAsteroid(3, 0.0115),
				ilios1(3, 0.0115),
				ilios2(3, 0.0115),
				netherQuartz(3, 0.0115),
				coal(3, 0.0115),
				copper(3, 0.0115),
				iron(3, 0.0115),
				emerald(3, 0.0115),
				uranium(3, 0.0115),
				gold(3, 0.0115),
				redstone(3, 0.0115),
				diamond(3, 0.0115),
				aluminum(3, 0.0115),
				titanium(3, 0.0115),
				lapis(3, 0.0115),
				cheth(3, 0.0115),
			),
			features = listOf(
				AsteroidConfig.AsteroidFeature(
					name = "Belt1",
					baseDensity = 0.0235,
					tubeSize = 5500.0,
					tubeRadius = 1500.0,
					origin = Pos(
						world = "Ilios",
						x = 30000,
						y = 100,
						z = 30000
					)
				),
				AsteroidConfig.AsteroidFeature(
					name = "Ilios",
					baseDensity = 0.0275,
					tubeSize = 22500.0,
					tubeRadius = 2000.0,
					origin = Pos(
						world = "Sirius",
						x = 30000,
						y = 100,
						z = 30000
					)
				)
			),
			wreckClasses = arrayListOf(),
			wreckMultiplier = 0.05,
		),
		"Trench" to AsteroidConfig(
			baseAsteroidDensity = 0.02,
			maxAsteroidSize = 75.0,
			blockPalettes = arrayListOf(
				ancientDebrisAsteroid(3, 0.014),
				netherQuartz(3, 0.014),
				coal(3, 0.014),
				copper(3, 0.014),
				iron(3, 0.014),
				emerald(3, 0.014),
				uranium(3, 0.014),
				gold(3, 0.014),
				redstone(3, 0.014),
				diamond(3, 0.014),
				aluminum(3, 0.014),
				titanium(3, 0.014),
				lapis(3, 0.014),
				cheth(3, 0.014),
			),
			features = listOf(
				AsteroidConfig.AsteroidFeature(
					name = "Belt1",
					baseDensity = 0.07325,
					tubeSize = 6500.0,
					tubeRadius = 3500.0,
					origin = Pos(
						world = "Trench",
						x = 30000,
						y = 100,
						z = 30000
					),
				),
				AsteroidConfig.AsteroidFeature(
					name = "Belt2",
					baseDensity = 0.07325,
					tubeSize = 15500.0,
					tubeRadius = 2000.0,
					origin = Pos(
						world = "Trench",
						x = 30000,
						y = 100,
						z = 30000
					),
				),
				AsteroidConfig.AsteroidFeature(
					name = "Belt3",
					baseDensity = 0.07325,
					tubeSize = 20500.0,
					tubeRadius = 1000.0,
					origin = Pos(
						world = "Trench",
						x = 30000,
						y = 100,
						z = 30000
					),
				),
				AsteroidConfig.AsteroidFeature(
					name = "Belt4",
					baseDensity = 0.07325,
					tubeSize = 23000.0,
					tubeRadius = 500.0,
					origin = Pos(
						world = "Trench",
						x = 30000,
						y = 100,
						z = 30000
					),
				),
				AsteroidConfig.AsteroidFeature(
					name = "Belt5",
					baseDensity = 0.07325,
					tubeSize = 25500.0,
					tubeRadius = 1000.0,
					origin = Pos(
						world = "Trench",
						x = 30000,
						y = 100,
						z = 30000
					),
				),
			),
			wreckClasses = arrayListOf(),
			wreckMultiplier = 0.05,
		),
		"AU-0821" to AsteroidConfig(
			baseAsteroidDensity = 0.0145,
			maxAsteroidSize = 60.0,
			blockPalettes = arrayListOf(
				au(3),
			),
			features = listOf(
				AsteroidConfig.AsteroidFeature(
					name = "Belt1",
					baseDensity = 0.0235,
					tubeSize = 5500.0,
					tubeRadius = 1500.0,
					origin = Pos(
						world = "Ilios",
						x = 30000,
						y = 100,
						z = 30000
					)
				),
				AsteroidConfig.AsteroidFeature(
					name = "Ilios",
					baseDensity = 0.0275,
					tubeSize = 22500.0,
					tubeRadius = 2000.0,
					origin = Pos(
						world = "Sirius",
						x = 30000,
						y = 100,
						z = 30000
					)
				)
			),
			wreckClasses = arrayListOf(),
			wreckMultiplier = 0.05,
		),
	)

	fun ancientDebrisAsteroid(weight: Int, oreRatio: Double): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:dead_tube_coral_block", 6),
			Palette.PaletteEntry("minecraft:andesite", 6),
			Palette.PaletteEntry("minecraft:basalt", 4),
			Palette.PaletteEntry("minecraft:smooth_basalt", 3),
			Palette.PaletteEntry("minecraft:polished_blackstone", 8),
			Palette.PaletteEntry("minecraft:nether_bricks", 10),
			Palette.PaletteEntry("minecraft:red_nether_bricks", 2),
			Palette.PaletteEntry("minecraft:netherrack", 3),
			Palette.PaletteEntry("minecraft:red_glazed_terracotta", 3),
			Palette.PaletteEntry("minecraft:netherrack", 3),
			Palette.PaletteEntry("minecraft:red_nether_bricks", 2),
			Palette.PaletteEntry("minecraft:nether_bricks", 3),
			Palette.PaletteEntry("minecraft:polished_blackstone", 8),
			Palette.PaletteEntry("minecraft:smooth_basalt", 3),
			Palette.PaletteEntry("minecraft:basalt", 4),
			Palette.PaletteEntry("minecraft:andesite", 6),
			Palette.PaletteEntry("minecraft:dead_tube_coral_block", 6)
		),
		ores = setOf(AsteroidConfig.Ore("minecraft:ancient_debris", 4)),
		oreRatio = oreRatio
	)

	fun ironCoal(weight: Int, oreRatio: Double): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:white_glazed_terracotta", 4),
			Palette.PaletteEntry("minecraft:white_concrete", 1),
			Palette.PaletteEntry("minecraft:diorite", 4),
			Palette.PaletteEntry("minecraft:polished_diorite", 3),
			Palette.PaletteEntry("minecraft:calcite", 4),
			Palette.PaletteEntry("minecraft:quartz_block", 4),
			Palette.PaletteEntry("minecraft:smooth_quartz", 1)
		),
		ores = setOf(
			AsteroidConfig.Ore("minecraft:brown_mushroom_block[down=false,east=true,north=true,south=false,up=true,west=false]", 6),
			AsteroidConfig.Ore("minecraft:iron_ore", 4),
			AsteroidConfig.Ore("minecraft:coal_ore", 2)
		),
		oreRatio = oreRatio
	)

	fun deepslateLapisCopper(weight: Int, oreRatio: Double): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:purple_glazed_terracotta", 4),
			Palette.PaletteEntry("minecraft:purple_terracotta", 3),
			Palette.PaletteEntry("minecraft:polished_blackstone", 4),
			Palette.PaletteEntry("minecraft:blackstone", 3),
			Palette.PaletteEntry("minecraft:crying_obsidian", 4),
			Palette.PaletteEntry("minecraft:blackstone", 4),
			Palette.PaletteEntry("minecraft:polished_blackstone", 4),
			Palette.PaletteEntry("minecraft:deepslate_tiles", 4)
		),
		ores = setOf(
			AsteroidConfig.Ore("minecraft:brown_mushroom_block[down=false,east=true,north=true,south=false,up=true,west=false]", 2),
			AsteroidConfig.Ore("minecraft:deepslate_lapis_ore", 4),
			AsteroidConfig.Ore("minecraft:copper_ore", 6)
		),
		oreRatio = oreRatio
	)

	fun netherQuartz(weight: Int, oreRatio: Double): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:magma_block", 2),
			Palette.PaletteEntry("minecraft:granite", 3),
			Palette.PaletteEntry("minecraft:netherrack", 4),
			Palette.PaletteEntry("minecraft:polished_granite", 2),
			Palette.PaletteEntry("minecraft:red_terracotta", 2),
		),
		ores = setOf(AsteroidConfig.Ore("minecraft:nether_quartz_ore", 4)),
		oreRatio = oreRatio
	)

	fun coal(weight: Int, oreRatio: Double): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:blackstone", 5),
			Palette.PaletteEntry("minecraft:polished_blackstone", 8),
			Palette.PaletteEntry("minecraft:smooth_basalt", 4),
			Palette.PaletteEntry("minecraft:deepslate", 6),
			Palette.PaletteEntry("minecraft:tuff", 6),
			Palette.PaletteEntry("minecraft:red_sandstone", 4),
			Palette.PaletteEntry("minecraft:smooth_red_sandstone", 4),
		),
		ores = setOf(AsteroidConfig.Ore("minecraft:coal_ore", 4)),
		oreRatio = oreRatio
	)

	fun copper(weight: Int, oreRatio: Double): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:dirt", 3),
			Palette.PaletteEntry("minecraft:dripstone_block", 6),
			Palette.PaletteEntry("minecraft:granite", 3),
			Palette.PaletteEntry("minecraft:packed_ice", 2),
			Palette.PaletteEntry("minecraft:blue_ice", 2),
			Palette.PaletteEntry("minecraft:packed_ice", 2),
			Palette.PaletteEntry("minecraft:packed_mud", 6),
			Palette.PaletteEntry("minecraft:polished_granite", 3)
		),
		ores = setOf(AsteroidConfig.Ore( "minecraft:copper_ore", 4)),
		oreRatio = oreRatio
	)

	fun iron(weight: Int, oreRatio: Double): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:granite", 8),
			Palette.PaletteEntry("minecraft:polished_granite", 6),
			Palette.PaletteEntry("minecraft:packed_mud", 7),
			Palette.PaletteEntry("minecraft:dripstone_block", 6),
			Palette.PaletteEntry("minecraft:deepslate", 4),
			Palette.PaletteEntry("minecraft:polished_basalt", 4),
			Palette.PaletteEntry("minecraft:stone", 4),
			Palette.PaletteEntry("minecraft:polished_andesite", 4),
			Palette.PaletteEntry("minecraft:andesite", 4),
			Palette.PaletteEntry("minecraft:cobblestone", 4),
			Palette.PaletteEntry("minecraft:clay", 6)
		),
		ores = setOf(AsteroidConfig.Ore( "minecraft:iron_ore", 4)),
		oreRatio = oreRatio
	)

	fun emerald(weight: Int, oreRatio: Double): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:verdant_froglight[axis=y]", 4),
			Palette.PaletteEntry("minecraft:lime_terracotta", 3),
			Palette.PaletteEntry("minecraft:dark_prismarine", 3),
			Palette.PaletteEntry("minecraft:purple_terracotta", 3),
			Palette.PaletteEntry("minecraft:magenta_terracotta", 2),
			Palette.PaletteEntry("minecraft:purple_terracotta", 2),
			Palette.PaletteEntry("minecraft:cobbled_deepslate", 6),
			Palette.PaletteEntry("minecraft:smooth_basalt", 4),
			Palette.PaletteEntry("minecraft:deepslate", 4),
			Palette.PaletteEntry("minecraft:tuff", 3),
			Palette.PaletteEntry("minecraft:andesite", 4),
			Palette.PaletteEntry("minecraft:polished_diorite", 4),
			Palette.PaletteEntry("minecraft:diorite", 3),
			Palette.PaletteEntry("minecraft:calcite", 4)
		),
		ores = setOf(AsteroidConfig.Ore( "minecraft:deepslate_emerald_ore", 7)),
		oreRatio = oreRatio
	)

	fun uranium(weight: Int, oreRatio: Double): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:verdant_froglight[axis=y]", 2),
			Palette.PaletteEntry("minecraft:slime_block", 2),
			Palette.PaletteEntry("minecraft:lime_terracotta", 1),
			Palette.PaletteEntry("minecraft:tuff", 6),
			Palette.PaletteEntry("minecraft:end_stone", 3),
			Palette.PaletteEntry("minecraft:end_stone_bricks", 1),
			Palette.PaletteEntry("minecraft:sandstone", 3),
			Palette.PaletteEntry("minecraft:smooth_sandstone", 4)
		),
		ores = setOf(AsteroidConfig.Ore( "minecraft:brown_mushroom_block[down=false,east=false,north=false,south=false,up=true,west=false]", 7)),
		oreRatio = oreRatio
	)

	fun gold(weight: Int, oreRatio: Double): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:sandstone", 2),
			Palette.PaletteEntry("minecraft:smooth_sandstone", 3),
			Palette.PaletteEntry("minecraft:sandstone", 4),
			Palette.PaletteEntry("minecraft:ochre_froglight", 3),
			Palette.PaletteEntry("minecraft:sandstone", 4),
			Palette.PaletteEntry("minecraft:diorite", 3),
			Palette.PaletteEntry("minecraft:polished_diorite", 3),
			Palette.PaletteEntry("minecraft:calcite", 2),
			Palette.PaletteEntry("minecraft:polished_diorite", 3),
			Palette.PaletteEntry("minecraft:diorite", 3),
			Palette.PaletteEntry("minecraft:clay", 4),
			Palette.PaletteEntry("minecraft:diorite", 3)
		),
		ores = setOf(AsteroidConfig.Ore( "minecraft:gold_ore", 2)),
		oreRatio = oreRatio
	)

	fun redstone(weight: Int, oreRatio: Double): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:red_nether_bricks", 8),
			Palette.PaletteEntry("minecraft:netherrack", 5),
			Palette.PaletteEntry("minecraft:red_glazed_terracotta", 1),
			Palette.PaletteEntry("minecraft:diorite", 2),
			Palette.PaletteEntry("minecraft:polished_diorite", 2),
			Palette.PaletteEntry("minecraft:calcite", 2),
			Palette.PaletteEntry("minecraft:quartz_bricks", 3),
			Palette.PaletteEntry("minecraft:quartz_block", 3),
			Palette.PaletteEntry("minecraft:smooth_quartz", 3)
		),
		ores = setOf(AsteroidConfig.Ore( "minecraft:redstone_ore", 2)),
		oreRatio = oreRatio
	)

	fun diamond(weight: Int, oreRatio: Double): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:blue_ice", 2),
			Palette.PaletteEntry("minecraft:packed_ice", 2),
			Palette.PaletteEntry("minecraft:ice", 1),
			Palette.PaletteEntry("minecraft:prismarine", 2),
			Palette.PaletteEntry("minecraft:prismarine_bricks", 2),
			Palette.PaletteEntry("minecraft:tuff", 4),
			Palette.PaletteEntry("minecraft:polished_diorite", 6),
			Palette.PaletteEntry("minecraft:calcite", 6),
			Palette.PaletteEntry("minecraft:quartz_block", 6),
			Palette.PaletteEntry("minecraft:smooth_quartz", 6),
		),
		ores = setOf(AsteroidConfig.Ore( "minecraft:diamond_ore", 2)),
		oreRatio = oreRatio
	)

	fun aluminumIron(weight: Int, oreRatio: Double): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:andesite", 8),
			Palette.PaletteEntry("minecraft:diorite", 2),
			Palette.PaletteEntry("minecraft:calcite", 2),
			Palette.PaletteEntry("minecraft:snow_block", 3),
			Palette.PaletteEntry("minecraft:calcite", 1),
			Palette.PaletteEntry("minecraft:diorite", 2),
			Palette.PaletteEntry("minecraft:andesite", 5),
			Palette.PaletteEntry("minecraft:tuff", 12),
			Palette.PaletteEntry("minecraft:polished_basalt", 3)
		),
		ores = setOf(
			AsteroidConfig.Ore( "minecraft:brown_mushroom_block[down=false,east=false,north=true,south=false,up=true,west=false]", 4),
			AsteroidConfig.Ore( "minecraft:iron_ore", 5)
		),
		oreRatio = oreRatio
	)

	fun aluminum(weight: Int, oreRatio: Double): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:andesite", 8),
			Palette.PaletteEntry("minecraft:diorite", 2),
			Palette.PaletteEntry("minecraft:calcite", 2),
			Palette.PaletteEntry("minecraft:snow_block", 3),
			Palette.PaletteEntry("minecraft:calcite", 1),
			Palette.PaletteEntry("minecraft:diorite", 2),
			Palette.PaletteEntry("minecraft:andesite", 5),
			Palette.PaletteEntry("minecraft:tuff", 12),
			Palette.PaletteEntry("minecraft:polished_basalt", 3)
		),
		ores = setOf(AsteroidConfig.Ore( "minecraft:brown_mushroom_block[down=false,east=false,north=true,south=false,up=true,west=false]", 4)),
		oreRatio = oreRatio
	)

	fun titanium(weight: Int, oreRatio: Double): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:sea_lantern", 1),
			Palette.PaletteEntry("minecraft:andesite", 5),
			Palette.PaletteEntry("minecraft:cyan_terracotta", 3),
			Palette.PaletteEntry("minecraft:packed_ice", 2),
			Palette.PaletteEntry("minecraft:stone", 8),
			Palette.PaletteEntry("minecraft:cyan_terracotta", 3),
			Palette.PaletteEntry("minecraft:packed_ice", 2)
		),
		ores = setOf(AsteroidConfig.Ore( "minecraft:brown_mushroom_block[down=false,east=false,north=false,south=false,up=true,west=true]", 4)),
		oreRatio = oreRatio
	)

	fun lapis(weight: Int, oreRatio: Double): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:crying_obsidian", 3),
			Palette.PaletteEntry("minecraft:blackstone", 2),
			Palette.PaletteEntry("minecraft:polished_blackstone", 1),
			Palette.PaletteEntry("minecraft:smooth_basalt", 6),
			Palette.PaletteEntry("minecraft:cyan_terracotta", 4),
			Palette.PaletteEntry("minecraft:mud", 4)
		),
		ores = setOf(AsteroidConfig.Ore( "minecraft:deepslate_lapis_ore", 4)),
		oreRatio = oreRatio
	)

	fun cheth(weight: Int, oreRatio: Double): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:pearlescent_froglight[axis=z]", 3),
			Palette.PaletteEntry("minecraft:purpur_block", 4),
			Palette.PaletteEntry("minecraft:amethyst_block", 3),
			Palette.PaletteEntry("minecraft:budding_amethyst", 1),
			Palette.PaletteEntry("minecraft:amethyst_block", 3),
			Palette.PaletteEntry("minecraft:blue_terracotta", 4),
			Palette.PaletteEntry("minecraft:smooth_basalt", 4),
			Palette.PaletteEntry("minecraft:tuff", 1),
			Palette.PaletteEntry("minecraft:andesite", 1),
			Palette.PaletteEntry("minecraft:tuff", 1),
			Palette.PaletteEntry("minecraft:deepslate", 1),
			Palette.PaletteEntry("minecraft:cobbled_deepslate", 1)
		),
		ores = setOf(AsteroidConfig.Ore( "minecraft:brown_mushroom_block[down=false,east=true,north=true,south=false,up=true,west=false]", 4)),
		oreRatio = oreRatio
	)

	fun copperGold(weight: Int, oreRatio: Double): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:green_glazed_terracotta", 4),
			Palette.PaletteEntry("minecraft:lime_terracotta", 3),
			Palette.PaletteEntry("minecraft:mossy_cobblestone", 4),
			Palette.PaletteEntry("minecraft:tuff", 3),
			Palette.PaletteEntry("minecraft:polished_basalt", 4),
			Palette.PaletteEntry("minecraft:cyan_terracotta", 4),
			Palette.PaletteEntry("minecraft:tuff", 4),
			Palette.PaletteEntry("minecraft:light_gray_terracotta", 4),
			Palette.PaletteEntry("minecraft:granite", 4),
			Palette.PaletteEntry("minecraft:polished_granite", 4)
		),
		ores = setOf(
			AsteroidConfig.Ore( "minecraft:copper_ore", 4),
			AsteroidConfig.Ore( "minecraft:brown_mushroom_block[down=false,east=false,north=false,south=false,up=true,west=false]", 2),
			AsteroidConfig.Ore( "minecraft:gold_ore", 6),
		),
		oreRatio = oreRatio
	)

	fun ironGoldRedstone(weight: Int, oreRatio: Double): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:orange_terracotta", 4),
			Palette.PaletteEntry("minecraft:smooth_red_sandstone", 4),
			Palette.PaletteEntry("minecraft:red_sandstone", 3),
			Palette.PaletteEntry("minecraft:packed_mud", 4),
			Palette.PaletteEntry("minecraft:light_gray_terracotta", 4),
			Palette.PaletteEntry("minecraft:dripstone_block", 4),
			Palette.PaletteEntry("minecraft:granite", 4),
			Palette.PaletteEntry("minecraft:polished_granite", 4)
		),
		ores = setOf(
			AsteroidConfig.Ore( "minecraft:iron_ore", 6),
			AsteroidConfig.Ore( "minecraft:gold_ore", 4),
			AsteroidConfig.Ore( "minecraft:redstone_ore", 2),
		),
		oreRatio = oreRatio
	)

	fun ilios1(weight: Int, oreRatio: Double): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:light_blue_glazed_terracotta", 4),
			Palette.PaletteEntry("minecraft:ice", 3),
			Palette.PaletteEntry("minecraft:light_blue_terracotta", 4),
			Palette.PaletteEntry("minecraft:tuff", 4),
			Palette.PaletteEntry("minecraft:cobblestone", 4),
			Palette.PaletteEntry("minecraft:andesite", 4),
			Palette.PaletteEntry("minecraft:polished_andesite", 4),
			Palette.PaletteEntry("minecraft:smooth_basalt", 4),
			Palette.PaletteEntry("minecraft:basalt", 4),
			Palette.PaletteEntry("minecraft:polished_basalt", 4)
		),
		ores = setOf(
			AsteroidConfig.Ore( "minecraft:brown_mushroom_block[down=false,east=false,north=true,south=false,up=true,west=false]", 6),
			AsteroidConfig.Ore( "minecraft:brown_mushroom_block[down=false,east=false,north=false,south=false,up=true,west=true]", 4),
			AsteroidConfig.Ore( "minecraft:nether_quartz_ore", 2),
		),
		oreRatio = oreRatio
	)

	fun ilios2(weight: Int, oreRatio: Double): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:sea_lantern", 4),
			Palette.PaletteEntry("minecraft:prismarine", 3),
			Palette.PaletteEntry("minecraft:andesite", 4),
			Palette.PaletteEntry("minecraft:tuff", 4),
			Palette.PaletteEntry("minecraft:cyan_terracotta", 4),
			Palette.PaletteEntry("minecraft:smooth_basalt", 4),
			Palette.PaletteEntry("minecraft:cyan_terracotta", 4),
			Palette.PaletteEntry("minecraft:tuff", 4)
		),
		ores = setOf(
			AsteroidConfig.Ore( "minecraft:brown_mushroom_block[down=false,east=false,north=true,south=false,up=true,west=false]", 4),
			AsteroidConfig.Ore(  "minecraft:brown_mushroom_block[down=false,east=false,north=false,south=false,up=true,west=true]", 2),
			AsteroidConfig.Ore( "minecraft:nether_quartz_ore", 6),
		),
		oreRatio = oreRatio
	)

	fun au(weight: Int): Palette = Palette(
		weight = weight,
		materials = listOf(
			Palette.PaletteEntry("minecraft:purple_glazed_terracotta", 4),
			Palette.PaletteEntry("minecraft:purple_terracotta", 3),
			Palette.PaletteEntry("minecraft:polished_blackstone", 4),
			Palette.PaletteEntry("minecraft:blackstone", 3),
			Palette.PaletteEntry("minecraft:crying_obsidian", 4),
			Palette.PaletteEntry("minecraft:blackstone", 4),
			Palette.PaletteEntry("minecraft:polished_blackstone", 4),
			Palette.PaletteEntry("minecraft:deepslate_tiles", 4),
		),
		ores = setOf(),
		oreRatio = 0.0
	)
}
