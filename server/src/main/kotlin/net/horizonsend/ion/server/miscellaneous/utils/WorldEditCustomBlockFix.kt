package net.horizonsend.ion.server.miscellaneous.utils

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.Extent
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.EmptyClipboardException
import com.sk89q.worldedit.extent.transform.BlockTransformExtent
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.math.Vector3
import com.sk89q.worldedit.math.transform.Transform
import com.sk89q.worldedit.world.block.BaseBlock
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.customBlock
import net.horizonsend.ion.server.features.custom.blocks.misc.DirectionalCustomBlock
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import kotlin.math.roundToInt

// Fixes the issue where users in creative rotate or flip selections with custom blocks (or any other edit that changes the faces/direction of custom blocks)
// There's probably a smarter fix, but for now this only looks for directional custom blocks
// If anyone changes this to include other clipboard flags they should read the world edit docs (currently cmd can either paste w/ or w/o air)

private val CARDINAL_FACES = arrayOf(
	BlockFace.NORTH,
	BlockFace.SOUTH,
	BlockFace.EAST,
	BlockFace.WEST,
	BlockFace.UP,
	BlockFace.DOWN
)

fun Transform.toCardinalFaceMap(): Map<BlockFace, BlockFace> {
	val origin = apply(Vector3.ZERO)

	return buildMap {
		for (face in CARDINAL_FACES) {
			val direction = Vector3.at(
				face.modX.toDouble(),
				face.modY.toDouble(),
				face.modZ.toDouble()
			)
			val transformed = apply(direction)
				.subtract(origin)
			val transformedX = transformed.x().roundToInt()
			val transformedY = transformed.y().roundToInt()
			val transformedZ = transformed.z().roundToInt()
			val newFace = CARDINAL_FACES.firstOrNull {
				it.modX == transformedX &&
						it.modY == transformedY &&
						it.modZ == transformedZ
			} ?: face

			put(face, newFace)
		}
	}
}

private fun resolveTransformedBlock(
	sourceBlock: BaseBlock,
	sourcePos: BlockVector3,
	faceMap: Map<BlockFace, BlockFace>,
	vanillaFallback: Extent
): BaseBlock {
	val blockData = sourceBlock.toImmutableState().toBukkitBlockData()
	val customBlock = blockData.customBlock ?: return vanillaFallback.getFullBlock(sourcePos)

	return when (customBlock) {
		is DirectionalCustomBlock -> {
			val currentFace = customBlock.getFace(blockData.nms)
			val newFace = faceMap[currentFace] ?: currentFace
			val newData = customBlock.faceData[newFace] ?: blockData
			BukkitAdapter.adapt(newData).toBaseBlock(sourceBlock.nbt)
		}
		else -> sourceBlock
	}
}

fun pasteClipboardCustomBlocks(
	clipboard: Clipboard,
	transform: Transform,
	destination: Extent,
	to: BlockVector3,
	ignoreAirBlocks: Boolean = false
) {
	val faceMap = transform.toCardinalFaceMap()
	val vanillaFallback = BlockTransformExtent(clipboard, transform)

	for (sourcePos in clipboard.region) {
		val sourceBlock = clipboard.getFullBlock(sourcePos)
		if (ignoreAirBlocks && sourceBlock.blockType.material.isAir) continue

		val destBlock = resolveTransformedBlock(sourceBlock, sourcePos, faceMap, vanillaFallback)
		val relative = sourcePos.subtract(clipboard.origin).toVector3()
		val destPos = transform.apply(relative).toBlockPoint().add(to)

		destination.setBlock(
			destPos.x(),
			destPos.y(),
			destPos.z(),
			destBlock
		)
	}
}

//Gets and edits the world edit session b/c custom block world edits gotta work with //redo & //undo
fun Player.pasteClipboardCustomBlocks(ignoreAirBlocks: Boolean = false) {
	val user = BukkitAdapter.adapt(this)
	val session = WorldEdit.getInstance().sessionManager.get(user)
	val holder = try {
		session.clipboard
	}
	catch (_:EmptyClipboardException) {
		sendMessage("Your clipboard is empty.")
		return
	}

	world.worldEditSession { editSession ->
		val to = BukkitAdapter.adapt(location).toVector().toBlockPoint()
		val clipboard = holder.clipboards.firstOrNull() ?: run {
			sendMessage("Your clipboard is empty.")
			return@worldEditSession
		}
		pasteClipboardCustomBlocks(clipboard, holder.transform, editSession, to, ignoreAirBlocks)
		session.remember(editSession)
	}
}
