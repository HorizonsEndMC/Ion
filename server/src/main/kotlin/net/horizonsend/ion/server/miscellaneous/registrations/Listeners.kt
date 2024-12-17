package net.horizonsend.ion.server.miscellaneous.registrations

import net.horizonsend.ion.server.features.client.VoidNetwork
import net.horizonsend.ion.server.features.client.networking.packets.WorldPacket
import net.horizonsend.ion.server.features.custom.blocks.CustomBlockListeners
import net.horizonsend.ion.server.features.custom.items.NewCustomItemListeners
import net.horizonsend.ion.server.features.custom.items.type.PersonalTransporterManager
import net.horizonsend.ion.server.features.custom.items.type.weapon.blaster.BlasterListeners
import net.horizonsend.ion.server.features.machine.CryoPods
import net.horizonsend.ion.server.features.multiblock.type.misc.AbstractTractorBeam
import net.horizonsend.ion.server.features.progression.achievements.AchievementListeners
import net.horizonsend.ion.server.features.space.encounters.EncounterManager
import net.horizonsend.ion.server.features.starship.control.controllers.player.ActivePlayerController
import net.horizonsend.ion.server.features.starship.hyperspace.HyperspaceBeaconManager
import net.horizonsend.ion.server.features.waypoint.WaypointListeners
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.generation.WorldGenerationManager
import net.horizonsend.ion.server.listener.fixers.BiomeFixer9001
import net.horizonsend.ion.server.listener.fixers.CancelListeners
import net.horizonsend.ion.server.listener.fixers.EdenFixer9000
import net.horizonsend.ion.server.listener.fixers.GameplayTweaksListeners
import net.horizonsend.ion.server.listener.gear.DoubleJumpListener
import net.horizonsend.ion.server.listener.gear.PowerArmorListener
import net.horizonsend.ion.server.listener.gear.SwordListener
import net.horizonsend.ion.server.listener.misc.BlockListener
import net.horizonsend.ion.server.listener.misc.BowHitListener
import net.horizonsend.ion.server.listener.misc.ChatListener
import net.horizonsend.ion.server.listener.misc.EntityListener
import net.horizonsend.ion.server.listener.misc.FurnaceListener
import net.horizonsend.ion.server.listener.misc.HeadListener
import net.horizonsend.ion.server.listener.misc.InteractListener
import net.horizonsend.ion.server.listener.misc.InventoryListener
import net.horizonsend.ion.server.listener.misc.JoinLeaveListener
import net.horizonsend.ion.server.listener.misc.MiscListeners
import net.horizonsend.ion.server.listener.misc.PlayerDeathListener
import net.horizonsend.ion.server.listener.misc.ProtectionListener
import net.horizonsend.ion.server.listener.misc.RecipeModifications
import net.horizonsend.ion.server.listener.misc.ResourcePackListener
import net.horizonsend.ion.server.listener.nations.FriendlyFireListener
import net.horizonsend.ion.server.listener.nations.MovementListener
import org.bukkit.event.Listener

val listeners: List<Listener> = listOf(
	IonWorld,

	// StarLegacy
	JoinLeaveListener,
	MovementListener,
	FriendlyFireListener,
	ProtectionListener,
	ChatListener,

	BlockListener,
	EntityListener,
	FurnaceListener,
	InteractListener,
	InventoryListener,
	BowHitListener,

	DoubleJumpListener,
	PowerArmorListener,
	SwordListener,

	// Ion
	BlasterListeners(),
	CancelListeners(),
	NewCustomItemListeners,
	CustomBlockListeners,
	EdenFixer9000(),
	GameplayTweaksListeners(),
	HeadListener(),
	HyperspaceBeaconManager,
	MiscListeners(),
	WorldGenerationManager,
	EncounterManager(),
	ResourcePackListener(),
	VoidNetwork(),
	CryoPods,
	BiomeFixer9001(),
	AbstractTractorBeam.Companion,
	PlayerDeathListener,
	net.horizonsend.ion.server.features.custom.items.type.tool.mods.ToolModMenu,
	RecipeModifications,
	ActivePlayerController.Companion,
	PersonalTransporterManager,

	// Achievement Listeners
	AchievementListeners(),
	WaypointListeners(),

	//Packet Listeners
	WorldPacket
)
