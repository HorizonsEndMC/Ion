package net.horizonsend.ion.server.features.custom.items.type

/* It's a surprise tool that will help us later
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.nations.NationBuffType
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class NationBuffCustomItem(
    key: IonRegistryKey<CustomItem, out CustomItem>,
    itemFactory: ItemFactory,
    displayName: Component,
    val nationBuff: NationBuffType
) : CustomItem(key, displayName, itemFactory) {
    override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
        addComponent(
            CustomComponentTypes.LISTENER_PLAYER_INTERACT,
            Listener.rightClickListener(this@NationBuffCustomItem) { event, _, itemStack ->
                activate(itemStack, event.player)
            })
    }

    fun activate(itemStack: ItemStack, livingEntity: LivingEntity) {
        if (livingEntity !is Player) return

        val nationId = PlayerCache[livingEntity].nationOid
        if (nationId == null) {
            livingEntity.userError("You must be in a nation to use this!")
            return
        }

        val nation = NationCache[nationId]
        if (nation.activatedBuffs.contains(nationBuff.key.key)) {
            livingEntity.userError("Your nation already has this buff active!")
            return
        }

        Tasks.async {
            Nation.removeAllAvailableBuffs(nationId)
            Nation.removeAllActivatedBuffs(nationId)

            Nation.addAvailableBuff(nationId, nationBuff.key.key)
            Nation.addActivatedBuff(nationId, nationBuff.key.key)

            Tasks.sync {
                val inventory = (livingEntity as? InventoryHolder)?.inventory ?: return@sync
                inventory.remove(itemStack)
            }

            livingEntity.success("Activated buff ${nationBuff.key.key}")
        }
    }
}
 */
