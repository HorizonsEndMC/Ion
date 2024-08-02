package net.horizonsend.ion.server.miscellaneous.registrations

import net.horizonsend.ion.server.features.achievements.AchievementListeners
import net.horizonsend.ion.server.features.client.VoidNetwork
import net.horizonsend.ion.server.features.client.networking.packets.WorldPacket
import net.horizonsend.ion.server.features.custom.ItemConverters
import net.horizonsend.ion.server.features.custom.blocks.CustomBlockListeners
import net.horizonsend.ion.server.features.custom.items.blasters.BlasterListeners
import net.horizonsend.ion.server.features.custom.items.mods.ToolModMenu
import net.horizonsend.ion.server.features.machine.CryoPods
import net.horizonsend.ion.server.features.misc.HyperspaceBeaconManager
import net.horizonsend.ion.server.features.multiblock.misc.TractorBeamMultiblock
import net.horizonsend.ion.server.features.space.encounters.EncounterManager
import net.horizonsend.ion.server.features.space.generation.SpaceGenerationManager
import net.horizonsend.ion.server.features.waypoint.WaypointListeners
import net.horizonsend.ion.server.listener.fixers.BiomeFixer9001
import net.horizonsend.ion.server.listener.fixers.CancelListeners
import net.horizonsend.ion.server.listener.fixers.EdenFixer9000
import net.horizonsend.ion.server.listener.fixers.GameplayTweaksListeners
import net.horizonsend.ion.server.listener.gear.DetonatorListener
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
import net.horizonsend.ion.server.miscellaneous.IonWorld
import org.bukkit.event.Listener

val listeners: List<Listener> = listOf(
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

	DetonatorListener,
	DoubleJumpListener,
	PowerArmorListener,
	SwordListener,

	// Ion
	BlasterListeners(),
	CancelListeners(),
	net.horizonsend.ion.server.features.custom.items.CustomItemListeners(),
	CustomBlockListeners,
	ItemConverters,
	EdenFixer9000(),
	GameplayTweaksListeners(),
	HeadListener(),
	HyperspaceBeaconManager,
	IonWorld,
	MiscListeners(),
	SpaceGenerationManager,
	EncounterManager(),
	ResourcePackListener(),
	VoidNetwork(),
	CryoPods,
	BiomeFixer9001(),
	TractorBeamMultiblock,
	PlayerDeathListener,
	ToolModMenu,
	RecipeModifications,

	// Achievement Listeners
	AchievementListeners(),
	WaypointListeners(),

	//Packet Listeners
	WorldPacket
)
