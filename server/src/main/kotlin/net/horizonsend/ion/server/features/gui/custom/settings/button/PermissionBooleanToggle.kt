package net.horizonsend.ion.server.features.gui.custom.settings.button

import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.luckPerms
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.command.misc.IonSitCommand.sitStateNode
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.luckperms.api.node.NodeEqualityPredicate
import net.luckperms.api.node.types.PermissionNode
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import java.util.function.Consumer

class PermissionBooleanToggle(
	val permissionNode: PermissionNode,
	name: Component,
	descriptin: String,
	icon: GuiItem,
	defaultValue: Boolean
) : SettingsMenuButton<Boolean>(name, descriptin, icon, defaultValue) {
	override fun setState(player: Player, state: Boolean) {
		luckPerms.userManager.modifyUser(player.uniqueId) { user ->
			user.data().add(permissionNode.toBuilder().value(state).build())
			player.success("Set ${name.plainText()} to $state")
		}
	}

	override fun getState(player: Player): Boolean {
		return luckPerms
			.userManager
			.getUser(player.uniqueId)
			?.data()
			?.contains(sitStateNode, NodeEqualityPredicate.EXACT)
			?.asBoolean() != false
	}

	override fun getSecondLine(player: Player): Component {
		val state = getState(player)
		return if (state) text("ENABLED", GREEN) else text("DISABLED", RED)
	}

	override fun handleClick(
		clicker: Player,
		oldValue: Boolean,
		gui: PagedGui<*>,
		parent: SettingsPageGui,
		newValueConsumer: Consumer<Boolean>
	) {
		newValueConsumer.accept(!oldValue)
	}
}
