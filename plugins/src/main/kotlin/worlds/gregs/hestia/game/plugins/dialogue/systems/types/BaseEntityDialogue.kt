package worlds.gregs.hestia.game.plugins.dialogue.systems.types

import com.artemis.Entity
import worlds.gregs.hestia.api.dialogue.DialogueBase
import worlds.gregs.hestia.game.events.send
import worlds.gregs.hestia.network.game.out.InterfaceComponentText
import java.security.InvalidParameterException

abstract class BaseEntityDialogue(text: String) : BaseChatDialogue(text) {

    override fun send(entity: Entity, ui: DialogueBase) {
        val interfaceId = getInterfaceId()

        ui.openChatInterface(entity.id, interfaceId)

        val maxLines = getMaxLines()

        if (lines.size > maxLines) {
            throw InvalidParameterException("Maximum dialogue lines $maxLines")
        }

        if (title != null) {
            entity.send(InterfaceComponentText(interfaceId, getComponentStart(), title!!))
        }

        lines.forEachIndexed { index, text ->
            entity.send(InterfaceComponentText(interfaceId, getComponentStart() + index + 1, text))
        }
    }

    abstract fun getInterfaceId(): Int

    abstract fun getComponentStart(): Int

    abstract fun getMaxLines(): Int
}