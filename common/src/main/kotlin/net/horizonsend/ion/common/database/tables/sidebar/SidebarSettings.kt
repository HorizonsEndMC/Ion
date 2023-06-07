package net.horizonsend.ion.common.database.tables.sidebar

import net.horizonsend.ion.common.database.IonEntityClass
import net.horizonsend.ion.common.database.PlayerData
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

class SidebarSettings(id: EntityID<Int>) : IntEntity(id) {
    var contactsStarships by DataTable.contactsStarships
    var contactsPlanets by DataTable.contactsPlanets
    var contactsStars by DataTable.contactsStars
    var contactsBeacons by DataTable.contactsBeacons

	var sidebarSettings by SidebarSettings.via(PlayerDataToSidebarSettingsTable.playerData, PlayerDataToSidebarSettingsTable.sidebarSettings)

    companion object : IonEntityClass<Int, SidebarSettings>(DataTable, SidebarSettings::class.java, ::SidebarSettings)

    object DataTable : IntIdTable("sidebarSettings") {
        val contactsStarships = bool("contactsStarships").default(true)
        val contactsPlanets = bool("contactsPlanets").default(true)
        val contactsStars = bool("contactsStars").default(true)
        val contactsBeacons = bool("contactsBeacons").default(true)
    }

	object PlayerDataToSidebarSettingsTable : Table() {
		val playerData = reference("playerData", DataTable)
		val sidebarSettings = reference("sidebarSettings", DataTable)
	}
}
