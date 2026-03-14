package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.DyedItemColor
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.starship.DoomsdayDeviceBalancing
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.features.client.display.modular.ItemDisplayContainer
import net.horizonsend.ion.server.features.nations.utils.toPlayersInRadius
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.destruction.SinkAnimation
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.DoomsdayDeviceWeaponSubsystem.Companion.WARM_UP_TIME_SECONDS
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.DoomsdayDeviceProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.features.transport.items.util.DYEABLE_CUBE_MONO
import net.horizonsend.ion.server.miscellaneous.playDirectionalStarshipSound
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.minecraft.util.Brightness
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.function.Supplier

class DoomsdayDeviceWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace,
) : CannonWeaponSubsystem<DoomsdayDeviceBalancing>(starship, pos, face, starship.balancingManager.getWeaponSupplier(DoomsdayDeviceWeaponSubsystem::class)), HeavyWeaponSubsystem, AmmoConsumingWeaponSubsystem {
	override val boostChargeNanos: Long get() = balancing.boostChargeNanos

    companion object {
        const val WARM_UP_TIME_SECONDS = 4
    }

    override val length: Int = 11

    override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
		toPlayersInRadius(loc, balancing.projectile.range * 20.0) { player ->
			playDirectionalStarshipSound(loc, player, balancing.projectile.fireSoundNear, balancing.projectile.fireSoundNear, balancing.projectile.range)
		}

		Tasks.async {
			repeat(3) {
				DoomsdayDeviceChargeAnimation(getFirePos().toVector(), shooter.color, { getFirePos() }).schedule()
			}
		}

		Tasks.syncDelay(20 * WARM_UP_TIME_SECONDS.toLong()) {
            val newFirePos = getFirePos()
            DoomsdayDeviceProjectile(StarshipProjectileSource(starship), getName(), newFirePos.toLocation(loc.world), dir, shooter).fire()
        }
    }

	override fun getName(): Component {
		return Component.text("Doomsday Device")
	}

	override fun isRequiredAmmo(item: ItemStack): Boolean {
		return requireCustomItem(item, CustomItemKeys.CHARGED_SHELL.getValue(), 1)
	}

	override fun consumeAmmo(itemStack: ItemStack) {
		consumeItem(itemStack, 1)
	}

	inner class DoomsdayDeviceChargeAnimation(
		origin: Vector,
		color: Color,
		posSupplier: Supplier<Vec3i>
	) : BukkitRunnable() {
		val item = DYEABLE_CUBE_MONO.construct { t -> t.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(color, false)) }
		val rotationAxis = Vector.getRandom()

		val block = object : SinkAnimation.ColoredSinkAnimationBlock(
			duration = (WARM_UP_TIME_SECONDS * 20).toLong(),
			wrapper = ItemDisplayContainer(
				world = starship.world,
				initPosition = origin,
				initHeading = BlockFace.NORTH.direction,
				initScale = 1.0f,
				item = item,
			),
			direction = Vector(0, 0, 0),
			initialScale = 1.0,
			finalScale = 12.0,
			rotationAxis = rotationAxis,
			rotationDegrees = 10.0,
			colors = mapOf(
				color.mixColors(Color.BLACK) to 5,
				color to 2,
			),
			motionAdjuster = {
				wrapper.position = posSupplier.get().toVector()
				rotationDegrees *= 1.01
			}
		) {}

		override fun run() {
			block.update()
			if (block.checkDead()) cancel()
		}

		fun schedule() = runTaskTimerAsynchronously(IonServer, 1L, 1L)
	}
}
