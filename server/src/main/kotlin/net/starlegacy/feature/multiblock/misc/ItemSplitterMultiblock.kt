package net.starlegacy.feature.multiblock.misc

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.starlegacy.feature.multiblock.FurnaceMultiblock
import net.starlegacy.feature.multiblock.InteractableMultiblock
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.Multiblock
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.inventory.FurnaceBurnEvent

object ItemSplitterMultiblock : Multiblock(), FurnaceMultiblock, InteractableMultiblock {
	override val name: String = "splitter"

	val BLACKLIST = text("BLACKLIST", NamedTextColor.BLACK, TextDecoration.BOLD)
	val WHITELIST = text("BLACKLIST", NamedTextColor.WHITE, TextDecoration.BOLD)

	override val signText: Array<Component?> = arrayOf(
		text("Item Splitter"),
		BLACKLIST,
		null,
		null
	)

	override fun LegacyMultiblockShape.buildStructure() {

	}

	override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {

	}

	override fun onSignInteract(sign: Sign, player: Player) {
		TODO("Not yet implemented")
	}
}
