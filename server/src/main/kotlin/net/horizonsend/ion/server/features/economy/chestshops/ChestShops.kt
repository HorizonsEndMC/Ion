package net.horizonsend.ion.server.features.economy.chestshops

import net.horizonsend.ion.common.database.schema.economy.ChestShop
import net.horizonsend.ion.common.database.schema.economy.ChestShop.Companion.setItem
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.gui.sendDepositMessage
import net.horizonsend.ion.common.utils.text.gui.sendWithdrawMessage
import net.horizonsend.ion.common.utils.text.join
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.common.utils.text.wrap
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.cache.ChestShopCache
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.sendText
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.getNMSData
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.misc.ServerInboxes
import net.horizonsend.ion.server.features.nations.utils.isNPC
import net.horizonsend.ion.server.features.transport.items.util.getTransferSpaceFor
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.ValidatorResult
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.depositMoney
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.front
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.hasEnoughMoney
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.withdrawMoney
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.SnbtPrinterTagVisitor
import net.minecraft.server.MinecraftServer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.block.data.type.WallSign
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.UUID
import kotlin.jvm.optionals.getOrNull
import net.minecraft.world.item.ItemStack as NMSItemStack

object ChestShops : IonServerComponent() {
	override fun onEnable() {
		Tasks.asyncRepeat(120L, 120L, ::displayShops)
	}

	@EventHandler
	fun onClickBlock(event: PlayerInteractEvent) {
		val block = event.clickedBlock ?: return
		if (block.type.isWallSign) checkSignInteraction(event, block)
		if (block.type == Material.CHEST) checkChestInteraction(event, block)
	}

	/**
	 * Main entrypoint of shops. Handles detection, setting the item, and purchases / sales.
	 **/
	fun checkSignInteraction(event: PlayerInteractEvent, clickedBlock: Block) {
		val placedOn = clickedBlock.getRelative((clickedBlock.blockData as WallSign).facing.oppositeFace)
		if (placedOn.type != Material.CHEST) return
		val chest = placedOn.state as Chest

		val sign = clickedBlock.state as Sign

		// Handle detecting shops
		val type = getUndetectedShopType(sign)
		if (type != null && event.action != Action.LEFT_CLICK_BLOCK) {
			setupShop(event.player, chest, sign, type)
			event.isCancelled = true

			return
		}

		val shop = getShop(sign) ?: return

		// Handle the case of the player destroying the shop
		if (event.player.slPlayerId == shop.owner && event.action == Action.LEFT_CLICK_BLOCK) return

		event.isCancelled = true

		// Handle unset items
		if (shop.soldItem == null && event.player.slPlayerId == shop.owner) {
			setItem(event.player, shop)
			return
		}

		// Handle purchases
		purchase(event.player, chest, shop)
	}

	/**
	 * Deny access to chests when they are a shop and the interacting player is not the owner
	 **/
	fun checkChestInteraction(event: PlayerInteractEvent, clickedBlock: Block) {
		val chest = clickedBlock.state as? Chest ?: return
		val shop = getShop(chest) ?: return

		if (event.player.slPlayerId != shop.owner) {
			event.isCancelled = true
		}
	}

	private fun setupShop(player: Player, chest: Chest, sign: Sign, type: ShopType) {
		if (getShop(chest) != null) {
			player.userError("That chest already has a shop!")
			return
		}

		Tasks.async {
			val priceResult = validatePrice(sign.front().line(1))
			val price = priceResult.result ?: return@async priceResult.sendReason(player)

			val priceValidation = Bazaars.checkValidPrice(price)
			if (!priceValidation.isSuccess()) return@async priceValidation.sendReason(player)

			val worldKey = sign.world.key().asString()
			val location = Vec3i(sign.x, sign.y, sign. z)

			ChestShop.create(
				player.slPlayerId,
				location = location,
				world = worldKey,
				soldItem = null,
				price = price,
				selling = type == ShopType.SELL,
			)
		}
	}

	/**
	 * Returns a validator result of the price, or an error if it is invalid.
	 **/
	private fun validatePrice(line: Component): ValidatorResult<Double> {
		val plain = line.plainText()

		if (plain.isEmpty()) return ValidatorResult.FailureResult(Component.text("You must enter a price!", NamedTextColor.RED))

		val asDouble = plain.toDoubleOrNull() ?: return ValidatorResult.FailureResult(Component.text("You must enter a valid price!", NamedTextColor.RED))

		return ValidatorResult.ValidatorSuccessSingleEntry(asDouble)
	}

	@EventHandler
	fun onBlockBreak(event: BlockBreakEvent) {
		val block = event.block

		if (block.type == Material.CHEST) {
			checkSurroundingShopsIntegrity(block.state as? Chest ?: return)
		}

		if (!block.type.isWallSign) return

		val state = block.state as Sign
		val shop = getShop(state) ?: return

		if (event.player.slPlayerId != shop.owner && !event.player.hasPermission("group.dutymode")) {
			event.isCancelled
			return
		}

		Tasks.async {
			ChestShop.delete(shop._id)
			event.player.success("Removed Chest Shop")
		}
	}

	/**
	 * Handle purchases and sales to the shop.
	 **/
	fun purchase(player: Player, chest: Chest, shop: ChestShop) {
		val itemData = shop.soldItem ?: return player.userError("That shop does not have an item configured!")

		val itemType = loadItem(itemData)

		if (itemType == null) {
			val id: UUID = UUID.randomUUID()

			log.info("Could not load item data! Id: $id, data: $itemData")
			player.serverError("That item could not be loaded. Please file a bug report. Id: $id")
			return
		}

		val itemValidation = verifyItem(itemType)
		if (!itemValidation.isSuccess()) return itemValidation.sendReason(player)

		if (player.slPlayerId == shop.owner) {
			player.userError("You can't purchase from your own shop!")
			return
		}

		when (shop.selling) {
			// Player buys if the shop is selling
			true -> buyItems(player, chest, shop, itemType)
			false -> sellItems(player, chest, shop, itemType)
		}
	}

	/**
	 * Sets the item sold in the shop.
	 **/
	private fun setItem(player: Player, shop: ChestShop) {
		val item = player.inventory.itemInMainHand
		val itemValidation = verifyItem(item)
		if (!itemValidation.isSuccess()) return itemValidation.sendReason(player)

		val stringRepresentation = getStringRepresentation(item)

		Tasks.async {
			setItem(shopId = shop._id, stringRepresentation)
			player.success("Set shop to sell {0}", item.displayNameComponent)
		}
	}

	private fun buyItems(interacting: Player, chest: Chest, shop: ChestShop, item: ItemStack) {
		val asOne = item.asOne()
		val room = getTransferSpaceFor(interacting.inventory as CraftInventory, asOne)

		if (room == 0) {
			interacting.userError("Your inventory is full!")
			return
		}

		val chestInv = chest.inventory
		val available = chestInv.sumOf { stack: ItemStack? -> stack?.takeIf { _ -> stack.isSimilar(asOne) }?.amount ?: 0 }

		if (available == 0) {
			interacting.userError("That shop is out of stock!")
			return
		}

		val amount = minOf(if (interacting.isSneaking) 10 else 1, available)
		val price = amount * shop.price

		if (!interacting.hasEnoughMoney(price)) {
			interacting.userError("You don't have enough money to purchase that!")
			return
		}

		val limit = Bazaars.takePlayerItemsOfType(chestInv, asOne, amount)
		val newPrice = limit * shop.price

		interacting.withdrawMoney(newPrice)
		sendWithdrawMessage(interacting, newPrice)
		Bukkit.getOfflinePlayer(shop.owner.uuid).depositMoney(newPrice)
		Bukkit.getPlayer(shop.owner.uuid)?.let {
			it.success("${interacting.name} purchased {0} of {1} for {2}", limit, asOne.displayNameComponent, price.toCreditComponent())
			sendDepositMessage(it, newPrice)
		}

		Bazaars.giveOrDropItems(item, limit, interacting)

		Tasks.async {
			val availableNew = chestInv.sumOf { stack: ItemStack? -> stack?.takeIf { _ -> stack.isSimilar(asOne) }?.amount ?: 0 }
			if (availableNew <= 0) ServerInboxes.sendServerMessage(shop.owner, Component.text("Sell Shop Empty"), Component.text("Your shop at ${chest.x}, ${chest.y}, ${chest.z} on ${chest.world.name} is empty!"))
		}
	}

	private fun sellItems(interacting: Player, chest: Chest, shop: ChestShop, item: ItemStack) {
		val asOne = item.asOne()

		val chestInv = chest.inventory
		val room = getTransferSpaceFor(chestInv as CraftInventory, asOne)

		if (room == 0) {
			interacting.userError("The shop inventory is full!")
			return
		}

		val available = interacting.inventory.sumOf { stack: ItemStack? -> stack?.takeIf { _ -> stack.isSimilar(asOne) }?.amount ?: 0 }

		if (available == 0) {
			interacting.userError("You don't have any items to sell!")
			return
		}

		val amount = minOf(if (interacting.isSneaking) 10 else 1, room)
		val profit = amount * shop.price

		val shopOwner = Bukkit.getOfflinePlayer(shop.owner.uuid)

		if (!shopOwner.hasEnoughMoney(profit)) {
			interacting.userError("The shop owner does not have enough money to purchase that!")
			return
		}

		val limit = Bazaars.takePlayerItemsOfType(interacting.inventory, asOne, amount)
		val newPrice = limit * shop.price

		interacting.depositMoney(newPrice)
		sendDepositMessage(interacting, newPrice)
		Bukkit.getOfflinePlayer(shop.owner.uuid).withdrawMoney(newPrice)

		Bukkit.getPlayer(shop.owner.uuid)?.let {
			it.success("${interacting.name} sold {0} of {1} for {2}", limit, asOne.displayNameComponent, profit.toCreditComponent())
			sendWithdrawMessage(it, newPrice)
		}

		Bazaars.giveOrDropItems(item, limit, chestInv, chest.location.toCenterLocation())

		Tasks.async {
			val room = getTransferSpaceFor(chestInv, asOne)
			if (room <= 0) ServerInboxes.sendServerMessage(shop.owner, Component.text("Buy Shop Full"), Component.text("Your shop at ${chest.x}, ${chest.y}, ${chest.z} on ${chest.world.name} is full!"))
		}
	}

	/**
	 * Blocks / allows items to be sold. Mostly here for future proofing if items need to be banned.
	 **/
	fun verifyItem(itemStack: ItemStack): InputResult {
		if (itemStack.isEmpty) return InputResult.FailureReason(listOf(Component.text("You must hold an item!", NamedTextColor.RED)))

		// Put future checks here

		return InputResult.InputSuccess
	}

	/**
	 * Returns a SNBT string containing the item data.
	 **/
	private fun getStringRepresentation(itemStack: ItemStack): String {
		val nms = CraftItemStack.asNMSCopy(itemStack)
		val tag: CompoundTag = nms.save(MinecraftServer.getServer().registryAccess(), CompoundTag()) as CompoundTag
		return SnbtPrinterTagVisitor().visit(tag)
	}

	/**
	 * Loads an itemstack from SNBT data.
	 **/
	private fun loadItem(string: String): ItemStack? {
		val nbt = NbtUtils.snbtToStructure(string)

		val nmsStack = NMSItemStack.parse(MinecraftServer.getServer().registryAccess(), nbt).getOrNull() ?: return null

		return CraftItemStack.asCraftMirror(nmsStack)
	}

	private enum class ShopType(val keyword: String) {
		BUY("[Buy Shop]"),
		SELL("[Sell Shop]")
	}

	private fun getUndetectedShopType(sign: Sign): ShopType? {
		val topText = sign.front().line(0).plainText()

		ShopType.entries.forEach { type ->
			if (type.keyword.equals(topText, ignoreCase = true)) return type
		}

		return null
	}

	private val SELL_SHOP_FIRST_LINE = bracketed(Component.text("Sell Shop", NamedTextColor.GREEN), leftBracket = '{', rightBracket = '}')
	private val BUY_SHOP_FIRST_LINE = bracketed(Component.text("Buy Shop", NamedTextColor.GREEN), leftBracket = '{', rightBracket = '}')
	private val NULL_ITEM_TEXT = Component.text("?", HE_MEDIUM_GRAY)

	/**
	 * Updates the sign text of the shop
	 **/
	fun updateSign(shop: ChestShop) = Tasks.sync {
		val worldKey = Key.key(shop.world)
		val world = Bukkit.getWorld(worldKey) ?: return@sync

		val (x, y, z) = Vec3i(shop.location)
		val block = getBlockIfLoaded(world, x, y, z) ?: return@sync

		val sign = block.state
		if (sign !is Sign) return@sync

		Tasks.async {
			sign.front().line(0, if (shop.selling) SELL_SHOP_FIRST_LINE else BUY_SHOP_FIRST_LINE)
			sign.front().line(1, shop.price.toCreditComponent())
			sign.front().line(2, Component.text(SLPlayer.getName(shop.owner)!!))
			sign.front().line(3, shop.soldItem?.let(::loadItem)?.displayNameComponent ?: NULL_ITEM_TEXT)

			Tasks.sync {
				sign.update()
			}
		}
	}

	/** Returns the chest attached to the chest with a sign */
	fun getShop(chest: Chest): ChestShop? {
		val worldKey = chest.world.key
		for (dir in CARDINAL_BLOCK_FACES) {
			val signBlock = chest.block.getRelativeIfLoaded(dir) ?: continue
			val vec3i = Vec3i(signBlock.x, signBlock.y, signBlock.z)

			val cached = ChestShopCache.getByLocation(worldKey, vec3i)
			if (cached != null) return cached
		}

		return null
	}

	/** Returns the chest shop at the sign */
	fun getShop(sign: Sign): ChestShop? {
		val worldKey = sign.world.key()
		val location = Vec3i(sign.x, sign.y, sign. z)

		return ChestShopCache.getByLocation(worldKey, location)
	}

	/**
	 * Checks the integrity of the provided shop.
	 **/
	private fun checkShopIntegrity(shop: ChestShop) {
		val world = Bukkit.getWorld(Key.key(shop.world)) ?: return // Different server
		val vec3i = Vec3i(shop.location)
		val data = getBlockDataSafe(world, vec3i.x, vec3i.y, vec3i.z) ?: return // Not loaded

		// If the sign has been removed, destroy the shop
		if (data !is WallSign) {
			Tasks.async {
				ChestShop.delete(shop._id)
			}
		}
	}

	/**
	 * Checks the integrity of shops placed on the chest.
	 **/
	private fun checkSurroundingShopsIntegrity(chest: Chest) {
		val shop = getShop(chest) ?: return
		// Give time for the block to break
		Tasks.syncDelay(2L) { checkShopIntegrity(shop) }
	}

	private fun displayShops() {
		val worlds = ChestShopCache.byLocation.rowKeySet()

		for (worldKey in worlds) {
			val world = Bukkit.getWorld(worldKey) ?: continue
			val players = world.players
			if (players.isEmpty()) continue

			val worldShops = ChestShopCache.byLocation.row(worldKey) ?: continue
			if (worldShops.isEmpty()) continue

			for ((vec3i, shop) in worldShops) {
				val soldItem = shop.soldItem?.let(::loadItem) ?: continue

				val data = getBlockDataSafe(world, vec3i.x, vec3i.y, vec3i.z) as? WallSign

				if (data == null) {
					checkShopIntegrity(shop)
					continue
				}

				displayShopContents(soldItem, data, vec3i, world, shop.price, players)
			}
		}
	}

	private fun displayShopContents(soldItem: ItemStack, signData: WallSign, shopLocation: Vec3i, world: World, price: Double, worldPlayers: Collection<Player>) {
		val offset = signData.facing.oppositeFace
		val location = shopLocation.toLocation(world).toCenterLocation().add(offset.direction).add(0.0, 0.25, 0.0)

		val players = worldPlayers.filter { player ->
			player.location.distance(location) < 20.0
				&& !player.isNPC
				&& player.getSetting(PlayerSettings::chestShopDisplays)
		}

		val itemDisplay = ClientDisplayEntityFactory.createItemDisplay(world.minecraft)

		itemDisplay.setItemStack(soldItem)
		itemDisplay.billboard = Display.Billboard.VERTICAL
		itemDisplay.brightness = Display.Brightness(15, 15)
		itemDisplay.transformation = Transformation(
			/* translation = */ Vector3f(0f),
			/* leftRotation = */ Quaternionf(),
			/* scale = */ Vector3f(0.75f),
			/* rightRotation = */ Quaternionf()
		)

		val nms = itemDisplay.getNMSData(location.x, location.y + 0.55, location.z)

		players.forEach { player ->
			val lines = soldItem.displayNameComponent.wrap(50).join(Component.newline())

			player.sendText(location.clone().add(0.0, 1.20, 0.0), lines, 121L, 0.75f)
			player.sendText(location.clone().add(0.0, 0.95, 0.0), price.toCreditComponent(), 121L)
			ClientDisplayEntities.sendEntityPacket(player, nms, 121L)
		}
	}
}
