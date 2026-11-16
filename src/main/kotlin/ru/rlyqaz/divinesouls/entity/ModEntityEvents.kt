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
import ru.rlyqaz.divinesouls.entity.custom.MeadowSlimeEntity
import ru.rlyqaz.divinesouls.entity.custom.SandyCrabEntity
import ru.rlyqaz.divinesouls.entity.custom.SorrowWraithEntity
import ru.rlyqaz.divinesouls.entity.custom.TwinWitherSkeletonEntity
import ru.rlyqaz.divinesouls.entity.custom.VoidDragonEntity

object ModEntityEvents {

    fun register(modEventBus: IEventBus) {
        // 1. Атрибуты (здоровье, урон, скорость)
        modEventBus.addListener { event: EntityAttributeCreationEvent ->
            event.put(ModEntities.VOID_DRAGON.get(), VoidDragonEntity.createAttributes().build())
            event.put(ModEntities.SORROW_WRAITH.get(), SorrowWraithEntity.createAttributes().build())
            event.put(ModEntities.TWIN_WITHER_SKELETON.get(), TwinWitherSkeletonEntity.createAttributes().build())
            event.put(ModEntities.MEADOW_SLIME.get(), MeadowSlimeEntity.createAttributes().build())
            event.put(ModEntities.SANDY_CRAB.get(), SandyCrabEntity.createAttributes().build())
        }

        // 2. Рендереры мобов
        modEventBus.addListener { event: EntityRenderersEvent.RegisterRenderers ->
            event.registerEntityRenderer(ModEntities.SORROW_WRAITH.get(), ::SorrowWraithRenderer)
            event.registerEntityRenderer(ModEntities.TWIN_WITHER_SKELETON.get(), ::TwinWitherSkeletonRenderer)
            event.registerEntityRenderer(ModEntities.MEADOW_SLIME.get(), ::MeadowSlimeRenderer)
            event.registerEntityRenderer(ModEntities.SANDY_CRAB.get(), ::SandyCrabRenderer)
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
        }
    }
}
