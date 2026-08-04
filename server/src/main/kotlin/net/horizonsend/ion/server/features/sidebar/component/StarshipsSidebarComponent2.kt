package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.nations.DominionTerritoryBuffTypes
//import net.horizonsend.ion.server.features.nations.NationBuffTypes
import net.horizonsend.ion.server.features.sidebar.tasks.StarshipsSidebar
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.input.DirectControlInput
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffectTypes
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

class StarshipsSidebarComponent2(starship: ActiveControlledStarship, player: Player) : SidebarComponent {
    private val currentVelocity = starship.cruiseData.velocity.length().roundToHundredth()
	private val speedModifier = starship.getStrongestActiveStatusEffectFromType(StarshipStatusEffectTypes.CRUISE_SPEED)?.strength ?: 0.0
	private val slowModifier = starship.getStrongestActiveStatusEffectFromType(StarshipStatusEffectTypes.CRUISE_SLOW)?.strength ?: 0.0
	/*
	private val nationCruiseModifier = starship.playerPilot?.let { player ->
		val cruiseBuffActive = NationBuffTypes.isEffectActive(player, NationBuffTypes.CRUISE_SPEED)
		if (cruiseBuffActive) {
			NationBuffTypes.CRUISE_SPEED.value
		} else 0.0
	} ?: 0.0
	 */
	val dominionBpsModifier = starship.playerPilot?.let { player ->
		if (DominionTerritoryBuffTypes.isEffectActive(player, DominionTerritoryBuffTypes.SPEED))
			DominionTerritoryBuffTypes.SPEED.value
		else 0.0
	} ?: 0.0
    private val maxVelocity = (starship.cruiseData.targetSpeed * (1 + speedModifier) * (1 - slowModifier) + /*nationCruiseModifier + */dominionBpsModifier).toInt()
    private val pmThruster = starship.reactor.powerDistributor.thrusterPortion
    private val acceleration = starship.cruiseData.getRealAccel(pmThruster).roundToHundredth()
    private val isDirectControlEnabled = starship.isDirectControlEnabled
    private val isDirectControlBoosted = (starship.controller.movementHandler.input as? DirectControlInput)?.isBoosting ?: false
    private val isCruising = StarshipCruising.isCruisingAndAccelerating(starship)
    private val isStopped = starship.cruiseData.velocity.lengthSquared() == 0.0
    private val isBlocked = starship.lastBlockedTime > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(StarshipCruising.SECONDS_PER_CRUISE.toLong()))
    private val compassComponent = StarshipsSidebar.compassComponent(starship, player)

    override fun draw(drawable: LineDrawable) {
        val line = ofChildren(
            compassComponent[1][0],
            space(),
            compassComponent[1][1],
            space(),
            compassComponent[1][2],
            text(" | ", GRAY),

            // Speed
            text("SPD: ", WHITE),
            StarshipsSidebar.speedComponent(isDirectControlEnabled, isDirectControlBoosted, isCruising, isStopped, isBlocked),
            space(),
            StarshipsSidebar.maxSpeedComponent(currentVelocity, maxVelocity, acceleration)
        )
        drawable.drawLine(line)
    }
}
