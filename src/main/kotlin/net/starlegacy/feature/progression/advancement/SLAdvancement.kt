package net.starlegacy.feature.progression.advancement

import net.starlegacy.PLUGIN
import net.starlegacy.util.nms
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player

/** Root advancements */
enum class SLAdvancementCategory(
    /** The title of the root advancements*/
    val title: String,
    /** The description of the root advancement */
    val description: String,
    /** The icon to use for this category */
    val icon: Material,
    /** The background image texture to use for this category */
    val background: String
) {
    DEFENSE_MACHINES(
        title = "Defense Machines",
        description = "Machines used for protecting bases",
        icon = Material.SHIELD,
        background = "block/blue_concrete"
    ),
    POWER_MACHINES(
        title = "Power Machines",
        description = "General machines that use or produce power.",
        icon = Material.REDSTONE_BLOCK,
        background = "block/iron_block"
    ),
    STARSHIP_COMPONENTS(
        title = "Starship Components",
        description = "Features used mainly for starships",
        icon = Material.CLOCK,
        background = "block/black_concrete"
    );

    val advancementKey = name.toLowerCase()

    val namespacedKey = PLUGIN.namespacedKey(advancementKey)
}

enum class SLAdvancement(
    /** The category to use as the root advancement */
    val category: SLAdvancementCategory,
    /** If not null, the parent advancement is required in order to unlock this advancement. */
    val parent: SLAdvancement? = null,
    /** The minimum level required to get this advancement. */
    val requiredLevel: Int = 0,
    /** The title to show in the display. */
    val title: String,
    /** The description to show in the icon display on hover. */
    val description: String,
    /** Material id used in the advancements screen as the icon of the advancement. */
    val icon: Material,
    /** -1 = auto unlock */
    val costMultiplier: Double
) {
    // region Defense Machines
    AREA_SHIELDS(
        category = SLAdvancementCategory.DEFENSE_MACHINES,
        title = "Area Shields",
        description = "Area shields are structures that use power to prevent explosions within their radius.",
        icon = Material.GLASS_PANE,
        costMultiplier = -1.0
    ),
    AREA_SHIELD_5(
        category = SLAdvancementCategory.DEFENSE_MACHINES,
        parent = AREA_SHIELDS,
        title = "Area Shields (5-block radius)",
        description = "Prevents explosions within 5 blocks of the sign.",
        icon = Material.IRON_BLOCK,
        costMultiplier = 3.0
    ),
    AREA_SHIELD_10(
        category = SLAdvancementCategory.DEFENSE_MACHINES,
        parent = AREA_SHIELD_5,
        title = "Area Shields (10-block radius)",
        description = "Prevents explosions within 10 blocks of the sign.",
        icon = Material.GOLD_BLOCK,
        costMultiplier = 5.0
    ),
    AREA_SHIELD_20(
        category = SLAdvancementCategory.DEFENSE_MACHINES,
        parent = AREA_SHIELD_10,
        title = "Area Shields (20-block radius)",
        description = "Prevents explosions within 20 blocks of the sign.",
        icon = Material.DIAMOND_BLOCK,
        costMultiplier = 7.0
    ),
    AREA_SHIELD_30(
        category = SLAdvancementCategory.DEFENSE_MACHINES,
        parent = AREA_SHIELD_20,
        title = "Area Shields (30-block radius)",
        description = "Prevents explosions within 30 blocks of the sign.",
        icon = Material.EMERALD_BLOCK,
        costMultiplier = 9.0
    ),

    BASE_SHIELDS(
        category = SLAdvancementCategory.DEFENSE_MACHINES,
        title = "Base Shields",
        description = "Base shields use power to create bubbles of energy that absorb explosions.",
        icon = Material.WHITE_STAINED_GLASS,
        costMultiplier = -1.0
    ),
    BASE_SHIELD_SMALL(
        category = SLAdvancementCategory.DEFENSE_MACHINES,
        parent = BASE_SHIELDS,
        title = "Base Shields (Small)",
        description = "Small base shield. 25 block radius.",
        icon = Material.GOLD_BLOCK,
        costMultiplier = 2.5
    ),
    BASE_SHIELD_MEDIUM(
        category = SLAdvancementCategory.DEFENSE_MACHINES,
        parent = BASE_SHIELD_SMALL,
        title = "Base Shields (Medium)",
        description = "Medium base shield. 50 block radius.",
        icon = Material.DIAMOND_BLOCK,
        costMultiplier = 5.0
    ),
    BASE_SHIELD_LARGE(
        category = SLAdvancementCategory.DEFENSE_MACHINES,
        parent = BASE_SHIELD_MEDIUM,
        title = "Base Shields (Large)",
        description = "Large base shield. 100 block radius.",
        icon = Material.EMERALD_BLOCK,
        costMultiplier = 10.0
    ),
    // endregion

    // region Power Machines
    CHARGERS(
        category = SLAdvancementCategory.POWER_MACHINES,
        title = "Power Chargers",
        description = "Chargers use power to power up powerable items.",
        icon = Material.ANVIL,
        costMultiplier = -1.0
    ),
    CHARGER_ONE(
        category = SLAdvancementCategory.POWER_MACHINES,
        parent = CHARGERS,
        title = "Power Chargers (Tier 1)",
        description = "Level one charger. Stores up to 100,000 power. Charges 1,000 power per second. Uses iron blocks.",
        icon = Material.IRON_BLOCK,
        costMultiplier = 1.0
    ),
    CHARGER_TWO(
        category = SLAdvancementCategory.POWER_MACHINES,
        parent = CHARGER_ONE,
        title = "Power Chargers (Tier 2)",
        description = "Level two charger. Stores up to 200,000 power. Charges 2,000 power per second. Uses iron blocks.",
        icon = Material.GOLD_BLOCK,
        costMultiplier = 2.0
    ),
    CHARGER_THREE(
        category = SLAdvancementCategory.POWER_MACHINES,
        parent = CHARGER_TWO,
        title = "Power Chargers (Tier 3)",
        description = "Level three charger. Stores up to 300,000 power. 3,000 power per second. Uses diamond blocks.",
        icon = Material.DIAMOND_BLOCK,
        costMultiplier = 4.0
    ),

    MOB_DEFENDER(
        category = SLAdvancementCategory.POWER_MACHINES,
        title = "Mob Defenders",
        description = "Mob defenders prevent monsters from spawning within 50 blocks of them.",
        icon = Material.ROTTEN_FLESH,
        costMultiplier = 30.0
    ),

    POWER_CELL(
        category = SLAdvancementCategory.POWER_MACHINES,
        title = "Power Cells",
        description = "A small power container which stores up to 50,000 power.",
        icon = Material.IRON_NUGGET,
        costMultiplier = 1.0
    ),
    POWER_BANKS(
        category = SLAdvancementCategory.POWER_MACHINES,
        parent = POWER_CELL,
        title = "Power Banks",
        description = "Power banks are machines that can store and export power. They have two extractors built in.",
        icon = Material.REDSTONE_BLOCK,
        costMultiplier = -1.0
    ),
    POWER_BANK_ONE(
        category = SLAdvancementCategory.POWER_MACHINES,
        parent = POWER_BANKS,
        title = "Power Banks (Tier 1)",
        description = "Level one power bank. Stores up to 300,000 power. Uses iron blocks.",
        icon = Material.IRON_BLOCK,
        costMultiplier = 2.0
    ),
    POWER_BANK_TWO(
        category = SLAdvancementCategory.POWER_MACHINES,
        parent = POWER_BANK_ONE,
        title = "Power Banks (Tier 2)",
        description = "Level two power bank. Stores up to 350,000 power. Uses gold blocks.",
        icon = Material.GOLD_BLOCK,
        costMultiplier = 4.0
    ),
    POWER_BANK_THREE(
        category = SLAdvancementCategory.POWER_MACHINES,
        parent = POWER_BANK_TWO,
        title = "Power Banks (Tier 3)",
        description = "Level three power bank. Stores up to 500,000 power. Uses diamond blocks.",
        icon = Material.DIAMOND_BLOCK,
        costMultiplier = 8.0
    ),

    POWER_FURNACES(
        category = SLAdvancementCategory.POWER_MACHINES,
        title = "Power Furnaces",
        description = "Power Furnaces are machines that use power to smelt items the way furnaces do with fuel.",
        icon = Material.FURNACE,
        costMultiplier = -1.0
    ),
    POWER_FURNACE_ONE(
        category = SLAdvancementCategory.POWER_MACHINES,
        parent = POWER_FURNACES,
        title = "Power Furnaces (Tier 1)",
        description = "Level one power furnace. Stores up to 25,000 power. Burns for 10s. Uses iron blocks.",
        icon = Material.IRON_BLOCK,
        costMultiplier = 2.0
    ),
    POWER_FURNACE_TWO(
        category = SLAdvancementCategory.POWER_MACHINES,
        parent = POWER_FURNACE_ONE,
        title = "Power Furnaces (Tier 2)",
        description = "Level two power furnace. Stores up to 50,000 power. Burns for 15s. Uses gold blocks.",
        icon = Material.GOLD_BLOCK,
        costMultiplier = 4.0
    ),
    POWER_FURNACE_THREE(
        category = SLAdvancementCategory.POWER_MACHINES,
        parent = POWER_FURNACE_TWO,
        title = "Power Furnaces (Tier 3)",
        description = "Level three power furnace. Stores up to ??,000 power. Burns for ??s. Uses diamond blocks.",
        icon = Material.DIAMOND_BLOCK,
        costMultiplier = 8.0
    ),

    POWER_GENERATORS(
        category = SLAdvancementCategory.POWER_MACHINES,
        title = "Power Generators",
        description = "Power generators generate power from fuel.",
        icon = Material.REDSTONE,
        costMultiplier = -1.0
    ),
    POWER_GENERATOR_ONE(
        category = SLAdvancementCategory.POWER_MACHINES,
        parent = POWER_GENERATORS,
        title = "Power Generators (Tier 1)",
        description = "Level one power generator. 1.0 speed. 100,000 max power. Uses iron blocks.",
        icon = Material.IRON_BLOCK,
        costMultiplier = 1.0
    ),
    POWER_GENERATOR_TWO(
        category = SLAdvancementCategory.POWER_MACHINES,
        parent = POWER_GENERATOR_ONE,
        title = "Power Generators (Tier 2)",
        description = "Level two power generator. 1.25 speed. 175,000 max power. Uses gold blocks.",
        icon = Material.GOLD_BLOCK,
        costMultiplier = 2.0
    ),
    POWER_GENERATOR_THREE(
        category = SLAdvancementCategory.POWER_MACHINES,
        parent = POWER_GENERATOR_TWO,
        title = "Power Generators (Tier 3)",
        description = "Level three power generator. 1.5 speed. Uses diamond blocks.",
        icon = Material.DIAMOND_BLOCK,
        costMultiplier = 4.0
    ),


    AUTO_CRAFTERS(
        category = SLAdvancementCategory.POWER_MACHINES,
        title = "Auto Crafters",
        description = "Auto Crafters allow you to automate the crafting of items.",
        icon = Material.REDSTONE,
        costMultiplier = -1.0
    ),
    AUTO_CRAFTER_ONE(
        category = SLAdvancementCategory.POWER_MACHINES,
        parent = AUTO_CRAFTERS,
        title = "Auto Crafters (Tier 1)",
        description = "Level one auto crafter. 2 iterations/second. 200,000 max power. Uses iron blocks.",
        icon = Material.IRON_BLOCK,
        costMultiplier = 5.0
    ),
    AUTO_CRAFTER_TWO(
        category = SLAdvancementCategory.POWER_MACHINES,
        parent = AUTO_CRAFTER_ONE,
        title = "Auto Crafters (Tier 2)",
        description = "Level two auto crafter. 4 iterations/second. 400,000 max power. Uses gold blocks.",
        icon = Material.GOLD_BLOCK,
        costMultiplier = 10.0
    ),
    AUTO_CRAFTER_THREE(
        category = SLAdvancementCategory.POWER_MACHINES,
        parent = AUTO_CRAFTER_TWO,
        title = "Auto Crafters (Tier 3)",
        description = "Level three auto crafter. 6 iterations/second. 600,000 max power. Uses diamond blocks.",
        icon = Material.DIAMOND_BLOCK,
        costMultiplier = 20.0
    ),

    PRINTERS(
        category = SLAdvancementCategory.POWER_MACHINES,
        title = "Printers",
        description = "Printers are machines that create blocks from cobblestone.",
        icon = Material.FURNACE_MINECART,
        costMultiplier = -1.0
    ),
    PRINTER_ARMOR(
        category = SLAdvancementCategory.POWER_MACHINES,
        parent = PRINTERS,
        title = "Armor Printers",
        description = "Armor printers make terracotta (stained clay) from cobblestone.",
        icon = Material.WHITE_TERRACOTTA,
        costMultiplier = 10.0
    ),
    PRINTER_GLASS(
        category = SLAdvancementCategory.POWER_MACHINES,
        parent = PRINTERS,
        title = "Armor Printers",
        description = "Glass printers make plain glass from cobblestone.",
        icon = Material.GLASS,
        costMultiplier = 10.0
    ),
    PRINTER_TECHNICAL(
        category = SLAdvancementCategory.POWER_MACHINES,
        parent = PRINTERS,
        title = "Armor Printers",
        description = "Technical printers make technical blocks (sponge) from cobblestone.",
        icon = Material.SPONGE,
        costMultiplier = 10.0
    ),
    PRINTER_CARBON(
        category = SLAdvancementCategory.POWER_MACHINES,
        parent = PRINTERS,
        title = "Carbon Printers",
        description = "Carbon printers make graxene powder (concrete powder) from cobblestone.",
        icon = Material.WHITE_CONCRETE_POWDER,
        costMultiplier = 10.0
    ),
    CARBON_PROCESSOR(
        category = SLAdvancementCategory.POWER_MACHINES,
        parent = PRINTER_CARBON,
        title = "Carbon Processors",
        description = "Carbon processors turn graxene powder (concrete powder) into carbyne (concrete)",
        icon = Material.WHITE_CONCRETE,
        costMultiplier = 1.0
    ),
    //endregion

    //region Starship Components
    ENTRANCES(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        title = "Entrances",
        description = "Get in and out of your ship the cool way!",
        icon = Material.IRON_DOOR,
        costMultiplier = -1.0
    ),
    AIRLOCK(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        parent = ENTRANCES,
        title = "Airlocks",
        description = "A futuristic door that's reinforced glass when off and an energy field when on.",
        icon = Material.IRON_BARS,
        costMultiplier = 0.5
    ),
    DOCKING_TUBE(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        parent = ENTRANCES,
        title = "Docking Tubes",
        description = "Docking tubes let you dock your ship to another tube, making a bridge between them while it's on.",
        icon = Material.STONE_BUTTON,
        costMultiplier = 0.5
    ),

    DRILLS(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        title = "Starship Drills",
        description = "Starship drills are machines you put on your ship and turn on, then move your ship. " +
                "Any blocks they touch are broken and the materials are collected.",
        icon = Material.IRON_PICKAXE,
        costMultiplier = -1.0
    ),
    DRILL_ONE(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        parent = DRILLS,
        title = "Starship Drills (Tier 1)",
        description = "Level one starship drill. Uses iron blocks.",
        icon = Material.IRON_BLOCK,
        costMultiplier = 5.0
    ),
    DRILL_TWO(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        parent = DRILL_ONE,
        title = "Starship Drills (Tier 2)",
        description = "Level two starship drill. Uses diamond blocks.",
        icon = Material.DIAMOND_BLOCK,
        costMultiplier = 10.0
    ),
    DRILL_THREE(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        parent = DRILL_TWO,
        title = "Starship Drills (Tier 3)",
        description = "Level three starship drill. Uses emerald blocks.",
        icon = Material.EMERALD_BLOCK,
        costMultiplier = 15.0
    ),

    GRAVITY_WELL(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        title = "Gravity Wells",
        description = "Gravity wells can either be turned on to inhibit hyperspace travel " +
                "or pulsed to slow down enemy ships' cruise speeds.",
        icon = Material.BREWING_STAND,
        costMultiplier = 10.0
    ),

    GRAVITY_WELL_AMPLIFIED(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        parent = GRAVITY_WELL,
        title = "Gravity Wells",
        description = "High radius gravity well for interdictors only.",
        icon = Material.BREWING_STAND,
        costMultiplier = 20.0
    ),

    HYPERDRIVES(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        title = "Hyperdrives",
        description = "Hyperdrives are machines you can put inside of starships to power nav computers and jump beacons " +
                "so you can move hundreds of blocks a second to fly between solar systems",
        icon = Material.HOPPER,
        costMultiplier = -1.0
    ),
    HYPERDRIVE_ONE(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        parent = HYPERDRIVES,
        title = "Hyperdrives (Tier 1)",
        description = "Level one hyperdrive.",
        icon = Material.IRON_BLOCK,
        costMultiplier = 4.0
    ),
    HYPERDRIVE_TWO(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        parent = HYPERDRIVE_ONE,
        title = "Hyperdrives (Tier 2)",
        description = "Level two hyperdrive.",
        icon = Material.GOLD_BLOCK,
        costMultiplier = 8.0
    ),
    HYPERDRIVE_THREE(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        parent = HYPERDRIVE_TWO,
        title = "Hyperdrives (Tier 3)",
        description = "Level three hyperdrive.",
        icon = Material.DIAMOND_BLOCK,
        costMultiplier = 16.0
    ),
    HYPERDRIVE_FOUR(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        parent = HYPERDRIVE_THREE,
        title = "Hyperdrives (Tier 4)",
        description = "Level four hyperdrive.",
        icon = Material.EMERALD_BLOCK,
        costMultiplier = 32.0
    ),

    NAV_COMPUTERS(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        title = "Navigation Computers",
        description = "Navigation computers are machines you can put in your ship to let you jump to hyperspace without a beacon.",
        icon = Material.COMPASS,
        costMultiplier = -1.0
    ),
    NAV_COMPUTER_BASIC(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        parent = NAV_COMPUTERS,
        title = "Navigation Computers (Basic)",
        description = "Basic navigation computer with limited functionality. Can jump up to 10,000 blocks.",
        icon = Material.IRON_NUGGET,
        costMultiplier = 2.5
    ),
    NAV_COMPUTER_ADVANCED(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        parent = NAV_COMPUTER_BASIC,
        title = "Navigation Computers (Advanced)",
        description = "Navigation computers with advanced functionality. Can jump up to 25,000 blocks.",
        icon = Material.IRON_INGOT,
        costMultiplier = 5.0
    ),

    MAGAZINES(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        title = "Magazines",
        description = "Magazines store and provide ammo for starship weapons",
        icon = Material.END_PORTAL_FRAME,
        costMultiplier = 5.0
    ),

    PARTICLE_SHIELDS(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        title = "Particle Shields",
        description = "Particle shields are used to protect starships from explosions in flight.",
        icon = Material.BARRIER,
        costMultiplier = -1.0
    ),
    PARTICLE_SHIELD_B(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        parent = PARTICLE_SHIELDS,
        title = "Particle Shields (Class B)",
        description = "B class particle shield. Custom-sized box shape.",
        icon = Material.NETHERITE_BLOCK,
        costMultiplier = 1.0
    ),
    PARTICLE_SHIELD_08(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        parent = PARTICLE_SHIELDS,
        title = "Particle Shields (Class 0.8)",
        description = "0.8 class particle shield. 8 block radius.",
        icon = Material.IRON_BLOCK,
        costMultiplier = 0.5
    ),
    PARTICLE_SHIELD_20(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        parent = PARTICLE_SHIELD_08,
        title = "Particle Shields (Class 2.0)",
        description = "2.0 class particle shield. 12 block radius.",
        icon = Material.GOLD_BLOCK,
        costMultiplier = 0.5
    ),
    PARTICLE_SHIELD_30(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        parent = PARTICLE_SHIELD_20,
        title = "Particle Shields (Class 3.0)",
        description = "3.0 class particle shield. 20 block radius.",
        icon = Material.GOLD_BLOCK,
        costMultiplier = 0.5
    ),
    PARTICLE_SHIELD_65(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        parent = PARTICLE_SHIELD_30,
        title = "Particle Shields (Class 6.5)",
        description = "6.5 class particle shield. 25 block radius.",
        icon = Material.DIAMOND_BLOCK,
        costMultiplier = 0.5
    ),
    PARTICLE_SHIELD_85(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        parent = PARTICLE_SHIELD_65,
        title = "Particle Shields (Class 8.5)",
        description = "8.5 class particle shield. 35 block radius.",
        icon = Material.EMERALD_BLOCK,
        costMultiplier = 0.5
    ),
    PARTICLE_SHIELD_08I(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        parent = PARTICLE_SHIELDS,
        title = "Particle Shields (Class 0.8i)",
        description = "0.8i class particle shield. 8 block radius.",
        icon = Material.PURPUR_BLOCK,
        costMultiplier = 1.0
    ),

    SHIP_FACTORY(
        category = SLAdvancementCategory.STARSHIP_COMPONENTS,
        title = "Ship Factories",
        description = "Ship factories place blocks to reconstruct a ship blueprint automatically, using resources.",
        icon = Material.FLINT_AND_STEEL,
        costMultiplier = 1.5
    );
    //endregion

    val advancementKey = name.toLowerCase()

    val namespacedKey = PLUGIN.namespacedKey(advancementKey)

    val nmsAdvancement
        get() = Bukkit.getServer().getAdvancement(namespacedKey)?.nms
            ?: error("Advanced $namespacedKey is missing the bukkit advancement!")

    fun getMoneyCost(): Int {
        return (costMultiplier * advancementBalancing.baseCost).toInt()
    }

    /**
     * Recursively checks if the player has the advancement's parents unlocked.
     * If the advancement has no parent, returns true.
     *
     * @return True if the player has the advancement's parents unlocked, else false.
     */
    fun hasParents(sender: Player): Boolean {
        val parent: SLAdvancement = parent ?: return true

        return (parent.costMultiplier < 0 || Advancements.has(
            sender,
            parent
        )) && parent.hasParents(sender)
    }

    companion object {
        val freeAdvancements = values().filter { it.costMultiplier == -1.0 }.toSet()
    }
}
