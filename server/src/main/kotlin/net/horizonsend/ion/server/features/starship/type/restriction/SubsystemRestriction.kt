package net.horizonsend.ion.server.features.starship.type.restriction

import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.subsystem.StarshipSubsystem
import kotlin.reflect.KClass

abstract class SubsystemRestriction(val clazz: KClass<out StarshipSubsystem>) {
	abstract fun check(starship: Starship): Boolean

	class RequiredSubsystem(clazz: KClass<out StarshipSubsystem>, val minAmount: Int) : SubsystemRestriction(clazz) {
		override fun check(starship: Starship): Boolean {
			return starship.subsystems.filterIsInstance(clazz.java).count() >= minAmount
		}
	}

	class CappedSubsystem(clazz: KClass<out StarshipSubsystem>, val maxCount: Int) : SubsystemRestriction(clazz) {
		override fun check(starship: Starship): Boolean {
			return starship.subsystems.filterIsInstance(clazz.java).count() <= maxCount
		}
	}

	companion object {
		inline fun <reified T: StarshipSubsystem> require(minAmount: Int): SubsystemRestriction = RequiredSubsystem(T::class, minAmount)
		inline fun <reified T: StarshipSubsystem> ban(maxCount: Int): SubsystemRestriction = CappedSubsystem(T::class, maxCount)
	}
}
