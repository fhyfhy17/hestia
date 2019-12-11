package worlds.gregs.hestia.core.plugins.client.systems.network.`in`

import net.mostlyoriginal.api.event.common.EventSystem
import worlds.gregs.hestia.GameServer
import worlds.gregs.hestia.artemis.events.IntegerEntered
import worlds.gregs.hestia.game.MessageHandlerSystem
import worlds.gregs.hestia.network.client.decoders.messages.IntegerEntry

class IntegerEntryHandler : MessageHandlerSystem<IntegerEntry>() {

    override fun initialize() {
        super.initialize()
        GameServer.gameMessages.bind(this)
    }

    private lateinit var es: EventSystem

    override fun handle(entityId: Int, message: IntegerEntry) {
        es.dispatch(IntegerEntered(entityId, message.integer))
    }

}