package net.starlegacy.feature.space_apartments

import org.bukkit.entity.Player
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

object ApartmentData {
    private fun getFile(): File {
        return File(SpaceApartments.getWorld().worldFolder, "data/space_apartments.dat")
    }

    private fun loadUsers(): LinkedHashSet<UUID> {
        val file = getFile()
        if (!file.exists()) {
            saveUsers(LinkedHashSet())
        }
        return readData(file)
    }

    private fun readData(file: File): LinkedHashSet<UUID> {
        val users = LinkedHashSet<UUID>()
        DataInputStream(FileInputStream(file)).use { stream ->
            val count = stream.readInt()
            repeat(count) {
                val mostSigBits = stream.readLong()
                val leastSigBits = stream.readLong()
                users.add(UUID(mostSigBits, leastSigBits))
            }
        }
        return users
    }

    private fun saveUsers(users: LinkedHashSet<UUID>) {
        val file = getFile()
        writeData(file, users)
    }

    private fun writeData(file: File, users: LinkedHashSet<UUID>) {
        DataOutputStream(FileOutputStream(file)).use { stream ->
            stream.writeInt(users.size)
            for (uuid in users) {
                stream.writeLong(uuid.mostSignificantBits)
                stream.writeLong(uuid.leastSignificantBits)
            }
        }
    }

    fun addUser(player: Player) {
        val users = loadUsers()
        users.add(player.uniqueId)
        saveUsers(users)
    }

    fun getIndex(player: Player): Int {
        val users = loadUsers()
        return users.indexOf(player.uniqueId)
    }

    fun contains(player: Player): Boolean {
        val users = loadUsers()
        return users.contains(player.uniqueId)
    }
}
