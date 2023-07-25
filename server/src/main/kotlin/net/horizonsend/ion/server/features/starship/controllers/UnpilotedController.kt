package net.horizonsend.ion.server.features.starship.controllers

import net.horizonsend.ion.server.features.starship.Starship
import org.bukkit.entity.Player

<<<<<<<< HEAD:server/src/main/kotlin/net/horizonsend/ion/server/features/starship/controllers/UnpilotedController.kt
class UnpilotedController(player: Player, starship: Starship) : PlayerController(player, starship, "Legacy") {
}
========
class LegacyController(player: Player, starship: Starship) : PlayerController(player, starship, "Legacy")
>>>>>>>> b6fdd808 (implementation):server/src/main/kotlin/net/horizonsend/ion/server/features/starship/controllers/LegacyController.kt
