package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.configuration.StarshipWeapons.PhaserBalancing
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PhaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.FaceAttachable
import org.bukkit.block.data.type.Grindstone
import org.bukkit.block.data.type.Hopper
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class PhaserWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace
) : CannonWeaponSubsystem<PhaserBalancing>(starship, pos, face, starship.balancingManager.getSupplier()), HeavyWeaponSubsystem, AmmoConsumingWeaponSubsystem {
	override val length: Int = 8

	override val boostChargeNanos: Long get() = balancing.boostChargeNanos

	companion object {
		private const val WARM_UP_TIME_SECONDS = 0.5
	}

	override fun isAcceptableDirection(face: BlockFace) = true

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
		if (starship.initialBlockCount > 12000) {
			starship.userError("You can't fire phasers on a ship larger than 12000 blocks!")
			return
		}

		loc.world.players.forEach {
			if (it.location.distance(loc) < balancing.range) {
				shooter.playSound(Sound.sound(Key.key("horizonsend:starship.weapon.plasma_cannon.shoot"), Sound.Source.PLAYER, 1.0f, 2.0f))
			}
		}

		fixDirections(loc)

		Tasks.syncDelay((20.0 * WARM_UP_TIME_SECONDS).toLong()) {
			val newFirePos = getFirePos().toCenterVector()
			PhaserProjectile(StarshipProjectileSource(starship), getName(), newFirePos.toLocation(loc.world), dir, shooter).fire()
		}
	}

	private fun fixDirections(loc: Location) {
		fixGrindstoneData(loc)
		fixHopperData(loc)
	}

	private fun fixGrindstoneData(loc: Location) {
		val grindstone = pos.toLocation(loc.world).add(face.direction.multiply(7)).block
		val grindstoneData = grindstone.blockData as Grindstone
		grindstoneData.attachedFace = FaceAttachable.AttachedFace.WALL
		grindstoneData.facing = face
		grindstone.setBlockData(grindstoneData, false)
	}

	private fun fixHopperData(loc: Location) {
		val hopper = pos.toLocation(loc.world).add(face.direction.multiply(6)).block
		val hopperData = hopper.blockData as Hopper
		hopperData.isEnabled = false
		hopperData.facing = face
		hopper.setBlockData(hopperData, false)
	}

	override fun getName(): Component {
		return Component.text("Phaser")
	}

	override fun isRequiredAmmo(item: ItemStack): Boolean {
		return requireMaterial(item, Material.PRISMARINE_CRYSTALS, 4)
	}

	override fun consumeAmmo(itemStack: ItemStack) {
		consumeItem(itemStack, 4)
	}
}
