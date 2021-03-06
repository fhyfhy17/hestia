package worlds.gregs.hestia.core.display.update.logic.sync

import com.artemis.*
import com.artemis.utils.BitVector
import com.nhaarman.mockitokotlin2.*
import net.mostlyoriginal.api.event.common.EventSystem
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.mockito.AdditionalMatchers.geq
import org.mockito.Matchers.anyBoolean
import org.mockito.Matchers.anyInt
import worlds.gregs.hestia.artemis.add
import worlds.gregs.hestia.artemis.bag.map.EntitySyncBag
import worlds.gregs.hestia.core.display.client.model.components.ClientIndex
import worlds.gregs.hestia.core.display.client.model.components.NetworkSession
import worlds.gregs.hestia.core.display.client.model.components.Viewport
import worlds.gregs.hestia.core.display.update.api.BlockFactory
import worlds.gregs.hestia.core.display.update.api.IndexSystem
import worlds.gregs.hestia.core.display.update.api.SyncFactory
import worlds.gregs.hestia.core.entity.entity.model.components.Position
import worlds.gregs.hestia.game.update.SyncStage
import worlds.gregs.hestia.game.update.UpdateBlock
import worlds.gregs.hestia.network.update.sync.Update
import java.util.*

abstract class SynchronizeTest<M : Update, T : SynchronizeSystem<M>> {

    abstract val sync: T
    internal lateinit var world: World
    internal lateinit var viewport: Viewport
    internal val entities = LinkedList<Int>()

    /*
        Setup
     */

    @BeforeEach
    private fun setup() {
        entities.clear()
    }

    internal fun world(vararg systems: BaseSystem): World {
        world = World(WorldConfigurationBuilder().with(sync, EventSystem(), *systems).build())
        val viewer = world.createEntity(ArchetypeBuilder().add(NetworkSession::class, Viewport::class, Position::class).build(world))
        viewport = viewer.getComponent(Viewport::class.java)
        world.process()
        return world
    }

    internal fun <T : Component> index(): IndexSystem<T> {
        val indexSystem = mock<IndexSystem<T>>()
        whenever(indexSystem.getId(anyInt())).thenAnswer {
            val entity = entities.getOrElse(it.arguments[0] as Int - 1) { -1 }
            if (entity != -1) {
                entity
            } else {
                null
            }
        }
        return indexSystem
    }


    internal fun emptyFactory(): SyncFactory<*> {
        val factory = mock<SyncFactory<SyncStage>>()
        whenever(factory.local).thenReturn(false)
        whenever(factory.npc).thenReturn(false)
        whenever(factory.active).thenReturn(false)
        return factory
    }

    internal inline fun <reified T : SyncStage> factory(local: Boolean, npc: Boolean): SyncFactory<T> {
        val factory = mock<SyncFactory<T>>()
        whenever(factory.local).thenReturn(local)
        whenever(factory.npc).thenReturn(npc)
        whenever(factory.active).thenReturn(true)
        factory.change(false)
        whenever(factory.create(anyInt(), anyInt(), anyBoolean())).thenReturn(if (T::class.objectInstance != null) T::class.objectInstance else T::class.java.getConstructor().newInstance())
        return factory
    }

    internal inline fun <reified T : UpdateBlock> block(added: Boolean, npc: Boolean, flag: Int): BlockFactory<T> {
        val factory = mock<BlockFactory<T>>()
        whenever(factory.added).thenReturn(added)
        whenever(factory.npc).thenReturn(npc)
        whenever(factory.flag).thenReturn(flag)
        whenever(factory.create(anyInt(), anyInt())).thenReturn(T::class.java.getConstructor().newInstance())
        return factory
    }

    internal fun BlockFactory<*>.active(active: Boolean) {
        val sub = mock<EntitySubscription>()
        whenever(subscription).thenReturn(sub)
        val ids = mock<BitVector>()
        whenever(sub.activeEntityIds).thenReturn(ids)
        whenever(ids.get(anyInt())).thenReturn(active)
    }

    internal fun SyncFactory<*>.change(change: Boolean) {
        whenever(change(eq(sync), any(), geq(1))).thenReturn(change)
    }

    internal fun addPlayers(direct: Boolean, vararg players: Int) {
        addEntities(viewport.localPlayers(), direct, *players)
    }

    internal fun addNpcs(direct: Boolean, vararg players: Int) {
        addEntities(viewport.localNpcs(), direct, *players)
    }

    private fun addEntities(bag: EntitySyncBag, direct: Boolean, vararg entities: Int) {
        entities.forEach {
            if (direct) {
                bag.set(it, this.entities.indexOf(it) + 1)
            } else {
                bag.insert(it, this.entities.indexOf(it) + 1)
            }
        }
        entities.forEach { entity ->
            val clientIndex = ClientIndex()
            clientIndex.index = this.entities.indexOf(entity) + 1
            world.getEntity(entity).edit().add(clientIndex)
        }
    }

    internal fun removePlayers(direct: Boolean, vararg players: Int) {
        removeEntities(viewport.localPlayers(), direct, *players)
    }

    internal fun removeNpcs(direct: Boolean, vararg players: Int) {
        removeEntities(viewport.localNpcs(), direct, *players)
    }

    private fun removeEntities(bag: EntitySyncBag, direct: Boolean, vararg entities: Int) {
        entities.forEach {
            if (direct) {
                bag.unset(this.entities.indexOf(it) + 1)
            } else {
                bag.remove(it, this.entities.indexOf(it) + 1)
            }
        }
    }

    /*
        Logic
     */

    internal fun tick() {
        world.process()
    }

    internal fun sync(): M {
        return sync.sync(0)
    }

    internal fun indexEntity(entity: Int): Int {
        entities.add(entity)
        return entity
    }

    /*
        Assertions
     */

    internal fun SyncFactory<*>.assertCreated(times: Int, vararg players: Int) {
        players.forEach {
            verify(this, times(times)).create(0, it, false)
        }
    }

    @Suppress("UNCHECKED_CAST")
    internal inline fun <reified T : SyncStage> M.assertStage(index: Int): T {
        assertThat(stages[index] as? T).isNotNull
        return stages[index] as T
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T : SyncStage> M.assertStages(range: IntRange) {
        range.forEach {
            assertThat(stages[it] as? T).isNotNull
        }
    }
}