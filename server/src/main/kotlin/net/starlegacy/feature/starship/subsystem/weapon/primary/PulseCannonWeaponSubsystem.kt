package net.starlegacy.feature.starship.subsystem.weapon.primary

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.starlegacy.feature.starship.StarshipType
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.CannonWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.projectile.PulseLaserProjectile
import net.starlegacy.util.STAINED_GLASS_TYPES
import net.horizonsend.ion.server.miscellaneous.Vec3i
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class PulseCannonWeaponSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace) :
	CannonWeaponSubsystem(starship, pos, face) {
	companion object {
		private val colorMap: Map<Material, Color> = STAINED_GLASS_TYPES
			.associateWith { DyeColor.valueOf(it.name.removeSuffix("_STAINED_GLASS")).color }
	}

	override val powerUsage: Int = IonServer.balancing.starshipWeapons.pulseCannon.powerUsage
	override val length: Int = IonServer.balancing.starshipWeapons.pulseCannon.length
	override val angleRadians: Double =
		Math.toRadians(IonServer.balancing.starshipWeapons.pulseCannon.angleRadians) // unrestricted
	override val convergeDist: Double = IonServer.balancing.starshipWeapons.pulseCannon.convergeDistance
	override val extraDistance: Int = IonServer.balancing.starshipWeapons.pulseCannon.extraDistance

	private val color: Color = getColor(starship, pos, face)

	private fun getColor(starship: ActiveStarship, pos: Vec3i, face: BlockFace): Color {
		val glassBlock = starship.serverLevel.world.getBlockAt(pos.x, pos.y, pos.z).getRelative(face)
		return when (val material = glassBlock.type) {
			Material.BLACK_STAINED_GLASS -> Color.WHITE
			else -> colorMap.getValue(material)
		}
	}

	override fun fire(
        loc: Location,
        dir: Vector,
        shooter: Controller,
        target: Vector?
	) {
		if (starship.type != StarshipType.GUNSHIP) {
			starship.controller?.userError("You can use Pulse Cannons only on Gunships!")
			return
		}

		PulseLaserProjectile(starship, loc, dir, color, shooter).fire()
	}
}
