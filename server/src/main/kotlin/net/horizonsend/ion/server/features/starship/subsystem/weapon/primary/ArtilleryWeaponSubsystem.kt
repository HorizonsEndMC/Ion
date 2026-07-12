package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.configuration.starship.ArtilleryBalancing
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.LOADED_HELIX_SHELL
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.LOADED_SHELL
import net.horizonsend.ion.server.features.nations.utils.toPlayersInRadius
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ArtilleryProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.playDirectionalStarshipSound
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector


class ArtilleryWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
) : CannonWeaponSubsystem<ArtilleryBalancing>(starship, pos, face, starship.balancingManager.getWeaponSupplier(ArtilleryWeaponSubsystem::class)), AmmoConsumingWeaponSubsystem {
	override val length: Int = 3
	override val extraDistance: Int = 3

	override val fireCooldownNanos: Long = balancing.fireCooldownNanos

	companion object {
		private const val WARM_UP_TIME_SECONDS = 0.0
	}

	override fun isAcceptableDirection(face: BlockFace) = true

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
		/*if (starship.initialBlockCount > 12000) {
			starship.userError("You can't fire phasers on a ship larger than 12000 blocks!")
			return
		}*/

		toPlayersInRadius(loc, balancing.projectile.range * 20.0) { player ->
			playDirectionalStarshipSound(loc, player, balancing.projectile.fireSoundNear, balancing.projectile.fireSoundNear, balancing.projectile.range)
		}

		//Tasks.syncDelay((20.0 * WARM_UP_TIME_SECONDS).toLong()) {
			val newFirePos = getFirePos().toCenterVector()
			ArtilleryProjectile(StarshipProjectileSource(starship), getName(), newFirePos.toLocation(loc.world), dir, shooter).fire()
		//}
	}

	override fun getName(): Component {
		return Component.text("Artillery")
	}

	override fun isRequiredAmmo(item: ItemStack): Boolean {
		return requireCustomItem(item, LOADED_HELIX_SHELL.getValue(), 1)
	}

	override fun consumeAmmo(itemStack: ItemStack) {
		consumeItem(itemStack, 1)
	}
}

