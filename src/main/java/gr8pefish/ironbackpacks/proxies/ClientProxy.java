package gr8pefish.ironbackpacks.proxies;

import gr8pefish.ironbackpacks.api.Constants;
import gr8pefish.ironbackpacks.api.items.backpacks.ItemIUpgradableITieredBackpack;
import gr8pefish.ironbackpacks.api.items.backpacks.interfaces.IBackpack;
import gr8pefish.ironbackpacks.api.items.backpacks.interfaces.IColorable;
import gr8pefish.ironbackpacks.api.register.ItemIBackpackRegistry;
import gr8pefish.ironbackpacks.client.ClientEventHandler;
import gr8pefish.ironbackpacks.client.KeyHandler;
import gr8pefish.ironbackpacks.client.renderer.LayerBackpack;
import gr8pefish.ironbackpacks.config.ConfigAdaptor;
import gr8pefish.ironbackpacks.registry.ItemRegistry;
import gr8pefish.ironbackpacks.registry.ProxyRegistry;
import gr8pefish.ironbackpacks.util.IronBackpacksConstants;
import gr8pefish.ironbackpacks.util.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

/**
 * All the specifics that need to be called on the client side
 */
public class ClientProxy extends CommonProxy {

    public void preInit(){
        KeyHandler.init();
        initClientEventHandlers();

        ProxyRegistry.preInitClient();
    }

    public void init(){

        //initialize extra layer for rendering the backpack on the player
        addBackpackModelLayer();
        addBackpackColors();

        ProxyRegistry.initClient();
    }

    public void postInit(){
        ProxyRegistry.postInitClient();
    }

    //============================================================Helper Methods===================================================================

    //helper init methods

    private void initClientEventHandlers(){
        FMLCommonHandler.instance().bus().register(new ClientEventHandler());
    }

    //random helper methods

    public String getModVersion(){
        return Constants.VERSION;
    }

    public String getRemoteUpdatedVersion(){
        return ConfigAdaptor.getLatestFilenameFromCurse(IronBackpacksConstants.Miscellaneous.URL_UPDATED_VERSION);
    }

    public World getClientWorld(){
        return Minecraft.getMinecraft().theWorld;
    }

    public EntityPlayer getClientPlayer(){
        return Minecraft.getMinecraft().thePlayer;
    }

    /**
     * Add backpack layer via reflection.
     * Credit to TehNut for the base code.
     */
    private void addBackpackModelLayer() {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        try {
            RenderPlayer renderPlayer = ObfuscationReflectionHelper.getPrivateValue(RenderManager.class, renderManager, "playerRenderer", "field_178637_m");
            renderPlayer.addLayer(new LayerBackpack(renderPlayer));
            Logger.info("Added Layer Backpack");
        } catch (Exception e) {
            Logger.error("Failed to add Layer Backpack!");
            Logger.error(e.getLocalizedMessage());
        }
    }

    /**
     * Add the 16 colors to backpacks.
     */
    private void addBackpackColors() {

        for (int i = 0; i < ItemIBackpackRegistry.getSize(); i++) {

            Minecraft.getMinecraft().getItemColors().registerItemColorHandler((stack, tintIndex) -> {
                if (stack != null && stack.getItem() instanceof IColorable) {
                    IColorable colorable = (IColorable) stack.getItem();
                    if (colorable.isColored(stack))
                        return colorable.getColor(stack).getRGB(); //ToDo: crashes
                }
                return -1;
            }, (ItemIUpgradableITieredBackpack)ItemIBackpackRegistry.getBackpackAtIndex(i)); //ToDo: Dangerous typecasting

        }
    }

}
