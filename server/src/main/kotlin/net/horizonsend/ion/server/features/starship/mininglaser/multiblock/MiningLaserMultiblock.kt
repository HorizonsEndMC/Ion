package net.horizonsend.ion.server.features.starship.mininglaser.multiblock

import net.horizonsend.ion.server.features.starship.mininglaser.MiningLaserSubsystem
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import net.starlegacy.feature.multiblock.starshipweapon.StarshipWeaponMultiblock
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getFacing
import net.starlegacy.util.leftFace
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

abstract class MiningLaserMultiblock : StarshipWeaponMultiblock<MiningLaserSubsystem>(), PowerStoringMultiblock {
	override val name = "mininglaser"
	abstract val range: Double

	abstract val beamOrigin: Triple<Int, Int, Int>
	abstract val mineRadius: Int
	abstract val beamCount: Int
	abstract val maxBroken: Int
	abstract val sound: String
	abstract val side: BlockFace
	abstract val tier: Int

	fun getOutput(sign: Sign): Inventory {
		val direction = sign.getFacing().oppositeFace

		return (
			sign.block.getRelative(direction)
				.getRelative(side.oppositeFace)
				.getRelative(direction.leftFace)
				.getState(false) as InventoryHolder
			).inventory
	}

	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): MiningLaserSubsystem {
		if (starship is ActivePlayerStarship) {
			return MiningLaserSubsystem(starship, pos, face, this)
		} else {
			throw IllegalStateException("Mining lasers can be only used on Player starships")
		}
	}

	override val inputComputerOffset = Vec3i(0, -1, 0)

	override fun onTransformSign(player: Player, sign: Sign) {
		super<PowerStoringMultiblock>.onTransformSign(player, sign)
	}

	abstract fun getFirePointOffset(): Vec3i
}
