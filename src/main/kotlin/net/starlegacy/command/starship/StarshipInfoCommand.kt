package net.starlegacy.command.starship

import co.aikar.commands.annotation.CommandAlias
import kotlin.math.round
import kotlin.math.roundToInt
import net.starlegacy.command.SLCommand
import net.starlegacy.feature.starship.StarshipDetection
import net.starlegacy.feature.starship.factory.StarshipFactories
import net.starlegacy.feature.starship.hyperspace.Hyperspace
import net.starlegacy.util.Vec3i
import net.starlegacy.util.isConcrete
import net.starlegacy.util.msg
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player

object StarshipInfoCommand : SLCommand() {
	@CommandAlias("starshipinfo|starship")
	fun onExecute(p: Player) {
		val ship = getStarshipPiloting(p)

		val blocks = ship.blocks.map { Vec3i(it) }.associateWith { it.toLocation(ship.world).block.state }

		val size = ship.blockCount

		p msg "&b${ship.data.type.displayName} &f($size blocks)"
		p msg "   &7Mass:&f ${ship.mass}"
		p msg "   &7World:&f ${ship.world.name}"
		p msg "   &7Pilot:&f ${ship.pilot?.name}"

		val passengers = ship.onlinePassengers.map { it.name }.joinToString()
		if (passengers.any()) {
			p msg "   &7Passengers:&f $passengers"
		}

		p msg "   &7Carbyne Percent:&f ${createPercent(blocks.values.count { it.type.isConcrete }, size)}"

		val inventoryCount = blocks.values.count { StarshipDetection.isInventory(it.type) } +
			blocks.values.count { it.type == Material.CHEST || it.type == Material.TRAPPED_CHEST } * 2
		p msg "   &7Inventory Percent:&f ${createPercent(inventoryCount, size)}"
		val hyperdrive = Hyperspace.findHyperdrive(ship)
		if (hyperdrive != null) {
			val hyperdriveClass = hyperdrive.multiblock.hyperdriveClass
			val vector = hyperdrive.pos
			p msg "   &7Hyperdrive:&f Class $hyperdriveClass at $vector"
		}
		if (!ship.weaponSets.isEmpty) {
			p msg "   &7Controlled Weapon Sets:"
			for (gunner in ship.weaponSetSelections.mapNotNull { Bukkit.getPlayer(it.key) }) {
				val weaponSet = ship.weaponSetSelections[gunner.uniqueId]
				p msg "         &6${gunner.name}:&c $weaponSet"
			}
		}
		val powerOutput = ship.reactor.output
		p msg "   &7Power Output:&f $powerOutput"
		p msg "   &7Weapon Capacitor Capacity:&f ${ship.reactor.weaponCapacitor.capacity}"
		p msg "   &7Heavy Weapon Booster Output:&f ${ship.reactor.heavyWeaponBooster.output}"

		p msg "   &7Power Division:"
		val powerTypes = listOf(
			"Shield" to ship.reactor.powerDistributor.thrusterPortion,
			"Weapon" to ship.reactor.powerDistributor.weaponPortion,
			"Thruster" to ship.reactor.powerDistributor.thrusterPortion
		)
		for ((name, percent) in powerTypes) {
			val percentRounded = (percent * 100).roundToInt()
			val currentPower = percent * powerOutput
			p msg "      &7$name:&e $percentRounded% ($currentPower)"
		}

		if (ship.autoTurretTargets.isNotEmpty()) {
			p msg "   &7Auto Turret Targets:"
			for ((set, targetId) in ship.autoTurretTargets) {
				val targetName = Bukkit.getOfflinePlayer(targetId).name
				p msg "      &6$set:&c $targetName"
			}
		}

		if (ship.shields.isNotEmpty()) {
			p msg "   &7Shields:"
			for (shield in ship.shields) {
				val percent = createPercent(shield.power, shield.maxPower)
				val shieldClass = shield.multiblock.signText[3]
				p msg "      &7${shield.name}:&b $percent ($shieldClass&b)"
			}
			p msg "   &7Shield Regen Efficiency:&b ${ship.shieldEfficiency}"
			p msg "    &7Maximum Shields the starship can handle:&b ${ship.maxShields}   "
		}

		p msg "   &7Hull Integrity:&f ${ship.hullIntegrity().times(100).roundToInt()}%"
		p msg "   &7Center of Mass:&f ${ship.centerOfMass}"

		val worth = blocks.values
			.sumOf { StarshipFactories.getPrice(it.blockData) ?: 0.0 }
			.roundToInt()

		p msg "   &7Worth:&f ~$worth"
	}

	// creates a percent that goes down to the tens place
	private fun createPercent(numerator: Int, denominator: Int): String =
		createPercent(numerator.toDouble() / denominator.toDouble())

	private fun createPercent(fraction: Double) = "${round(fraction * 1000) / 10}%"
}
