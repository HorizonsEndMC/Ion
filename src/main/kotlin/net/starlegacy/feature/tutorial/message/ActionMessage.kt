package net.starlegacy.feature.tutorial.message

import org.bukkit.entity.Player

class ActionMessage(title: String, subtitle: String, private val action: (Player) -> Unit) :
    PopupMessage(title, subtitle) {
    override fun show(player: Player) {
        super.show(player)
        action(player)
    }
}
