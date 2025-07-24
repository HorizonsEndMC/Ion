package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PhaserProjectile
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
import java.util.concurrent.TimeUnit
import kotlin.math.min

class PhaserWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace
) : CannonWeaponSubsystem(starship, pos, face),
	HeavyWeaponSubsystem,
	AmmoConsumingWeaponSubsystem {

	companion object {
		private const val WARM_UP_TIME_SECONDS = 0.5
	}

	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.phaser
	override val length: Int = balancing.length
	override val convergeDist: Double = balancing.convergeDistance
	override val extraDistance: Int = balancing.extraDistance
	override val angleRadiansHorizontal: Double = Math.toRadians(balancing.angleRadiansHorizontal)
	override val angleRadiansVertical: Double = Math.toRadians(balancing.angleRadiansVertical) // unrestricted
	override val powerUsage: Int = balancing.powerUsage
	override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(balancing.boostChargeSeconds)

	override fun isAcceptableDirection(face: BlockFace) = true

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
		if (starship.initialBlockCount > 12000) {
			starship.userError("You can't fire phasers on a ship larger than 12000 blocks!")
			return
		}

		loc.world.players.forEach { player ->
			val distance = player.location.distance(loc)
			val dir = Vector(loc.x - player.location.x, loc.y - player.location.y, loc.z - player.location.z)
			val offsetDistance = min(distance, 16.0)
			val soundLoc = player.location.add(dir.normalize().multiply(offsetDistance))
			if (distance < balancing.range * 20) {
				player.playSound(Sound.sound(Key.key(balancing.soundFireNear.key), balancing.soundFireNear.source, balancing.soundFireNear.volume * nearSoundVolumeMod(distance), balancing.soundFireNear.pitch), soundLoc.x, soundLoc.y, soundLoc.z)
				player.playSound(Sound.sound(Key.key(balancing.soundFireFar.key), balancing.soundFireFar.source, balancing.soundFireFar.volume * farSoundVolumeMod(distance), balancing.soundFireFar.pitch), soundLoc.x, soundLoc.y, soundLoc.z)
			}
		}

		fixDirections(loc)

		Tasks.syncDelay((20.0 * WARM_UP_TIME_SECONDS).toLong()) {
			val newFirePos = getFirePos().toCenterVector()
			PhaserProjectile(starship, getName(), newFirePos.toLocation(loc.world), dir, shooter).fire()
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

	private fun nearSoundVolumeMod(distance: Double): Float {
		val range = balancing.range
		val normalized = distance / range
		return when (distance.toInt()) {
			in 0 until (range * 0.5).toInt() -> 1f
			// -0.666667x + 1.33333
			in (range * 0.5).toInt() until (range * 2).toInt() -> ((-0.666667 * normalized) + 1.33333).toFloat()
			else -> 0f
		}
	}

	private fun farSoundVolumeMod(distance: Double): Float {
		val range = balancing.range
		val normalized = distance / range
		return when (distance.toInt()) {
			in 0 until (range * 2).toInt() -> 1f
			// -0.0555556x + 1.11111
			in (range * 2).toInt() until (range * 20).toInt() -> ((-0.0555556 * normalized) + 1.11111).toFloat()
			else -> 0f
		}
	}
}
