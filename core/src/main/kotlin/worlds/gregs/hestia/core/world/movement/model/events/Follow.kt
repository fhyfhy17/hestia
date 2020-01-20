package worlds.gregs.hestia.core.world.movement.model.events

import worlds.gregs.hestia.artemis.InstantEvent
import worlds.gregs.hestia.core.action.model.EntityAction

data class Follow(val target: Int) : EntityAction(), InstantEvent