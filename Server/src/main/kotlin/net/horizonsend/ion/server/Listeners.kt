package net.horizonsend.ion.server

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.listeners.*
import net.horizonsend.ion.server.listeners.PlayerJoinListener
import net.horizonsend.ion.server.legacy.listeners.*

val listeners = arrayOf(
	BlockFadeListener(),
	BlockFormListener(),
	ChunkLoadListener(Ion),
	EnchantItemListener(),
	InventoryClickListener(),
	InventoryCloseListener(),
	InventoryDragListener(),
	InventoryMoveItemListener(),
	PlayerAttemptPickupItemListener(),
	PlayerDeathListener(),
	PlayerFishListener(),
	PlayerItemConsumeListener(),
	PlayerJoinListener(Ion),
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
	CaptureStationListener(),
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