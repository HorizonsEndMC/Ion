package net.starlegacy.feature.starship.subsystem.weapon.secondary

import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.CannonWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.projectile.PhaserProjectile
import net.starlegacy.util.Vec3i
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.FaceAttachable
import org.bukkit.block.data.type.Grindstone
import org.bukkit.block.data.type.Hopper
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class PhaserWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace
) : CannonWeaponSubsystem(starship, pos, face),
    HeavyWeaponSubsystem,
    AmmoConsumingWeaponSubsystem {
    override val length: Int = 8
    override val convergeDist: Double = 0.0
    override val extraDistance: Int = 0
    override val angleRadians: Double = Math.toRadians(180.0) // unrestricted
    override val powerUsage: Int = 50000
    override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(3L)

    override fun isAcceptableDirection(face: BlockFace) = true

    override fun fire(loc: Location, dir: Vector, shooter: Player, target: Vector?) {
        fixDirections(loc)
        PhaserProjectile(starship, loc, dir, shooter).fire()
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

    override fun getRequiredAmmo(): ItemStack {
        return ItemStack(Material.PRISMARINE_CRYSTALS, 4)
    }
}
