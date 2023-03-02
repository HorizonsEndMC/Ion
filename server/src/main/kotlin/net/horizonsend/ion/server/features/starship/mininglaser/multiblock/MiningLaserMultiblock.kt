package net.horizonsend.ion.server.features.starship.mininglaser.multiblock

import net.horizonsend.ion.server.features.starship.mininglaser.MiningLaserSubsystem
import net.starlegacy.feature.multiblock.starshipweapon.StarshipWeaponMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace

abstract class MiningLaserMultiblock : StarshipWeaponMultiblock<MiningLaserSubsystem>() {
	override val name = "mininglaser"
	abstract val range: Int
	abstract val axis: Triple<Int, Int, Int>
	abstract val circleRadius: Int
	abstract val beamCount: Int

	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): MiningLaserSubsystem {
		return MiningLaserSubsystem(starship, pos, face, this)
	}

	abstract fun getFirePointOffset(): Vec3i

	abstract fun upDownFace(): BlockFace
}
