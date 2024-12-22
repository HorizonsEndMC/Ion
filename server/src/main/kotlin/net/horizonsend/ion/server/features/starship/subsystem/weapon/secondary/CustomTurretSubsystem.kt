package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.TurretBaseMultiblock
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.StarshipSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import java.util.ArrayDeque
import java.util.LinkedList

class CustomTurretSubsystem(starship: Starship, pos: Vec3i, override var face: BlockFace) : StarshipSubsystem(starship, pos), DirectionalSubsystem {
	override fun isIntact(): Boolean {
		val block = getBlockIfLoaded(starship.world, pos.x, pos.y, pos.z) ?: return false
		return TurretBaseMultiblock.shape.checkRequirements(block, face, loadChunks = false, particles = false)
	}

	val blocks = LongOpenHashSet()
	val captiveSubsystems = LinkedList<StarshipSubsystem>()

	fun detectTurret() {
		if (!starship.contains(pos.x, pos.y + 1, pos.z)) return

		val visitedBlocks = ObjectOpenHashSet<Block>()
		val toVisit = ArrayDeque<Block>()

		toVisit.add(starship.world.getBlockAt(pos.x, pos.y + 1, pos.z))

		while (toVisit.isNotEmpty()) {
			val block = toVisit.removeFirst()

			if (!canDetect(block)) continue

			visitedBlocks.add(block)

			for (offsetX in -1..1) for (offsetY in -1..1) for (offsetZ in -1..1) {
				val newBlock = block.getRelative(offsetX, offsetY, offsetZ)

				if (block == newBlock) continue

				if (!starship.contains(newBlock.x, newBlock.y, newBlock.z)) continue

				if (visitedBlocks.contains(newBlock)) continue

				toVisit.addLast(newBlock)
			}
		}

		blocks.addAll(visitedBlocks.map { Vec3i(it.x, it.y, it.z).toBlockKey() })
		starship.subsystems.filterTo(captiveSubsystems) { blocks.contains(it.pos.toBlockKey()) }
	}

	private fun canDetect(block: Block): Boolean {
		return block.y > pos.y
	}

	fun rotate() {

	}

	override fun handleRelease() {
		blocks.forEach { Bukkit.getPlayer("GutinGongoozler")?.highlightBlock(Vec3i(it), 50L) }
		captiveSubsystems.forEach { Bukkit.getPlayer("GutinGongoozler")?.highlightBlock(it.pos, 150L) }
	}
}
