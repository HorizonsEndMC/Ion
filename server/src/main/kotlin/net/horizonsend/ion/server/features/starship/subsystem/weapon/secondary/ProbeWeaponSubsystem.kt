package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_BLUE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.lineBreakWithCenterText
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.configuration.starship.ProbeBalancing
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ProbeProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.awt.Color
import kotlin.math.atan2

class ProbeWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace
) : CannonWeaponSubsystem<ProbeBalancing>(starship, pos, face, starship.balancingManager.getWeaponSupplier(ProbeWeaponSubsystem::class)), HeavyWeaponSubsystem, AmmoConsumingWeaponSubsystem {
	override val length: Int = 4

	override val boostChargeNanos: Long get() = balancing.boostChargeNanos

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
		ProbeProjectile(StarshipProjectileSource(starship), getName(), loc, dir, shooter).fire()

		Tasks.syncDelay(60L) {
			shooter.sendMessage(lineBreakWithCenterText(text("[COMBAT PROBE SCAN START]", HE_LIGHT_ORANGE)))
			val ships = ActiveStarships.all().filter {
				it.controller is PlayerController
				it.world == starship.world
				it.centerOfMass.distanceSquared(starship.centerOfMass) < 56250000 //7.5km squared
				it.type != StarshipType.RECON_STARFIGHTER
			}
			val totalShips = ships.size
			for (ship in ships) {
				val name = ship.playerPilot?.name ?: "None"
				val pilot: Component = text(name, HE_LIGHT_BLUE) //TODO: Change this to change on frontier nation color
				val starshipName: Component = text(ship.type.displayName, TextColor.fromHexString(ship.type.color))

				val dx: Double =
					ship.centerOfMass.toLocation(ship.world).x - starship.centerOfMass.toLocation(starship.world).x
				val dz: Double =
					ship.centerOfMass.toLocation(ship.world).z - starship.centerOfMass.toLocation(starship.world).z
				var angle = Math.toDegrees(atan2(dz, dx)) - 90
				angle = (angle % 360 + 360) % 360

				val direction: String = when {

					(angle !in 45.0..<315.0) -> "South"
					(angle in 45.0..<135.0) -> "West"
					(angle in 135.0..<225.0) -> "North"
					(angle in 225.0..<315.0) -> "East"
					else -> "Narnia"
				}

				val distance =
					starship.centerOfMass.toLocation(starship.world).distance(ship.centerOfMass.toLocation(ship.world)).toInt()
				val distanceColor = when {
					distance < 500 -> RED
					distance < 1500 -> YELLOW
					distance < 2500 -> GREEN
					else -> DARK_GREEN
				}

				val line = template(
					"{0} piloted by {1} {2}m to the {3}.",
					color = HE_LIGHT_GRAY,
					paramColor = HE_LIGHT_GRAY,
					useQuotesAroundObjects = true,
					starshipName,
					pilot,
					text(distance, distanceColor),
					text(direction, HE_MEDIUM_GRAY)
				)
				shooter.sendMessage(line)
			}
			shooter.sendMessage(net.horizonsend.ion.common.utils.text.lineBreak(47))
			shooter.sendMessage(ofChildren(text("Total Ships", HE_MEDIUM_GRAY), text(": ", HE_DARK_GRAY), text(totalShips, HE_LIGHT_BLUE)))
			shooter.sendMessage(lineBreakWithCenterText(text("[COMBAT PROBE SCAN END]", HE_DARK_GRAY)))
		}
	}

	override fun getName(): Component {
		return text("Probe")
	}

	override fun isRequiredAmmo(item: ItemStack): Boolean {
		return requireMaterial(item, Material.REDSTONE, 2)
	}

	override fun consumeAmmo(itemStack: ItemStack) {
		consumeItem(itemStack, 2)
	}
}
