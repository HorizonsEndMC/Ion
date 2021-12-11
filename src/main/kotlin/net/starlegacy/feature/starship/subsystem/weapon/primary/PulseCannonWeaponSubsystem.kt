package net.starlegacy.feature.starship.subsystem.weapon.primary

import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.CannonWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.projectile.PulseLaserProjectile
import net.starlegacy.util.STAINED_GLASS_TYPES
import net.starlegacy.util.Vec3i
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class PulseCannonWeaponSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace) :
    CannonWeaponSubsystem(starship, pos, face) {
    companion object {
        private val colorMap: Map<Material, Color> = STAINED_GLASS_TYPES
            .associateWith { DyeColor.valueOf(it.name.removeSuffix("_STAINED_GLASS")).color }
    }

    override val powerUsage: Int = 1800
    override val length: Int = 2
    override val angleRadians: Double = Math.toRadians(180.0) // unrestricted
    override val convergeDist: Double = 16.0
    override val extraDistance: Int = 2

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
        shooter: Player,
        target: Vector?
    ) {
        PulseLaserProjectile(starship, loc, dir, color, shooter).fire()
    }
}
