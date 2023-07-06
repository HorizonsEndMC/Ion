package net.horizonsend.ion.server.features.multiblock.landsieges.tank

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.landsieges.IFVCannonMultiblock
import net.starlegacy.feature.starship.StarshipType
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.RestrictedWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.TurretWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack

class IFVCannonSubsystem(ship: ActiveStarship, pos: Vec3i, face: BlockFace) : TurretWeaponSubsystem(ship, pos, face), RestrictedWeaponSubsystem, AmmoConsumingWeaponSubsystem {
	override val multiblock get() = IFVCannonMultiblock
	override val inaccuracyRadians get() = IonServer.balancing.starshipWeapons.ifvCannon.inaccuracyRadians
	override val powerUsage get() = IonServer.balancing.starshipWeapons.ifvCannon.powerUsage
	override fun isRestricted(starship: ActiveStarship) =
		starship.type != StarshipType.IFV

	override fun getRequiredAmmo(): ItemStack = ItemStack(Material.STONE)
}
