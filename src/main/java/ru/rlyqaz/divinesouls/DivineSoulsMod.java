package ru.rlyqaz.divinesouls;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib3.GeckoLib;

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.16.5 Forge)
 * ГЛАВНЫЙ КЛАСС: DivineSoulsMod (Ядро и глобальные триггеры)
 *
 * ИИ-КОНТЕКСТ ДЛЯ БУДУЩИХ МОДЕЛЕЙ:
 * - Мод является глобальным RPG-проектом с прогрессией Террария-стайла.
 * - ПРЕДМЕТЫ РАЗДЕЛЕНЫ: ModItems (ресурсы), ModWeapons (оружие), ModArmor (броня).
 * - Все они используют общую шину DeferredRegister<Item> ITEMS из этого класса.
 * - БЛОКИРОВКА ПРОГРЕССА: Ванильный Незер и ванильный Энд намертво заблокированы здесь.
 * ====================================================================
 */
@Mod(DivineSoulsMod.MOD_ID)
public class DivineSoulsMod {

    public static final String MOD_ID = "divinesouls";
    public static final Logger LOGGER = LogManager.getLogger();

    // Единые коробки регистрации для всего мода
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);

    public DivineSoulsMod() {
        GeckoLib.initialize(); // Активируем анимации Geckolib

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);

        // Инициализация разделенных модулей предметов
        ModItems.register();   // Ресурсы и BlockItems
        ModWeapons.register(); // Оружие и боевые инструменты
        ModArmor.register();   // Защитные сеты брони

        ModBlocks.register();  // Кастомные блоки и растительность

        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Divine Souls core initialized with modular architecture!");
    }

    // 1. БЛОКИРОВКА НЕЗЕРА: Огниво горит, но фиолетовые блоки портала не спавнятся
    @SubscribeEvent
    public void onPortalSpawn(BlockEvent.PortalSpawnEvent event) {
        event.setCanceled(true);
    }

    // 2. БЛОКИРОВКА ЭНДА: Предотвращаем ванильное появление космоса при 12-м глазе
    @SubscribeEvent
    public void onEndPortalActivate(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        BlockPos pos = event.getPos();

        // Проверяем, что игрок кликает Оком Края по пустой рамке портала
        if (!world.isClientSide() && event.getItemStack().getItem() == Items.ENDER_EYE) {
            if (world.getBlockState(pos).getBlock() == Blocks.END_PORTAL_FRAME &&
                    !world.getBlockState(pos).getValue(EndPortalFrameBlock.HAS_EYE)) {

                // Считаем, сколько глаз уже установлено в этой структуре
                int eyeCount = 0;
                for (int x = -5; x <= 5; x++) {
                    for (int z = -5; z <= 5; z++) {
                        BlockPos checkPos = pos.offset(x, 0, z);
                        if (world.getBlockState(checkPos).getBlock() == Blocks.END_PORTAL_FRAME) {
                            if (world.getBlockState(checkPos).getValue(EndPortalFrameBlock.HAS_EYE)) {
                                eyeCount++;
                            }
                        }
                    }
                }

                // Если это 12-й глаз (в мире уже 11 активных рамок) — перехватываем управление
                if (eyeCount >= 11) {
                    event.setCanceled(true); // Отменяем стандартный клик, ломая ванильный триггер спавна космоса

                    // Вставляем Око Края в рамку вручную (чтобы визуально она была полной)
                    world.setBlock(pos, world.getBlockState(pos).setValue(EndPortalFrameBlock.HAS_EYE, true), 2);

                    // Проигрываем ванильный звук успешной сборки портала для красоты
                    world.levelEvent(null, 1038, pos, 0);

                    // Тратим Око Края из руки игрока
                    if (event.getPlayer() != null && !event.getPlayer().isCreative()) {
                        event.getItemStack().shrink(1);
                    }

                    LOGGER.info("Divine Souls: Автоматическая активация Портала Края успешно заблокирована!");
                }
            }
        }
    }
}
