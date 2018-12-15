package worlds.gregs.hestia.network.update.mob

import com.artemis.ComponentMapper
import worlds.gregs.hestia.game.plugins.entity.components.update.CombatLevel
import worlds.gregs.hestia.game.update.UpdateEncoder
import world.gregs.hestia.core.network.packets.Packet

class MobCombatLevelMask(private val combatLevelMapper: ComponentMapper<CombatLevel>) : UpdateEncoder {

    override val encode: Packet.Builder.(Int, Int) -> Unit = { _, other ->
        writeLEShort(combatLevelMapper.get(other)?.level ?: 0)
    }

}