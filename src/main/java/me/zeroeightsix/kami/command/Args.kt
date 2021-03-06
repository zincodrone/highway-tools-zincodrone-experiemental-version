package me.zeroeightsix.kami.command

import me.zeroeightsix.kami.manager.managers.UUIDManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import org.kamiblue.capeapi.PlayerProfile
import org.kamiblue.command.AbstractArg
import org.kamiblue.command.AutoComplete
import org.kamiblue.command.DynamicPrefixMatch
import org.kamiblue.command.StaticPrefixMatch
import java.util.*
import kotlin.streams.toList

class ModuleArg(
    override val name: String
) : AbstractArg<Module>(), AutoComplete by StaticPrefixMatch(allAlias) {

    override suspend fun convertToType(string: String?): Module? {
        return ModuleManager.getModuleOrNull(string)
    }

    private companion object {
        val allAlias = ModuleManager.getModules().stream()
            .flatMap { Arrays.stream(it.alias) }
            .sorted()
            .toList()
    }

}

class BlockPosArg(
    override val name: String
) : AbstractArg<BlockPos>(), AutoComplete by DynamicPrefixMatch({ playerPosString?.let { listOf(it) } }) {

    override suspend fun convertToType(string: String?): BlockPos? {
        if (string == null) return null

        val splitInts = string.split(',').mapNotNull { it.toIntOrNull() }
        if (splitInts.size != 3) return null

        return BlockPos(splitInts[0], splitInts[1], splitInts[2])
    }

    private companion object {
        val playerPosString: String?
            get() = Wrapper.player?.position?.let { "${it.x},${it.y},${it.z}" }
    }

}

class BlockArg(
    override val name: String
) : AbstractArg<Block>(), AutoComplete by StaticPrefixMatch(allBlockNames) {

    override suspend fun convertToType(string: String?): Block? {
        if (string == null) return null
        return Block.getBlockFromName(string)
    }

    private companion object {
        val allBlockNames = ArrayList<String>().run {
            Block.REGISTRY.keys.forEach {
                add(it.toString())
                add(it.path)
            }
            sorted()
        }
    }
}

class ItemArg(
    override val name: String
) : AbstractArg<Item>(), AutoComplete by StaticPrefixMatch(allItemNames) {

    override suspend fun convertToType(string: String?): Item? {
        if (string == null) return null
        return Item.getByNameOrId(string)
    }

    private companion object {
        val allItemNames = ArrayList<String>().run {
            Item.REGISTRY.keys.forEach {
                add(it.toString())
                add(it.path)
            }
            sorted()
        }
    }

}

class PlayerArg(
    override val name: String
) : AbstractArg<PlayerProfile>(), AutoComplete by DynamicPrefixMatch(::playerInfoMap) {

    override suspend fun convertToType(string: String?): PlayerProfile? {
        return UUIDManager.getByString(string)
    }

    private companion object {
        val playerInfoMap: Collection<String>?
            get() {
                val playerInfoMap = Wrapper.minecraft.connection?.playerInfoMap ?: return null
                return playerInfoMap.stream()
                    .map { it.gameProfile.name }
                    .sorted()
                    .toList()
            }
    }

}