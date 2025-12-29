package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.configuration.starship.WebifierBalancing
import net.horizonsend.ion.server.features.nations.utils.toPlayersInRadius
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.WebifierProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.playDirectionalStarshipSound
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.FaceAttachable
import org.bukkit.block.data.type.Grindstone
import org.bukkit.block.data.type.Hopper
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class WebifierWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace
) : CannonWeaponSubsystem<WebifierBalancing>(starship, pos, face, starship.balancingManager.getWeaponSupplier(WebifierWeaponSubsystem::class)), HeavyWeaponSubsystem, AmmoConsumingWeaponSubsystem {
	override val length: Int = 7

	override val boostChargeNanos: Long get() = balancing.boostChargeNanos

	companion object {
		private const val WARM_UP_TIME_SECONDS = 0.5
	}

	override fun isAcceptableDirection(face: BlockFace) = true

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {

		toPlayersInRadius(loc, balancing.projectile.range * 20.0) { player ->
			playDirectionalStarshipSound(loc, player, balancing.projectile.fireSoundNear, balancing.projectile.fireSoundNear, balancing.projectile.range)
		}

		fixDirections(loc)

		val newFirePos = getFirePos().toCenterVector()
		WebifierProjectile(StarshipProjectileSource(starship), getName(), newFirePos.toLocation(loc.world), dir, shooter).fire()
	}

	private fun fixDirections(loc: Location) {
		fixGrindstoneData(loc)
	}

	private fun fixGrindstoneData(loc: Location) {
		val grindstone = pos.toLocation(loc.world).add(face.direction.multiply(6)).block
		val grindstoneData = grindstone.blockData as Grindstone
		grindstoneData.attachedFace = FaceAttachable.AttachedFace.WALL
		grindstoneData.facing = face
		grindstone.setBlockData(grindstoneData, false)
	}


	override fun getName(): Component {
		return Component.text("Webifier")
	}

	override fun isRequiredAmmo(item: ItemStack): Boolean {
		return requireMaterial(item, Material.PRISMARINE_CRYSTALS, 4)
	}

	override fun consumeAmmo(itemStack: ItemStack) {
		consumeItem(itemStack, 4)
	}
}
