package worlds.gregs.hestia.game.plugins.region.systems.load

import com.artemis.ComponentMapper
import com.artemis.annotations.Wire
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import worlds.gregs.hestia.game.api.SubscriptionSystem
import worlds.gregs.hestia.game.api.land.LandObjects
import worlds.gregs.hestia.game.api.map.Map
import worlds.gregs.hestia.game.api.map.MapSettings
import worlds.gregs.hestia.game.api.region.Dynamic
import worlds.gregs.hestia.game.plugins.core.components.map.Chunk.getChunkX
import worlds.gregs.hestia.game.plugins.core.components.map.Chunk.getChunkY
import worlds.gregs.hestia.game.plugins.core.components.map.Chunk.getRotatedChunkPlane
import worlds.gregs.hestia.game.plugins.core.components.map.Chunk.getRotatedChunkRotation
import worlds.gregs.hestia.game.plugins.core.components.map.Chunk.getRotatedChunkX
import worlds.gregs.hestia.game.plugins.core.components.map.Chunk.getRotatedChunkY
import worlds.gregs.hestia.game.plugins.core.systems.cache.CacheSystem
import worlds.gregs.hestia.game.plugins.region.components.Loaded
import worlds.gregs.hestia.game.plugins.region.components.Loading
import worlds.gregs.hestia.game.plugins.region.components.RegionIdentifier
import worlds.gregs.hestia.services.Aspect

/**
 * RegionFileSystem
 * Loads the maps from the cache and passes the data off to [MapSettings] & [LandObjects]
 */
@Wire(failOnNull = false)
class RegionFileSystem : SubscriptionSystem(Aspect.all(RegionIdentifier::class, Loading::class)) {

    private var cache: CacheSystem? = null
    private var settings: MapSettings? = null
    private var objects: LandObjects? = null
    private var dynamic: Dynamic? = null
    private lateinit var regionMapper: ComponentMapper<RegionIdentifier>
    private lateinit var loadingMapper: ComponentMapper<Loading>
    private lateinit var loadedMapper: ComponentMapper<Loaded>

    private var map: Map? = null

    private val logger = LoggerFactory.getLogger(RegionFileSystem::class.java)

    override fun inserted(entityId: Int) {
        //Clear any old clipping data
        map?.unload(entityId)
        val region = regionMapper.get(entityId)
        //Load async
        GlobalScope.launch {
            loadingMapper.remove(entityId)
            load(entityId, region.id)
            loadedMapper.create(entityId)
        }
    }

    /**
     * Load clipping for region [regionId]
     * @param entityId Entity id of the region
     * @param regionId Location id of the region
     */
    private fun load(entityId: Int, regionId: Int) {
        val regionX = regionId shr 8
        val regionY = regionId and 0xff
        //Calculate region coordinates
        val x = regionX * 64
        val y = regionY * 64

        if (dynamic?.isDynamic(entityId) == false) {
            //Load static region
            load(entityId, x, y, regionX, regionY)
        } else {
            //Load dynamic region
            val dynamic = dynamic?.get(entityId)!!
            //For each chunk which need reloading
            dynamic.regionData.filter { dynamic.reloads.contains(it.key) }.forEach { chunk, shift ->
                //Doesn't need reloading anymore
                dynamic.reloads.remove(chunk)
                //Get the data of the chunk we are copying
                val newChunkX = getRotatedChunkX(shift)
                val newChunkY = getRotatedChunkY(shift)
                val plane = getRotatedChunkPlane(shift)
                val rotation = getRotatedChunkRotation(shift)

                //Calculate the location of the new chunks region
                val newRegionX = newChunkX / 8
                val newRegionY = newChunkY / 8

                //Get position of the chunk we are replacing and calculate the local chunk position (in the region) in tiles
                val chunkX = (getChunkX(chunk) % 8) * 8
                val chunkY = (getChunkY(chunk) % 8) * 8

                //Load dynamic region
                load(entityId, x, y, newRegionX, newRegionY, rotation, chunkX, chunkY, plane)
            }
        }
    }

    /**
     * Loads clipping & objects from map files
     */
    private fun load(entityId: Int, x: Int, y: Int, regionX: Int, regionY: Int, rotation: Int? = null, chunkX: Int? = null, chunkY: Int? = null, chunkPlane: Int? = null) {
        val index = cache?.getIndex(5)
        //Get the archive id's for the regions
        val landIndex = index?.getArchiveId("l${regionX}_$regionY") ?: -1
        val mapIndex = index?.getArchiveId("m${regionX}_$regionY") ?: -1

        //Make sure the cache has the necessary files
        if (landIndex == -1 || mapIndex == -1) {
            return
        }

        //Get the map files
        val landContainerData = index?.getFile(landIndex)
        val mapContainerData = index?.getFile(mapIndex)

        var decodedSettings: Array<Array<ByteArray>>? = null
        try {
            val settings = this.settings
            if (mapContainerData != null && settings != null) {
                //Load the clipping
                //TODO settings could be cached for better performance
                decodedSettings = settings.load(mapContainerData)
                settings.apply(entityId, decodedSettings, rotation, chunkX, chunkY, chunkPlane)
            }

        } catch (t: Throwable) {
            t.printStackTrace()
        }
        if (landContainerData != null) {
            //Load the objects
            objects?.load(entityId, x, y, landContainerData, decodedSettings, rotation, chunkX, chunkY, chunkPlane)
        } else {
            logger.warn("Missing xteas for region ${(regionX shl 8) + regionY}.")
        }
    }
}