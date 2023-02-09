package net.horizonsend.ion.server.miscellaneous

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.features.HyperspaceBeaconManager
import net.horizonsend.ion.server.features.achievements.AchievementListeners
import net.horizonsend.ion.server.features.blasters.BlasterListeners
import net.horizonsend.ion.server.features.bounties.BountyListener
import net.horizonsend.ion.server.features.customItems.CustomItemListeners
import net.horizonsend.ion.server.features.worlds.WorldListeners
import net.horizonsend.ion.server.legacy.listeners.ChunkLoadListener
import net.horizonsend.ion.server.miscellaneous.listeners.CancelListeners
import net.horizonsend.ion.server.miscellaneous.listeners.GameplayTweaksListeners
import net.horizonsend.ion.server.miscellaneous.listeners.MiscListeners
import net.horizonsend.ion.server.miscellaneous.listeners.ResourcePackListener
import net.horizonsend.ion.server.features.screens.screens.listeners.InventoryClickListener
import net.horizonsend.ion.server.features.screens.screens.listeners.InventoryCloseListener
import net.horizonsend.ion.server.features.screens.screens.listeners.InventoryDragListener
import net.horizonsend.ion.server.features.screens.screens.listeners.InventoryMoveItemListener

val listeners = arrayOf(
	ChunkLoadListener(Ion),
	WorldListeners(),
	HyperspaceBeaconManager,
	InventoryClickListener(),
	InventoryCloseListener(),
	InventoryDragListener(),
	InventoryMoveItemListener(),
	CancelListeners(),
	GameplayTweaksListeners(),
	ResourcePackListener(),
	MiscListeners(),
	CustomItemListeners(),
	BlasterListeners(),
	BountyListener(),

	// Achievement Listeners
	AchievementListeners()
)
