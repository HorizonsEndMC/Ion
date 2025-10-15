package net.horizonsend.ion.server.gui.invui.misc

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window
import kotlin.reflect.KProperty1

class AccessManagementMenu<Db : DbObject, Id : Oid<Db>>(
	viewer: Player,
	private val id: Id,
	private val companion: OidDbObjectCompanion<Db>,
	private val playerProperty: KProperty1<Db, Iterable<SLPlayerId>>,
	private val settlementProperty: KProperty1<Db, Iterable<Oid<Settlement>>>,
	private val nationProperty: KProperty1<Db, Iterable<Oid<Nation>>>
) : InvUIWindowWrapper(viewer, async = true) {
	override fun buildWindow(): Window {
		val gui = Gui.normal()
			.setStructure(
				"x . . . . . . . .",
				"P p p p p p p p p",
				"S s s s s s s s s",
				"N n n n n n n n n",
				". . . . . . . . .",
			)
			.addIngredient('p', playerButton)
			.addIngredient('P', trustedPlayersIcon)
			.addIngredient('s', settlementsButton)
			.addIngredient('S', trustedSettlementsIcon)
			.addIngredient('n', nationsButton)
			.addIngredient('N', trustedNationsIcon)
			.addIngredient('x', parentOrBackButton())

		return normalWindow(gui.build())
	}

	private val trustedPlayersIcon = ItemStack(Material.PLAYER_HEAD).updateDisplayName(Component.text("Manage Trusted Players")).makeGuiButton { _, _ ->
		TrustManagementMenu.player(viewer, id, companion, playerProperty, this)
	}
	private val playerButton = GuiItem.EMPTY.makeItem(Component.text("Manage Trusted Players")).makeGuiButton { _, _ ->
		TrustManagementMenu.player(viewer, id, companion, playerProperty, this)
	}
	private val settlementsButton = GuiItem.EMPTY.makeItem(Component.text("Manage Trusted Settlements")).makeGuiButton { _, _ ->
		TrustManagementMenu.settlement(viewer, id, companion, settlementProperty, this)
	}
	private val trustedSettlementsIcon = GuiItem.CITY.makeItem(Component.text("Manage Trusted Settlements")).makeGuiButton { _, _ ->
		TrustManagementMenu.settlement(viewer, id, companion, settlementProperty, this)
	}
	private val nationsButton = GuiItem.EMPTY.makeItem(Component.text("Manage Trusted Nations")).makeGuiButton { _, _ ->
		TrustManagementMenu.nation(viewer, id, companion, nationProperty, this)
	}
	private val trustedNationsIcon = GuiItem.WORLD.makeItem(Component.text("Manage Trusted Nations")).makeGuiButton { _, _ ->
		TrustManagementMenu.nation(viewer, id, companion, nationProperty, this)
	}

	override fun buildTitle(): Component {
		return GuiText("Manage Access")
			.setSlotOverlay(
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #"
			)
			.add(Component.text("Manage Trusted Players"), line = 2, horizontalShift = 20, verticalShift = 5)
			.add(Component.text("Manage Trusted Settlements"), line = 4, horizontalShift = 20, verticalShift = 5)
			.add(Component.text("Manage Trusted Nations"), line = 6, horizontalShift = 20, verticalShift = 5)
			.build()
	}
}
