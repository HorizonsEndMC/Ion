package net.horizonsend.ion.server.features.starship.type

import net.horizonsend.ion.server.configuration.StarshipBalancing
import net.horizonsend.ion.server.core.registries.IonRegistryKey
import net.horizonsend.ion.server.features.sidebar.SidebarIcon
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.TypeCategory
import net.horizonsend.ion.server.features.starship.destruction.SinkProvider
import net.horizonsend.ion.server.features.starship.type.restriction.DetectionParameters
import net.horizonsend.ion.server.features.starship.type.restriction.PilotRestrictions
import net.horizonsend.ion.server.features.starship.type.restriction.SubsystemRestrictions
import net.horizonsend.ion.server.features.starship.type.restriction.WorldRestrictions
import net.horizonsend.ion.server.features.world.IonWorld
import org.bukkit.entity.Player
import java.util.function.Supplier

open class StarshipType<T: StarshipBalancing>(
	val key: IonRegistryKey<StarshipType<*>, out StarshipType<T>>,

	// Display settings
	val displayName: String,
	val icon: String = SidebarIcon.GENERIC_STARSHIP_ICON.text,
	val color: String,
	val dynmapIcon: String = "anchor",

	val detectionParameters: DetectionParameters,
	val typeCategory: TypeCategory,
	val menuConfiguration: ComputerMenuConfiguration,

	val pilotRestrictions: PilotRestrictions,
	val subsystemRestrictions: SubsystemRestrictions,
	val worldRestrictions: WorldRestrictions,

	val sinkProvider: SinkProvider.SinkProviders = SinkProvider.SinkProviders.STANDARD,
	private val balancingProvider: Supplier<T>
) : Cloneable {
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
}
