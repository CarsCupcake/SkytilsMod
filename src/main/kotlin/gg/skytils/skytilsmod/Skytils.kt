/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package gg.skytils.skytilsmod

import com.google.common.base.Predicate
import com.mojang.authlib.properties.Property
import gg.essential.elementa.state.state
import gg.essential.universal.UChat
import gg.essential.universal.UKeyboard
import gg.skytils.skytilsmod.commands.impl.*
import gg.skytils.skytilsmod.commands.stats.impl.CataCommand
import gg.skytils.skytilsmod.commands.stats.impl.SlayerCommand
import gg.skytils.skytilsmod.core.*
import gg.skytils.skytilsmod.events.impl.MainReceivePacketEvent
import gg.skytils.skytilsmod.events.impl.PacketEvent
import gg.skytils.skytilsmod.features.impl.dungeons.*
import gg.skytils.skytilsmod.features.impl.dungeons.solvers.*
import gg.skytils.skytilsmod.features.impl.dungeons.solvers.terminals.*
import gg.skytils.skytilsmod.features.impl.events.GriffinBurrows
import gg.skytils.skytilsmod.features.impl.events.MayorDiana
import gg.skytils.skytilsmod.features.impl.events.MayorJerry
import gg.skytils.skytilsmod.features.impl.events.TechnoMayor
import gg.skytils.skytilsmod.features.impl.farming.FarmingFeatures
import gg.skytils.skytilsmod.features.impl.farming.GardenFeatures
import gg.skytils.skytilsmod.features.impl.farming.TreasureHunter
import gg.skytils.skytilsmod.features.impl.farming.VisitorHelper
import gg.skytils.skytilsmod.features.impl.handlers.*
import gg.skytils.skytilsmod.features.impl.mining.MiningFeatures
import gg.skytils.skytilsmod.features.impl.mining.StupidTreasureChestOpeningThing
import gg.skytils.skytilsmod.features.impl.misc.*
import gg.skytils.skytilsmod.features.impl.overlays.AuctionPriceOverlay
import gg.skytils.skytilsmod.features.impl.protectitems.ProtectItems
import gg.skytils.skytilsmod.features.impl.spidersden.RainTimer
import gg.skytils.skytilsmod.features.impl.spidersden.RelicWaypoints
import gg.skytils.skytilsmod.features.impl.spidersden.SpidersDenFeatures
import gg.skytils.skytilsmod.features.impl.trackers.impl.DupeTracker
import gg.skytils.skytilsmod.features.impl.trackers.impl.MayorJerryTracker
import gg.skytils.skytilsmod.features.impl.trackers.impl.MythologicalTracker
import gg.skytils.skytilsmod.gui.OptionsGui
import gg.skytils.skytilsmod.gui.ReopenableGUI
import gg.skytils.skytilsmod.listeners.ChatListener
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.localapi.LocalAPI
import gg.skytils.skytilsmod.mixins.extensions.ExtensionEntityLivingBase
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorCommandHandler
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiStreamUnavailable
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorSettingsGui
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.colors.CustomColor
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiGameOver
import net.minecraft.client.gui.GuiIngameMenu
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.settings.KeyBinding
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.launchwrapper.Launch
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.server.*
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import org.lwjgl.input.Keyboard
import skytils.hylin.HylinAPI
import sun.misc.Unsafe
import java.io.File
import java.io.StringWriter
import java.io.Writer
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.function.Consumer
import kotlin.coroutines.CoroutineContext

@Mod(
    modid = Skytils.MOD_ID,
    name = Skytils.MOD_NAME,
    version = Skytils.VERSION,
    acceptedMinecraftVersions = "[1.8.9]",
    clientSideOnly = true
)
class Skytils {

    companion object : CoroutineScope {
        const val MOD_ID = Reference.MOD_ID
        const val MOD_NAME = Reference.MOD_NAME
        const val VERSION = Reference.VERSION

        @JvmStatic
        val mc: Minecraft by lazy {
            Minecraft.getMinecraft()
        }

        val config by lazy {
            Config
        }

        val modDir by lazy {
            File(File(mc.mcDataDir, "config"), "skytils").also {
                it.mkdirs()
                File(it, "trackers").mkdirs()
            }
        }

        @JvmStatic
        lateinit var guiManager: GuiManager

        @JvmField
        val sendMessageQueue = ArrayDeque<String>()

        @JvmField
        var usingLabymod = false

        @JvmField
        var usingNEU = false

        @JvmField
        var usingSBA = false

        @JvmField
        var jarFile: File? = null
        private var lastChatMessage = 0L

        @JvmField
        var displayScreen: GuiScreen? = null

        @JvmField
        val threadPool = Executors.newFixedThreadPool(10) as ThreadPoolExecutor

        @JvmField
        val dispatcher = threadPool.asCoroutineDispatcher()

        val IO = object : CoroutineScope {
            override val coroutineContext = Dispatchers.IO + SupervisorJob() + CoroutineName("Skytils IO")
        }

        override val coroutineContext: CoroutineContext = dispatcher + SupervisorJob() + CoroutineName("Skytils")

        val hylinAPI by lazy {
            HylinAPI("", false, this, HylinConnectionHandler, "https://hypixel.skytils.gg")
        }

        val deobfEnvironment by lazy {
            Launch.blackboard.getOrDefault("fml.deobfuscatedEnvironment", false) as Boolean
        }

        val unsafe by lazy {
            Unsafe::class.java.getDeclaredField("theUnsafe").apply {
                isAccessible = true
            }.get(null) as Unsafe
        }
        val KEY_CATEGORY = "key.category.skytils"
        val GHOST_BLOCK = KeyBinding("key.ghostblocks", Keyboard.KEY_V, KEY_CATEGORY)

        val json = Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            serializersModule = SerializersModule {
                include(serializersModule)
                contextual(CustomColor::class, CustomColor.Serializer)
                contextual(Regex::class, RegexAsString)
                contextual(UUID::class, UUIDAsString)
            }
        }

        val client = HttpClient(CIO) {
            install(ContentEncoding) {
                deflate(1.0F)
                gzip(0.9F)
            }
            install(ContentNegotiation) {
                json(json)
            }
            install(HttpCache)
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 3)
                exponentialDelay()
            }
            install(UserAgent) {
                agent = "Skytils/$VERSION"
            }

            engine {
                endpoint {
                    connectTimeout = 10000
                    keepAliveTime = 5000
                    requestTimeout = 10000
                    socketTimeout = 10000
                }
            }
        }

        val areaRegex = Regex("§r§b§l(?<area>[\\w]+): §r§7(?<loc>[\\w ]+)§r")

        var domain = "api.skytils.gg"

        const val prefix = "§9§lSkytils §8»"
        const val successPrefix = "§a§lSkytils §8»"
        const val failPrefix = "§c§lSkytils §8»"
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        DataFetcher.preload()
        guiManager = GuiManager
        jarFile = event.sourceFile
        mc.framebuffer.enableStencil()
        ClientRegistry.registerKeyBinding(GHOST_BLOCK)
    }

    @SubscribeEvent
    fun onKeyPress(event: InputEvent.KeyInputEvent) {
        if (GHOST_BLOCK.isPressed) {
            if (config.instaGhost) {
                for (i in 0..4) {
                    val pos = BlockPos(
                        mc.thePlayer.position.x + (mc.thePlayer.lookVec.x * i),
                        mc.thePlayer.position.y + mc.thePlayer.eyeHeight + (mc.thePlayer.lookVec.y * i),
                        mc.thePlayer.position.z + (mc.thePlayer.lookVec.z * i)
                    )
                    val state = mc.theWorld.getBlockState(pos)
                    if (state.block != Blocks.air) {
                        mc.theWorld.setBlockToAir(pos)
                        break;
                    }
                }
            }
        }
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        config.init()
        UpdateChecker.downloadDeleteTask()
        arrayOf(
            this,
            ChatListener,
            DungeonListener,
            guiManager,
            LocalAPI,
            MayorInfo,
            SBInfo,
            SoundQueue,
            TickTask,
            UpdateChecker,
            AlignmentTaskSolver,
            AntiFool,
            ArmorColor,
            AuctionData,
            AuctionPriceOverlay,
            BetterStash,
            BlazeSolver,
            BloodHelper,
            BrewingFeatures,
            BossHPDisplays,
            BoulderSolver,
            ChatTabs,
            ChangeAllToSameColorSolver,
            ChestProfit,
            ClickInOrderSolver,
            CreeperSolver,
            CommandAliases,
            ContainerSellValue,
            CooldownTracker,
            CustomNotifications,
            DamageSplash,
            DungeonFeatures,
            DungeonMap,
            DungeonTimer,
            DupeTracker,
            EnchantNames,
            FarmingFeatures,
            FavoritePets,
            Funny,
            GardenFeatures,
            GlintCustomizer,
            GriffinBurrows,
            IceFillSolver,
            IcePathSolver,
            ItemFeatures,
            KeyShortcuts,
            KuudraFeatures,
            LockOrb,
            MasterMode7Features,
            MayorDiana,
            MayorJerry,
            MayorJerryTracker,
            MiningFeatures,
            MinionFeatures,
            MiscFeatures,
            MythologicalTracker,
            PartyAddons,
            PartyFeatures,
            PartyFinderStats,
            PetFeatures,
            Ping,
            PotionEffectTimers,
            PricePaid,
            ProtectItems,
            RainTimer,
            RandomStuff,
            RelicWaypoints,
            ScamCheck,
            ScoreCalculation,
            SelectAllColorSolver,
            ShootTheTargetSolver,
            SimonSaysSolver,
            SlayerFeatures,
            SpidersDenFeatures,
            SpamHider,
            SpiritLeap,
            StartsWithSequenceSolver,
            StupidTreasureChestOpeningThing,
            TankDisplayStuff,
            TechnoMayor,
            TeleportMazeSolver,
            TerminalFeatures,
            ThreeWeirdosSolver,
            TicTacToeSolver,
            TreasureHunter,
            TriviaSolver,
            VisitorHelper,
            WaterBoardSolver,
            Waypoints
        ).forEach(MinecraftForge.EVENT_BUS::register)
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        usingLabymod = Loader.isModLoaded("labymod")
        usingNEU = Loader.isModLoaded("notenoughupdates")
        usingSBA = Loader.isModLoaded("skyblockaddons")

        MayorInfo.fetchMayorData()

        PersistentSave.loadData()

        ModChecker.checkModdedForge()

        ScreenRenderer.init()
    }

    @Mod.EventHandler
    fun loadComplete(event: FMLLoadCompleteEvent) {
        val cch = ClientCommandHandler.instance

        if (cch !is AccessorCommandHandler) throw RuntimeException(
            "Skytils was unable to mixin to the CommandHandler. Please report this on our Discord at discord.gg/skytils."
        )
        cch.registerCommand(SkytilsCommand)

        cch.registerCommand(CataCommand)
        cch.registerCommand(CalcXPCommand)
        cch.registerCommand(FragBotCommand)
        cch.registerCommand(HollowWaypointCommand)
        cch.registerCommand(LimboCommand)
        cch.registerCommand(ScamCheckCommand)
        cch.registerCommand(SlayerCommand)

        if (!cch.commands.containsKey("armorcolor")) {
            cch.registerCommand(ArmorColorCommand)
        }

        if (!cch.commands.containsKey("glintcustomize")) {
            cch.registerCommand(GlintCustomizeCommand)
        }

        if (!cch.commands.containsKey("protectitem")) {
            cch.registerCommand(ProtectItemCommand)
        }

        if (!cch.commands.containsKey("trackcooldown")) {
            cch.registerCommand(TrackCooldownCommand)
        }

        cch.commandSet.add(RepartyCommand)
        cch.commandMap["skytilsreparty"] = RepartyCommand
        if (config.overrideReparty || !cch.commands.containsKey("reparty")) {
            cch.commandMap["reparty"] = RepartyCommand
        }

        if (config.overrideReparty || !cch.commands.containsKey("rp")) {
            cch.commandMap["rp"] = RepartyCommand
        }
    }

    var i = 0;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return

        ScreenRenderer.refresh()
        EntityManager.tickEntities()

        ScoreboardUtil.sidebarLines = ScoreboardUtil.fetchScoreboardLines().map { ScoreboardUtil.cleanSB(it) }
        TabListUtils.tabEntries = TabListUtils.fetchTabEntries().map { it to it.text }
        if (displayScreen != null) {
            if (mc.thePlayer?.openContainer == mc.thePlayer?.inventoryContainer) {
                mc.displayGuiScreen(displayScreen)
                displayScreen = null
            }
        }

        if (mc.thePlayer != null && sendMessageQueue.isNotEmpty() && System.currentTimeMillis() - lastChatMessage > 250) {
            val msg = sendMessageQueue.pollFirst()
            if (!msg.isNullOrBlank()) mc.thePlayer.sendChatMessage(msg)
        }
        if (Utils.inSkyblock && DevTools.getToggle("copydetails") && UKeyboard.isCtrlKeyDown()) {
            if (UKeyboard.isKeyDown(UKeyboard.KEY_TAB)) {
                UChat.chat("Copied tab data to clipboard")
                GuiScreen.setClipboardString(TabListUtils.tabEntries.map { it.second }.toString())
            }
            if (UKeyboard.isKeyDown(UKeyboard.KEY_CAPITAL)) {
                UChat.chat("Copied scoreboard data to clipboard")
                GuiScreen.setClipboardString(ScoreboardUtil.sidebarLines.toString())
            }
            if (UKeyboard.isKeyDown(UKeyboard.KEY_LSHIFT)) {
                val vec = mc.thePlayer.lookVec.times(5.0)
                val base = mc.thePlayer.lookVec
                val m1 = base.rotateYaw(90f).times(0.5)
                val m2 = base.rotateYaw(-90f).times(0.5).add(vec)
                val x = mc.thePlayer.posX;
                val y = mc.thePlayer.posY;
                val z = mc.thePlayer.posZ;
                val bb = AxisAlignedBB(x + m1.x, y + m1.y, z + m1.z, x + m2.x, y + 2 + m2.y, z + m2.z);
                var entitys = mc.theWorld.getEntitiesWithinAABB(
                    EntityLivingBase::class.java,
                    bb,
                    object : Predicate<EntityLivingBase?> {
                        override fun apply(input: EntityLivingBase?): Boolean {
                            return input?.entityId != mc.thePlayer.entityId && (input?.name?.contains("❤") == false)
                        }
                    })
                if (!entitys.isEmpty()) {
                    var e = entitys[0]
                    var dis = 5000.0
                    for (en in entitys) {
                        val d = en.position.distanceSq(mc.thePlayer.position)
                        if (d < dis) {
                            dis = d
                            e = en
                        }
                    }
                    val comp = NBTTagCompound()
                    e.writeToNBT(comp)
                    GuiScreen.setClipboardString(comp.toString())
                    UChat.chat("Copied entitys nbt from ${e.name} (${e.javaClass.simpleName})")
                    if (e is EntityOtherPlayerMP) {
                        val player: EntityOtherPlayerMP = e
                        val propertys = player.gameProfile.properties.get("textures")
                        propertys.stream().filter(object : java.util.function.Predicate<Property?> {
                            override fun test(t: Property?): Boolean {
                                return t?.name == "textures"
                            }
                        }).forEach(object : Consumer<Property?> {
                            override fun accept(t: Property?) {

                                UChat.chat("${t?.value}")
                                UChat.chat("${t?.signature}")
                            }
                        })
                    }
                    /*Thread( Runnable() {
                        fun run() {
                            val file = File(File(modDir, "entitydata"), "${Ae.uniqueID}.txt")
                            if(file.exists())
                                file.delete()
                            else file.mkdirs()
                            file.createNewFile()
                            printDevMessage("Start writing:")
                            printDevMessage(e.serializeNBT().toString())
                            file.writeText(e.serializeNBT().toString())

                        }
                }).start()*/


                } else UChat.chat("§cNo entity found!")
            }
            val container = mc.thePlayer?.openContainer
            if (UKeyboard.isKeyDown(UKeyboard.KEY_LMETA) && container is ContainerChest) {
                UChat.chat("Copied container data to clipboard")
                GuiScreen.setClipboardString(
                    "Name: '${container.lowerChestInventory.name}', Items: ${
                        container.inventorySlots.filter { it.inventory == container.lowerChestInventory }
                            .map { it.stack?.serializeNBT() }
                    }"
                )
            }
        }
    }

    init {
        TickTask(20, repeats = true) {
            if (mc.thePlayer != null) {
                if (deobfEnvironment) {
                    if (DevTools.toggles.getOrDefault("forcehypixel", false)) Utils.isOnHypixel = true
                    if (DevTools.toggles.getOrDefault("forceskyblock", false)) Utils.skyblock = true
                    if (DevTools.toggles.getOrDefault("forcedungeons", false)) Utils.dungeons = true
                }
                if (DevTools.getToggle("sprint"))
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.keyCode, true)
            }
        }
    }

    @SubscribeEvent
    fun onConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        Utils.isOnHypixel = mc.runCatching {
            !event.isLocal && (thePlayer?.clientBrand?.lowercase()?.contains("hypixel")
                ?: currentServerData?.serverIP?.lowercase()?.contains("hypixel") ?: false)
        }.onFailure { it.printStackTrace() }.getOrDefault(false)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onPacket(event: MainReceivePacketEvent<*, *>) {
        if (event.packet is S01PacketJoinGame) {
            Utils.skyblock = false
            Utils.dungeons = false
        }
        if (!Utils.inSkyblock && Utils.isOnHypixel && event.packet is S3DPacketDisplayScoreboard && event.packet.func_149371_c() == 1) {
            Utils.skyblock = event.packet.func_149370_d() == "SBScoreboard"
            printDevMessage("score ${event.packet.func_149370_d()}", "utils")
            printDevMessage("sb ${Utils.inSkyblock}", "utils")
        }
        if (event.packet is S1CPacketEntityMetadata && mc.thePlayer != null) {
            val nameObj = event.packet.func_149376_c()?.find { it.dataValueId == 2 } ?: return
            val entity = mc.theWorld.getEntityByID(event.packet.entityId)

            if (entity is ExtensionEntityLivingBase) {
                entity.skytilsHook.onNewDisplayName(nameObj.`object` as String)
            }
        }
        if (!Utils.isOnHypixel && event.packet is S3FPacketCustomPayload && event.packet.channelName == "MC|Brand") {
            if (event.packet.bufferData.readStringFromBuffer(Short.MAX_VALUE.toInt()).lowercase().contains("hypixel"))
                Utils.isOnHypixel = true
        }
        if (Utils.inDungeons || !Utils.isOnHypixel || event.packet !is S38PacketPlayerListItem ||
            (event.packet.action != S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME &&
                    event.packet.action != S38PacketPlayerListItem.Action.ADD_PLAYER)
        ) return
        event.packet.entries.forEach { playerData ->
            val name = playerData?.displayName?.formattedText ?: playerData?.profile?.name ?: return@forEach
            areaRegex.matchEntire(name)?.let { result ->
                Utils.dungeons = Utils.inSkyblock && result.groups["area"]?.value == "Dungeon"
                printDevMessage("dungeons ${Utils.inDungeons} action ${event.packet.action}", "utils")
                if (Utils.inDungeons)
                    ScoreCalculation.updateText(ScoreCalculation.totalScore.get())
                return@forEach
            }
        }
    }

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        Utils.isOnHypixel = false
        Utils.skyblock = false
        Utils.dungeons = false
    }

    @SubscribeEvent
    fun onSendPacket(event: PacketEvent.SendEvent) {
        if (event.packet is C01PacketChatMessage) {
            lastChatMessage = System.currentTimeMillis()
        }
    }

    @SubscribeEvent
    fun onRenderGameOverlay(event: RenderGameOverlayEvent) {
        if (mc.currentScreen is OptionsGui && event.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onGuiInitPost(event: GuiScreenEvent.InitGuiEvent.Post) {
        if (config.configButtonOnPause && event.gui is GuiIngameMenu) {
            val x = event.gui.width - 105
            val x2 = x + 100
            var y = event.gui.height - 22
            var y2 = y + 20
            val sorted = event.buttonList.sortedWith { a, b -> b.yPosition + b.height - a.yPosition + a.height }
            for (button in sorted) {
                val otherX = button.xPosition
                val otherX2 = button.xPosition + button.width
                val otherY = button.yPosition
                val otherY2 = button.yPosition + button.height
                if (otherX2 > x && otherX < x2 && otherY2 > y && otherY < y2) {
                    y = otherY - 20 - 2
                    y2 = y + 20
                }
            }
            event.buttonList.add(GuiButton(6969420, x, 0.coerceAtLeast(y), 100, 20, "Skytils"))
        }
    }

    @SubscribeEvent
    fun onGuiAction(event: GuiScreenEvent.ActionPerformedEvent.Post) {
        if (config.configButtonOnPause && event.gui is GuiIngameMenu && event.button.id == 6969420) {
            displayScreen = OptionsGui()
        }
    }

    @SubscribeEvent
    fun onGuiChange(event: GuiOpenEvent) {
        val old = mc.currentScreen
        if (event.gui == null && config.reopenOptionsMenu) {
            if (old is ReopenableGUI || (old is AccessorSettingsGui && old.config is Config)) {
                TickTask(1) {
                    if (mc.thePlayer?.openContainer == mc.thePlayer?.inventoryContainer)
                        displayScreen = OptionsGui()
                }
            }
        }
        if (old is AccessorGuiStreamUnavailable) {
            if (config.twitchFix && event.gui == null && !(Utils.inSkyblock && old.parentScreen is GuiGameOver)) {
                event.gui = old.parentScreen
            }
        }
    }
}
