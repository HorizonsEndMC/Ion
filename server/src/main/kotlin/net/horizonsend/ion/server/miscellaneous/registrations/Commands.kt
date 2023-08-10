package net.horizonsend.ion.server.miscellaneous.registrations

import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.command.admin.AdminCommands
import net.horizonsend.ion.server.command.admin.BatteryCommand
import net.horizonsend.ion.server.command.admin.CustomItemCommand
import net.horizonsend.ion.server.command.admin.GracePeriod
import net.horizonsend.ion.server.command.admin.IonCommand
import net.horizonsend.ion.server.command.admin.RemoveGhostShipCommand
import net.horizonsend.ion.server.command.economy.BazaarCommand
import net.horizonsend.ion.server.command.economy.CityNpcCommand
import net.horizonsend.ion.server.command.economy.CollectedItemCommand
import net.horizonsend.ion.server.command.economy.CollectorCommand
import net.horizonsend.ion.server.command.economy.EcoStationCommand
import net.horizonsend.ion.server.command.economy.TradeDebugCommand
import net.horizonsend.ion.server.command.misc.DyeCommand
import net.horizonsend.ion.server.command.misc.GToggleCommand
import net.horizonsend.ion.server.command.misc.GlobalGameRuleCommand
import net.horizonsend.ion.server.command.misc.ListCommand
import net.horizonsend.ion.server.command.misc.MultiblockCommand
import net.horizonsend.ion.server.command.misc.PlayerInfoCommand
import net.horizonsend.ion.server.command.misc.RegenerateCommand
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
import net.horizonsend.ion.server.command.qol.FixExtractorsCommand
import net.horizonsend.ion.server.command.space.PlanetCommand
import net.horizonsend.ion.server.command.space.SpaceWorldCommand
import net.horizonsend.ion.server.command.space.StarCommand
import net.horizonsend.ion.server.command.starship.BlueprintCommand
import net.horizonsend.ion.server.command.starship.MiscStarshipCommands
import net.horizonsend.ion.server.command.starship.RainbowProjectileCommand
import net.horizonsend.ion.server.command.starship.RandomTargetCommand
import net.horizonsend.ion.server.command.starship.StarshipDebugCommand
import net.horizonsend.ion.server.command.starship.StarshipInfoCommand
import net.horizonsend.ion.server.command.starship.Starships
import net.horizonsend.ion.server.command.starship.TutorialStartStopCommand
import net.horizonsend.ion.server.configuration.ConfigurationCommands
import net.horizonsend.ion.server.features.achievements.AchievementsCommand
import net.horizonsend.ion.server.features.client.whereisit.SearchCommand
import net.horizonsend.ion.server.features.customitems.commands.ConvertCommand
import net.horizonsend.ion.server.features.misc.NewPlayerProtection
import net.horizonsend.ion.server.features.sidebar.command.ContactsCommand
import net.horizonsend.ion.server.features.space.generation.SpaceGenCommand

val commands: List<SLCommand> = listOf(
	GToggleCommand,
	PlayerInfoCommand,
	DyeCommand,
	GlobalGameRuleCommand,

	BatteryCommand,
	CustomItemCommand,
	ListCommand,
	TransportDebugCommand,
	ShuttleCommand,
	BuyXPCommand,
	RainbowProjectileCommand,
	RandomTargetCommand,

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
	TutorialStartStopCommand,
	StarshipInfoCommand,

	Starships,
	GracePeriod,
	NewPlayerProtection,
	AdminCommands,

	MultiblockCommand,
	SpaceGenCommand,
	ConfigurationCommands,
	ConvertCommand,
	net.horizonsend.ion.server.features.customitems.commands.CustomItemCommand,
	IonCommand,
	SearchCommand,
	CalcExpCommand,
	CheckProtectionCommand,
	FixExtractorsCommand,
	RegenerateCommand,
	RemoveGhostShipCommand,

	AchievementsCommand,
	BlastResistanceCommand,
	ContactsCommand
)
