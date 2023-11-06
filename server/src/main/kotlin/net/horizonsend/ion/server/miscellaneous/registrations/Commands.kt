package net.horizonsend.ion.server.miscellaneous.registrations

import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.command.admin.AdminCommands
import net.horizonsend.ion.server.command.admin.BatteryCommand
import net.horizonsend.ion.server.command.admin.ConvertCommand
import net.horizonsend.ion.server.command.admin.CustomItemCommand
import net.horizonsend.ion.server.command.admin.GracePeriod
import net.horizonsend.ion.server.command.admin.IonCommand
import net.horizonsend.ion.server.command.admin.ItemDebugCommand
import net.horizonsend.ion.server.command.admin.RemoveGhostShipCommand
import net.horizonsend.ion.server.command.admin.WorldCommand
import net.horizonsend.ion.server.command.economy.BazaarCommand
import net.horizonsend.ion.server.command.economy.CityNpcCommand
import net.horizonsend.ion.server.command.economy.CollectedItemCommand
import net.horizonsend.ion.server.command.economy.CollectorCommand
import net.horizonsend.ion.server.command.economy.EcoStationCommand
import net.horizonsend.ion.server.command.economy.TradeDebugCommand
import net.horizonsend.ion.server.command.misc.BlockCommand
import net.horizonsend.ion.server.command.misc.BountyAdminCommand
import net.horizonsend.ion.server.command.misc.BountyCommand
import net.horizonsend.ion.server.command.misc.DyeCommand
import net.horizonsend.ion.server.command.misc.GToggleCommand
import net.horizonsend.ion.server.command.misc.GlobalGameRuleCommand
import net.horizonsend.ion.server.command.misc.HelpCommand
import net.horizonsend.ion.server.command.misc.IonBroadcastCommand
import net.horizonsend.ion.server.command.misc.ListCommand
import net.horizonsend.ion.server.command.misc.LocatorCommands
import net.horizonsend.ion.server.command.misc.MultiblockCommand
import net.horizonsend.ion.server.command.misc.PlayerInfoCommand
import net.horizonsend.ion.server.command.misc.RegenerateCommand
import net.horizonsend.ion.server.command.misc.ShipFactoryCommand
import net.horizonsend.ion.server.command.misc.ShuttleCommand
import net.horizonsend.ion.server.command.misc.TransportDebugCommand
import net.horizonsend.ion.server.command.nations.NationCommand
import net.horizonsend.ion.server.command.nations.NationRelationCommand
import net.horizonsend.ion.server.command.nations.SettlementCommand
import net.horizonsend.ion.server.command.nations.SiegeCommand
import net.horizonsend.ion.server.command.nations.SpaceStationCommand
import net.horizonsend.ion.server.command.nations.admin.CityManageCommand
import net.horizonsend.ion.server.command.nations.admin.NPCOwnerCommand
import net.horizonsend.ion.server.command.nations.admin.NationAdminCommand
import net.horizonsend.ion.server.command.nations.money.NationMoneyCommand
import net.horizonsend.ion.server.command.nations.money.SettlementMoneyCommand
import net.horizonsend.ion.server.command.nations.roles.NationRoleCommand
import net.horizonsend.ion.server.command.nations.roles.SettlementRoleCommand
import net.horizonsend.ion.server.command.nations.settlementZones.SettlementPlotCommand
import net.horizonsend.ion.server.command.nations.settlementZones.SettlementZoneCommand
import net.horizonsend.ion.server.command.progression.AdvanceAdminCommand
import net.horizonsend.ion.server.command.progression.BuyXPCommand
import net.horizonsend.ion.server.command.progression.XPCommand
import net.horizonsend.ion.server.command.qol.BlastResistanceCommand
import net.horizonsend.ion.server.command.qol.CalcExpCommand
import net.horizonsend.ion.server.command.qol.CheckProtectionCommand
import net.horizonsend.ion.server.command.qol.ContainerCommand
import net.horizonsend.ion.server.command.qol.FixExtractorsCommand
import net.horizonsend.ion.server.command.qol.SearchCommand
import net.horizonsend.ion.server.command.qol.SetPowerCommand
import net.horizonsend.ion.server.command.space.PlanetCommand
import net.horizonsend.ion.server.command.space.SpaceWorldCommand
import net.horizonsend.ion.server.command.space.StarCommand
import net.horizonsend.ion.server.command.starship.BlueprintCommand
import net.horizonsend.ion.server.command.starship.MiscStarshipCommands
import net.horizonsend.ion.server.command.starship.RainbowProjectileCommand
import net.horizonsend.ion.server.command.starship.StarshipDebugCommand
import net.horizonsend.ion.server.command.starship.StarshipInfoCommand
import net.horizonsend.ion.server.command.starship.StarshipsCommand
import net.horizonsend.ion.server.command.starship.TutorialStartStopCommand
import net.horizonsend.ion.server.command.starship.ai.AIDebugCommand
import net.horizonsend.ion.server.configuration.ConfigurationCommands
import net.horizonsend.ion.server.features.achievements.AchievementsCommand
import net.horizonsend.ion.server.features.client.commands.HudCommand
import net.horizonsend.ion.server.features.gui.custom.settings.commands.SettingsCommand
import net.horizonsend.ion.server.features.misc.NewPlayerProtection
import net.horizonsend.ion.server.features.sidebar.command.BookmarkCommand
import net.horizonsend.ion.server.features.sidebar.command.SidebarCommand
import net.horizonsend.ion.server.features.sidebar.command.SidebarContactsCommand
import net.horizonsend.ion.server.features.sidebar.command.SidebarStarshipsCommand
import net.horizonsend.ion.server.features.sidebar.command.SidebarWaypointsCommand
import net.horizonsend.ion.server.features.space.generation.SpaceGenCommand
import net.horizonsend.ion.server.features.starship.fleet.FleetCommand
import net.horizonsend.ion.server.features.waypoint.command.WaypointCommand

val commands: List<SLCommand> = listOf(
	GToggleCommand,
	PlayerInfoCommand,
	DyeCommand,
	GlobalGameRuleCommand,

	BatteryCommand,
	ListCommand,
	TransportDebugCommand,
	ShuttleCommand,
	BuyXPCommand,
	RainbowProjectileCommand,

	SettlementCommand,
	NationCommand,
	SpaceStationCommand,
	NationRelationCommand,

	CityManageCommand,
	NationAdminCommand,
	NPCOwnerCommand,

	NationMoneyCommand,
	SettlementMoneyCommand,

	NationRoleCommand,
	SettlementRoleCommand,

	SettlementPlotCommand,
	SettlementZoneCommand,

	SiegeCommand,

	AdvanceAdminCommand,
	XPCommand,

	PlanetCommand,
	SpaceWorldCommand,
	StarCommand,

	BazaarCommand,
	CityNpcCommand,
	CollectedItemCommand,
	CollectorCommand,
	EcoStationCommand,
	TradeDebugCommand,

	MiscStarshipCommands,
	BlueprintCommand,
	StarshipDebugCommand,
	AIDebugCommand,
	TutorialStartStopCommand,
	StarshipInfoCommand,

	StarshipsCommand,
	GracePeriod,
	NewPlayerProtection,
	AdminCommands,

	MultiblockCommand,
	SpaceGenCommand,
	ConfigurationCommands,
	WorldCommand,
	ConvertCommand,
	CustomItemCommand,
	IonCommand,
	SearchCommand,
	ContainerCommand,
	CalcExpCommand,
	CheckProtectionCommand,
	FixExtractorsCommand,
	SetPowerCommand,
	RegenerateCommand,
	RemoveGhostShipCommand,

	AchievementsCommand,
	BlastResistanceCommand,
	SidebarCommand,
	SidebarContactsCommand,
	SidebarWaypointsCommand,
	SidebarStarshipsCommand,
    WaypointCommand,
	BookmarkCommand,
	HudCommand,
    BountyCommand,
	BountyAdminCommand,

	IonBroadcastCommand,
	BlockCommand,
	ShipFactoryCommand,
	SettingsCommand,
	FleetCommand,
	LocatorCommands,
	ItemDebugCommand,
	HelpCommand,
)
