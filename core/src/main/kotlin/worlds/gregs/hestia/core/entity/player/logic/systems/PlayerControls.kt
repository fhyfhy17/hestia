package worlds.gregs.hestia.core.entity.player.logic.systems

import com.artemis.Entity
import worlds.gregs.hestia.core.display.update.model.components.UpdateClanMember

class PlayerControls
/**
 * Player clan chat
 */
fun Entity.updateClanChat(sameClan: Boolean) {
    edit().add(UpdateClanMember(sameClan))
}