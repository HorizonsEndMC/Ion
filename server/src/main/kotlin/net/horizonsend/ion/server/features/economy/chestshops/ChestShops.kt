package net.horizonsend.ion.server.features.economy.chestshops

import net.horizonsend.ion.common.database.schema.economy.ChestShop
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.colors.PRIVATEER_LIGHT_TEAL
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.cache.ChestShopCache
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.ValidatorResult
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.front
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.SnbtPrinterTagVisitor
import net.minecraft.server.MinecraftServer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.block.data.type.WallSign
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import kotlin.jvm.optionals.getOrNull
import net.minecraft.world.item.ItemStack as NMSItemStack

object ChestShops : IonServerComponent() {
	@EventHandler
	fun onClickSign(event: PlayerInteractEvent) {
		val block = event.clickedBlock ?: return
		if (!block.type.isWallSign) return

		val placedOn = block.getRelative((block.blockData as WallSign).facing.oppositeFace)
		if (placedOn.type != Material.CHEST) return

		val sign = block.state as Sign

		if (matchesUndetectedSign(sign)) {
			setupShop(event.player, placedOn.state as Chest, sign)
			return
		}

		val shop = getShop(sign) ?: return
		interactWithShop(event.player, shop)
	}

	@EventHandler
	fun onBlockBreak(event: BlockBreakEvent) {
		val block = event.block
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

	fun setupShop(player: Player, chest: Chest, sign: Sign) {
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
				selling = true,
			)
		}
	}

	fun validatePrice(line: Component): ValidatorResult<Double> {
		val plain = line.plainText()

		if (plain.isEmpty()) return ValidatorResult.FailureResult(Component.text("You must enter a price!", NamedTextColor.RED))

		val asDouble = plain.toDoubleOrNull() ?: return ValidatorResult.FailureResult(Component.text("You must enter a valid price!", NamedTextColor.RED))

		return ValidatorResult.ValidatorSuccessSingleEntry(asDouble)
	}

	fun interactWithShop(player: Player, shop: ChestShop) {
		if (shop.soldItem == null && player.slPlayerId == shop.owner) {
			val item = player.inventory.itemInMainHand
			val itemValidation = verifyItem(item)
			if (!itemValidation.isSuccess()) return itemValidation.sendReason(player)

			val stringRepresentation = getStringRepresentation(item)

			Tasks.async {
				ChestShop.setItem(shopId = shop._id, stringRepresentation)
				player.success("Set shop to sell {0}", item.displayNameComponent)
			}
		}

		player.serverError("TODO")
	}

	fun verifyItem(itemStack: ItemStack): InputResult {
		if (itemStack.isEmpty) return InputResult.FailureReason(listOf(Component.text("You must hold an item!", NamedTextColor.RED)))

		// Put future checks here

		return InputResult.InputSuccess
	}

	fun matchesUndetectedSign(sign: Sign): Boolean {
		return sign.front().line(0).plainText().equals("[shop]", ignoreCase = true)
	}

	fun getShop(sign: Sign): ChestShop? {
		val worldKey = sign.world.key()
		val location = Vec3i(sign.x, sign.y, sign. z)

		return ChestShopCache.getByLocation(worldKey, location)
	}

	fun getStringRepresentation(itemStack: ItemStack): String {
		val nms = CraftItemStack.asNMSCopy(itemStack)
		val tag: CompoundTag = nms.save(MinecraftServer.getServer().registryAccess(), CompoundTag()) as CompoundTag
		return SnbtPrinterTagVisitor().visit(tag)
	}

	fun loadItem(string: String): ItemStack? {
		val nbt = NbtUtils.snbtToStructure(string)

		val nmsStack = NMSItemStack.parse(MinecraftServer.getServer().registryAccess(), nbt).getOrNull() ?: return null

		return CraftItemStack.asCraftMirror(nmsStack)
	}

	private val SELL_SHOP_FIRST_LINE = bracketed(Component.text("Sell Shop", PRIVATEER_LIGHT_TEAL))
	private val BUY_SHOP_FIRST_LINE = bracketed(Component.text("Buy Shop", PRIVATEER_LIGHT_TEAL))
	private val NULL_ITEM_TEXT = Component.text("?", HE_MEDIUM_GRAY)

	fun updateSign(shop: ChestShop) = Tasks.sync {
		val worldKey = Key.key(shop.world)
		val world = Bukkit.getWorld(worldKey) ?: return@sync

		val (x, y, z) = Vec3i(shop.location)
		val block = getBlockIfLoaded(world, x, y, z) ?: return@sync

		val sign = block.state
		if (sign !is Sign) return@sync

		Tasks.async {
			sign.front().line(0, SELL_SHOP_FIRST_LINE)
			sign.front().line(1, shop.price.toCreditComponent())
			sign.front().line(2, Component.text(SLPlayer.getName(shop.owner)!!))
			sign.front().line(3, shop.soldItem?.let(::loadItem)?.displayNameComponent ?: NULL_ITEM_TEXT)

			Tasks.sync {
				sign.update()
			}
		}
	}

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
}
