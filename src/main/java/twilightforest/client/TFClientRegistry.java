package twilightforest.client;

import com.google.common.eventbus.Subscribe;
import net.minecraft.Block;
import net.minecraft.Item;
import net.minecraft.Minecraft;
import net.minecraft.ModelPig;
import net.minecraft.ModelSilverfish;
import net.minecraft.ModelSlime;
import net.minecraft.ModelWolf;
import net.minecraft.RenderSnowball;
import net.minecraft.ResourceLocation;
import net.xiaoyu233.fml.reload.event.EntityRendererRegistryEvent;
import net.xiaoyu233.fml.reload.event.MITEEvents;
import net.xiaoyu233.fml.reload.event.SoundsRegisterEvent;
import net.xiaoyu233.fml.reload.event.TileEntityRendererRegisterEvent;
import twilightforest.client.model.ModelTFAdherent;
import twilightforest.client.model.ModelTFBighorn;
import twilightforest.client.model.ModelTFBighornFur;
import twilightforest.client.model.ModelTFBlockGoblin;
import twilightforest.client.model.ModelTFBoar;
import twilightforest.client.model.ModelTFBunny;
import twilightforest.client.model.ModelTFDeathTome;
import twilightforest.client.model.ModelTFDeer;
import twilightforest.client.model.ModelTFFireBeetle;
import twilightforest.client.model.ModelTFGhast;
import twilightforest.client.model.ModelTFGoblinChain;
import twilightforest.client.model.ModelTFGoblinKnightLower;
import twilightforest.client.model.ModelTFGoblinKnightUpper;
import twilightforest.client.model.ModelTFHelmetCrab;
import twilightforest.client.model.ModelTFHydra;
import twilightforest.client.model.ModelTFHydraHead;
import twilightforest.client.model.ModelTFHydraNeck;
import twilightforest.client.model.ModelTFKnightPhantom2;
import twilightforest.client.model.ModelTFKobold;
import twilightforest.client.model.ModelTFLich;
import twilightforest.client.model.ModelTFLichMinion;
import twilightforest.client.model.ModelTFLoyalZombie;
import twilightforest.client.model.ModelTFMinoshroom;
import twilightforest.client.model.ModelTFMinotaur;
import twilightforest.client.model.ModelTFMosquitoSwarm;
import twilightforest.client.model.ModelTFNaga;
import twilightforest.client.model.ModelTFPenguin;
import twilightforest.client.model.ModelTFPinchBeetle;
import twilightforest.client.model.ModelTFRaven;
import twilightforest.client.model.ModelTFRedcap;
import twilightforest.client.model.ModelTFSkeletonDruid;
import twilightforest.client.model.ModelTFSlimeBeetle;
import twilightforest.client.model.ModelTFSpikeBlock;
import twilightforest.client.model.ModelTFSquirrel;
import twilightforest.client.model.ModelTFTinyBird;
import twilightforest.client.model.ModelTFTowerBoss;
import twilightforest.client.model.ModelTFTowerGolem;
import twilightforest.client.model.ModelTFTroll;
import twilightforest.client.model.ModelTFWraith;
import twilightforest.client.model.ModelTFYeti;
import twilightforest.client.model.ModelTFYetiAlpha;
import twilightforest.client.renderer.TFFieryItemRenderer;
import twilightforest.client.renderer.TFGiantBlockRenderer;
import twilightforest.client.renderer.TFGiantItemRenderer;
import twilightforest.client.renderer.TFIceItemRenderer;
import twilightforest.client.renderer.TFMagicMapRenderer;
import twilightforest.client.renderer.TFMazeMapRenderer;
import twilightforest.client.renderer.TileEntityTFCicadaRenderer;
import twilightforest.client.renderer.TileEntityTFFireflyRenderer;
import twilightforest.client.renderer.TileEntityTFMoonwormRenderer;
import twilightforest.client.renderer.TileEntityTFTrophyRenderer;
import twilightforest.client.renderer.blocks.RenderBlockTFCastleMagic;
import twilightforest.client.renderer.blocks.RenderBlockTFFireflyJar;
import twilightforest.client.renderer.blocks.RenderBlockTFHugeLilyPad;
import twilightforest.client.renderer.blocks.RenderBlockTFKnightMetal;
import twilightforest.client.renderer.blocks.RenderBlockTFMagicLeaves;
import twilightforest.client.renderer.blocks.RenderBlockTFNagastone;
import twilightforest.client.renderer.blocks.RenderBlockTFPedestal;
import twilightforest.client.renderer.blocks.RenderBlockTFPlants;
import twilightforest.client.renderer.blocks.RenderBlockTFThorns;
import twilightforest.client.renderer.entity.RenderTFAdherent;
import twilightforest.client.renderer.entity.RenderTFBighorn;
import twilightforest.client.renderer.entity.RenderTFBiped;
import twilightforest.client.renderer.entity.RenderTFBird;
import twilightforest.client.renderer.entity.RenderTFBlockGoblin;
import twilightforest.client.renderer.entity.RenderTFBoar;
import twilightforest.client.renderer.entity.RenderTFBunny;
import twilightforest.client.renderer.entity.RenderTFChainBlock;
import twilightforest.client.renderer.entity.RenderTFCharm;
import twilightforest.client.renderer.entity.RenderTFCubeOfAnnihilation;
import twilightforest.client.renderer.entity.RenderTFDeer;
import twilightforest.client.renderer.entity.RenderTFFallingIce;
import twilightforest.client.renderer.entity.RenderTFGenericLiving;
import twilightforest.client.renderer.entity.RenderTFGiant;
import twilightforest.client.renderer.entity.RenderTFGoblinKnightUpper;
import twilightforest.client.renderer.entity.RenderTFHarbingerCube;
import twilightforest.client.renderer.entity.RenderTFHedgeSpider;
import twilightforest.client.renderer.entity.RenderTFHydra;
import twilightforest.client.renderer.entity.RenderTFHydraHead;
import twilightforest.client.renderer.entity.RenderTFHydraMortar;
import twilightforest.client.renderer.entity.RenderTFIceCrystal;
import twilightforest.client.renderer.entity.RenderTFIceExploder;
import twilightforest.client.renderer.entity.RenderTFIceShooter;
import twilightforest.client.renderer.entity.RenderTFKingSpider;
import twilightforest.client.renderer.entity.RenderTFKnightPhantom;
import twilightforest.client.renderer.entity.RenderTFKobold;
import twilightforest.client.renderer.entity.RenderTFLich;
import twilightforest.client.renderer.entity.RenderTFMazeSlime;
import twilightforest.client.renderer.entity.RenderTFMiniGhast;
import twilightforest.client.renderer.entity.RenderTFMinoshroom;
import twilightforest.client.renderer.entity.RenderTFMistWolf;
import twilightforest.client.renderer.entity.RenderTFMoonwormShot;
import twilightforest.client.renderer.entity.RenderTFNaga;
import twilightforest.client.renderer.entity.RenderTFNagaSegment;
import twilightforest.client.renderer.entity.RenderTFProtectionBox;
import twilightforest.client.renderer.entity.RenderTFQuestRam;
import twilightforest.client.renderer.entity.RenderTFRovingCube;
import twilightforest.client.renderer.entity.RenderTFSlideBlock;
import twilightforest.client.renderer.entity.RenderTFSlimeBeetle;
import twilightforest.client.renderer.entity.RenderTFSnowGuardian;
import twilightforest.client.renderer.entity.RenderTFSnowQueen;
import twilightforest.client.renderer.entity.RenderTFSnowQueenIceShield;
import twilightforest.client.renderer.entity.RenderTFSpikeBlock;
import twilightforest.client.renderer.entity.RenderTFSwarmSpider;
import twilightforest.client.renderer.entity.RenderTFThrownAxe;
import twilightforest.client.renderer.entity.RenderTFThrownIce;
import twilightforest.client.renderer.entity.RenderTFTinyBird;
import twilightforest.client.renderer.entity.RenderTFTinyFirefly;
import twilightforest.client.renderer.entity.RenderTFTowerBroodling;
import twilightforest.client.renderer.entity.RenderTFTowerGhast;
import twilightforest.client.renderer.entity.RenderTFTowerGolem;
import twilightforest.client.renderer.entity.RenderTFUrGhast;
import twilightforest.client.renderer.entity.RenderTFWinterWolf;
import twilightforest.client.renderer.entity.RenderTFWraith;
import twilightforest.client.renderer.entity.RenderTFYeti;
import twilightforest.TwilightForestMod;
import twilightforest.block.TFBlocks;
import twilightforest.item.TFItems;
import twilightforest.tileentity.TileEntityTFCicada;
import twilightforest.tileentity.TileEntityTFFirefly;
import twilightforest.tileentity.TileEntityTFMoonworm;
import twilightforest.tileentity.TileEntityTFTrophy;

/**
 * Everything Forge's RenderingRegistry / ClientRegistry / MinecraftForgeClient / sounds.json did in
 * TFClientProxy.doOnLoadRegistration. All three FML events fire from inside Minecraft.startGame: the
 * sound and renderer ones before the block and item icons are stitched, so renderers must not read an
 * Icon in their constructors. Block and item renderers are registered from the entity renderer event
 * because TFBlocks/TFItems already exist then.
 */
public final class TFClientRegistry {
    /**
     * Sound names (as TF code plays them, behind "twilightforest:") and their variant counts. The files live at
     * assets/twilightforest/sound/&lt;a&gt;/&lt;b&gt;/&lt;c&gt;&lt;n&gt;.ogg numbered from 1 (no number for a single
     * variant): MITE's SoundPool strips trailing digits and turns '/' into '.', which gives back the name.
     */
    private static final Object[][] SOUNDS = {
            {"mob.kobold.die", 3},
            {"mob.kobold.kobold", 6},
            {"mob.kobold.hurt", 3},
            {"mob.cicada", 2},
            {"mob.naga.hiss", 3},
            {"mob.naga.hurt", 3},
            {"mob.naga.rattle", 2},
            {"mob.raven.caw", 2},
            {"mob.raven.squawk", 2},
            {"mob.redcap.die", 3},
            {"mob.redcap.redcap", 6},
            {"mob.redcap.hurt", 4},
            {"mob.tinybird.chirp", 3},
            {"mob.tinybird.hurt", 2},
            {"mob.tinybird.song", 2},
            {"mob.urghast.trapactive", 1},
            {"mob.urghast.trapon", 5},
            {"mob.urghast.trapspindown", 1},
            {"mob.urghast.trapwarmup", 1},
            {"mob.wraith.wraith", 4},
            {"mob.hydra.hurt", 4},
            {"mob.hydra.death", 1},
            {"mob.hydra.growl", 3},
            {"mob.hydra.roar", 2},
            {"mob.hydra.warn", 1},
            {"mob.mosquito.mosquito", 1},
            {"mob.ice.noise", 2},
            {"mob.ice.hurt", 2},
            {"mob.ice.death", 2},
            {"random.slider", 1},
    };

    private static boolean registered;

    private TFClientRegistry() {
    }

    /** Called from TFClientInit before Minecraft is constructed. */
    public static void register() {
        if (!registered) {
            registered = true;
            MITEEvents.MITE_EVENT_BUS.register(new TFClientRegistry());
        }
    }

    @Subscribe
    public void onEntityRenderers(EntityRendererRegistryEvent e) {
        TwilightForestMod.proxy.doOnLoadRegistration();

        e.register(twilightforest.entity.passive.EntityTFBoar.class, new RenderTFBoar(new ModelTFBoar(), new ModelPig(0.5F), 0.7F));
        e.register(twilightforest.entity.passive.EntityTFBighorn.class, new RenderTFBighorn(new ModelTFBighorn(), new ModelTFBighornFur(), 0.7F));
        e.register(twilightforest.entity.passive.EntityTFDeer.class, new RenderTFDeer(new ModelTFDeer(), 0.7F));
        e.register(twilightforest.entity.EntityTFRedcap.class, new RenderTFBiped(new ModelTFRedcap(), 0.625F, "redcap.png"));
        e.register(twilightforest.entity.passive.EntityTFTinyFirefly.class, new RenderTFTinyFirefly());
        e.register(twilightforest.entity.EntityTFSkeletonDruid.class, new RenderTFBiped(new ModelTFSkeletonDruid(), 0.5F, "skeletondruid.png"));
        e.register(twilightforest.entity.EntityTFWraith.class, new RenderTFWraith(new ModelTFWraith(), 0.5F));
        e.register(twilightforest.entity.boss.EntityTFHydra.class, new RenderTFHydra(new ModelTFHydra(), 1.0F));
        e.register(twilightforest.entity.boss.EntityTFLich.class, new RenderTFLich(new ModelTFLich(), 1.0F));
        e.register(twilightforest.entity.passive.EntityTFPenguin.class, new RenderTFBird(new ModelTFPenguin(), 1.0F, "penguin.png"));
        e.register(twilightforest.entity.boss.EntityTFLichMinion.class, new RenderTFBiped(new ModelTFLichMinion(), 1.0F, "textures/entity/zombie/zombie.png"));
        e.register(twilightforest.entity.EntityTFLoyalZombie.class, new RenderTFBiped(new ModelTFLoyalZombie(), 1.0F, "textures/entity/zombie/zombie.png"));
        e.register(twilightforest.entity.passive.EntityTFTinyBird.class, new RenderTFTinyBird(new ModelTFTinyBird(), 1.0F));
        e.register(twilightforest.entity.passive.EntityTFSquirrel.class, new RenderTFGenericLiving(new ModelTFSquirrel(), 1.0F, "squirrel2.png"));
        e.register(twilightforest.entity.passive.EntityTFBunny.class, new RenderTFBunny(new ModelTFBunny(), 1.0F));
        e.register(twilightforest.entity.passive.EntityTFRaven.class, new RenderTFBird(new ModelTFRaven(), 1.0F, "raven.png"));
        e.register(twilightforest.entity.passive.EntityTFQuestRam.class, new RenderTFQuestRam());
        e.register(twilightforest.entity.EntityTFKobold.class, new RenderTFKobold(new ModelTFKobold(), 0.625F, "kobold.png"));
        e.register(twilightforest.entity.EntityTFBoggard.class, new RenderTFBiped(new ModelTFLoyalZombie(), 0.625F, "kobold.png"));
        e.register(twilightforest.entity.EntityTFMosquitoSwarm.class, new RenderTFGenericLiving(new ModelTFMosquitoSwarm(), 0.625F, "mosquitoswarm.png"));
        e.register(twilightforest.entity.EntityTFDeathTome.class, new RenderTFGenericLiving(new ModelTFDeathTome(), 0.625F, "textures/entity/enchanting_table_book.png"));
        e.register(twilightforest.entity.EntityTFMinotaur.class, new RenderTFBiped(new ModelTFMinotaur(), 0.625F, "minotaur.png"));
        e.register(twilightforest.entity.boss.EntityTFMinoshroom.class, new RenderTFMinoshroom(new ModelTFMinoshroom(), 0.625F));
        e.register(twilightforest.entity.EntityTFFireBeetle.class, new RenderTFGenericLiving(new ModelTFFireBeetle(), 0.625F, "firebeetle.png"));
        e.register(twilightforest.entity.EntityTFSlimeBeetle.class, new RenderTFSlimeBeetle(new ModelTFSlimeBeetle(), 0.625F));
        e.register(twilightforest.entity.EntityTFPinchBeetle.class, new RenderTFGenericLiving(new ModelTFPinchBeetle(), 0.625F, "pinchbeetle.png"));
        e.register(twilightforest.entity.EntityTFMistWolf.class, new RenderTFMistWolf(new ModelWolf(), new ModelWolf(), 0.625F));
        e.register(twilightforest.entity.passive.EntityTFMobileFirefly.class, new RenderTFTinyFirefly());
        e.register(twilightforest.entity.EntityTFMiniGhast.class, new RenderTFMiniGhast(new ModelTFGhast(), 0.625F));
        e.register(twilightforest.entity.EntityTFTowerGolem.class, new RenderTFTowerGolem(new ModelTFTowerGolem(), 0.5F));
        e.register(twilightforest.entity.EntityTFTowerTermite.class, new RenderTFGenericLiving(new ModelSilverfish(), 0.3F, "towertermite.png"));
        e.register(twilightforest.entity.EntityTFTowerGhast.class, new RenderTFTowerGhast(new ModelTFGhast(), 0.625F));
        e.register(twilightforest.entity.boss.EntityTFUrGhast.class, new RenderTFUrGhast(new ModelTFTowerBoss(), 0.625F, 24F));
        e.register(twilightforest.entity.EntityTFBlockGoblin.class, new RenderTFBlockGoblin(new ModelTFBlockGoblin(), 0.625F));
        e.register(twilightforest.entity.EntityTFGoblinChain.class, new RenderTFSpikeBlock(new ModelTFGoblinChain(), 0.625F));
        e.register(twilightforest.entity.EntityTFSpikeBlock.class, new RenderTFSpikeBlock(new ModelTFSpikeBlock(), 0.625F));
        e.register(twilightforest.entity.EntityTFGoblinKnightUpper.class, new RenderTFGoblinKnightUpper(new ModelTFGoblinKnightUpper(), 0.625F));
        e.register(twilightforest.entity.EntityTFGoblinKnightLower.class, new RenderTFBiped(new ModelTFGoblinKnightLower(), 0.625F, "doublegoblin.png"));
        e.register(twilightforest.entity.EntityTFHelmetCrab.class, new RenderTFGenericLiving(new ModelTFHelmetCrab(), 0.625F, "helmetcrab.png"));
        e.register(twilightforest.entity.boss.EntityTFKnightPhantom.class, new RenderTFKnightPhantom(new ModelTFKnightPhantom2(), 0.625F));
        e.register(twilightforest.entity.boss.EntityTFNaga.class, new RenderTFNaga(new ModelTFNaga(), 0.625F));
        e.register(twilightforest.entity.boss.EntityTFNagaSegment.class, new RenderTFNagaSegment(new ModelTFNaga(), 0.625F));
        e.register(twilightforest.entity.EntityTFSwarmSpider.class, new RenderTFSwarmSpider());
        e.register(twilightforest.entity.EntityTFKingSpider.class, new RenderTFKingSpider());
        e.register(twilightforest.entity.EntityTFTowerBroodling.class, new RenderTFTowerBroodling());
        e.register(twilightforest.entity.EntityTFHedgeSpider.class, new RenderTFHedgeSpider());
        e.register(twilightforest.entity.EntityTFRedcapSapper.class, new RenderTFBiped(new ModelTFRedcap(), 0.625F, "redcapsapper.png"));
        e.register(twilightforest.entity.EntityTFMazeSlime.class, new RenderTFMazeSlime(new ModelSlime(16), new ModelSlime(0), 0.625F));
        e.register(twilightforest.entity.EntityTFYeti.class, new RenderTFYeti(new ModelTFYeti(), 0.625F, "yeti2.png"));
        e.register(twilightforest.entity.EntityTFProtectionBox.class, new RenderTFProtectionBox());
        e.register(twilightforest.entity.boss.EntityTFYetiAlpha.class, new RenderTFYeti(new ModelTFYetiAlpha(), 0.625F, "yetialpha.png"));
        e.register(twilightforest.entity.EntityTFWinterWolf.class, new RenderTFWinterWolf(new ModelWolf(), new ModelWolf(), 0.625F));
        e.register(twilightforest.entity.EntityTFSnowGuardian.class, new RenderTFSnowGuardian());
        e.register(twilightforest.entity.EntityTFIceShooter.class, new RenderTFIceShooter());
        e.register(twilightforest.entity.EntityTFIceExploder.class, new RenderTFIceExploder());
        e.register(twilightforest.entity.boss.EntityTFSnowQueen.class, new RenderTFSnowQueen());
        e.register(twilightforest.entity.boss.EntityTFSnowQueenIceShield.class, new RenderTFSnowQueenIceShield());
        e.register(twilightforest.entity.EntityTFTroll.class, new RenderTFBiped(new ModelTFTroll(), 0.625F, "troll.png"));
        e.register(twilightforest.entity.EntityTFGiantMiner.class, new RenderTFGiant());
        e.register(twilightforest.entity.boss.EntityTFIceCrystal.class, new RenderTFIceCrystal());
        e.register(twilightforest.entity.EntityTFChainBlock.class, new RenderTFChainBlock(new ModelTFSpikeBlock(), 0.625F));
        e.register(twilightforest.entity.EntityTFCubeOfAnnihilation.class, new RenderTFCubeOfAnnihilation());
        e.register(twilightforest.entity.EntityTFHarbingerCube.class, new RenderTFHarbingerCube());
        e.register(twilightforest.entity.EntityTFAdherent.class, new RenderTFAdherent(new ModelTFAdherent(), 0.625F, "adherent.png"));
        e.register(twilightforest.entity.EntityTFRovingCube.class, new RenderTFRovingCube());
        // projectiles
        e.register(twilightforest.entity.EntityTFNatureBolt.class, new RenderSnowball(Item.seeds));
        e.register(twilightforest.entity.boss.EntityTFLichBolt.class, new RenderSnowball(Item.enderPearl));
        e.register(twilightforest.entity.EntityTFTwilightWandBolt.class, new RenderSnowball(Item.enderPearl));
        e.register(twilightforest.entity.EntityTFTomeBolt.class, new RenderSnowball(Item.paper));
        e.register(twilightforest.entity.boss.EntityTFHydraMortar.class, new RenderTFHydraMortar());
        e.register(twilightforest.entity.EntityTFSlimeProjectile.class, new RenderSnowball(Item.slimeBall));
        e.register(twilightforest.entity.EntityTFMoonwormShot.class, new RenderTFMoonwormShot());
        e.register(twilightforest.entity.EntityTFCharmEffect.class, new RenderTFCharm(null));
        e.register(twilightforest.entity.boss.EntityTFLichBomb.class, new RenderSnowball(Item.magmaCream));
        e.register(twilightforest.entity.boss.EntityTFThrownAxe.class, new RenderTFThrownAxe(TFItems.knightlyAxe));
        e.register(twilightforest.entity.boss.EntityTFThrownPick.class, new RenderTFThrownAxe(TFItems.knightlyPick));
        e.register(twilightforest.entity.boss.EntityTFFallingIce.class, new RenderTFFallingIce());
        e.register(twilightforest.entity.boss.EntityTFIceBomb.class, new RenderTFThrownIce());
        e.register(twilightforest.entity.EntityTFIceSnowball.class, new RenderSnowball(Item.snowball));
        e.register(twilightforest.entity.EntityTFSlideBlock.class, new RenderTFSlideBlock());
        // the hydra gets its own section
        e.register(twilightforest.entity.boss.EntityTFHydraHead.class, new RenderTFHydraHead(new ModelTFHydraHead(), 1.0F));
        e.register(twilightforest.entity.boss.EntityTFHydraNeck.class, new RenderTFGenericLiving(new ModelTFHydraNeck(), 1.0F, "hydra4.png"));

        registerItemRenderers();
        registerBlockRenderers();
    }

    @Subscribe
    public void onTileEntityRenderers(TileEntityRendererRegisterEvent e) {
        e.register(TileEntityTFFirefly.class, new TileEntityTFFireflyRenderer());
        e.register(TileEntityTFCicada.class, new TileEntityTFCicadaRenderer());
        e.register(TileEntityTFMoonworm.class, new TileEntityTFMoonwormRenderer());
        e.register(TileEntityTFTrophy.class, new TileEntityTFTrophyRenderer());
    }

    @Subscribe
    public void onSounds(SoundsRegisterEvent e) {
        for (Object[] sound : SOUNDS) {
            // verify=false: these are looked up by name, not resources to check at startup
            e.registerSound(new ResourceLocation(TwilightForestMod.ID + ":" + sound[0], false), (Integer) sound[1]);
        }
    }

    private static void registerItemRenderers() {
        Minecraft mc = Minecraft.getMinecraft();

        // map item renderers
        TFItemRenderers.register(TFItems.magicMap, new TFMagicMapRenderer(mc.gameSettings, mc.getTextureManager()));
        TFMazeMapRenderer mazeRenderer = new TFMazeMapRenderer(mc.gameSettings, mc.getTextureManager());
        TFItemRenderers.register(TFItems.mazeMap, mazeRenderer);
        TFItemRenderers.register(TFItems.oreMap, mazeRenderer);

        // giant item renderers
        TFGiantItemRenderer giantRenderer = new TFGiantItemRenderer(mc.gameSettings, mc.getTextureManager());
        TFItemRenderers.register(TFItems.giantPick, giantRenderer);
        TFItemRenderers.register(TFItems.giantSword, giantRenderer);

        TFGiantBlockRenderer giantBlockRenderer = new TFGiantBlockRenderer(mc.gameSettings, mc.getTextureManager());
        TFItemRenderers.register(blockItem(TFBlocks.giantLeaves), giantBlockRenderer);
        TFItemRenderers.register(blockItem(TFBlocks.giantCobble), giantBlockRenderer);
        TFItemRenderers.register(blockItem(TFBlocks.giantLog), giantBlockRenderer);
        TFItemRenderers.register(blockItem(TFBlocks.giantObsidian), giantBlockRenderer);

        // fiery item renderer
        TFFieryItemRenderer fieryRenderer = new TFFieryItemRenderer(mc.gameSettings, mc.getTextureManager());
        TFItemRenderers.register(TFItems.fieryPick, fieryRenderer);
        TFItemRenderers.register(TFItems.fierySword, fieryRenderer);
        TFItemRenderers.register(TFItems.fieryIngot, fieryRenderer);
        TFItemRenderers.register(TFItems.fieryHelm, fieryRenderer);
        TFItemRenderers.register(TFItems.fieryPlate, fieryRenderer);
        TFItemRenderers.register(TFItems.fieryLegs, fieryRenderer);
        TFItemRenderers.register(TFItems.fieryBoots, fieryRenderer);

        // ice item renderer
        TFIceItemRenderer iceRenderer = new TFIceItemRenderer(mc.gameSettings, mc.getTextureManager());
        TFItemRenderers.register(TFItems.iceSword, iceRenderer);
        TFItemRenderers.register(TFItems.glassSword, iceRenderer);
        TFItemRenderers.register(TFItems.iceBow, iceRenderer);
    }

    private static Item blockItem(Block block) {
        return Item.itemsList[block.blockID];
    }

    /**
     * The critter block (firefly/cicada/moonworm) has no handler any more: it is a TESR block with render type -1.
     * RenderBlockTFGiantBlocks was never registered in 1.7.10 either.
     */
    private static void registerBlockRenderers() {
        TFBlockRenderers.register(TFRenderTypes.COMPLEX, new RenderBlockTFFireflyJar());
        TFBlockRenderers.register(TFRenderTypes.PLANT, new RenderBlockTFPlants());
        TFBlockRenderers.register(TFRenderTypes.NAGASTONE, new RenderBlockTFNagastone());
        TFBlockRenderers.register(TFRenderTypes.MAGIC_LEAVES, new RenderBlockTFMagicLeaves());
        TFBlockRenderers.register(TFRenderTypes.PEDESTAL, new RenderBlockTFPedestal());
        TFBlockRenderers.register(TFRenderTypes.THORNS, new RenderBlockTFThorns());
        TFBlockRenderers.register(TFRenderTypes.KNIGHTMETAL, new RenderBlockTFKnightMetal());
        TFBlockRenderers.register(TFRenderTypes.HUGE_LILY_PAD, new RenderBlockTFHugeLilyPad());
        TFBlockRenderers.register(TFRenderTypes.CASTLE_MAGIC, new RenderBlockTFCastleMagic());
    }
}
