package net.horizonsend.ion.server.features.starship.type

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.StarshipBalancing
import net.horizonsend.ion.server.core.registries.Registry
import net.horizonsend.ion.server.core.registries.keys.StarshipTypeKeys
import net.horizonsend.ion.server.features.starship.FLYABLE_BLOCKS
import net.horizonsend.ion.server.features.starship.TypeCategory
import net.horizonsend.ion.server.features.starship.destruction.SinkProvider
import net.horizonsend.ion.server.features.starship.type.restriction.DetectionParameters
import net.horizonsend.ion.server.features.starship.type.restriction.PilotRestrictions
import net.horizonsend.ion.server.features.starship.type.restriction.PilotRestrictions.None
import net.horizonsend.ion.server.features.starship.type.restriction.SubsystemRestrictions
import net.horizonsend.ion.server.features.starship.type.restriction.WorldRestrictions

class StarshipTypeRegistry : Registry<StarshipType<*>>("STARSHIP_TYPE") {
	override fun boostrap() {
		register(StarshipTypeKeys.SPEEDER, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.SPEEDER,
			displayName = "Speeder",
			color = "#ffff32",
			detectionParameters = DetectionParameters(
				minSize = 25,
				maxSize = 100,
				concretePercent = 0.0,
				containerPercent = 0.25,
				crateLimitMultiplier = 0.125,
				pilotableBlockList = FLYABLE_BLOCKS
			),
			typeCategory = TypeCategory.SPECIALTY,
			menuConfiguration = ComputerMenuConfiguration.IconWithSubclassses(StarshipTypeKeys.AI_SPEEDER),
			pilotRestrictions = None,
			subsystemRestrictions = SubsystemRestrictions(),
			worldRestrictions = WorldRestrictions(),
			sinkProvider = SinkProvider.SinkProviders.STANDARD,
			balancingProvider = ConfigurationFiles.starshipBalancing()::speeder
		))
		register(StarshipTypeKeys.AI_SPEEDER, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.AI_SPEEDER,
			displayName = "AI Speeder",
			color = "#ffff32",
			detectionParameters = DetectionParameters(
				minSize = 25,
				maxSize = 100,
				concretePercent = 0.0,
				containerPercent = 0.25,
				crateLimitMultiplier = 0.125,
				pilotableBlockList = FLYABLE_BLOCKS
			),
			typeCategory = TypeCategory.SPECIALTY,
			menuConfiguration = ComputerMenuConfiguration.NormalIcon(),
			pilotRestrictions = PilotRestrictions.PermissionLocked("ion.ships.ai", allowOverridePermission = false),
			subsystemRestrictions = SubsystemRestrictions(),
			worldRestrictions = WorldRestrictions(),
			sinkProvider = SinkProvider.SinkProviders.STANDARD,
			balancingProvider = ConfigurationFiles.starshipBalancing()::speeder
		))
	}
}
