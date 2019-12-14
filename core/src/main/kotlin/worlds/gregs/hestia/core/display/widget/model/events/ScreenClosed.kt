package worlds.gregs.hestia.core.display.widget.model.events

import worlds.gregs.hestia.core.display.widget.model.components.ScreenWidget
import worlds.gregs.hestia.artemis.InstantEvent

data class ScreenClosed(val entityId: Int, val screen: ScreenWidget) : InstantEvent