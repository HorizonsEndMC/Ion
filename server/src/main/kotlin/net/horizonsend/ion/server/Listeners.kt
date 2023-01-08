package net.horizonsend.ion.server

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.legacy.listeners.BuySpawnShuttleListener
import net.horizonsend.ion.server.legacy.listeners.ChunkLoadListener
import net.horizonsend.ion.server.legacy.listeners.CompleteCargoRunListener
import net.horizonsend.ion.server.legacy.listeners.CreateNationListener
import net.horizonsend.ion.server.legacy.listeners.CreateNationOutpostListener
import net.horizonsend.ion.server.legacy.listeners.CreateSettlementListener
import net.horizonsend.ion.server.legacy.listeners.DetectShipListener
import net.horizonsend.ion.server.legacy.listeners.EnterPlanetListener
import net.horizonsend.ion.server.legacy.listeners.HyperspaceEnterListener
import net.horizonsend.ion.server.legacy.listeners.LevelUpListener
import net.horizonsend.ion.server.legacy.listeners.MultiblockDetectListener
import net.horizonsend.ion.server.legacy.listeners.ShipKillListener
import net.horizonsend.ion.server.legacy.listeners.StationSiegeBeginListener
import net.horizonsend.ion.server.listeners.AsteroidChunkLoadListener
import net.horizonsend.ion.server.listeners.BlockFadeListener
import net.horizonsend.ion.server.listeners.BlockFormListener
import net.horizonsend.ion.server.listeners.EnchantItemListener
import net.horizonsend.ion.server.listeners.EntityDamageListener
import net.horizonsend.ion.server.listeners.InventoryClickListener
import net.horizonsend.ion.server.listeners.InventoryCloseListener
import net.horizonsend.ion.server.listeners.InventoryDragListener
import net.horizonsend.ion.server.listeners.InventoryMoveItemListener
import net.horizonsend.ion.server.listeners.PlayerAttemptPickupItemListener
import net.horizonsend.ion.server.listeners.PlayerDeathListener
import net.horizonsend.ion.server.listeners.PlayerFishListener
import net.horizonsend.ion.server.listeners.PlayerInteractListener
import net.horizonsend.ion.server.listeners.PlayerItemConsumeListener
import net.horizonsend.ion.server.listeners.PlayerItemHoldListener
import net.horizonsend.ion.server.listeners.PlayerItemSwapListener
import net.horizonsend.ion.server.listeners.PlayerJoinListener
import net.horizonsend.ion.server.listeners.PlayerLoginListener
import net.horizonsend.ion.server.listeners.PlayerQuitListener
import net.horizonsend.ion.server.listeners.PlayerResourcePackStatusListener
import net.horizonsend.ion.server.listeners.PlayerTeleportListener
import net.horizonsend.ion.server.listeners.PotionSplashListener
import net.horizonsend.ion.server.listeners.PrepareItemCraftListener
import net.horizonsend.ion.server.listeners.PrepareItemEnchantListener
import net.horizonsend.ion.server.listeners.WorldInitListener
import net.horizonsend.ion.server.listeners.WorldUnloadListener
import net.horizonsend.ion.server.managers.HyperspaceBeaconManager

val listeners = arrayOf(
	BlockFadeListener(),
	BlockFormListener(),
	ChunkLoadListener(Ion),
	AsteroidChunkLoadListener(),
	EnchantItemListener(),
	EntityDamageListener(),
	HyperspaceBeaconManager,
	InventoryClickListener(),
	InventoryCloseListener(),
	InventoryDragListener(),
	InventoryMoveItemListener(),
	PlayerAttemptPickupItemListener(),
	PlayerDeathListener(),
	PlayerItemSwapListener(),
	PlayerFishListener(),
	PlayerItemConsumeListener(),
	PlayerItemHoldListener(),
	PlayerInteractListener(),
	PlayerJoinListener(),
	PlayerLoginListener(),
	PlayerQuitListener(),
	PlayerResourcePackStatusListener(),
	PlayerTeleportListener(),
	PotionSplashListener(),
	PrepareItemCraftListener(),
	PrepareItemEnchantListener(),
	WorldInitListener(),
	WorldUnloadListener(),

	BuySpawnShuttleListener(),
	CompleteCargoRunListener(),
	CreateNationListener(),
	CreateNationOutpostListener(),
	CreateSettlementListener(),
	DetectShipListener(),
	EnterPlanetListener(),
	HyperspaceEnterListener(),
	LevelUpListener(),
	MultiblockDetectListener(),
	ShipKillListener(),
	StationSiegeBeginListener()
)
