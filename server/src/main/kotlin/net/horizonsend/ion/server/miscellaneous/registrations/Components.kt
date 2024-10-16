package net.horizonsend.ion.server.miscellaneous.registrations

import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.DBManager
import net.horizonsend.ion.common.redis.RedisActions
import net.horizonsend.ion.server.data.migrator.DataMigrators
import net.horizonsend.ion.server.features.ai.spawning.AISpawningManager
import net.horizonsend.ion.server.features.ai.spawning.spawner.AISpawners
import net.horizonsend.ion.server.features.cache.Caches
import net.horizonsend.ion.server.features.chat.ChannelSelections
import net.horizonsend.ion.server.features.chat.ChatChannel
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.client.display.HudIcons
import net.horizonsend.ion.server.features.client.display.modular.MultiBlockDisplay
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.type.weapon.sword.SwordListener
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
import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.machine.AntiAirCannons
import net.horizonsend.ion.server.features.machine.AreaShields
import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.machine.decomposer.Decomposers
import net.horizonsend.ion.server.features.misc.AutoRestart
import net.horizonsend.ion.server.features.misc.CapturableStationCache
import net.horizonsend.ion.server.features.misc.GameplayTweaks
import net.horizonsend.ion.server.features.misc.ProxyMessaging
import net.horizonsend.ion.server.features.misc.Shuttles
import net.horizonsend.ion.server.features.misc.UnusedSoldShipPurge
import net.horizonsend.ion.server.features.multiblock.Multiblocks
import net.horizonsend.ion.server.features.multiblock.type.crafting.MultiblockRecipes
import net.horizonsend.ion.server.features.nations.NationsBalancing
import net.horizonsend.ion.server.features.nations.NationsMap
import net.horizonsend.ion.server.features.nations.NationsMasterTasks
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.sieges.SolarSieges
import net.horizonsend.ion.server.features.nations.sieges.StationSieges
import net.horizonsend.ion.server.features.npcs.StarshipDealers
import net.horizonsend.ion.server.features.npcs.TutorialNPCs
import net.horizonsend.ion.server.features.npcs.traits.NPCTraits
import net.horizonsend.ion.server.features.ores.generation.OreGeneration
import net.horizonsend.ion.server.features.player.CombatNPCs
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.features.player.DutyModeMonitor
import net.horizonsend.ion.server.features.player.EventLogger
import net.horizonsend.ion.server.features.player.NMSAchievements
import net.horizonsend.ion.server.features.progression.Levels
import net.horizonsend.ion.server.features.progression.PlayerXPLevelCache
import net.horizonsend.ion.server.features.progression.SLXP
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.progression.bounties.Bounties
import net.horizonsend.ion.server.features.sidebar.Sidebar
import net.horizonsend.ion.server.features.sidebar.tasks.ContactsJammingSidebar
import net.horizonsend.ion.server.features.space.Orbits
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.SpaceMap
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.Hangars
import net.horizonsend.ion.server.features.starship.Interdiction
import net.horizonsend.ion.server.features.starship.LastPilotedStarship
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.StarshipComputers
import net.horizonsend.ion.server.features.starship.StarshipDetection
import net.horizonsend.ion.server.features.starship.active.ActiveStarshipMechanics
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.active.StarshipDisplay
import net.horizonsend.ion.server.features.starship.control.movement.PlayerStarshipControl
import net.horizonsend.ion.server.features.starship.control.movement.StarshipControl
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.features.starship.control.signs.StarshipSignControl
import net.horizonsend.ion.server.features.starship.control.weaponry.PlayerStarshipWeaponry
import net.horizonsend.ion.server.features.starship.control.weaponry.StarshipWeaponry
import net.horizonsend.ion.server.features.starship.factory.StarshipFactories
import net.horizonsend.ion.server.features.starship.fleet.Fleets
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.hyperspace.HyperspaceBeacons
import net.horizonsend.ion.server.features.starship.movement.MovementScheduler
import net.horizonsend.ion.server.features.starship.movement.PlanetTeleportCooldown
import net.horizonsend.ion.server.features.starship.subsystem.shield.StarshipShields
import net.horizonsend.ion.server.features.transport.Extractors
import net.horizonsend.ion.server.features.transport.TransportConfig
import net.horizonsend.ion.server.features.transport.Wires
import net.horizonsend.ion.server.features.transport.pipe.Pipes
import net.horizonsend.ion.server.features.transport.pipe.filter.Filters
import net.horizonsend.ion.server.features.tutorial.Tutorials
import net.horizonsend.ion.server.features.waypoint.WaypointManager
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomRecipes
import net.horizonsend.ion.server.miscellaneous.utils.Discord
import net.horizonsend.ion.server.miscellaneous.utils.Notify

val components: List<IonComponent> = listOf(
	GameplayTweaks,
	DBManager,
	RedisActions,
	AutoRestart,
	Caches,
	Discord,
	Notify,
	Shuttles,
	ProxyMessaging,

	PlayerXPLevelCache,
	Levels,
	SLXP,

	NPCTraits,
	CombatNPCs,

	CustomItemRegistry,
	DataMigrators,
	CustomRecipes,
	Crafting,
	NMSAchievements,

	Space,
	Orbits,

	NationsBalancing,
	Regions,

	StationSieges,
	SolarSieges,

	Multiblocks,
	MultiblockRecipes,
	PowerMachines,
	AreaShields,

	TransportConfig.Companion,
	Extractors,
	Pipes,
	Filters,
	Wires,

	SwordListener,

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
	MovementScheduler,
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
	Tutorials,
	Interdiction,
	StarshipDealers,
	TutorialNPCs,
	ShipKillXP,
	Decomposers,

	ChatChannel.ChannelActions,
	ChannelSelections,

	LastPilotedStarship,

	DutyModeMonitor,
	EventLogger,
	Sidebar,

	SpaceMap,
	NationsMap,
	HyperspaceBeacons,
	Collectors,
	CityNPCs,
	AreaShields,
	NationsMasterTasks,
    WaypointManager,

	Bounties,

	AISpawners,
	AISpawningManager,
	StarshipDisplay,

	AntiAirCannons,
	OreGeneration,
	CapturableStationCache,
	UnusedSoldShipPurge,
	ClientDisplayEntities,
	HudIcons,
	Fleets,
	ContactsJammingSidebar,
	CombatTimer,
	PlanetTeleportCooldown,
	MultiBlockDisplay,
)
