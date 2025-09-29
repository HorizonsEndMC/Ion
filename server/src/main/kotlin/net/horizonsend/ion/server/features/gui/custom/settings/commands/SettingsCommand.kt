package net.horizonsend.ion.server.features.gui.custom.settings.commands

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.luckPerms
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.command.misc.IonSitCommand
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsMainMenuGui
import net.horizonsend.ion.server.features.gui.custom.settings.SoundSettings
import net.horizonsend.ion.server.features.sidebar.MainSidebar
import net.horizonsend.ion.server.features.sidebar.tasks.ContactsSidebar
import net.horizonsend.ion.server.miscellaneous.AudioRange
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.entity.Player
import kotlin.reflect.KMutableProperty1

@CommandAlias("settings")
@Suppress("unused")
object SettingsCommand : SLCommand() {

    override fun onEnable(manager: PaperCommandManager) {
        registerAsyncCompletion(manager, "contactsChangeSortOrder") { _ ->
            ContactsSidebar.ContactsSorting.entries.map { setting -> setting.name }
        }

        registerAsyncCompletion(manager, "contactsChangeColoring") { _ ->
            ContactsSidebar.ContactsColoring.entries.map { setting -> setting.name }
        }

        registerAsyncCompletion(manager, "clientDisplayEntitiesVisibility") { _ ->
            ClientDisplayEntities.Visibility.entries.map { setting -> setting.name }
        }

        registerAsyncCompletion(manager, "cruiseIndicatorSound") { _ ->
            SoundSettingsCommand.CruiseIndicatorSounds.entries.map { setting -> setting.name }
        }

        registerAsyncCompletion(manager, "audioRange") { _ ->
            AudioRange.entries.map { setting -> setting.name }
        }
    }

    @Default
    @Suppress("unused")
    fun onSettings(sender: Player) {
        SettingsMainMenuGui(sender).openGui()
    }

    @CommandAlias("control dcoverridescruise")
    @CommandCompletion("true|false")
    fun onSettingsControlDcOverridesCruise(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::useAlternateDCCruise, enabled)
    }

    @CommandAlias("control dcrefreshrate")
    @CommandCompletion("-1|1000000")
    fun onSettingsControlDcRefreshRate(sender: Player, value: Int) = asyncCommand(sender) {
        handleIntegerInputSetting(sender, PlayerSettings::dcRefreshRate, value, -1, 1_000_000)
    }

    @CommandAlias("control enablefloatingwhiledc")
    @CommandCompletion("true|false")
    fun onSettingsControlEnableFloatingWhileDc(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::floatWhileDc, enabled)
    }

    @CommandAlias("control reversedcspeedboostkey")
    @CommandCompletion("true|false")
    fun onSettingsControlReverseDcSpeedBoostKey(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::reverseDcBoost, enabled)
    }

    @CommandAlias("control toggledcspeedboostkey")
    @CommandCompletion("true|false")
    fun onSettingsControlToggleDcSpeedBoostKey(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::toggleDcBoost, enabled)
    }

    @CommandAlias("sidebar combattimer enablecombattimerinfo")
    @CommandCompletion("true|false")
    fun onSettingsSidebarCombatTimerEnableCombatTimerInfo(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::combatTimerEnabled, enabled)
    }

    @CommandAlias("sidebar starships enablestarshipinfo")
    @CommandCompletion("true|false")
    fun onSettingsSidebarStarshipsEnableStarshipInfo(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::starshipsEnabled, enabled)
    }

    @CommandAlias("sidebar starships displayadvancedinfo")
    @CommandCompletion("true|false")
    fun onSettingsSidebarStarshipsDisplayAdvancedInfo(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::advancedStarshipInfo, enabled)
    }

    @CommandAlias("sidebar starships rotatingcompass")
    @CommandCompletion("true|false")
    fun onSettingsSidebarStarshipsRotatingCompass(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::rotateCompass, enabled)
    }

    @CommandAlias("sidebar contacts enablecontactsinfo")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableContactsInfo(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::contactsEnabled, enabled)
    }

    @CommandAlias("sidebar contacts changecontactsrange")
    @CommandCompletion("0|6000")
    fun onSettingsSidebarContactsChangeContactsRange(sender: Player, value: Int) = asyncCommand(sender) {
        handleIntegerInputSetting(sender, PlayerSettings::contactsDistance, value, 0, MainSidebar.CONTACTS_RANGE)
    }

    @CommandAlias("sidebar contacts changemaxnamelength")
    @CommandCompletion("1|6000")
    fun onSettingsSidebarContactsChangeMaxNameLength(sender: Player, value: Int) = asyncCommand(sender) {
        handleIntegerInputSetting(sender, PlayerSettings::contactsMaxNameLength, value, 0, MainSidebar.MAX_NAME_LENGTH)
    }

    @CommandAlias("sidebar contacts changesortorder")
    @CommandCompletion("@contactsChangeSortOrder")
    fun onSettingsSidebarContactsChangeSortOrder(sender: Player, value: ContactsSidebar.ContactsSorting) = asyncCommand(sender) {
        handleEnumCycleSetting(sender, PlayerSettings::contactsSort, value, ContactsSidebar.ContactsSorting::class.java)
    }

    @CommandAlias("sidebar contacts changecoloring")
    @CommandCompletion("@contactsChangeColoring")
    fun onSettingsSidebarContactsChangeColoring(sender: Player, value: ContactsSidebar.ContactsColoring) = asyncCommand(sender) {
        handleEnumCycleSetting(sender, PlayerSettings::contactsColoring, value, ContactsSidebar.ContactsColoring::class.java)
    }

    @CommandAlias("sidebar contacts enablestarships")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableStarships(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::contactsStarships, enabled)
    }

    @CommandAlias("sidebar contacts enablelaststarship")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableLastStarship(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::lastStarshipEnabled, enabled)
    }

    @CommandAlias("sidebar contacts enableplanets")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnablePlanets(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::planetsEnabled, enabled)
    }

    @CommandAlias("sidebar contacts enablestars")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableStars(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::starsEnabled, enabled)
    }

    @CommandAlias("sidebar contacts enablebeacons")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableBeacons(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::beaconsEnabled, enabled)
    }

    @CommandAlias("sidebar contacts enablestations")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableStations(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::stationsEnabled, enabled)
    }

    @CommandAlias("sidebar contacts enablebookmarks")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableBookmarks(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::bookmarksEnabled, enabled)
    }

    @CommandAlias("sidebar contacts enableaistarships")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableAiStarships(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::relationAiEnabled, enabled)
    }

    @CommandAlias("sidebar contacts enablenorelationstarships")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableNoRelationStarships(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::relationNoneEnabled, enabled)
    }

    @CommandAlias("sidebar contacts enableenemystarships")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableEnemyStarships(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::relationEnemyEnabled, enabled)
    }

    @CommandAlias("sidebar contacts enableunfriendlystarships")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableUnfriendlyStarships(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::relationUnfriendlyEnabled, enabled)
    }

    @CommandAlias("sidebar contacts enableneutralstarships")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableNeutralStarships(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::relationNeutralEnabled, enabled)
    }

    @CommandAlias("sidebar contacts enablefriendlystarships")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableFriendlyStarships(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::relationFriendlyEnabled, enabled)
    }

    @CommandAlias("sidebar contacts enableallystarships")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableAllyStarships(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::relationAllyEnabled, enabled)
    }

    @CommandAlias("sidebar contacts enablenationstarships")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableNationStarships(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::relationNationEnabled, enabled)
    }

    @CommandAlias("sidebar contacts enableaistations")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableAiStations(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::relationAiStationEnabled, enabled)
    }

    @CommandAlias("sidebar contacts enablenorelationstations")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableNoRelationStations(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::relationNoneStationEnabled, enabled)
    }

    @CommandAlias("sidebar contacts enableenemystations")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableEnemyStations(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::relationEnemyStationEnabled, enabled)
    }

    @CommandAlias("sidebar contacts enableunfriendlystations")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableUnfriendlyStations(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::relationUnfriendlyStationEnabled, enabled)
    }

    @CommandAlias("sidebar contacts enableneutralstations")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableNeutralStations(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::relationNeutralStationEnabled, enabled)
    }

    @CommandAlias("sidebar contacts enablefriendlystations")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableFriendlyStations(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::relationFriendlyStationEnabled, enabled)
    }

    @CommandAlias("sidebar contacts enableallystations")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableAllyStations(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::relationAllyStationEnabled, enabled)
    }

    @CommandAlias("sidebar contacts enablenationstations")
    @CommandCompletion("true|false")
    fun onSettingsSidebarContactsEnableNationStations(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::relationNationStationEnabled, enabled)
    }

    @CommandAlias("sidebar route enablerouteinfo")
    @CommandCompletion("true|false")
    fun onSettingsSidebarRouteEnableRouteInfo(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::waypointsEnabled, enabled)
    }

    @CommandAlias("sidebar route routesegmentsenabled")
    @CommandCompletion("true|false")
    fun onSettingsSidebarRouteRouteSegmentsEnabled(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::compactWaypoints, enabled)
    }

    @CommandAlias("graphics hudicon toggleplanetselector")
    @CommandCompletion("true|false")
    fun onSettingsGraphicsHudIconTogglePlanetSector(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::hudPlanetsSelector, enabled)
    }

    @CommandAlias("graphics hudicon toggleplanetvisibility")
    @CommandCompletion("true|false")
    fun onSettingsGraphicsHudIconTogglePlanetVisibility(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::hudPlanetsImage, enabled)
    }

    @CommandAlias("graphics hudicon togglestarvisibility")
    @CommandCompletion("true|false")
    fun onSettingsGraphicsHudIconToggleStarVisibility(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::hudIconStars, enabled)
    }

    @CommandAlias("graphics hudicon togglebeaconvisibility")
    @CommandCompletion("true|false")
    fun onSettingsGraphicsHudIconToggleBeaconVisibility(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::hudIconBeacons, enabled)
    }

    @CommandAlias("graphics hudicon togglestationvisibility")
    @CommandCompletion("true|false")
    fun onSettingsGraphicsHudIconToggleStationVisibility(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::hudIconStations, enabled)
    }

    @CommandAlias("graphics hudicon togglebookmarkvisibility")
    @CommandCompletion("true|false")
    fun onSettingsGraphicsHudIconToggleBookmarkVisibility(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::hudIconBookmarks, enabled)
    }

    @CommandAlias("graphics effects displayentities")
    @CommandCompletion("@clientDisplayEntitiesVisibility")
    fun onSettingsGraphicsEffectsDisplayEntities(sender: Player, value: ClientDisplayEntities.Visibility) = asyncCommand(sender) {
        handleEnumCycleSetting(sender, PlayerSettings::displayEntityVisibility, value, ClientDisplayEntities.Visibility::class.java)
    }

    @CommandAlias("graphics effects togglealternativeshieldimpactparticles")
    @CommandCompletion("true|false")
    fun onSettingsGraphicsEffectsToggleAlternativeShieldImpactParticles(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::useAlternateShieldHitParticle, enabled)
    }

    @CommandAlias("graphics effects flareduration")
    @CommandCompletion("1|100")
    fun onSettingsGraphicsEffectsFlareDuration(sender: Player, value: Int) = asyncCommand(sender) {
        handleIntegerInputSetting(sender, PlayerSettings::flareTime, value, 1, 100)
    }

    @CommandAlias("sound enableadditionalsounds")
    @CommandCompletion("true|false")
    fun onSettingsSoundEnableAdditionalSounds(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::enableAdditionalSounds, enabled)
    }

    @CommandAlias("sound cruiseindicatorsound")
    @CommandCompletion("@cruiseIndicatorSound")
    fun onSettingsSoundCruiseIndicatorSound(sender: Player, value: SoundSettings.CruiseIndicatorSounds) = asyncCommand(sender) {
        handleEnumCycleSetting(sender, PlayerSettings::soundCruiseIndicator, value, SoundSettings.CruiseIndicatorSounds::class.java)
    }

    @CommandAlias("sound hitmarkeronhull")
    @CommandCompletion("true|false")
    fun onSettingsSoundHitmarkerOnHull(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::hitmarkerOnHull, enabled)
    }

    @CommandAlias("sound hitmarkeronshield")
    @CommandCompletion("true|false")
    fun onSettingsSoundHitmarkerOnShield(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::hitmarkerOnShield, enabled)
    }

    @CommandAlias("sound nearbyweaponsounds")
    @CommandCompletion("@audioRange")
    fun onSettingsSoundNearbyWeaponSounds(sender: Player, value: AudioRange) = asyncCommand(sender) {
        handleEnumCycleSetting(sender, PlayerSettings::nearbyWeaponSounds, value, AudioRange::class.java)
    }

    @CommandAlias("sound farweaponsounds")
    @CommandCompletion("@audioRange")
    fun onSettingsSoundFarWeaponSounds(sender: Player, value: AudioRange) = asyncCommand(sender) {
        handleEnumCycleSetting(sender, PlayerSettings::farWeaponSounds, value, AudioRange::class.java)
    }

    @CommandAlias("other enablecombattimeralerts")
    @CommandCompletion("true|false")
    fun onSettingsOtherEnableCombatTimerAlerts(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::enableCombatTimerAlerts, enabled)
    }

    @CommandAlias("other enableprotectionmessages")
    @CommandCompletion("true|false")
    fun onSettingsOtherEnableProtectionMessages(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::protectionMessagesEnabled, enabled)
    }

    @CommandAlias("other shortenchatmessages")
    @CommandCompletion("true|false")
    fun onSettingsOtherShortenChatMessages(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::shortenChatChannels, enabled)
    }

    @CommandAlias("other removeuserprefixes")
    @CommandCompletion("true|false")
    fun onSettingsOtherRemoveUserPrefixes(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::hideUserPrefixes, enabled)
    }

    @CommandAlias("other removeglobalprefixes")
    @CommandCompletion("true|false")
    fun onSettingsOtherRemoveGlobalPrefixes(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::hideGlobalPrefixes, enabled)
    }

    @CommandAlias("other showitemsearchitems")
    @CommandCompletion("true|false")
    fun onSettingsOtherShowItemsearchItems(sender: Player, @Optional enabled: Boolean?) = asyncCommand(sender) {
        handleBooleanToggleSetting(sender, PlayerSettings::showItemSearchItem, enabled)
    }

    @CommandAlias("other togglesitting")
    @CommandCompletion("true|false")
    fun onSettingsOtherToggleSitting(sender: Player, enabled: Boolean) = asyncCommand(sender) {
        luckPerms.userManager.modifyUser(sender.uniqueId) { user ->
            user.data().add(IonSitCommand.sitStateNode.toBuilder().value(enabled).build())
            sender.success("Set ToggleSitting to $enabled")
        }
    }

    private fun handleBooleanToggleSetting(sender: Player, db: KMutableProperty1<PlayerSettings, Boolean>, value: Boolean?) {
        val newValue = value ?: !PlayerSettingsCache.getSettingOrThrow(sender.slPlayerId, db)
        PlayerSettingsCache.updateSetting(sender.slPlayerId, db, newValue)
        sender.success("Set ${db.name} to $newValue")
    }

    private fun handleIntegerInputSetting(sender: Player, db: KMutableProperty1<PlayerSettings, Int>, value: Int, min: Int, max: Int) {
        val newValue = value.coerceIn(min, max)
        PlayerSettingsCache.updateSetting(sender.slPlayerId, db, newValue)
        sender.success("Set ${db.name} to $newValue")
    }

    private fun <T : Enum<T>> handleEnumCycleSetting(sender: Player, db: KMutableProperty1<PlayerSettings, Int>, value: T, enum: Class<T>) {
        if (enum.enumConstants.contains(value)) {
            PlayerSettingsCache.updateSetting(sender.slPlayerId, db, enum.enumConstants.indexOf(value))
        }
        sender.success("Set ${db.name} to ${value.name}")
    }
}
