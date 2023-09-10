package net.horizonsend.ion.server.miscellaneous.registrations

import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.DBManager
import net.horizonsend.ion.common.utils.redisaction.RedisActions
import net.horizonsend.ion.server.features.bounties.Bounties
import net.horizonsend.ion.server.features.cache.Caches
import net.horizonsend.ion.server.features.chat.ChannelSelections
import net.horizonsend.ion.server.features.client.whereisit.mod.ModNetworking
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.economy.bazaar.Merchants
import net.horizonsend.ion.server.features.economy.cargotrade.CrateRestrictions
import net.horizonsend.ion.server.features.economy.cargotrade.ShipmentBalancing
import net.horizonsend.ion.server.features.economy.cargotrade.ShipmentGenerator
import net.horizonsend.ion.server.features.economy.cargotrade.ShipmentManager
import net.horizonsend.ion.server.features.economy.city.CityNPCs
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.economy.collectors.CollectionMissions
import net.horizonsend.ion.server.features.economy.collectors.Collectors
import net.horizonsend.ion.server.features.enviornment.mobs.CustomMobSpawning
import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.gear.Gear
import net.horizonsend.ion.server.features.machine.AreaShields
import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.misc.CombatNPCs
import net.horizonsend.ion.server.features.misc.Decomposers
import net.horizonsend.ion.server.features.misc.DutyModeMonitor
import net.horizonsend.ion.server.features.misc.GameplayTweaks
import net.horizonsend.ion.server.features.misc.PacketHandler
import net.horizonsend.ion.server.features.misc.Shuttles
import net.horizonsend.ion.server.features.multiblock.Multiblocks
import net.horizonsend.ion.server.features.nations.NationsBalancing
import net.horizonsend.ion.server.features.nations.NationsMap
import net.horizonsend.ion.server.features.nations.NationsMasterTasks
import net.horizonsend.ion.server.features.nations.StationSieges
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.progression.Levels
import net.horizonsend.ion.server.features.progression.PlayerXPLevelCache
import net.horizonsend.ion.server.features.progression.SLXP
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.sidebar.Sidebar
import net.horizonsend.ion.server.features.space.Orbits
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.SpaceMap
import net.horizonsend.ion.server.features.space.SpaceMechanics
import net.horizonsend.ion.server.features.space.SpaceWorlds
import net.horizonsend.ion.server.features.space.spacestations.SpaceStations
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.Hangars
import net.horizonsend.ion.server.features.starship.Interdiction
import net.horizonsend.ion.server.features.starship.LastPilotedStarship
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.StarshipComputers
import net.horizonsend.ion.server.features.starship.StarshipDealers
import net.horizonsend.ion.server.features.starship.StarshipDetection
import net.horizonsend.ion.server.features.starship.active.ActiveStarshipMechanics
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.active.ai.AIManager
import net.horizonsend.ion.server.features.starship.active.ai.AISpawningManager
import net.horizonsend.ion.server.features.starship.active.ai.AIStarshipTemplates
import net.horizonsend.ion.server.features.starship.control.movement.PlayerStarshipControl
import net.horizonsend.ion.server.features.starship.control.movement.StarshipControl
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.features.starship.control.signs.StarshipSignControl
import net.horizonsend.ion.server.features.starship.control.weaponry.PlayerStarshipWeaponry
import net.horizonsend.ion.server.features.starship.control.weaponry.StarshipWeaponry
import net.horizonsend.ion.server.features.starship.factory.StarshipFactories
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.hyperspace.HyperspaceBeacons
import net.horizonsend.ion.server.features.starship.hyperspace.HyperspaceMap
import net.horizonsend.ion.server.features.starship.subsystem.shield.StarshipShields
import net.horizonsend.ion.server.features.transport.Extractors
import net.horizonsend.ion.server.features.transport.TransportConfig
import net.horizonsend.ion.server.features.transport.Wires
import net.horizonsend.ion.server.features.transport.pipe.Pipes
import net.horizonsend.ion.server.features.transport.pipe.filter.Filters
import net.horizonsend.ion.server.features.tutorial.TutorialManager
import net.horizonsend.ion.server.features.waypoint.WaypointManager
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomRecipes
import net.horizonsend.ion.server.miscellaneous.utils.Notify

val components: List<IonComponent> = listOf(
	GameplayTweaks,
	DBManager,
	RedisActions,
	Caches,
	Notify,
	Shuttles,

	PlayerXPLevelCache,
	Levels,
	SLXP,

	CombatNPCs,

	CustomRecipes,
	Crafting,

	SpaceWorlds,
	Space,
	Orbits,

	SpaceMechanics,

	NationsBalancing,
	Regions,

	StationSieges,

	Multiblocks,
	PowerMachines,
	AreaShields,

	TransportConfig.Companion,
	Extractors,
	Pipes,
	Filters,
	Wires,

	Gear,

	TradeCities,

	CollectionMissions,

	CrateRestrictions,

	ShipmentBalancing,
	ShipmentGenerator,
	ShipmentManager,

	Gasses,

	Bazaars,
	Merchants,

	Hyperspace,
	HyperspaceBeacons,

	DeactivatedPlayerStarships,
	ActiveStarships,
	ActiveStarshipMechanics,

	PilotedStarships,
	StarshipDetection,
	StarshipComputers,

	StarshipControl,
	PlayerStarshipControl,
	StarshipWeaponry,
	PlayerStarshipWeaponry,
	StarshipSignControl,

	StarshipShields,
	StarshipCruising,
	Hangars,
	StarshipFactories,
	TutorialManager,
	Interdiction,
	StarshipDealers,
	ShipKillXP,
	Decomposers,
	ChannelSelections,
	LastPilotedStarship,

	DutyModeMonitor,

	SpaceStations,
	Sidebar,
	PacketHandler,
	ModNetworking,

	SpaceMap,
	NationsMap,
	HyperspaceMap,
	HyperspaceBeacons,
	Collectors,
	CityNPCs,
	AreaShields,
	NationsMasterTasks,
    WaypointManager,

	Bounties,

	CustomMobSpawning,

	AISpawningManager,
	AIManager,
	AIStarshipTemplates,
)
