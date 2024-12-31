package net.horizonsend.ion.server.miscellaneous.registrations

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemEnchantments
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.ALUMINUM_BLOCK
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.ALUMINUM_INGOT
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.ARMOR_MODIFICATION_ENVIRONMENT
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.ARMOR_MODIFICATION_NIGHT_VISION
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.ARMOR_MODIFICATION_PRESSURE_FIELD
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.ARMOR_MODIFICATION_ROCKET_BOOSTING
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.ARMOR_MODIFICATION_SHOCK_ABSORBING
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.ARMOR_MODIFICATION_SPEED_BOOSTING
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.AUTO_COMPOST
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.AUTO_REPLANT
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.AUTO_SMELT
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.BARGE_REACTOR_CORE
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.BATTERY_A
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.BATTERY_G
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.BATTERY_M
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.BATTLECRUISER_REACTOR_CORE
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.BLASTER_CANNON
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.BLASTER_PISTOL
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.BLASTER_RIFLE
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.BLASTER_SHOTGUN
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.BLASTER_SNIPER
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.CANNON_RECEIVER
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.CHETHERITE
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.CHETHERITE_BLOCK
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.CIRCUITRY
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.CIRCUIT_BOARD
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.CRATE_PLACER
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.CRUISER_REACTOR_CORE
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.DETONATOR
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.ENERGY_SWORD_BLUE
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.ENERGY_SWORD_GREEN
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.ENERGY_SWORD_ORANGE
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.ENERGY_SWORD_PINK
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.ENERGY_SWORD_PURPLE
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.ENERGY_SWORD_RED
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.ENERGY_SWORD_YELLOW
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.ENERGY_SWORD_BLACK
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.ENRICHED_URANIUM
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.ENRICHED_URANIUM_BLOCK
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.EXTENDED_BAR
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.FABRICATED_ASSEMBLY
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.FERTILIZER_DISPENSER
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.FORTUNE_1
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.FORTUNE_2
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.FORTUNE_3
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.FUEL_CELL
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.FUEL_CONTROL
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.FUEL_ROD_CORE
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.GAS_CANISTER_EMPTY
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.GAS_CANISTER_HYDROGEN
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.GAS_CANISTER_OXYGEN
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.GUN_BARREL
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.MOTHERBOARD
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.MULTIBLOCK_WORKBENCH
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.NETHERITE_CASING
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.PISTOL_RECEIVER
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.POWER_ARMOR_BOOTS
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.POWER_ARMOR_CHESTPLATE
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.POWER_ARMOR_HELMET
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.POWER_ARMOR_LEGGINGS
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.POWER_CAPACITY_25
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.POWER_CAPACITY_50
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.POWER_CHAINSAW_ADVANCED
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.POWER_CHAINSAW_BASIC
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.POWER_CHAINSAW_ENHANCED
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.POWER_DRILL_ADVANCED
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.POWER_DRILL_BASIC
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.POWER_DRILL_ENHANCED
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.POWER_HOE_ADVANCED
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.POWER_HOE_BASIC
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.POWER_HOE_ENHANCED
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.RANGE_1
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.RANGE_2
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.RANGE_3
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.RAW_ALUMINUM
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.RAW_ALUMINUM_BLOCK
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.RAW_TITANIUM
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.RAW_TITANIUM_BLOCK
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.RAW_URANIUM
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.RAW_URANIUM_BLOCK
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.REACTIVE_ASSEMBLY
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.REACTIVE_CHASSIS
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.REACTIVE_COMPONENT
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.REACTIVE_HOUSING
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.REACTIVE_MEMBRANE
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.REACTIVE_PLATING
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.REACTOR_CONTROL
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.REACTOR_FRAME
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.REINFORCED_FRAME
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.RIFLE_RECEIVER
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.SHOTGUN_RECEIVER
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.SILK_TOUCH_MOD
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.SMB_RECEIVER
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.SMOKE_GRENADE
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.SNIPER_RECEIVER
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.SPECIAL_MAGAZINE
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.STANDARD_MAGAZINE
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.STEEL_ASSEMBLY
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.STEEL_BLOCK
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.STEEL_CHASSIS
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.STEEL_INGOT
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.STEEL_MODULE
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.STEEL_PLATE
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.SUBMACHINE_BLASTER
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.SUPERCONDUCTOR
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.SUPERCONDUCTOR_BLOCK
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.SUPERCONDUCTOR_CORE
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.TITANIUM_BLOCK
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.TITANIUM_INGOT
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.UNCHARGED_SHELL
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.UNLOADED_ARSENAL_MISSILE
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.UNLOADED_SHELL
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.URANIUM
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.URANIUM_BLOCK
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.URANIUM_CORE
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.URANIUM_ROD
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.VEIN_MINER_25
import net.horizonsend.ion.server.features.custom.items.type.CustomBlockItem
import net.horizonsend.ion.server.features.custom.items.type.armor.PowerArmorItem
import net.horizonsend.ion.server.features.custom.items.type.tool.Battery
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ModificationItem
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.TERRACOTTA_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.WOOL_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Material.AMETHYST_SHARD
import org.bukkit.Material.BELL
import org.bukkit.Material.BLACKSTONE
import org.bukkit.Material.BLAST_FURNACE
import org.bukkit.Material.CHAINMAIL_HELMET
import org.bukkit.Material.CHERRY_LEAVES
import org.bukkit.Material.COAL
import org.bukkit.Material.COBWEB
import org.bukkit.Material.COMPOSTER
import org.bukkit.Material.COPPER_BLOCK
import org.bukkit.Material.COPPER_INGOT
import org.bukkit.Material.CRAFTING_TABLE
import org.bukkit.Material.DARK_PRISMARINE
import org.bukkit.Material.DIAMOND
import org.bukkit.Material.DIAMOND_BLOCK
import org.bukkit.Material.DISPENSER
import org.bukkit.Material.EMERALD
import org.bukkit.Material.EMERALD_BLOCK
import org.bukkit.Material.ENCHANTED_BOOK
import org.bukkit.Material.FEATHER
import org.bukkit.Material.FIREWORK_ROCKET
import org.bukkit.Material.GILDED_BLACKSTONE
import org.bukkit.Material.GLASS
import org.bukkit.Material.GLASS_PANE
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
import org.bukkit.Material.WITHER_ROSE
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.RecipeChoice.ExactChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe

@Suppress("unused") // Lots of helper functions which may not be used now but will be in the future
object Crafting : IonServerComponent() {
	override fun onEnable() {
		// Prismarine Bricks
		Bukkit.addRecipe(FurnaceRecipe(
			NamespacedKey(IonServer, "prismarine_bricks"),
			ItemStack(PRISMARINE_BRICKS),
			PRISMARINE,
			1f,
			200
		))
		// Bell
		shaped("bell", BELL) {
			shape("sos", "igi", "ggg")

			setIngredient('g', GOLD_BLOCK)
			setIngredient('i', IRON_BLOCK)
			setIngredient('o', OAK_LOG)
			setIngredient('s', STICK)
		}
		// Wool -> String
		for (material in WOOL_TYPES) shapeless(material.name.lowercase(), ItemStack(STRING, 4), material)
		shaped("saddle", SADDLE) {
			shape("lll", "t t")

			setIngredient('l', LEATHER)
			setIngredient('t', TRIPWIRE_HOOK)
		}
		shapedMaterial("nametag", NAME_TAG, "s", "t", "p", 's' to STRING, 't' to TRIPWIRE_HOOK, 'p' to PAPER)
		shapedMaterial("gilded_blackstone", GILDED_BLACKSTONE, "gbg", "bgb", "gbg", 'g' to GOLD_NUGGET, 'b' to BLACKSTONE)
		shapedMaterial("sniffer_egg", SNIFFER_EGG, "rdr", "ded", "rdr", 'r' to RED_TERRACOTTA, 'd' to DARK_PRISMARINE, 'e' to TURTLE_EGG)
		shapedMaterial("ochre_froglight", OCHRE_FROGLIGHT, " x ", "xlx", " x ", 'x' to HONEYCOMB, 'l' to SHROOMLIGHT)
		shapedMaterial("verdant_froglight", VERDANT_FROGLIGHT, " x ", "xlx", " x ", 'x' to SLIME_BALL, 'l' to SHROOMLIGHT)
		shapedMaterial("pearlescent_froglight", PEARLESCENT_FROGLIGHT, " x ", "xlx", " x ", 'x' to AMETHYST_SHARD, 'l' to SHROOMLIGHT)
		shaped("spore_blossom", SPORE_BLOSSOM) {
			shape(" a ", "ctc", " m ")

			setIngredient('a', AMETHYST_SHARD)
			setIngredient('t', PINK_TULIP)
			setIngredient('c', MOSS_CARPET)
			setIngredient('m', MOSS_BLOCK)
		}
		shapeless("prismarine_crystals", ItemStack(PRISMARINE_CRYSTALS, 4), SEA_LANTERN)
		shapeless("pink_petals", ItemStack(PINK_PETALS, 4), CHERRY_LEAVES)
		shapeless("nether_warts", ItemStack(NETHER_WART, 9), NETHER_WART_BLOCK)
		shapeless("honeycomb", ItemStack(HONEYCOMB, 9), HONEYCOMB_BLOCK)
		shapedMaterial("cobweb", COBWEB, "s s", " s ", "s s", 's' to STRING)
		shaped("Unloaded__Shell", UNLOADED_SHELL.constructItemStack()) {
			shape(" y ", " z ")

			setIngredient('y', LAPIS_LAZULI)
			setIngredient('z', ExactChoice(TITANIUM_INGOT.constructItemStack()))
		}
		shaped("Uncharged_Shell", UNCHARGED_SHELL.constructItemStack()) {
			shape(" y ", " z ")

			setIngredient('y', PRISMARINE_CRYSTALS)
			setIngredient('z', COPPER_INGOT)
		}
		shaped("Unloaded_Arsenal_Missile", UNLOADED_ARSENAL_MISSILE.constructItemStack()) {
			shape("aba", "mum", "hlo")

			setIngredient('a', ExactChoice(REACTIVE_HOUSING.constructItemStack()))
			setIngredient('b', ExactChoice(STEEL_PLATE.constructItemStack()))
			setIngredient('m', ExactChoice(CIRCUITRY.constructItemStack()))
			setIngredient('u', ExactChoice(URANIUM_ROD.constructItemStack()))
			setIngredient('h', ExactChoice(GAS_CANISTER_HYDROGEN.constructItemStack()))
			setIngredient('l', LAPIS_BLOCK)
			setIngredient('o', ExactChoice(GAS_CANISTER_OXYGEN.constructItemStack()))
		}
		shaped("blaster_barrel", GUN_BARREL.constructItemStack()) {
			shape("tct", "ppp", "tct")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.constructItemStack()))
			setIngredient('c', COPPER_INGOT)
			setIngredient('p', PRISMARINE_CRYSTALS)
		}
		shaped("pistol_receiver", PISTOL_RECEIVER.constructItemStack()) {
			shape("irt")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.constructItemStack()))
			setIngredient('r', REDSTONE_BLOCK)
			setIngredient('i', IRON_TRAPDOOR)
		}
		shaped("rifle_receiver", RIFLE_RECEIVER.constructItemStack()) {
			shape(" t ", "igt", " t ")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.constructItemStack()))
			setIngredient('g', GOLD_BLOCK)
			setIngredient('i', IRON_TRAPDOOR)
		}
		shaped("smb_receiver", SMB_RECEIVER.constructItemStack()) {
			shape(" t ", "id ", " t ")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.constructItemStack()))
			setIngredient('d', DIAMOND_BLOCK)
			setIngredient('i', IRON_TRAPDOOR)
		}
		shaped("sniper_receiver", SNIPER_RECEIVER.constructItemStack()) {
			shape(" t ", "ieb", " t ")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.constructItemStack()))
			setIngredient('e', EMERALD_BLOCK)
			setIngredient('b', ExactChoice(TITANIUM_BLOCK.constructItemStack()))
			setIngredient('i', IRON_TRAPDOOR)
		}
		shaped("shotgun_receiver", SHOTGUN_RECEIVER.constructItemStack()) {
			shape("   ", "icb", " t ")

			setIngredient('t', TITANIUM_INGOT)
			setIngredient('c', COPPER_BLOCK)
			setIngredient('b', TITANIUM_BLOCK)
			setIngredient('i', IRON_TRAPDOOR)
		}
		shaped("cannon_receiver", CANNON_RECEIVER.constructItemStack()) {
			shape("   ", " ba", "g  ")

			setIngredient('a', ALUMINUM_INGOT)
			setIngredient('b', ALUMINUM_BLOCK)
			setIngredient('g', GOLD_INGOT)
		}
		shaped("pistol", BLASTER_PISTOL.constructItemStack()) {
			shape("   ", "apb", "c  ")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
			setIngredient('p', PISTOL_RECEIVER.constructItemStack())
			setIngredient('b', GUN_BARREL.constructItemStack())
			setIngredient('c', CIRCUITRY.constructItemStack())

		}
		shaped("rifle", BLASTER_RIFLE.constructItemStack()) {
			shape("   ", "apb", "ac ")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
			setIngredient('p', RIFLE_RECEIVER.constructItemStack())
			setIngredient('b', GUN_BARREL.constructItemStack())
			setIngredient('c', CIRCUITRY.constructItemStack())

		}
		shaped("submachine_blaster", SUBMACHINE_BLASTER.constructItemStack()) {
			shape("   ", "apb", "ac ")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
			setIngredient('p', SMB_RECEIVER.constructItemStack())
			setIngredient('b', GUN_BARREL.constructItemStack())
			setIngredient('c', CIRCUITRY.constructItemStack())

		}
		shaped("sniper", BLASTER_SNIPER.constructItemStack()) {
			shape(" g ", "apb", "ac ")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
			setIngredient('p', SNIPER_RECEIVER.constructItemStack())
			setIngredient('b', GUN_BARREL.constructItemStack())
			setIngredient('c', CIRCUITRY.constructItemStack())
			setIngredient('g', GLASS)

		}
		shaped("shotgun", BLASTER_SHOTGUN.constructItemStack()) {
			shape("  b", "apb", "ac ")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
			setIngredient('p', SHOTGUN_RECEIVER.constructItemStack())
			setIngredient('b', GUN_BARREL.constructItemStack())
			setIngredient('c', CIRCUITRY.constructItemStack())

		}
		shaped("cannon", BLASTER_CANNON.constructItemStack()) {
			shape(" a ", " cb", "p  ")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
			setIngredient('p', CANNON_RECEIVER.constructItemStack())
			setIngredient('b', GUN_BARREL.constructItemStack())
			setIngredient('c', CIRCUITRY.constructItemStack())

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
		}
		shaped("power_drill_basic", POWER_DRILL_BASIC.constructItemStack()) {
			shape("i  ", " bt", " ts")

			setIngredient('i', ExactChoice(ItemStack(IRON_INGOT)))
			setIngredient('b', ExactChoice(BATTERY_M.constructItemStack()))
			setIngredient('t', ExactChoice(TITANIUM_INGOT.constructItemStack()))
			setIngredient('s', STICK)

		}
		shaped("power_drill_enhanced", POWER_DRILL_ENHANCED.constructItemStack()) {
			shape("ii ", "idc", " ts")

			setIngredient('i', ExactChoice(TITANIUM_BLOCK.constructItemStack()))
			setIngredient('d', ExactChoice(POWER_DRILL_BASIC.constructItemStack()))
			setIngredient('c', ExactChoice(CIRCUITRY.constructItemStack()))
			setIngredient('t', ExactChoice(URANIUM_BLOCK.constructItemStack()))
			setIngredient('s', ExactChoice(BATTERY_G.constructItemStack()))

		}
		shaped("power_drill_advanced", POWER_DRILL_ADVANCED.constructItemStack()) {
			shape("ii ", "idc", " ts")

			setIngredient('i', ExactChoice(STEEL_PLATE.constructItemStack()))
			setIngredient('d', ExactChoice(POWER_DRILL_ENHANCED.constructItemStack()))
			setIngredient('c', ExactChoice(CIRCUIT_BOARD.constructItemStack()))
			setIngredient('t', ExactChoice(SUPERCONDUCTOR.constructItemStack()))
			setIngredient('s', ExactChoice(STEEL_CHASSIS.constructItemStack()))

		}
		shaped("power_chainsaw_basic", POWER_CHAINSAW_BASIC.constructItemStack()) {
			shape("ii ", "idc", " cs")

			setIngredient('i', ExactChoice(ItemStack(IRON_INGOT)))
			setIngredient('d', ExactChoice(BATTERY_M.constructItemStack()))
			setIngredient('c', ExactChoice(TITANIUM_INGOT.constructItemStack()))
			setIngredient('s', STICK)

		}
		shaped("power_chainsaw_enhanced", POWER_CHAINSAW_ENHANCED.constructItemStack()) {
			shape("ii ", "idc", " us")

			setIngredient('i', ExactChoice(TITANIUM_BLOCK.constructItemStack()))
			setIngredient('d', ExactChoice(POWER_CHAINSAW_BASIC.constructItemStack()))
			setIngredient('c', ExactChoice(CIRCUITRY.constructItemStack()))
			setIngredient('u', ExactChoice(URANIUM_BLOCK.constructItemStack()))
			setIngredient('s', ExactChoice(BATTERY_G.constructItemStack()))
		}
		shaped("power_chainsaw_advanced", POWER_CHAINSAW_ADVANCED.constructItemStack()) {
			shape("pb ", "bdc", " ts")

			setIngredient('p', ExactChoice(STEEL_PLATE.constructItemStack()))
			setIngredient('b', ExactChoice(STEEL_BLOCK.constructItemStack()))
			setIngredient('d', ExactChoice(POWER_CHAINSAW_ENHANCED.constructItemStack()))
			setIngredient('c', ExactChoice(CIRCUIT_BOARD.constructItemStack()))
			setIngredient('t', ExactChoice(SUPERCONDUCTOR.constructItemStack()))
			setIngredient('s', ExactChoice(STEEL_CHASSIS.constructItemStack()))
		}
		shaped("power_hoe_basic", POWER_HOE_BASIC.constructItemStack()) {
			shape(" ib", " si", "cc ")

			setIngredient('b', ExactChoice(BATTERY_M.constructItemStack()))
			setIngredient('i', COPPER_INGOT)
			setIngredient('c', ExactChoice(TITANIUM_INGOT.constructItemStack()))
			setIngredient('s', STICK)
		}
		shaped("power_hoe_enhanced", POWER_HOE_ENHANCED.constructItemStack()) {
			shape(" us", " dc", "ii ")

			setIngredient('d', ExactChoice(POWER_HOE_BASIC.constructItemStack()))
			setIngredient('i', ExactChoice(TITANIUM_BLOCK.constructItemStack()))
			setIngredient('c', ExactChoice(CIRCUITRY.constructItemStack()))
			setIngredient('u', ExactChoice(URANIUM_BLOCK.constructItemStack()))
			setIngredient('s', ExactChoice(BATTERY_G.constructItemStack()))
		}
		shaped("power_hoe_advanced", POWER_HOE_ADVANCED.constructItemStack()) {
			shape(" tu", " dc", "ss ")

			setIngredient('d', ExactChoice(POWER_HOE_ENHANCED.constructItemStack()))
			setIngredient('s', ExactChoice(STEEL_BLOCK.constructItemStack()))
			setIngredient('c', ExactChoice(CIRCUIT_BOARD.constructItemStack()))
			setIngredient('t', ExactChoice(SUPERCONDUCTOR.constructItemStack()))
			setIngredient('u', ExactChoice(STEEL_CHASSIS.constructItemStack()))
		}
		shaped("crate_placer", CRATE_PLACER.constructItemStack()) {
			shape(" s ", " cd", "t  ")

			setIngredient('s', ExactChoice(STEEL_INGOT.constructItemStack()))
			setIngredient('t', GAS_CANISTER_EMPTY.constructItemStack())
			setIngredient('d', DIAMOND)
			setIngredient('c', CIRCUITRY.constructItemStack())
		}
		shaped("circuitry_1", CIRCUITRY.constructItemStack()) {
			shape("qdq", "arg", "ccc")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
			setIngredient('c', COPPER_INGOT)
			setIngredient('q', QUARTZ)
			setIngredient('g', GOLD_INGOT)
			setIngredient('d', GREEN_DYE)
			setIngredient('r', REDSTONE)
		}
		shaped("circuitry_2", CIRCUITRY.constructItemStack()) {
			shape("qdq", "gra", "ccc")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
			setIngredient('c', COPPER_INGOT)
			setIngredient('q', QUARTZ)
			setIngredient('g', GOLD_INGOT)
			setIngredient('d', GREEN_DYE)
			setIngredient('r', REDSTONE)
		}
		shaped("standard_magazine", STANDARD_MAGAZINE.constructItemStack()) {
			shape("   ", "rlr", "ttt")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.constructItemStack()))
			setIngredient('l', LAPIS_BLOCK)
			setIngredient('r', REDSTONE)

		}
		shaped("special_magazine", SPECIAL_MAGAZINE.constructItemStack()) {
			shape("   ", "rer", "ttt")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.constructItemStack()))
			setIngredient('e', EMERALD_BLOCK)
			setIngredient('r', REDSTONE)

		}
		shaped("empty_gas_canister", GAS_CANISTER_EMPTY.constructItemStack()) {
			shape(" i ", "igi", " i ")

			setIngredient('i', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
			setIngredient('g', GLASS_PANE)

		}
		shaped("detonator", DETONATOR.constructItemStack()) {
			shape(" r ", "tut", " t ")

			setIngredient('r', REDSTONE)
			setIngredient('t', TITANIUM_INGOT.constructItemStack())
			setIngredient('u', URANIUM.constructItemStack())
		}
		shaped("smokeGrenade", SMOKE_GRENADE.constructItemStack()) {
			shape(" i ", "tct", " t ")

			setIngredient('i', IRON_INGOT)
			setIngredient('t', TITANIUM_INGOT.constructItemStack())
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

		shapeless("steelPlate", STEEL_PLATE.constructItemStack(), STEEL_BLOCK.constructItemStack(9))
		shapeless("steelModule", STEEL_MODULE.constructItemStack(), STEEL_CHASSIS.constructItemStack(9))
		shapeless("steelAssembly", STEEL_ASSEMBLY.constructItemStack(), STEEL_MODULE.constructItemStack(4))
		shapeless("reactorFrame", REACTOR_FRAME.constructItemStack(), REINFORCED_FRAME.constructItemStack(4))
		shapeless("uraniumCore", URANIUM_CORE.constructItemStack(), ENRICHED_URANIUM_BLOCK.constructItemStack(9))
		shapeless("fuelRodCore", FUEL_ROD_CORE.constructItemStack(), URANIUM_ROD.constructItemStack(9))
		shapeless("fuelControl", FUEL_CONTROL.constructItemStack(), FUEL_CELL.constructItemStack(9))
		shapeless("melonToSlices", ItemStack(Material.MELON_SLICE).asQuantity(4), MELON)

		shaped("reactiveComponent", REACTIVE_HOUSING.constructItemStack()) {
			shape("xxx", "yyy", "xxx")

			setIngredient('x', RecipeChoice.MaterialChoice(*TERRACOTTA_TYPES.toTypedArray()) )
			setIngredient('y', SPONGE)
		}
		shaped("netheriteCasing", NETHERITE_CASING.constructItemStack()) {
			shape("xvx", "xyx", "xvx")

			setIngredient('x', NETHERITE_BLOCK)
			setIngredient('y', STEEL_PLATE.constructItemStack())
			setIngredient('v', REACTIVE_HOUSING.constructItemStack())
		}
		shaped("reactiveHousing", REACTIVE_COMPONENT.constructItemStack()) {
			shape("xxx", "yyy", "xxx")

			setIngredient('x', REDSTONE_BLOCK )
			setIngredient('y', COPPER_BLOCK)
		}
		shapeless("reactivePlating", result = REACTIVE_PLATING.constructItemStack(), REACTIVE_COMPONENT, REACTIVE_HOUSING)
		shapeless("reactiveMembrane", result = REACTIVE_MEMBRANE.constructItemStack(), REACTIVE_CHASSIS.constructItemStack(7), CIRCUITRY.constructItemStack(), ENRICHED_URANIUM.constructItemStack())
		shapeless("reactiveAssembly", REACTIVE_ASSEMBLY.constructItemStack(), REACTIVE_MEMBRANE.constructItemStack(9))
		shapeless("circuitBoard", MOTHERBOARD.constructItemStack(), CIRCUIT_BOARD.constructItemStack(9))
		shapeless("reactorControl", REACTOR_CONTROL.constructItemStack(), FABRICATED_ASSEMBLY.constructItemStack(6), MOTHERBOARD.constructItemStack(3))
		materialBlockRecipes(SUPERCONDUCTOR_BLOCK, SUPERCONDUCTOR)
		shapeless("superconductorCore", SUPERCONDUCTOR_CORE.constructItemStack(), SUPERCONDUCTOR_BLOCK.constructItemStack(), MOTHERBOARD.constructItemStack(4))
		shaped("bcreactorCore", BATTLECRUISER_REACTOR_CORE.constructItemStack()) {
			shape("wxw", "yzy", "wxw")

			setIngredient('w', REACTOR_FRAME.constructItemStack())
			setIngredient('x', REACTOR_CONTROL.constructItemStack())
			setIngredient('y', FUEL_CONTROL.constructItemStack())
			setIngredient('z', SUPERCONDUCTOR_CORE.constructItemStack())
		}
		shaped("bargereactorCore", BARGE_REACTOR_CORE.constructItemStack()) {
			shape("wxw", "zzz", "vyv")

			setIngredient('w', REACTOR_FRAME.constructItemStack())
			setIngredient('x', REACTOR_CONTROL.constructItemStack())
			setIngredient('y', FUEL_CONTROL.constructItemStack())
			setIngredient('z', SUPERCONDUCTOR.constructItemStack())
			setIngredient('v', REINFORCED_FRAME.constructItemStack())
		}
		shaped("cruiserreactorCore", CRUISER_REACTOR_CORE.constructItemStack()) {
			shape("wxw", "wyw", "wzw")

			setIngredient('w', REINFORCED_FRAME.constructItemStack())
			setIngredient('x', REACTOR_CONTROL.constructItemStack())
			setIngredient('y', SUPERCONDUCTOR_CORE.constructItemStack())
			setIngredient('z', FUEL_CONTROL.constructItemStack())
		}

		shaped("multiblock_workbench", MULTIBLOCK_WORKBENCH.constructItemStack()) {
			shape("i", "c")

			setIngredient('i', IRON_BLOCK)
			setIngredient('c', CRAFTING_TABLE)
		}

		// Tool Mods start
		shaped("silk_touch_modifier", SILK_TOUCH_MOD.constructItemStack()) {
			shape("gbg", "tst", "ctc")

			setIngredient('g', RAW_GOLD)
			setIngredient('b', TITANIUM_BLOCK.constructItemStack())
			setIngredient('t', TITANIUM_INGOT.constructItemStack())
			setIngredient('s', ItemStack(ENCHANTED_BOOK).updateData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments(mutableMapOf(Enchantment.SILK_TOUCH to 1), true)))
			setIngredient('c', CIRCUIT_BOARD.constructItemStack())
		}

		shaped("fortune_1_touch_modifier", FORTUNE_1.constructItemStack()) {
			shape("dgd", "csc", "dgd")

			setIngredient('d', DIAMOND)
			setIngredient('g', GOLD_BLOCK)
			setIngredient('c', REACTIVE_COMPONENT.constructItemStack())
			setIngredient('s', SUPERCONDUCTOR.constructItemStack())
		}
		shaped("fortune_2_touch_modifier", FORTUNE_2.constructItemStack()) {
			shape("dgd", "csc", "dgd")

			setIngredient('d', STEEL_PLATE.constructItemStack())
			setIngredient('g', URANIUM_BLOCK.constructItemStack())
			setIngredient('c', REACTIVE_PLATING.constructItemStack())
			setIngredient('s', FORTUNE_1.constructItemStack())
		}
		shaped("fortune_3_touch_modifier", FORTUNE_3.constructItemStack()) {
			shape("dgd", "csc", "dgd")

			setIngredient('d', STEEL_ASSEMBLY.constructItemStack())
			setIngredient('g', ENRICHED_URANIUM_BLOCK.constructItemStack())
			setIngredient('c', REACTIVE_ASSEMBLY.constructItemStack())
			setIngredient('s', FORTUNE_2.constructItemStack())
		}
		shaped("power_capacity_25_modifier", POWER_CAPACITY_25.constructItemStack()) {
			shape("sbs", "brb", "scs")

			setIngredient('s', STEEL_INGOT.constructItemStack())
			setIngredient('b', BATTERY_M.constructItemStack())
			setIngredient('r', REDSTONE_BLOCK)
			setIngredient('c', CIRCUITRY.constructItemStack())
		}
		shaped("power_capacity_50_modifier", POWER_CAPACITY_50.constructItemStack()) {
			shape("sbs", "brb", "scs")

			setIngredient('s', STEEL_PLATE.constructItemStack())
			setIngredient('b', BATTERY_M.constructItemStack())
			setIngredient('r', REDSTONE_BLOCK)
			setIngredient('c', CIRCUIT_BOARD.constructItemStack())
		}
		shaped("auto_smelt_modifier", AUTO_SMELT.constructItemStack()) {
			shape("srs", "bfb", "scs")

			setIngredient('s', STEEL_PLATE.constructItemStack())
			setIngredient('b', GOLD_BLOCK)
			setIngredient('f', BLAST_FURNACE)
			setIngredient('r', REDSTONE_BLOCK)
			setIngredient('c', CIRCUIT_BOARD.constructItemStack())
		}
		shaped("auto_compost_modifier", AUTO_COMPOST.constructItemStack()) {
			shape("tit", "tct", "trt")

			setIngredient('t', TITANIUM_INGOT.constructItemStack())
			setIngredient('i', IRON_INGOT)
			setIngredient('c', COMPOSTER)
			setIngredient('r', REDSTONE_BLOCK)
		}
		shaped("auto_replant_modifier", AUTO_REPLANT.constructItemStack()) {
			shape("ipi", "tct", "iri")

			setIngredient('i', IRON_INGOT)
			setIngredient('p', PISTON)
			setIngredient('t', TITANIUM_INGOT.constructItemStack())
			setIngredient('c', DISPENSER)
			setIngredient('r', REDSTONE_BLOCK)
		}
		shaped("auto_fertilizer_modifier", FERTILIZER_DISPENSER.constructItemStack()) {
			shape("ipi", "tct", "iri")

			setIngredient('i', IRON_INGOT)
			setIngredient('p', HOPPER)
			setIngredient('t', TITANIUM_INGOT.constructItemStack())
			setIngredient('c', DISPENSER)
			setIngredient('r', REDSTONE_BLOCK)
		}
		shaped("extended_bar_modifier", EXTENDED_BAR.constructItemStack()) {
			shape("st ", "tct", " ts")

			setIngredient('s', STEEL_PLATE.constructItemStack())
			setIngredient('t', TITANIUM_INGOT.constructItemStack())
			setIngredient('c', STEEL_CHASSIS.constructItemStack())
		}
		shaped("aoe_1_modifier", RANGE_1.constructItemStack()) {
			shape("ipi", "prp", "ipi")

			setIngredient('i', IRON_BLOCK)
			setIngredient('p', PISTON)
			setIngredient('r', REDSTONE_BLOCK)
		}
		shaped("aoe_2_modifier", RANGE_2.constructItemStack()) {
			shape("ipi", "prp", "ipi")

			setIngredient('i', ExactChoice(STEEL_BLOCK.constructItemStack()))
			setIngredient('p', PISTON)
			setIngredient('r', ExactChoice(RANGE_1.constructItemStack()))
		}

		shaped("aoe_3_modifier", RANGE_3.constructItemStack()) {
			shape("ipi", "prp", "ipi")

			setIngredient('i', ExactChoice(STEEL_BLOCK.constructItemStack()))
			setIngredient('p', ExactChoice(STEEL_PLATE.constructItemStack()))
			setIngredient('r', ExactChoice(RANGE_2.constructItemStack()))
		}
		shaped("vein_miner_modifier", VEIN_MINER_25.constructItemStack()) {
			shape("ipi", "prp", "ipi")

			setIngredient('i', ExactChoice(ALUMINUM_BLOCK.constructItemStack()))
			setIngredient('p', OBSERVER)
			setIngredient('r', RANGE_1.constructItemStack())
		}


		fun registerBatteryRecipe(battery: Battery, material: Material) = shaped(battery.identifier.lowercase(), battery.constructItemStack()) {
			shape("aba", "aba", "aba")
			setIngredient('a', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
			setIngredient('b', material)
		}
		registerBatteryRecipe(BATTERY_A, GLOWSTONE_DUST)
		registerBatteryRecipe(BATTERY_M, REDSTONE)
		registerBatteryRecipe(BATTERY_G, SEA_LANTERN)

		fun registerArmorRecipe(result: PowerArmorItem, vararg shape: String) = shaped(result.identifier.lowercase(), result.constructItemStack()) {
			shape(*shape)
			setIngredient('*', TITANIUM_INGOT)
			setIngredient('b', BATTERY_G)
		}
		registerArmorRecipe(POWER_ARMOR_HELMET, "*b*", "* *")
		registerArmorRecipe(POWER_ARMOR_CHESTPLATE, "* *", "*b*", "***")
		registerArmorRecipe(POWER_ARMOR_LEGGINGS, "*b*", "* *", "* *")
		registerArmorRecipe(POWER_ARMOR_BOOTS, "* *", "*b*")

		fun registerPowerArmorModule(result: ModificationItem, center: RecipeChoice) = shaped(result.identifier.lowercase(), result.constructItemStack()) {
			shape("aga", "g*g", "aga")
			setIngredient('a', ALUMINUM_INGOT)
			setIngredient('g', GLASS_PANE)
			setIngredient('*', BATTERY_G)
		}
		registerPowerArmorModule(ARMOR_MODIFICATION_SHOCK_ABSORBING, ExactChoice(TITANIUM_INGOT.constructItemStack()))
		registerPowerArmorModule(ARMOR_MODIFICATION_SPEED_BOOSTING, RecipeChoice.MaterialChoice(FEATHER))
		registerPowerArmorModule(ARMOR_MODIFICATION_ROCKET_BOOSTING, RecipeChoice.MaterialChoice(FIREWORK_ROCKET))
		registerPowerArmorModule(ARMOR_MODIFICATION_NIGHT_VISION, RecipeChoice.MaterialChoice(SPIDER_EYE))
		registerPowerArmorModule(ARMOR_MODIFICATION_ENVIRONMENT, RecipeChoice.MaterialChoice(CHAINMAIL_HELMET))
		registerPowerArmorModule(ARMOR_MODIFICATION_PRESSURE_FIELD, RecipeChoice.ExactChoice(GAS_CANISTER_EMPTY.constructItemStack()))

		fun registerSwordRecipes(sword: CustomItem, choice: RecipeChoice) = shaped(sword.identifier.lowercase(), sword) {
			shape("aga", "a*a", "ata")
			setIngredient('a', ALUMINUM_INGOT)
			setIngredient('g', GLASS_PANE)
			setIngredient('*', choice)
			setIngredient('t', TITANIUM_INGOT)
		}

		registerSwordRecipes(ENERGY_SWORD_BLUE, RecipeChoice.MaterialChoice(DIAMOND))
		registerSwordRecipes(ENERGY_SWORD_RED, RecipeChoice.MaterialChoice(REDSTONE))
		registerSwordRecipes(ENERGY_SWORD_YELLOW, RecipeChoice.MaterialChoice(COAL))
		registerSwordRecipes(ENERGY_SWORD_GREEN, RecipeChoice.MaterialChoice(EMERALD))
		registerSwordRecipes(ENERGY_SWORD_PURPLE, ExactChoice(CHETHERITE.constructItemStack()))
		registerSwordRecipes(ENERGY_SWORD_ORANGE, RecipeChoice.MaterialChoice(COPPER_INGOT))
		registerSwordRecipes(ENERGY_SWORD_PINK, RecipeChoice.MaterialChoice(PINK_TULIP))
		registerSwordRecipes(ENERGY_SWORD_BLACK, RecipeChoice.MaterialChoice(WITHER_ROSE))
	}

	// Different names due to signature problems from type erasure
	private fun shapedMaterial(name: String, result: Material, shape1: String, shape2: String, shape3: String, vararg ingredients: Pair<Char, Material>) {
		val recipe = ShapedRecipe(NamespacedKeys.key(name), ItemStack(result))
		recipe.shape(shape1, shape2, shape3)
		for ((key, ingredient) in ingredients) recipe.setIngredient(key, ingredient)
		Bukkit.addRecipe(recipe)
	}

	private fun shapedItemStack(name: String, result: Material, shape1: String, shape2: String, shape3: String, vararg ingredients: Pair<Char, ItemStack>) {
		val recipe = ShapedRecipe(NamespacedKeys.key(name), ItemStack(result))
		recipe.shape(shape1, shape2, shape3)
		for ((key, ingredient) in ingredients) recipe.setIngredient(key, ingredient)
		Bukkit.addRecipe(recipe)
	}

	private fun shapedCustomItem(name: String, result: Material, shape1: String, shape2: String, shape3: String, vararg ingredients: Pair<Char, CustomItem>) {
		val recipe = ShapedRecipe(NamespacedKeys.key(name), ItemStack(result))
		recipe.shape(shape1, shape2, shape3)
		for ((key, ingredient) in ingredients) recipe.setIngredient(key, ingredient)
		Bukkit.addRecipe(recipe)
	}

	private fun shaped(name: String, result: Material, execute: ShapedRecipe.() -> Unit) {
		val recipe = ShapedRecipe(NamespacedKeys.key(name), ItemStack(result))
		execute(recipe)
		Bukkit.addRecipe(recipe)
	}

	private fun shaped(name: String, result: ItemStack, execute: ShapedRecipe.() -> Unit) {
		val recipe = ShapedRecipe(NamespacedKeys.key(name), result)
		execute(recipe)
		Bukkit.addRecipe(recipe)
	}

	private fun shaped(name: String, result: CustomItem, execute: ShapedRecipe.() -> Unit) {
		val recipe = ShapedRecipe(NamespacedKeys.key(name), result.constructItemStack())
		execute(recipe)
		Bukkit.addRecipe(recipe)
	}

	private fun shapeless(name: String, result: ItemStack, execute: ShapelessRecipe.() -> Unit) {
		val recipe = ShapelessRecipe(NamespacedKeys.key(name), result)
		execute(recipe)
		Bukkit.addRecipe(recipe)
	}

	private fun shapeless(name: String, result: CustomItem, execute: ShapelessRecipe.() -> Unit) {
		val recipe = ShapelessRecipe(NamespacedKeys.key(name), result.constructItemStack())
		execute(recipe)
		Bukkit.addRecipe(recipe)
	}

	private fun shapeless(name: String, result: ItemStack, vararg ingredients: Material) {
		val recipe = ShapelessRecipe(NamespacedKeys.key(name), result)
		for (ingreidient in ingredients) {
			recipe.addIngredient(RecipeChoice.MaterialChoice(ingreidient))
		}
		Bukkit.addRecipe(recipe)
	}

	private fun shapeless(name: String, result: ItemStack, vararg ingredients: ItemStack) {
		val recipe = ShapelessRecipe(NamespacedKeys.key(name), result)
		for (ingreidient in ingredients) {
			recipe.addIngredient(ingreidient)
		}
		Bukkit.addRecipe(recipe)
	}

	private fun shapeless(name: String, result: ItemStack, vararg ingredients: CustomItem) {
		val recipe = ShapelessRecipe(NamespacedKeys.key(name), result)
		for (ingreidient in ingredients) {
			recipe.addIngredient(ingreidient.constructItemStack())
		}
		Bukkit.addRecipe(recipe)
	}

	private fun ShapedRecipe.setIngredient(key: Char, customItem: CustomItem) = setIngredient(key, customItem.constructItemStack())

	fun materialBlockRecipes(blockItem: CustomBlockItem, ingotItem: CustomItem) {
		shapeless(blockItem.identifier.lowercase(), blockItem.constructItemStack()) {
			addIngredient(ingotItem.constructItemStack(9))
		}

		shapeless(ingotItem.identifier.lowercase(), ingotItem.constructItemStack(9)) {
			addIngredient(blockItem.constructItemStack())
		}
	}
}
