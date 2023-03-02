package net.starlegacy.feature.starship.subsystem.weapon

import com.google.common.collect.HashMultimap
import com.google.common.util.concurrent.AtomicDouble
import net.horizonsend.ion.common.extensions.alertActionMessage
import net.horizonsend.ion.common.extensions.userErrorActionMessage
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import org.bukkit.entity.Player
import org.bukkit.util.Vector

object StarshipWeapons {
	interface QueuedShot {
		val weapon: WeaponSubsystem

		fun shoot()
	}

	data class ManualQueuedShot(
		override val weapon: WeaponSubsystem,
		val shooter: Player,
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
		val target: Player,
		val dir: Vector
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
			val heavyWeaponTypes =
				queuedShots.filter { it.weapon is HeavyWeaponSubsystem }.map { it.weapon.name }.distinct()
			if (heavyWeaponTypes.count() > 1) {
				ship.onlinePassengers.forEach { player ->
					player.userErrorActionMessage(
						"You can only fire one type of heavy weapon at a time!"
					)
				}

				return
			}

			val heavyWeaponType = heavyWeaponTypes.single()
			val newWarmup = queuedShots
				.filter { it.weapon is HeavyWeaponSubsystem }
				.maxOf { (it.weapon as HeavyWeaponSubsystem).boostChargeNanos }
			val output = ship.reactor.heavyWeaponBooster.boost(heavyWeaponType, newWarmup)
			boostPower.set(output)
		}

		val firedCounts = HashMultimap.create<String, WeaponSubsystem>()
		for (shot in queuedShots.sortedBy { it.weapon.lastFire }) {
			val weapon = shot.weapon

			val maxPerShot = weapon.getMaxPerShot()

			val firedSet = firedCounts[weapon.name]
			if (maxPerShot != null && firedSet.size >= maxPerShot) {
				continue
			}

			if (resourcesUnavailable(weapon, ship, boostPower)) {
				continue
			}

			shot.shoot()

			consumeResources(weapon, boostPower, ship)

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
