package net.horizonsend.ion.server.miscellaneous.registrations

//import net.horizonsend.ion.server.features.custom.items.CustomItems.CRUDE_FUEL
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.custom.items.CustomItems.ALUMINUM_BLOCK
import net.horizonsend.ion.server.features.custom.items.CustomItems.ALUMINUM_INGOT
import net.horizonsend.ion.server.features.custom.items.CustomItems.AUTO_REPLANT
import net.horizonsend.ion.server.features.custom.items.CustomItems.BARGE_REACTOR_CORE
import net.horizonsend.ion.server.features.custom.items.CustomItems.BATTLECRUISER_REACTOR_CORE
import net.horizonsend.ion.server.features.custom.items.CustomItems.CANNON_RECEIVER
import net.horizonsend.ion.server.features.custom.items.CustomItems.CHETHERITE
import net.horizonsend.ion.server.features.custom.items.CustomItems.CHETHERITE_BLOCK
import net.horizonsend.ion.server.features.custom.items.CustomItems.CIRCUITRY
import net.horizonsend.ion.server.features.custom.items.CustomItems.CIRCUIT_BOARD
import net.horizonsend.ion.server.features.custom.items.CustomItems.CRUISER_REACTOR_CORE
import net.horizonsend.ion.server.features.custom.items.CustomItems.DETONATOR
import net.horizonsend.ion.server.features.custom.items.CustomItems.ENRICHED_URANIUM
import net.horizonsend.ion.server.features.custom.items.CustomItems.ENRICHED_URANIUM_BLOCK
import net.horizonsend.ion.server.features.custom.items.CustomItems.EXTENDED_BAR
import net.horizonsend.ion.server.features.custom.items.CustomItems.FABRICATED_ASSEMBLY
import net.horizonsend.ion.server.features.custom.items.CustomItems.FERTILIZER_DISPENSER
import net.horizonsend.ion.server.features.custom.items.CustomItems.FORTUNE_1
import net.horizonsend.ion.server.features.custom.items.CustomItems.FORTUNE_2
import net.horizonsend.ion.server.features.custom.items.CustomItems.FORTUNE_3
import net.horizonsend.ion.server.features.custom.items.CustomItems.FUEL_CELL
import net.horizonsend.ion.server.features.custom.items.CustomItems.FUEL_CONTROL
import net.horizonsend.ion.server.features.custom.items.CustomItems.FUEL_ROD_CORE
import net.horizonsend.ion.server.features.custom.items.CustomItems.GAS_CANISTER_EMPTY
import net.horizonsend.ion.server.features.custom.items.CustomItems.GAS_CANISTER_HYDROGEN
import net.horizonsend.ion.server.features.custom.items.CustomItems.GAS_CANISTER_OXYGEN
import net.horizonsend.ion.server.features.custom.items.CustomItems.GUN_BARREL
import net.horizonsend.ion.server.features.custom.items.CustomItems.MOTHERBOARD
import net.horizonsend.ion.server.features.custom.items.CustomItems.NETHERITE_CASING
import net.horizonsend.ion.server.features.custom.items.CustomItems.PISTOL
import net.horizonsend.ion.server.features.custom.items.CustomItems.PISTOL_RECEIVER
import net.horizonsend.ion.server.features.custom.items.CustomItems.POWER_CAPACITY_25
import net.horizonsend.ion.server.features.custom.items.CustomItems.POWER_CAPACITY_50
import net.horizonsend.ion.server.features.custom.items.CustomItems.POWER_CHAINSAW_ADVANCED
import net.horizonsend.ion.server.features.custom.items.CustomItems.POWER_CHAINSAW_BASIC
import net.horizonsend.ion.server.features.custom.items.CustomItems.POWER_CHAINSAW_ENHANCED
import net.horizonsend.ion.server.features.custom.items.CustomItems.POWER_DRILL_ADVANCED
import net.horizonsend.ion.server.features.custom.items.CustomItems.POWER_DRILL_BASIC
import net.horizonsend.ion.server.features.custom.items.CustomItems.POWER_DRILL_ENHANCED
import net.horizonsend.ion.server.features.custom.items.CustomItems.POWER_HOE_ADVANCED
import net.horizonsend.ion.server.features.custom.items.CustomItems.POWER_HOE_BASIC
import net.horizonsend.ion.server.features.custom.items.CustomItems.POWER_HOE_ENHANCED
import net.horizonsend.ion.server.features.custom.items.CustomItems.RANGE_1
import net.horizonsend.ion.server.features.custom.items.CustomItems.RANGE_2
import net.horizonsend.ion.server.features.custom.items.CustomItems.RAW_ALUMINUM
import net.horizonsend.ion.server.features.custom.items.CustomItems.RAW_ALUMINUM_BLOCK
import net.horizonsend.ion.server.features.custom.items.CustomItems.RAW_TITANIUM
import net.horizonsend.ion.server.features.custom.items.CustomItems.RAW_TITANIUM_BLOCK
import net.horizonsend.ion.server.features.custom.items.CustomItems.RAW_URANIUM
import net.horizonsend.ion.server.features.custom.items.CustomItems.RAW_URANIUM_BLOCK
import net.horizonsend.ion.server.features.custom.items.CustomItems.REACTIVE_ASSEMBLY
import net.horizonsend.ion.server.features.custom.items.CustomItems.REACTIVE_CHASSIS
import net.horizonsend.ion.server.features.custom.items.CustomItems.REACTIVE_COMPONENT
import net.horizonsend.ion.server.features.custom.items.CustomItems.REACTIVE_HOUSING
import net.horizonsend.ion.server.features.custom.items.CustomItems.REACTIVE_MEMBRANE
import net.horizonsend.ion.server.features.custom.items.CustomItems.REACTIVE_PLATING
import net.horizonsend.ion.server.features.custom.items.CustomItems.REACTOR_CONTROL
import net.horizonsend.ion.server.features.custom.items.CustomItems.REACTOR_FRAME
import net.horizonsend.ion.server.features.custom.items.CustomItems.REINFORCED_FRAME
import net.horizonsend.ion.server.features.custom.items.CustomItems.RIFLE
import net.horizonsend.ion.server.features.custom.items.CustomItems.RIFLE_RECEIVER
import net.horizonsend.ion.server.features.custom.items.CustomItems.SHOTGUN_RECEIVER
import net.horizonsend.ion.server.features.custom.items.CustomItems.SILK_TOUCH_MOD
import net.horizonsend.ion.server.features.custom.items.CustomItems.SMB_RECEIVER
import net.horizonsend.ion.server.features.custom.items.CustomItems.SMOKE_GRENADE
import net.horizonsend.ion.server.features.custom.items.CustomItems.SNIPER_RECEIVER
import net.horizonsend.ion.server.features.custom.items.CustomItems.SPECIAL_MAGAZINE
import net.horizonsend.ion.server.features.custom.items.CustomItems.STANDARD_MAGAZINE
import net.horizonsend.ion.server.features.custom.items.CustomItems.STEEL_ASSEMBLY
import net.horizonsend.ion.server.features.custom.items.CustomItems.STEEL_BLOCK
import net.horizonsend.ion.server.features.custom.items.CustomItems.STEEL_CHASSIS
import net.horizonsend.ion.server.features.custom.items.CustomItems.STEEL_INGOT
import net.horizonsend.ion.server.features.custom.items.CustomItems.STEEL_MODULE
import net.horizonsend.ion.server.features.custom.items.CustomItems.STEEL_PLATE
import net.horizonsend.ion.server.features.custom.items.CustomItems.SUBMACHINE_BLASTER
import net.horizonsend.ion.server.features.custom.items.CustomItems.SUPERCONDUCTOR
import net.horizonsend.ion.server.features.custom.items.CustomItems.SUPERCONDUCTOR_BLOCK
import net.horizonsend.ion.server.features.custom.items.CustomItems.SUPERCONDUCTOR_CORE
import net.horizonsend.ion.server.features.custom.items.CustomItems.TITANIUM_BLOCK
import net.horizonsend.ion.server.features.custom.items.CustomItems.TITANIUM_INGOT
import net.horizonsend.ion.server.features.custom.items.CustomItems.UNCHARGED_SHELL
import net.horizonsend.ion.server.features.custom.items.CustomItems.UNLOADED_ARSENAL_MISSILE
import net.horizonsend.ion.server.features.custom.items.CustomItems.UNLOADED_SHELL
import net.horizonsend.ion.server.features.custom.items.CustomItems.URANIUM
import net.horizonsend.ion.server.features.custom.items.CustomItems.URANIUM_BLOCK
import net.horizonsend.ion.server.features.custom.items.CustomItems.URANIUM_CORE
import net.horizonsend.ion.server.features.custom.items.CustomItems.URANIUM_ROD
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.BATTERY_LARGE
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.BATTERY_MEDIUM
import net.horizonsend.ion.server.miscellaneous.utils.TERRACOTTA_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Material.AIR
import org.bukkit.Material.AMETHYST_SHARD
import org.bukkit.Material.BELL
import org.bukkit.Material.BLACKSTONE
import org.bukkit.Material.BLACK_WOOL
import org.bukkit.Material.BLAST_FURNACE
import org.bukkit.Material.BLUE_WOOL
import org.bukkit.Material.BROWN_WOOL
import org.bukkit.Material.COAL
import org.bukkit.Material.COBWEB
import org.bukkit.Material.COMPOSTER
import org.bukkit.Material.COPPER_BLOCK
import org.bukkit.Material.COPPER_INGOT
import org.bukkit.Material.CYAN_WOOL
import org.bukkit.Material.DARK_PRISMARINE
import org.bukkit.Material.DIAMOND
import org.bukkit.Material.DIAMOND_BLOCK
import org.bukkit.Material.DISPENSER
import org.bukkit.Material.EMERALD_BLOCK
import org.bukkit.Material.ENCHANTED_BOOK
import org.bukkit.Material.GILDED_BLACKSTONE
import org.bukkit.Material.GLASS
import org.bukkit.Material.GLASS_PANE
import org.bukkit.Material.GOLD_BLOCK
import org.bukkit.Material.GOLD_INGOT
import org.bukkit.Material.GOLD_NUGGET
import org.bukkit.Material.GRAY_WOOL
import org.bukkit.Material.GREEN_DYE
import org.bukkit.Material.GREEN_WOOL
import org.bukkit.Material.HONEYCOMB
import org.bukkit.Material.HOPPER
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.IRON_INGOT
import org.bukkit.Material.IRON_TRAPDOOR
import org.bukkit.Material.LAPIS_BLOCK
import org.bukkit.Material.LAPIS_LAZULI
import org.bukkit.Material.LEATHER
import org.bukkit.Material.LIGHT_BLUE_WOOL
import org.bukkit.Material.LIGHT_GRAY_WOOL
import org.bukkit.Material.LIME_WOOL
import org.bukkit.Material.MAGENTA_WOOL
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
import org.bukkit.Material.ORANGE_WOOL
import org.bukkit.Material.PAPER
import org.bukkit.Material.PEARLESCENT_FROGLIGHT
import org.bukkit.Material.PINK_TULIP
import org.bukkit.Material.PINK_WOOL
import org.bukkit.Material.PISTON
import org.bukkit.Material.PRISMARINE
import org.bukkit.Material.PRISMARINE_BRICKS
import org.bukkit.Material.PRISMARINE_CRYSTALS
import org.bukkit.Material.PURPLE_WOOL
import org.bukkit.Material.QUARTZ
import org.bukkit.Material.RAW_GOLD
import org.bukkit.Material.REDSTONE
import org.bukkit.Material.REDSTONE_BLOCK
import org.bukkit.Material.RED_TERRACOTTA
import org.bukkit.Material.RED_WOOL
import org.bukkit.Material.SADDLE
import org.bukkit.Material.SEA_LANTERN
import org.bukkit.Material.SHROOMLIGHT
import org.bukkit.Material.SLIME_BALL
import org.bukkit.Material.SNIFFER_EGG
import org.bukkit.Material.SPONGE
import org.bukkit.Material.SPORE_BLOSSOM
import org.bukkit.Material.STICK
import org.bukkit.Material.STRING
import org.bukkit.Material.TRIPWIRE_HOOK
import org.bukkit.Material.TURTLE_EGG
import org.bukkit.Material.VERDANT_FROGLIGHT
import org.bukkit.Material.WHITE_WOOL
import org.bukkit.Material.YELLOW_WOOL
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.RecipeChoice.ExactChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe

object Crafting : IonServerComponent() {
	override fun onEnable() {
		itemStackShapelessRecipe("steelBlock", STEEL_BLOCK.constructItemStack()) {
			addIngredient(STEEL_INGOT.constructItemStack().asQuantity(9))
		}

		itemStackShapelessRecipe("steelIngot", STEEL_INGOT.constructItemStack().asQuantity(9)) {
			addIngredient(STEEL_BLOCK.constructItemStack().asQuantity(1))
		}

		// Prismarine Bricks
		Bukkit.addRecipe(
			FurnaceRecipe(
				NamespacedKey(IonServer, "prismarine_bricks"),
				ItemStack(PRISMARINE_BRICKS),
				PRISMARINE,
				1f,
				200
			)
		)

		// Bell
		shapedRecipe("bell", BELL) {
			shape("sos", "igi", "ggg")

			setIngredient('g', GOLD_BLOCK)
			setIngredient('i', IRON_BLOCK)
			setIngredient('o', OAK_LOG)
			setIngredient('s', STICK)
		}

		// Wool -> String
		val wool = arrayOf(
			LIGHT_BLUE_WOOL,
			LIGHT_GRAY_WOOL,
			MAGENTA_WOOL,
			ORANGE_WOOL,
			PURPLE_WOOL,
			YELLOW_WOOL,
			BLACK_WOOL,
			BROWN_WOOL,
			GREEN_WOOL,
			WHITE_WOOL,
			BLUE_WOOL,
			CYAN_WOOL,
			GRAY_WOOL,
			LIME_WOOL,
			PINK_WOOL,
			RED_WOOL
		)

		for (material in wool) {
			shapelessRecipe(material.name.lowercase(), ItemStack(STRING, 4), arrayOf(material))
		}

		// Saddle
		shapedRecipe("saddle", SADDLE) {
			shape("lll", "tat")

			setIngredient('l', LEATHER)
			setIngredient('t', TRIPWIRE_HOOK)
			setIngredient('a', AIR)
		}

		// Nametag
		shapedRecipe("nametag", NAME_TAG) {
			shape("s","t","p")

			setIngredient('s', STRING)
			setIngredient('t', TRIPWIRE_HOOK)
			setIngredient('p', PAPER)
		}

		// Gilded Blackstone
		shapedRecipe("gilded_blackstone", GILDED_BLACKSTONE) {
			shape("gbg", "bgb", "gbg")

			setIngredient('g', GOLD_NUGGET)
			setIngredient('b', BLACKSTONE)
		}

		// Sniffer Egg
		shapedRecipe("sniffer_egg", SNIFFER_EGG) {
			shape("rdr", "ded", "rdr")

			setIngredient('r', RED_TERRACOTTA)
			setIngredient('d', DARK_PRISMARINE)
			setIngredient('e', TURTLE_EGG)
		}

		// Ochre Froglight
		shapedRecipe("ochre_froglight", OCHRE_FROGLIGHT) {
			shape("xhx", "hlh", "xhx")

			setIngredient('h', HONEYCOMB)
			setIngredient('l', SHROOMLIGHT)
			setIngredient('x', AIR)
		}

		// Verdant Froglight
		shapedRecipe("verdant_froglight", VERDANT_FROGLIGHT) {
			shape("xsx", "sls", "xsx")

			setIngredient('s', SLIME_BALL)
			setIngredient('l', SHROOMLIGHT)
			setIngredient('x', AIR)
		}

		// Pearlescent Froglight
		shapedRecipe("pearlescent_froglight", PEARLESCENT_FROGLIGHT) {
			shape("xax", "ala", "xax")

			setIngredient('a', AMETHYST_SHARD)
			setIngredient('l', SHROOMLIGHT)
			setIngredient('x', AIR)
		}

		// Spore Blossom
		shapedRecipe("spore_blossom", SPORE_BLOSSOM) {
			shape("xax", "ctc", "xmx")

			setIngredient('a', AMETHYST_SHARD)
			setIngredient('t', PINK_TULIP)
			setIngredient('c', MOSS_CARPET)
			setIngredient('m', MOSS_BLOCK)
			setIngredient('x', AIR)
		}

		// Prismarine Crystals
		shapelessRecipe("prismarine_crystals", ItemStack(PRISMARINE_CRYSTALS, 4), arrayOf(SEA_LANTERN))

		// Nether Wart Block -> Nether Warts
		shapelessRecipe("nether_warts", ItemStack(NETHER_WART, 9), arrayOf(NETHER_WART_BLOCK))

		// Cobweb
		shapedRecipe("cobweb", COBWEB) {
			shape("sxs", "xsx", "sxs")

			setIngredient('s', STRING)
		}

		//Unloaded Turret Shell Crafting
		itemStackShapeRecipe("Unloaded__Shell", UNLOADED_SHELL.constructItemStack()) {
			shape("xyx", "xzx", "xxx")

			setIngredient('x', AIR)
			setIngredient('y', LAPIS_LAZULI)
			setIngredient('z', ExactChoice(TITANIUM_INGOT.constructItemStack()))
		}

		itemStackShapeRecipe("Uncharged_Shell", UNCHARGED_SHELL.constructItemStack()) {
			shape("xyx", "xzx", "xxx")

			setIngredient('x', AIR)
			setIngredient('y', PRISMARINE_CRYSTALS)
			setIngredient('z', COPPER_INGOT)
		}

		itemStackShapeRecipe("Unloaded_Arsenal_Missile", UNLOADED_ARSENAL_MISSILE.constructItemStack()) {
			shape("aba", "mum", "hlo")

			setIngredient('a', ExactChoice(REACTIVE_HOUSING.constructItemStack()))
			setIngredient('b', ExactChoice(STEEL_PLATE.constructItemStack()))
			setIngredient('m', ExactChoice(CIRCUITRY.constructItemStack()))
			setIngredient('u', ExactChoice(URANIUM_ROD.constructItemStack()))
			setIngredient('h', ExactChoice(GAS_CANISTER_HYDROGEN.constructItemStack()))
			setIngredient('l', LAPIS_BLOCK)
			setIngredient('o', ExactChoice(GAS_CANISTER_OXYGEN.constructItemStack()))
		}

		// Blaster Barrel Crafting
		itemStackShapeRecipe("blaster_barrel", GUN_BARREL.constructItemStack()) {
			shape("tct", "ppp", "tct")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.constructItemStack()))
			setIngredient('c', COPPER_INGOT)
			setIngredient('p', PRISMARINE_CRYSTALS)
		}

		// Pistol Receiver Crafting
		itemStackShapeRecipe("pistol_receiver", PISTOL_RECEIVER.constructItemStack()) {
			shape("xxx", "irt", "xxx")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.constructItemStack()))
			setIngredient('r', REDSTONE_BLOCK)
			setIngredient('i', IRON_TRAPDOOR)
			setIngredient('x', AIR)
		}

		// Rifle Receiver Crafting
		itemStackShapeRecipe("rifle_receiver", RIFLE_RECEIVER.constructItemStack()) {
			shape("xtx", "igt", "xtx")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.constructItemStack()))
			setIngredient('g', GOLD_BLOCK)
			setIngredient('i', IRON_TRAPDOOR)
			setIngredient('x', AIR)
		}

		// SMB Receiver Crafting
		itemStackShapeRecipe("smb_receiver", SMB_RECEIVER.constructItemStack()) {
			shape("xtx", "idx", "xtx")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.constructItemStack()))
			setIngredient('d', DIAMOND_BLOCK)
			setIngredient('i', IRON_TRAPDOOR)
			setIngredient('x', AIR)
		}

		// Sniper Receiver Crafting
		itemStackShapeRecipe("sniper_receiver", SNIPER_RECEIVER.constructItemStack()) {
			shape("xtx", "ieb", "xtx")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.constructItemStack()))
			setIngredient('e', EMERALD_BLOCK)
			setIngredient('b', ExactChoice(TITANIUM_BLOCK.constructItemStack()))
			setIngredient('i', IRON_TRAPDOOR)
			setIngredient('x', AIR)
		}

		// Shotgun Receiver Crafting
		itemStackShapeRecipe("shotgun_receiver", SHOTGUN_RECEIVER.constructItemStack()) {
			shape("xxx", "icb", "xtx")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.constructItemStack()))
			setIngredient('c', COPPER_BLOCK)
			setIngredient('b', ExactChoice(TITANIUM_BLOCK.constructItemStack()))
			setIngredient('i', IRON_TRAPDOOR)
			setIngredient('x', AIR)
		}

		// Cannon Receiver Crafting
		itemStackShapeRecipe("cannon_receiver", CANNON_RECEIVER.constructItemStack()) {
			shape("xxx", "xba", "gxx")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
			setIngredient('b', ExactChoice(ALUMINUM_BLOCK.constructItemStack()))
			setIngredient('g', GOLD_INGOT)
			setIngredient('x', AIR)
		}

		// Pistol Crafting
		itemStackShapeRecipe("pistol", PISTOL.constructItemStack()) {
			shape("xxx", "apb", "cxx")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
			setIngredient('p', PISTOL_RECEIVER.constructItemStack())
			setIngredient('b', GUN_BARREL.constructItemStack())
			setIngredient('c', CIRCUITRY.constructItemStack())
			setIngredient('x', AIR)
		}

		// Rifle Crafting
		itemStackShapeRecipe("rifle", RIFLE.constructItemStack()) {
			shape("xxx", "apb", "acx")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
			setIngredient('p', RIFLE_RECEIVER.constructItemStack())
			setIngredient('b', GUN_BARREL.constructItemStack())
			setIngredient('c', CIRCUITRY.constructItemStack())
			setIngredient('x', AIR)
		}

		// SMB Crafting
		itemStackShapeRecipe("submachine_blaster", SUBMACHINE_BLASTER.constructItemStack()) {
			shape("xxx", "apb", "acx")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
			setIngredient('p', SMB_RECEIVER.constructItemStack())
			setIngredient('b', GUN_BARREL.constructItemStack())
			setIngredient('c', CIRCUITRY.constructItemStack())
			setIngredient('x', AIR)
		}

		// Sniper Crafting
		itemStackShapeRecipe("sniper", CustomItems.SNIPER.constructItemStack()) {
			shape("xgx", "apb", "acx")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
			setIngredient('p', SNIPER_RECEIVER.constructItemStack())
			setIngredient('b', GUN_BARREL.constructItemStack())
			setIngredient('c', CIRCUITRY.constructItemStack())
			setIngredient('g', GLASS)
			setIngredient('x', AIR)
		}

		// Shotgun Crafting
		itemStackShapeRecipe("shotgun", CustomItems.SHOTGUN.constructItemStack()) {
			shape("xxb", "apb", "acx")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
			setIngredient('p', SHOTGUN_RECEIVER.constructItemStack())
			setIngredient('b', GUN_BARREL.constructItemStack())
			setIngredient('c', CIRCUITRY.constructItemStack())
			setIngredient('x', AIR)
		}

		// Cannon Crafting
		itemStackShapeRecipe("cannon", CustomItems.CANNON.constructItemStack()) {
			shape("xax", "xcb", "pxx")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
			setIngredient('p', CANNON_RECEIVER.constructItemStack())
			setIngredient('b', GUN_BARREL.constructItemStack())
			setIngredient('c', CIRCUITRY.constructItemStack())
			setIngredient('x', AIR)
			setIngredient('a', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
		}

		// Basic Power Drill Crafting
		itemStackShapeRecipe("power_drill_basic", POWER_DRILL_BASIC.constructItemStack()) {
			shape("ixx", "xbt", "xts")

			setIngredient('i', ExactChoice(ItemStack(IRON_INGOT)))
			setIngredient('b', ExactChoice(BATTERY_MEDIUM.singleItem()))
			setIngredient('t', ExactChoice(TITANIUM_INGOT.constructItemStack()))
			setIngredient('s', STICK)
			setIngredient('x', AIR)
		}

		// Enhanced Power Drill Crafting
		itemStackShapeRecipe("power_drill_enhanced", POWER_DRILL_ENHANCED.constructItemStack()) {
			shape("iix", "idc", "xts")

			setIngredient('i', ExactChoice(TITANIUM_BLOCK.constructItemStack()))
			setIngredient('d', ExactChoice(POWER_DRILL_BASIC.constructItemStack()))
			setIngredient('c', ExactChoice(CIRCUITRY.constructItemStack()))
			setIngredient('t', ExactChoice(URANIUM_BLOCK.constructItemStack()))
			setIngredient('s', ExactChoice(BATTERY_LARGE.singleItem()))
			setIngredient('x', AIR)
		}

		// Advanced Power Drill Crafting
		itemStackShapeRecipe("power_drill_advanced", POWER_DRILL_ADVANCED.constructItemStack()) {
			shape("iix", "idc", "xts")

			setIngredient('i', ExactChoice(STEEL_PLATE.constructItemStack()))
			setIngredient('d', ExactChoice(POWER_DRILL_ENHANCED.constructItemStack()))
			setIngredient('c', ExactChoice(CIRCUIT_BOARD.constructItemStack()))
			setIngredient('t', ExactChoice(SUPERCONDUCTOR.constructItemStack()))
			setIngredient('s', ExactChoice(STEEL_CHASSIS.constructItemStack()))
			setIngredient('x', AIR)
		}

		// Basic Power Chainsaw
		itemStackShapeRecipe("power_chainsaw_basic", POWER_CHAINSAW_BASIC.constructItemStack()) {
			shape("iix", "idc", "xcs")

			setIngredient('i', ExactChoice(ItemStack(IRON_INGOT)))
			setIngredient('d', ExactChoice(BATTERY_MEDIUM.singleItem()))
			setIngredient('c', ExactChoice(TITANIUM_INGOT.constructItemStack()))
			setIngredient('s', STICK)
			setIngredient('x', AIR)
		}

		// Enhanced Power Chainsaw
		itemStackShapeRecipe("power_chainsaw_enhanced", POWER_CHAINSAW_ENHANCED.constructItemStack()) {
			shape("iix", "idc", "xus")

			setIngredient('i', ExactChoice(TITANIUM_BLOCK.constructItemStack()))
			setIngredient('d', ExactChoice(POWER_CHAINSAW_BASIC.constructItemStack()))
			setIngredient('c', ExactChoice(CIRCUITRY.constructItemStack()))
			setIngredient('u', ExactChoice(URANIUM_BLOCK.constructItemStack()))
			setIngredient('s', ExactChoice(BATTERY_LARGE.singleItem()))
			setIngredient('x', AIR)
		}

		// Advanced Power Chainsaw Crafting
		itemStackShapeRecipe("power_chainsaw_advanced", POWER_CHAINSAW_ADVANCED.constructItemStack()) {
			shape("pbx", "bdc", "xts")

			setIngredient('p', ExactChoice(STEEL_PLATE.constructItemStack()))
			setIngredient('b', ExactChoice(STEEL_BLOCK.constructItemStack()))
			setIngredient('d', ExactChoice(POWER_CHAINSAW_ENHANCED.constructItemStack()))
			setIngredient('c', ExactChoice(CIRCUIT_BOARD.constructItemStack()))
			setIngredient('t', ExactChoice(SUPERCONDUCTOR.constructItemStack()))
			setIngredient('s', ExactChoice(STEEL_CHASSIS.constructItemStack()))
			setIngredient('x', AIR)
		}

		// Basic Power Hoe
		itemStackShapeRecipe("power_hoe_basic", POWER_HOE_BASIC.constructItemStack()) {
			shape("xib", "xsi", "ccx")

			setIngredient('b', ExactChoice(BATTERY_MEDIUM.singleItem()))
			setIngredient('i', COPPER_INGOT)
			setIngredient('c', ExactChoice(TITANIUM_INGOT.constructItemStack()))
			setIngredient('s', STICK)
			setIngredient('x', AIR)
		}

		// Enhanced Power Hoe
		itemStackShapeRecipe("power_hoe_enhanced", POWER_HOE_ENHANCED.constructItemStack()) {
			shape("xus", "xdc", "iix")

			setIngredient('d', ExactChoice(POWER_HOE_BASIC.constructItemStack()))
			setIngredient('i', ExactChoice(TITANIUM_BLOCK.constructItemStack()))
			setIngredient('c', ExactChoice(CIRCUITRY.constructItemStack()))
			setIngredient('u', ExactChoice(URANIUM_BLOCK.constructItemStack()))
			setIngredient('s', ExactChoice(BATTERY_LARGE.singleItem()))
			setIngredient('x', AIR)
		}

		// Advanced Power Hoe Crafting
		itemStackShapeRecipe("power_hoe_advanced", POWER_HOE_ADVANCED.constructItemStack()) {
			shape("xtu", "xdc", "ssx")

			setIngredient('d', ExactChoice(POWER_HOE_ENHANCED.constructItemStack()))
			setIngredient('s', ExactChoice(STEEL_BLOCK.constructItemStack()))
			setIngredient('c', ExactChoice(CIRCUIT_BOARD.constructItemStack()))
			setIngredient('t', ExactChoice(SUPERCONDUCTOR.constructItemStack()))
			setIngredient('u', ExactChoice(STEEL_CHASSIS.constructItemStack()))
			setIngredient('x', AIR)
		}

		// Circuitry Crafting 1
		itemStackShapeRecipe("circuitry_1", CIRCUITRY.constructItemStack()) {
			shape("qdq", "arg", "ccc")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
			setIngredient('c', COPPER_INGOT)
			setIngredient('q', QUARTZ)
			setIngredient('g', GOLD_INGOT)
			setIngredient('d', GREEN_DYE)
			setIngredient('r', REDSTONE)
		}

		// Circuitry Crafting 2
		itemStackShapeRecipe("circuitry_2", CIRCUITRY.constructItemStack()) {
			shape("qdq", "gra", "ccc")

			setIngredient('a', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
			setIngredient('c', COPPER_INGOT)
			setIngredient('q', QUARTZ)
			setIngredient('g', GOLD_INGOT)
			setIngredient('d', GREEN_DYE)
			setIngredient('r', REDSTONE)
		}

		// Standard Magazine Crafting
		itemStackShapeRecipe("standard_magazine", STANDARD_MAGAZINE.constructItemStack()) {
			shape("xxx", "rlr", "ttt")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.constructItemStack()))
			setIngredient('l', LAPIS_BLOCK)
			setIngredient('r', REDSTONE)
			setIngredient('x', AIR)
		}

		// Special Magazine Crafting
		itemStackShapeRecipe("special_magazine", SPECIAL_MAGAZINE.constructItemStack()) {
			shape("xxx", "rer", "ttt")

			setIngredient('t', ExactChoice(TITANIUM_INGOT.constructItemStack()))
			setIngredient('e', EMERALD_BLOCK)
			setIngredient('r', REDSTONE)
			setIngredient('x', AIR)
		}

		// Empty Gas Canister Crafting
		itemStackShapeRecipe("empty_gas_canister", GAS_CANISTER_EMPTY.constructItemStack()) {
			shape("xix", "igi", "xix")

			setIngredient('i', ExactChoice(ALUMINUM_INGOT.constructItemStack()))
			setIngredient('g', GLASS_PANE)
			setIngredient('x', AIR)
		}

		// Crude Fuel Crafting
//		itemStackShapeRecipe("crude_fuel", CRUDE_FUEL.constructItemStack()) {
//			shape("xxx", "yzy", "xxx")
//
//			setIngredient('x', AIR)
//			setIngredient('y', GAS_CANISTER_EMPTY.constructItemStack())
//			setIngredient('z', DIAMOND)
//		}

		// Detonator Crafting
		itemStackShapeRecipe("detonator", DETONATOR.constructItemStack()) {
			shape(" r ", "tut", " t ",)

			setIngredient('r', REDSTONE)
			setIngredient('t', TITANIUM_INGOT.constructItemStack())
			setIngredient('u', URANIUM.constructItemStack())
		}

		itemStackShapeRecipe("smokeGrenade", SMOKE_GRENADE.constructItemStack()) {
			shape(" i ", "tct", " t ",)

			setIngredient('i', IRON_INGOT)
			setIngredient('t', TITANIUM_INGOT.constructItemStack())
			setIngredient('c', COAL)
		}

		// Aluminum Block Crafting
		itemStackShapelessRecipe("aluminumBlock", ALUMINUM_BLOCK.constructItemStack()) {
			addIngredient(ALUMINUM_INGOT.constructItemStack().asQuantity(9))
		}

		// Raw Aluminum Block Crafting
		itemStackShapelessRecipe("rawAluminumBlock", RAW_ALUMINUM_BLOCK.constructItemStack()) {
			addIngredient(RAW_ALUMINUM.constructItemStack().asQuantity(9))
		}

		// Aluminum Crafting
		itemStackShapelessRecipe("aluminum", ALUMINUM_INGOT.constructItemStack().asQuantity(9)) {
			addIngredient(ALUMINUM_BLOCK.constructItemStack())
		}

		// Raw Aluminum Crafting
		itemStackShapelessRecipe("rawAluminum", RAW_ALUMINUM.constructItemStack().asQuantity(9)) {
			addIngredient(RAW_ALUMINUM_BLOCK.constructItemStack())
		}

		// Chetherite Block Crafting
		itemStackShapelessRecipe("chetheriteBlock", CHETHERITE_BLOCK.constructItemStack()) {
			addIngredient(CHETHERITE.constructItemStack().asQuantity(9))
		}

		// Chetherite Crafting
		itemStackShapelessRecipe("chetherite", CHETHERITE.constructItemStack().asQuantity(9)) {
			addIngredient(CHETHERITE_BLOCK.constructItemStack())
		}

		// Titanium Block Crafting
		itemStackShapelessRecipe("titaniumBlock", TITANIUM_BLOCK.constructItemStack()) {
			addIngredient(TITANIUM_INGOT.constructItemStack().asQuantity(9))
		}

		// Raw Titanium Block Crafting
		itemStackShapelessRecipe("rawTitaniumBlock", RAW_TITANIUM_BLOCK.constructItemStack()) {
			addIngredient(RAW_TITANIUM.constructItemStack().asQuantity(9))
		}

		// Titanium Crafting
		itemStackShapelessRecipe("titanium", TITANIUM_INGOT.constructItemStack().asQuantity(9)) {
			addIngredient(TITANIUM_BLOCK.constructItemStack())
		}

		// Raw Titanium Crafting
		itemStackShapelessRecipe("rawTitanium", RAW_TITANIUM.constructItemStack().asQuantity(9)) {
			addIngredient(RAW_TITANIUM_BLOCK.constructItemStack())
		}

		// Uranium Block Crafting
		itemStackShapelessRecipe("uraniumBlock", URANIUM_BLOCK.constructItemStack()) {
			addIngredient(URANIUM.constructItemStack().asQuantity(9))
		}

		// Raw Uranium Block Crafting
		itemStackShapelessRecipe("rawUraniumBlock", RAW_URANIUM_BLOCK.constructItemStack()) {
			addIngredient(RAW_URANIUM.constructItemStack().asQuantity(9))
		}

		// Uranium Crafting
		itemStackShapelessRecipe("uranium", URANIUM.constructItemStack().asQuantity(9)) {
			addIngredient(URANIUM_BLOCK.constructItemStack())
		}

		//Steel Plate Crafting
		itemStackShapelessRecipe("steelPlate", STEEL_PLATE.constructItemStack()) {
			addIngredient(STEEL_BLOCK.constructItemStack().asQuantity(9))
		}

		//Steel Module Crafting
		itemStackShapelessRecipe("steelModule", STEEL_MODULE.constructItemStack()) {
			addIngredient(STEEL_CHASSIS.constructItemStack().asQuantity(9))
		}

		//Steel Assembly Crafting
		itemStackShapelessRecipe("steelAssembly", STEEL_ASSEMBLY.constructItemStack()) {
			addIngredient(STEEL_MODULE.constructItemStack().asQuantity(4))
		}

		//Reactor Frame Crafting
		itemStackShapelessRecipe("reactorFrame", REACTOR_FRAME.constructItemStack()) {
			addIngredient(REINFORCED_FRAME.constructItemStack().asQuantity(4))
		}

		// Enriched Uranium Crafting
		itemStackShapelessRecipe("enrichedUranium", ENRICHED_URANIUM.constructItemStack().asQuantity(9)) {
			addIngredient(ENRICHED_URANIUM_BLOCK.constructItemStack())
		}
		// Enriched Uranium Block Crafting
		itemStackShapelessRecipe("enrichedUraniumBlock", ENRICHED_URANIUM_BLOCK.constructItemStack()) {
			addIngredient(ENRICHED_URANIUM.constructItemStack().asQuantity(9))
		}

		// Raw Uranium Crafting
		itemStackShapelessRecipe("rawUranium", RAW_URANIUM.constructItemStack().asQuantity(9)) {
			addIngredient(RAW_URANIUM_BLOCK.constructItemStack())
		}

		//Uranium Core Crafting
		itemStackShapelessRecipe("uraniumCore", URANIUM_CORE.constructItemStack()) {
			addIngredient(ENRICHED_URANIUM_BLOCK.constructItemStack().asQuantity(9))
		}

		//Fuel Rod Core Crafting
		itemStackShapelessRecipe("fuelRodCore", FUEL_ROD_CORE.constructItemStack()) {
			addIngredient(URANIUM_ROD.constructItemStack().asQuantity(9))
		}

		//Fuel Control Crafting
		itemStackShapelessRecipe("fuelControl", FUEL_CONTROL.constructItemStack()) {
			addIngredient(FUEL_CELL.constructItemStack().asQuantity(9))
		}

		itemStackShapelessRecipe("melonToSlices", ItemStack(Material.MELON_SLICE).asQuantity(4)) {
			addIngredient(MELON)
		}

		//Reactive Component Crafting
		itemStackShapeRecipe("reactiveComponent", REACTIVE_HOUSING.constructItemStack()) {
			shape("xxx", "yyy", "xxx")

			setIngredient('x', RecipeChoice.MaterialChoice(*TERRACOTTA_TYPES.toTypedArray()) )
			setIngredient('y', SPONGE)
		}

		itemStackShapeRecipe("netheriteCasing", NETHERITE_CASING.constructItemStack()) {
			shape("xvx", "xyx", "xvx")

			setIngredient('x', NETHERITE_BLOCK)
			setIngredient('y', STEEL_PLATE.constructItemStack())
			setIngredient('v', REACTIVE_HOUSING.constructItemStack())
		}

		//Reactive Housing Crafting
		itemStackShapeRecipe("reactiveHousing", REACTIVE_COMPONENT.constructItemStack()) {
			shape("xxx", "yyy", "xxx")

			setIngredient('x', REDSTONE_BLOCK )
			setIngredient('y', COPPER_BLOCK)
		}

		//Reactive Plating Crafting
		itemStackShapelessRecipe("reactivePlating", REACTIVE_PLATING.constructItemStack()) {
			addIngredient(REACTIVE_COMPONENT.constructItemStack())
			addIngredient(REACTIVE_HOUSING.constructItemStack())
		}

		//Reactive Membrane Crafting
		itemStackShapelessRecipe("reactiveMembrane", REACTIVE_MEMBRANE.constructItemStack()) {
			addIngredient(REACTIVE_CHASSIS.constructItemStack().asQuantity(7))
			addIngredient(CIRCUITRY.constructItemStack())
			addIngredient(ENRICHED_URANIUM.constructItemStack())
		}

		//Reactive Assembly Crafting
		itemStackShapelessRecipe("reactiveAssembly", REACTIVE_ASSEMBLY.constructItemStack()) {
			addIngredient(REACTIVE_MEMBRANE.constructItemStack().asQuantity(9))
		}

		//Advanced Circuitry Crafting Recipe
		itemStackShapelessRecipe("circuitBoard", MOTHERBOARD.constructItemStack()) {
			addIngredient(CIRCUIT_BOARD.constructItemStack().asQuantity(9))
		}

		//Reactor Control Crafting
		itemStackShapelessRecipe("reactorControl", REACTOR_CONTROL.constructItemStack()) {
			addIngredient(FABRICATED_ASSEMBLY.constructItemStack().asQuantity(6))
			addIngredient(MOTHERBOARD.constructItemStack().asQuantity(3))
		}

		//Superconductor Crafting
		itemStackShapelessRecipe("superconductor", SUPERCONDUCTOR.constructItemStack().asQuantity(9)) {
			addIngredient(SUPERCONDUCTOR_BLOCK.constructItemStack())
		}

		//Superconductor Block Crafting
		itemStackShapelessRecipe("superconductorBlock", SUPERCONDUCTOR_BLOCK.constructItemStack()) {
			addIngredient(SUPERCONDUCTOR.constructItemStack().asQuantity(9))
		}

		//Superconductor Core Crafting
		itemStackShapelessRecipe("superconductorCore", SUPERCONDUCTOR_CORE.constructItemStack()) {
			addIngredient(SUPERCONDUCTOR_BLOCK.constructItemStack())
			addIngredient(MOTHERBOARD.constructItemStack().asQuantity(4))
		}

		//Reactor Core Crafting
		itemStackShapeRecipe("bcreactorCore", BATTLECRUISER_REACTOR_CORE.constructItemStack()) {
			shape("wxw", "yzy", "wxw")

			setIngredient('w', REACTOR_FRAME.constructItemStack())
			setIngredient('x', REACTOR_CONTROL.constructItemStack())
			setIngredient('y', FUEL_CONTROL.constructItemStack())
			setIngredient('z', SUPERCONDUCTOR_CORE.constructItemStack())
		}

		itemStackShapeRecipe("bargereactorCore", BARGE_REACTOR_CORE.constructItemStack()) {
			shape("wxw", "zzz", "vyv")

			setIngredient('w', REACTOR_FRAME.constructItemStack())
			setIngredient('x', REACTOR_CONTROL.constructItemStack())
			setIngredient('y', FUEL_CONTROL.constructItemStack())
			setIngredient('z', SUPERCONDUCTOR.constructItemStack())
			setIngredient('v', REINFORCED_FRAME.constructItemStack())
		}

		itemStackShapeRecipe("cruiserreactorCore", CRUISER_REACTOR_CORE.constructItemStack()) {
			shape("wxw", "wyw", "wzw")

			setIngredient('w', REINFORCED_FRAME.constructItemStack())
			setIngredient('x', REACTOR_CONTROL.constructItemStack())
			setIngredient('y', SUPERCONDUCTOR_CORE.constructItemStack())
			setIngredient('z', FUEL_CONTROL.constructItemStack())
		}

		// Tool Mods start
		itemStackShapeRecipe("silk_touch_modifier", SILK_TOUCH_MOD.constructItemStack()) {
			shape("gbg", "tst", "ctc")

			setIngredient('g', RAW_GOLD)
			setIngredient('b', TITANIUM_BLOCK.constructItemStack())
			setIngredient('t', TITANIUM_INGOT.constructItemStack())
			setIngredient('s', ItemStack(ENCHANTED_BOOK).updateMeta {
				it.addEnchant(Enchantment.SILK_TOUCH, 1, true)
			})
			setIngredient('c', CIRCUIT_BOARD.constructItemStack())
		}

		itemStackShapeRecipe("fortune_1_touch_modifier", FORTUNE_1.constructItemStack()) {
			shape("dgd", "csc", "dgd")

			setIngredient('d', DIAMOND)
			setIngredient('g', GOLD_BLOCK)
			setIngredient('c', REACTIVE_COMPONENT.constructItemStack())
			setIngredient('s', SUPERCONDUCTOR.constructItemStack())
		}

		itemStackShapeRecipe("fortune_2_touch_modifier", FORTUNE_2.constructItemStack()) {
			shape("dgd", "csc", "dgd")

			setIngredient('d', STEEL_PLATE.constructItemStack())
			setIngredient('g', URANIUM_BLOCK.constructItemStack())
			setIngredient('c', REACTIVE_PLATING.constructItemStack())
			setIngredient('s', FORTUNE_1.constructItemStack())
		}

		itemStackShapeRecipe("fortune_3_touch_modifier", FORTUNE_3.constructItemStack()) {
			shape("dgd", "csc", "dgd")

			setIngredient('d', STEEL_ASSEMBLY.constructItemStack())
			setIngredient('g', ENRICHED_URANIUM_BLOCK.constructItemStack())
			setIngredient('c', REACTIVE_ASSEMBLY.constructItemStack())
			setIngredient('s', FORTUNE_2.constructItemStack())
		}

		itemStackShapeRecipe("power_capacity_25_modifier", POWER_CAPACITY_25.constructItemStack()) {
			shape("sbs", "brb", "scs")

			setIngredient('s', STEEL_INGOT.constructItemStack())
			setIngredient('b', BATTERY_MEDIUM.singleItem())
			setIngredient('r', REDSTONE_BLOCK)
			setIngredient('c', CIRCUITRY.constructItemStack())
		}

		itemStackShapeRecipe("power_capacity_50_modifier", POWER_CAPACITY_50.constructItemStack()) {
			shape("sbs", "brb", "scs")

			setIngredient('s', STEEL_PLATE.constructItemStack())
			setIngredient('b', BATTERY_LARGE.singleItem())
			setIngredient('r', REDSTONE_BLOCK)
			setIngredient('c', CIRCUIT_BOARD.constructItemStack())
		}

		itemStackShapeRecipe("auto_smelt_modifier", POWER_CAPACITY_50.constructItemStack()) {
			shape("srs", "bfb", "scs")

			setIngredient('s', STEEL_PLATE.constructItemStack())
			setIngredient('b', GOLD_BLOCK)
			setIngredient('f', BLAST_FURNACE)
			setIngredient('r', REDSTONE_BLOCK)
			setIngredient('c', CIRCUIT_BOARD.constructItemStack())
		}

		itemStackShapeRecipe("auto_compost_modifier", POWER_CAPACITY_50.constructItemStack()) {
			shape("tit", "tct", "trt")

			setIngredient('t', TITANIUM_INGOT.constructItemStack())
			setIngredient('i', IRON_INGOT)
			setIngredient('c', COMPOSTER)
			setIngredient('r', REDSTONE_BLOCK)
		}

		itemStackShapeRecipe("auto_replant_modifier", AUTO_REPLANT.constructItemStack()) {
			shape("ipi", "tct", "iri")

			setIngredient('i', IRON_INGOT)
			setIngredient('p', PISTON)
			setIngredient('t', TITANIUM_INGOT.constructItemStack())
			setIngredient('c', DISPENSER)
			setIngredient('r', REDSTONE_BLOCK)
		}

		itemStackShapeRecipe("auto_fertilizer_modifier", FERTILIZER_DISPENSER.constructItemStack()) {
			shape("ipi", "tct", "iri")

			setIngredient('i', IRON_INGOT)
			setIngredient('p', HOPPER)
			setIngredient('t', TITANIUM_INGOT.constructItemStack())
			setIngredient('c', DISPENSER)
			setIngredient('r', REDSTONE_BLOCK)
		}

		itemStackShapeRecipe("extended_bar_modifier", EXTENDED_BAR.constructItemStack()) {
			shape("stx", "tct", "xts")

			setIngredient('s', STEEL_PLATE.constructItemStack())
			setIngredient('t', TITANIUM_INGOT.constructItemStack())
			setIngredient('c', STEEL_CHASSIS.constructItemStack())
			setIngredient('x', AIR)
		}

		itemStackShapeRecipe("aoe_1_modifier", EXTENDED_BAR.constructItemStack()) {
			shape("ipi", "prp", "ipi")

			setIngredient('i', IRON_BLOCK)
			setIngredient('p', PISTON)
			setIngredient('r', REDSTONE_BLOCK)
		}

		itemStackShapeRecipe("aoe_2_modifier", EXTENDED_BAR.constructItemStack()) {
			shape("ipi", "prp", "ipi")

			setIngredient('i', ExactChoice(STEEL_BLOCK.constructItemStack()))
			setIngredient('p', PISTON)
			setIngredient('r', ExactChoice(RANGE_1.constructItemStack()))
		}

		itemStackShapeRecipe("aoe_3_modifier", EXTENDED_BAR.constructItemStack()) {
			shape("ipi", "prp", "ipi")

			setIngredient('i', ExactChoice(STEEL_BLOCK.constructItemStack()))
			setIngredient('p', ExactChoice(STEEL_PLATE.constructItemStack()))
			setIngredient('r', ExactChoice(RANGE_2.constructItemStack()))
		}

		itemStackShapeRecipe("vein_miner_modifier", EXTENDED_BAR.constructItemStack()) {
			shape("ipi", "prp", "ipi")

			setIngredient('i', ExactChoice(ALUMINUM_BLOCK.constructItemStack()))
			setIngredient('p', OBSERVER)
			setIngredient('r', ExactChoice(RANGE_1.constructItemStack()))
		}

		// Tool mods end
	}

	private fun shapedRecipe(name: String, result: Material, execute: ShapedRecipe.() -> Unit) {
		val recipe = ShapedRecipe(NamespacedKey(IonServer, name), ItemStack(result))
		execute(recipe)
		Bukkit.addRecipe(recipe)
	}

	private fun itemStackShapeRecipe(name: String, result: ItemStack, execute: ShapedRecipe.() -> Unit) {
		val recipe = ShapedRecipe(NamespacedKey(IonServer, name), result)
		execute(recipe)
		Bukkit.addRecipe(recipe)
	}

	private fun shapelessRecipe(name: String, result: ItemStack, ingredients: Array<Material>) {
		val recipe = ShapelessRecipe(NamespacedKey(IonServer, name), result)
		for (ingredient in ingredients) recipe.addIngredient(ingredient)
		Bukkit.addRecipe(recipe)
	}

	private fun itemStackShapelessRecipe(name: String, result: ItemStack, execute: ShapelessRecipe.() -> Unit) {
		val recipe = ShapelessRecipe(NamespacedKey(IonServer, name), result)
		execute(recipe)
		Bukkit.addRecipe(recipe)
	}
}
