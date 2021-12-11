package net.starlegacy.feature.machine

import co.aikar.timings.Timing
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import net.starlegacy.SLComponent
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.feature.multiblock.baseshield.BaseShieldMultiblock
import net.starlegacy.util.NMSBlockData
import net.starlegacy.util.Tasks
import net.starlegacy.util.Vec3i
import net.starlegacy.util.blockKey
import net.starlegacy.util.blockKeyX
import net.starlegacy.util.blockKeyY
import net.starlegacy.util.blockKeyZ
import net.starlegacy.util.getNMSBlockData
import net.starlegacy.util.getNMSBlockDataSafe
import net.starlegacy.util.getSphereBlocks
import net.starlegacy.util.isStainedGlass
import net.starlegacy.util.isWallSign
import net.starlegacy.util.setAir
import net.starlegacy.util.setNMSBlockData
import net.starlegacy.util.timing
import net.starlegacy.util.toNMSBlockData
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.LinkedList
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.collections.set
import kotlin.math.max
import kotlin.math.min

object BaseShields : SLComponent() {
    private lateinit var impactTiming: Timing
    private lateinit var regenerationTiming: Timing
    private lateinit var cleanupTiming: Timing

    const val REGEN_TICK_INTERVAL = 8

    private data class ShieldBlockKey(val world: UUID, val blockKey: Long)

    private val pendingShieldBlocks = Object2LongOpenHashMap<ShieldBlockKey>()

    private val lock = Any()
    private val thread = Executors.newSingleThreadExecutor(Tasks.namedThreadFactory("base-shields"))
    private fun runAsync(block: () -> Unit) = thread.submit { synchronized(lock, block) }

    override fun onEnable() {
        impactTiming = timing("Base Shield Impact")
        regenerationTiming = timing("Base Shield Regeneration")
        cleanupTiming = timing("Base Shield Cleanup")

        // schedule this for later so that it can load after worlds
        Tasks.sync {
            runAsync {
                loadData()
            }
        }

        Tasks.syncRepeat(20L, 20L * 60L) {
            runAsync(this::saveData)
        }

        Tasks.syncRepeat(2L, 2L) {
            runAsync(this::decayShields)
        }

        registerListeners()
    }

    override fun onDisable() {
        thread.shutdown()
        thread.awaitTermination(15, TimeUnit.SECONDS)
        saveData()
    }

    private fun loadData() {
        val file = File(plugin.dataFolder, "baseshields.dat")
        DataInputStream(GZIPInputStream(FileInputStream(file))).use { dis ->
            repeat(dis.readInt()) {
                val world = UUID(dis.readLong(), dis.readLong())
                val blockKey = dis.readLong()
                val time = dis.readLong()
                pendingShieldBlocks[ShieldBlockKey(world, blockKey)] = time
            }
        }
    }

    @Synchronized
    private fun saveData() {
        val file = File(plugin.dataFolder, "baseshields.dat")
        DataOutputStream(GZIPOutputStream(FileOutputStream(file))).use { dos ->
            val clone = pendingShieldBlocks.toMap()
            dos.writeInt(clone.size)
            for ((shieldBlockKey, time) in clone) {
                val world = shieldBlockKey.world
                dos.writeLong(world.mostSignificantBits)
                dos.writeLong(world.leastSignificantBits)
                dos.writeLong(shieldBlockKey.blockKey)
                dos.writeLong(time)
            }
        }
    }

    @Synchronized
    private fun decayShields() {
        val queuedRemoves = mutableListOf<ShieldBlockKey>()
        val time = System.currentTimeMillis()

        val random = ThreadLocalRandom.current()

        for ((shieldBlockKey, placed) in pendingShieldBlocks) {
            if (time - placed > 2_000 + random.nextFloat() * 3_000) {
                queuedRemoves.add(shieldBlockKey)
            }
        }

        if (queuedRemoves.isEmpty()) {
            return
        }

        Tasks.sync {
            val start = System.nanoTime()

            val removedKeys = LinkedList<ShieldBlockKey>()

            for (shieldBlockKey in queuedRemoves) {
                if (System.nanoTime() - start > 10_000_000) {
                    break
                }

                val world = Bukkit.getWorld(shieldBlockKey.world) ?: continue

                val x = blockKeyX(shieldBlockKey.blockKey)
                val y = blockKeyY(shieldBlockKey.blockKey)
                val z = blockKeyZ(shieldBlockKey.blockKey)

                if (!world.isChunkLoaded(x shr 4, z shr 4)) {
                    continue
                }

                val block = world.getBlockAt(x, y, z)

                if (block.type.isStainedGlass) {
                    block.setType(Material.AIR, false)
                }

                removedKeys.add(shieldBlockKey)
            }

            runAsync {
                for (key in removedKeys) {
                    pendingShieldBlocks.removeLong(key)
                }
            }
        }
    }

    private val enabledText = "${ChatColor.DARK_GREEN}Enabled"
    private val disabledText = "${ChatColor.RED}Disabled"

    fun isBaseShieldDisabled(sign: Sign): Boolean {
        return sign.getLine(3) != enabledText
    }

    fun setBaseShieldEnabled(sign: Sign, enabled: Boolean) {
        sign.setLine(3, if (enabled) enabledText else disabledText)
        sign.update()
    }

    //region Damage Listeners
    private val lastExploded = Long2LongOpenHashMap()

    private fun onShieldExplode(blockList: MutableList<Block>) = blockList.removeIf { block ->
        if (!isShieldBlock(block)) {
            return@removeIf false
        }
        onShieldBlockExplode(block)
        return@removeIf true
    }

    private fun onShieldBlockExplode(block: Block) {
        val world = block.world
        val key = block.blockKey

        if (world.time - lastExploded.get(key) < REGEN_TICK_INTERVAL) {
            return
        }
        lastExploded[key] = world.time

        val damageLevel = getDamageLevel(block.getNMSBlockData()) - 1

        if (damageLevel == 0) {
            world.setAir(block.x, block.y, block.z, applyPhysics = false)
        } else {
            world.setNMSBlockData(block.x, block.y, block.z, getDataFromDamage(damageLevel))
        }
    }
    //endregion

    private data class ShieldKey(val world: UUID, val signLoc: Long, val radius: Int)

    private val shieldBlockCache = mutableMapOf<ShieldKey, List<ShieldBlockKey>>()

    private fun getShieldBlocks(sign: Sign, radius: Int): List<ShieldBlockKey> {
        val world = sign.world.uid
        return shieldBlockCache.getOrPut(ShieldKey(world, sign.location.toBlockKey(), radius)) {
            getSphereBlocks(radius, lowerBoundOffset = 0.5).map {
                val x = it.x + sign.x
                val y = (it.y + sign.y).coerceIn(0..255)
                val z = it.z + sign.z
                return@map ShieldBlockKey(world, blockKey(x, y, z))
            }
        }
    }

    fun regenerateBaseShield(sign: Sign, radius: Int) = runAsync {
        val world = sign.world

        var amount = 0
        val time = System.currentTimeMillis()

        val newBlocks = LongOpenHashSet()
        val toUpdate = LinkedList<Vec3i>()

        for (shieldBlockKey in getShieldBlocks(sign, radius)) {
            val blockKey = shieldBlockKey.blockKey
            val x = blockKeyX(blockKey)
            val y = blockKeyY(blockKey)
            val z = blockKeyZ(blockKey)

            val pendingRemoval = pendingShieldBlocks.contains(shieldBlockKey)
            if (pendingRemoval) {
                pendingShieldBlocks[shieldBlockKey] = time
            }

            val oldData = getNMSBlockDataSafe(sign.world, x, y, z) ?: continue
            val material = oldData.bukkitMaterial

            // if it's not air, and it's not a stained glass block in the list of blocks, don't overwrite it
            if (!material.isAir && !(material.isStainedGlass && pendingRemoval)) {
                continue
            }

            val damageLevel = if (pendingRemoval) min(4, getDamageLevel(oldData) + 1) else 4
            val newData = getDataFromDamage(damageLevel)

            if (oldData == newData) {
                continue
            }

            if (pendingRemoval) {
                amount++
            } else {
                pendingShieldBlocks[shieldBlockKey] = time
            }

            toUpdate.add(Vec3i(x, y, z))
            if (!pendingRemoval) newBlocks.add(blockKey)
        }

        val power = amount

        Tasks.sync {
            if (isBaseShieldDisabled(sign)) {
                return@sync
            }

            for ((x, y, z) in toUpdate) {
                val data = getNMSBlockDataSafe(world, x, y, z) ?: continue
                val material = data.bukkitMaterial
                if (!material.isAir && !material.isStainedGlass) {
                    continue
                }

                val newDamageLevel = when {
                    newBlocks.contains(blockKey(x, y, z)) -> 4
                    else -> min(4, getDamageLevel(data) + 1)
                }

                val newData = getDataFromDamage(newDamageLevel)
                if (data == newData) {
                    continue
                }

                world.setNMSBlockData(x, y, z, newData, applyPhysics = false)
            }

            if (power >= 0) {
                PowerMachines.removePower(sign, max(1, power))
            }
        }
    }

    private val lightBlueGlassData = Material.LIGHT_BLUE_STAINED_GLASS.toNMSBlockData()
    private val whiteGlassData = Material.WHITE_STAINED_GLASS.toNMSBlockData()
    private val yellowGlassData = Material.YELLOW_STAINED_GLASS.toNMSBlockData()
    private val orangeGlassData = Material.ORANGE_STAINED_GLASS.toNMSBlockData()
    private val redGlassData = Material.RED_STAINED_GLASS.toNMSBlockData()
    private val air = Material.AIR.toNMSBlockData()

    private fun getDamageLevel(state: NMSBlockData): Int = when (state) {
        lightBlueGlassData, whiteGlassData -> 4
        yellowGlassData -> 3
        orangeGlassData -> 2
        redGlassData -> 1
        else -> 0
    }

    private fun getDataFromDamage(damageLevel: Int): NMSBlockData = when (damageLevel) {
        4 -> lightBlueGlassData
        3 -> yellowGlassData
        2 -> orangeGlassData
        1 -> redGlassData
        else -> air
    }

    private fun shieldBlockKey(location: Location) = ShieldBlockKey(location.world.uid, location.toBlockKey())

    fun isShieldBlock(block: Block): Boolean {
        return pendingShieldBlocks.containsKey(shieldBlockKey(block.location))
    }

    private fun registerListeners() {
        plugin.listen<BlockExplodeEvent>(ignoreCancelled = true) { event ->
            onShieldExplode(event.blockList())
        }
        plugin.listen<EntityExplodeEvent>(ignoreCancelled = true) { event ->
            onShieldExplode(event.blockList())
        }

        plugin.listen<BlockBreakEvent>(priority = EventPriority.HIGHEST, ignoreCancelled = true) { event ->
            if (isShieldBlock(event.block)) {
                event.isCancelled = true
                onShieldBlockExplode(event.block)
            }
        }

        plugin.listen<BlockPhysicsEvent>(priority = EventPriority.HIGHEST, ignoreCancelled = true) { event ->
            if (isShieldBlock(event.block)) {
                event.isCancelled = true
                onShieldBlockExplode(event.block)
            }
        }

        plugin.listen<BlockPistonExtendEvent>(ignoreCancelled = true) { event ->
            if (event.blocks.any { isShieldBlock(it) }) {
                event.isCancelled = true
            }
        }

        plugin.listen<BlockPistonRetractEvent>(ignoreCancelled = true) { event ->
            if (event.blocks.any { isShieldBlock(it) }) {
                event.isCancelled = true
            }
        }

        plugin.listen<PlayerInteractEvent> { event ->
            val clickedBlock = event.clickedBlock ?: return@listen
            if (event.isCancelled
                || event.hand != EquipmentSlot.HAND
                || event.action != Action.RIGHT_CLICK_BLOCK
                || !clickedBlock.type.isWallSign
            ) return@listen

            val sign = clickedBlock.getState(false) as Sign

            if (Multiblocks[sign] !is BaseShieldMultiblock) return@listen

            setBaseShieldEnabled(sign, isBaseShieldDisabled(sign))
        }
    }
}
