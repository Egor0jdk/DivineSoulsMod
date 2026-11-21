package ru.rlyqaz.divinesouls.entity

import net.minecraft.world.entity.SpawnPlacementTypes
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.level.levelgen.Heightmap
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent
import ru.rlyqaz.divinesouls.entity.client.MeadowSlimeRenderer
import ru.rlyqaz.divinesouls.entity.client.SandyCrabModel
import ru.rlyqaz.divinesouls.entity.client.SandyCrabRenderer
import ru.rlyqaz.divinesouls.entity.client.SorrowWraithModel
import ru.rlyqaz.divinesouls.entity.client.SorrowWraithRenderer
import ru.rlyqaz.divinesouls.entity.client.TwinWitherSkeletonModel
import ru.rlyqaz.divinesouls.entity.client.TwinWitherSkeletonRenderer
import ru.rlyqaz.divinesouls.entity.client.CaveGoblinModel
import ru.rlyqaz.divinesouls.entity.client.CaveGoblinRenderer
import ru.rlyqaz.divinesouls.entity.client.SwampSpiritModel
import ru.rlyqaz.divinesouls.entity.client.SwampSpiritRenderer
import ru.rlyqaz.divinesouls.entity.client.GiantWaspModel
import ru.rlyqaz.divinesouls.entity.client.GiantWaspRenderer
import ru.rlyqaz.divinesouls.entity.client.PoisonDaggerRenderer
import ru.rlyqaz.divinesouls.entity.client.IceElementalModel
import ru.rlyqaz.divinesouls.entity.client.IceElementalRenderer
import ru.rlyqaz.divinesouls.entity.client.IceShardRenderer
import ru.rlyqaz.divinesouls.entity.client.JungleSpiderRenderer
import ru.rlyqaz.divinesouls.entity.client.ForestEntModel
import ru.rlyqaz.divinesouls.entity.client.ForestEntRenderer
import ru.rlyqaz.divinesouls.entity.custom.MeadowSlimeEntity
import ru.rlyqaz.divinesouls.entity.custom.SandyCrabEntity
import ru.rlyqaz.divinesouls.entity.custom.SorrowWraithEntity
import ru.rlyqaz.divinesouls.entity.custom.TwinWitherSkeletonEntity
import ru.rlyqaz.divinesouls.entity.custom.VoidDragonEntity
import ru.rlyqaz.divinesouls.entity.custom.CaveGoblinEntity
import ru.rlyqaz.divinesouls.entity.custom.SwampSpiritEntity
import ru.rlyqaz.divinesouls.entity.custom.GiantWaspEntity
import ru.rlyqaz.divinesouls.entity.custom.IceElementalEntity
import ru.rlyqaz.divinesouls.entity.custom.JungleSpiderEntity
import ru.rlyqaz.divinesouls.entity.custom.ForestEntEntity

object ModEntityEvents {

    fun register(modEventBus: IEventBus) {
        // 1. Атрибуты (здоровье, урон, скорость)
        modEventBus.addListener { event: EntityAttributeCreationEvent ->
            event.put(ModEntities.VOID_DRAGON.get(), VoidDragonEntity.createAttributes().build())
            event.put(ModEntities.SORROW_WRAITH.get(), SorrowWraithEntity.createAttributes().build())
            event.put(ModEntities.TWIN_WITHER_SKELETON.get(), TwinWitherSkeletonEntity.createAttributes().build())
            event.put(ModEntities.MEADOW_SLIME.get(), MeadowSlimeEntity.createAttributes().build())
            event.put(ModEntities.SANDY_CRAB.get(), SandyCrabEntity.createAttributes().build())
            event.put(ModEntities.CAVE_GOBLIN.get(), CaveGoblinEntity.createAttributes().build())
            event.put(ModEntities.SWAMP_SPIRIT.get(), SwampSpiritEntity.createAttributes().build())
            event.put(ModEntities.GIANT_WASP.get(), GiantWaspEntity.createAttributes().build())
            event.put(ModEntities.ICE_ELEMENTAL.get(), IceElementalEntity.createAttributes().build())
            event.put(ModEntities.JUNGLE_SPIDER.get(), JungleSpiderEntity.createAttributes().build())
            event.put(ModEntities.FOREST_ENT.get(), ForestEntEntity.createAttributes().build())
        }

        // 2. Рендереры мобов
        modEventBus.addListener { event: EntityRenderersEvent.RegisterRenderers ->
            event.registerEntityRenderer(ModEntities.SORROW_WRAITH.get(), ::SorrowWraithRenderer)
            event.registerEntityRenderer(ModEntities.TWIN_WITHER_SKELETON.get(), ::TwinWitherSkeletonRenderer)
            event.registerEntityRenderer(ModEntities.MEADOW_SLIME.get(), ::MeadowSlimeRenderer)
            event.registerEntityRenderer(ModEntities.SANDY_CRAB.get(), ::SandyCrabRenderer)
            event.registerEntityRenderer(ModEntities.CAVE_GOBLIN.get(), ::CaveGoblinRenderer)
            event.registerEntityRenderer(ModEntities.SWAMP_SPIRIT.get(), ::SwampSpiritRenderer)
            event.registerEntityRenderer(ModEntities.GIANT_WASP.get(), ::GiantWaspRenderer)
            event.registerEntityRenderer(ModEntities.POISON_DAGGER.get(), ::PoisonDaggerRenderer)
            event.registerEntityRenderer(ModEntities.ICE_ELEMENTAL.get(), ::IceElementalRenderer)
            event.registerEntityRenderer(ModEntities.ICE_SHARD.get(), ::IceShardRenderer)
            event.registerEntityRenderer(ModEntities.JUNGLE_SPIDER.get(), ::JungleSpiderRenderer)
            event.registerEntityRenderer(ModEntities.FOREST_ENT.get(), ::ForestEntRenderer)
        }

        // 3. 3D-слои моделей
        modEventBus.addListener { event: EntityRenderersEvent.RegisterLayerDefinitions ->
            event.registerLayerDefinition(SorrowWraithModel.LAYER_LOCATION) {
                SorrowWraithModel.createBodyLayer()
            }
            event.registerLayerDefinition(TwinWitherSkeletonModel.LAYER_LOCATION) {
                TwinWitherSkeletonModel.createBodyLayer()
            }
            event.registerLayerDefinition(SandyCrabModel.LAYER_LOCATION) {
                SandyCrabModel.createBodyLayer()
            }
            event.registerLayerDefinition(CaveGoblinModel.LAYER_LOCATION) {
                CaveGoblinModel.createBodyLayer()
            }
            event.registerLayerDefinition(SwampSpiritModel.LAYER_LOCATION) {
                SwampSpiritModel.createBodyLayer()
            }
            event.registerLayerDefinition(GiantWaspModel.LAYER_LOCATION) {
                GiantWaspModel.createBodyLayer()
            }
            event.registerLayerDefinition(IceElementalModel.LAYER_LOCATION) {
                IceElementalModel.createBodyLayer()
            }
            event.registerLayerDefinition(ForestEntModel.LAYER_LOCATION) {
                ForestEntModel.createBodyLayer()
            }
        }

        // 4. Правила естественного спавна
        modEventBus.addListener { event: RegisterSpawnPlacementsEvent ->
            event.register(
                ModEntities.SORROW_WRAITH.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
            )
            event.register(
                ModEntities.MEADOW_SLIME.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MeadowSlimeEntity::checkMeadowSlimeSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
            )
            event.register(
                ModEntities.SANDY_CRAB.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SandyCrabEntity::checkSandyCrabSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
            )
            event.register(
                ModEntities.CAVE_GOBLIN.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                CaveGoblinEntity::checkCaveGoblinSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
            )
            event.register(
                ModEntities.SWAMP_SPIRIT.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SwampSpiritEntity::checkSwampSpiritSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
            )
            event.register(
                ModEntities.GIANT_WASP.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                GiantWaspEntity::checkGiantWaspSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
            )
            event.register(
                ModEntities.ICE_ELEMENTAL.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                IceElementalEntity::checkIceElementalSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
            )
            event.register(
                ModEntities.JUNGLE_SPIDER.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                JungleSpiderEntity::checkJungleSpiderSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
            )
            event.register(
                ModEntities.FOREST_ENT.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ForestEntEntity::checkForestEntSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
            )
        }
    }
}
