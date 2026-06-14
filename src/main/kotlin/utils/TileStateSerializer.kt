package dev.elysium.servlogger.utils

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Nameable
import org.bukkit.Registry
import org.bukkit.block.Banner
import org.bukkit.block.Beehive
import org.bukkit.block.Campfire
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
    fun serializeTileState(state: TileState) {
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
                val key = RegistryAccess.registryAccess().getRegistry(RegistryKey.BANNER_PATTERN).getKey(pattern.pattern).toString()
                bannerDos.writeByte(key.length)
                bannerDos.write(key.toByteArray(Charsets.UTF_8))
            }

            writeMyTLV(dos, 0x01, bannerBos.toByteArray())
        }

        if (state is Colorable) {
            if (state.color != null)
                writeMyTLV(dos, 0x02, byteArrayOf(state.color!!.woolData))
        }

        if (state is Beehive) {
            if (state.entityCount > 0)
                writeMyTLV(dos, 0x03, byteArrayOf(state.entityCount.toByte()))
        }

        if (state is Campfire) {
            val campfireStream = java.io.ByteArrayOutputStream()
            val campfireDos = java.io.DataOutputStream(campfireStream)
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
        }

        // Skip:
        // Furnace
        // BrewingStand
        // BrushableBlock
    }
}