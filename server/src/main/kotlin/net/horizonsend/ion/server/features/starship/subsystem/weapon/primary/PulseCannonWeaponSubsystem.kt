package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PulseLaserProjectile
import net.horizonsend.ion.server.miscellaneous.utils.STAINED_GLASS_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit


class PulseCannonWeaponSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace) :
	CannonWeaponSubsystem(starship, pos, face) {
	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.pulseCannon
	companion object {
		private val colorMap: Map<Material, Color> = STAINED_GLASS_TYPES
			.associateWith { DyeColor.valueOf(it.name.removeSuffix("_STAINED_GLASS")).color }
	}

	override val powerUsage: Int = balancing.powerUsage
	override val length: Int = balancing.length
	override val angleRadiansHorizontal: Double = Math.toRadians(balancing.angleRadiansHorizontal)
	override val angleRadiansVertical: Double = Math.toRadians(balancing.angleRadiansVertical) // unrestricted
	override val convergeDist: Double = balancing.convergeDistance
	override val extraDistance: Int = balancing.extraDistance
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(balancing.fireCooldownMillis)

	private val color: Color = getColor(starship, pos, face)

	private fun getColor(starship: ActiveStarship, pos: Vec3i, face: BlockFace): Color {
		val glassBlock = starship.world.getBlockAt(pos.x, pos.y, pos.z).getRelative(face)
		return when (val material = glassBlock.type) {
			Material.BLACK_STAINED_GLASS -> Color.WHITE
			else -> colorMap.getValue(material)
		}
	}

	override fun fire(
        loc: Location,
        dir: Vector,
        shooter: Damager,
        target: Vector?
	) {
		PulseLaserProjectile(starship, getName(), loc, dir, color, shooter).fire()
	}

	override fun getName(): Component {
		return Component.text("Pulse Cannon")
	}
}
