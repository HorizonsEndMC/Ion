package net.starlegacy.listener.minigame

import net.starlegacy.listener.SLEventListener

object BedWarsListener : SLEventListener() {
/*    @EventHandler
    fun onJoinedLobby(event: BedwarsPlayerJoinedEvent) {
        val game = event.game
        val player = event.player

        val teams = game.availableTeams
        var team: Team? = null
        var minCount = Int.MAX_VALUE
        for (otherTeam in teams) {
            val connectedPlayers = game.connectedPlayers
            val count = connectedPlayers.count { otherPlayer ->
                val teamOfPlayer: RunningTeam? = game.getTeamOfPlayer(otherPlayer)
                teamOfPlayer?.name == otherTeam.name
            }

            if (count >= minCount) {
                continue
            }

            team = otherTeam
            minCount = count
        }

        if (team == null) {
            return
        }

        game.selectPlayerTeam(player, team)
    }*/
}
