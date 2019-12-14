package worlds.gregs.hestia.core.world.movement.logic.systems.calc

import com.artemis.ComponentMapper
import worlds.gregs.hestia.artemis.SubscriptionSystem
import worlds.gregs.hestia.core.display.update.model.components.direction.Watch
import worlds.gregs.hestia.core.entity.entity.model.components.Position
import worlds.gregs.hestia.core.entity.entity.model.components.Size
import worlds.gregs.hestia.core.entity.entity.model.components.height
import worlds.gregs.hestia.core.entity.entity.model.components.width
import worlds.gregs.hestia.core.world.movement.logic.systems.calc.StepBesideSystem.Companion.withinContact
import worlds.gregs.hestia.core.world.movement.model.components.calc.Beside
import worlds.gregs.hestia.core.world.movement.model.components.calc.Follow
import worlds.gregs.hestia.service.Aspect

/**
 * FollowSubscriptionSystem
 * Adds/removes watching the entity being followed when [Follow] is inserted/removed
 */
class FollowSubscriptionSystem : SubscriptionSystem(Aspect.all(Position::class, Follow::class)) {
    private lateinit var followMapper: ComponentMapper<Follow>
    private lateinit var watchMapper: ComponentMapper<Watch>
    private lateinit var besideMapper: ComponentMapper<Beside>
    private lateinit var positionMapper: ComponentMapper<Position>
    private lateinit var sizeMapper: ComponentMapper<Size>

    override fun inserted(entityId: Int) {
        //Watch
        val follow = followMapper.get(entityId)
        watchMapper.create(entityId).entity = follow.entity

        val position = positionMapper.get(entityId)
        val targetPosition = positionMapper.get(follow.entity)

        val width = sizeMapper.width(follow.entity)
        val height = sizeMapper.height(follow.entity)

        if(withinContact(position.x, position.y, targetPosition.x, targetPosition.y, width, height)) {
            return
        }

        //Walk beside (if not already)
        besideMapper.create(entityId).apply {
            this.x = targetPosition.x
            this.y = targetPosition.y
            this.sizeX = width
            this.sizeY = height
            this.calculate = true
            this.beside = true
        }
    }

    override fun removed(entityId: Int) {
        watchMapper.create(entityId).entity = -1
    }
}