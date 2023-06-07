package net.horizonsend.ion.common.database.tables.sidebar

import net.horizonsend.ion.common.database.IonEntityClass
import net.horizonsend.ion.common.database.PlayerData
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption.CASCADE

class SidebarSettings(id: EntityID<Int>) : IntEntity(id) {
    var playerData by PlayerData referencedOn Table.playerData
    var contactsStarships by Table.contactsStarships
    var contactsPlanets by Table.contactsPlanets
    var contactsStars by Table.contactsStars
    var contactsBeacons by Table.contactsBeacons

    companion object : IonEntityClass<Int, SidebarSettings>(Table, SidebarSettings::class.java, ::SidebarSettings)

    object Table : IntIdTable("sidebarSettings") {
        val playerData = reference("player", PlayerData.Table, onDelete = CASCADE).index()
        val contactsStarships = bool("contactsStarships").default(true)
        val contactsPlanets = bool("contactsPlanets").default(true)
        val contactsStars = bool("contactsStars").default(true)
        val contactsBeacons = bool("contactsBeacons").default(true)
    }
}
