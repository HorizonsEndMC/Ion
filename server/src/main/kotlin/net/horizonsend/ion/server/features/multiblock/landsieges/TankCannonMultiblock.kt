package net.horizonsend.ion.server.features.multiblock.landsieges

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.landsieges.tank.TankCannonSubsystem
import net.horizonsend.ion.server.features.multiblock.starshipweapon.StarshipWeaponMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.Vec3i
import org.bukkit.Material
import org.bukkit.block.BlockFace

object TankCannonMultiblock: Multiblock(), StarshipWeaponMultiblock<TankCannonSubsystem> {
	override val name = "tankcannon"
	override val signText = createSignText("&8Tank cannon", null, null, null)

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).furnace()
		at(+0, +0, +1).ironBlock()

		at(+0, +0, +2).ironBlock()
		at(+0, -1, +2).dispenser()

		at(+0, +0, +3).ironBlock()
		at(+0, +0, +4).anyType(Material.GRINDSTONE)
		at(+0, +0, +5).anyType(Material.GRINDSTONE)

		at(+0, +0, +6).endRod()
		at(+0, +0, +7).endRod()
		at(+0, +0, +8).endRod()
	}

	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace) =
		TankCannonSubsystem(starship, pos, face)
}
