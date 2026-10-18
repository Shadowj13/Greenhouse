package it.bonsaigreenhouse.qa;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import it.bonsaigreenhouse.*;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** A separate test mod: never packaged inside the installable Greenhouse JAR. */
@Mod("greenhouse_qa")
public final class ConsolidationTests {
    private static final int X = 51200, Z = 51200, Y = 100;
    private ServerLevel level;
    private int next;
    private final Map<String,Object> results = new LinkedHashMap<>();
    private final BlockPos networkPos = new BlockPos(X,Y,Z+64);
    private final List<BlockPos> soakPositions = new ArrayList<>();
    private long soakStartTick, soakStartNanos;
    private boolean poplarSoak;

    public ConsolidationTests(net.neoforged.bus.api.IEventBus modBus) {
        // Keep clean full-client acceptance free of test-only registry entries.
        // The default integration suite registers them for namespace regressions.
        if (!Boolean.getBoolean("greenhouse.qa.skipLookalikes")) CropAndRecipeTests.ITEMS.register(modBus);
        NeoForge.EVENT_BUS.addListener(this::commands);
    }
    private void commands(RegisterCommandsEvent event) {
        var root = Commands.literal("greenhouseqa");
        for (String phase : List.of("core", "crops", "recipes", "checkpoint", "unload", "reload", "benchmark",
                "networksetup", "networkassert", "soaksetup", "soakstats", "soakassert",
                "poplarsoaksetup", "poplarsoakassert"))
            root.then(Commands.literal(phase).executes(c -> run(c.getSource(), phase)));
        event.getDispatcher().register(root);
    }
    private int run(CommandSourceStack source, String phase) {
        level = source.getLevel(); next = 0; results.clear();
        switch (phase) {
            case "core" -> core();
            case "crops" -> new CropAndRecipeTests(level, this::test).crops();
            case "recipes" -> new CropAndRecipeTests(level, this::test).recipes();
            case "checkpoint" -> checkpoint();
            case "unload" -> unloaded();
            case "reload" -> reload();
            case "benchmark" -> benchmark();
            case "networksetup" -> networkSetup(source);
            case "networkassert" -> networkAssert(source);
            case "soaksetup" -> { poplarSoak=false; soakSetup(); }
            case "poplarsoaksetup" -> { poplarSoak=true; soakSetup(); }
            case "soakstats" -> soakStats(source);
            case "soakassert" -> soakAssert(source);
            case "poplarsoakassert" -> soakAssert(source);
        }
        System.out.println("GREENHOUSE_QA_RESULT " + new Gson().toJson(Map.of("phase",phase,"results",results)));
        return results.values().stream().anyMatch(v -> v instanceof String s && s.startsWith("FAIL")) ? 0 : 1;
    }
    private void test(String name, Runnable body) {
        try { body.run(); results.put(name, "PASS"); }
        catch (Throwable ex) { results.put(name, "FAIL: " + ex); ex.printStackTrace(); }
    }
    private List<net.minecraft.server.level.ServerPlayer> networkPlayers(CommandSourceStack source) {
        return source.getServer().getPlayerList().getPlayers().stream()
            .filter(p->p.getPlainTextName().equals("GreenhouseNetA") || p.getPlainTextName().equals("GreenhouseNetB")).toList();
    }
    private void networkSetup(CommandSourceStack source) {
        test("two_real_clients_open_same_menu",()->{
            var players=networkPlayers(source);
            require(players.size()==2,"expected two connected real network clients");
            level.setChunkForced(networkPos.getX()>>4,networkPos.getZ()>>4,true);
            for(int x=-2;x<=4;x++) for(int z=-2;z<=5;z++)
                level.setBlock(networkPos.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState(),3);
            var be=greenhouse(networkPos,false); inputs(be,Items.DIRT,Items.POPLAR_SAPLING,64); tick(be,450);
            int i=0;
            for(var p:players) {
                p.getInventory().clearContent(); p.setGameMode(GameType.CREATIVE);
                p.teleportTo(level,networkPos.getX()+0.5+i++,Y,networkPos.getZ()+3.2,Set.of(),180,20,true);
                require(p.openMenu(be).isPresent(),"server did not open menu for "+p.getPlainTextName());
                p.containerMenu.broadcastChanges();
            }
            require(be.dataAccess().get(2)==1 && be.getItem(2).getCount()==63,"network fixture not mature");
        });
    }
    private void networkAssert(CommandSourceStack source) {
        test("real_clients_remain_on_greenhouse_menu",()->{
            var players=networkPlayers(source); require(players.size()==2,"network client disconnected");
            for(var p:players) require(p.containerMenu instanceof GreenhouseMenu && p.containerMenu.stillValid(p),"client menu closed or invalid");
        });
        test("real_client_harvest_packet_delivers_exactly_once",()->{
            var players=networkPlayers(source);
            require(players.size()==2 && players.stream().mapToInt(p->p.getInventory().countItem(Items.POPLAR_LOG)).sum()==2,"network harvest lost or duplicated poplar logs");
        });
        test("network_harvest_resets_authoritative_state",()->{
            var be=(GreenhouseBlockEntity)level.getBlockEntity(networkPos);
            require(be!=null && be.dataAccess().get(2)==0 && be.dataAccess().get(0)==0,"maturity did not reset while ticks frozen");
            require(be.getItem(2).getCount()==63 && be.dataAccess().get(3)==3,"network harvest spent fertilizer twice");
            require(be.getBlockState().getValue(GreenhouseBlock.CROP).isPoplar(),"network fixture lost poplar color");
        });
    }
    private void soakSetup() {
        test("prepare_256_naturally_ticking_machines",()->{
            soakPositions.clear();
            for(int cx=(X+256)>>4;cx<=(X+286)>>4;cx++) for(int cz=Z>>4;cz<=(Z+30)>>4;cz++) level.setChunkForced(cx,cz,true);
            for(int x=0;x<16;x++) for(int z=0;z<16;z++) {
                BlockPos p=new BlockPos(X+256+x*2,Y,Z+z*2);
                var be=greenhouse(p,true); inputs(be,Items.DIRT,poplarSoak?Items.POPLAR_SAPLING:Items.OAK_SAPLING,64); soakPositions.add(p);
            }
            soakStartTick=level.getGameTime(); soakStartNanos=System.nanoTime();
        });
    }
    private void soakStats(CommandSourceStack source) {
        results.put("machines",soakPositions.size()); results.put("natural_world_ticks",level.getGameTime()-soakStartTick);
        results.put("crop",poplarSoak?"poplar":"oak");
        results.put("wall_seconds",(System.nanoTime()-soakStartNanos)/1_000_000_000.0);
        results.put("server_average_tick_ms",source.getServer().getAverageTickTimeNanos()/1_000_000.0);
        results.put("scope","Full local server ticks with 256 forced-loaded machines; wall time includes any explicit tick freeze");
    }
    private void soakAssert(CommandSourceStack source) {
        soakStats(source);
        test("at_least_6000_natural_world_ticks",()->require(level.getGameTime()-soakStartTick>=6000,"soak duration not reached"));
        long[] minimumTicks={Long.MAX_VALUE}, maximumActivation={0};
        test("all_256_machines_tick_and_preserve_exact_log_yield",()->{
            require(soakPositions.size()==256,"soak fixtures missing");
            long ticks=level.getGameTime()-soakStartTick;
            for(BlockPos p:soakPositions) {
                var be=(GreenhouseBlockEntity)level.getBlockEntity(p);
                require(be!=null,"soak block entity missing at "+p);
                // Force-loaded chunks become ticking asynchronously. Infer cycle
                // starts from fertilizer/charge state independently of output,
                // then check exact output and a bounded initial activation lag.
                long started=4L*(64-be.getItem(2).getCount())-be.dataAccess().get(3);
                long completed=started-(be.dataAccess().get(1)>0?1:0);
                long actualTicks=completed*450+be.dataAccess().get(0);
                require(actualTicks>=6000 && ticks-actualTicks>=0 && ticks-actualTicks<=40,
                    "unexpected ticking gap at "+p+": world="+ticks+", machine="+actualTicks);
                var log=poplarSoak?Items.POPLAR_LOG:Items.OAK_LOG;
                require(be.countItem(log)==2*completed,
                    "incorrect natural yield at "+p+": expected="+(2*completed)+", actual="+be.countItem(log));
                require(be.getItem(0).getCount()==1 && be.getItem(1).getCount()==1,"soak consumed substrate/plant");
                minimumTicks[0]=Math.min(minimumTicks[0],actualTicks);
                maximumActivation[0]=Math.max(maximumActivation[0],ticks-actualTicks);
            }
        });
        if(poplarSoak) test("all_three_poplar_colors_present_under_natural_ticks",()->{
            Set<Crop> colors=EnumSet.noneOf(Crop.class);
            for(BlockPos p:soakPositions) {
                Crop color=level.getBlockState(p).getValue(GreenhouseBlock.CROP);
                require(color.isPoplar(),"machine lost poplar selection at "+p); colors.add(color);
            }
            require(colors.size()==3,"not all colors were naturally selected: "+colors);
        });
        if(minimumTicks[0]!=Long.MAX_VALUE) {
            results.put("minimum_machine_ticks",minimumTicks[0]);
            results.put("maximum_initial_chunk_activation_ticks",maximumActivation[0]);
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private BlockPos pos() { return new BlockPos(X + (next++ % 24)*3, Y, Z + (next/24)*3); }
    private GreenhouseBlockEntity greenhouse(BlockPos pos, boolean auto) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(pos, (auto ? BonsaiGreenhouse.HOPPING : BonsaiGreenhouse.NORMAL).get().defaultBlockState(), 3);
        return (GreenhouseBlockEntity)level.getBlockEntity(pos);
    }
    private GreenhouseBlockEntity greenhouse(boolean auto) { return greenhouse(pos(), auto); }
    private void inputs(GreenhouseBlockEntity be, Item soil, Item seed, int bone) {
        be.setItem(0, new ItemStack(soil)); be.setItem(1, new ItemStack(seed));
        if (bone > 0) be.setItem(2, new ItemStack(Items.BONE_MEAL, bone));
    }
    private void tick(GreenhouseBlockEntity be, int ticks) {
        for (int i=0;i<ticks;i++) GreenhouseBlockEntity.serverTick(level, be.getBlockPos(), be.getBlockState(), be);
    }
    private CompoundTag state(GreenhouseBlockEntity be) { return be.saveWithoutMetadata(level.registryAccess()); }
    private void load(GreenhouseBlockEntity be, CompoundTag tag) {
        be.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
    }
    private CompoundTag inventory(ItemStack... items) {
        var list = NonNullList.withSize(27, ItemStack.EMPTY);
        for(int i=0;i<items.length;i++) list.set(i,items[i]);
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        ContainerHelper.saveAllItems(out,list);
        return out.buildResult();
    }
    private void outputs(GreenhouseBlockEntity be, ItemStack item) {
        ItemStack[] slots = new ItemStack[27]; Arrays.fill(slots,ItemStack.EMPTY); slots[9]=item;
        load(be, inventory(slots));
    }
    private GreenhouseBlockEntity blockedReady(BlockPos pos) {
        var be = greenhouse(pos,true);
        ItemStack[] slots = new ItemStack[27]; Arrays.fill(slots, ItemStack.EMPTY);
        slots[0]=new ItemStack(Items.DIRT); slots[1]=new ItemStack(Items.OAK_SAPLING);
        slots[2]=new ItemStack(Items.BONE_MEAL,63);
        for(int i=9;i<18;i++) slots[i]=new ItemStack(Items.COBBLESTONE,64);
        CompoundTag tag=inventory(slots);
        var pending=NonNullList.withSize(9,ItemStack.EMPTY); pending.set(0,new ItemStack(Items.OAK_LOG,2));
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        ContainerHelper.saveAllItems(out,pending); tag.put("pending",out.buildResult());
        tag.putString("crop","oak"); tag.putInt("elapsed",450); tag.putInt("duration",450);
        tag.putInt("chargedCycles",3); tag.putBoolean("mature",true); tag.putBoolean("newCycle",false);
        load(be,tag); return be;
    }
    private FakePlayer player(String name, BlockPos near) {
        var p=FakePlayerFactory.get(level,new GameProfile(UUID.nameUUIDFromBytes(name.getBytes(java.nio.charset.StandardCharsets.UTF_8)),name));
        p.setGameMode(GameType.SURVIVAL); p.setPos(near.getX()+0.5,near.getY()+1,near.getZ()+0.5);
        p.getInventory().clearContent(); return p;
    }
    private int dropped(BlockPos pos, Item item) {
        return level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(1)).stream()
            .filter(e->e.getItem().is(item)).mapToInt(e->e.getItem().getCount()).sum();
    }
    private RestrictedChest target(GreenhouseBlockEntity be, int max, boolean allow) {
        BlockPos below=be.getBlockPos().below(); level.setBlock(below,Blocks.CHEST.defaultBlockState(),3);
        var chest=new RestrictedChest(below,level.getBlockState(below),max,allow);
        level.setBlockEntity(chest); return chest;
    }
    private void core() {
        test("growth_and_manual_harvest_once",()->{
            var be=greenhouse(false); inputs(be,Items.DIRT,Items.OAK_SAPLING,0); tick(be,900);
            require(be.dataAccess().get(2)==1,"manual crop not mature");
            var p=player("GHManual",be.getBlockPos()); be.collectBy(p); be.collectBy(p);
            require(p.getInventory().countItem(Items.OAK_LOG)==2,"manual harvest lost or duplicated logs");
            require(be.getItem(0).getCount()==1 && be.getItem(1).getCount()==1,"inputs consumed");
        });
        test("fertilizer_four_cycles",()->{
            var be=greenhouse(false); inputs(be,Items.DIRT,Items.OAK_SAPLING,2);
            var p=player("GHBoost",be.getBlockPos());
            for(int i=0;i<4;i++) { tick(be,450); require(be.dataAccess().get(2)==1,"boosted duration changed"); be.collectBy(p); }
            require(be.getItem(2).getCount()==1,"bone meal did not last four cycles");
            tick(be,1); require(be.getItem(2).isEmpty(),"fifth cycle did not consume next bone meal");
        });
        test("invalid_soil_and_fertilizer_rejected",()->{
            var be=greenhouse(false); inputs(be,Items.SAND,Items.OAK_SAPLING,64); tick(be,1000);
            require(be.dataAccess().get(0)==0 && be.getItem(2).getCount()==64,"invalid soil grew or consumed fertilizer");
            be.setItem(2,new ItemStack(Items.DIAMOND)); require(be.getItem(2).is(Items.BONE_MEAL),"invalid fertilizer accepted");
        });
        test("progress_marks_chunk_dirty",()->{
            var be=greenhouse(false); inputs(be,Items.DIRT,Items.OAK_SAPLING,1); tick(be,10);
            var chunk=level.getChunkAt(be.getBlockPos()); chunk.tryMarkSaved(); tick(be,1);
            require(chunk.isUnsaved(),"in-progress tick can be skipped by chunk saving");
        });
        test("idle_automatic_does_not_dirty_chunk",()->{
            var be=greenhouse(true); target(be,64,true);
            var chunk=level.getChunkAt(be.getBlockPos()); chunk.tryMarkSaved(); tick(be,32);
            require(!chunk.isUnsaved(),"idle machine repeatedly marks an unchanged inventory dirty");
        });
        test("full_outputs_backpressure_and_resume",()->{
            var be=blockedReady(pos()); var before=state(be).getCompound("pending").orElseThrow().copy(); tick(be,1000);
            require(be.dataAccess().get(2)==1 && state(be).getCompound("pending").orElseThrow().equals(before),"blocked harvest lost/rerolled");
            require(be.getItem(2).getCount()==63,"blocked harvest spent fertilizer");
            be.removeItem(9,64); tick(be,1);
            require(be.getItem(9).is(Items.OAK_LOG) && be.getItem(9).getCount()==2,"resume did not store exactly once");
        });
        test("target_stack_limit_no_item_loss",()->{
            var be=greenhouse(true); outputs(be,new ItemStack(Items.OAK_LOG,64)); var chest=target(be,8,true); tick(be,8);
            require(chest.getItem(0).getCount()==8 && be.getItem(9).getCount()==56,"target limit discarded source items");
        });
        test("target_up_face_is_respected",()->{
            var be=greenhouse(true); outputs(be,new ItemStack(Items.OAK_LOG,10)); var chest=target(be,64,false); tick(be,8);
            require(chest.isEmpty() && be.getItem(9).getCount()==10,"inserted through forbidden target face");
        });
        test("full_target_does_not_consume_output",()->{
            var be=greenhouse(true); outputs(be,new ItemStack(Items.OAK_LOG,10)); var chest=target(be,64,true);
            chest.setItem(0,new ItemStack(Items.COBBLESTONE,64)); tick(be,32);
            require(be.getItem(9).getCount()==10,"output lost with full chest");
        });
        test("furnace_top_does_not_accept_into_fuel_slot",()->{
            var be=greenhouse(true); outputs(be,new ItemStack(Items.OAK_LOG,10));
            var below=be.getBlockPos().below(); level.setBlock(below,Blocks.FURNACE.defaultBlockState(),3);
            var furnace=(Container)level.getBlockEntity(below); furnace.setItem(0,new ItemStack(Items.COBBLESTONE,64));
            tick(be,8); require(furnace.getItem(1).isEmpty() && be.getItem(9).getCount()==10,"top insertion bypassed furnace slot rules");
        });
        test("double_chest_uses_other_half",()->{
            var be=greenhouse(true); outputs(be,new ItemStack(Items.OAK_LOG,10));
            var left=be.getBlockPos().below(); var right=left.east();
            level.setBlock(right,Blocks.AIR.defaultBlockState(),3);
            level.setBlock(left,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.NORTH).setValue(ChestBlock.TYPE,ChestType.LEFT),3);
            level.setBlock(right,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.NORTH).setValue(ChestBlock.TYPE,ChestType.RIGHT),3);
            var c1=(Container)level.getBlockEntity(left); var c2=(Container)level.getBlockEntity(right);
            var combined=ChestBlock.getContainer((ChestBlock)Blocks.CHEST,level.getBlockState(left),level,left,true);
            require(combined!=null && combined.getContainerSize()==54,"fixture did not form a double chest");
            for(int i=0;i<c1.getContainerSize();i++) c1.setItem(i,new ItemStack(Items.COBBLESTONE,64));
            tick(be,8); require(be.getItem(9).isEmpty() && c2.countItem(Items.OAK_LOG)==10,"double chest transfer: source="+be.getItem(9)+", other-half logs="+c2.countItem(Items.OAK_LOG));
        });
        test("removal_preserves_pending_harvest",()->{
            var be=blockedReady(pos()); var p=be.getBlockPos(); level.removeBlock(p,false);
            require(dropped(p,Items.OAK_LOG)==2,"non-player removal lost pending mature harvest");
            require(dropped(p,Items.COBBLESTONE)==576,"stored items lost/duplicated on removal");
        });
        test("survival_break_drops_once",()->{
            var be=blockedReady(pos()); var p=be.getBlockPos(); var player=player("GHBreak",p);
            require(player.gameMode.destroyBlock(p),"survival break failed");
            require(dropped(p,Items.OAK_LOG)==2 && dropped(p,Items.COBBLESTONE)==576,"break duplicated or lost contents");
            require(dropped(p,BonsaiGreenhouse.HOPPING_ITEM.get())==1,"survival block item count incorrect");
        });
        test("manual_harvest_with_full_player_inventory",()->{
            var be=greenhouse(false); inputs(be,Items.DIRT,Items.OAK_SAPLING,0); tick(be,900);
            var p=player("GHFull",be.getBlockPos());
            for(int i=0;i<36;i++) p.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));
            be.collectBy(p); be.collectBy(p); require(dropped(be.getBlockPos(),Items.OAK_LOG)==2,"overflow lost/duplicated harvest");
        });
        test("two_player_menu_progress_sync_and_harvest",()->{
            var be=greenhouse(false); inputs(be,Items.DIRT,Items.OAK_SAPLING,1);
            var p1=player("GHSync1",be.getBlockPos()); var p2=player("GHSync2",be.getBlockPos());
            var m1=new GreenhouseMenu(1,p1.getInventory(),be); var m2=new GreenhouseMenu(2,p2.getInventory(),be);
            var l1=new Listener(); var l2=new Listener(); m1.addSlotListener(l1); m2.addSlotListener(l2);
            tick(be,450); m1.broadcastChanges(); m2.broadcastChanges();
            require(Arrays.equals(l1.data,l2.data) && l1.data[0]==450 && l1.data[2]==1,"menu viewers diverged");
            require(m1.clickMenuButton(p1,0) && m2.clickMenuButton(p2,0),"menu harvest button rejected valid viewers");
            m1.broadcastChanges(); m2.broadcastChanges();
            require(p1.getInventory().countItem(Items.OAK_LOG)+p2.getInventory().countItem(Items.OAK_LOG)==2,"two viewers duplicated harvest");
            require(l1.data[2]==0 && l2.data[2]==0,"maturity reset did not reach both viewers");
        });
        test("shift_click_and_slot_rules",()->{
            var be=greenhouse(true); var p=player("GHShift",be.getBlockPos()); var menu=new GreenhouseMenu(1,p.getInventory(),be);
            p.getInventory().setItem(9,new ItemStack(Items.DIRT,64)); menu.quickMoveStack(p,12);
            require(be.getItem(0).getCount()==1 && p.getInventory().getItem(9).getCount()==63,"shift-click soil limit broken");
            p.getInventory().setItem(10,new ItemStack(Items.OAK_SAPLING,64)); menu.quickMoveStack(p,13);
            require(be.getItem(1).getCount()==1 && p.getInventory().getItem(10).getCount()==63,"shift-click plant limit broken");
            require(!menu.slots.get(3).mayPlace(new ItemStack(Items.OAK_LOG)),"output slot accepts user items");
        });
        test("serialization_roundtrip",()->{
            var be=blockedReady(pos()); var tag=state(be); var fresh=greenhouse(true); load(fresh,tag.copy());
            require(state(fresh).equals(tag),"block entity value I/O roundtrip changed inventory or growth");
        });
    }
    private void checkpoint() {
        test("persist_in_progress",()->{ var be=greenhouse(new BlockPos(X+100,Y,Z+100),false); inputs(be,Items.DIRT,Items.OAK_SAPLING,64); tick(be,137); });
        test("persist_blocked_mature",()->{ blockedReady(new BlockPos(X+103,Y,Z+100)); });
    }
    private void reload() {
        test("disk_reload_exact_progress",()->{
            var be=(GreenhouseBlockEntity)level.getBlockEntity(new BlockPos(X+100,Y,Z+100));
            require(be!=null && be.dataAccess().get(0)==137 && be.dataAccess().get(1)==450 && be.dataAccess().get(3)==3,"progress/boost changed across restart");
            require(be.getItem(2).getCount()==63,"reload spent fertilizer again"); tick(be,313);
            require(be.dataAccess().get(2)==1,"reloaded crop did not finish at original deadline");
        });
        test("disk_reload_pending_and_full_outputs",()->{
            var be=(GreenhouseBlockEntity)level.getBlockEntity(new BlockPos(X+103,Y,Z+100));
            require(be!=null && be.dataAccess().get(2)==1 && be.getItem(9).getCount()==64,"blocked mature state lost on restart");
            be.removeItem(9,64); tick(be,1); require(be.getItem(9).is(Items.OAK_LOG) && be.getItem(9).getCount()==2,"pending harvest lost or rerolled after restart");
        });
    }
    private void unloaded() {
        test("in_progress_chunk_really_unloaded",()->{
            require(level.getChunkSource().getChunkNow((X+100)>>4,(Z+100)>>4)==null,"fixture chunk still loaded; unload test not established");
        });
        test("blocked_harvest_chunk_really_unloaded",()->{
            require(level.getChunkSource().getChunkNow((X+103)>>4,(Z+100)>>4)==null,"fixture chunk still loaded; unload test not established");
        });
    }
    private void benchmark() {
        var machines=new ArrayList<GreenhouseBlockEntity>();
        for(int i=0;i<256;i++) { var be=greenhouse(true); inputs(be,Items.DIRT,Items.OAK_SAPLING,64); machines.add(be); }
        for(var be:machines) tick(be,20);
        long start=System.nanoTime();
        for(int t=0;t<200;t++) for(var be:machines) tick(be,1);
        double elapsed=(System.nanoTime()-start)/1_000_000.0;
        results.put("machines",256); results.put("simulated_server_ticks",200);
        results.put("total_machine_ticks",51200); results.put("average_ms_per_server_tick",elapsed/200);
        results.put("scope","256 active machines only; excludes full world tick, networking and rendering");
    }
    private static final class Listener implements ContainerListener {
        final int[] data=new int[4];
        public void slotChanged(AbstractContainerMenu menu,int slot,ItemStack stack) {}
        public void dataChanged(AbstractContainerMenu menu,int id,int value) { data[id]=value; }
    }
    private static final class RestrictedChest extends ChestBlockEntity implements WorldlyContainer {
        final int max; final boolean allow;
        RestrictedChest(BlockPos pos,BlockState state,int max,boolean allow) { super(pos,state); this.max=max; this.allow=allow; }
        @Override public int getMaxStackSize() { return max; }
        @Override public boolean canPlaceItem(int slot,ItemStack stack) { return slot==0; }
        public int[] getSlotsForFace(Direction face) { return new int[]{0}; }
        public boolean canPlaceItemThroughFace(int slot,ItemStack stack,Direction face) { return allow && face==Direction.UP; }
        public boolean canTakeItemThroughFace(int slot,ItemStack stack,Direction face) { return true; }
    }
}
