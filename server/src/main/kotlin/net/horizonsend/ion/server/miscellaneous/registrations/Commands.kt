package net.horizonsend.ion.server.miscellaneous.registrations

import net.horizonsend.ion.server.configuration.ConfigurationCommands
import net.horizonsend.ion.server.features.achievements.AchievementsCommand
import net.horizonsend.ion.server.features.client.whereisit.SearchCommand
import net.horizonsend.ion.server.features.customitems.commands.ConvertCommand
import net.horizonsend.ion.server.features.regeneration.RegenerateCommand
import net.horizonsend.ion.server.features.sidebar.command.ContactsCommand
import net.horizonsend.ion.server.features.space.generation.SpaceGenCommand
import net.horizonsend.ion.server.features.misc.NewPlayerProtection
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.command.admin.*
import net.horizonsend.ion.server.command.starship.*
import net.horizonsend.ion.server.command.economy.*
import net.horizonsend.ion.server.command.misc.*
import net.horizonsend.ion.server.command.nations.*
import net.horizonsend.ion.server.command.nations.admin.*
import net.horizonsend.ion.server.command.nations.money.*
import net.horizonsend.ion.server.command.nations.roles.*
import net.horizonsend.ion.server.command.nations.settlementZones.*
import net.horizonsend.ion.server.command.progression.*
import net.horizonsend.ion.server.command.qol.BlastResistanceCommand
import net.horizonsend.ion.server.command.qol.CalcExpCommand
import net.horizonsend.ion.server.command.qol.CheckProtectionCommand
import net.horizonsend.ion.server.command.qol.FixExtractorsCommand
import net.horizonsend.ion.server.command.space.*

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
