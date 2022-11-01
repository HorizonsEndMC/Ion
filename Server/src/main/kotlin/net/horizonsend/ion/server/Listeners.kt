package net.horizonsend.ion.server

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.listeners.bukkit.BlockFadeListener
import net.horizonsend.ion.server.listeners.bukkit.BlockFormListener
import net.horizonsend.ion.server.listeners.bukkit.ChunkLoadListener
import net.horizonsend.ion.server.listeners.bukkit.EnchantItemListener
import net.horizonsend.ion.server.listeners.bukkit.InventoryClickListener
import net.horizonsend.ion.server.listeners.bukkit.InventoryCloseListener
import net.horizonsend.ion.server.listeners.bukkit.InventoryDragListener
import net.horizonsend.ion.server.listeners.bukkit.InventoryMoveItemListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerDeathListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerFishListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerItemConsumeListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerJoinListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerLoginListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerPickUpItemListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerQuitListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerResourcePackStatusListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerTeleportListener
import net.horizonsend.ion.server.listeners.bukkit.PotionSplashListener
import net.horizonsend.ion.server.listeners.bukkit.PrepareItemCraftListener
import net.horizonsend.ion.server.listeners.bukkit.PrepareItemEnchantListener
import net.horizonsend.ion.server.listeners.bukkit.WorldInitListener
import net.horizonsend.ion.server.listeners.bukkit.WorldLoadListener
import net.horizonsend.ion.server.listeners.bukkit.WorldUnloadListener
import net.horizonsend.ion.server.listeners.ioncore.BuySpawnShuttleListener
import net.horizonsend.ion.server.listeners.ioncore.CaptureStationListener
import net.horizonsend.ion.server.listeners.ioncore.CompleteCargoRunListener
import net.horizonsend.ion.server.listeners.ioncore.CreateNationListener
import net.horizonsend.ion.server.listeners.ioncore.CreateNationOutpostListener
import net.horizonsend.ion.server.listeners.ioncore.CreateSettlementListener
import net.horizonsend.ion.server.listeners.ioncore.DetectShipListener
import net.horizonsend.ion.server.listeners.ioncore.EnterPlanetListener
import net.horizonsend.ion.server.listeners.ioncore.HyperspaceEnterListener
import net.horizonsend.ion.server.listeners.ioncore.LevelUpListener
import net.horizonsend.ion.server.listeners.ioncore.MultiblockDetectListener
import net.horizonsend.ion.server.listeners.ioncore.ShipKillListener
import net.horizonsend.ion.server.listeners.ioncore.StationSiegeBeginListener

val listeners = arrayOf(
	BlockFadeListener(),
	BlockFormListener(),
	ChunkLoadListener(Ion),
	EnchantItemListener(),
	InventoryClickListener(),
	InventoryCloseListener(),
	InventoryDragListener(),
	InventoryMoveItemListener(),
	PlayerDeathListener(),
	PlayerFishListener(),
	PlayerItemConsumeListener(),
	PlayerJoinListener(Ion),
	PlayerLoginListener(),
	PlayerPickUpItemListener(),
	PlayerQuitListener(),
	PlayerResourcePackStatusListener(),
	PlayerTeleportListener(),
	PotionSplashListener(),
	PrepareItemCraftListener(),
	PrepareItemEnchantListener(),
	WorldInitListener(),
	WorldLoadListener(),
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