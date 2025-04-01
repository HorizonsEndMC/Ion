package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PulseLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.STAINED_GLASS_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector


class PulseCannonWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
) : CannonWeaponSubsystem<StarshipWeapons.PulseCannonBalancing>(starship, pos, face, starship.balancingManager.getSupplier()) {
	override val length: Int = 2

	companion object {
		private val colorMap: Map<Material, Color> = STAINED_GLASS_TYPES
			.associateWith { DyeColor.valueOf(it.name.removeSuffix("_STAINED_GLASS")).color }
	}

	private val color: Color = getColor(starship, pos, face)

	private fun getColor(starship: ActiveStarship, pos: Vec3i, face: BlockFace): Color {
		val glassBlock = starship.world.getBlockAt(pos.x, pos.y, pos.z).getRelative(face)
		return when (val material = glassBlock.type) {
			Material.BLACK_STAINED_GLASS -> Color.WHITE
			Material.GLASS -> Color.WHITE
			else -> colorMap.getValue(material)
		}
	}

	override fun fire(
        loc: Location,
        dir: Vector,
        shooter: Damager,
        target: Vector
	) {
		PulseLaserProjectile(StarshipProjectileSource(starship), getName(), loc, dir, color, shooter).fire()
	}

	override fun getName(): Component {
		return Component.text("Pulse Cannon")
	}
}
