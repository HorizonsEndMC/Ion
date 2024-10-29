package net.horizonsend.ion.server.features.world.configuration

import net.horizonsend.ion.server.configuration.util.StaticDoubleAmount
import net.horizonsend.ion.server.configuration.util.StaticIntegerAmount
import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.gas.collection.ChildWeight
import net.horizonsend.ion.server.features.gas.collection.CollectedGas
import net.horizonsend.ion.server.features.gas.collection.HeightRamp
import net.horizonsend.ion.server.features.gas.collection.StaticBase
import net.horizonsend.ion.server.features.gas.type.WorldGasConfiguration
import net.horizonsend.ion.server.features.world.WorldSettings
import org.bukkit.entity.EntityType

@Suppress("UNUSED")
object DefaultWorldConfiguration {
	private val defaultConfigs = mutableMapOf<String, WorldSettings>()

	operator fun get(world: String): WorldSettings = defaultConfigs[world] ?: WorldSettings()

	private fun register(worldName: String, settings: WorldSettings): WorldSettings {
		defaultConfigs[worldName] = settings
		return settings
	}

	val TEST = register("Chandra", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.HYDROGEN.identifier, ChildWeight(
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
		CollectedGas(Gasses.NITROGEN.identifier,
			ChildWeight(
				parent = ChildWeight(
					parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
				),
				weight = StaticDoubleAmount(0.5)
			),
		),
		CollectedGas(Gasses.METHANE.identifier, ChildWeight(
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
		CollectedGas(Gasses.OXYGEN.identifier,
			ChildWeight(
				parent = ChildWeight(
					parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
				),
				weight = StaticDoubleAmount(0.5)
			),
		),
		CollectedGas(Gasses.CHLORINE.identifier, ChildWeight(
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
		CollectedGas(Gasses.FLUORINE.identifier, ChildWeight(
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
	))))

	val CHANDRA = register("Chandra", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.HYDROGEN.identifier, ChildWeight(
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
	))))

	val ILIUS = register("Ilius", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.NITROGEN.identifier,
			ChildWeight(
				parent = ChildWeight(
					parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
				),
				weight = StaticDoubleAmount(0.5)
			),
		),
		CollectedGas(Gasses.OXYGEN.identifier,
			ChildWeight(
				parent = ChildWeight(
					parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
				),
				weight = StaticDoubleAmount(0.2)
			),
		),
	))))

	val LUXITERNA = register("Luxiterna", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.HYDROGEN.identifier, ChildWeight(
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
		CollectedGas(Gasses.OXYGEN.identifier,
			ChildWeight(
				parent = ChildWeight(
					parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
				),
				weight = StaticDoubleAmount(0.25)
			),
		),
		CollectedGas(Gasses.FLUORINE.identifier, ChildWeight(
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
	))))

	val HERDOLI = register("Herdoli", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.HYDROGEN.identifier, ChildWeight(
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
	))))

	val RUBACIEA = register("Rubaciea", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.HYDROGEN.identifier, ChildWeight(
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
		CollectedGas(Gasses.NITROGEN.identifier,
			ChildWeight(
				parent = ChildWeight(
					parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
				),
				weight = StaticDoubleAmount(0.3)
			),
		),
		CollectedGas(Gasses.OXYGEN.identifier,
			ChildWeight(
				parent = ChildWeight(
					parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
				),
				weight = StaticDoubleAmount(0.1)
			),
		),
	))))

	val ISIK = register("Isik", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.METHANE.identifier, ChildWeight(
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
		CollectedGas(Gasses.CHLORINE.identifier, ChildWeight(
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
	))))

	val CHIMGARA = register("Chimgara", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.NITROGEN.identifier,
			ChildWeight(
				parent = ChildWeight(
					parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
				),
				weight = StaticDoubleAmount(0.2)
			),
		),
		CollectedGas(Gasses.OXYGEN.identifier,
			ChildWeight(
				parent = ChildWeight(
					parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
				),
				weight = StaticDoubleAmount(0.2)
			),
		),
		CollectedGas(Gasses.CHLORINE.identifier, ChildWeight(
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
	))))

	val DAMKOTH = register("Damkoth", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.METHANE.identifier, ChildWeight(
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
		CollectedGas(Gasses.CHLORINE.identifier, ChildWeight(
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
	))))

	val KRIO = register("Krio", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.NITROGEN.identifier,
			ChildWeight(
				parent = ChildWeight(
					parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
				),
				weight = StaticDoubleAmount(0.333)
			),
		),
		CollectedGas(Gasses.OXYGEN.identifier,
			ChildWeight(
				parent = ChildWeight(
					parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
				),
				weight = StaticDoubleAmount(0.333)
			),
		),
	))))

	val ARET = register("Aret", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.NITROGEN.identifier,
			ChildWeight(
				parent = ChildWeight(
					parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
				),
				weight = StaticDoubleAmount(0.4)
			),
		),
		CollectedGas(Gasses.OXYGEN.identifier,
			ChildWeight(
				parent = ChildWeight(
					parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
				),
				weight = StaticDoubleAmount(0.2)
			),
		),
	))))

	val AERACH = register("Aerach", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.NITROGEN.identifier,
			ChildWeight(
				parent = ChildWeight(
					parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
				),
				weight = StaticDoubleAmount(0.5)
			),
		),
		CollectedGas(Gasses.OXYGEN.identifier,
			ChildWeight(
				parent = ChildWeight(
					parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
				),
				weight = StaticDoubleAmount(0.3)
			),
		),
	))))

	val VASK = register("Vask", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.NITROGEN.identifier,
			ChildWeight(
				parent = ChildWeight(
					parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
				),
				weight = StaticDoubleAmount(0.5)
			),
		),
		CollectedGas(Gasses.OXYGEN.identifier,
			ChildWeight(
				parent = ChildWeight(
					parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
				),
				weight = StaticDoubleAmount(0.5)
			),
		),
	))))

	val GAHARA = register("Gahara", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.HYDROGEN.identifier, ChildWeight(
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
		CollectedGas(Gasses.NITROGEN.identifier,
			ChildWeight(
				parent = ChildWeight(
					parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
				),
				weight = StaticDoubleAmount(0.2)
			),
		),
		CollectedGas(Gasses.OXYGEN.identifier,
			ChildWeight(
				parent = ChildWeight(
					parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
				),
				weight = StaticDoubleAmount(0.5)
			),
		),
	))))

	val QATRA = register("Qatra", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.METHANE.identifier, ChildWeight(
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
		CollectedGas(Gasses.CHLORINE.identifier, ChildWeight(
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
		CollectedGas(Gasses.FLUORINE.identifier, ChildWeight(
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
	))))

	val KOVFEFE = register("Kovfefe", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.HYDROGEN.identifier, ChildWeight(
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
	))))

	val LIODA = register("Lioda", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.NITROGEN.identifier,
			ChildWeight(
				parent = ChildWeight(
					parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
				),
				weight = StaticDoubleAmount(0.5)
			),
		),
		CollectedGas(Gasses.OXYGEN.identifier,
			ChildWeight(
				parent = ChildWeight(
					parent = StaticBase(amount = StaticIntegerAmount(85)), weight = StaticDoubleAmount(0.75)
				),
				weight = StaticDoubleAmount(0.5)
			),
		),
	))))

	val TURMS = register("Turms", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.HYDROGEN.identifier, ChildWeight(
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
	))))

	val EDEN = register("Ilius_horizonsend_eden", WorldSettings(
		gasConfiguration = WorldGasConfiguration(gasses = listOf(
			CollectedGas(Gasses.METHANE.identifier, ChildWeight(
				parent = HeightRamp(
					parent = StaticBase(amount = StaticIntegerAmount(85)),
					minHeight = StaticIntegerAmount(0),
					maxHeight = StaticIntegerAmount(384),
					minWeight = StaticDoubleAmount(0.5),
					maxWeight = StaticDoubleAmount(1.0)
				),
				weight = StaticDoubleAmount(0.45))
			),
			CollectedGas(Gasses.FLUORINE.identifier, ChildWeight(
				parent = HeightRamp(
					parent = StaticBase(amount = StaticIntegerAmount(85)),
					minHeight = StaticIntegerAmount(0),
					maxHeight = StaticIntegerAmount(128),
					minWeight = StaticDoubleAmount(1.0),
					maxWeight = StaticDoubleAmount(0.0)
				),
				weight = StaticDoubleAmount(0.5))
			),
		)),
		customMobSpawns = listOf(WorldSettings.SpawnedMob(
			function = WorldSettings.SpawnedMob.AlwaysReplace,
			spawningWeight = 1.0,
			type = EntityType.WARDEN.name
		))
	))
}
