package net.horizonsend.ion.server.features.starship.type

import net.horizonsend.ion.server.configuration.StarshipBalancing
import net.horizonsend.ion.server.core.registries.IonRegistryKey
import net.horizonsend.ion.server.core.registries.Keyed
import net.horizonsend.ion.server.features.sidebar.SidebarIcon
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.TypeCategory
import net.horizonsend.ion.server.features.starship.destruction.SinkProvider
import net.horizonsend.ion.server.features.starship.type.restriction.DetectionParmeterHolder
import net.horizonsend.ion.server.features.starship.type.restriction.PilotRestrictions
import net.horizonsend.ion.server.features.starship.type.restriction.SubsystemRestrictions
import net.horizonsend.ion.server.features.starship.type.restriction.WorldRestrictions
import net.horizonsend.ion.server.features.world.IonWorld
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import java.util.function.Supplier

open class StarshipType<T: StarshipBalancing>(
	override val key: IonRegistryKey<StarshipType<*>, out StarshipType<T>>,

	// Display settings
	val displayName: String,
	val icon: String = SidebarIcon.GENERIC_STARSHIP_ICON.text,
	val color: String,
	val dynmapIcon: String,

	val detectionParameters: DetectionParmeterHolder,
	val typeCategory: TypeCategory,
	val menuConfiguration: ComputerMenuConfiguration,

	val pilotRestrictions: PilotRestrictions,
	val subsystemRestrictions: SubsystemRestrictions,
	val worldRestrictions: WorldRestrictions,

	val sinkProvider: SinkProvider.SinkProviders = SinkProvider.SinkProviders.STANDARD,
	private val balancingProvider: Supplier<T>
) : Keyed<StarshipType<*>> {
	val balancing get() = balancingProvider.get()

	open fun canPilot(player: Player): Boolean {
		return pilotRestrictions.canPilot(player)
	}

	open fun canFinishPiloting(starship: Starship): Boolean {
		return subsystemRestrictions.check(starship)
	}

	open fun canPilotIn(starship: Starship, newWorld: IonWorld): Boolean {
		return worldRestrictions.canPilotIn(newWorld)
	}

	val textColor = TextColor.fromHexString(color)!!
	val displayNameComponent = Component.text(displayName, textColor)
}
