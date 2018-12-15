package worlds.gregs.hestia.game.plugins.client.systems.network.`in`

import net.mostlyoriginal.api.event.common.EventSystem
import world.gregs.hestia.core.network.packets.Packet
import world.gregs.hestia.core.network.packets.PacketOpcode
import world.gregs.hestia.core.network.packets.PacketSize
import worlds.gregs.hestia.game.PacketHandler
import worlds.gregs.hestia.game.events.CreateBot
import worlds.gregs.hestia.game.events.CreateMob
import worlds.gregs.hestia.game.events.schedule
import worlds.gregs.hestia.game.plugins.core.components.map.Position
import worlds.gregs.hestia.game.plugins.entity.components.update.CombatLevel
import worlds.gregs.hestia.game.plugins.entity.components.update.DisplayName
import worlds.gregs.hestia.game.plugins.entity.components.update.ForceMovement
import worlds.gregs.hestia.game.plugins.entity.systems.*
import worlds.gregs.hestia.game.plugins.mob.component.update.UpdateCombatLevel
import worlds.gregs.hestia.game.plugins.mob.component.update.UpdateDisplayName
import worlds.gregs.hestia.game.plugins.mob.systems.change
import worlds.gregs.hestia.game.plugins.movement.components.RunToggled
import worlds.gregs.hestia.game.plugins.movement.components.calc.Navigate
import worlds.gregs.hestia.game.plugins.player.component.update.PlayerMiniMapDot
import worlds.gregs.hestia.game.plugins.player.component.update.UpdateMovement
import worlds.gregs.hestia.game.plugins.player.component.update.UpdateUnknown
import worlds.gregs.hestia.game.plugins.player.component.update.appearance.Hidden
import worlds.gregs.hestia.game.plugins.player.systems.updateClanChat
import worlds.gregs.hestia.game.plugins.region.systems.RegionBuilderSystem
import worlds.gregs.hestia.game.update.Marker
import worlds.gregs.hestia.network.login.Packets
import worlds.gregs.hestia.services.*

@PacketSize(-1)
@PacketOpcode(Packets.COMMAND)
class CommandHandler : PacketHandler() {

    private lateinit var es: EventSystem

    override fun handle(entityId: Int, packet: Packet, length: Int) {
        packet.readUnsignedByte()//client command
        packet.readUnsignedByte()
        val command = packet.readString()
        val parts = command.split(" ")
        handle(entityId, command, parts)
    }

    private fun handle(entityId: Int, command: String, parts: List<String>) {

        val entity = world.getEntity(entityId)

        if (command.contains(",")) {
            val params = parts[1].split(",")
            val plane = params[0].toInt()
            val x = params[1].toInt() shl 6 or params[3].toInt()
            val y = params[2].toInt() shl 6 or params[4].toInt()
            entity.move(x, y, plane)
            return
        }

        println("Command ${parts[0]}")
        when (parts[0]) {
            "tele", "tp" -> {
                entity.move(parts[1].toInt(), parts[2].toInt(), if(parts.size > 3) parts[3].toInt() else 0)
            }
            "dy" -> {
                val position = entity.getComponent(Position::class)!!
                val builder = world.getSystem(RegionBuilderSystem::class)
//                dynamicRegionSystem.create(position.regionId)
//                if(regionSystem.toggle(position.regionId)) {
//                builder.reset(position.regionId)
                builder.clear(position.regionId)
//                builder.set(position.chunkX, position.chunkY, 0, position.chunkX, position.chunkY, 0, parts[1].toInt())
                builder.set(position.chunkX, position.chunkY, 0, position.chunkX, position.chunkY, 0, 1, 2, parts[1].toInt())
//                regionSystem.changeArea(position.chunkX + 1, position.chunkY - 1, 0, position.chunkX - 1, position.chunkY - 1, 0, 3, 2, 3)
//                    regionSystem.changeRegion(position.regionId, position.regionId, 3)
//                }
                if(builder.build(position.regionId)) {
                    entity.updateMapRegion(false, true)
                } else {
                    println("Failed to load region ${position.regionId}")
                }

            }
            //Player updating flags
            "batch" -> {
                entity.batchAnim()
            }
            "hide" -> {
                entity.edit().toggle(Hidden())
                entity.updateAppearance()
            }
            "colour" -> {
                //70 110 90 130 green used for kalphite king
                entity.colour(parts[1].toInt(), parts[2].toInt(), parts[3].toInt(), parts[4].toInt(), 1)
            }
            "bar" -> {
                entity.time()
            }
            "clan" -> {
                world.players().forEach {
                    world.getEntity(it).updateClanChat(true)
                }
            }
            "uk" -> {
                entity.edit().add(UpdateUnknown())
            }
            "p" -> {
                world.players().forEach {
                    val other = world.getEntity(it)
                    other.edit().add(PlayerMiniMapDot(parts[1].toBoolean()))
                }
            }
            "model" -> {
                val mob = world.getEntity(world.mobs().first())
                mob.change()
//                mob.change(intArrayOf(390, 456, 332, 326, 151, 177, 12138, 181), intArrayOf(10508, -10342, 4550))
            }
            "party" -> {
                var count = 0
                /*world.players().filterNot { it == entity.id }.forEach {
                    world.delete(it)
                }
                world.mobs().forEach {
                    world.delete(it)
                }*/
                for (y in 3482 until 3518) {
                    for (x in 3070 until 3104) {
                        if ((x + y).rem(2) == 0) {
                            es.dispatch(CreateMob(1, x, y))
                        } else {
                            es.dispatch(CreateBot("Bot ${count++}", x, y))
                        }
                    }
                }
            }
            "anim" -> {
                entity.animate(parts[1].toInt())//863
            }
            "gfx" -> {
                entity.graphic(parts[1].toInt())//93
            }
            "transform" -> {
                val id = parts[1].toInt()
                if (id < 0) {
                    entity.updateAppearance()
                } else {
                    entity.transform(parts[1].toInt())
                }
            }
            "bot" -> {
                val position = entity.getComponent(Position::class)!!
                var count = 0
                /*for (y in (3482 until 3518)) {
                    for (x in 3070 until 3104) {
                        es.dispatch(CreateBot("Bot ${count++}", x, y))
                    }
                }*/
                es.dispatch(CreateBot("Bot ${count++}", position.x, position.y - 1))
            }
            "b" -> {
                val bot = world.getEntity(world.players().last())
                /*bot.edit().add(Hidden())
                bot.updateAppearance()*/
                world.deleteEntity(bot)
            }
            "mob" -> {
                val position = entity.getComponent(Position::class)!!
                for (y in (3482 until 3518)) {
                    for (x in 3070 until 3104) {
                        es.dispatch(CreateMob(1, x, y))
                    }
                }
                /*entity.schedule(4, 0) {
                    world.mobs().forEachIndexed { index, it ->
                        val displayName = DisplayName()
                        displayName.name = "Mob ${index + 1} ${3482 + index}"
                        world.getEntity(it).edit().add(displayName).add(UpdateDisplayName())
                    }
                    stop()
                }*/
            }
            "m" -> {
                val mob = world.getEntity(world.mobs().first())
//                val position = mob.getComponent(Position::class)!!
//                mob.navigate(position.x + 1, position.y + 2)
//                mob.animate(2312)
//                mob.transform(mob.getComponent(Type::class)?.id ?: 0)
//                mob.colour(70, 110, 90, 130, duration = 60)
                world.delete(world.mobs().first())
            }
            "hit" -> {
                for (i in 0 until 5)
                    entity.hit(10, Marker.MELEE)
            }
            "force" -> {
                val position = entity.getComponent(Position::class)!!
                val target = Position.clone(position)
                target.y += 1
                entity.force(1, target, 2, ForceMovement.SOUTH)
//                entity.force(p1, 1, SOUTH, target, 4)
            }
            "chat" -> {
                entity.force("Hello")
            }
            "pos" -> {
                val position = entity.getComponent(Position::class)
                println("${position?.x ?: -1}, ${position?.y ?: -1}, ${position?.plane ?: -1}")
            }
            "move" -> {
                val position = entity.getComponent(Position::class)!!
                val move = Navigate()
                move.x = position.x + 1
                move.y = position.y - 2
                entity.edit()?.add(move)
            }
            "run" -> {
                entity.edit().toggle(RunToggled()).add(UpdateMovement())
            }
            "turn" -> {
                entity.turn(-1, 0)
            }
            "face" -> {
                val position = entity.getComponent(Position::class)!!
                entity.face(position.x, position.y - 1)
            }
            "watch" -> {
                entity.watch(1)
            }
            //Mob updating flags
            "mobdemo" -> {
                val mob = world.getEntity(world.mobs().first())
                world.schedule(0, 1) {
                    when (tick) {
                        0 -> {
                            mob.force("Graphic")
                            mob.graphic(40)
                            mob.graphic(60)
                            mob.graphic(80)
                            mob.graphic(110)
                        }
                        1 -> {
                            mob.force("Watch entity")
                            mob.watch(entity.id)
                        }
                        2 -> {
                            mob.force("Hits")
                            mob.watch(-1)
                            mob.hit(10)
                        }
                        3 -> {
                            mob.force("Time bar")
                            mob.time(increment = 1)
                        }
                        4 -> {
                            mob.force("Display name")
                            mob.edit().add(DisplayName("Manly Man")).add(UpdateDisplayName())
                        }
                        5 -> {
                            mob.force("Transform")
                            mob.transform(0)
                        }
                        6 -> {
                            mob.transform(-1)
                            mob.force("Force chat")
                        }
                        7 -> {
                            mob.force("Face direction")
                            mob.face(0, -1)
                        }
                        8 -> {
                            mob.force("Combat level")
                            val update = CombatLevel()
                            update.level = 1000
                            mob.edit().add(update).add(UpdateCombatLevel())
                        }
                        9 -> {
                            mob.force("Force movement")
                            val position = mob.getComponent(Position::class)!!
                            val target = Position.clone(position)
                            target.y -= 1
                            mob.force(1, target, 2, ForceMovement.SOUTH)
                        }
                        11 -> {
                            mob.force("Animations")
                            mob.animate(855)
                            mob.animate(856)
                            mob.animate(857)
                            mob.animate(858)
                        }
                        12 -> {
                            mob.force("Change models & colours")
                            mob.change(intArrayOf(390, 456, 332, 326, 151, 177, 12138, 181), intArrayOf(10508, -10342, 4550))
                        }
                        13 -> {
                            mob.force("Colour overlay")
                            mob.colour(70, 110, 90, 130, duration = 60)
                        }
                        14 -> {
                            mob.force("Finish")
                            mob.change()
                        }
                        15 -> {
                            stop()
                        }
                    }
                }
            }
            "botdemo" -> {
                val bot = world.getEntity(world.players()[1])
                world.schedule(0, 1) {
                    when (tick) {
                        0 -> {
                            bot.force("Animation")
                            bot.animate(855)
                            bot.animate(856)
                            bot.animate(857)
                            bot.animate(858)
                        }
                        1 -> {
                            bot.force("Graphic")
                            bot.graphic(40)
                            bot.graphic(60)
                            bot.graphic(80)
                            bot.graphic(110)
                        }
                        2 -> {
                            bot.force("Colour overlay")
                            bot.colour(70, 110, 90, 130, duration = 60)
                        }
                        3 -> {
                            bot.force("Time bar")
                            bot.time(increment = 1)
                        }
                        4 -> {
                            bot.force("Clan chat member")
                            entity.updateClanChat(true)
                        }
                        5 -> {
                            bot.force("Hits")
                            bot.hit(10)
                        }
                        6 -> {
                            bot.force("Appearance")
                            bot.updateAppearance()
                        }
                        7 -> {
                            bot.force("Force chat")
                        }
                        8 -> {
                            bot.force("Watch entity")
                            bot.watch(entity.id)
                        }
                        9 -> {
                            bot.force("Force movement")
                            bot.watch(-1)
                            val position = bot.getComponent(Position::class)!!
                            val target = Position.clone(position)
                            target.y -= 1
                            bot.force(1, target, 2, ForceMovement.SOUTH)
                        }
                        10 -> {
                            bot.force("Face")
                            bot.turn(-1, 0)
                        }
                    }
                }
            }
        }
    }
}