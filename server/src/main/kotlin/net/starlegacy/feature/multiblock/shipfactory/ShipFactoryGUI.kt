package net.starlegacy.feature.multiblock.shipfactory

import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.SHIP_FACTORY_DATA
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.core.BlockPos
import net.starlegacy.database.schema.starships.Blueprint
import net.starlegacy.database.slPlayerId
import net.starlegacy.util.MenuHelper
import net.starlegacy.util.component1
import net.starlegacy.util.component2
import net.starlegacy.util.component3
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ShipFactoryGUI(val player: Player, val multiblock: Sign) {
	val data get(): ShipFactoryData? = multiblock.persistentDataContainer.get(SHIP_FACTORY_DATA, ShipFactoryData)
	val blueprint get(): Blueprint? = data?.blueprintName?.let { Blueprint.get(player.uniqueId.slPlayerId, it) }
	val clipboard get(): Clipboard? = (blueprint)?.blockData?.let { Blueprint.parseData(it) }
	fun show(player: Player) = MenuHelper.apply {
		val data = data ?: return@apply

		val gui = gui(
			5,
			text("Ship Factory Configuration")
				.color(TextColor.fromHexString("#eb9234"))
				.decoration(TextDecoration.BOLD, true)
				.decoration(TextDecoration.ITALIC, false)
		)

		val topBuffer = staticPane(0, 0, 9, 1)
		topBuffer.fillWith(ItemStack(Material.BLACK_STAINED_GLASS_PANE))
		val bottomBuffer = staticPane(0, 5, 9, 1)
		bottomBuffer.fillWith(ItemStack(Material.BLACK_STAINED_GLASS_PANE))

		gui.addPane(topBuffer)
		gui.addPane(bottomBuffer)

		gui.show(player)
	}

//	fun calculateRemainingCost(): Double {}

//	fun getBounds(origin: BlockPos): Pair<BlockPos, BlockPos> {
//		val data = data!!
//		val clipboard = clipboard!!
//
//		val (offsetX: Int , offsetY: Int , offsetZ: Int) = data
//		val rotation = data.rotation
//
//		val (signX, signY, signZ) = origin
//
//		val originX = signX + offsetX
//		val originY = signY + offsetY
//		val originZ = signZ + offsetZ
//
//
//	}

	fun drawBoundingBox() {
		val data = data ?: return

		val (originX: Int , originY: Int , originZ: Int) = data
		val rotation = data.rotation

	}

	fun manageOffsetsMenu() {}

	fun printMaterials() {}

	fun showMaterials() {}

	fun toggle() {}

//	fun checkObstructions(): Boolean {}

	fun iterateRegion(origin: BlockPos, action: (blueprintBlock: Block, worldBlock: Block) -> Unit) {
		val data = data ?: return
		val clipboard = clipboard ?: return

		val (offsetX: Int , offsetY: Int , offsetZ: Int) = data
		val rotation = data.rotation

		val (signX, signY, signZ) = origin

		val originX = signX + offsetX
		val originY = signY + offsetY
		val originZ = signZ + offsetZ

		val region = clipboard.region.clone()
		val targetBlockVector: BlockVector3 = BlockVector3.at(data.offsetX, data.offsetY, data.offsetZ)
		val offset: BlockVector3 = targetBlockVector.subtract(clipboard.origin)


	}
}
