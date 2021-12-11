package net.starlegacy.feature.starship.event

import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.movement.RotationMovement
import net.starlegacy.feature.starship.movement.TranslateMovement
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

class StarshipRotateEvent(
    ship: ActivePlayerStarship,
    player: Player,
    override val movement: RotationMovement
) : StarshipMoveEvent(ship, player, movement), Cancellable {
    val clockwise = movement.clockwise

    private var cancelled: Boolean = false

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
