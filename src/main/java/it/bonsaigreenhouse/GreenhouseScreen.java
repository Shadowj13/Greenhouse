package it.bonsaigreenhouse;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Compact, contrast-first greenhouse GUI with a stable localized title. */
public final class GreenhouseScreen extends AbstractContainerScreen<GreenhouseMenu> {
    private Button harvest;

    public GreenhouseScreen(GreenhouseMenu menu, Inventory player, Component title) {
        super(menu, player, title, 176, 216);
        this.titleLabelX = 10;
        this.titleLabelY = 10;
        this.inventoryLabelX = 9;
        this.inventoryLabelY = 127;
    }

    @Override protected void init() {
        super.init();
        if (!menu.isAutomatic()) {
            harvest = Button.builder(harvestLabel(), button -> {
                if (minecraft != null && minecraft.gameMode != null) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
                }
            }).bounds(leftPos + 42, topPos + 101, 92, 20).build();
            harvest.active = menu.hasHarvest();
            addRenderableWidget(harvest);
        }
    }

    @Override protected void containerTick() {
        super.containerTick();
        if (harvest != null) {
            harvest.active = menu.hasHarvest();
            harvest.setMessage(harvestLabel());
        }
    }

    /** "Harvest n/4": the normal greenhouse keeps up to four ready harvests. */
    private Component harvestLabel() {
        return Component.translatable("gui.greenhouse.harvest_count",
            menu.readyHarvests(), GreenhouseBlockEntity.NORMAL_HARVESTS);
    }

    private void slot(GuiGraphicsExtractor g, int x, int y) {
        g.fill(x-1,y-1,x+17,y+17,0xFF534B3E);
        g.fill(x,y,x+16,y+16,0xFFB5AA8F);
        g.fill(x+1,y+1,x+15,y+15,0xFF756E62);
    }

    private void clearLabel(GuiGraphicsExtractor g, Component label, int centerX, int y) {
        g.text(font,label,centerX-font.width(label)/2,y,0xFF1D1C18,false);
    }

    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta) {
        super.extractBackground(g,mouseX,mouseY,delta);
        int x = leftPos, y = topPos;

        // Framed oak + parchment structure. Font stays vanilla, contrast is increased.
        g.fill(x-4,y-4,x+180,y+220,0xFF4C3828);
        g.fill(x-2,y-2,x+178,y+218,0xFF866747);
        g.fill(x,y,x+176,y+216,0xFFE1D6B9);

        // Title comes from the current block keys, never from the block entity's
        // saved name, so stale alpha-world translation keys cannot appear.
        g.fill(x+5,y+5,x+171,y+21,0xFF4C6449);
        g.centeredText(font,Component.translatable(menu.isAutomatic()?
                       "block.greenhouse.automatic_greenhouse":"block.greenhouse.greenhouse"),
                       x+88,y+9,0xFFFFFFFF);

        // The former green title bar overlapped the Soil caption.
        // Make separate high-contrast header chips with NO font shadow, and
        // leave enough horizontal space for the complete "Bone Meal" label.
        g.fill(x+7,y+23,x+169,y+59,0xFFD4C6A7);
        g.fill(x+10,y+24,x+54,y+36,0xFFF3E8D1);
        g.fill(x+65,y+24,x+111,y+36,0xFFF3E8D1);
        g.fill(x+116,y+24,x+169,y+36,0xFFF3E8D1);
        clearLabel(g,Component.translatable("gui.greenhouse.soil"),x+32,y+26);
        clearLabel(g,Component.translatable("gui.greenhouse.plant"),x+88,y+26);
        clearLabel(g,Component.translatable("gui.greenhouse.fertilizer"),x+144,y+26);
        slot(g,x+24,y+39);
        slot(g,x+80,y+39);
        slot(g,x+136,y+39);

        // Progress block. Percentage sits on the same line as its heading.
        g.fill(x+7,y+61,x+169,y+96,0xFFCBBF9F);
        g.text(font,Component.translatable("gui.greenhouse.progress"),x+13,y+63,0xFF342C22,false);
        int percent = menu.mature()?100:
            (menu.duration()==0?0:Math.min(100,100*menu.elapsed()/Math.max(1,menu.duration())));
        String percentText = percent+"%";
        // Remaining time while actually growing (not ready, not paused).
        if(!menu.mature() && !menu.powered() && menu.duration()>0)
            percentText += " · " + Component.translatable("gui.greenhouse.seconds",
                GreenhouseBlockEntity.remainingSeconds(menu.elapsed(),menu.duration())).getString();
        g.text(font,Component.literal(percentText),x+163-font.width(percentText),y+63,0xFF342C22,false);
        g.fill(x+13,y+74,x+163,y+83,0xFF554D39);
        g.fill(x+15,y+76,x+161,y+81,0xFF243729);
        int fill = (146*percent)/100;
        if (fill>0) g.fill(x+15,y+76,x+15+fill,y+81,menu.mature()?0xFFD5B45B:0xFF75B36D);

        String state;
        // An automatic greenhouse stores a ready harvest on the same tick unless
        // its nine output slots cannot hold it, so "mature" means it is waiting.
        // A harvest still "mature" is waiting: automatic outputs full, or the
        // normal greenhouse holds all four harvests.
        if(menu.mature()) state=Component.translatable(menu.isAutomatic()?"gui.greenhouse.output_full":"gui.greenhouse.full").getString();
        else if(!menu.hasSoil()) state=Component.translatable("gui.greenhouse.missing_soil").getString();
        else if(menu.crop()==Crop.EMPTY) state=Component.translatable("gui.greenhouse.missing_plant").getString();
        else if(!menu.validCrop()) state=Component.translatable("gui.greenhouse.invalid_soil").getString();
        else if(menu.powered()) state=Component.translatable("gui.greenhouse.paused").getString();
        else state=Component.translatable("gui.greenhouse.growing").getString();
        g.text(font,Component.literal(state),x+13,y+86,0xFF332B22,false);
        g.text(font,Component.translatable("gui.greenhouse.bonus"),x+108,y+86,0xFF332B22,false);
        g.text(font,Component.literal(menu.bonusCycles()+"/4"),x+144,y+86,0xFF332B22,false);

        // Either manual harvest button or the automatic 9-slot storage row.
        if(menu.isAutomatic()) {
            g.fill(x+7,y+97,x+169,y+125,0xFFB8AB8B);
            g.text(font,Component.translatable("gui.greenhouse.storage"),x+9,y+97,0xFF302A22,false);
            for(int i=0;i<9;i++) slot(g,x+8+i*18,y+107);
        } else {
            g.fill(x+7,y+98,x+169,y+124,0xFFB8AB8B);
        }

        // Player inventory is visually independent, with clear separators.
        g.fill(x+5,y+126,x+171,y+214,0xFFC5B798);
        g.fill(x+7,y+137,x+169,y+138,0xFF9C8A68);
        for(int row=0;row<3;row++)
            for(int col=0;col<9;col++) slot(g,x+8+col*18,y+139+row*18);
        for(int col=0;col<9;col++) slot(g,x+8+col*18,y+195);
    }

    @Override protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY) {
        // Never draw this.title: older block entities may still hold a pre-rename
        // translation key. The validated short title is painted in the header.
        g.text(font,playerInventoryTitle,9,127,0xFF30281F,false);
    }
}
