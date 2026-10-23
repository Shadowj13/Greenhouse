package it.bonsaigreenhouse.qa;

import com.mojang.authlib.GameProfile;
import it.bonsaigreenhouse.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
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
        Crop.SUNFLOWER,Crop.LILAC,Crop.PEONY,Crop.ROSE_BUSH);
    private record Spec(Crop crop, int duration, int min, int max, Set<String> bonus) {}
    private Spec spec(Crop crop) {
        // Independent acceptance limits: do not infer expected yields/timings
        // from the production fields whose behavior is being verified.
        int duration = switch(crop) { case WHEAT,CARROTS,POTATOES,BEETROOT,CACTUS,SUGAR_CANE,KELP,
                DANDELION,POPPY,BLUE_ORCHID,ALLIUM,AZURE_BLUET,RED_TULIP,ORANGE_TULIP,
                WHITE_TULIP,PINK_TULIP,OXEYE_DAISY,CORNFLOWER,LILY_OF_THE_VALLEY,
                SUNFLOWER,LILAC,PEONY,ROSE_BUSH -> 600; default -> 900; };
        int min = switch(crop) {
            case WHEAT,BEETROOT,NETHER_WART,MELON -> 2;
            default -> crop.isTree()?2:1;
        };
        int max = switch(crop) {
            case MELON -> 4;
            case CARROTS,POTATOES,COCOA,BAMBOO,SWEET_BERRIES,GLOW_BERRIES,RED_MUSHROOM,BROWN_MUSHROOM -> min+1;
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
        for(Crop crop:Crop.values()) if(crop!=Crop.EMPTY) {
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
                require(!crop.accepts(new ItemStack(Items.STONE)) && !crop.accepts(ItemStack.EMPTY),"invalid substrate accepted");
                var be=fresh(false); inputs(be,crop,1);
                be.setItem(0,new ItemStack(crop.soilId.equals("sand")?Items.DIRT:Items.SAND)); tick(be,1000);
                require(be.dataAccess().get(0)==0 && be.getItem(2).getCount()==1,"invalid combination grew or spent fertilizer");
            });
            test.accept(prefix+"_manual_timing_and_four_boosted_cycles",()->{
                var be=fresh(false); inputs(be,crop,0); tick(be,s.duration-1);
                require(be.getBlockState().getValue(GreenhouseBlock.CROP)==crop,"selected color/crop changed");
                require(be.dataAccess().get(2)==0 && be.dataAccess().get(0)==s.duration-1,"early/late maturity");
                tick(be,1); require(be.dataAccess().get(2)==1,"maturity missing at expected tick");
                var p=player(be.getBlockPos()); be.collectBy(p); var collected=contents(p.getInventory()); harvest(s,collected);
                be.collectBy(p); require(contents(p.getInventory()).equals(collected),"duplicate harvest");
                be.setItem(2,new ItemStack(Items.BONE_MEAL));
                for(int cycle=0;cycle<4;cycle++) {
                    tick(be,s.duration/2-1); require(be.dataAccess().get(2)==0,"boosted cycle matured early");
                    tick(be,1); require(be.dataAccess().get(2)==1,"boosted cycle did not mature");
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
        for(Crop crop:ADDITIONS) test.accept("priority123_"+crop.getSerializedName()+"_reload_and_menu",()->{
            var be=fresh(false); inputs(be,crop,1);
            var p=player(be.getBlockPos()); var menu=new GreenhouseMenu(1,p.getInventory(),be);
            require(menu.slots.get(0).mayPlace(new ItemStack(Items.DIRT)) &&
                menu.slots.get(1).mayPlace(new ItemStack(item(crop.plantId))),"menu rejects new crop");
            require(!crop.isTree(),"new flower/berry classified as a tree");
            tick(be,137); var saved=state(be); load(be,saved); tick(be,1);
            require(be.dataAccess().get(0)==138 && be.dataAccess().get(1)==spec(crop).duration/2 &&
                be.dataAccess().get(3)==3,"reload loses progress or boost");
            require(be.getBlockState().getValue(GreenhouseBlock.CROP)==crop,"reload changes plant identity");
            tick(be,spec(crop).duration/2-138); be.collectBy(p); harvest(spec(crop),contents(p.getInventory()));
            require(be.getItem(0).is(Items.DIRT) && be.getItem(1).is(item(crop.plantId)) &&
                be.getItem(1).getCount()==1,"harvest consumes original plant or soil");
        });
        test.accept("priority123_scope_and_excluded_plants",()->{
            require(Crop.values().length==49 && ADDITIONS.size()==17,"unexpected crop scope");
            for(String path:List.of("golden_dandelion","wither_rose","torchflower","torchflower_seeds",
                    "pitcher_plant","pitcher_pod","red_shrub","shelf_mushroom","open_eyeblossom",
                    "closed_eyeblossom","dead_bush","pink_petals","wildflowers","vine"))
                require(Crop.of(new ItemStack(item(path)))==Crop.EMPTY,"unapproved crop accepted: "+path);
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
    }
    private BlockPos persisted(Crop crop) {
        return new BlockPos(53248+ADDITIONS.indexOf(crop)*3,100,51400);
    }
    void checkpointAdditions() {
        for(Crop crop:ADDITIONS) test.accept("persist_priority123_"+crop.getSerializedName(),()->{
            BlockPos at=persisted(crop);
            level.setBlock(at,Blocks.AIR.defaultBlockState(),3);
            level.setBlock(at.below(),Blocks.STONE.defaultBlockState(),3);
            level.setBlock(at,BonsaiGreenhouse.NORMAL.get().defaultBlockState(),3);
            var be=(GreenhouseBlockEntity)level.getBlockEntity(at);
            inputs(be,crop,1); tick(be,137);
        });
    }
    void reloadAdditions() {
        for(Crop crop:ADDITIONS) test.accept("disk_reload_priority123_"+crop.getSerializedName(),()->{
            var be=(GreenhouseBlockEntity)level.getBlockEntity(persisted(crop));
            require(be!=null && be.dataAccess().get(0)==137 &&
                be.dataAccess().get(1)==spec(crop).duration/2 && be.dataAccess().get(3)==3,
                "new crop progress/boost changed across process restart");
            require(be.getBlockState().getValue(GreenhouseBlock.CROP)==crop,"disk reload loses plant identity");
            tick(be,spec(crop).duration/2-137);
            require(be.dataAccess().get(2)==1,"disk-restored crop misses original maturity deadline");
            var p=player(be.getBlockPos()); be.collectBy(p); harvest(spec(crop),contents(p.getInventory()));
            require(be.getItem(0).is(Items.DIRT) && be.getItem(1).is(item(crop.plantId)),
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
