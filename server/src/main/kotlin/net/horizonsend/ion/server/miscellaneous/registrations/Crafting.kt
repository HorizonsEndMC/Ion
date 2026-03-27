package net.horizonsend.ion.server.miscellaneous.registrations

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemEnchantments
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ADVANCED_ITEM_EXTRACTOR
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ALUMINUM_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ALUMINUM_INGOT
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ALUMINUM_ORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ARMOR_MODIFICATION_ENVIRONMENT
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ARMOR_MODIFICATION_NIGHT_VISION
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ARMOR_MODIFICATION_PRESSURE_FIELD
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ARMOR_MODIFICATION_ROCKET_BOOSTING
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ARMOR_MODIFICATION_SHOCK_ABSORBING
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ARMOR_MODIFICATION_SPEED_BOOSTING
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.BARGE_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.BATTERY_A
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.BATTERY_G
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.BATTERY_M
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.BATTLECRUISER_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.BLASTER_CANNON
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.BLASTER_PISTOL
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.BLASTER_RIFLE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.BLASTER_SHOTGUN
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.BLASTER_SNIPER
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.CANNON_RECEIVER
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.CHETHERITE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.CHETHERITE_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.CIRCUITRY
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.CIRCUIT_BOARD
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.CRATE_PLACER
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.CRUISER_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.DETONATOR
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ENERGY_SWORD_BLUE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ENERGY_SWORD_GREEN
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ENERGY_SWORD_ORANGE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ENERGY_SWORD_PINK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ENERGY_SWORD_PURPLE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ENERGY_SWORD_RED
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ENERGY_SWORD_YELLOW
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ENRICHED_URANIUM
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ENRICHED_URANIUM_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.FABRICATED_ASSEMBLY
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.FUEL_CELL
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.FUEL_CONTROL
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.FUEL_ROD_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.GAS_CANISTER_EMPTY
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.GAS_CANISTER_HYDROGEN
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.GAS_CANISTER_OXYGEN
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.GUN_BARREL
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ITEM_FILTER
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.MOTHERBOARD
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.MULTIBLOCK_WORKBENCH
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.MULTIMETER
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.NETHERITE_CASING
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.PISTOL_RECEIVER
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.POWER_ARMOR_BOOTS
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.POWER_ARMOR_CHESTPLATE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.POWER_ARMOR_HELMET
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.POWER_ARMOR_LEGGINGS
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.POWER_CHAINSAW_ADVANCED
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.POWER_CHAINSAW_BASIC
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.POWER_CHAINSAW_ENHANCED
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.POWER_DRILL_ADVANCED
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.POWER_DRILL_BASIC
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.POWER_DRILL_ENHANCED
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.POWER_HOE_ADVANCED
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.POWER_HOE_BASIC
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.POWER_HOE_ENHANCED
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.RAW_ALUMINUM
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.RAW_ALUMINUM_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.RAW_TITANIUM
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.RAW_TITANIUM_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.RAW_URANIUM
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.RAW_URANIUM_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.REACTIVE_ASSEMBLY
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.REACTIVE_CHASSIS
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.REACTIVE_COMPONENT
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.REACTIVE_HOUSING
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.REACTIVE_MEMBRANE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.REACTIVE_PLATING
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.REACTOR_CONTROL
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.REACTOR_FRAME
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.REINFORCED_FRAME
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.RIFLE_RECEIVER
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.SHOTGUN_RECEIVER
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.SMB_RECEIVER
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.SMOKE_GRENADE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.SNIPER_RECEIVER
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.SPECIAL_MAGAZINE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.STANDARD_MAGAZINE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.STEEL_ASSEMBLY
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.STEEL_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.STEEL_CHASSIS
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.STEEL_INGOT
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.STEEL_MODULE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.STEEL_PLATE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.SUBMACHINE_BLASTER
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.SUPERCONDUCTOR
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.SUPERCONDUCTOR_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.SUPERCONDUCTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.TITANIUM_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.TITANIUM_INGOT
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.TITANIUM_ORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.TOOL_MODIFICATION_AUTO_COMPOST
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.TOOL_MODIFICATION_AUTO_REPLANT
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.TOOL_MODIFICATION_AUTO_SMELT
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.TOOL_MODIFICATION_EXTENDED_BAR
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.TOOL_MODIFICATION_FERTILIZER_DISPENSER
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.TOOL_MODIFICATION_FORTUNE_1
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.TOOL_MODIFICATION_FORTUNE_2
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.TOOL_MODIFICATION_FORTUNE_3
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.TOOL_MODIFICATION_POWER_CAPACITY_25
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.TOOL_MODIFICATION_POWER_CAPACITY_50
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.TOOL_MODIFICATION_RANGE_1
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.TOOL_MODIFICATION_RANGE_2
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.TOOL_MODIFICATION_RANGE_3
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.TOOL_MODIFICATION_SILK_TOUCH_MOD
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.TOOL_MODIFICATION_VEIN_MINER_25
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.UNCHARGED_SHELL
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.UNLOADED_ARSENAL_MISSILE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.UNLOADED_SHELL
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.URANIUM
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.URANIUM_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.URANIUM_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.URANIUM_ORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.URANIUM_ROD
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.WRENCH
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.ALL_GLASS_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.TERRACOTTA_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.WOOL_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Material.AMETHYST_SHARD
import org.bukkit.Material.BELL
import org.bukkit.Material.BLACKSTONE
import org.bukkit.Material.BLACK_DYE
import org.bukkit.Material.BLAST_FURNACE
import org.bukkit.Material.CHAINMAIL_HELMET
import org.bukkit.Material.CHARCOAL
import org.bukkit.Material.CHERRY_LEAVES
import org.bukkit.Material.COAL
import org.bukkit.Material.COBBLESTONE
import org.bukkit.Material.COBWEB
import org.bukkit.Material.COMPOSTER
import org.bukkit.Material.COPPER_BLOCK
import org.bukkit.Material.COPPER_INGOT
import org.bukkit.Material.CRAFTING_TABLE
import org.bukkit.Material.DARK_PRISMARINE
import org.bukkit.Material.DEEPSLATE_GOLD_ORE
import org.bukkit.Material.DEEPSLATE_REDSTONE_ORE
import org.bukkit.Material.DIAMOND
import org.bukkit.Material.DIAMOND_BLOCK
import org.bukkit.Material.DISPENSER
import org.bukkit.Material.EMERALD
import org.bukkit.Material.EMERALD_BLOCK
import org.bukkit.Material.ENCHANTED_BOOK
import org.bukkit.Material.END_ROD
import org.bukkit.Material.FEATHER
import org.bukkit.Material.FIREWORK_ROCKET
import org.bukkit.Material.GILDED_BLACKSTONE
import org.bukkit.Material.GLASS
import org.bukkit.Material.GLASS_PANE
import org.bukkit.Material.GLOWSTONE
import org.bukkit.Material.GLOWSTONE_DUST
import org.bukkit.Material.GOLD_BLOCK
import org.bukkit.Material.GOLD_INGOT
import org.bukkit.Material.GOLD_NUGGET
import org.bukkit.Material.GREEN_DYE
import org.bukkit.Material.HONEYCOMB
import org.bukkit.Material.HONEYCOMB_BLOCK
import org.bukkit.Material.HOPPER
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.IRON_INGOT
import org.bukkit.Material.IRON_NUGGET
import org.bukkit.Material.IRON_TRAPDOOR
import org.bukkit.Material.LAPIS_BLOCK
import org.bukkit.Material.LAPIS_LAZULI
import org.bukkit.Material.LEATHER
import org.bukkit.Material.MELON
import org.bukkit.Material.MOSS_BLOCK
import org.bukkit.Material.MOSS_CARPET
import org.bukkit.Material.NAME_TAG
import org.bukkit.Material.NETHERITE_BLOCK
import org.bukkit.Material.NETHER_WART
import org.bukkit.Material.NETHER_WART_BLOCK
import org.bukkit.Material.OAK_LOG
import org.bukkit.Material.OBSERVER
import org.bukkit.Material.OCHRE_FROGLIGHT
import org.bukkit.Material.PAPER
import org.bukkit.Material.PEARLESCENT_FROGLIGHT
import org.bukkit.Material.PINK_PETALS
import org.bukkit.Material.PINK_TULIP
import org.bukkit.Material.PISTON
import org.bukkit.Material.PRISMARINE
import org.bukkit.Material.PRISMARINE_BRICKS
import org.bukkit.Material.PRISMARINE_CRYSTALS
import org.bukkit.Material.QUARTZ
import org.bukkit.Material.RAW_GOLD
import org.bukkit.Material.REDSTONE
import org.bukkit.Material.REDSTONE_BLOCK
import org.bukkit.Material.RED_TERRACOTTA
import org.bukkit.Material.RESIN_CLUMP
import org.bukkit.Material.SADDLE
import org.bukkit.Material.SEA_LANTERN
import org.bukkit.Material.SHROOMLIGHT
import org.bukkit.Material.SLIME_BALL
import org.bukkit.Material.SNIFFER_EGG
import org.bukkit.Material.SPIDER_EYE
import org.bukkit.Material.SPONGE
import org.bukkit.Material.SPORE_BLOSSOM
import org.bukkit.Material.STICK
import org.bukkit.Material.STRING
import org.bukkit.Material.TRIPWIRE_HOOK
import org.bukkit.Material.TURTLE_EGG
import org.bukkit.Material.VERDANT_FROGLIGHT
import org.bukkit.Material.YELLOW_CONCRETE
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.BlastingRecipe
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.RecipeChoice.ExactChoice
import org.bukkit.inventory.RecipeChoice.MaterialChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.inventory.recipe.CookingBookCategory
import org.bukkit.inventory.recipe.CraftingBookCategory

@Suppress("unused") // Lots of helper functions which may not be used now but will be in the future
object Crafting : IonServerComponent() {
	val listOfCustomRecipes = mutableListOf<NamespacedKey>()

	override fun onEnable() {
		registerOreFurnaceRecipes()
		registerTools()
		registerMisc()

		// Prismarine Bricks
		val primarineBricksFurnaceRecipe = FurnaceRecipe(
			NamespacedKey(IonServer, "prismarine_bricks"),
			ItemStack(PRISMARINE_BRICKS),
			PRISMARINE,
			1f,
			200
		)
		primarineBricksFurnaceRecipe.category = CookingBookCategory.BLOCKS
		Bukkit.addRecipe(primarineBricksFurnaceRecipe)
		listOfCustomRecipes.add(primarineBricksFurnaceRecipe.key)

		// Bell
		shaped("bell", BELL, CraftingBookCategory.BUILDING) {
			shape("sos", "igi", "ggg")

			setIngredient('g', GOLD_BLOCK)
			setIngredient('i', IRON_BLOCK)
			setIngredient('o', OAK_LOG)
			setIngredient('s', STICK)
		}
		// Wool -> String
		for (material in WOOL_TYPES) shapeless(material.name.lowercase(), ItemStack(STRING, 4), CraftingBookCategory.BUILDING, material)
		shaped("saddle", SADDLE) {
			shape("lll", "t t")

			setIngredient('l', LEATHER)
			setIngredient('t', TRIPWIRE_HOOK)
		}
		shapedMaterial("nametag", NAME_TAG, "s", "t", "p", CraftingBookCategory.MISC, 's' to STRING, 't' to TRIPWIRE_HOOK, 'p' to PAPER)
		shapedMaterial("gilded_blackstone", GILDED_BLACKSTONE, "gbg", "bgb", "gbg", CraftingBookCategory.BUILDING, 'g' to GOLD_NUGGET, 'b' to BLACKSTONE)
		shapedMaterial("deepslate_restone_ore", DEEPSLATE_REDSTONE_ORE, "ggg", "gbg", "ggg", CraftingBookCategory.BUILDING, 'g' to REDSTONE, 'b' to Material.DEEPSLATE)
		shapedMaterial("deepslate_gold_ore", DEEPSLATE_GOLD_ORE, "gbg", "bgb", "gbg", CraftingBookCategory.BUILDING, 'g' to RAW_GOLD, 'b' to Material.DEEPSLATE)
		shapedMaterial("sniffer_egg", SNIFFER_EGG, "rdr", "ded", "rdr", CraftingBookCategory.MISC, 'r' to RED_TERRACOTTA, 'd' to DARK_PRISMARINE, 'e' to TURTLE_EGG)
		shapedMaterial("ochre_froglight", OCHRE_FROGLIGHT, " x ", "xlx", " x ", CraftingBookCategory.BUILDING, 'x' to HONEYCOMB, 'l' to SHROOMLIGHT)
		shapeless("pale_oak", ItemStack(Material.PALE_OAK_SAPLING), CraftingBookCategory.BUILDING, Material.OAK_SAPLING, Material.BONE)
		shapeless("pale_moss", ItemStack(Material.PALE_MOSS_BLOCK), CraftingBookCategory.BUILDING, MOSS_BLOCK, Material.PALE_OAK_LEAVES)
		shapedMaterial("verdant_froglight", VERDANT_FROGLIGHT, " x ", "xlx", " x ", CraftingBookCategory.BUILDING, 'x' to SLIME_BALL, 'l' to SHROOMLIGHT)
		shapedMaterial("pearlescent_froglight", PEARLESCENT_FROGLIGHT, " x ", "xlx", " x ", CraftingBookCategory.BUILDING, 'x' to AMETHYST_SHARD, 'l' to SHROOMLIGHT)
		shaped("spore_blossom", SPORE_BLOSSOM, CraftingBookCategory.BUILDING) {
			shape(" a ", "ctc", " m ")

			setIngredient('a', AMETHYST_SHARD)
			setIngredient('t', PINK_TULIP)
			setIngredient('c', MOSS_CARPET)
			setIngredient('m', MOSS_BLOCK)
		}
		shapeless("prismarine_crystals", ItemStack(PRISMARINE_CRYSTALS, 4), CraftingBookCategory.MISC, SEA_LANTERN)
		shapeless("pink_petals", ItemStack(PINK_PETALS, 4), CraftingBookCategory.BUILDING, CHERRY_LEAVES)
		shapeless("nether_warts", ItemStack(NETHER_WART, 9), CraftingBookCategory.BUILDING, NETHER_WART_BLOCK)
		shapeless("honeycomb", ItemStack(HONEYCOMB, 9), CraftingBookCategory.MISC, HONEYCOMB_BLOCK)
		shapedMaterial("cobweb", COBWEB, "s s", " s ", "s s", CraftingBookCategory.BUILDING, 's' to STRING)
		shapedMaterial("small_dripleaf" , Material.SMALL_DRIPLEAF, shape1 = "xx ", shape2 = " y ", shape3 = "   ", CraftingBookCategory.BUILDING,'x' to Material.OAK_LEAVES, 'y' to Material.BAMBOO)
		shapedMaterial("big_dripleaf" , Material.BIG_DRIPLEAF, shape1 = "xxx", shape2 = "  y", shape3 = "  y", CraftingBookCategory.BUILDING, 'x' to Material.OAK_LEAVES, 'y' to Material.BAMBOO)
		shapeless("glowstone_dust", ItemStack(GLOWSTONE_DUST, 4), CraftingBookCategory.MISC, GLOWSTONE)
		shapeless("resin", ItemStack(RESIN_CLUMP), CraftingBookCategory.MISC, ItemStack(HONEYCOMB, 2), ItemStack(COBBLESTONE, 2))

		Bukkit.removeRecipe(Material.ENDER_CHEST.key)
		Bukkit.removeRecipe(Material.IRON_CHAIN.key)
		shaped("chain", ItemStack(Material.IRON_CHAIN, 4), CraftingBookCategory.BUILDING) {
			shape("n", "i", "n")

			setIngredient('n', IRON_NUGGET)
			setIngredient('i', IRON_INGOT)
		}
		shaped("Unloaded__Shell", UNLOADED_SHELL.getValue().constructItemStack(), CraftingBookCategory.MISC) {
			shape(" y ", " z ")

			setIngredient('y', LAPIS_LAZULI)
			setIngredient('z', ExactChoice(TITANIUM_INGOT.getValue().constructItemStack()))
		}
		shaped("Uncharged_Shell", UNCHARGED_SHELL.getValue().constructItemStack(), CraftingBookCategory.MISC) {
			shape(" y ", " z ")

			setIngredient('y', PRISMARINE_CRYSTALS)
			setIngredient('z', COPPER_INGOT)
		}
		shaped("Unloaded_Arsenal_Missile", UNLOADED_ARSENAL_MISSILE.getValue().constructItemStack(), CraftingBookCategory.MISC) {
			shape("aba", "mum", "hlo")

			setIngredient('a', ExactChoice(REACTIVE_HOUSING.getValue().constructItemStack()))
			setIngredient('b', ExactChoice(STEEL_PLATE.getValue().constructItemStack()))
			setIngredient('m', ExactChoice(CIRCUITRY.getValue().constructItemStack()))
			setIngredient('u', ExactChoice(URANIUM_ROD.getValue().constructItemStack()))
			setIngredient('h', ExactChoice(GAS_CANISTER_HYDROGEN.getValue().constructItemStack()))
			setIngredient('l', LAPIS_BLOCK)
			setIngredient('o', ExactChoice(GAS_CANISTER_OXYGEN.getValue().constructItemStack()))
		}
		shaped("blaster_barrel", GUN_BARREL.getValue().constructItemStack(), CraftingBookCategory.MISC) {
			shape("tct", "ppp", "tct")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.getValue().constructItemStack()))
			setIngredient('c', COPPER_INGOT)
			setIngredient('p', PRISMARINE_CRYSTALS)
		}
		shaped("pistol_receiver", PISTOL_RECEIVER.getValue().constructItemStack(), CraftingBookCategory.MISC) {
			shape("irt")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.getValue().constructItemStack()))
			setIngredient('r', REDSTONE_BLOCK)
			setIngredient('i', IRON_TRAPDOOR)
		}
		shaped("rifle_receiver", RIFLE_RECEIVER.getValue().constructItemStack(), CraftingBookCategory.MISC) {
			shape(" t ", "igt", " t ")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.getValue().constructItemStack()))
			setIngredient('g', GOLD_BLOCK)
			setIngredient('i', IRON_TRAPDOOR)
		}
		shaped("smb_receiver", SMB_RECEIVER.getValue().constructItemStack(), CraftingBookCategory.MISC) {
			shape(" t ", "id ", " t ")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.getValue().constructItemStack()))
			setIngredient('d', DIAMOND_BLOCK)
			setIngredient('i', IRON_TRAPDOOR)
		}
		shaped("sniper_receiver", SNIPER_RECEIVER.getValue().constructItemStack(), CraftingBookCategory.MISC) {
			shape(" t ", "ieb", " t ")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.getValue().constructItemStack()))
			setIngredient('e', EMERALD_BLOCK)
			setIngredient('b', ExactChoice(TITANIUM_BLOCK.getValue().constructItemStack()))
			setIngredient('i', IRON_TRAPDOOR)
		}
		shaped("shotgun_receiver", SHOTGUN_RECEIVER.getValue().constructItemStack(), CraftingBookCategory.MISC) {
			shape("   ", "icb", " t ")

			setIngredient('t', TITANIUM_INGOT)
			setIngredient('c', COPPER_BLOCK)
			setIngredient('b', TITANIUM_BLOCK)
			setIngredient('i', IRON_TRAPDOOR)
		}
		shaped("cannon_receiver", CANNON_RECEIVER.getValue().constructItemStack(), CraftingBookCategory.MISC) {
			shape("   ", " ba", "g  ")

			setIngredient('a', ALUMINUM_INGOT)
			setIngredient('b', ALUMINUM_BLOCK)
			setIngredient('g', GOLD_INGOT)
		}
		shaped("pistol", BLASTER_PISTOL.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("   ", "apb", "c  ")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.getValue().constructItemStack()))
			setIngredient('p', PISTOL_RECEIVER.getValue().constructItemStack())
			setIngredient('b', GUN_BARREL.getValue().constructItemStack())
			setIngredient('c', CIRCUITRY.getValue().constructItemStack())

		}
		shaped("rifle", BLASTER_RIFLE.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("   ", "apb", "ac ")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.getValue().constructItemStack()))
			setIngredient('p', RIFLE_RECEIVER.getValue().constructItemStack())
			setIngredient('b', GUN_BARREL.getValue().constructItemStack())
			setIngredient('c', CIRCUITRY.getValue().constructItemStack())

		}
		shaped("submachine_blaster", SUBMACHINE_BLASTER.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("   ", "apb", "ac ")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.getValue().constructItemStack()))
			setIngredient('p', SMB_RECEIVER.getValue().constructItemStack())
			setIngredient('b', GUN_BARREL.getValue().constructItemStack())
			setIngredient('c', CIRCUITRY.getValue().constructItemStack())

		}
		shaped("sniper", BLASTER_SNIPER.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape(" g ", "apb", "ac ")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.getValue().constructItemStack()))
			setIngredient('p', SNIPER_RECEIVER.getValue().constructItemStack())
			setIngredient('b', GUN_BARREL.getValue().constructItemStack())
			setIngredient('c', CIRCUITRY.getValue().constructItemStack())
			setIngredient('g', GLASS)

		}
		shaped("shotgun", BLASTER_SHOTGUN.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("  b", "apb", "ac ")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.getValue().constructItemStack()))
			setIngredient('p', SHOTGUN_RECEIVER.getValue().constructItemStack())
			setIngredient('b', GUN_BARREL.getValue().constructItemStack())
			setIngredient('c', CIRCUITRY.getValue().constructItemStack())

		}
		shaped("cannon", BLASTER_CANNON.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape(" a ", " cb", "p  ")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.getValue().constructItemStack()))
			setIngredient('p', CANNON_RECEIVER.getValue().constructItemStack())
			setIngredient('b', GUN_BARREL.getValue().constructItemStack())
			setIngredient('c', CIRCUITRY.getValue().constructItemStack())

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.getValue().constructItemStack()))
		}
		shaped("power_drill_basic", POWER_DRILL_BASIC.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("i  ", " bt", " ts")

			setIngredient('i', ExactChoice(ItemStack(IRON_INGOT)))
			setIngredient('b', ExactChoice(BATTERY_M.getValue().constructItemStack()))
			setIngredient('t', ExactChoice(TITANIUM_INGOT.getValue().constructItemStack()))
			setIngredient('s', STICK)

		}
		shaped("power_drill_enhanced", POWER_DRILL_ENHANCED.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("ii ", "idc", " ts")

			setIngredient('i', ExactChoice(TITANIUM_BLOCK.getValue().constructItemStack()))
			setIngredient('d', ExactChoice(POWER_DRILL_BASIC.getValue().constructItemStack()))
			setIngredient('c', ExactChoice(CIRCUITRY.getValue().constructItemStack()))
			setIngredient('t', ExactChoice(URANIUM_BLOCK.getValue().constructItemStack()))
			setIngredient('s', ExactChoice(BATTERY_G.getValue().constructItemStack()))

		}
		shaped("power_drill_advanced", POWER_DRILL_ADVANCED.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("ii ", "idc", " ts")

			setIngredient('i', ExactChoice(STEEL_PLATE.getValue().constructItemStack()))
			setIngredient('d', ExactChoice(POWER_DRILL_ENHANCED.getValue().constructItemStack()))
			setIngredient('c', ExactChoice(CIRCUIT_BOARD.getValue().constructItemStack()))
			setIngredient('t', ExactChoice(SUPERCONDUCTOR.getValue().constructItemStack()))
			setIngredient('s', ExactChoice(STEEL_CHASSIS.getValue().constructItemStack()))

		}
		shaped("power_chainsaw_basic", POWER_CHAINSAW_BASIC.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("ii ", "idc", " cs")

			setIngredient('i', ExactChoice(ItemStack(IRON_INGOT)))
			setIngredient('d', ExactChoice(BATTERY_M.getValue().constructItemStack()))
			setIngredient('c', ExactChoice(TITANIUM_INGOT.getValue().constructItemStack()))
			setIngredient('s', STICK)

		}
		shaped("power_chainsaw_enhanced", POWER_CHAINSAW_ENHANCED.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("ii ", "idc", " us")

			setIngredient('i', ExactChoice(TITANIUM_BLOCK.getValue().constructItemStack()))
			setIngredient('d', ExactChoice(POWER_CHAINSAW_BASIC.getValue().constructItemStack()))
			setIngredient('c', ExactChoice(CIRCUITRY.getValue().constructItemStack()))
			setIngredient('u', ExactChoice(URANIUM_BLOCK.getValue().constructItemStack()))
			setIngredient('s', ExactChoice(BATTERY_G.getValue().constructItemStack()))
		}
		shaped("power_chainsaw_advanced", POWER_CHAINSAW_ADVANCED.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("pb ", "bdc", " ts")

			setIngredient('p', ExactChoice(STEEL_PLATE.getValue().constructItemStack()))
			setIngredient('b', ExactChoice(STEEL_BLOCK.getValue().constructItemStack()))
			setIngredient('d', ExactChoice(POWER_CHAINSAW_ENHANCED.getValue().constructItemStack()))
			setIngredient('c', ExactChoice(CIRCUIT_BOARD.getValue().constructItemStack()))
			setIngredient('t', ExactChoice(SUPERCONDUCTOR.getValue().constructItemStack()))
			setIngredient('s', ExactChoice(STEEL_CHASSIS.getValue().constructItemStack()))
		}
		shaped("power_hoe_basic", POWER_HOE_BASIC.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape(" ib", " si", "cc ")

			setIngredient('b', ExactChoice(BATTERY_M.getValue().constructItemStack()))
			setIngredient('i', COPPER_INGOT)
			setIngredient('c', ExactChoice(TITANIUM_INGOT.getValue().constructItemStack()))
			setIngredient('s', STICK)
		}
		shaped("power_hoe_enhanced", POWER_HOE_ENHANCED.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape(" us", " dc", "ii ")

			setIngredient('d', ExactChoice(POWER_HOE_BASIC.getValue().constructItemStack()))
			setIngredient('i', ExactChoice(TITANIUM_BLOCK.getValue().constructItemStack()))
			setIngredient('c', ExactChoice(CIRCUITRY.getValue().constructItemStack()))
			setIngredient('u', ExactChoice(URANIUM_BLOCK.getValue().constructItemStack()))
			setIngredient('s', ExactChoice(BATTERY_G.getValue().constructItemStack()))
		}
		shaped("power_hoe_advanced", POWER_HOE_ADVANCED.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape(" tu", " dc", "ss ")

			setIngredient('d', ExactChoice(POWER_HOE_ENHANCED.getValue().constructItemStack()))
			setIngredient('s', ExactChoice(STEEL_BLOCK.getValue().constructItemStack()))
			setIngredient('c', ExactChoice(CIRCUIT_BOARD.getValue().constructItemStack()))
			setIngredient('t', ExactChoice(SUPERCONDUCTOR.getValue().constructItemStack()))
			setIngredient('u', ExactChoice(STEEL_CHASSIS.getValue().constructItemStack()))
		}
		shaped("crate_placer", CRATE_PLACER.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape(" s ", " cd", "t  ")

			setIngredient('s', ExactChoice(STEEL_INGOT.getValue().constructItemStack()))
			setIngredient('t', GAS_CANISTER_EMPTY.getValue().constructItemStack())
			setIngredient('d', DIAMOND)
			setIngredient('c', CIRCUITRY.getValue().constructItemStack())
		}
		shaped("circuitry_1", CIRCUITRY.getValue().constructItemStack()) {
			shape("qdq", "arg", "ccc")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.getValue().constructItemStack()))
			setIngredient('c', COPPER_INGOT)
			setIngredient('q', QUARTZ)
			setIngredient('g', GOLD_INGOT)
			setIngredient('d', GREEN_DYE)
			setIngredient('r', REDSTONE)
		}
		shaped("circuitry_2", CIRCUITRY.getValue().constructItemStack()) {
			shape("qdq", "gra", "ccc")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.getValue().constructItemStack()))
			setIngredient('c', COPPER_INGOT)
			setIngredient('q', QUARTZ)
			setIngredient('g', GOLD_INGOT)
			setIngredient('d', GREEN_DYE)
			setIngredient('r', REDSTONE)
		}
		shaped("circuitry_3", CIRCUITRY) {
			shape("grg", "qqq", "ccc")

			setIngredient('c', COPPER_INGOT)
			setIngredient('g', GOLD_INGOT)
			setIngredient('q', QUARTZ)
			setIngredient('r', REDSTONE)
		}
		shaped("standard_magazine", STANDARD_MAGAZINE.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("   ", "rlr", "ttt")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.getValue().constructItemStack()))
			setIngredient('l', LAPIS_BLOCK)
			setIngredient('r', REDSTONE)

		}
		shaped("special_magazine", SPECIAL_MAGAZINE.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("   ", "rer", "ttt")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.getValue().constructItemStack()))
			setIngredient('e', EMERALD_BLOCK)
			setIngredient('r', REDSTONE)

		}
		shaped("empty_gas_canister", GAS_CANISTER_EMPTY.getValue().constructItemStack()) {
			shape(" i ", "igi", " i ")

			setIngredient('i', ExactChoice(ALUMINUM_INGOT.getValue().constructItemStack()))
			setIngredient('g', GLASS_PANE)

		}
		shaped("detonator", DETONATOR.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape(" r ", "tut", " t ")

			setIngredient('r', REDSTONE)
			setIngredient('t', TITANIUM_INGOT.getValue().constructItemStack())
			setIngredient('u', URANIUM.getValue().constructItemStack())
		}
		shaped("smokeGrenade", SMOKE_GRENADE.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape(" i ", "tct", " t ")

			setIngredient('i', IRON_INGOT)
			setIngredient('t', TITANIUM_INGOT.getValue().constructItemStack())
			setIngredient('c', COAL)
		}
		materialBlockRecipes(ALUMINUM_BLOCK, ALUMINUM_INGOT)
		materialBlockRecipes(RAW_ALUMINUM_BLOCK, RAW_ALUMINUM)
		materialBlockRecipes(CHETHERITE_BLOCK, CHETHERITE)
		materialBlockRecipes(TITANIUM_BLOCK, TITANIUM_INGOT)
		materialBlockRecipes(RAW_TITANIUM_BLOCK, RAW_TITANIUM)
		materialBlockRecipes(URANIUM_BLOCK, URANIUM)
		materialBlockRecipes(RAW_URANIUM_BLOCK, RAW_URANIUM)
		materialBlockRecipes(ENRICHED_URANIUM_BLOCK, ENRICHED_URANIUM)
		materialBlockRecipes(STEEL_BLOCK, STEEL_INGOT)

		shapeless("steelPlate", STEEL_PLATE.getValue().constructItemStack(), CraftingBookCategory.MISC, STEEL_BLOCK.getValue().constructItemStack(9))
		shapeless("steelModule", STEEL_MODULE.getValue().constructItemStack(), CraftingBookCategory.MISC, STEEL_CHASSIS.getValue().constructItemStack(9))
		shapeless("steelAssembly", STEEL_ASSEMBLY.getValue().constructItemStack(), CraftingBookCategory.MISC, STEEL_MODULE.getValue().constructItemStack(4))
		shapeless("reactorFrame", REACTOR_FRAME.getValue().constructItemStack(), CraftingBookCategory.MISC, REINFORCED_FRAME.getValue().constructItemStack(4))
		shapeless("uraniumCore", URANIUM_CORE.getValue().constructItemStack(), CraftingBookCategory.MISC, ENRICHED_URANIUM_BLOCK.getValue().constructItemStack(9))
		shapeless("fuelRodCore", FUEL_ROD_CORE.getValue().constructItemStack(), CraftingBookCategory.MISC, URANIUM_ROD.getValue().constructItemStack(9))
		shapeless("fuelControl", FUEL_CONTROL.getValue().constructItemStack(), CraftingBookCategory.MISC, FUEL_CELL.getValue().constructItemStack(9))
		shapeless("melonToSlices", ItemStack(Material.MELON_SLICE).asQuantity(4), CraftingBookCategory.MISC, MELON)

		shaped("reactiveComponent", REACTIVE_HOUSING.getValue().constructItemStack()) {
			shape("xxx", "yyy", "xxx")

			setIngredient('x', MaterialChoice(*TERRACOTTA_TYPES.toTypedArray()) )
			setIngredient('y', SPONGE)
		}
		shaped("netheriteCasing", NETHERITE_CASING.getValue().constructItemStack(), CraftingBookCategory.BUILDING) {
			shape("xvx", "xyx", "xvx")

			setIngredient('x', NETHERITE_BLOCK)
			setIngredient('y', STEEL_PLATE.getValue().constructItemStack())
			setIngredient('v', REACTIVE_HOUSING.getValue().constructItemStack())
		}
		shaped("reactiveHousing", REACTIVE_COMPONENT.getValue().constructItemStack()) {
			shape("xxx", "yyy", "xxx")

			setIngredient('x', REDSTONE_BLOCK )
			setIngredient('y', COPPER_BLOCK)
		}
		shapeless("reactivePlating", result = REACTIVE_PLATING.getValue().constructItemStack(), CraftingBookCategory.MISC, REACTIVE_COMPONENT, REACTIVE_HOUSING)
		shapeless("reactiveMembrane", result = REACTIVE_MEMBRANE.getValue().constructItemStack(), CraftingBookCategory.MISC, REACTIVE_CHASSIS.getValue().constructItemStack(7), CIRCUITRY.getValue().constructItemStack(), ENRICHED_URANIUM.getValue().constructItemStack())
		shapeless("reactiveAssembly", REACTIVE_ASSEMBLY.getValue().constructItemStack(), CraftingBookCategory.MISC, REACTIVE_MEMBRANE.getValue().constructItemStack(9))
		shapeless("circuitBoard", MOTHERBOARD.getValue().constructItemStack(), CraftingBookCategory.MISC, CIRCUIT_BOARD.getValue().constructItemStack(9))
		shapeless("reactorControl", REACTOR_CONTROL.getValue().constructItemStack(), CraftingBookCategory.MISC, FABRICATED_ASSEMBLY.getValue().constructItemStack(6), MOTHERBOARD.getValue().constructItemStack(3))
		materialBlockRecipes(SUPERCONDUCTOR_BLOCK, SUPERCONDUCTOR)
		shapeless("superconductorCore", SUPERCONDUCTOR_CORE.getValue().constructItemStack(), CraftingBookCategory.MISC, SUPERCONDUCTOR_BLOCK.getValue().constructItemStack(), MOTHERBOARD.getValue().constructItemStack(4))
		shaped("bcreactorCore", BATTLECRUISER_REACTOR_CORE.getValue().constructItemStack(), CraftingBookCategory.BUILDING) {
			shape("wxw", "yzy", "wxw")

			setIngredient('w', REACTOR_FRAME.getValue().constructItemStack())
			setIngredient('x', REACTOR_CONTROL.getValue().constructItemStack())
			setIngredient('y', FUEL_CONTROL.getValue().constructItemStack())
			setIngredient('z', SUPERCONDUCTOR_CORE.getValue().constructItemStack())
		}
		shaped("bargereactorCore", BARGE_REACTOR_CORE.getValue().constructItemStack(), CraftingBookCategory.BUILDING) {
			shape("wxw", "zzz", "vyv")

			setIngredient('w', REACTOR_FRAME.getValue().constructItemStack())
			setIngredient('x', REACTOR_CONTROL.getValue().constructItemStack())
			setIngredient('y', FUEL_CONTROL.getValue().constructItemStack())
			setIngredient('z', SUPERCONDUCTOR.getValue().constructItemStack())
			setIngredient('v', REINFORCED_FRAME.getValue().constructItemStack())
		}
		shaped("cruiserreactorCore", CRUISER_REACTOR_CORE.getValue().constructItemStack(), CraftingBookCategory.BUILDING) {
			shape("wxw", "wyw", "wzw")

			setIngredient('w', REINFORCED_FRAME.getValue().constructItemStack())
			setIngredient('x', REACTOR_CONTROL.getValue().constructItemStack())
			setIngredient('y', SUPERCONDUCTOR_CORE.getValue().constructItemStack())
			setIngredient('z', FUEL_CONTROL.getValue().constructItemStack())
		}

		shaped("multiblock_workbench", MULTIBLOCK_WORKBENCH.getValue().constructItemStack(), CraftingBookCategory.MISC) {
			shape("i", "c")

			setIngredient('i', IRON_BLOCK)
			setIngredient('c', CRAFTING_TABLE)
		}

		// Tool Mods start
		shaped("silk_touch_modifier", TOOL_MODIFICATION_SILK_TOUCH_MOD.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("gbg", "tst", "ctc")

			setIngredient('g', RAW_GOLD)
			setIngredient('b', TITANIUM_BLOCK.getValue().constructItemStack())
			setIngredient('t', TITANIUM_INGOT.getValue().constructItemStack())
			setIngredient('s', ItemStack(ENCHANTED_BOOK).updateData(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantments.itemEnchantments(mapOf(Enchantment.SILK_TOUCH to 1))))
			setIngredient('c', CIRCUIT_BOARD.getValue().constructItemStack())
		}

		shaped("fortune_1_touch_modifier", TOOL_MODIFICATION_FORTUNE_1.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("dgd", "csc", "dgd")

			setIngredient('d', DIAMOND)
			setIngredient('g', GOLD_BLOCK)
			setIngredient('c', REACTIVE_COMPONENT.getValue().constructItemStack())
			setIngredient('s', SUPERCONDUCTOR.getValue().constructItemStack())
		}
		shaped("fortune_2_touch_modifier", TOOL_MODIFICATION_FORTUNE_2.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("dgd", "csc", "dgd")

			setIngredient('d', STEEL_PLATE.getValue().constructItemStack())
			setIngredient('g', URANIUM_BLOCK.getValue().constructItemStack())
			setIngredient('c', REACTIVE_PLATING.getValue().constructItemStack())
			setIngredient('s', TOOL_MODIFICATION_FORTUNE_1.getValue().constructItemStack())
		}
		shaped("fortune_3_touch_modifier", TOOL_MODIFICATION_FORTUNE_3.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("dgd", "csc", "dgd")

			setIngredient('d', STEEL_ASSEMBLY.getValue().constructItemStack())
			setIngredient('g', ENRICHED_URANIUM_BLOCK.getValue().constructItemStack())
			setIngredient('c', REACTIVE_ASSEMBLY.getValue().constructItemStack())
			setIngredient('s', TOOL_MODIFICATION_FORTUNE_2.getValue().constructItemStack())
		}
		shaped("power_capacity_25_modifier", TOOL_MODIFICATION_POWER_CAPACITY_25.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("ibi", "brb", "ici")

			setIngredient('i', IRON_INGOT)
			setIngredient('b', BATTERY_M.getValue().constructItemStack())
			setIngredient('r', REDSTONE_BLOCK)
			setIngredient('c', END_ROD)
		}
		shaped("power_capacity_50_modifier", TOOL_MODIFICATION_POWER_CAPACITY_50.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("sbs", "brb", "scs")

			setIngredient('s', IRON_INGOT)
			setIngredient('b', BATTERY_G.getValue().constructItemStack())
			setIngredient('r', TOOL_MODIFICATION_POWER_CAPACITY_25)
			setIngredient('c', END_ROD)
		}
		shaped("auto_smelt_modifier", TOOL_MODIFICATION_AUTO_SMELT.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("iri", "bfb", "ici")

			setIngredient('i', IRON_INGOT)
			setIngredient('b', GOLD_BLOCK)
			setIngredient('f', BLAST_FURNACE)
			setIngredient('r', REDSTONE_BLOCK)
			setIngredient('c', CIRCUITRY.getValue().constructItemStack())
		}
		shaped("auto_compost_modifier", TOOL_MODIFICATION_AUTO_COMPOST.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("tit", "tct", "trt")

			setIngredient('t', TITANIUM_INGOT.getValue().constructItemStack())
			setIngredient('i', IRON_INGOT)
			setIngredient('c', COMPOSTER)
			setIngredient('r', REDSTONE_BLOCK)
		}
		shaped("auto_replant_modifier", TOOL_MODIFICATION_AUTO_REPLANT.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("ipi", "tct", "iri")

			setIngredient('i', IRON_INGOT)
			setIngredient('p', PISTON)
			setIngredient('t', TITANIUM_INGOT.getValue().constructItemStack())
			setIngredient('c', DISPENSER)
			setIngredient('r', REDSTONE_BLOCK)
		}
		shaped("auto_fertilizer_modifier", TOOL_MODIFICATION_FERTILIZER_DISPENSER.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("ipi", "tct", "iri")

			setIngredient('i', IRON_INGOT)
			setIngredient('p', HOPPER)
			setIngredient('t', TITANIUM_INGOT.getValue().constructItemStack())
			setIngredient('c', DISPENSER)
			setIngredient('r', REDSTONE_BLOCK)
		}
		shaped("extended_bar_modifier", TOOL_MODIFICATION_EXTENDED_BAR.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("st ", "tst", " ts")

			setIngredient('s', STEEL_INGOT.getValue().constructItemStack())
			setIngredient('t', TITANIUM_INGOT.getValue().constructItemStack())
		}
		shaped("aoe_1_modifier", TOOL_MODIFICATION_RANGE_1.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("ipi", "prp", "ipi")

			setIngredient('i', IRON_INGOT)
			setIngredient('p', PISTON)
			setIngredient('r', REDSTONE_BLOCK)
		}
		shaped("aoe_2_modifier", TOOL_MODIFICATION_RANGE_2.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("iii", "prp", "iii")

			setIngredient('i', IRON_BLOCK)
			setIngredient('p', PISTON)
			setIngredient('r', ExactChoice(TOOL_MODIFICATION_RANGE_1.getValue().constructItemStack()))
		}

		shaped("aoe_3_modifier", TOOL_MODIFICATION_RANGE_3.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("iii", "prp", "iii")

			setIngredient('i', ExactChoice(STEEL_INGOT.getValue().constructItemStack()))
			setIngredient('p', PISTON)
			setIngredient('r', ExactChoice(TOOL_MODIFICATION_RANGE_2.getValue().constructItemStack()))
		}
		shaped("vein_miner_modifier", TOOL_MODIFICATION_VEIN_MINER_25.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("ipi", "prp", "ipi")

			setIngredient('i', ALUMINUM_INGOT)
			setIngredient('p', OBSERVER)
			setIngredient('r', TOOL_MODIFICATION_RANGE_1)
		}


		fun registerBatteryRecipe(battery: IonRegistryKey<CustomItem, out CustomItem>, material: Material) = shaped(battery.key.lowercase(), battery.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("aba", "aba", "aba")
			setIngredient('a', ExactChoice(ALUMINUM_INGOT.getValue().constructItemStack()))
			setIngredient('b', material)
		}
		registerBatteryRecipe(BATTERY_A, GLOWSTONE_DUST)
		registerBatteryRecipe(BATTERY_M, REDSTONE)
		registerBatteryRecipe(BATTERY_G, SEA_LANTERN)

		fun registerArmorRecipe(result: IonRegistryKey<CustomItem, out CustomItem>, vararg shape: String) = shaped(result.key.lowercase(), result.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape(*shape)
			setIngredient('*', TITANIUM_INGOT)
			setIngredient('b', BATTERY_G)
		}
		registerArmorRecipe(POWER_ARMOR_HELMET, "*b*", "* *")
		registerArmorRecipe(POWER_ARMOR_CHESTPLATE, "* *", "*b*", "***")
		registerArmorRecipe(POWER_ARMOR_LEGGINGS, "*b*", "* *", "* *")
		registerArmorRecipe(POWER_ARMOR_BOOTS, "* *", "*b*")

		fun registerPowerArmorModule(result: IonRegistryKey<CustomItem, out CustomItem>, center: RecipeChoice) = shaped(result.key.lowercase(), result.getValue().constructItemStack(), CraftingBookCategory.EQUIPMENT) {
			shape("aga", "g*g", "aga")
			setIngredient('a', ALUMINUM_INGOT)
			setIngredient('g', GLASS_PANE)
			setIngredient('*', center)
		}

		registerPowerArmorModule(ARMOR_MODIFICATION_SHOCK_ABSORBING, ExactChoice(TITANIUM_INGOT.getValue().constructItemStack()))
		registerPowerArmorModule(ARMOR_MODIFICATION_SPEED_BOOSTING, MaterialChoice(FEATHER))
		registerPowerArmorModule(ARMOR_MODIFICATION_ROCKET_BOOSTING, MaterialChoice(FIREWORK_ROCKET))
		registerPowerArmorModule(ARMOR_MODIFICATION_NIGHT_VISION, MaterialChoice(SPIDER_EYE))
		registerPowerArmorModule(ARMOR_MODIFICATION_ENVIRONMENT, MaterialChoice(CHAINMAIL_HELMET))
		registerPowerArmorModule(ARMOR_MODIFICATION_PRESSURE_FIELD, ExactChoice(GAS_CANISTER_EMPTY.getValue().constructItemStack()))

		fun registerSwordRecipes(sword: IonRegistryKey<CustomItem, out CustomItem>, choice: RecipeChoice) = shaped(sword.key.lowercase(), sword, CraftingBookCategory.EQUIPMENT) {
			shape("aga", "a*a", "ata")
			setIngredient('a', ALUMINUM_INGOT)
			setIngredient('g', GLASS_PANE)
			setIngredient('*', choice)
			setIngredient('t', TITANIUM_INGOT)
		}

		registerSwordRecipes(ENERGY_SWORD_BLUE, MaterialChoice(DIAMOND))
		registerSwordRecipes(ENERGY_SWORD_RED, MaterialChoice(REDSTONE))
		registerSwordRecipes(ENERGY_SWORD_YELLOW, MaterialChoice(COAL))
		registerSwordRecipes(ENERGY_SWORD_GREEN, MaterialChoice(EMERALD))
		registerSwordRecipes(ENERGY_SWORD_PURPLE, ExactChoice(CHETHERITE.getValue().constructItemStack()))
		registerSwordRecipes(ENERGY_SWORD_ORANGE, MaterialChoice(COPPER_INGOT))
		registerSwordRecipes(ENERGY_SWORD_PINK, MaterialChoice(PINK_TULIP))
	}

	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		event.player.discoverRecipes(listOfCustomRecipes)
	}

	private fun registerOreFurnaceRecipes() {
		fun registerFurnaceRecipe(smelted: IonRegistryKey<CustomItem, out CustomItem>, result: IonRegistryKey<CustomItem, out CustomItem>, category: CookingBookCategory = CookingBookCategory.MISC) {
			val furnaceRecipe = FurnaceRecipe(
				NamespacedKey(IonServer, "${smelted.key.lowercase()}_smelting"),
				result.getValue().constructItemStack(),
				ExactChoice(smelted.getValue().constructItemStack()),
				0.5f,
				200
			)
			furnaceRecipe.category = category
			Bukkit.addRecipe(furnaceRecipe)
			listOfCustomRecipes.add(furnaceRecipe.key)

			val blastingRecipe = BlastingRecipe(
				NamespacedKey(IonServer, "${smelted.key.lowercase()}_blasting"),
				result.getValue().constructItemStack(),
				ExactChoice(smelted.getValue().constructItemStack()),
				0.5f,
				100
			)
			blastingRecipe.category = category
			Bukkit.addRecipe(blastingRecipe)
			listOfCustomRecipes.add(blastingRecipe.key)
		}

		fun registerOreType(rawType: IonRegistryKey<CustomItem, out CustomItem>, oreType: IonRegistryKey<CustomItem, out CustomItem>, smeltedType: IonRegistryKey<CustomItem, out CustomItem>) {
			registerFurnaceRecipe(rawType, smeltedType)
			registerFurnaceRecipe(oreType, smeltedType)
		}

		registerOreType(rawType = RAW_ALUMINUM, oreType = ALUMINUM_ORE, smeltedType = ALUMINUM_INGOT)
		registerOreType(rawType = RAW_TITANIUM, oreType = TITANIUM_ORE, smeltedType = TITANIUM_INGOT)
		registerOreType(rawType = RAW_URANIUM, oreType = URANIUM_ORE, smeltedType = URANIUM)
		registerFurnaceRecipe(smelted = CHETHERITE_BLOCK, result = CHETHERITE)
	}

	private fun registerTools() {
		shaped("wrench", WRENCH, CraftingBookCategory.EQUIPMENT) {
			shape("a a", " a ", " a ")
			setIngredient('a', IRON_INGOT)
		}
		shaped("multimeter", MULTIMETER, CraftingBookCategory.EQUIPMENT) {
			shape("yry", "ycy", "yiy")
			setIngredient('y', YELLOW_CONCRETE)
			setIngredient('r', REDSTONE)
			setIngredient('c', CIRCUITRY)
			setIngredient('i', COPPER_INGOT)
		}
	}

	private fun registerMisc() {
		shaped("advanced_item_extractor", ADVANCED_ITEM_EXTRACTOR, CraftingBookCategory.BUILDING) {
			shape(" g ", "rcr", " g ")
			setIngredient('c', CRAFTING_TABLE)
			setIngredient('g', MaterialChoice(*ALL_GLASS_TYPES.toTypedArray()))
			setIngredient('r', REDSTONE)
		}
		shaped("item_filter", ITEM_FILTER, CraftingBookCategory.BUILDING) {
			shape(" g ", "rhr", " g ")
			setIngredient('h', HOPPER)
			setIngredient('g', MaterialChoice(*ALL_GLASS_TYPES.toTypedArray()))
			setIngredient('r', REDSTONE)
		}
		val blackDyeRecipe = FurnaceRecipe(
			NamespacedKey(IonServer, "black_dye_smelting"),
			ItemStack(BLACK_DYE),
			MaterialChoice(CHARCOAL),
			0.5f,
			200
		)
		blackDyeRecipe.category = CookingBookCategory.MISC
		Bukkit.addRecipe(blackDyeRecipe)
		listOfCustomRecipes.add(blackDyeRecipe.key)
	}

	// Different names due to signature problems from type erasure
	private fun shapedMaterial(name: String, result: Material, shape1: String, shape2: String, shape3: String, category: CraftingBookCategory = CraftingBookCategory.MISC, vararg ingredients: Pair<Char, Material>) {
		val recipe = ShapedRecipe(NamespacedKeys.key(name), ItemStack(result))
		recipe.shape(shape1, shape2, shape3)
		for ((key, ingredient) in ingredients) recipe.setIngredient(key, ingredient)
		recipe.category = category
		Bukkit.addRecipe(recipe)
		listOfCustomRecipes.add(NamespacedKeys.key(name))
	}

	private fun shapedItemStack(name: String, result: Material, shape1: String, shape2: String, shape3: String, category: CraftingBookCategory = CraftingBookCategory.MISC, vararg ingredients: Pair<Char, ItemStack>) {
		val recipe = ShapedRecipe(NamespacedKeys.key(name), ItemStack(result))
		recipe.shape(shape1, shape2, shape3)
		for ((key, ingredient) in ingredients) recipe.setIngredient(key, ingredient)
		recipe.category = category
		Bukkit.addRecipe(recipe)
		listOfCustomRecipes.add(NamespacedKeys.key(name))
	}

	private fun shapedCustomItem(name: String, result: Material, shape1: String, shape2: String, shape3: String, category: CraftingBookCategory = CraftingBookCategory.MISC, vararg ingredients: Pair<Char, IonRegistryKey<CustomItem, out CustomItem>>) {
		val recipe = ShapedRecipe(NamespacedKeys.key(name), ItemStack(result))
		recipe.shape(shape1, shape2, shape3)
		for ((key, ingredient) in ingredients) recipe.setIngredient(key, ingredient)
		recipe.category = category
		Bukkit.addRecipe(recipe)
		listOfCustomRecipes.add(NamespacedKeys.key(name))
	}

	private fun shaped(name: String, result: Material, category: CraftingBookCategory = CraftingBookCategory.MISC, execute: ShapedRecipe.() -> Unit) {
		val recipe = ShapedRecipe(NamespacedKeys.key(name), ItemStack(result))
		recipe.category = category
		execute(recipe)
		Bukkit.addRecipe(recipe)
		listOfCustomRecipes.add(NamespacedKeys.key(name))
	}

	private fun shaped(name: String, result: ItemStack, category: CraftingBookCategory = CraftingBookCategory.MISC, execute: ShapedRecipe.() -> Unit) {
		val recipe = ShapedRecipe(NamespacedKeys.key(name), result)
		recipe.category = category
		execute(recipe)
		Bukkit.addRecipe(recipe)
		listOfCustomRecipes.add(NamespacedKeys.key(name))
	}

	private fun shaped(name: String, result: IonRegistryKey<CustomItem, out CustomItem>, category: CraftingBookCategory = CraftingBookCategory.MISC, execute: ShapedRecipe.() -> Unit) {
		val recipe = ShapedRecipe(NamespacedKeys.key(name), result.getValue().constructItemStack())
		recipe.category = category
		execute(recipe)
		Bukkit.addRecipe(recipe)
		listOfCustomRecipes.add(NamespacedKeys.key(name))
	}

	private fun shapeless(name: String, result: ItemStack, category: CraftingBookCategory = CraftingBookCategory.MISC, execute: ShapelessRecipe.() -> Unit) {
		val recipe = ShapelessRecipe(NamespacedKeys.key(name), result)
		recipe.category = category
		execute(recipe)
		Bukkit.addRecipe(recipe)
		listOfCustomRecipes.add(NamespacedKeys.key(name))
	}

	private fun shapeless(name: String, result: IonRegistryKey<CustomItem, out CustomItem>, category: CraftingBookCategory = CraftingBookCategory.MISC, execute: ShapelessRecipe.() -> Unit) {
		val recipe = ShapelessRecipe(NamespacedKeys.key(name), result.getValue().constructItemStack())
		recipe.category = category
		execute(recipe)
		Bukkit.addRecipe(recipe)
		listOfCustomRecipes.add(NamespacedKeys.key(name))
	}

	private fun shapeless(name: String, result: ItemStack, category: CraftingBookCategory = CraftingBookCategory.MISC, vararg ingredients: Material) {
		val recipe = ShapelessRecipe(NamespacedKeys.key(name), result)
		for (ingreidient in ingredients) {
			recipe.addIngredient(MaterialChoice(ingreidient))
		}
		recipe.category = category
		Bukkit.addRecipe(recipe)
		listOfCustomRecipes.add(NamespacedKeys.key(name))
	}

	private fun shapeless(name: String, result: ItemStack, category: CraftingBookCategory = CraftingBookCategory.MISC, vararg ingredients: ItemStack) {
		val recipe = ShapelessRecipe(NamespacedKeys.key(name), result)
		for (ingreidient in ingredients) {
			recipe.addIngredient(ingreidient)
		}
		recipe.category = category
		Bukkit.addRecipe(recipe)
		listOfCustomRecipes.add(NamespacedKeys.key(name))
	}

	private fun shapeless(name: String, result: ItemStack, category: CraftingBookCategory = CraftingBookCategory.MISC, vararg ingredients: IonRegistryKey<CustomItem, out CustomItem>) {
		val recipe = ShapelessRecipe(NamespacedKeys.key(name), result)
		for (ingreidient in ingredients) {
			recipe.addIngredient(ingreidient.getValue().constructItemStack())
		}
		recipe.category = category
		Bukkit.addRecipe(recipe)
		listOfCustomRecipes.add(NamespacedKeys.key(name))
	}

	private fun ShapedRecipe.setIngredient(key: Char, customItem: IonRegistryKey<CustomItem, out CustomItem>) = setIngredient(key, customItem.getValue().constructItemStack())

	private fun materialBlockRecipes(blockItem: IonRegistryKey<CustomItem, out CustomItem>, ingotItem: IonRegistryKey<CustomItem, out CustomItem>) {
		shapeless(blockItem.key.lowercase(), blockItem.getValue().constructItemStack(), CraftingBookCategory.BUILDING) {
			addIngredient(ingotItem.getValue().constructItemStack(9))
		}

		shapeless(ingotItem.key.lowercase(), ingotItem.getValue().constructItemStack(9)) {
			addIngredient(blockItem.getValue().constructItemStack())
		}
	}
}
