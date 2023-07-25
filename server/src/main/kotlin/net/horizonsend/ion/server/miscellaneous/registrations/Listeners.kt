package net.horizonsend.ion.server.miscellaneous.registrations

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.HyperspaceBeaconManager
import net.horizonsend.ion.server.features.achievements.AchievementListeners
import net.horizonsend.ion.server.features.blasters.BlasterListeners
import net.horizonsend.ion.server.features.client.VoidNetwork
import net.horizonsend.ion.server.features.cryopods.CryoPods
import net.horizonsend.ion.server.features.customitems.CustomItemListeners
import net.horizonsend.ion.server.features.qol.RecipeListener
import net.horizonsend.ion.server.features.screens.listeners.InventoryClickListener
import net.horizonsend.ion.server.features.screens.listeners.InventoryCloseListener
import net.horizonsend.ion.server.features.screens.listeners.InventoryDragListener
import net.horizonsend.ion.server.features.screens.listeners.InventoryMoveItemListener
import net.horizonsend.ion.server.features.space.encounters.EncounterManager
import net.horizonsend.ion.server.features.space.generation.SpaceGenerationManager
import net.horizonsend.ion.server.legacy.NewPlayerProtection
import net.horizonsend.ion.server.legacy.listeners.BiomeFixer9001
import net.horizonsend.ion.server.legacy.listeners.BowHitListener
import net.horizonsend.ion.server.legacy.listeners.ChunkLoadListener
import net.horizonsend.ion.server.legacy.listeners.EdenFixer9000
import net.horizonsend.ion.server.miscellaneous.IonWorld
import net.horizonsend.ion.server.miscellaneous.listeners.*
import net.starlegacy.feature.multiblock.misc.TractorBeamMultiblock
import net.starlegacy.listener.gear.DetonatorListener
import net.starlegacy.listener.gear.DoubleJumpListener
import net.starlegacy.listener.gear.PowerArmorListener
import net.starlegacy.listener.gear.PowerToolListener
import net.starlegacy.listener.gear.SwordListener
import net.starlegacy.listener.misc.*
import net.starlegacy.listener.nations.FriendlyFireListener
import net.starlegacy.listener.nations.MovementListener

val listeners = listOf(
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
	RecipeListener(),
	SpaceGenerationManager,
	EncounterManager(),
	ResourcePackListener(),
	VoidNetwork(),
	CryoPods,
	BiomeFixer9001(),
	NewPlayerProtection,
	TractorBeamMultiblock,

	// Achievement Listeners
	AchievementListeners()
)
