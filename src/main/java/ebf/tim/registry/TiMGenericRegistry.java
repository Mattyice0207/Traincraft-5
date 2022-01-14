package ebf.tim.registry;


import buildcraft.api.fuels.BuildcraftFuelRegistry;
import ebf.tim.TrainsInMotion;
import ebf.tim.blocks.BlockDynamic;
import ebf.tim.blocks.BlockTrainFluid;
import ebf.tim.blocks.OreGen;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.items.CustomItemModel;
import ebf.tim.items.ItemBlockTiM;
import ebf.tim.items.ItemCraftGuide;
import ebf.tim.items.ItemTransport;
import ebf.tim.render.RenderWagon;
import ebf.tim.utility.*;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ebf.tim.utility.RecipeManager.getRecipeWithTier;
import static net.minecraftforge.registries.GameData.BLOCKS;
import static net.minecraftforge.registries.GameData.ITEMS;

/**
 * <h1>Train registry</h1>
 * this class lists the data necessary to register trains and rollingstock.
 * this is not intended to be a way to get specific trains from the mod. (use the unlocalized entity name, or a class instanceof check)
 * @author Eternal Blue Flame
 */
public class TiMGenericRegistry {

    public GenericRailTransport transport;
    public Item[] recipe;

    //todo:purge redundancy checks on postinit
    private static List<String> usedNames = new ArrayList<>();
    private static List<String> redundantTiles = new ArrayList<>();
    private static List<String> redundantBlocks = new ArrayList<>();
    public static int registryPosition = 18;

    public TiMGenericRegistry(GenericRailTransport transport, Item[] recipe) {
        this.transport = transport;
        this.recipe = recipe;
    }

    /**
     * @param block             the block in question
     * @param tab               the creative tab to put the block's item into, leave null for no creative tab entry.
     * @param MODID             modid of the host add-on, used for translations, texture names, and generic identification.
     * @param unlocalizedName   unlocalized name, used for translations, texture names, and generic identification.
     * @param oreDictionaryName ore directory name, used for other mods to identify type, mainly used for ingots, ores, and wood.
     * @param render            a ModelBase instance for rendering the tile entity.
     *                          Can instead be a TESR instance for more rendering control.
     *                          Null will fallback to a normal textured block render.
     * @return
     */
    public static Block registerBlock(Block block, CreativeTabs tab, String MODID, String unlocalizedName, @Nullable String oreDictionaryName, @Nullable Object render) {
        if (render instanceof ModelBase) {
            return registerBlock(block, tab, MODID, unlocalizedName, oreDictionaryName, TrainsInMotion.proxy.getTESR(), (ModelBase) render);
        } else {
            return registerBlock(block, tab, MODID, unlocalizedName, oreDictionaryName, render, null);
        }
    }

    public static Block registerBlock(Block block, CreativeTabs tab, String unlocalizedName, @Nullable String oreDictionaryName, @Nullable Object render) {
        return registerBlock(block, tab, null, unlocalizedName, oreDictionaryName, render);
    }

    public static Block registerBlock(Block block, CreativeTabs tab, String MODID, String unlocalizedName, @Nullable String oreDictionaryName, @Nullable Object TESR, @Nullable ModelBase model) {
        if (usedNames.contains(unlocalizedName)) {
            DebugUtil.println("ERROR: ", "attempted to register Block with a used unlocalizedName", unlocalizedName);
            DebugUtil.throwStackTrace();
        }
        if (tab != null) {
            block.setCreativeTab(tab);
        }
        if (unlocalizedName.length() > 0) {
            block.setTranslationKey(unlocalizedName);
            ((ForgeRegistry<Block>)RegistryManager.ACTIVE.getRegistry(BLOCKS))
                    .register(block.setRegistryName(MODID,unlocalizedName));
            if(model!=null) {
                RegisterItem(new ItemBlockTiM(block), MODID, unlocalizedName + ".item", oreDictionaryName + ".item", tab, null, ebf.tim.items.CustomItemModel.instance);
            } else {
                RegisterItem(new ItemBlockTiM(block), MODID, unlocalizedName + ".item", oreDictionaryName + ".item", tab, null, null);
            }
            usedNames.add(unlocalizedName);
        } else {
            DebugUtil.println("ERROR: ", "attempted to register Block with no unlocalizedName");
            DebugUtil.throwStackTrace();
        }

        if (TrainsInMotion.proxy.isClient() && MODID != null) {
            //todo: block textures aren't this configurable anymore
            //block.setBlockTextureName(MODID + ":" + unlocalizedName);
        }
        if (oreDictionaryName != null) {
            OreDictionary.registerOre(oreDictionaryName, block);
        }
        if (DebugUtil.dev() && TrainsInMotion.proxy.isClient() && block.getTranslationKey().equals(CommonUtil.translate(block.getTranslationKey()))) {
            DebugUtil.println("Block missing lang entry: " + block.getTranslationKey() + " : " + unlocalizedName);
        }
        if (block instanceof BlockDynamic) {
            if (model != null) {
                ((BlockDynamic) block).setModel(model);
            } else if (TESR != null) {
                ((BlockDynamic) block).setTESR(TESR);
            }
        }
        if (block instanceof ITileEntityProvider) {
            Class<? extends TileEntity> tile = ((ITileEntityProvider) block).createNewTileEntity(null, 0).getClass();
            if (!redundantTiles.contains(tile.getName())) {
                GameRegistry.registerTileEntity(tile, new ResourceLocation(MODID,unlocalizedName + "tile"));
                redundantTiles.add(tile.getName());
                if (TrainsInMotion.proxy.isClient() && TESR != null) {

                    CustomItemModel.renderItems.add(new ResourceLocation(MODID,unlocalizedName));
                    net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(tile, (TileEntitySpecialRenderer) TESR);
                    //MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(block), CustomItemModel.instance);
                    CustomItemModel.renderItems.add(new ResourceLocation(MODID, unlocalizedName + "tile"));
                    CustomItemModel.registerBlockTextures(Item.getItemFromBlock(block), ((ITileEntityProvider) block).createNewTileEntity(null, 0));
                } else if (TrainsInMotion.proxy.isClient()) {
                    net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(tile, (TileEntitySpecialRenderer) TrainsInMotion.proxy.getTESR());
                    //MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(block), CustomItemModel.instance);
                    CustomItemModel.renderItems.add(new ResourceLocation(MODID, unlocalizedName + "tile"));
                    CustomItemModel.registerBlockTextures(Item.getItemFromBlock(block), ((ITileEntityProvider) block).createNewTileEntity(null, 0));
                }
            } else {
                DebugUtil.println("redundant tile name found", unlocalizedName + "tile");
                DebugUtil.printStackTrace();
            }
        }

        return block;
    }

    public static Item RegisterItem(Item itm, String MODID, String unlocalizedName, CreativeTabs tab) {
        return RegisterItem(itm, MODID, unlocalizedName, null, tab, null, null);
    }

    public static Item RegisterItem(Item itm, String MODID, String unlocalizedName, @Nullable String oreDictionaryName, @Nullable CreativeTabs tab, @Nullable Item container, @Nullable Object itemRender) {
        if (usedNames.contains(unlocalizedName)) {
            DebugUtil.println("ERROR: ", "attempted to register Item with a used unlocalizedName", unlocalizedName);
            DebugUtil.throwStackTrace();
        }
        if (tab != null) {
            itm.setCreativeTab(tab);
        }
        if (container != null) {
            itm.setContainerItem(container);
        }
        if (!unlocalizedName.equals("")) {
            itm.setTranslationKey(unlocalizedName);
            usedNames.add(unlocalizedName);
        } else {
            DebugUtil.println("ERROR: ", "attempted to register Item with no unlocalizedName");
            DebugUtil.throwStackTrace();
        }
        if (TrainsInMotion.proxy.isClient()) {
            //todo: item textures aren't this configurable anymore
            //itm.setTextureName(MODID + ":" + unlocalizedName);
        }

        ((ForgeRegistry<Item>)RegistryManager.ACTIVE.getRegistry(ITEMS))
                .register(itm.setRegistryName(MODID,unlocalizedName));
        if (oreDictionaryName != null) {
            OreDictionary.registerOre(oreDictionaryName, itm);
        }
        if (DebugUtil.dev() && TrainsInMotion.proxy != null && TrainsInMotion.proxy.isClient() && itm.getTranslationKey().equals(CommonUtil.translate(itm.getTranslationKey()))) {
            DebugUtil.println("Item missing lang entry: " + itm.getTranslationKey());
        }
        if (TrainsInMotion.proxy.isClient() && itemRender != null) {
            //MinecraftForgeClient.registerItemRenderer(itm, (IItemRenderer) itemRender);
            CustomItemModel.renderItems.add(new ResourceLocation(MODID,unlocalizedName));
            net.minecraftforge.client.model.ModelLoader.setCustomModelResourceLocation(itm,0,new ModelResourceLocation(new ResourceLocation(MODID,unlocalizedName),""));

        } else if (TrainsInMotion.proxy.isClient() && itm instanceof ItemTransport) {
            CustomItemModel.renderItems.add(new ResourceLocation(MODID,unlocalizedName));
            net.minecraftforge.client.model.ModelLoader.setCustomModelResourceLocation(itm,0,new ModelResourceLocation(new ResourceLocation(MODID,unlocalizedName),""));

            //todo:this somehow? actually it might just be forced in 1.8+
            if (ClientProxy.preRenderModels) {
                //ebf.tim.items.CustomItemModel.instance.renderItem(IItemRenderer.ItemRenderType.INVENTORY, new ItemStack(itm));
            }
        }
        return itm;
    }


    public static void RegisterFluid(Fluid fluid, @Nullable Item bucket, String MODID, String unlocalizedName, boolean isGaseous, int density, MapColor color, @Nullable CreativeTabs tab) {
        if (usedNames.contains(unlocalizedName)) {
            DebugUtil.println("ERROR: ", "attempted to register Fluid with a used unlocalizedName", unlocalizedName);
            DebugUtil.throwStackTrace();
        }
        if (!unlocalizedName.equals("")) {
            fluid.setUnlocalizedName(unlocalizedName);
            usedNames.add(unlocalizedName);
        } else {
            DebugUtil.println("ERROR: ", "attempted to register Fluid with no unlocalizedName");
            DebugUtil.throwStackTrace();
        }
        fluid.setGaseous(isGaseous).setDensity(density);
        FluidRegistry.registerFluid(fluid);

        Block block = new BlockTrainFluid(fluid, new MaterialLiquid(color))
                .getBlockState().getBlock().setTranslationKey("block." + unlocalizedName.replace(".item", ""));
                //todo: block textures aren't this configurable anymore
                //.setBlockTextureName(MODID + ":block_" + unlocalizedName);
        ((BlockTrainFluid) block).setModID(MODID);

        ((ForgeRegistry<Block>)RegistryManager.ACTIVE.getRegistry(BLOCKS))
                .register(block.setRegistryName(MODID,"block." + unlocalizedName));
        if (TrainsInMotion.proxy.isClient()) {
            //todo: block textures aren't this configurable anymore
            //block.setBlockTextureName(MODID + ":" + unlocalizedName);
        }
        fluid.setBlock(block);

        if (bucket == null) {
            bucket = new ItemBucket(block).setCreativeTab(tab).setContainerItem(Items.BUCKET);
            if (TrainsInMotion.proxy.isClient()) {
                bucket.setTranslationKey(MODID + ":bucket_" + unlocalizedName);
            }
        }
        bucket.setTranslationKey(unlocalizedName + ".bucket");
        ((ForgeRegistry<Item>)RegistryManager.ACTIVE.getRegistry(ITEMS))
                .register(bucket.setRegistryName(MODID,"fluid." +unlocalizedName + ".bucket"));
        //todo: 1.12 uses instanceof stuff, so this _shouldn't_ be needed?
        //FluidContainerRegistry.registerFluidContainer(fluid, new ItemStack(bucket), new ItemStack(Items.BUCKET));


        if (DebugUtil.dev() && TrainsInMotion.proxy.isClient()) {
            if (fluid.getUnlocalizedName().equals(CommonUtil.translate(fluid.getUnlocalizedName()))) {
                DebugUtil.println("Fluid missing lang entry: " + fluid.getUnlocalizedName());
            }
            if (bucket.getTranslationKey().equals(block.getLocalizedName())) {
                DebugUtil.println("Item missing lang entry: " + bucket.getTranslationKey());
            }
            if (block.getTranslationKey().equals(block.getLocalizedName())) {
                DebugUtil.println("Block missing lang entry: " + block.getTranslationKey());
            }

        }
    }


    public static void registerTransports(String MODID, List<GenericRailTransport> entities, Object entityRender) {
        registerTransports(MODID,entities.toArray(new GenericRailTransport[]{}), entityRender);
    }

    public static void registerTransports(String MODID, GenericRailTransport[] entities, Object entityRender) {
        if (registryPosition == -1) {
            DebugUtil.println("ERROR", "ADDING TRANSPORT REGISTRY ITEMS OUTSIDE MOD INIT", "PLEASE REGISTER YOUR ENTITIES IN THE FOLLOWING EVENT:",
                    "@Mod.EventHandler public void init(FMLInitializationEvent event)");
        }
        for (GenericRailTransport registry : entities) {
            if (DebugUtil.dev() && usedNames.contains(registry.transportName())) {
                DebugUtil.println(registry.getClass().getName(), "is trying to register under the name", usedNames.contains(registry.transportName()), "which is already used");
                DebugUtil.throwStackTrace();
            }
            net.minecraftforge.fml.common.registry.EntityRegistry.registerModEntity(
                    new ResourceLocation(MODID,registry.transportName().replace(" ", "") + ".entity"),
                    registry.getClass(),
                    registry.transportName().replace(" ", "") + ".entity",
                    registryPosition, TrainsInMotion.instance, 1600, 3, true);

            RegisterItem(registry.getCartItem().getItem(),MODID,registry.transportName()+".item",
                    null,registry.getItem().getCreativeTab(),null,null);
            if (registry.getRecipe() != null) {
                    tempReipes.put(MODID, new recipePreReg(registry.getRecipe(), registry.getCartItem(), registry.getTier()));
            }
            registry.registerSkins();
            if (registry.getRecipe() != null) {
               // RecipeManager.registerRecipe(registry.getRecipe(), registry.getCartItem(), registry.getTier());
            }
            ItemCraftGuide.itemEntries.add(registry.getClass());
            if (TrainsInMotion.proxy.isClient()) {
                if (entityRender == null) {
                    net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(registry.getClass(), RenderWagon.INSTANCE);
                    if (ClientProxy.preRenderModels) {
                        //((net.minecraftforge.fml.client.registry.IRenderFactory<GenericRailTransport>) TrainsInMotion.proxy.getEntityRender()).ren(registry, 0, 0, 0, 0, 0);
                    }
                } else {
                    net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(registry.getClass(), (net.minecraft.client.renderer.entity.Render<GenericRailTransport>) entityRender);
                    if (ClientProxy.preRenderModels) {
                        //((net.minecraftforge.fml.client.registry.IRenderFactory<GenericRailTransport>) entityRender).doRender(registry, 0, 0, 0, 0, 0);
                    }
                }
                if (ClientProxy.preRenderModels && ClientProxy.hdTransportItems) {
                    ebf.tim.items.CustomItemModel.instance.renderItemModel(registry.getCartItem(), ItemCameraTransforms.TransformType.GUI,null);
                }
            }
            usedNames.add(registry.transportName());
            registryPosition++;
        }
    }

    /**
     * @param priority the priority to generate, higher numbers tend to generate after other mods.
     */
    public static void registerOreGen(int priority, OreGen veinConfig) {
        GameRegistry.registerWorldGenerator(veinConfig, priority);
    }



    public static void endRegistration() {

        for(String key : tempReipes.keySet()){
            if (!CommonProxy.recipesInMods.containsKey(key)) {
                CommonProxy.recipesInMods.put(key, new ArrayList<Recipe>());
            }
            CommonProxy.recipesInMods.get(key).add(getRecipeWithTier(tempReipes.get(key).inputs, tempReipes.get(key).result, tempReipes.get(key).tier));
            RecipeManager.registerRecipe(tempReipes.get(key).inputs, tempReipes.get(key).result, tempReipes.get(key).tier);
        }

        usedNames = null;
        registryPosition = -1;
        redundantTiles = null;
    }

    private static Map<String, recipePreReg> tempReipes = new HashMap<>();
    private static class recipePreReg {
        int tier;ItemStack result;ItemStack[] inputs;

        public recipePreReg(ItemStack[] i, ItemStack r, int t) {
            this.inputs=i;
            this.result = r;
            this.tier=t;
        }
    }

    //todo:add support for buildcraft/railcraft burnable fluids

    @Optional.Method(modid = "BuildCraft|Energy")
    static void registerBCFluid(Fluid f, int powerPerCycle, int totalBurningTime) {
        BuildcraftFuelRegistry.fuel.addFuel(f, powerPerCycle, totalBurningTime);
    }

    /*@Optional.Method(modid = "Railcraft")
    static void registerRCFluid(Fluid f, int totalBurningTime) {
        FuelManager.addBoilerFuel(f, totalBurningTime);
    }*/

}
