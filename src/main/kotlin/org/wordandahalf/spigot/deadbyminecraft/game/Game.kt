package org.wordandahalf.spigot.deadbyminecraft.game

import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.wordandahalf.spigot.deadbyminecraft.DeadByMinecraft
import org.wordandahalf.spigot.deadbyminecraft.game.player.DeadByMinecraftPlayer
import org.wordandahalf.spigot.deadbyminecraft.game.player.roles.killer.KillerRole
import org.wordandahalf.spigot.deadbyminecraft.game.states.GameState
import org.wordandahalf.spigot.deadbyminecraft.game.states.LobbyState
import org.wordandahalf.spigot.deadbyminecraft.game.worlds.Worlds

/**
 * Represents a game of Dead by Minecraft and all associated data.
 */
class Game(val id: Int, val maxPlayers: Int)
{
    val players : ArrayList<DeadByMinecraftPlayer> = ArrayList(maxPlayers)

    val lobbyWorld = Worlds.cloneLobby()
    val gameWorld = Worlds.cloneGame()

    var state : GameState = LobbyState(this)
        set(newState)
        {
            field.onLeave()
            field = newState
        }

    fun stop()
    {
        state.onLeave()
        removeAllPlayers()
    }

    //
    // Player management functions
    //

    fun addPlayer(player: DeadByMinecraftPlayer) : Boolean
    {
        if(!players.contains(player))
        {
            state.onPlayerJoin(player)
            sendMessage(Component.text().content(player.bukkit.displayName + " has joined the game!").color(TextColor.fromHexString("E8E8E8")).build())
            players.add(player)
            player.data.gameID = id
            // player.saveData()
            return true
        }

        return false
    }

    fun removeAllPlayers()
    {
        val it = players.iterator()
        while(it.hasNext())
        {
            val player = it.next()

            state.onPlayerLeave(player)
            player.data.gameID = null
            // player.deleteData()
            it.remove()
        }
    }

    fun removePlayer(player: DeadByMinecraftPlayer) : Boolean
    {
        if(players.contains(player))
        {
            state.onPlayerLeave(player)
            sendMessage(Component.text().content(player.bukkit.displayName + " has left the game!").color(TextColor.fromHexString("E8E8E8")).build())
            player.data.gameID = null
            // player.deleteData()
            players.remove(player)
            return true
        }

        return false
    }

    fun hasKiller() : Boolean
    {
        return players.any { it.data.role is KillerRole }
    }

    fun numberOfPlayers() : Int { return players.size }

    //
    // World functions
    //

    fun sendMessage(message: Component)
    {
        players.forEach {
            DeadByMinecraft.Audience.player(it.bukkit).sendMessage(Identity.nil(), message)
        }
    }
}