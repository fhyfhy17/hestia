package worlds.gregs.hestia.core.display.dialogue.logic.systems.types

import worlds.gregs.hestia.core.display.dialogue.logic.systems.EntityDialogue
import worlds.gregs.hestia.game.task.TaskScope
import worlds.gregs.hestia.service.wrap

data class PlayerDialogue(override val lines: List<String>, override val title: String?, val animation: Int) : EntityDialogue() {
    init {
        check(lines.size <= 4) { "Maximum player dialogue lines 4" }
    }
}

suspend fun TaskScope.player(text: String, anim: Int = -1, title: String? = null) {
    deferral = PlayerDialogue(text.wrap(), title, anim)
    defer()
}

/*
    Expressions
    9750-9753 - Head turning talking then shout (Shock)
    9757-9759 - Talking sad then look down (Sad)
    9760 - Talking sad (Sad)
    9761-9763 - Talking sad then slight shake of head (Sad)
    9764 - Talking sad low jaw (Sad)
    9765-9767 - Crying & wailing (Sad)
    9768 - Crying talking
    9769-9772 - Talking eyebrows down quivering lips (Concerned/Uncertain/about to cry)
    9773-9776 - Shocked talking while looking south/south-west
    9777-9780 - Shocked looking south
    9781-9784 - Angry gritting teeth
    9785-9788 - Angry talking through gritted teeth
    9789-9792 - Angry shouting
    9793 - Head banging
    9802 - Sleeping head tilted west
    9803 - Talking
    9804 - Blink
    9805-9808 - Blink then talk
    9810 - Talking
    9811-9813 - Shake head then look up (disagree then think)
    9814 - Shake head (Disagree)
    9827-9829 - Talking eyebrow raised then look down (Uncertain/Disregard)
    9830 - Talking eyebrow raised
    9832-9833 - Looks down then roles eyes (Disapproval/Disregard)
    9834 - Looking down from left to right while talking
    9835 - Rocking head expression changing from sad to happy
    9836-9839 - Looking side to side while talking (Suspicious)
    9840 - Chuckle
    9841 - Laughing head role
    9842 - Overly dramatic evil laugh
    9843-9846 - Talking slight head rock side to side
    9847-9850 - Talking eyebrows raised nodding
    9851-9854 - Laughing head nodding
    9877 - Crying talking eyes closed
    9878 - Yawn eyes role stick tongue out
*/