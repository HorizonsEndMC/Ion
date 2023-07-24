package net.horizonsend.ion.server.miscellaneous.registrations

import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.DBManager
import net.horizonsend.ion.common.utils.redisaction.RedisActions
import net.horizonsend.ion.server.features.cache.Caches
import net.horizonsend.ion.server.features.sidebar.Sidebar
import net.horizonsend.ion.server.features.spacestations.SpaceStations
import net.horizonsend.ion.server.features.CombatNPCs
import net.starlegacy.feature.chat.ChannelSelections
import net.starlegacy.feature.economy.bazaar.Bazaars
import net.starlegacy.feature.economy.bazaar.Merchants
import net.starlegacy.feature.economy.cargotrade.CrateRestrictions
import net.starlegacy.feature.economy.cargotrade.ShipmentBalancing
import net.starlegacy.feature.economy.cargotrade.ShipmentGenerator
import net.starlegacy.feature.economy.cargotrade.ShipmentManager
import net.starlegacy.feature.economy.city.TradeCities
import net.starlegacy.feature.economy.collectors.CollectionMissions
import net.starlegacy.feature.gear.Gear
import net.starlegacy.feature.hyperspace.HyperspaceBeacons
import net.starlegacy.feature.machine.AreaShields
import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.feature.misc.*
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.feature.nations.NationsBalancing
import net.starlegacy.feature.nations.StationSieges
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.progression.Levels
import net.starlegacy.feature.progression.PlayerXPLevelCache
import net.starlegacy.feature.progression.SLXP
import net.starlegacy.feature.progression.ShipKillXP
import net.starlegacy.feature.space.Orbits
import net.starlegacy.feature.space.Space
import net.starlegacy.feature.space.SpaceMechanics
import net.starlegacy.feature.space.SpaceWorlds
import net.starlegacy.feature.starship.*
import net.starlegacy.feature.starship.active.ActiveStarshipMechanics
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.control.StarshipControl
import net.starlegacy.feature.starship.control.StarshipCruising
import net.starlegacy.feature.starship.factory.StarshipFactories
import net.starlegacy.feature.starship.hyperspace.Hyperspace
import net.starlegacy.feature.starship.subsystem.shield.StarshipShields
import net.starlegacy.feature.transport.Extractors
import net.starlegacy.feature.transport.TransportConfig
import net.starlegacy.feature.transport.Wires
import net.starlegacy.feature.transport.pipe.Pipes
import net.starlegacy.feature.transport.pipe.filter.Filters
import net.starlegacy.feature.tutorial.TutorialManager
import net.starlegacy.util.Notify

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

	DutyModeMonitor,

	SpaceStations,
	Sidebar
)
