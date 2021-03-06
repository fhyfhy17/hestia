package worlds.gregs.hestia.core.world.movement.api.types

import com.artemis.Aspect
import com.artemis.systems.IteratingSystem

/**
 * Walk
 */
abstract class Walk(builder: Aspect.Builder) : IteratingSystem(builder) {

    /**
     * @param entityId The entity to check
     * @return Whether the entity has a walk step
     */
    abstract fun hasStep(entityId: Int): Boolean

}