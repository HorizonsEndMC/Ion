package net.starlegacy.updater

import com.google.gson.Gson
import com.mongodb.MongoWriteException
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader
import net.starlegacy.PLUGIN
import net.starlegacy.SLComponent
import net.starlegacy.database.schema.economy.BazaarItem
import net.starlegacy.database.schema.economy.CollectedItem
import net.starlegacy.database.schema.space.Planet
import net.starlegacy.database.schema.space.Star
import net.starlegacy.database.schema.starships.Blueprint
import net.starlegacy.database.schema.starships.PlayerStarshipData
import net.starlegacy.database.slPlayerId
import net.starlegacy.feature.economy.bazaar.Merchants
import net.starlegacy.feature.misc.CryoPods
import net.starlegacy.feature.misc.CustomItem
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.feature.starship.DeactivatedPlayerStarships
import net.starlegacy.feature.starship.StarshipType
import net.starlegacy.util.Tasks
import net.starlegacy.util.Vec3i
import net.starlegacy.util.stripColor
import net.starlegacy.util.updateMeta
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockState
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.material.MaterialData
import org.litote.kmongo.combine
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.setValue
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.util.Base64
import java.util.Properties
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.component1
import kotlin.collections.component2

object Updater : SLComponent() {
    val customItemMappings: Map<String, CustomItem> = mapOf(
        "Thermal Detonator" to "detonator",
        "Empty Gas Canister" to "empty_canister",
        "Helium Gas Canister" to "gas_helium",
        "Oxygen Gas Canister" to "gas_oxygen",
        "Hydrogen Gas Canister" to "gas_hydrogen",
        "Nitrogen Gas Canister" to "gas_nitrogen",
        "Carbon Dioxide Gas Canister" to "gas_carbon_dioxide",
        "Size-A Battery" to "battery_a",
        "Size-M Battery" to "battery_m",
        "Size-G Battery" to "battery_g",
        "Blue Energy Sword" to "energy_sword_blue",
        "Red Energy Sword" to "energy_sword_red",
        "Yellow Energy Sword" to "energy_sword_yellow",
        "Green Energy Sword" to "energy_sword_green",
        "Purple Energy Sword" to "energy_sword_purple",
        "Orange Energy Sword" to "energy_sword_orange",
        "Blaster Pistol" to "blaster_pistol",
        "Blaster Rifle" to "blaster_rifle",
        "Blaster Sniper" to "blaster_sniper",
        "Blaster Cannon" to "blaster_cannon",
        "Power Helmet" to "power_armor_helmet",
        "Power Chestplate" to "power_armor_chestplate",
        "Power Leggings" to "power_armor_leggings",
        "Power Boots" to "power_armor_boots",
        "Shock Absorbing Module" to "power_module_shock_absorbing",
        "Speed Boosting Module" to "power_module_speed_boosting",
        "Rocket Boosting Module" to "power_module_rocket_boosting",
        "Night Vision Module" to "power_module_night_vision",
        "Environment Module" to "power_module_environment",
        "Power Pickaxe" to "power_tool_drill",
        "Power Chainsaw" to "power_tool_chainsaw",
        "Copper Ore" to "copper_ore",
        "Copper Block" to "copper_block",
        "Copper" to "copper",
        "Aluminum Ore" to "aluminum_ore",
        "Aluminum Block" to "aluminum_block",
        "Aluminum" to "aluminum",
        "Chetherite Ore" to "chetherite_ore",
        "Chetherite Block" to "chetherite_block",
        "Chetherite" to "chetherite",
        "Titanium Ore" to "titanium_ore",
        "Titanium Block" to "titanium_block",
        "Titanium" to "titanium",
        "Uranium Ore" to "uranium_ore",
        "Uranium Block" to "uranium_block",
        "Uranium" to "uranium",
        "Aecor" to "planet_icon_aecor",
        "Arbusto" to "planet_icon_arbusto",
        "Cerus Alpha" to "planet_icon_cerusalpha",
        "Cerus Beta" to "planet_icon_cerusbeta",
        "Collis" to "planet_icon_collis",
        "Harenum" to "planet_icon_harenum",
        "Koryza" to "planet_icon_koryza",
        "Orcus" to "planet_icon_orcus",
        "Porrus" to "planet_icon_porrus",
        "Quod Canis" to "planet_icon_quodcanis",
        "Sakaro" to "planet_icon_sakaro",
        "Syre" to "planet_icon_syre",
        "Terram" to "planet_icon_terram",
        "Titus" to "planet_icon_titus",
        "Trunkadis" to "planet_icon_trunkadis"
    ).mapValues { (_, id) -> CustomItems[id] ?: error("No item $id") }

//    private lateinit var updatedChunks: Object2ObjectOpenHashMap<String, LongOpenHashBigSet>

//    private val file = File(plugin.dataFolder, "custom_item_update_data.dat.gz")

    override fun onEnable() {
        customItemConversion()
        // save data every 5 minutes, so that a crash doesn't affect it too severely
        Tasks.asyncRepeat(20L * 60L * 5L, 20L * 60L * 5L, Updater::save)
    }

    override fun onDisable() {
        save()
    }

    private fun customItemConversion() {
/*        if (!file.exists()) {
            updatedChunks = Object2ObjectOpenHashMap()
        } else {
            DataInputStream(GZIPInputStream(FileInputStream(file))).use { input ->
                val worldCount = input.readInt()
                updatedChunks = Object2ObjectOpenHashMap(worldCount)
                repeat(worldCount) {
                    val worldNameLength = input.readInt()
                    val chars = CharArray(worldNameLength)
                    for (i in 0 until worldNameLength) {
                        chars[i] = input.readChar()
                    }
                    val worldName = String(chars)
                    val chunkCount = input.readLong()
                    val set = LongOpenHashBigSet(chunkCount)
                    for (i in 0L until chunkCount) {
                        set.add(input.readLong())
                    }
                    updatedChunks[worldName] = set
                }
            }
        }*/

        fun fixItem(item: ItemStack?): Boolean {
            val itemMeta: ItemMeta = item?.itemMeta ?: return false
            if (!itemMeta.isUnbreakable) {
                return false
            }
            val customItem = customItemMappings[itemMeta.displayName.stripColor()] ?: return false
            val lore = item.lore
            item.type = customItem.material
            item.updateMeta {
                it.setCustomModelData(customItem.model)
                it.isUnbreakable = customItem.unbreakable
                if (customItem == CustomItems.POWER_TOOL_DRILL) {
                    it.setDisplayName(customItem.displayName)
                }
                if (it is Damageable) {
                    it.damage = 0
                }
                it.lore = lore
            }
            return true
        }

        fun fixInventory(inventory: Inventory): Boolean {
            var any = false
            for (item in inventory.contents) {
                any = fixItem(item) || any
            }
            return any
        }

        subscribe<PlayerJoinEvent> { event ->
            // if they haven't been updated before, check their inventory for items to update
            fixInventory(event.player.inventory)
        }

/*        subscribe<ChunkLoadEvent> { event ->
            val chunk = event.chunk
            val chunkSet = updatedChunks.getOrPut(chunk.world.name) { LongOpenHashBigSet() }
            if (!chunkSet.add(chunk.chunkKey)) {
                return@subscribe
            }
            // if the chunk hasn't been updated before, check every inventory in the chunk
            for (tileEntity in chunk.getTileEntities(false)) { // false because we don't need a snapshot/copy of each TE
                if (tileEntity is InventoryHolder) {
                    if (fixInventory(tileEntity.inventory)) {
                        tileEntity.update(false, false)
                    }
                }
            }
            for (entity in chunk.entities) {
                if (entity is Item) {
                    fixItem(entity.itemStack)
                }
            }
        }*/

        subscribe<InventoryOpenEvent> { event ->
            val inventory = event.inventory

            if (fixInventory(inventory)) {
                (inventory.holder as? BlockState)?.update(false, false)
            }
        }
    }

    @Synchronized
    private fun save() {
/*        val tmpFile = File(file.parent, file.name + ".tmp")
        DataOutputStream(GZIPOutputStream(FileOutputStream(tmpFile))).use { output ->
            output.writeInt(updatedChunks.size)
            for ((world, chunks) in updatedChunks) {
                output.writeInt(world.length)
                output.writeChars(world)
                output.writeLong(chunks.size64())
                chunks.forEach(output::writeLong)
            }
        }
        tmpFile.renameTo(file)*/
    }

    fun updateDatabase() {
        val start = System.nanoTime()

        try {
            updateStars()
            updatePlanets()
            updateBazaarItems()
            updateCollectedItems()
            updateBlueprints()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        println("Time spent updating database: ${TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)}ms")
    }

    private fun updateStars() {
        for (star in Star.all()) {
            if (Material.getMaterial(star.material, false) == null) {
                Star.updateById(star._id, setValue(Star::material, Material.getMaterial(star.material, true)!!.name))
            }
        }
    }

    private fun updatePlanets() {
        for (planet in Planet.all()) {
            for (mat in planet.crustMaterials) {
                if (!isOutdated(mat)) {
                    continue
                }

                // if there's one outdated they're all outdated
                fun convert(list: List<String>): List<String> = list.map(Updater::updatedItemString)

                val crust = convert(planet.crustMaterials)
                val cloud = convert(planet.cloudMaterials)

                val id = planet._id
                Planet.updateById(
                    id,
                    combine(setValue(Planet::crustMaterials, crust), setValue(Planet::cloudMaterials, cloud))
                )
                break // finishes up with this planet
            }
        }
    }

    private fun updateBlueprints() {
        val folder = File(PLUGIN.dataFolder, "exported_blueprints")
        if (!folder.exists()) {
            return
        }

        val imported = AtomicInteger()

        val pool = Executors.newWorkStealingPool()

        for (childFolder in folder.listFiles()) {
            pool.submit {
                val names = mutableSetOf<String>()
                var count = 0
                for (file in childFolder.listFiles()) {
                    try {
                        importBlueprint(file, childFolder, names)
                        count++
                    } catch (e: Exception) {
                        log.error("Failed to import blueprint file ${file.absolutePath}", e)
                    }
                }
                log.info("${imported.addAndGet(count)} blueprints imported")
            }
        }

        pool.shutdown()
        pool.awaitTermination(1L, TimeUnit.HOURS)

        folder.renameTo(File(folder.absolutePath + "_bak"))
    }

    private fun importBlueprint(
        file: File?,
        childFolder: File,
        names: MutableSet<String>
    ) {
        DataInputStream(FileInputStream(file)).use { input ->
            var name = input.readUTF().toLowerCase()

            val size = input.readInt()

            var owner = UUID(input.readLong(), input.readLong())
            try {
                owner = UUID.fromString(childFolder.name.removeSuffix("-blueprints"))
            } catch (e: Exception) {
                println("${childFolder.name} could not be converted to a UUID")
            }
            val ownerId = owner.slPlayerId

            val type = input.readUTF()

            val x = input.readInt()
            val y = input.readInt()
            val z = input.readInt()

            val data = getBlueprintData(input)

            while (names.contains(name)) {
                name += "_dupe"
            }

            names.add(name)

            val starshipType = getStarshipType(type) ?: StarshipType.SHUTTLE

            val existing = Blueprint.get(ownerId, name)
            if (existing != null) {
                Blueprint.delete(existing._id)
            }
            Blueprint.create(ownerId, name, starshipType, Vec3i(x, y, z), size, data)
        }
    }

    private fun getBlueprintData(input: DataInputStream): String {
        val schematicBytes = ByteArray(input.readInt())
        input.read(schematicBytes)

        val clipboard = BuiltInClipboardFormat.MCEDIT_SCHEMATIC
            .getReader(ByteArrayInputStream(schematicBytes))
            .use(ClipboardReader::read)

        val newBytes = ByteArrayOutputStream().also {
            BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(it)
                .use { writer -> writer.write(clipboard) }
        }.toByteArray()
        return Base64.getEncoder().encodeToString(newBytes)
    }

    private fun updateBazaarItems() {
        for (item in BazaarItem.all()) {
            try {
                val itemString = item.itemString
                if (!isOutdated(itemString)) {
                    continue
                }
                BazaarItem.updateById(
                    item._id, setValue(
                        BazaarItem::itemString,
                        updatedItemString(itemString)
                    )
                )
            } catch (e: MongoWriteException) {
                e.printStackTrace()
                BazaarItem.col.deleteOneById(item._id)
            }
        }
    }

    private fun updateCollectedItems() {
        for (item in CollectedItem.all()) {
            try {
                val itemString = item.itemString
                if (!isOutdated(itemString)) {
                    continue
                }
                val updated = updatedItemString(itemString)
                CollectedItem.updateById(item._id, setValue(CollectedItem::itemString, updated))
            } catch (e: MongoWriteException) {
                e.printStackTrace()
                BazaarItem.col.deleteOneById(item._id)
            }
        }
    }

    fun isOutdated(materialString: String): Boolean = materialString.contains(":") || materialString == "power_pickaxe"

    @Suppress("deprecation")
    fun updatedItemString(materialString: String): String {
        if (materialString == "power_pickaxe") {
            return "power_drill"
        }
        val split = materialString.split(":")
        val material = Material.getMaterial(Material.LEGACY_PREFIX + split[0])
        val data = split[1].toInt().toByte()
        return Bukkit.getUnsafe().fromLegacy(MaterialData(material, data))?.name
            ?: error("Failed to convert $materialString")
    }


    fun updateCryoPods() {
        val oldFolder = File(plugin.dataFolder.parentFile, "Starships/cryopods")
        if (!oldFolder.exists()) {
            return
        }
        importCryoPods(oldFolder)
        oldFolder.renameTo(File(oldFolder.parentFile, oldFolder.name + "_bak"))
    }

    private fun importCryoPods(oldFolder: File) {
        for (child in oldFolder.listFiles()) {
            try {
                if (child.extension != "json") {
                    continue
                }
                val legacy = FileReader(child).use {
                    Gson().fromJson(it, LegacyCryoData::class.java)
                }
                val player = UUID.fromString(child.nameWithoutExtension)
                val world = legacy.world.toLowerCase()
                val pos = Vec3i(legacy.x, legacy.y, legacy.z)
                CryoPods.setCryoPod(player, world, pos)
            } catch (e: Exception) {
                log.error("Failed to import ${child.name}", e)
            }
        }
    }

    fun updateStarshipComputers() {
        val oldFile = File(plugin.dataFolder.parentFile, "Starships/computers.yml")
        if (!oldFile.exists()) {
            return
        }
        val config = YamlConfiguration.loadConfiguration(oldFile)
        importComputers(config)
        oldFile.renameTo(File(oldFile.parentFile, oldFile.name + "_bak"))
    }

    private fun importComputers(config: YamlConfiguration) {
        loop@ for (uuid in config.getKeys(false)) {
            try {
                val originalType = config.getString("$uuid.type") ?: continue
                val type = getStarshipType(originalType) ?: continue
                val worldName = config.getString("$uuid.world") ?: continue
                val world = Bukkit.getWorld(worldName) ?: continue
                val x = config.getDouble("$uuid.x").toInt()
                val y = config.getDouble("$uuid.x").toInt()
                val z = config.getDouble("$uuid.x").toInt()
                val captain = UUID.fromString(config.getString("$uuid.captain") ?: continue)
                val pilots = config.getStringList("$uuid.pilots").map { UUID.fromString(it) }
                DeactivatedPlayerStarships.createAsync(world, x, y, z, captain) { data ->
                    val lastUsed = config.getLong("$uuid.lastPiloted", System.currentTimeMillis())
                    data.lastUsed = lastUsed
                    PlayerStarshipData.updateById(data._id, setValue(PlayerStarshipData::lastUsed, lastUsed))
                    DeactivatedPlayerStarships.updateType(data, type)
                    for (pilot in pilots) {
                        DeactivatedPlayerStarships.addPilot(data, pilot.slPlayerId)
                    }
                }
            } catch (exception: java.lang.Exception) {
                continue
            }
        }
    }

    private fun getStarshipType(originalType: String): StarshipType? = when (originalType.toLowerCase()) {
        "star fighter", "star_fighter" -> StarshipType.STARFIGHTER
        "corvette" -> StarshipType.GUNSHIP
        "frigate" -> StarshipType.CORVETTE
        "destroyer" -> StarshipType.FRIGATE
        "cruiser" -> StarshipType.DESTROYER
        "battle cruiser", "battle_cruiser" -> StarshipType.BATTLECRUISER
        "star destroyer", "star_destroyer", "stardestroyer" -> StarshipType.BATTLESHIP
        else -> StarshipType.getType(originalType)
    }

    fun updateMerchants() {
        val oldFile = File(plugin.sharedDataFolder, "merchant_prices.properties")

        if (!oldFile.exists()) {
            return
        }

        val properties = Properties()

        FileReader(oldFile).use { reader ->
            properties.load(reader)
        }

        for (key in properties.stringPropertyNames()) {
            val value = properties.getProperty(key).toDouble()
            Merchants.setMerchantDefaultPrice(updatedItemString(key), value)
        }

        oldFile.renameTo(File(oldFile.parent, oldFile.name + ".old"))
    }

    override fun supportsVanilla(): Boolean {
        return true
    }
}
