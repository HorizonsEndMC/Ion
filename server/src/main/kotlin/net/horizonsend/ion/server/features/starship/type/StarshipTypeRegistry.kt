package net.horizonsend.ion.server.features.starship.type

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.StarshipBalancing
import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.StarshipTypeKeys
import net.horizonsend.ion.server.core.registration.registries.Registry
import net.horizonsend.ion.server.features.sidebar.SidebarIcon
import net.horizonsend.ion.server.features.starship.TypeCategory
import net.horizonsend.ion.server.features.starship.destruction.SinkProvider
import net.horizonsend.ion.server.features.starship.subsystem.misc.MiningLaserSubsystem
import net.horizonsend.ion.server.features.starship.type.restriction.BalancingProvided
import net.horizonsend.ion.server.features.starship.type.restriction.DetectionParameters
import net.horizonsend.ion.server.features.starship.type.restriction.PilotRestrictions.LevelRequirement
import net.horizonsend.ion.server.features.starship.type.restriction.PilotRestrictions.None
import net.horizonsend.ion.server.features.starship.type.restriction.PilotRestrictions.PermissionLocked
import net.horizonsend.ion.server.features.starship.type.restriction.SubsystemRestriction.CappedSubsystem
import net.horizonsend.ion.server.features.starship.type.restriction.SubsystemRestrictions
import net.horizonsend.ion.server.features.starship.type.restriction.WorldRestrictions

class StarshipTypeRegistry : Registry<StarshipType<*>>("STARSHIP_TYPE") {
	override val keySet: KeyRegistry<StarshipType<*>> = StarshipTypeKeys
	override fun boostrap() {
		registerTradeShips()
		registerCombatShips()
		registerMiscShips()
	}

	fun registerTradeShips() {
		register(StarshipTypeKeys.SHUTTLE, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.SHUTTLE,

		))
		register(StarshipTypeKeys.TRANSPORT, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.TRANSPORT,

		))
		register(StarshipTypeKeys.LIGHT_FREIGHTER, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.LIGHT_FREIGHTER,

		))
		register(StarshipTypeKeys.MEDIUM_FREIGHTER, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.MEDIUM_FREIGHTER,

		))
		register(StarshipTypeKeys.HEAVY_FREIGHTER, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.HEAVY_FREIGHTER,

		))
		register(StarshipTypeKeys.BARGE, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.BARGE,

		))
	}

	fun registerCombatShips() {
		register(StarshipTypeKeys.STARFIGHTER, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.STARFIGHTER,
			displayName = "Starfighter",
			icon = SidebarIcon.STARFIGHTER_ICON.text,
			color = "#ff8000",
			dynmapIcon = "starfighter",
			detectionParameters = BalancingProvided(StarshipTypeKeys.STARFIGHTER),
			typeCategory = TypeCategory.WAR_SHIP,
			menuConfiguration = ComputerMenuConfiguration.IconWithSubclassses(StarshipTypeKeys.INTERCEPTOR),
			pilotRestrictions = LevelRequirement(1, overridePermissionString = "ion.ships.override.1"),
			subsystemRestrictions = SubsystemRestrictions(CappedSubsystem(MiningLaserSubsystem::class, 0)),
			worldRestrictions = WorldRestrictions(),
			sinkProvider = SinkProvider.SinkProviders.STANDARD,
			balancingProvider = ConfigurationFiles.starshipBalancing()::starfighter
		))
		register(StarshipTypeKeys.INTERCEPTOR, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.INTERCEPTOR,
			displayName = "Interceptor",
			icon = SidebarIcon.INTERCEPTOR_ICON.text,
			color = "#ff8000",
			dynmapIcon = "interceptor",
			detectionParameters = BalancingProvided(StarshipTypeKeys.INTERCEPTOR),
			typeCategory = TypeCategory.WAR_SHIP,
			menuConfiguration = ComputerMenuConfiguration.NormalIcon(),
			pilotRestrictions = LevelRequirement(1, overridePermissionString = "ion.ships.override.1"),
			subsystemRestrictions = SubsystemRestrictions(CappedSubsystem(MiningLaserSubsystem::class, 0)),
			worldRestrictions = WorldRestrictions(),
			sinkProvider = SinkProvider.SinkProviders.STANDARD,
			balancingProvider = ConfigurationFiles.starshipBalancing()::interceptor
		))
		register(StarshipTypeKeys.GUNSHIP, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.GUNSHIP,
			displayName = "Gunship",
			icon = SidebarIcon.GUNSHIP_ICON.text,
			color = "#ff4000",
			dynmapIcon = "gunship",
			detectionParameters = BalancingProvided(StarshipTypeKeys.GUNSHIP),
			typeCategory = TypeCategory.WAR_SHIP,
			menuConfiguration = ComputerMenuConfiguration.NormalIcon(),
			pilotRestrictions = LevelRequirement(1, overridePermissionString = "ion.ships.override.10"),
			subsystemRestrictions = SubsystemRestrictions(CappedSubsystem(MiningLaserSubsystem::class, 0)),
			worldRestrictions = WorldRestrictions(),
			sinkProvider = SinkProvider.SinkProviders.STANDARD,
			balancingProvider = ConfigurationFiles.starshipBalancing()::gunship
		))
		register(StarshipTypeKeys.CORVETTE, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.CORVETTE,
			displayName = "Corvette",
			icon = SidebarIcon.CORVETTE_ICON.text,
			color = "#ff0000",
			dynmapIcon = "corvette",
			detectionParameters = BalancingProvided(StarshipTypeKeys.CORVETTE),
			typeCategory = TypeCategory.WAR_SHIP,
			menuConfiguration = ComputerMenuConfiguration.NormalIcon(),
			pilotRestrictions = LevelRequirement(1, overridePermissionString = "ion.ships.override.20"),
			subsystemRestrictions = SubsystemRestrictions(CappedSubsystem(MiningLaserSubsystem::class, 0)),
			worldRestrictions = WorldRestrictions(),
			sinkProvider = SinkProvider.SinkProviders.STANDARD,
			balancingProvider = ConfigurationFiles.starshipBalancing()::corvette
		))
		register(StarshipTypeKeys.LOGISTICS_CORVETTE, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.LOGISTICS_CORVETTE,
			displayName = "Logistic Corvette",
			icon = SidebarIcon.CORVETTE_ICON.text,
			color = "#ff0000",
			dynmapIcon = "corvette",
			detectionParameters = BalancingProvided(StarshipTypeKeys.LOGISTICS_CORVETTE),
			typeCategory = TypeCategory.WAR_SHIP,
			menuConfiguration = ComputerMenuConfiguration.NormalIcon(),
			pilotRestrictions = PermissionLocked(permissionString = "ion.ships.ai.corvette", allowGlobalOverride = false),
			subsystemRestrictions = SubsystemRestrictions(),
			worldRestrictions = WorldRestrictions(),
			sinkProvider = SinkProvider.SinkProviders.STANDARD,
			balancingProvider = ConfigurationFiles.starshipBalancing()::aiCorvetteLogistic
		))
		register(StarshipTypeKeys.FRIGATE, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.FRIGATE,
			displayName = "Frigate",
			icon = SidebarIcon.FRIGATE_ICON.text,
			color = "#c00000",
			dynmapIcon = "frigate",
			detectionParameters = BalancingProvided(StarshipTypeKeys.FRIGATE),
			typeCategory = TypeCategory.WAR_SHIP,
			menuConfiguration = ComputerMenuConfiguration.NormalIcon(),
			pilotRestrictions = LevelRequirement(1, overridePermissionString = "ion.ships.override.40"),
			subsystemRestrictions = SubsystemRestrictions(),
			worldRestrictions = WorldRestrictions(),
			sinkProvider = SinkProvider.SinkProviders.STANDARD,
			balancingProvider = ConfigurationFiles.starshipBalancing()::frigate
		))
		register(StarshipTypeKeys.DESTROYER, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.DESTROYER,
			displayName = "Destroyer",
			icon = SidebarIcon.DESTROYER_ICON.text,
			color = "#800000",
			dynmapIcon = "destroyer",

		))
		register(StarshipTypeKeys.CRUISER, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.CRUISER,
			displayName = "Cruiser",
			icon = SidebarIcon.BATTLECRUISER_ICON.text,
			color = "#FFD700",
			dynmapIcon = "cruiser",

		))
		register(StarshipTypeKeys.BATTLECRUISER, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.BATTLECRUISER,
			displayName = "Battlecruiser",
			icon = SidebarIcon.BATTLECRUISER_ICON.text,
			color = "#0c5ce8",
			dynmapIcon = "battlecruiser",

		))
		register(StarshipTypeKeys.BATTLESHIP, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.BATTLESHIP,
			displayName = "Battleship",
			icon = SidebarIcon.BATTLESHIP_ICON.text,
			color = "#0c1cff",
			dynmapIcon = "anchor",
		))
		register(StarshipTypeKeys.DREADNOUGHT, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.DREADNOUGHT,
			displayName = "Dreadnought",
			icon = SidebarIcon.DREADNOUGHT_ICON.text,
			color = "#320385",
			dynmapIcon = "anchor",
		))
		register(StarshipTypeKeys.TANK, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.TANK,
			displayName = "Tank",
			icon = SidebarIcon.STARFIGHTER_ICON.text,
			color = "#ff8000",
			dynmapIcon = "anchor",
		))
	}

	fun registerMiscShips() {
		register(StarshipTypeKeys.SPEEDER, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.SPEEDER,
			displayName = "Speeder",
			color = "#ffff32",
			detectionParameters = DetectionParameters(
				minSize = 25,
				maxSize = 100,
				concretePercent = 0.0,
				containerPercent = 0.25,
				crateLimitMultiplier = 0.125
			),
			typeCategory = TypeCategory.SPECIALTY,
			menuConfiguration = ComputerMenuConfiguration.NormalIcon(),
			pilotRestrictions = None,
			subsystemRestrictions = SubsystemRestrictions(),
			worldRestrictions = WorldRestrictions(),
			sinkProvider = SinkProvider.SinkProviders.STANDARD,
			balancingProvider = ConfigurationFiles.starshipBalancing()::speeder
		))
		register(StarshipTypeKeys.PLATFORM, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.PLATFORM,

			dynmapIcon = "anchor",
		))
		register(StarshipTypeKeys.UNIDENTIFIEDSHIP, StarshipType<StarshipBalancing>(
			key = StarshipTypeKeys.UNIDENTIFIEDSHIP,

			dynmapIcon = "anchor",
		))
	}
}
