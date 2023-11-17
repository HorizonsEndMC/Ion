package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PulseLaserProjectile
import net.horizonsend.ion.server.miscellaneous.utils.STAINED_GLASS_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class PulseCannonWeaponSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace) :
	CannonWeaponSubsystem(starship, pos, face) {
	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.pulseCannon
	companion object {
		private val colorMap: Map<Material, Color> = STAINED_GLASS_TYPES
			.associateWith { DyeColor.valueOf(it.name.removeSuffix("_STAINED_GLASS")).color }
	}

	override val powerUsage: Int = balancing.powerUsage
	override val length: Int = balancing.length
	override val angleRadians: Double = Math.toRadians(balancing.angleRadians) // unrestricted
	override val convergeDist: Double = balancing.convergeDistance
	override val extraDistance: Int = balancing.extraDistance

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
		if (starship.type != StarshipType.GUNSHIP || starship.initialBlockCount < 1000) {
			starship.controller.userError("You can only use Pulse Cannons above 1000 blocks on Gunships!")
			return
		}

		PulseLaserProjectile(starship, loc, dir, color, shooter).fire()
	}
}
