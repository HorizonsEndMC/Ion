package net.horizonsend.ion.server.miscellaneous

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonWorld
import net.horizonsend.ion.server.features.HyperspaceBeaconManager
import net.horizonsend.ion.server.features.achievements.AchievementListeners
import net.horizonsend.ion.server.features.blasters.BlasterListeners
import net.horizonsend.ion.server.features.bounties.BountyListener
import net.horizonsend.ion.server.features.client.VoidNetwork
import net.horizonsend.ion.server.features.customitems.CustomItemListeners
import net.horizonsend.ion.server.features.qol.RecipeListener
import net.horizonsend.ion.server.features.screens.listeners.InventoryClickListener
import net.horizonsend.ion.server.features.screens.listeners.InventoryCloseListener
import net.horizonsend.ion.server.features.screens.listeners.InventoryDragListener
import net.horizonsend.ion.server.features.screens.listeners.InventoryMoveItemListener
import net.horizonsend.ion.server.legacy.listeners.ChunkLoadListener
import net.horizonsend.ion.server.legacy.listeners.EdenFixer9000
import net.horizonsend.ion.server.miscellaneous.listeners.CancelListeners
import net.horizonsend.ion.server.miscellaneous.listeners.GameplayTweaksListeners
import net.horizonsend.ion.server.miscellaneous.listeners.HeadListener
import net.horizonsend.ion.server.miscellaneous.listeners.MiscListeners
import net.horizonsend.ion.server.miscellaneous.listeners.ResourcePackListener

val listeners = arrayOf(
	BlasterListeners(),
	BountyListener(),
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
	ResourcePackListener(),
	VoidNetwork(),

	// Achievement Listeners
	AchievementListeners()
)
