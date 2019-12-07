package worlds.gregs.hestia.core.plugins.movement.components

import com.artemis.Component
import com.artemis.annotations.EntityId
import com.artemis.annotations.PooledWeaver

@PooledWeaver
class RunToggled : Component() {
    @EntityId
    @JvmField
    var entity = -1
}