package net.horizonsend.ion.server.features.gas

import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import kotlin.math.roundToInt

enum class Oxidizers(override val id: String, val powerMultiplier: Double) : GasProperties {
	OXYGEN(CustomItems.GAS_CANISTER_OXYGEN.id, 1.0),
	CHLORINE(CustomItems.GAS_CANISTER_CHLORINE.id, 1.0),
	FLUORINE(CustomItems.GAS_CANISTER_FLUORINE.id, 1.0);

	override fun getPower(other: GasProperties): Int {
		if (other !is Fuels) return 0

		return (other.power * powerMultiplier).roundToInt()
	}
}

enum class Fuels(override val id: String, val power: Int, val cooldown: Int) : GasProperties {
	METHANE(CustomItems.GAS_CANISTER_METHANE.id, 400, 150),
	HYDROGEN(CustomItems.GAS_CANISTER_HYDROGEN.id, 500, 200),
	NITROGEN(CustomItems.GAS_CANISTER_NITROGEN.id, 250, 100);

	override fun getPower(other: GasProperties): Int {
		if (other !is Oxidizers) return 0

		return (other.powerMultiplier * power).roundToInt()
	}
}

interface GasProperties {
	val id: String

	fun getPower(other: GasProperties): Int

	companion object {
		private val allProperties = mutableListOf<GasProperties>().apply {
			this.addAll(Oxidizers.values())
			this.addAll(Fuels.values())
		}

		operator fun get(id: String): GasProperties? = allProperties.firstOrNull { it.id == id }

		fun all() = allProperties.toList()
	}
}
