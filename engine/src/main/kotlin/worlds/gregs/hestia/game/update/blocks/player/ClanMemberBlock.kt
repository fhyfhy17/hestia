package worlds.gregs.hestia.game.update.blocks.player

import worlds.gregs.hestia.game.update.UpdateBlock

data class ClanMemberBlock(override val flag: Int, val sameClanChat: Boolean) : UpdateBlock