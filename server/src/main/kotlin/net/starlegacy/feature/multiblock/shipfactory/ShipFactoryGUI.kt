package net.starlegacy.feature.multiblock.shipfactory

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.BLUEPRINT
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.core.BlockPos
import net.starlegacy.database.schema.starships.Blueprint
import net.starlegacy.database.slPlayerId
import net.starlegacy.util.MenuHelper
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING

class ShipFactoryGUI(
	val player: Player,
	val multiblock: Sign
) {
	val blueprint get(): Blueprint? =
		multiblock.persistentDataContainer.get(BLUEPRINT, STRING)?.let { Blueprint.get(player.uniqueId.slPlayerId, it) }

	fun show(player: Player) = MenuHelper.apply {
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
	}

//	fun calculateRemainingCost(): Double {}
//
//	fun getBounds(): Pair<BlockPos, BlockPos> {}

	fun drawBoundingBox() {}

	fun manageOffsets() {}

	fun printMaterials() {}

	fun showMaterials() {}

	fun toggle() {}
}
