package net.horizonsend.ion.server.features.world.configuration

import net.horizonsend.ion.server.configuration.util.DurationConfig
import net.horizonsend.ion.server.configuration.util.DurationRange
import net.horizonsend.ion.server.configuration.util.StaticDoubleAmount
import net.horizonsend.ion.server.configuration.util.StaticIntegerAmount
import net.horizonsend.ion.server.configuration.util.WeightedIntegerAmount
import net.horizonsend.ion.server.core.registration.keys.AtmosphericGasKeys
import net.horizonsend.ion.server.core.registration.keys.WeatherTypeKeys
import net.horizonsend.ion.server.features.gas.collection.ChildWeight
import net.horizonsend.ion.server.features.gas.collection.CollectedGas
import net.horizonsend.ion.server.features.gas.collection.HeightRamp
import net.horizonsend.ion.server.features.gas.collection.StaticBase
import net.horizonsend.ion.server.features.gas.type.WorldGasConfiguration
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.features.world.WorldSettings
import net.horizonsend.ion.server.features.world.environment.configuration.GravityModuleConfiguration
import net.horizonsend.ion.server.features.world.environment.configuration.NoGravityModuleConfiguration
import net.horizonsend.ion.server.features.world.environment.configuration.UltracoldEnvironmentModuleConfiguration
import net.horizonsend.ion.server.features.world.environment.configuration.VacuumEnvironmentConfiguration
import net.horizonsend.ion.server.features.world.environment.configuration.WorldEnvironmentConfiguration
import net.horizonsend.ion.server.features.world.environment.modules.GravityModule.Companion.DEFAULT_GRAVITY
import net.horizonsend.ion.server.features.world.environment.weather.configuration.WeatherTypeConfiguration
import net.horizonsend.ion.server.features.world.environment.weather.configuration.WorldWeatherConfiguration
import net.horizonsend.ion.server.features.world.generation.generators.configuration.feature.AsteroidPlacementConfiguration
import net.horizonsend.ion.server.features.world.generation.generators.configuration.feature.WreckPlacementConfiguration
import net.horizonsend.ion.server.features.world.generation.generators.configuration.generator.FeatureGeneratorConfiguration
import org.bukkit.entity.EntityType
import java.util.concurrent.TimeUnit

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
				WreckPlacementConfiguration()
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
					VacuumEnvironmentConfiguration,
					UltracoldEnvironmentModuleConfiguration
				),
				weatherConfiguration = WorldWeatherConfiguration(
					weatherSeparation = DurationRange(DurationConfig(TimeUnit.SECONDS, 15), DurationConfig(TimeUnit.SECONDS, 60)),
					weatherTypes = listOf(
						WeatherTypeConfiguration(WeatherTypeKeys.BLIZZARD, duration = DurationRange(DurationConfig(TimeUnit.SECONDS, 15), DurationConfig(TimeUnit.SECONDS, 60)))
					)
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
}
