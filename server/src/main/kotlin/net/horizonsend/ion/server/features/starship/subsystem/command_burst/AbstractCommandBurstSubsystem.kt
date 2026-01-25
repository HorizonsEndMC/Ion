package net.horizonsend.ion.server.features.starship.subsystem.command_burst

import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.server.configuration.starship.StarshipCommandBurstBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.AbstractCommandBurstMultiblock
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.spherePoints
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.block.Sign
import java.util.function.Supplier

abstract class AbstractCommandBurstSubsystem<T : StarshipCommandBurstBalancing>(
	starship: Starship,
	sign: Sign,
	multiblock: AbstractCommandBurstMultiblock,
	val balancingSupplier: Supplier<T>
) : AbstractMultiblockSubsystem<AbstractCommandBurstMultiblock>(starship, sign, multiblock) {

	/** Balancing values for this subsystem **/
	val balancing get() = balancingSupplier.get()
	var lastActivated: Long = System.currentTimeMillis()

	/** Cooldown between activating abilities of this command burst **/
	open val activateCooldownMillis: Long get() = balancing.activateCooldownMillis

	abstract val color: Color

	fun isCooledDown(): Boolean {
		return System.currentTimeMillis() - lastActivated >= activateCooldownMillis
	}

	fun canCreateSubsystem(): Boolean {
		if (starship.type.eventShip) return true
		if (!balancing.activateRestrictions.canActivate && !starship.type.eventShip) return false
		return starship.initialBlockCount in balancing.activateRestrictions.minBlockCount..balancing.activateRestrictions.maxBlockCount
	}

	fun activate() {
		val starshipsInRange = ActiveStarships.getInWorld(starship.world).filter { otherStarship ->
			otherStarship.centerOfMass.toLocation(starship.world).distanceSquared(starship.centerOfMass.toLocation(starship.world)) <= balancing.range
		}

		activateEffect(starshipsInRange.toSet())
		spawnParticles()
	}

	protected abstract fun activateEffect(starships: Set<Starship>)

	fun postActivate() {
		lastActivated = System.currentTimeMillis()
	}

	abstract fun getName(): Component

	fun spawnParticles() {
		val task = Tasks.syncRepeatTask(0L, 4L) {
			for (endPoint in starship.centerOfMass.toLocation(starship.world).spherePoints(100.0, 300)) {
				starship.world.spawnParticle(
					Particle.TRAIL,
					starship.centerOfMass.toLocation(starship.world),
					1,
					0.5,
					0.5,
					0.5,
					0.0,
					Particle.Trail(endPoint, color, randomInt(90, 100)),
					true
				)
			}
		}
		Tasks.syncDelay(60L) {
			task.cancel()
		}
	}
}
