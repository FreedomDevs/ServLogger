package dev.elysium.servlogger.utils

import dev.elysium.servlogger.database.ServLoggerDatabase
import io.papermc.paper.command.CommandBlockHolder
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.Nameable
import org.bukkit.block.Banner
import org.bukkit.block.Beehive
import org.bukkit.block.Campfire
import org.bukkit.block.EndGateway
import org.bukkit.block.Lectern
import org.bukkit.block.ShulkerBox
import org.bukkit.block.TileState
import org.bukkit.material.Colorable
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

object TileStateSerializer {
    fun writeMyTLV(dos: DataOutputStream, id: Byte, data: ByteArray) {
        dos.writeByte(id.toInt())
        dos.writeShort(data.size)
        dos.write(data)
    }

    fun serializeComponent(component: Component): ByteArray {
        return GsonComponentSerializer.gson().serialize(component).toByteArray(Charsets.UTF_8)
    }

    @Suppress("UnstableApiUsage")
    fun serializeTileState(database: ServLoggerDatabase, state: TileState): ByteArray? {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)

        if (state is Nameable) {
            val customName = state.customName()

            if (customName != null)
                writeMyTLV(dos, 0x00, serializeComponent(customName))
        }

        if (state is Banner) {
            val bannerBos = ByteArrayOutputStream()
            val bannerDos = DataOutputStream(bannerBos)

            bannerDos.writeByte(state.baseColor.woolData.toInt())
            bannerDos.writeByte(state.numberOfPatterns())
            for (pattern in state.patterns) {
                bannerDos.writeByte(pattern.color.woolData.toInt())
                var key =
                    RegistryAccess.registryAccess().getRegistry(RegistryKey.BANNER_PATTERN).getKey(pattern.pattern)
                        .toString()
                if (key.startsWith("minecraft:"))
                    key = key.substring(10)

                bannerDos.writeByte(key.length)
                bannerDos.write(key.toByteArray(Charsets.UTF_8))
            }

            writeMyTLV(dos, 0x01, bannerBos.toByteArray())
        }

        if (state is Colorable) {
            if (state.color != null)
                writeMyTLV(dos, 0x02, byteArrayOf(state.color!!.woolData))
        }

        if (state is ShulkerBox) {
            if (state.color != null)
                writeMyTLV(dos, 0x02, byteArrayOf(state.color!!.woolData))
        }

        if (state is Beehive) {
            if (state.entityCount > 0)
                writeMyTLV(dos, 0x03, byteArrayOf(state.entityCount.toByte()))
        }

        if (state is Campfire) {
            val campfireStream = ByteArrayOutputStream()
            val campfireDos = DataOutputStream(campfireStream)
            for (slot in 0 until 4) {
                val item = state.getItem(slot)

                if (item != null && !item.type.isAir) {
                    campfireDos.writeBoolean(true)

                    val itemBytes = item.serializeAsBytes()
                    campfireDos.writeInt(itemBytes.size)
                    campfireDos.write(itemBytes)

                    campfireDos.writeShort(state.getCookTime(slot))
                    campfireDos.writeShort(state.getCookTimeTotal(slot))
                } else {
                    campfireDos.writeBoolean(false)
                }
            }

            writeMyTLV(dos, 0x04, campfireStream.toByteArray())
        }

        if (state is CommandBlockHolder) {
            if (!state.command.isEmpty())
                writeMyTLV(dos, 0x05, state.command.toByteArray(Charsets.UTF_8))
        }

        if (state is EndGateway) {
            val endGatewayStream = ByteArrayOutputStream()
            val endGatewayDos = DataOutputStream(endGatewayStream)

            endGatewayDos.writeLong(state.age)
            endGatewayDos.writeBoolean(state.isExactTeleport)

            if (state.exitLocation != null) {
                endGatewayDos.writeInt(state.exitLocation!!.blockX)
                endGatewayDos.writeInt(state.exitLocation!!.blockY)
                endGatewayDos.writeInt(state.exitLocation!!.blockZ)
                endGatewayDos.writeInt(state.exitLocation!!.blockZ)
                endGatewayDos.writeLong(database.worlds.getOrCreateByIdentifier(state.exitLocation!!.world.name))
            }

            writeMyTLV(dos, 0x06, endGatewayStream.toByteArray())
        }

        if (state is Lectern) {
            if (state.page > 1) {
                val lecternStream = ByteArrayOutputStream()
                val lecternDos = DataOutputStream(lecternStream)
                lecternDos.writeInt(state.page)
                writeMyTLV(dos, 0x07, lecternStream.toByteArray())
            }
        }

        // PersistentDataHolder
        val bytes = state.persistentDataContainer.serializeToBytes()
        if (!bytes.contentEquals(byteArrayOf(0x0A, 0x00, 0x00, 0x00))) {
            writeMyTLV(dos, 0x08, bytes)
        }


        // TODO Crafter
        // TODO CreatureSpawner
        // TODO TrialSpawner
        // TODO Sign
        // TODO Skull
        // TODO Structure
        // TODO Vault
        // TODO Furnace
        // TODO BrewingStand
        // TODO BrushableBlock

        if (bos.size() == 0) return null
        return bos.toByteArray()
    }
}