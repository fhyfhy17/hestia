package worlds.gregs.hestia.core.display.interfaces.logic.systems

import com.artemis.ComponentMapper
import net.mostlyoriginal.api.event.common.EventSystem
import worlds.gregs.hestia.artemis.send
import worlds.gregs.hestia.core.display.interfaces.api.ContextMenus
import worlds.gregs.hestia.core.display.interfaces.model.PlayerOptions
import worlds.gregs.hestia.core.display.request.model.components.ContextMenu
import worlds.gregs.hestia.network.client.encoders.messages.PlayerContextMenuOption

class ContextMenuSystem : ContextMenus() {

    private lateinit var contextMenuMapper: ComponentMapper<ContextMenu>
    private lateinit var es: EventSystem

    override fun inserted(entityId: Int) {
        setOption(entityId, PlayerOptions.FOLLOW)
        setOption(entityId, PlayerOptions.TRADE)
        setOption(entityId, PlayerOptions.ASSIST)
    }

    override fun setOption(entityId: Int, option: PlayerOptions): ContextMenuResult {
        val contextMenu = contextMenuMapper.get(entityId)
        val current = contextMenu.options.getOrNull(option.slot)
        //If empty slot or slot matches
        return if (current == null && option.slot in contextMenu.options.indices || current == option) {
            contextMenu.options[option.slot] = option
            es.send(entityId, PlayerContextMenuOption(option.string, option.slot, option.top))
            ContextMenuResult.Success
        } else {
            ContextMenuResult.SlotInUse
        }
    }

    override fun removeOption(entityId: Int, option: PlayerOptions) {
        val contextMenu = contextMenuMapper.get(entityId)
        contextMenu.options[option.slot] = null
    }

    override fun hasOption(entityId: Int, option: PlayerOptions): Boolean {
        val contextMenu = contextMenuMapper.get(entityId)
        return contextMenu.options[option.slot] == option
    }
}