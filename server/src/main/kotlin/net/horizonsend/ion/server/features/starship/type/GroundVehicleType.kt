package net.horizonsend.ion.server.features.starship.type

import net.horizonsend.ion.server.configuration.StarshipBalancing
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.sidebar.SidebarIcon
import net.horizonsend.ion.server.features.starship.TypeCategory
import net.horizonsend.ion.server.features.starship.destruction.SinkProvider
import net.horizonsend.ion.server.features.starship.type.restriction.DetectionParameters
import net.horizonsend.ion.server.features.starship.type.restriction.PilotRestrictions
import net.horizonsend.ion.server.features.starship.type.restriction.SubsystemRestrictions
import net.horizonsend.ion.server.features.starship.type.restriction.WorldRestrictions
import java.util.function.Supplier

class GroundVehicleType<T : StarshipBalancing>(
	key: IonRegistryKey<StarshipType<*>, out StarshipType<T>>,
	displayName: String,
	icon: String = SidebarIcon.GENERIC_STARSHIP_ICON.text,
	color: String,
	dynmapIcon: String = "anchor",
	detectionParameters: DetectionParameters,
	typeCategory: TypeCategory,
	menuConfiguration: ComputerMenuConfiguration,
	pilotRestrictions: PilotRestrictions,
	flightRestrictions: SubsystemRestrictions,
	worldRestrictions: WorldRestrictions,
	sinkProvider: SinkProvider.SinkProviders = SinkProvider.SinkProviders.STANDARD,
	balancingProvider: Supplier<T>,

    val groundClearanceHeight: Int,
) : StarshipType<T>(key, displayName, icon, color, dynmapIcon, detectionParameters, typeCategory, menuConfiguration, pilotRestrictions, flightRestrictions, worldRestrictions, sinkProvider, balancingProvider)
