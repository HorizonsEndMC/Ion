package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.QuadTurretMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import java.util.concurrent.TimeUnit

class QuadTurretWeaponSubsystem(
    ship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace,
    override val multiblock: QuadTurretMultiblock
) : TurretWeaponSubsystem(ship, pos, face), PermissionWeaponSubsystem, AmmoConsumingWeaponSubsystem {
	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.quadTurret
	override val inaccuracyRadians: Double get() = Math.toRadians(balancing.inaccuracyRadians)
	override val powerUsage: Int get() = balancing.powerUsage
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(balancing.fireCooldownMillis)
	override fun getMaxPerShot(): Int = balancing.maxPerShot

	override val permission: String = "ion.multiblock.quadturret"

	override fun getRequiredAmmo(): ItemStack {
		return CustomItemRegistry.LOADED_SHELL.constructItemStack()
	}

	override fun getName(): Component {
		return Component.text("Quad Turret")
	}
}
