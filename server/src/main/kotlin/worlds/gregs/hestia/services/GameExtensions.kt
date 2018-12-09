package worlds.gregs.hestia.services

import com.artemis.ArtemisPlugin
import com.artemis.BaseSystem
import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import com.artemis.utils.IntBag
import net.mostlyoriginal.api.event.common.EventSystem
import world.gregs.hestia.core.network.packets.Packet
import worlds.gregs.hestia.game.events.OutBoundPacket
import java.util.*
import kotlin.reflect.KClass

/*
    Kotlin extension helpers
 */

/*
    Artemis
 */
fun WorldConfigurationBuilder.dependsOn(vararg clazz: KClass<out ArtemisPlugin>): WorldConfigurationBuilder {
    return dependsOn(*clazz.map { it.java }.toTypedArray())
}

fun EventSystem.send(entity: Int, builder: Packet.Builder) {
    send(entity, builder.build())
}


fun EventSystem.send(entity: Int, packet: Packet) {
    dispatch(OutBoundPacket(entity, packet))
}

fun Int.nearby(size: Int): IntRange {
    return this - size .. this + size
}

fun IntBag.toArray(): IntArray {
    return Arrays.copyOf(data, size())
}

fun IntBag.forEach(function: (Int) -> Unit) {
    for(i in 0 until size()) {
        function(get(i))
    }
}

fun <T : BaseSystem>World.getSystem(type: KClass<T>) : T {
    return getSystem(type.java)
}