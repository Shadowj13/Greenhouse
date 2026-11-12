package it.bonsaigreenhouse.qa;

import com.mojang.authlib.GameProfile;
import it.bonsaigreenhouse.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Runtime crop and crafting acceptance tests, isolated from the shipped mod. */
final class CropAndRecipeTests {
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("greenhouse_qa");
    private static final java.util.function.Supplier<Item> FOREIGN_PLANT = ITEMS.registerSimpleItem("oak_sapling");
    private static final java.util.function.Supplier<Item> FOREIGN_SOIL = ITEMS.registerSimpleItem("dirt");
    private final ServerLevel level;
    private final BiConsumer<String,Runnable> test;
    private int next;
    CropAndRecipeTests(ServerLevel level,BiConsumer<String,Runnable> test) {
        this.level=level; this.test=test;
    }
    private static final List<Crop> ADDITIONS=List.of(
        Crop.GLOW_BERRIES,Crop.DANDELION,Crop.POPPY,Crop.BLUE_ORCHID,Crop.ALLIUM,
        Crop.AZURE_BLUET,Crop.RED_TULIP,Crop.ORANGE_TULIP,Crop.WHITE_TULIP,
        Crop.PINK_TULIP,Crop.OXEYE_DAISY,Crop.CORNFLOWER,Crop.LILY_OF_THE_VALLEY,
        Crop.SUNFLOWER,Crop.LILAC,Crop.PEONY,Crop.ROSE_BUSH,
        Crop.SEA_PICKLE,Crop.MOSS,Crop.CHORUS,
        Crop.VINE,Crop.GLOW_LICHEN,Crop.TWISTING_VINES,Crop.WEEPING_VINES,Crop.BIG_DRIPLEAF,Crop.SHELF_MUSHROOM);
    private record Spec(Crop crop, int duration, int min, int max, Set<String> bonus) {}
    private Spec spec(Crop crop) {
        // Independent acceptance limits: do not infer expected yields/timings
        // from the production fields whose behavior is being verified.
        int duration = switch(crop) { case WHEAT,CARROTS,POTATOES,BEETROOT,CACTUS,SUGAR_CANE,KELP,
                DANDELION,POPPY,BLUE_ORCHID,ALLIUM,AZURE_BLUET,RED_TULIP,ORANGE_TULIP,
                WHITE_TULIP,PINK_TULIP,OXEYE_DAISY,CORNFLOWER,LILY_OF_THE_VALLEY,
                SUNFLOWER,LILAC,PEONY,ROSE_BUSH,SEA_PICKLE,
                VINE,GLOW_LICHEN,TWISTING_VINES,WEEPING_VINES,BIG_DRIPLEAF -> 600; default -> 900; };
        int min = switch(crop) {
            case WHEAT,BEETROOT,NETHER_WART,MELON -> 2;
            default -> crop.isTree()?2:1;
        };
        int max = switch(crop) {
            case MELON -> 4;
            case CARROTS,POTATOES,COCOA,BAMBOO,SWEET_BERRIES,GLOW_BERRIES,RED_MUSHROOM,BROWN_MUSHROOM,CHORUS,SHELF_MUSHROOM -> min+1;
            default -> min;
        };
        Set<String> bonus = new HashSet<>();
        if(crop==Crop.CRIMSON_FUNGUS || crop==Crop.WARPED_FUNGUS) {
            bonus.addAll(List.of(crop==Crop.CRIMSON_FUNGUS?"nether_wart_block":"warped_wart_block",crop.plantId,"shroomlight"));
        } else if(crop.isTree()) {
            String leaf=switch(crop) {
                case AZALEA -> "azalea_leaves";
                case FLOWERING_AZALEA -> "flowering_azalea_leaves";
                case POPLAR_RED -> "red_poplar_leaves";
                case POPLAR_ORANGE -> "orange_poplar_leaves";
                case POPLAR_YELLOW -> "yellow_poplar_leaves";
                default -> crop.getSerializedName()+"_leaves";
            };
            bonus.addAll(List.of("stick",leaf,crop.plantId));
            if(crop==Crop.OAK || crop==Crop.DARK_OAK) bonus.add("apple");
            if(crop==Crop.MANGROVE) bonus.add("mangrove_roots");
        }
        if(crop==Crop.WHEAT) bonus.add("wheat_seeds");
        if(crop==Crop.BEETROOT) bonus.add("beetroot_seeds");
        if(crop==Crop.POTATOES) bonus.add("poisonous_potato");
        return new Spec(crop,duration,min,max,bonus);
    }
    private static void require(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
    /** A harvest is available: moved into the outputs, or waiting because they are full. */
    private static boolean ready(GreenhouseBlockEntity be) { return be.dataAccess().get(2)==1 || !be.getItem(9).isEmpty(); }
    private static Item item(String path) {
        var id=Identifier.withDefaultNamespace(path);
        require(BuiltInRegistries.ITEM.containsKey(id),"unregistered vanilla item: "+id);
        Item result=BuiltInRegistries.ITEM.getValue(id);
        require(result!=Items.AIR,"AIR resolved for "+id); return result;
    }
    private static boolean poplar(Crop crop) {
        return crop==Crop.POPLAR_RED || crop==Crop.POPLAR_ORANGE || crop==Crop.POPLAR_YELLOW;
    }
    private BlockPos pos() { return new BlockPos(53248+(next++%24)*3,100,51200+(next/24)*3); }
    private GreenhouseBlockEntity fresh(boolean automatic) {
        var p=pos(); level.setBlock(p,Blocks.AIR.defaultBlockState(),3);
        level.setBlock(p.below(),Blocks.STONE.defaultBlockState(),3);
        level.setBlock(p,(automatic?BonsaiGreenhouse.HOPPING:BonsaiGreenhouse.NORMAL).get().defaultBlockState(),3);
        return (GreenhouseBlockEntity)level.getBlockEntity(p);
    }
    private void tick(GreenhouseBlockEntity be,int ticks) {
        for(int i=0;i<ticks;i++) GreenhouseBlockEntity.serverTick(level,be.getBlockPos(),be.getBlockState(),be);
    }
    private CompoundTag state(GreenhouseBlockEntity be) { return be.saveWithoutMetadata(level.registryAccess()); }
    private void load(GreenhouseBlockEntity be,CompoundTag tag) {
        be.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),tag));
    }
    private void inputs(GreenhouseBlockEntity be,Crop crop,int bone) {
        be.setItem(0,new ItemStack(item(crop.soilId))); be.setItem(1,new ItemStack(item(crop.plantId)));
        if(bone>0) be.setItem(2,new ItemStack(Items.BONE_MEAL,bone));
        // Exercise every already-serialized poplar color as well as natural
        // selection (tested separately), including worlds saved before this fix.
        if(poplar(crop)) { var nbt=state(be); nbt.putString("crop",crop.getSerializedName()); load(be,nbt); }
    }
    private FakePlayer player(BlockPos near) {
        String name="GHCrops";
        var p=FakePlayerFactory.get(level,new GameProfile(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)),name));
        p.setGameMode(GameType.SURVIVAL); p.setPos(near.getX()+0.5,near.getY()+1,near.getZ()+0.5);
        p.getInventory().clearContent(); return p;
    }
    private Map<String,Integer> contents(Container inventory) {
        Map<String,Integer> result=new HashMap<>();
        for(int i=0;i<inventory.getContainerSize();i++) {
            var stack=inventory.getItem(i); if(!stack.isEmpty())
                result.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath(),stack.getCount(),Integer::sum);
        }
        return result;
    }
    private void harvest(Spec spec,Map<String,Integer> actual) {
        int main=actual.getOrDefault(spec.crop.dropId,0);
        require(main>=spec.min && main<=spec.max,"main yield out of bounds: "+actual);
        for(var e:actual.entrySet()) if(!e.getKey().equals(spec.crop.dropId))
            require(spec.bonus.contains(e.getKey()) && e.getValue()==1,"invalid bonus: "+actual);
    }
    void crops() {
        for(Crop crop:Crop.values()) if(crop.isCultivable()) {
            Spec s=spec(crop); String prefix="crop_"+crop.getSerializedName();
            test.accept(prefix+"_registry_and_substrates",()->{
                item(crop.dropId); for(String bonus:s.bonus) item(bonus);
                Crop found=Crop.of(new ItemStack(item(crop.plantId)));
                require(found==crop || (poplar(crop) && poplar(found)),"plant selects wrong crop: "+found);
                List<String> soils=new ArrayList<>(List.of(crop.soilId));
                if(crop.soilId.equals("dirt")) soils.addAll(List.of("grass_block","podzol","rooted_dirt","coarse_dirt"));
                if(crop.soilId.equals("sand")) soils.add("red_sand");
                if(crop.soilId.equals("mycelium")) soils.add("podzol");
                for(String soil:soils) require(crop.accepts(new ItemStack(item(soil))),"rejected substrate "+soil);
                require(!crop.accepts(new ItemStack(Items.COBBLESTONE)) && !crop.accepts(ItemStack.EMPTY),"invalid substrate accepted");
                require(crop.accepts(new ItemStack(Items.STONE))==(crop==Crop.GLOW_LICHEN),"stone accepted by wrong crop");
                var be=fresh(false); inputs(be,crop,1);
                be.setItem(0,new ItemStack(crop.soilId.equals("sand")?Items.DIRT:Items.SAND)); tick(be,1000);
                require(be.dataAccess().get(0)==0 && be.getItem(2).getCount()==1,"invalid combination grew or spent fertilizer");
            });
            test.accept(prefix+"_manual_timing_and_four_boosted_cycles",()->{
                var be=fresh(false); inputs(be,crop,0); tick(be,s.duration-1);
                require(be.getBlockState().getValue(GreenhouseBlock.CROP)==crop,"selected color/crop changed");
                require(!ready(be) && be.dataAccess().get(0)==s.duration-1,"early/late maturity");
                tick(be,1); require(ready(be),"maturity missing at expected tick");
                var p=player(be.getBlockPos()); be.collectBy(p); var collected=contents(p.getInventory()); harvest(s,collected);
                be.collectBy(p); require(contents(p.getInventory()).equals(collected),"duplicate harvest");
                be.setItem(2,new ItemStack(Items.BONE_MEAL));
                for(int cycle=0;cycle<4;cycle++) {
                    tick(be,s.duration/2-1); require(!ready(be),"boosted cycle matured early");
                    tick(be,1); require(ready(be),"boosted cycle did not mature");
                    p.getInventory().clearContent(); be.collectBy(p); harvest(s,contents(p.getInventory()));
                }
                tick(be,1); require(be.dataAccess().get(1)==s.duration && be.dataAccess().get(3)==0,"boost lasted beyond four cycles");
                require(be.getItem(0).getCount()==1 && be.getItem(1).getCount()==1 && be.getItem(2).isEmpty(),"incorrect input consumption");
            });
            test.accept(prefix+"_automatic_export",()->{
                var be=fresh(true); inputs(be,crop,0);
                level.setBlock(be.getBlockPos().below(),Blocks.CHEST.defaultBlockState(),3);
                var target=(Container)level.getBlockEntity(be.getBlockPos().below());
                tick(be,s.duration); require(be.getBlockState().getValue(GreenhouseBlock.CROP)==crop,"automatic color/crop changed");
                tick(be,8); harvest(s,contents(target));
                for(int slot=9;slot<18;slot++) require(be.getItem(slot).isEmpty(),"harvest left in machine after export");
                require(be.getItem(0).getCount()==1 && be.getItem(1).getCount()==1,"automatic inputs consumed");
            });
            test.accept(prefix+"_10000_seeded_drop_rolls",()->{
                var rng=RandomSource.create(0x475245454eL+crop.ordinal());
                Set<String> seen=new HashSet<>(); Set<Integer> mains=new HashSet<>();
                for(int i=0;i<10000;i++) {
                    Map<String,Integer> rolled=new HashMap<>();
                    for(var stack:crop.roll(rng)) {
                        require(!stack.isEmpty(),"empty product");
                        rolled.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath(),stack.getCount(),Integer::sum);
                    }
                    harvest(s,rolled); seen.addAll(rolled.keySet()); mains.add(rolled.get(crop.dropId));
                }
                require(seen.containsAll(s.bonus),"unreachable bonus "+s.bonus+"; observed "+seen);
                for(int count=s.min;count<=s.max;count++) require(mains.contains(count),"unreachable main yield "+count);
            });
        }
        for(Crop crop:ADDITIONS) if(crop.isCultivable()) test.accept("priority123_"+crop.getSerializedName()+"_reload_and_menu",()->{
            var be=fresh(false); inputs(be,crop,1);
            var p=player(be.getBlockPos()); var menu=new GreenhouseMenu(1,p.getInventory(),be);
            require(menu.slots.get(0).mayPlace(new ItemStack(item(crop.soilId))) &&
                menu.slots.get(1).mayPlace(new ItemStack(item(crop.plantId))),"menu rejects new crop");
            require(!crop.isTree(),"non-tree crop classified as a tree");
            tick(be,137); var saved=state(be); load(be,saved); tick(be,1);
            require(be.dataAccess().get(0)==138 && be.dataAccess().get(1)==spec(crop).duration/2 &&
                be.dataAccess().get(3)==3,"reload loses progress or boost");
            require(be.getBlockState().getValue(GreenhouseBlock.CROP)==crop,"reload changes plant identity");
            tick(be,spec(crop).duration/2-138); be.collectBy(p); harvest(spec(crop),contents(p.getInventory()));
            require(be.getItem(0).is(item(crop.soilId)) && be.getItem(1).is(item(crop.plantId)) &&
                be.getItem(1).getCount()==1,"harvest consumes original plant or soil");
        });
        test.accept("priority123_scope_and_excluded_plants",()->{
            require(Arrays.stream(Crop.values()).filter(Crop::isCultivable).count()==56 && ADDITIONS.size()==26,"unexpected crop scope");
            for(String path:List.of("golden_dandelion","wither_rose","torchflower","torchflower_seeds",
                    "pitcher_plant","pitcher_pod","red_shrub","moss_block","open_eyeblossom",
                    "closed_eyeblossom","dead_bush","pink_petals","wildflowers","seagrass","small_dripleaf"))
                require(Crop.of(new ItemStack(item(path)))==Crop.EMPTY,"unapproved crop accepted: "+path);
        });
        for(Crop crop:List.of(Crop.VINE,Crop.GLOW_LICHEN,Crop.TWISTING_VINES,Crop.WEEPING_VINES,Crop.BIG_DRIPLEAF))
            test.accept("beta5_"+crop.getSerializedName()+"_exact_inputs_and_conserved_harvest",()->{
                List<Item> accepted=switch(crop) {
                    case VINE,BIG_DRIPLEAF -> List.of(Items.DIRT,Items.GRASS_BLOCK,Items.PODZOL,Items.ROOTED_DIRT,Items.COARSE_DIRT);
                    case GLOW_LICHEN -> List.of(Items.STONE);
                    case TWISTING_VINES -> List.of(Items.WARPED_NYLIUM);
                    case WEEPING_VINES -> List.of(Items.CRIMSON_NYLIUM);
                    default -> throw new AssertionError("wrong beta5 test crop");
                };
                for(Item soil:accepted) {
                    var be=fresh(false); be.setItem(0,new ItemStack(soil));
                    be.setItem(1,new ItemStack(item(crop.plantId))); tick(be,599);
                    require(!ready(be),"new plant matured early");
                    tick(be,1); var player=player(be.getBlockPos()); be.collectBy(player);
                    require(contents(player.getInventory()).equals(Map.of(crop.plantId,1)),"wrong new-plant harvest");
                    be.collectBy(player);
                    require(contents(player.getInventory()).equals(Map.of(crop.plantId,1)),"duplicate new-plant harvest");
                    require(be.getItem(0).is(soil) && be.getItem(1).is(item(crop.plantId)),"new plant consumes original inputs");
                    tick(be,600); require(ready(be),"new plant fails to regrow");
                }
                for(Item soil:List.of(Items.DIRT,Items.STONE,Items.SAND,Items.WATER_BUCKET,Items.CRIMSON_NYLIUM,
                        Items.WARPED_NYLIUM,Items.NETHER_WART_BLOCK,Items.NETHERRACK,Items.CLAY))
                    require(crop.accepts(new ItemStack(soil))==accepted.contains(soil),"unexpected substrate for "+crop);
                require(!crop.accepts(new ItemStack(FOREIGN_SOIL.get())),"foreign dirt bypasses beta5 input contract");
            });
        test.accept("sea_pickle_water_and_original_input_contract",()->{
            require(Crop.SEA_PICKLE.accepts(new ItemStack(Items.WATER_BUCKET)),"water rejected");
            for(Item invalid:List.of(Items.BUCKET,Items.LAVA_BUCKET,Items.DIRT,Items.STONE,Items.TUBE_CORAL_BLOCK))
                require(!Crop.SEA_PICKLE.accepts(new ItemStack(invalid)),"invalid aquatic substrate accepted");
            require(Crop.of(new ItemStack(Items.SEA_PICKLE))==Crop.SEA_PICKLE,"wrong pickle input");
            var be=fresh(false); inputs(be,Crop.SEA_PICKLE,0); tick(be,600);
            var p=player(be.getBlockPos()); be.collectBy(p);
            require(contents(p.getInventory()).equals(Map.of("sea_pickle",1)),"wrong pickle harvest");
            require(be.getItem(0).is(Items.WATER_BUCKET) && be.getItem(1).is(Items.SEA_PICKLE),
                "pickle harvest consumes water or original colony");
        });
        test.accept("retired_moss_cannot_enter_plant_slot",()->{
            for(boolean automatic:List.of(false,true)) {
                var be=fresh(automatic);
                var moss=new ItemStack(Items.MOSS_BLOCK);
                require(Crop.of(moss)==Crop.EMPTY && !be.canPlaceItem(1,moss) &&
                    !be.canPlaceItemThroughFace(1,moss,net.minecraft.core.Direction.UP),"retired moss accepted");
                be.setItem(0,new ItemStack(Items.STONE)); be.setItem(1,moss);
                tick(be,600); require(be.getItem(1).isEmpty() && be.dataAccess().get(0)==0,"retired moss grew");
            }
        });
        test.accept("shelf_mushroom_grows_on_mycelium_and_podzol",()->{
            for(Item soil:List.of(Items.MYCELIUM,Items.PODZOL)) {
                var be=fresh(false);
                be.setItem(0,new ItemStack(soil)); be.setItem(1,new ItemStack(item("shelf_mushroom")));
                tick(be,900); var p=player(be.getBlockPos()); be.collectBy(p);
                var result=contents(p.getInventory());
                require(result.size()==1 && result.getOrDefault("shelf_mushroom",0)>=1 &&
                    result.getOrDefault("shelf_mushroom",0)<=2,"incorrect shelf mushroom harvest");
                require(be.getItem(0).is(soil) && be.getItem(1).is(item("shelf_mushroom")),"inputs consumed");
            }
            for(Item soil:List.of(Items.DIRT,Items.STONE,Items.POPLAR_LOG,Items.WATER_BUCKET,Items.MOSS_BLOCK))
                require(!Crop.SHELF_MUSHROOM.accepts(new ItemStack(soil)),"unsupported shelf mushroom substrate");
        });
        for(Crop crop:List.of(Crop.MOSS,Crop.WEEPING_VINES))
            for(boolean automatic:List.of(false,true))
                test.accept("legacy_ready_"+crop.getSerializedName()+(automatic?"_auto":"_manual"),()->{
                    var be=fresh(automatic);
                    Item soil=crop==Crop.MOSS?Items.STONE:Items.NETHER_WART_BLOCK;
                    Item plant=item(crop.plantId);
                    loadLegacyHarvest(be,crop,soil,plant);
                    if(automatic) level.setBlock(be.getBlockPos().below(),Blocks.CHEST.defaultBlockState(),3);
                    tick(be,1);
                    var p=player(be.getBlockPos());
                    if(!automatic) be.collectBy(p);
                    tick(be,1200);
                    var result=automatic?contents((Container)level.getBlockEntity(be.getBlockPos().below())):contents(p.getInventory());
                    require(result.equals(Map.of(crop.plantId,1)),"legacy harvest lost or duplicated: "+result);
                    require(be.getItem(0).is(soil) && be.getItem(1).is(plant) &&
                        be.getItem(2).is(Items.BONE_MEAL) && be.dataAccess().get(0)==0,"legacy inputs lost or grew again");
                });
        test.accept("chorus_requires_flower_and_end_stone_and_drops_fruit_only",()->{
            require(Crop.of(new ItemStack(Items.CHORUS_FLOWER))==Crop.CHORUS,"flower rejected");
            for(Item invalid:List.of(Items.CHORUS_FRUIT,Items.POPPED_CHORUS_FRUIT,Items.CHORUS_PLANT))
                require(Crop.of(new ItemStack(invalid))==Crop.EMPTY,"fruit/branch bypasses flower acquisition");
            require(Crop.CHORUS.accepts(new ItemStack(Items.END_STONE)),"end stone rejected");
            for(Item invalid:List.of(Items.DIRT,Items.STONE,Items.END_STONE_BRICKS,Items.WATER_BUCKET))
                require(!Crop.CHORUS.accepts(new ItemStack(invalid)),"invalid chorus substrate accepted");
            var be=fresh(false); inputs(be,Crop.CHORUS,0); tick(be,900);
            var p=player(be.getBlockPos()); be.collectBy(p);
            var got=contents(p.getInventory());
            require(got.size()==1 && got.getOrDefault("chorus_fruit",0)>=1 &&
                got.getOrDefault("chorus_fruit",0)<=2,"chorus generated a flower or processed product");
            require(be.getItem(0).is(Items.END_STONE) && be.getItem(1).is(Items.CHORUS_FLOWER),
                "chorus harvest consumes original flower or substrate");
        });
        test.accept("kelp_aquatic_inputs_reload_and_menu",()->{
            var be=fresh(false); inputs(be,Crop.KELP,0);
            for(Item invalid:List.of(Items.BUCKET,Items.LAVA_BUCKET,Items.DIRT,Items.SAND))
                require(!Crop.KELP.accepts(new ItemStack(invalid)),"non-water substrate accepted");
            require(Crop.of(new ItemStack(Items.DRIED_KELP))==Crop.EMPTY,"dried kelp can be planted");
            var p=player(be.getBlockPos());
            var menu=new GreenhouseMenu(1,p.getInventory(),be);
            require(menu.slots.get(0).mayPlace(new ItemStack(Items.WATER_BUCKET)) &&
                menu.slots.get(1).mayPlace(new ItemStack(Items.KELP)),"menu rejects aquatic inputs");
            tick(be,187); var saved=state(be); load(be,saved); tick(be,1);
            require(be.dataAccess().get(0)==188 && be.dataAccess().get(1)==600,"reload resets kelp progress");
            require(be.getBlockState().getValue(GreenhouseBlock.CROP)==Crop.KELP,"reload loses kelp identity");
            tick(be,412); be.collectBy(p);
            require(contents(p.getInventory()).equals(Map.of("kelp",1)),"wrong kelp harvest after reload");
            require(be.getItem(0).is(Items.WATER_BUCKET) && be.getItem(0).getCount()==1 &&
                be.getItem(1).is(Items.KELP),"kelp consumes water/plant");
        });
        test.accept("poplar_natural_color_selection_and_reload",()->{
            Set<Crop> seen=EnumSet.noneOf(Crop.class);
            for(int seed=0;seed<48;seed++) {
                var be=fresh(false); be.setItem(0,new ItemStack(Items.DIRT)); be.setItem(1,new ItemStack(Items.POPLAR_SAPLING));
                level.getRandom().setSeed(seed); tick(be,1);
                Crop color=be.getBlockState().getValue(GreenhouseBlock.CROP); require(poplar(color),"not a poplar variant"); seen.add(color);
                tick(be,120); var saved=state(be); load(be,saved); tick(be,1);
                require(be.getBlockState().getValue(GreenhouseBlock.CROP)==color && be.dataAccess().get(0)==122,"reload reset color/progress");
                tick(be,778); var p=player(be.getBlockPos()); be.collectBy(p); tick(be,1);
                require(be.getBlockState().getValue(GreenhouseBlock.CROP)==color,"harvest rerolled established tree color");
            }
            require(seen.size()==3,"unreachable poplar colors; observed "+seen);
        });
        test.accept("foreign_namespace_lookalikes_rejected",()->{
            require(Crop.of(new ItemStack(FOREIGN_PLANT.get()))==Crop.EMPTY,"foreign oak_sapling accepted as vanilla");
            require(!Crop.OAK.accepts(new ItemStack(FOREIGN_SOIL.get())),"foreign dirt accepted as vanilla");
            var be=fresh(false);
            require(!be.canPlaceItem(0,new ItemStack(FOREIGN_SOIL.get())) && !be.canPlaceItem(1,new ItemStack(FOREIGN_PLANT.get())),"foreign inputs enter menu/hopper slots");
        });
        test.accept("ready_harvest_survives_input_change_manual",()->{
            // Full internal slots make the ready harvest wait inside the greenhouse.
            var be=fresh(false); loadOutputs(be,9); inputs(be,Crop.OAK,0); tick(be,900);
            require(be.dataAccess().get(2)==1,"oak fixture not waiting");
            be.setItem(1,ItemStack.EMPTY); tick(be,20);
            require(be.dataAccess().get(2)==1 && be.getBlockState().getValue(GreenhouseBlock.CROP)==Crop.OAK,"removing the plant discarded the ready harvest");
            be.setItem(1,new ItemStack(Items.BIRCH_SAPLING)); tick(be,20);
            be.setItem(0,ItemStack.EMPTY); tick(be,20);
            require(be.dataAccess().get(2)==1 && be.getBlockState().getValue(GreenhouseBlock.CROP)==Crop.OAK,"changing plant or soil discarded the ready harvest");
            var p=player(be.getBlockPos()); be.collectBy(p);
            var got=contents(p.getInventory());
            require(got.getOrDefault("oak_log",0)==2 && got.getOrDefault("cobblestone",0)==576,"ready harvest or outputs not returned: "+got);
            be.setItem(0,new ItemStack(Items.DIRT)); tick(be,1);
            require(be.getBlockState().getValue(GreenhouseBlock.CROP)==Crop.BIRCH && be.dataAccess().get(2)==0 &&
                be.dataAccess().get(0)==1,"new crop did not start after collecting the old harvest");
        });
        test.accept("ready_harvest_survives_input_change_automatic",()->{
            var be=fresh(true);
            loadOutputs(be,9); inputs(be,Crop.OAK,0); tick(be,900);
            require(be.dataAccess().get(2)==1 && be.getBlockState().getValue(GreenhouseBlock.CROP)==Crop.OAK,
                "oak fixture did not mature with blocked outputs");
            be.setItem(1,new ItemStack(Items.BIRCH_SAPLING)); tick(be,40);
            require(be.dataAccess().get(2)==1 && be.getBlockState().getValue(GreenhouseBlock.CROP)==Crop.OAK,"blocked harvest discarded by plant change");
            // Oak can roll logs plus up to three different bonus items.
            for (int slot=9;slot<13;slot++) be.removeItem(slot,64);
            tick(be,1);
            require(be.getItem(9).is(Items.OAK_LOG) && be.getItem(9).getCount()==2,"blocked harvest not exported after plant change");
            tick(be,1);
            require(be.getBlockState().getValue(GreenhouseBlock.CROP)==Crop.BIRCH && be.dataAccess().get(2)==0,"new crop did not start after export");
        });
        test.accept("interrupted_boosted_cycle_refunds_bone_meal_charge",()->{
            var be=fresh(false); inputs(be,Crop.OAK,1); tick(be,100);
            require(be.dataAccess().get(1)==450 && be.dataAccess().get(3)==3 && be.getItem(2).isEmpty(),"boosted fixture not started");
            be.setItem(1,ItemStack.EMPTY); tick(be,1);
            require(be.dataAccess().get(3)==4 && be.dataAccess().get(0)==0,"interrupted boosted cycle not refunded");
            be.setItem(1,new ItemStack(Items.OAK_SAPLING));
            var p=player(be.getBlockPos());
            for(int cycle=0;cycle<4;cycle++) {
                tick(be,449); require(!ready(be),"refunded boosted cycle matured early");
                tick(be,1); require(ready(be),"refunded charge did not give four boosted cycles");
                be.collectBy(p);
            }
            tick(be,1); require(be.dataAccess().get(1)==900 && be.dataAccess().get(3)==0,"refund granted more than four boosted cycles");
        });
        test.accept("automation_keeps_single_soil_and_plant",()->{
            var up=net.minecraft.core.Direction.UP;
            var be=fresh(true);
            require(be.canPlaceItemThroughFace(0,new ItemStack(Items.DIRT),up) &&
                be.canPlaceItemThroughFace(1,new ItemStack(Items.OAK_SAPLING),up),"automation cannot fill empty input slots");
            be.setItem(0,new ItemStack(Items.DIRT)); be.setItem(1,new ItemStack(Items.OAK_SAPLING));
            require(!be.canPlaceItemThroughFace(0,new ItemStack(Items.DIRT),up) &&
                !be.canPlaceItemThroughFace(1,new ItemStack(Items.OAK_SAPLING),up),"automation stacks extra soil or plants");
            require(be.canPlaceItemThroughFace(2,new ItemStack(Items.BONE_MEAL),up) &&
                be.canPlaceItemThroughFace(2,new ItemStack(Items.BONE_MEAL),net.minecraft.core.Direction.NORTH),"automation rejects bone meal");
            be.setItem(1,new ItemStack(Items.BIRCH_SAPLING));
            require(be.getItem(1).is(Items.BIRCH_SAPLING) && be.getItem(1).getCount()==1,"player/menu replacement of an occupied slot broken");
        });
        test.accept("crop_catalog_matches_rules",()->{
            // The JEI catalog reads soilIds/minYield/maxYield/bonuses: check them
            // against the independent acceptance limits used by every crop test.
            for(Crop crop:Crop.values()) if(crop.isCultivable()) {
                Spec s=spec(crop);
                Set<String> soils=new HashSet<>(List.of(crop.soilId));
                if(crop.soilId.equals("dirt")) soils.addAll(List.of("grass_block","podzol","rooted_dirt","coarse_dirt"));
                if(crop.soilId.equals("sand")) soils.add("red_sand");
                if(crop.soilId.equals("mycelium")) soils.add("podzol");
                require(new HashSet<>(crop.soilIds()).equals(soils),"catalog soils differ for "+crop+": "+crop.soilIds());
                for(String soil:crop.soilIds()) require(crop.accepts(new ItemStack(item(soil))),"catalog soil rejected: "+soil);
                require(crop.minYield()==s.min && crop.maxYield()==s.max,"catalog yield differs for "+crop);
                Set<String> bonus=new HashSet<>();
                for(var b:crop.bonuses()) {
                    item(b.itemId()); bonus.add(b.itemId());
                    require(b.chancePercent()>0 && b.chancePercent()<100,"invalid bonus chance for "+crop);
                }
                require(bonus.equals(s.bonus),"catalog bonuses differ for "+crop+": "+bonus);
            }
        });
        test.accept("comparator_normal_progress_and_ready",()->{
            var be=fresh(false); var at=be.getBlockPos();
            require(be.getBlockState().hasAnalogOutputSignal(),"greenhouse has no comparator output");
            require(signal(be)==0,"empty greenhouse emits a signal");
            inputs(be,Crop.OAK,0); tick(be,1);
            require(signal(be)==1,"growing crop does not start at signal 1");
            tick(be,449); require(signal(be)==8,"half-grown crop signal: "+signal(be));
            tick(be,450); require(ready(be) && be.dataAccess().get(2)==0 && signal(be)==0,"harvest not moved into the outputs");
            // Three more harvests: the fourth waits and the comparator reads 15.
            tick(be,2700);
            require(be.dataAccess().get(2)==1 && be.dataAccess().get(5)==4 && signal(be)==15,"four ready harvests do not emit 15");
            be.collectBy(player(at)); require(signal(be)==0 && !ready(be),"signal or outputs not reset after harvest");
        });
        test.accept("comparator_automatic_output_fill",()->{
            var be=fresh(true);
            require(signal(be)==0,"empty outputs emit a signal");
            loadOutputs(be,1); require(signal(be)==2,"one full output stack: "+signal(be));
            loadOutputs(be,9); require(signal(be)==15,"full outputs: "+signal(be));
        });
        test.accept("redstone_pauses_growth",()->{
            var be=fresh(false); var side=be.getBlockPos().east();
            inputs(be,Crop.OAK,1); tick(be,100);
            level.setBlock(side,Blocks.REDSTONE_BLOCK.defaultBlockState(),3); tick(be,500);
            require(be.dataAccess().get(4)==1 && be.dataAccess().get(0)==100,"powered greenhouse kept growing");
            level.setBlock(side,Blocks.AIR.defaultBlockState(),3); tick(be,350);
            require(be.dataAccess().get(4)==0 && ready(be),"unpowered greenhouse did not resume");
            var idle=fresh(false); var idleSide=idle.getBlockPos().east();
            level.setBlock(idleSide,Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
            inputs(idle,Crop.OAK,1); tick(idle,200);
            require(idle.dataAccess().get(1)==0 && idle.getItem(2).getCount()==1,"powered greenhouse started a cycle or spent bone meal");
            level.setBlock(idleSide,Blocks.AIR.defaultBlockState(),3);
        });
        automationAndData();
    }
    void automationAndData() {
        test.accept("item_capability_respects_faces",()->{
            var be=fresh(true); var at=be.getBlockPos();
            var cap=net.neoforged.neoforge.capabilities.Capabilities.Item.BLOCK;
            var up=level.getCapability(cap,at,net.minecraft.core.Direction.UP);
            var side=level.getCapability(cap,at,net.minecraft.core.Direction.NORTH);
            var down=level.getCapability(cap,at,net.minecraft.core.Direction.DOWN);
            require(up!=null && side!=null && down!=null,"greenhouse exposes no item handler to other mods");
            require(level.getCapability(cap,at,null)==null,"unsided handler bypasses face rules");
            int soil,plant,bone,wrongSide;
            try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                soil=net.neoforged.neoforge.transfer.ResourceHandlerUtil.insertStacking(up,net.neoforged.neoforge.transfer.item.ItemResource.of(Items.DIRT),64,tx);
                plant=net.neoforged.neoforge.transfer.ResourceHandlerUtil.insertStacking(up,net.neoforged.neoforge.transfer.item.ItemResource.of(Items.OAK_SAPLING),64,tx);
                bone=net.neoforged.neoforge.transfer.ResourceHandlerUtil.insertStacking(side,net.neoforged.neoforge.transfer.item.ItemResource.of(Items.BONE_MEAL),64,tx);
                wrongSide=net.neoforged.neoforge.transfer.ResourceHandlerUtil.insertStacking(side,net.neoforged.neoforge.transfer.item.ItemResource.of(Items.DIRT),64,tx);
                tx.commit();
            }
            require(soil==1 && plant==1 && bone==64 && wrongSide==0,"handler insertion limits: "+soil+"/"+plant+"/"+bone+"/"+wrongSide);
            require(be.getItem(0).getCount()==1 && be.getItem(1).getCount()==1 && be.getItem(2).getCount()==64,"handler stacked inputs");
            int stolen;
            try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                stolen=up.extract(net.neoforged.neoforge.transfer.item.ItemResource.of(Items.DIRT),1,tx)
                    +down.extract(net.neoforged.neoforge.transfer.item.ItemResource.of(Items.OAK_SAPLING),1,tx);
                tx.commit();
            }
            require(stolen==0 && be.getItem(0).is(Items.DIRT) && be.getItem(1).is(Items.OAK_SAPLING),"inputs extractable by other mods");
            tick(be,450);
            require(be.getItem(9).is(Items.OAK_LOG) && be.getItem(9).getCount()==2,"boosted harvest not stored");
            int first,rest;
            try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                first=down.extract(net.neoforged.neoforge.transfer.item.ItemResource.of(Items.OAK_LOG),1,tx);
                tx.commit();
            }
            require(first==1 && be.getItem(9).getCount()==1,"partial extraction duplicated or lost logs");
            try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                rest=down.extract(net.neoforged.neoforge.transfer.item.ItemResource.of(Items.OAK_LOG),64,tx);
                tx.commit();
            }
            require(rest==1 && be.getItem(9).isEmpty(),"remaining products not extractable from below");
        });
        test.accept("axe_tag_and_recipe_book_advancements",()->{
            for(var block:List.of(BonsaiGreenhouse.NORMAL.get(),BonsaiGreenhouse.HOPPING.get())) {
                var state=block.defaultBlockState();
                require(state.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_AXE),"greenhouse not mineable with an axe");
                require(new ItemStack(Items.IRON_AXE).getDestroySpeed(state)>1f,"axe does not speed up breaking");
            }
            var advancements=level.getServer().getAdvancements();
            for(String path:List.of("recipes/misc/greenhouse","recipes/misc/automatic_greenhouse"))
                require(advancements.get(Identifier.fromNamespaceAndPath(BonsaiGreenhouse.ID,path))!=null,"missing recipe advancement "+path);
        });
        packing();
    }
    // ---- Contents kept inside the dropped item and through the upgrade recipe ----
    private List<net.minecraft.world.entity.item.ItemEntity> dropEntities(BlockPos at) {
        return level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(at).inflate(1.5));
    }
    private List<ItemStack> collectDrops(BlockPos at) {
        List<ItemStack> result=new ArrayList<>();
        for(var e:dropEntities(at)) { result.add(e.getItem().copy()); e.discard(); }
        return result;
    }
    private Map<String,Integer> packedContents(ItemStack item) {
        Map<String,Integer> result=new HashMap<>();
        var contents=item.get(net.minecraft.core.component.DataComponents.CONTAINER);
        if(contents!=null) contents.nonEmptyItemCopyStream().forEach(s->
            result.merge(BuiltInRegistries.ITEM.getKey(s.getItem()).getPath(),s.getCount(),Integer::sum));
        return result;
    }
    void packing() {
        test.accept("normal_greenhouse_keeps_four_harvests",()->{
            var be=fresh(false); var at=be.getBlockPos();
            inputs(be,Crop.DANDELION,0);
            for(int n=1;n<=3;n++) {
                tick(be,600);
                require(be.dataAccess().get(5)==n && be.dataAccess().get(2)==0,"harvest "+n+" not kept: "+be.dataAccess().get(5));
            }
            tick(be,600);
            require(be.dataAccess().get(5)==4 && be.dataAccess().get(2)==1 && signal(be)==15,"fourth harvest does not fill the greenhouse");
            tick(be,600);
            require(be.dataAccess().get(0)==600 && be.dataAccess().get(5)==4,"full greenhouse kept growing");
            var down=net.minecraft.core.Direction.DOWN;
            require(be.getSlotsForFace(down).length==0 && !be.canTakeItemThroughFace(9,be.getItem(9),down),"stored harvests extractable by hoppers");
            var handler=level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.Item.BLOCK,at,down);
            int taken=0;
            if(handler!=null) try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                taken=handler.extract(net.neoforged.neoforge.transfer.item.ItemResource.of(Items.DANDELION),64,tx); tx.commit();
            }
            require(taken==0,"stored harvests extractable by pipes");
            require(level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.Item.BLOCK,at,null)==null,
                "stored harvests extractable by an unsided handler");
            var menu=new GreenhouseMenu(1,player(at).getInventory(),be);
            require(menu.slots.size()==3+36 && menu.readyHarvests()==4,"normal greenhouse menu shows outputs or wrong count");
            var p=player(at); be.collectBy(p);
            require(contents(p.getInventory()).equals(Map.of("dandelion",4)) && be.dataAccess().get(5)==0 && be.dataAccess().get(2)==0,
                "harvest did not hand over all four harvests: "+contents(p.getInventory()));
            tick(be,1); require(be.dataAccess().get(0)==1,"growth did not resume after harvest");
        });
        test.accept("remaining_time_shown_by_gui_and_jade",()->{
            require(GreenhouseBlockEntity.remainingSeconds(0,900)==45 && GreenhouseBlockEntity.remainingSeconds(899,900)==1
                && GreenhouseBlockEntity.remainingSeconds(900,900)==0 && GreenhouseBlockEntity.remainingSeconds(0,450)==23
                && GreenhouseBlockEntity.remainingSeconds(10,0)==0,"remaining seconds rounding");
            var be=fresh(false); inputs(be,Crop.OAK,0); tick(be,300);
            require(be.activeCrop()==Crop.OAK && GreenhouseBlockEntity.remainingSeconds(be.dataAccess().get(0),be.dataAccess().get(1))==30,
                "remaining time of a growing crop");
        });
        test.accept("survival_break_keeps_contents_and_restores_growth",()->{
            var be=fresh(false); var at=be.getBlockPos(); collectDrops(at);
            inputs(be,Crop.OAK,3); tick(be,100);
            require(be.dataAccess().get(0)==100 && be.dataAccess().get(3)==3,"fixture not growing");
            require(player(at).gameMode.destroyBlock(at),"survival break failed");
            var drops=collectDrops(at);
            require(drops.size()==1 && drops.get(0).is(BonsaiGreenhouse.NORMAL_ITEM.get()) && drops.get(0).getCount()==1,"expected one packed greenhouse, got "+drops);
            require(packedContents(drops.get(0)).equals(Map.of("dirt",1,"oak_sapling",1,"bone_meal",2)),"packed contents: "+packedContents(drops.get(0)));
            var copy=fresh(false); copy.applyComponentsFromItemStack(drops.get(0)); tick(copy,1);
            require(copy.getItem(0).is(Items.DIRT) && copy.getItem(1).is(Items.OAK_SAPLING) && copy.getItem(2).getCount()==2,"inputs not restored");
            require(copy.dataAccess().get(0)==101 && copy.dataAccess().get(1)==450 && copy.dataAccess().get(3)==3,"growth state not restored");
            require(copy.getBlockState().getValue(GreenhouseBlock.CROP)==Crop.OAK,"restored crop not shown");
        });
        test.accept("creative_break_drops_packed_item_once",()->{
            var be=fresh(true); var at=be.getBlockPos(); collectDrops(at);
            loadOutputs(be,2); be.setItem(0,new ItemStack(Items.DIRT)); be.setItem(1,new ItemStack(Items.OAK_SAPLING));
            var p=player(at); p.setGameMode(GameType.CREATIVE);
            require(p.gameMode.destroyBlock(at),"creative break failed");
            var drops=collectDrops(at);
            require(drops.size()==1 && drops.get(0).is(BonsaiGreenhouse.HOPPING_ITEM.get()),"creative break drops: "+drops);
            require(packedContents(drops.get(0)).equals(Map.of("cobblestone",128,"dirt",1,"oak_sapling",1)),"creative packed contents: "+packedContents(drops.get(0)));
        });
        test.accept("destroy_with_drops_keeps_ready_harvest_once",()->{
            for (boolean exploded : new boolean[]{false,true}) {
                var be=fresh(false); var at=be.getBlockPos(); collectDrops(at);
                inputs(be,Crop.OAK,0); tick(be,900); require(ready(be),"fixture not mature");
                if (exploded) {
                    var explosion=new ServerExplosion(level,null,null,null,
                        Vec3.atCenterOf(at),2.0f,false,Explosion.BlockInteraction.DESTROY);
                    be.getBlockState().onExplosionHit(level,at,explosion,(stack,pos) -> Block.popResource(level,pos,stack));
                    require(level.getBlockState(at).isAir(),"explosion did not break the greenhouse");
                } else require(level.destroyBlock(at,true),"destroyBlock did not break the greenhouse");
                var drops=collectDrops(at);
                require(drops.size()==1 && drops.get(0).is(BonsaiGreenhouse.NORMAL_ITEM.get()),"unexpected drops after destruction: "+drops);
                var packed=packedContents(drops.get(0));
                require(packed.getOrDefault("oak_log",0)==2 && packed.getOrDefault("dirt",0)==1
                    && packed.getOrDefault("oak_sapling",0)>=1,"ready harvest or inputs not packed: "+packed);
                var copy=fresh(false); copy.applyComponentsFromItemStack(drops.get(0)); tick(copy,1);
                require(ready(copy),"restored harvest not ready");
                var p=player(copy.getBlockPos()); copy.collectBy(p);
                require(contents(p.getInventory()).getOrDefault("oak_log",0)==2,"restored harvest lost or duplicated");
            }
        });
        test.accept("empty_break_preserves_charges_or_drops_plain_item",()->{
            var be=fresh(false); var at=be.getBlockPos(); collectDrops(at);
            require(player(at).gameMode.destroyBlock(at),"survival break failed");
            var drops=collectDrops(at);
            require(drops.size()==1 && ItemStack.isSameItemSameComponents(drops.get(0),new ItemStack(BonsaiGreenhouse.NORMAL_ITEM.get())),
                "empty greenhouse item carries data and will not stack: "+drops);
            for (var mode : List.of(GameType.SURVIVAL,GameType.CREATIVE)) {
                var charged=fresh(false); var chargedPos=charged.getBlockPos(); collectDrops(chargedPos);
                inputs(charged,Crop.OAK,1); tick(charged,1);
                charged.removeItem(0,1); charged.removeItem(1,1); tick(charged,1);
                require(charged.isEmpty() && charged.dataAccess().get(3)==4,"fixture must hold only four charges");
                var p=player(chargedPos); p.setGameMode(mode);
                require(p.gameMode.destroyBlock(chargedPos),"charged greenhouse break failed in "+mode);
                var chargedDrops=collectDrops(chargedPos);
                require(chargedDrops.size()==1 && chargedDrops.get(0).is(BonsaiGreenhouse.NORMAL_ITEM.get()),
                    "charged greenhouse drops in "+mode+": "+chargedDrops);
                var copy=fresh(false); copy.applyComponentsFromItemStack(chargedDrops.get(0)); tick(copy,1);
                require(copy.isEmpty() && copy.dataAccess().get(3)==4,"replaced greenhouse lost charges in "+mode);
            }
        });
        test.accept("upgrade_recipe_keeps_contents",()->{
            var all=NonNullList.withSize(36,ItemStack.EMPTY);
            all.set(0,new ItemStack(Items.DIRT)); all.set(1,new ItemStack(Items.OAK_SAPLING));
            all.set(2,new ItemStack(Items.BONE_MEAL,5)); all.set(27,new ItemStack(Items.OAK_LOG,2));
            var packed=new ItemStack(BonsaiGreenhouse.NORMAL_ITEM.get());
            packed.set(net.minecraft.core.component.DataComponents.CONTAINER,net.minecraft.world.item.component.ItemContainerContents.fromItems(all));
            var cells=new ArrayList<>(grid(true,Items.OAK_SLAB,1)); cells.set(4,packed);
            var input=CraftingInput.of(3,3,cells);
            var holder=recipe(cells).orElseThrow(()->new AssertionError("packed greenhouse not accepted by upgrade recipe"));
            var result=holder.value().assemble(input);
            require(result.is(BonsaiGreenhouse.HOPPING_ITEM.get()) && result.getCount()==1,"upgrade result: "+result);
            require(packedContents(result).equals(packedContents(packed)),"upgrade lost contents: "+packedContents(result));
            var plain=holder.value().assemble(CraftingInput.of(3,3,grid(true,Items.OAK_SLAB,1)));
            require(ItemStack.isSameItemSameComponents(plain,new ItemStack(BonsaiGreenhouse.HOPPING_ITEM.get())),"plain upgrade gained data");
        });
    }
    private int signal(GreenhouseBlockEntity be) {
        return be.getBlockState().getAnalogOutputSignal(level,be.getBlockPos(),net.minecraft.core.Direction.NORTH);
    }
    private void loadOutputs(GreenhouseBlockEntity be,int fullSlots) {
        var slots=NonNullList.withSize(27,ItemStack.EMPTY);
        for(int i=0;i<fullSlots;i++) slots.set(9+i,new ItemStack(Items.COBBLESTONE,64));
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        ContainerHelper.saveAllItems(out,slots);
        load(be,out.buildResult());
    }
    private void loadLegacyHarvest(GreenhouseBlockEntity be,Crop crop,Item soil,Item plant) {
        var slots=NonNullList.withSize(27,ItemStack.EMPTY);
        slots.set(0,new ItemStack(soil)); slots.set(1,new ItemStack(plant)); slots.set(2,new ItemStack(Items.BONE_MEAL));
        var pending=NonNullList.withSize(9,ItemStack.EMPTY);
        pending.set(0,new ItemStack(plant));
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        ContainerHelper.saveAllItems(out,slots);
        ContainerHelper.saveAllItems(out.child("pending"),pending);
        var tag=out.buildResult();
        tag.putString("crop",crop.getSerializedName()); tag.putInt("elapsed",600); tag.putInt("duration",600);
        tag.putBoolean("mature",true); tag.putBoolean("newCycle",false);
        load(be,tag);
    }
    private BlockPos persisted(Crop crop) {
        return new BlockPos(53248+ADDITIONS.indexOf(crop)*3,100,51400);
    }
    void checkpointAdditions() {
        for(Crop crop:ADDITIONS) if(crop.isCultivable()) test.accept("persist_priority123_"+crop.getSerializedName(),()->{
            BlockPos at=persisted(crop);
            level.setBlock(at,Blocks.AIR.defaultBlockState(),3);
            level.setBlock(at.below(),Blocks.STONE.defaultBlockState(),3);
            level.setBlock(at,BonsaiGreenhouse.NORMAL.get().defaultBlockState(),3);
            var be=(GreenhouseBlockEntity)level.getBlockEntity(at);
            inputs(be,crop,1); tick(be,137);
        });
    }
    void reloadAdditions() { reloadAdditions(ADDITIONS); }
    void reloadBeta3Additions() { reloadAdditions(ADDITIONS.subList(0,17)); }
    void reloadBeta4Additions() {
        reloadAdditions(ADDITIONS.subList(0,20));
        verifyRetiredInput(Crop.MOSS,Items.STONE);
    }
    void reloadBeta5Additions() {
        reloadAdditions(ADDITIONS.subList(0,25).stream().filter(crop->crop!=Crop.WEEPING_VINES).toList());
        verifyRetiredInput(Crop.MOSS,Items.STONE);
        verifyRetiredInput(Crop.WEEPING_VINES,Items.NETHER_WART_BLOCK);
    }
    private void verifyRetiredInput(Crop crop,Item soil) {
        test.accept("disk_reload_retired_"+crop.getSerializedName(),()->{
            var be=(GreenhouseBlockEntity)level.getBlockEntity(persisted(crop));
            require(be!=null && be.getItem(0).is(soil) && be.getItem(1).is(item(crop.plantId)),"legacy inputs missing");
            tick(be,1200);
            require(be.dataAccess().get(0)==0 && be.dataAccess().get(2)==0 && be.getItem(0).is(soil) &&
                be.getItem(1).is(item(crop.plantId)),"legacy crop grew or lost inputs");
            if(crop==Crop.WEEPING_VINES) {
                be.setItem(0,new ItemStack(Items.CRIMSON_NYLIUM));
                tick(be,300);
                var p=player(be.getBlockPos()); be.collectBy(p);
                require(contents(p.getInventory()).equals(Map.of(crop.plantId,1)),"replacement soil cannot resume harvest");
            }
        });
    }
    private void reloadAdditions(List<Crop> restored) {
        for(Crop crop:restored) if(crop.isCultivable()) test.accept("disk_reload_priority123_"+crop.getSerializedName(),()->{
            var be=(GreenhouseBlockEntity)level.getBlockEntity(persisted(crop));
            require(be!=null && be.dataAccess().get(0)==137 &&
                be.dataAccess().get(1)==spec(crop).duration/2 && be.dataAccess().get(3)==3,
                "new crop progress/boost changed across process restart");
            require(be.getBlockState().getValue(GreenhouseBlock.CROP)==crop,"disk reload loses plant identity");
            tick(be,spec(crop).duration/2-137);
            require(ready(be),"disk-restored crop misses original maturity deadline");
            var p=player(be.getBlockPos()); be.collectBy(p); harvest(spec(crop),contents(p.getInventory()));
            require(be.getItem(0).is(item(crop.soilId)) && be.getItem(1).is(item(crop.plantId)),
                "disk-restored harvest loses inputs");
        });
    }
    private List<ItemStack> grid(boolean automatic,Item slab,int count) {
        Item g=Items.GLASS_PANE;
        Item[] cells=automatic?new Item[]{g,g,g,g,BonsaiGreenhouse.NORMAL_ITEM.get(),g,Items.BARREL,Items.HOPPER,Items.BARREL}
            :new Item[]{g,g,g,g,Items.FLOWER_POT,g,slab,slab,slab};
        return Arrays.stream(cells).map(i->new ItemStack(i,count)).toList();
    }
    private Optional<RecipeHolder<CraftingRecipe>> recipe(List<ItemStack> cells) {
        return level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,CraftingInput.of(3,3,cells),level);
    }
    private void matches(List<ItemStack> cells,boolean automatic) {
        var holder=recipe(cells).orElseThrow(()->new AssertionError("recipe not loaded/matched"));
        String id=automatic?"automatic_greenhouse":"greenhouse";
        require(holder.id().identifier().equals(Identifier.fromNamespaceAndPath("greenhouse",id)),"wrong matched recipe: "+holder.id());
        var out=holder.value().assemble(CraftingInput.of(3,3,cells));
        require(out.is((automatic?BonsaiGreenhouse.HOPPING_ITEM:BonsaiGreenhouse.NORMAL_ITEM).get()) && out.getCount()==1,"wrong crafting output");
    }
    void recipes() {
        test.accept("normal_recipe_accepts_every_loaded_wooden_slab",()->{
            int slabs=0; for(var holder:BuiltInRegistries.ITEM.getTagOrEmpty(ItemTags.WOODEN_SLABS)) {
                matches(grid(false,holder.value(),1),false); slabs++;
            }
            require(slabs>=12,"wooden slab tag unexpectedly empty/incomplete: "+slabs);
            var mixed=new ArrayList<>(grid(false,Items.OAK_SLAB,1));
            mixed.set(7,new ItemStack(Items.POPLAR_SLAB)); mixed.set(8,new ItemStack(Items.CRIMSON_SLAB)); matches(mixed,false);
        });
        test.accept("automatic_recipe_matches_loaded_manager",()->matches(grid(true,Items.OAK_SLAB,1),true));
        for(boolean automatic:List.of(false,true)) {
            String prefix=automatic?"automatic":"normal";
            test.accept(prefix+"_recipe_rejects_missing_and_wrong_ingredients",()->{
                for(int slot=0;slot<9;slot++) {
                    var missing=new ArrayList<>(grid(automatic,Items.OAK_SLAB,1)); missing.set(slot,ItemStack.EMPTY);
                    require(recipe(missing).isEmpty(),"missing ingredient accepted at "+slot);
                    var wrong=new ArrayList<>(grid(automatic,Items.OAK_SLAB,1)); wrong.set(slot,new ItemStack(Items.STONE));
                    require(recipe(wrong).isEmpty(),"wrong ingredient accepted at "+slot);
                }
            });
            test.accept(prefix+"_crafting_menu_consumes_once_per_result",()->{
                var at=pos(); level.setBlock(at,Blocks.CRAFTING_TABLE.defaultBlockState(),3); var p=player(at);
                var menu=new CraftingMenu(1,p.getInventory(),ContainerLevelAccess.create(level,at));
                var cells=grid(automatic,Items.OAK_SLAB,2);
                for(int i=0;i<9;i++) menu.getSlot(1+i).set(cells.get(i));
                require(menu.getResultSlot().hasItem(),"crafting menu has no result");
                for(int craft=0;craft<2;craft++) {
                    require(!menu.quickMoveStack(p,0).isEmpty(),"result cannot be taken");
                    for(int i=1;i<=9;i++) require(menu.getSlot(i).getItem().getCount()==1-craft,"ingredient consumed incorrectly at "+i);
                    require(p.getInventory().countItem((automatic?BonsaiGreenhouse.HOPPING_ITEM:BonsaiGreenhouse.NORMAL_ITEM).get())==craft+1,"crafting output duplicated or lost");
                }
                require(menu.quickMoveStack(p,0).isEmpty() && !menu.getResultSlot().hasItem(),"crafting without ingredients");
            });
        }
    }
}
