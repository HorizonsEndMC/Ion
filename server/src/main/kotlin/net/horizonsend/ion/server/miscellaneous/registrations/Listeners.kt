package net.horizonsend.ion.server.miscellaneous.registrations

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.misc.HyperspaceBeaconManager
import net.horizonsend.ion.server.features.achievements.AchievementListeners
import net.horizonsend.ion.server.features.blasters.BlasterListeners
import net.horizonsend.ion.server.features.client.VoidNetwork
import net.horizonsend.ion.server.features.cryopods.CryoPods
import net.horizonsend.ion.server.features.customitems.CustomItemListeners
import net.horizonsend.ion.server.features.screens.listeners.InventoryClickListener
import net.horizonsend.ion.server.features.screens.listeners.InventoryCloseListener
import net.horizonsend.ion.server.features.screens.listeners.InventoryDragListener
import net.horizonsend.ion.server.features.screens.listeners.InventoryMoveItemListener
import net.horizonsend.ion.server.features.space.encounters.EncounterManager
import net.horizonsend.ion.server.features.space.generation.SpaceGenerationManager
import net.horizonsend.ion.server.listener.fixers.BiomeFixer9001
import net.horizonsend.ion.server.listener.misc.BowHitListener
import net.horizonsend.ion.server.features.ores.ChunkLoadListener
import net.horizonsend.ion.server.listener.fixers.EdenFixer9000
import net.horizonsend.ion.server.listener.gear.*
import net.horizonsend.ion.server.listener.misc.*
import net.horizonsend.ion.server.miscellaneous.IonWorld
import net.horizonsend.ion.server.miscellaneous.listeners.*
import net.horizonsend.ion.server.features.multiblock.misc.TractorBeamMultiblock
import net.horizonsend.ion.server.listener.nations.FriendlyFireListener
import net.horizonsend.ion.server.listener.nations.MovementListener
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
	PowerToolListener,
	SwordListener,

	// Ion
	BlasterListeners(),
	CancelListeners(),
	ChunkLoadListener(IonServer),
	CustomItemListeners(),
	EdenFixer9000(),
	GameplayTweaksListeners(),
	HeadListener(),
	HyperspaceBeaconManager,
	InventoryClickListener(),
	InventoryCloseListener(),
	InventoryDragListener(),
	InventoryMoveItemListener(),
	IonWorld,
	MiscListeners(),
	SpaceGenerationManager,
	EncounterManager(),
	ResourcePackListener(),
	VoidNetwork(),
	CryoPods,
	BiomeFixer9001(),
	TractorBeamMultiblock,

	// Achievement Listeners
	AchievementListeners()
)
