package net.horizonsend.ion.server.features.world.configuration

import net.horizonsend.ion.server.configuration.util.StaticDoubleAmount
import net.horizonsend.ion.server.configuration.util.StaticIntegerAmount
import net.horizonsend.ion.server.configuration.util.WeightedIntegerAmount
import net.horizonsend.ion.server.core.registration.keys.AtmosphericGasKeys
import net.horizonsend.ion.server.core.registration.keys.WreckStructureKeys
import net.horizonsend.ion.server.features.gas.collection.ChildWeight
import net.horizonsend.ion.server.features.gas.collection.CollectedGas
import net.horizonsend.ion.server.features.gas.collection.HeightRamp
import net.horizonsend.ion.server.features.gas.collection.StaticBase
import net.horizonsend.ion.server.features.gas.type.WorldGasConfiguration
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.features.world.WorldSettings
import net.horizonsend.ion.server.features.world.environment.configuration.GravityModuleConfiguration
import net.horizonsend.ion.server.features.world.environment.configuration.NoGravityModuleConfiguration
import net.horizonsend.ion.server.features.world.environment.configuration.VacuumEnvironmentConfiguration
import net.horizonsend.ion.server.features.world.environment.configuration.WorldEnvironmentConfiguration
import net.horizonsend.ion.server.features.world.environment.modules.GravityModule.Companion.DEFAULT_GRAVITY
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.StaticConfigurationGlobal
import net.horizonsend.ion.server.features.world.generation.generators.configuration.feature.AsteroidPlacementConfiguration
import net.horizonsend.ion.server.features.world.generation.generators.configuration.feature.WreckPlacementConfiguration
import net.horizonsend.ion.server.features.world.generation.generators.configuration.generator.FeatureGeneratorConfiguration
import org.bukkit.entity.EntityType

@Suppress("UNUSED")
object DefaultWorldConfiguration {
	private val defaultConfigs = mutableMapOf<String, WorldSettings>()

	operator fun get(world: String): WorldSettings = defaultConfigs[world] ?: WorldSettings()

	private fun register(worldName: String, settings: WorldSettings): WorldSettings {
		defaultConfigs[worldName] = settings
		return settings
	}

	val TEST = register(
		"Space", WorldSettings(
			gasConfiguration = WorldGasConfiguration(
				gasses = listOf(
					CollectedGas(
						AtmosphericGasKeys.HYDROGEN, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(100),
								maxHeight = StaticIntegerAmount(384),
								minWeight = StaticDoubleAmount(0.0),
								maxWeight = StaticDoubleAmount(1.0)
							),
							weight = StaticDoubleAmount(0.5)
						)
					),
					CollectedGas(
						AtmosphericGasKeys.NITROGEN,
						ChildWeight(
							parent = ChildWeight(
								parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
							),
							weight = StaticDoubleAmount(0.5)
						),
					),
					CollectedGas(
						AtmosphericGasKeys.METHANE, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(0),
								maxHeight = StaticIntegerAmount(384),
								minWeight = StaticDoubleAmount(0.5),
								maxWeight = StaticDoubleAmount(1.0)
							),
							weight = StaticDoubleAmount(0.5)
						)
					),
					CollectedGas(
						AtmosphericGasKeys.OXYGEN,
						ChildWeight(
							parent = ChildWeight(
								parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
							),
							weight = StaticDoubleAmount(0.5)
						),
					),
					CollectedGas(
						AtmosphericGasKeys.CHLORINE, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(0),
								maxHeight = StaticIntegerAmount(384),
								minWeight = StaticDoubleAmount(1.0),
								maxWeight = StaticDoubleAmount(0.0)
							),
							weight = StaticDoubleAmount(0.5)
						)
					),
					CollectedGas(
						AtmosphericGasKeys.FLUORINE, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(0),
								maxHeight = StaticIntegerAmount(128),
								minWeight = StaticDoubleAmount(1.0),
								maxWeight = StaticDoubleAmount(0.0)
							),
							weight = StaticDoubleAmount(0.5)
						)
					)
				)
			),
			terrainGenerationSettings = FeatureGeneratorConfiguration(features = setOf(
				AsteroidPlacementConfiguration(),
				WreckPlacementConfiguration(
					0.08,
					listOf(
						WreckPlacementConfiguration.WeightedStructure(WreckStructureKeys.EMPTY, 1.0)
					)
				)
			))
		)
	)

	val CHANDRA = register(
		"Chandra", WorldSettings(
			gasConfiguration = WorldGasConfiguration(
				gasses = listOf(
					CollectedGas(
						AtmosphericGasKeys.HYDROGEN, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(100),
								maxHeight = StaticIntegerAmount(384),
								minWeight = StaticDoubleAmount(0.0),
								maxWeight = StaticDoubleAmount(1.0)
							),
							weight = StaticDoubleAmount(0.9)
						)
					),
				)
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					GravityModuleConfiguration(0.03),
					VacuumEnvironmentConfiguration
				)
			),
			flags = mutableSetOf(
				WorldFlag.PLANET_WORLD,
			)
		)
	)

	val ILIUS = register(
		"Ilius", WorldSettings(
			gasConfiguration = WorldGasConfiguration(
				gasses = listOf(
					CollectedGas(
						AtmosphericGasKeys.NITROGEN,
						ChildWeight(
							parent = ChildWeight(
								parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
							),
							weight = StaticDoubleAmount(0.5)
						),
					),
					CollectedGas(
						AtmosphericGasKeys.OXYGEN,
						ChildWeight(
							parent = ChildWeight(
								parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
							),
							weight = StaticDoubleAmount(0.2)
						),
					),
				)
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					GravityModuleConfiguration(DEFAULT_GRAVITY),
					VacuumEnvironmentConfiguration
				)
			),
			flags = mutableSetOf(
				WorldFlag.PLANET_WORLD,
			)
		)
	)

	val LUXITERNA = register(
		"Luxiterna", WorldSettings(
			gasConfiguration = WorldGasConfiguration(
				gasses = listOf(
					CollectedGas(
						AtmosphericGasKeys.HYDROGEN, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(100),
								maxHeight = StaticIntegerAmount(384),
								minWeight = StaticDoubleAmount(0.0),
								maxWeight = StaticDoubleAmount(1.0)
							),
							weight = StaticDoubleAmount(0.2)
						)
					),
					CollectedGas(
						AtmosphericGasKeys.OXYGEN,
						ChildWeight(
							parent = ChildWeight(
								parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
							),
							weight = StaticDoubleAmount(0.25)
						),
					),
					CollectedGas(
						AtmosphericGasKeys.FLUORINE, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(0),
								maxHeight = StaticIntegerAmount(128),
								minWeight = StaticDoubleAmount(1.0),
								maxWeight = StaticDoubleAmount(0.0)
							),
							weight = StaticDoubleAmount(0.25)
						)
					)
				)
			),
			flags = mutableSetOf(
				WorldFlag.PLANET_WORLD,
			)
		)
	)

	val HERDOLI = register(
		"Herdoli", WorldSettings(
			gasConfiguration = WorldGasConfiguration(
				gasses = listOf(
					CollectedGas(
						AtmosphericGasKeys.HYDROGEN, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(100),
								maxHeight = StaticIntegerAmount(384),
								minWeight = StaticDoubleAmount(0.0),
								maxWeight = StaticDoubleAmount(1.0)
							),
							weight = StaticDoubleAmount(0.2)
						)
					),
				)
			),
			flags = mutableSetOf(
				WorldFlag.PLANET_WORLD,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.3,
				moduleConfiguration = listOf(
					GravityModuleConfiguration(0.05),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val RUBACIEA = register(
		"Rubaciea", WorldSettings(
			gasConfiguration = WorldGasConfiguration(
				gasses = listOf(
					CollectedGas(
						AtmosphericGasKeys.HYDROGEN, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(100),
								maxHeight = StaticIntegerAmount(384),
								minWeight = StaticDoubleAmount(0.0),
								maxWeight = StaticDoubleAmount(1.0)
							),
							weight = StaticDoubleAmount(0.6)
						)
					),
					CollectedGas(
						AtmosphericGasKeys.NITROGEN,
						ChildWeight(
							parent = ChildWeight(
								parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
							),
							weight = StaticDoubleAmount(0.3)
						),
					),
					CollectedGas(
						AtmosphericGasKeys.OXYGEN,
						ChildWeight(
							parent = ChildWeight(
								parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
							),
							weight = StaticDoubleAmount(0.1)
						),
					),
				)
			),
			flags = mutableSetOf(
				WorldFlag.PLANET_WORLD,
			)
		)
	)

	val ISIK = register(
		"Isik", WorldSettings(
			gasConfiguration = WorldGasConfiguration(
				gasses = listOf(
					CollectedGas(
						AtmosphericGasKeys.METHANE, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(0),
								maxHeight = StaticIntegerAmount(384),
								minWeight = StaticDoubleAmount(0.5),
								maxWeight = StaticDoubleAmount(1.0)
							),
							weight = StaticDoubleAmount(0.25)
						)
					),
					CollectedGas(
						AtmosphericGasKeys.CHLORINE, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(0),
								maxHeight = StaticIntegerAmount(384),
								minWeight = StaticDoubleAmount(1.0),
								maxWeight = StaticDoubleAmount(0.0)
							),
							weight = StaticDoubleAmount(0.5)
						)
					),
				)
			),
			customMobSpawns = listOf(
				WorldSettings.SpawnedMob(
					function = WorldSettings.SpawnedMob.AlwaysReplace,
					spawningWeight = 1.0,
					type = EntityType.BLAZE.name
				)
			),
			flags = mutableSetOf(
				WorldFlag.PLANET_WORLD,
			)
		)
	)

	val CHIMGARA = register(
		"Chimgara", WorldSettings(
			gasConfiguration = WorldGasConfiguration(
				gasses = listOf(
					CollectedGas(
						AtmosphericGasKeys.NITROGEN,
						ChildWeight(
							parent = ChildWeight(
								parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
							),
							weight = StaticDoubleAmount(0.2)
						),
					),
					CollectedGas(
						AtmosphericGasKeys.OXYGEN,
						ChildWeight(
							parent = ChildWeight(
								parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
							),
							weight = StaticDoubleAmount(0.2)
						),
					),
					CollectedGas(
						AtmosphericGasKeys.CHLORINE, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(0),
								maxHeight = StaticIntegerAmount(384),
								minWeight = StaticDoubleAmount(1.0),
								maxWeight = StaticDoubleAmount(0.0)
							),
							weight = StaticDoubleAmount(0.3)
						)
					),
				)
			),
			flags = mutableSetOf(
				WorldFlag.PLANET_WORLD,
			)
		)
	)

	val DAMKOTH = register(
		"Damkoth", WorldSettings(
			gasConfiguration = WorldGasConfiguration(
				gasses = listOf(
					CollectedGas(
						AtmosphericGasKeys.METHANE, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(0),
								maxHeight = StaticIntegerAmount(384),
								minWeight = StaticDoubleAmount(0.5),
								maxWeight = StaticDoubleAmount(1.0)
							),
							weight = StaticDoubleAmount(0.2)
						)
					),
					CollectedGas(
						AtmosphericGasKeys.CHLORINE, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(0),
								maxHeight = StaticIntegerAmount(384),
								minWeight = StaticDoubleAmount(1.0),
								maxWeight = StaticDoubleAmount(0.0)
							),
							weight = StaticDoubleAmount(0.3)
						)
					),
				)
			),
			flags = mutableSetOf(
				WorldFlag.PLANET_WORLD,
			)
		)
	)

	val KRIO = register(
		"Krio", WorldSettings(
			gasConfiguration = WorldGasConfiguration(
				gasses = listOf(
					CollectedGas(
						AtmosphericGasKeys.NITROGEN,
						ChildWeight(
							parent = ChildWeight(
								parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
							),
							weight = StaticDoubleAmount(0.333)
						),
					),
					CollectedGas(
						AtmosphericGasKeys.OXYGEN,
						ChildWeight(
							parent = ChildWeight(
								parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
							),
							weight = StaticDoubleAmount(0.333)
						),
					),
				)
			),
			flags = mutableSetOf(
				WorldFlag.PLANET_WORLD,
			)
		)
	)

	val ARET = register(
		"Aret", WorldSettings(
			gasConfiguration = WorldGasConfiguration(
				gasses = listOf(
					CollectedGas(
						AtmosphericGasKeys.NITROGEN,
						ChildWeight(
							parent = ChildWeight(
								parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
							),
							weight = StaticDoubleAmount(0.4)
						),
					),
					CollectedGas(
						AtmosphericGasKeys.OXYGEN,
						ChildWeight(
							parent = ChildWeight(
								parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
							),
							weight = StaticDoubleAmount(0.2)
						),
					),
				)
			),
			flags = mutableSetOf(
				WorldFlag.PLANET_WORLD,
			)
		)
	)

	val AERACH = register(
		"Aerach", WorldSettings(
			gasConfiguration = WorldGasConfiguration(
				gasses = listOf(
					CollectedGas(
						AtmosphericGasKeys.NITROGEN,
						ChildWeight(
							parent = ChildWeight(
								parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
							),
							weight = StaticDoubleAmount(0.5)
						),
					),
					CollectedGas(
						AtmosphericGasKeys.OXYGEN,
						ChildWeight(
							parent = ChildWeight(
								parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
							),
							weight = StaticDoubleAmount(0.3)
						),
					),
				)
			),
			flags = mutableSetOf(
				WorldFlag.PLANET_WORLD,
			)
		)
	)

	val VASK = register(
		"Vask", WorldSettings(
			gasConfiguration = WorldGasConfiguration(
				gasses = listOf(
					CollectedGas(
						AtmosphericGasKeys.NITROGEN,
						ChildWeight(
							parent = ChildWeight(
								parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
							),
							weight = StaticDoubleAmount(0.5)
						),
					),
					CollectedGas(
						AtmosphericGasKeys.OXYGEN,
						ChildWeight(
							parent = ChildWeight(
								parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
							),
							weight = StaticDoubleAmount(0.5)
						),
					),
				)
			),
			flags = mutableSetOf(
				WorldFlag.PLANET_WORLD,
			)
		)
	)

	val GAHARA = register(
		"Gahara", WorldSettings(
			gasConfiguration = WorldGasConfiguration(
				gasses = listOf(
					CollectedGas(
						AtmosphericGasKeys.HYDROGEN, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(100),
								maxHeight = StaticIntegerAmount(384),
								minWeight = StaticDoubleAmount(0.0),
								maxWeight = StaticDoubleAmount(1.0)
							),
							weight = StaticDoubleAmount(0.3)
						)
					),
					CollectedGas(
						AtmosphericGasKeys.NITROGEN,
						ChildWeight(
							parent = ChildWeight(
								parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
							),
							weight = StaticDoubleAmount(0.2)
						),
					),
					CollectedGas(
						AtmosphericGasKeys.OXYGEN,
						ChildWeight(
							parent = ChildWeight(
								parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
							),
							weight = StaticDoubleAmount(0.5)
						),
					),
				)
			),
			flags = mutableSetOf(
				WorldFlag.PLANET_WORLD,
			)
		)
	)

	val QATRA = register(
		"Qatra", WorldSettings(
			gasConfiguration = WorldGasConfiguration(
				gasses = listOf(
					CollectedGas(
						AtmosphericGasKeys.METHANE, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(0),
								maxHeight = StaticIntegerAmount(384),
								minWeight = StaticDoubleAmount(0.5),
								maxWeight = StaticDoubleAmount(1.0)
							),
							weight = StaticDoubleAmount(0.35)
						)
					),
					CollectedGas(
						AtmosphericGasKeys.CHLORINE, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(0),
								maxHeight = StaticIntegerAmount(384),
								minWeight = StaticDoubleAmount(1.0),
								maxWeight = StaticDoubleAmount(0.0)
							),
							weight = StaticDoubleAmount(0.2)
						)
					),
					CollectedGas(
						AtmosphericGasKeys.FLUORINE, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(0),
								maxHeight = StaticIntegerAmount(128),
								minWeight = StaticDoubleAmount(1.0),
								maxWeight = StaticDoubleAmount(0.0)
							),
							weight = StaticDoubleAmount(0.6)
						)
					)
				)
			),
			flags = mutableSetOf(
				WorldFlag.PLANET_WORLD,
			)
		)
	)

	val KOVFEFE = register(
		"Kovfefe", WorldSettings(
			gasConfiguration = WorldGasConfiguration(
				gasses = listOf(
					CollectedGas(
						AtmosphericGasKeys.HYDROGEN, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(100),
								maxHeight = StaticIntegerAmount(384),
								minWeight = StaticDoubleAmount(0.0),
								maxWeight = StaticDoubleAmount(1.0)
							),
							weight = StaticDoubleAmount(0.1)
						)
					),
				)
			),
			flags = mutableSetOf(
				WorldFlag.PLANET_WORLD,
			)
		)
	)

	val LIODA = register(
		"Lioda", WorldSettings(
			gasConfiguration = WorldGasConfiguration(
				gasses = listOf(
					CollectedGas(
						AtmosphericGasKeys.NITROGEN,
						ChildWeight(
							parent = ChildWeight(
								parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
							),
							weight = StaticDoubleAmount(0.5)
						),
					),
					CollectedGas(
						AtmosphericGasKeys.OXYGEN,
						ChildWeight(
							parent = ChildWeight(
								parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
							),
							weight = StaticDoubleAmount(0.5)
						),
					),
				)
			),
			flags = mutableSetOf(
				WorldFlag.PLANET_WORLD,
			)
		)
	)

	val TURMS = register(
		"Turms", WorldSettings(
			gasConfiguration = WorldGasConfiguration(
				gasses = listOf(
					CollectedGas(
						AtmosphericGasKeys.HYDROGEN, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(100),
								maxHeight = StaticIntegerAmount(384),
								minWeight = StaticDoubleAmount(0.0),
								maxWeight = StaticDoubleAmount(1.0)
							),
							weight = StaticDoubleAmount(0.4)
						)
					),
				)
			),
			flags = mutableSetOf(
				WorldFlag.PLANET_WORLD,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					GravityModuleConfiguration(0.01),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val EDEN = register(
		"Ilius_horizonsend_eden", WorldSettings(
			gasConfiguration = WorldGasConfiguration(
				gasses = listOf(
					CollectedGas(
						AtmosphericGasKeys.METHANE, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(0),
								maxHeight = StaticIntegerAmount(384),
								minWeight = StaticDoubleAmount(0.5),
								maxWeight = StaticDoubleAmount(1.0)
							),
							weight = StaticDoubleAmount(0.45)
						)
					),
					CollectedGas(
						AtmosphericGasKeys.FLUORINE, ChildWeight(
							parent = HeightRamp(
								parent = StaticBase(amount = StaticIntegerAmount(85)),
								minHeight = StaticIntegerAmount(0),
								maxHeight = StaticIntegerAmount(128),
								minWeight = StaticDoubleAmount(1.0),
								maxWeight = StaticDoubleAmount(0.0)
							),
							weight = StaticDoubleAmount(0.5)
						)
					),
				)
			),
			customMobSpawns = listOf(
				WorldSettings.SpawnedMob(
					function = WorldSettings.SpawnedMob.AlwaysReplace,
					spawningWeight = 1.0,
					type = EntityType.WARDEN.name
				)
			),
			flags = mutableSetOf(
				WorldFlag.PLANET_WORLD,
			)
		)
	)

	val TRENCH = register(
		"Trench", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.15),
					Pair(1, 0.35),
					Pair(2, 0.35),
					Pair(3, 0.15)
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_AI_SPAWNS,
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.AREA_SHIELDS_DISABLED,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.NOT_SECURE,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
			terrainGenerationSettings = FeatureGeneratorConfiguration(features = setOf(
				AsteroidPlacementConfiguration(
					densityProvider = StaticConfigurationGlobal(0.06),
					selector = AsteroidPlacementConfiguration.AsteroidSelectorCondition.WeightedRandom(listOf(
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("TRENCH_ANCIENT_DEBRIS") to 2.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("TRENCH_NETHER_QUARTZ") to 1.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("TRENCH_COAL") to 2.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("TRENCH_COPPER") to 3.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("TRENCH_IRON") to 8.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("TRENCH_EMERALD") to 7.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("TRENCH_URANIUM") to 3.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("TRENCH_GOLD") to 3.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("TRENCH_REDSTONE") to 6.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("TRENCH_TITANIUM") to 5.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("TRENCH_LAPIS") to 2.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("TRENCH_CHETHERITE") to 6.0,
					)),
					minSize = 30.0,
					maxSize = 100.0
				)
			))
		)
	)

	val AU0821 = register(
		"AU-0821", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.0),
					Pair(1, 0.1),
					Pair(2, 0.55),
					Pair(3, 0.25),
					Pair(4, 0.1),
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_AI_SPAWNS,
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.AREA_SHIELDS_DISABLED,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.NOT_SECURE,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val HORIZON = register(
		"Horizon", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.2),
					Pair(1, 0.50),
					Pair(2, 0.25),
					Pair(3, 0.05),
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_AI_SPAWNS,
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.AREA_SHIELDS_DISABLED,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.NOT_SECURE,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val ASTERI = register(
		"Asteri", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.5),
					Pair(1, 0.35),
					Pair(2, 0.15)
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.ALLOW_SPACE_STATIONS,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val ILIOS = register(
		"Ilios", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.5),
					Pair(1, 0.35),
					Pair(2, 0.15)
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_AI_SPAWNS,
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.ALLOW_SPACE_STATIONS,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val SIRIUS = register(
		"Sirius", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.5),
					Pair(1, 0.35),
					Pair(2, 0.15)
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_AI_SPAWNS,
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.ALLOW_SPACE_STATIONS,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val REGULUS = register(
		"Regulus", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.5),
					Pair(1, 0.35),
					Pair(2, 0.15)
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_AI_SPAWNS,
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.ALLOW_SPACE_STATIONS,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val REGULUS_HYPERSPACE = register(
		"Regulus_hyperspace", WorldSettings(
			flags = mutableSetOf(
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.HYPERSPACE_WORLD,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val ASTERI_HYPERSPACE = register(
		"Asteri_hyperspace", WorldSettings(
			flags = mutableSetOf(
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.HYPERSPACE_WORLD,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val ILIOS_HYPERSPACE = register(
		"Ilios_hyperspace", WorldSettings(
			flags = mutableSetOf(
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.HYPERSPACE_WORLD,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val SIRIUS_HYPERSPACE = register(
		"SIRIUS_hyperspace", WorldSettings(
			flags = mutableSetOf(
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.HYPERSPACE_WORLD,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val TRENCH_HYPERSPACE = register(
		"Trench_hyperspace", WorldSettings(
			flags = mutableSetOf(
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.HYPERSPACE_WORLD,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val AU_0821_HYPERSPACE = register(
		"AU-0821_hyperspace", WorldSettings(
			flags = mutableSetOf(
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.HYPERSPACE_WORLD,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val HORIZON_HYPERSPACE = register(
		"Horizon_hyperspace", WorldSettings(
			flags = mutableSetOf(
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.HYPERSPACE_WORLD,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val LOA_7 = register(
		"LOA-7", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.15),
					Pair(1, 0.35),
					Pair(2, 0.35),
					Pair(3, 0.15)
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_AI_SPAWNS,
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.NOT_SECURE,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.ALLOW_SIGNATURE_SPAWNS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
			terrainGenerationSettings = FeatureGeneratorConfiguration(features = setOf(
				AsteroidPlacementConfiguration(
					densityProvider = StaticConfigurationGlobal(0.055),
					selector = AsteroidPlacementConfiguration.AsteroidSelectorCondition.WeightedRandom(listOf(
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("DEEP_SPACE_IRON") to 1.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("DEEP_SPACE_CHETHERITE") to 1.0
					)),
					minSize = 30.0,
					maxSize = 75.0
				)
			))
		)
	)

	val LOA_7_HYPERSPACE = register(
		"LOA-7_hyperspace", WorldSettings(
			flags = mutableSetOf(
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.HYPERSPACE_WORLD,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val TT_91 = register(
		"TT-91", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.15),
					Pair(1, 0.35),
					Pair(2, 0.35),
					Pair(3, 0.15)
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_AI_SPAWNS,
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.NOT_SECURE,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.ALLOW_SIGNATURE_SPAWNS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
			terrainGenerationSettings = FeatureGeneratorConfiguration(features = setOf(
				AsteroidPlacementConfiguration(
					densityProvider = StaticConfigurationGlobal(0.55),
					selector = AsteroidPlacementConfiguration.AsteroidSelectorCondition.WeightedRandom(listOf(
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("DEEP_SPACE_IRON") to 1.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("DEEP_SPACE_CHETHERITE") to 1.0
					)),
					minSize = 30.0,
					maxSize = 75.0
				)
			))
		)
	)

	val TT_91_HYPERSPACE = register(
		"TT-91_hyperspace", WorldSettings(
			flags = mutableSetOf(
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.HYPERSPACE_WORLD,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)
	val CXK_3 = register(
		"CXK-3", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.15),
					Pair(1, 0.35),
					Pair(2, 0.35),
					Pair(3, 0.15)
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_AI_SPAWNS,
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.NOT_SECURE,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.ALLOW_SIGNATURE_SPAWNS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
			terrainGenerationSettings = FeatureGeneratorConfiguration(features = setOf(
				AsteroidPlacementConfiguration(
					densityProvider = StaticConfigurationGlobal(0.55),
					selector = AsteroidPlacementConfiguration.AsteroidSelectorCondition.WeightedRandom(listOf(
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("DEEP_SPACE_IRON") to 1.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("DEEP_SPACE_CHETHERITE") to 1.0
					)),
					minSize = 30.0,
					maxSize = 75.0
				)
			))
		)
	)

	val CXK_3_HYPERSPACE = register(
		"CXK-3_hyperspace", WorldSettings(
			flags = mutableSetOf(
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.HYPERSPACE_WORLD,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val QIM_8 = register(
		"QIM-8", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.15),
					Pair(1, 0.35),
					Pair(2, 0.35),
					Pair(3, 0.15)
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_AI_SPAWNS,
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.NOT_SECURE,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.ALLOW_SIGNATURE_SPAWNS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
			terrainGenerationSettings = FeatureGeneratorConfiguration(features = setOf(
				AsteroidPlacementConfiguration(
					densityProvider = StaticConfigurationGlobal(0.055),
					selector = AsteroidPlacementConfiguration.AsteroidSelectorCondition.WeightedRandom(listOf(
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("DEEP_SPACE_IRON") to 1.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("DEEP_SPACE_CHETHERITE") to 1.0
					)),
					minSize = 30.0,
					maxSize = 75.0
				)
			))
		)
	)

	val QIM_8_HYPERSPACE = register(
		"QIM-8_hyperspace", WorldSettings(
			flags = mutableSetOf(
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.HYPERSPACE_WORLD,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val F3L_I = register(
		"F3L-I", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.15),
					Pair(1, 0.35),
					Pair(2, 0.35),
					Pair(3, 0.15)
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_AI_SPAWNS,
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.NOT_SECURE,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.ALLOW_SIGNATURE_SPAWNS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
			terrainGenerationSettings = FeatureGeneratorConfiguration(features = setOf(
				AsteroidPlacementConfiguration(
					densityProvider = StaticConfigurationGlobal(0.055),
					selector = AsteroidPlacementConfiguration.AsteroidSelectorCondition.WeightedRandom(listOf(
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("DEEP_SPACE_IRON") to 1.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("DEEP_SPACE_CHETHERITE") to 1.0
					)),
					minSize = 30.0,
					maxSize = 75.0
				)
			))
		)
	)

	val F3L_I_HYPERSPACE = register(
		"F3L-I_hyperspace", WorldSettings(
			flags = mutableSetOf(
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.HYPERSPACE_WORLD,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val BQ_5A = register(
		"BQ-5A", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.15),
					Pair(1, 0.35),
					Pair(2, 0.35),
					Pair(3, 0.15)
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_AI_SPAWNS,
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.NOT_SECURE,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.ALLOW_SIGNATURE_SPAWNS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
			terrainGenerationSettings = FeatureGeneratorConfiguration(features = setOf(
				AsteroidPlacementConfiguration(
					densityProvider = StaticConfigurationGlobal(0.055),
					selector = AsteroidPlacementConfiguration.AsteroidSelectorCondition.WeightedRandom(listOf(
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("DEEP_SPACE_IRON") to 1.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("DEEP_SPACE_CHETHERITE") to 1.0
					)),
					minSize = 30.0,
					maxSize = 75.0
				)
			))
		)
	)

	val BQ_5A_HYPERSPACE = register(
		"BQ-5A_hyperspace", WorldSettings(
			flags = mutableSetOf(
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.HYPERSPACE_WORLD,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val KRY_2 = register(
		"KRY-2", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.15),
					Pair(1, 0.35),
					Pair(2, 0.35),
					Pair(3, 0.15)
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_AI_SPAWNS,
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.NOT_SECURE,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.ALLOW_SIGNATURE_SPAWNS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
			terrainGenerationSettings = FeatureGeneratorConfiguration(features = setOf(
				AsteroidPlacementConfiguration(
					densityProvider = StaticConfigurationGlobal(0.055),
					selector = AsteroidPlacementConfiguration.AsteroidSelectorCondition.WeightedRandom(listOf(
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("DEEP_SPACE_IRON") to 1.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("DEEP_SPACE_CHETHERITE") to 1.0
					)),
					minSize = 30.0,
					maxSize = 75.0
				)
			))
		)
	)

	val KRY_2_HYPERSPACE = register(
		"KRY-2_hyperspace", WorldSettings(
			flags = mutableSetOf(
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.HYPERSPACE_WORLD,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val OMN_6 = register(
		"OMN-6", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.15),
					Pair(1, 0.35),
					Pair(2, 0.35),
					Pair(3, 0.15)
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_AI_SPAWNS,
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.NOT_SECURE,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.ALLOW_SIGNATURE_SPAWNS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
			terrainGenerationSettings = FeatureGeneratorConfiguration(features = setOf(
				AsteroidPlacementConfiguration(
					densityProvider = StaticConfigurationGlobal(0.055),
					selector = AsteroidPlacementConfiguration.AsteroidSelectorCondition.WeightedRandom(listOf(
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("DEEP_SPACE_IRON") to 1.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("DEEP_SPACE_CHETHERITE") to 1.0
					)),
					minSize = 30.0,
					maxSize = 75.0
				)
			))
		)
	)

	val OMN_6_HYPERSPACE = register(
		"OMN-6_hyperspace", WorldSettings(
			flags = mutableSetOf(
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.HYPERSPACE_WORLD,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val SUNDER = register(
		"Sunder", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.15),
					Pair(1, 0.35),
					Pair(2, 0.35),
					Pair(3, 0.15)
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_AI_SPAWNS,
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.NOT_SECURE,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.ALLOW_SIGNATURE_SPAWNS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
			terrainGenerationSettings = FeatureGeneratorConfiguration(features = setOf(
				AsteroidPlacementConfiguration(
					densityProvider = StaticConfigurationGlobal(0.055),
					selector = AsteroidPlacementConfiguration.AsteroidSelectorCondition.WeightedRandom(listOf(
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("DEEP_SPACE_IRON") to 1.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("DEEP_SPACE_CHETHERITE") to 1.0
					)),
					minSize = 30.0,
					maxSize = 75.0
				)
			))
		)
	)

	val SUNDER_HYPERSPACE = register(
		"Sunder_hyperspace", WorldSettings(
			flags = mutableSetOf(
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.HYPERSPACE_WORLD,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val MERIDIAN = register(
		"Meridian", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.15),
					Pair(1, 0.35),
					Pair(2, 0.35),
					Pair(3, 0.15)
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_AI_SPAWNS,
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.NOT_SECURE,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.ALLOW_SIGNATURE_SPAWNS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
			terrainGenerationSettings = FeatureGeneratorConfiguration(features = setOf(
				AsteroidPlacementConfiguration(
					densityProvider = StaticConfigurationGlobal(0.055),
					selector = AsteroidPlacementConfiguration.AsteroidSelectorCondition.WeightedRandom(listOf(
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("DEEP_SPACE_IRON") to 1.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("DEEP_SPACE_CHETHERITE") to 1.0
					)),
					minSize = 30.0,
					maxSize = 75.0
				)
			))
		)
	)

	val MERIDIAN_HYPERSPACE = register(
		"Meridian_hyperspace", WorldSettings(
			flags = mutableSetOf(
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.HYPERSPACE_WORLD,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)

	val FAULT = register(
		"Fault", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.15),
					Pair(1, 0.35),
					Pair(2, 0.35),
					Pair(3, 0.15)
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_AI_SPAWNS,
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.NOT_SECURE,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.ALLOW_SIGNATURE_SPAWNS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
			terrainGenerationSettings = FeatureGeneratorConfiguration(features = setOf(
				AsteroidPlacementConfiguration(
					densityProvider = StaticConfigurationGlobal(0.06),
					selector = AsteroidPlacementConfiguration.AsteroidSelectorCondition.WeightedRandom(listOf(
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_ANCIENT_DEBRIS") to 2.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_NETHER_QUARTZ") to 1.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_COAL") to 2.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_COPPER") to 3.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_IRON") to 9.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_EMERALD") to 7.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_URANIUM") to 4.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_GOLD") to 4.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_REDSTONE") to 7.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_TITANIUM") to 7.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_LAPIS") to 4.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_CHETHERITE") to 9.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_SCORDITE") to 0.5,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_VANADIUM") to 0.2,
					)),
					minSize = 30.0,
					maxSize = 75.0
				)
			))
		)
	)

	val VERTIGO = register(
		"Vertigo", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.15),
					Pair(1, 0.35),
					Pair(2, 0.35),
					Pair(3, 0.15)
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_AI_SPAWNS,
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.NOT_SECURE,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.ALLOW_SIGNATURE_SPAWNS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
			terrainGenerationSettings = FeatureGeneratorConfiguration(features = setOf(
				AsteroidPlacementConfiguration(
					densityProvider = StaticConfigurationGlobal(0.06),
					selector = AsteroidPlacementConfiguration.AsteroidSelectorCondition.WeightedRandom(listOf(
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_ANCIENT_DEBRIS") to 2.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_NETHER_QUARTZ") to 1.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_COAL") to 2.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_COPPER") to 3.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_IRON") to 9.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_EMERALD") to 7.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_URANIUM") to 4.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_GOLD") to 4.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_REDSTONE") to 7.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_TITANIUM") to 7.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_LAPIS") to 4.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_CHETHERITE") to 9.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_SCORDITE") to 0.5,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_VANADIUM") to 0.2,
					)),
					minSize = 30.0,
					maxSize = 75.0
				)
			))
		)
	)

	val ANCHOR = register(
		"Anchor", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.15),
					Pair(1, 0.35),
					Pair(2, 0.35),
					Pair(3, 0.15)
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_AI_SPAWNS,
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.NOT_SECURE,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.ALLOW_SIGNATURE_SPAWNS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
			terrainGenerationSettings = FeatureGeneratorConfiguration(features = setOf(
				AsteroidPlacementConfiguration(
					densityProvider = StaticConfigurationGlobal(0.06),
					selector = AsteroidPlacementConfiguration.AsteroidSelectorCondition.WeightedRandom(listOf(
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_ANCIENT_DEBRIS") to 2.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_NETHER_QUARTZ") to 1.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_COAL") to 2.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_COPPER") to 3.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_IRON") to 9.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_EMERALD") to 7.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_URANIUM") to 4.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_GOLD") to 4.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_REDSTONE") to 7.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_TITANIUM") to 7.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_LAPIS") to 4.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_CHETHERITE") to 9.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_SCORDITE") to 0.5,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_VANADIUM") to 0.2,
					)),
					minSize = 30.0,
					maxSize = 75.0
				)
			))
		)
	)

	val RELIQUARY = register(
		"Reliquary", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.15),
					Pair(1, 0.35),
					Pair(2, 0.35),
					Pair(3, 0.15)
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_AI_SPAWNS,
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.NOT_SECURE,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.ALLOW_SIGNATURE_SPAWNS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
			terrainGenerationSettings = FeatureGeneratorConfiguration(features = setOf(
				AsteroidPlacementConfiguration(
					densityProvider = StaticConfigurationGlobal(0.06),
					selector = AsteroidPlacementConfiguration.AsteroidSelectorCondition.WeightedRandom(listOf(
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_ANCIENT_DEBRIS") to 2.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_NETHER_QUARTZ") to 1.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_COAL") to 2.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_COPPER") to 3.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_IRON") to 9.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_EMERALD") to 7.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_URANIUM") to 4.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_GOLD") to 4.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_REDSTONE") to 7.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_TITANIUM") to 7.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_LAPIS") to 4.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_CHETHERITE") to 9.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_SCORDITE") to 0.5,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_VANADIUM") to 0.2,
					)),
					minSize = 30.0,
					maxSize = 75.0
				)
			))
		)
	)
	val TESTING= register(
		"TESTING", WorldSettings(
			aiDifficulty = WeightedIntegerAmount(
				setOf(
					Pair(0, 0.15),
					Pair(1, 0.35),
					Pair(2, 0.35),
					Pair(3, 0.15)
				)
			),
			flags = mutableSetOf(
				WorldFlag.ALLOW_AI_SPAWNS,
				WorldFlag.ALLOW_MINING_LASERS,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.NOT_SECURE,
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.ALLOW_SIGNATURE_SPAWNS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
			terrainGenerationSettings = FeatureGeneratorConfiguration(features = setOf(
				AsteroidPlacementConfiguration(
					densityProvider = StaticConfigurationGlobal(0.06),
					selector = AsteroidPlacementConfiguration.AsteroidSelectorCondition.WeightedRandom(listOf(

						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_SCORDITE") to 5.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_VANADIUM") to 5.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("TEST_WORLD_ZIRCON") to 5.0,
						AsteroidPlacementConfiguration.AsteroidSelectorCondition.BuilderReference("MINING_WORLD_ATAVUM") to 5.0
						,
					)),
					minSize = 50.0,
					maxSize = 90.0
				)
			))
		)
	)

	val FAULT_HYPERSPACE = register(
		"Fault_hyperspace", WorldSettings(
			flags = mutableSetOf(
				WorldFlag.SPACE_WORLD,
				WorldFlag.SPEEDERS_EXPLODE,
				WorldFlag.HYPERSPACE_WORLD,
				WorldFlag.NO_SHIP_LOCKS,
				WorldFlag.DOMINION_WORLD,
				WorldFlag.REGION_WORLD_FRACTURE,
			),
			environments = WorldEnvironmentConfiguration(
				atmosphericPressure = 0.0,
				moduleConfiguration = listOf(
					NoGravityModuleConfiguration(ignoreIndoors = false),
					VacuumEnvironmentConfiguration
				)
			),
		)
	)
}
