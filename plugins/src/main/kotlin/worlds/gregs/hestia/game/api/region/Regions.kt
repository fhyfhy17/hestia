package worlds.gregs.hestia.game.api.region

import com.artemis.Aspect
import worlds.gregs.hestia.game.api.SubscriptionSystem

/**
 * Regions
 * Holds list of regions
 */
abstract class Regions(builder: Aspect.Builder) : SubscriptionSystem(builder) {

    /**
     * Returns the id of the region with [regionId]
     * @param regionId The region to find
     * @return The id of the region's entity
     */
    abstract fun getEntityId(regionId: Int): Int?

    /**
     * Checks if region [regionId] exists
     * @param regionId The region to find
     * @return Whether entity with [regionId] has been created
     */
    open fun contains(regionId: Int): Boolean {
        return false
    }

}