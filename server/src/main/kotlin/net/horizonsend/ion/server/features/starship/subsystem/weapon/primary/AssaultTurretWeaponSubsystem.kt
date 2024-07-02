package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.multiblock.starshipweapon.turret.AssaultTurretMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import java.util.concurrent.TimeUnit

class AssaultTurretWeaponSubsystem(
		ship: ActiveStarship,
		pos: Vec3i,
		face: BlockFace,
		override val multiblock: AssaultTurretMultiblock
) : TurretWeaponSubsystem(ship, pos, face), AmmoConsumingWeaponSubsystem {
	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.assaultTurret

	override val inaccuracyRadians: Double get() = Math.toRadians(balancing.inaccuracyRadians)
	override val powerUsage: Int get() = balancing.powerUsage
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(balancing.fireCooldownMillis)
	override fun getRequiredAmmo(): ItemStack {
		return CustomItems.LOADED_ASSAULT_SHELL.constructItemStack()
	}

}
