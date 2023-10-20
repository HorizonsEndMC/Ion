package net.horizonsend.ion.server.features.starship.subsystem.weapon

import org.bukkit.entity.Player
import com.google.common.collect.HashMultimap
import com.google.common.util.concurrent.AtomicDouble
import net.horizonsend.ion.common.extensions.alertActionMessage
import net.horizonsend.ion.common.extensions.userErrorActionMessage
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.command.admin.debugRed
import net.horizonsend.ion.server.features.starship.AutoTurretTargeting
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.StarshipCooldownSubsystem
import org.bukkit.util.Vector
import java.util.concurrent.ThreadLocalRandom

object StarshipWeapons {
	interface QueuedShot {
		val weapon: WeaponSubsystem

		fun shoot()
	}

	data class ManualQueuedShot(
        override val weapon: WeaponSubsystem,
        val shooter: Damager,
        val direction: Vector,
        val target: Vector
	) : QueuedShot {
		override fun shoot() {
			check(weapon is ManualWeaponSubsystem)
			weapon.manualFire(shooter, direction, target)
			weapon.postFire()
		}
	}

	data class AutoQueuedShot(
		override val weapon: WeaponSubsystem,
		val target: AutoTurretTargeting.AutoTurretTarget<*>,
		val dir: Vector,
	) : QueuedShot {
		override fun shoot() {
			check(weapon is AutoWeaponSubsystem)

			weapon.autoFire(target, dir)
			weapon.postFire()
		}
	}

	fun fireQueuedShots(queuedShots: List<QueuedShot>, ship: ActiveStarship) {
		val boostPower = AtomicDouble(0.0)

		if (queuedShots.any { it.weapon is HeavyWeaponSubsystem }) {
			ship.debug("we have heavy weapons")

			val heavyWeaponTypes =
				queuedShots.filter { it.weapon is HeavyWeaponSubsystem }.map { it.weapon.name }.distinct()

			ship.debug("heavyWeaponTypes = ${heavyWeaponTypes.joinToString(", ")}")

			if (heavyWeaponTypes.count() > 1) {
				ship.onlinePassengers.forEach { player ->
					player.userErrorActionMessage(
						"You can only fire one type of heavy weapon at a time!"
					)
				}

				return
			}

			val heavyWeaponType = heavyWeaponTypes.single()
			ship.debug("heavyWeaponType = $heavyWeaponType")

			val newWarmup = queuedShots
				.filter { it.weapon is HeavyWeaponSubsystem }
				.maxOf { (it.weapon as HeavyWeaponSubsystem).boostChargeNanos }
			ship.debug("newWarmup = $newWarmup")
			val output = ship.reactor.heavyWeaponBooster.boost(heavyWeaponType, newWarmup)
			ship.debug("output = $output")
			boostPower.set(output)
		}

		val firedCounts = HashMultimap.create<String, WeaponSubsystem>()

		for (shot in queuedShots.shuffled(ThreadLocalRandom.current())) {
			if (shot.weapon is StarshipCooldownSubsystem) {
				val clazz = shot.weapon::class.java

				for (subsystem in ship.subsystems.filterIsInstance(clazz)) {
					subsystem.lastFire = System.nanoTime()
				}
			}

			val weapon = shot.weapon

			val maxPerShot = weapon.getMaxPerShot()
			ship.debug("iterating shots, $weapon, $maxPerShot")

			val firedSet = firedCounts[weapon.name]
			ship.debug("have we fired those already?")
			if (maxPerShot != null && firedSet.size >= maxPerShot) {
				ship.debug("we did, goodbye (${firedSet.size}, $maxPerShot)")

				continue
			}

			ship.debug("is resource available?")
			if (resourcesUnavailable(weapon, ship, boostPower)) {
				ship.debug("its not, goodbye")
				continue
			}

			ship.debugRed("shootings!!")
			shot.shoot()

			ship.debug("taking resources")
			consumeResources(weapon, boostPower, ship)

			ship.debug("adding to fired")
			firedSet.add(weapon)
		}

		ship.reactor.heavyWeaponBooster.reduceWarmup(boostPower.get())
	}

	private fun resourcesUnavailable(
		weapon: WeaponSubsystem,
		ship: ActiveStarship,
		boostPower: AtomicDouble
	): Boolean {
		if (weapon is AmmoConsumingWeaponSubsystem &&
			ship.magazines.none { it.isAmmoAvailable(weapon.getRequiredAmmo()) }
		) {
			ship.onlinePassengers.forEach { player ->
				player.alertActionMessage(
					"Insufficient ammunition"
				)
			}

			return true
		}

		if (!isPowerAvailable(weapon, boostPower)) {
			ship.controller.debug("out of power")
			return true
		}

		return false
	}

	private fun consumeResources(
		weapon: WeaponSubsystem,
		boostPower: AtomicDouble,
		ship: ActiveStarship
	) {
		check(tryConsumePower(weapon, boostPower))

		if (weapon is AmmoConsumingWeaponSubsystem) {
			check(ship.magazines.any { it.tryConsumeAmmo(weapon.getRequiredAmmo()) })
		}
	}

	private fun isPowerAvailable(weapon: WeaponSubsystem, boostPower: AtomicDouble): Boolean {
		val reactor = weapon.starship.reactor
		val powerUsage = weapon.powerUsage.toDouble()
		return when {
			weapon is HeavyWeaponSubsystem -> boostPower.get() >= powerUsage
			else -> reactor.weaponCapacitor.isAvailable(powerUsage)
		}
	}

	private fun tryConsumePower(weapon: WeaponSubsystem, boostPower: AtomicDouble): Boolean {
		val reactor = weapon.starship.reactor

		val powerUsage = weapon.powerUsage.toDouble()

		if (weapon is HeavyWeaponSubsystem) {
			if (boostPower.get() < powerUsage) {
				return false
			}

			boostPower.addAndGet(-powerUsage)
			return true
		}

		return reactor.weaponCapacitor.tryConsume(powerUsage)
	}
}
