package net.starlegacy

import net.horizonsend.ion.server.legacy.NewPlayerProtection
import net.horizonsend.ion.server.legacy.commands.AdminCommands
import net.horizonsend.ion.server.legacy.commands.GracePeriod
import net.horizonsend.ion.server.legacy.commands.RainbowProjectileCommand
import net.horizonsend.ion.server.legacy.commands.RandomTargetCommand
import net.horizonsend.ion.server.legacy.commands.Starships
import net.starlegacy.command.economy.BazaarCommand
import net.starlegacy.command.economy.CityNpcCommand
import net.starlegacy.command.economy.CollectedItemCommand
import net.starlegacy.command.economy.CollectorCommand
import net.starlegacy.command.economy.EcoStationCommand
import net.starlegacy.command.economy.TradeDebugCommand
import net.starlegacy.command.misc.BatteryCommand
import net.starlegacy.command.misc.CustomItemCommand
import net.starlegacy.command.misc.DyeCommand
import net.starlegacy.command.misc.GToggleCommand
import net.starlegacy.command.misc.GlobalGameRuleCommand
import net.starlegacy.command.misc.ListCommand
import net.starlegacy.command.misc.PlayerInfoCommand
import net.starlegacy.command.misc.ShuttleCommand
import net.starlegacy.command.misc.TransportDebugCommand
import net.starlegacy.command.nations.NationCommand
import net.starlegacy.command.nations.NationRelationCommand
import net.starlegacy.command.nations.SpaceStationCommand
import net.starlegacy.command.nations.SettlementCommand
import net.starlegacy.command.nations.SiegeCommand
import net.starlegacy.command.nations.admin.CityManageCommand
import net.starlegacy.command.nations.admin.NPCOwnerCommand
import net.starlegacy.command.nations.admin.NationAdminCommand
import net.starlegacy.command.nations.money.NationMoneyCommand
import net.starlegacy.command.nations.money.SettlementMoneyCommand
import net.starlegacy.command.nations.roles.NationRoleCommand
import net.starlegacy.command.nations.roles.SettlementRoleCommand
import net.starlegacy.command.nations.settlementZones.SettlementPlotCommand
import net.starlegacy.command.nations.settlementZones.SettlementZoneCommand
import net.starlegacy.command.progression.AdvanceAdminCommand
import net.starlegacy.command.progression.BuyXPCommand
import net.starlegacy.command.progression.XPCommand
import net.starlegacy.command.space.MoonCommand
import net.starlegacy.command.space.PlanetCommand
import net.starlegacy.command.space.SpaceWorldCommand
import net.starlegacy.command.space.StarCommand
import net.starlegacy.command.starship.BlueprintCommand
import net.starlegacy.command.starship.MiscStarshipCommands
import net.starlegacy.command.starship.StarshipDebugCommand
import net.starlegacy.command.starship.StarshipInfoCommand
import net.starlegacy.command.starship.TutorialStartStopCommand

val commands = listOf(
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

	MoonCommand,
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

	Starships(),
	GracePeriod,
	NewPlayerProtection,
	AdminCommands
)
