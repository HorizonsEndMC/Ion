package net.horizonsend.ion.server.features.world.configuration

import net.horizonsend.ion.server.configuration.VariableIntegerAmount
import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.gas.collection.CollectedGas
import net.horizonsend.ion.server.features.gas.collection.StaticBase
import net.horizonsend.ion.server.features.gas.type.WorldGasConfiguration
import net.horizonsend.ion.server.features.world.WorldSettings

@Suppress("UNUSED")
object DefaultWorldConfiguration {
	private val defaultConfigs = mutableMapOf<String, WorldSettings>()

	operator fun get(world: String): WorldSettings = defaultConfigs[world] ?: WorldSettings()

	private fun register(worldName: String, settings: WorldSettings): WorldSettings {
		defaultConfigs[worldName] = settings
		return settings
	}

	val CHANDRA = register("Chandra", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.HYDROGEN.identifier, StaticBase(VariableIntegerAmount(-1, 8)))
	))))

	val ILIUS = register("Ilius", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.OXYGEN.identifier, StaticBase(VariableIntegerAmount(-8, 2))),
		CollectedGas(Gasses.NITROGEN.identifier, StaticBase(VariableIntegerAmount(-1, 1)))
	))))

	val LUXITERNA = register("Luxiterna", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.HYDROGEN.identifier, StaticBase(VariableIntegerAmount(-8, 2))),
		CollectedGas(Gasses.OXYGEN.identifier, StaticBase(VariableIntegerAmount(-4, 1))),
		CollectedGas(Gasses.FLUORINE.identifier, StaticBase(VariableIntegerAmount(-4, 1))),
	))))

	val HERDOLI = register("Herdoli", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.HYDROGEN.identifier, StaticBase(VariableIntegerAmount(-8, 2)))
	))))

	val RUBACIEA = register("Rubaciea", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.HYDROGEN.identifier, StaticBase(VariableIntegerAmount(-2, 4))),
		CollectedGas(Gasses.NITROGEN.identifier, StaticBase(VariableIntegerAmount(-3, 2))),
		CollectedGas(Gasses.OXYGEN.identifier, StaticBase(VariableIntegerAmount(-8, 1)))
	))))

	val ISIK = register("Isik", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.METHANE.identifier, StaticBase(VariableIntegerAmount(-4, 1))),
		CollectedGas(Gasses.CHLORINE.identifier, StaticBase(VariableIntegerAmount(-1, 2)))
	))))

	val CHIMGARA = register("Chimgara", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.NITROGEN.identifier, StaticBase(VariableIntegerAmount(-5, 1))),
		CollectedGas(Gasses.OXYGEN.identifier, StaticBase(VariableIntegerAmount(-5, 1))),
		CollectedGas(Gasses.CHLORINE.identifier, StaticBase(VariableIntegerAmount(-2, 3)))
	))))

	val DAMKOTH = register("Damkoth", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.METHANE.identifier, StaticBase(VariableIntegerAmount(-6, 1))),
		CollectedGas(Gasses.CHLORINE.identifier, StaticBase(VariableIntegerAmount(-4, 3)))
	))))

	val KRIO = register("Krio", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.NITROGEN.identifier, StaticBase(VariableIntegerAmount(-2, 1))),
		CollectedGas(Gasses.OXYGEN.identifier, StaticBase(VariableIntegerAmount(-2, 1)))
	))))

	val ARET = register("Aret", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.NITROGEN.identifier, StaticBase(VariableIntegerAmount(-2, 5))),
		CollectedGas(Gasses.OXYGEN.identifier, StaticBase(VariableIntegerAmount(-6, 1)))
	))))

	val AERACH = register("Aerach", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.NITROGEN.identifier, StaticBase(VariableIntegerAmount(-2, 2))),
		CollectedGas(Gasses.OXYGEN.identifier, StaticBase(VariableIntegerAmount(-3, 2)))
	))))

	val VASK = register("Vask", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.NITROGEN.identifier, StaticBase(VariableIntegerAmount(-2, 2))),
		CollectedGas(Gasses.OXYGEN.identifier, StaticBase(VariableIntegerAmount(-2, 2)))
	))))

	val GAHARA = register("Gahara", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.HYDROGEN.identifier, StaticBase(VariableIntegerAmount(-6, 2))),
		CollectedGas(Gasses.NITROGEN.identifier, StaticBase(VariableIntegerAmount(-8, 2))),
		CollectedGas(Gasses.OXYGEN.identifier, StaticBase(VariableIntegerAmount(-2, 2)))
	))))

	val QATRA = register("Qatra", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.METHANE.identifier, StaticBase(VariableIntegerAmount(-6, 2))),
		CollectedGas(Gasses.CHLORINE.identifier, StaticBase(VariableIntegerAmount(-8, 2))),
		CollectedGas(Gasses.FLUORINE.identifier, StaticBase(VariableIntegerAmount(-2, 4)))
	))))

	val KOVFEFE = register("Kovfefe", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.HYDROGEN.identifier, StaticBase(VariableIntegerAmount(-9, 2)))
	))))

	val LIODA = register("Lioda", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.NITROGEN.identifier, StaticBase(VariableIntegerAmount(-2, 2))),
		CollectedGas(Gasses.OXYGEN.identifier, StaticBase(VariableIntegerAmount(-2, 2)))
	))))

	val TURMS = register("Turms", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.HYDROGEN.identifier, StaticBase(VariableIntegerAmount(-2, 6)))
	))))

	val EDEN = register("Ilius_horizonsend_eden", WorldSettings(gasConfiguration = WorldGasConfiguration(gasses = listOf(
		CollectedGas(Gasses.METHANE.identifier, StaticBase(VariableIntegerAmount(-4, 4))),
		CollectedGas(Gasses.FLUORINE.identifier, StaticBase(VariableIntegerAmount(-2, 2)))
	))))
}
