package it.bonsaigreenhouse;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class GreenhouseScreen extends AbstractContainerScreen<GreenhouseMenu> {
    private Button harvest;
    public GreenhouseScreen(GreenhouseMenu menu, Inventory player, Component title) {
        super(menu, player, title, 176, 264);
        this.titleLabelX = 10;
        this.titleLabelY = 10;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 163;
    }
    @Override protected void init() {
        super.init();
        if (!menu.isAutomatic()) {
            harvest = Button.builder(Component.translatable("gui.greenhouse.harvest"), button -> {
                if (minecraft != null && minecraft.gameMode != null)
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0);
            }).bounds(leftPos+51,topPos+126,74,20).build();
            harvest.active = menu.mature();
            addRenderableWidget(harvest);
        }
    }
    @Override protected void containerTick() {
        super.containerTick();
        if (harvest!=null) harvest.active=menu.mature();
    }
    private void slot(GuiGraphicsExtractor g,int x,int y) {
        g.fill(x-1,y-1,x+17,y+17,0xFF524634);
        g.fill(x,y,x+16,y+16,0xFFB8AF94);
        g.fill(x+1,y+1,x+16,y+16,0xFF726B60);
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta) {
        super.extractBackground(g,mouseX,mouseY,delta);
        int x=leftPos,y=topPos;
        // Timber surround, warm parchment panels, and restrained sage accents.
        g.fill(x-4,y-4,x+180,y+268,0xFF49372B);
        g.fill(x,y,x+176,y+264,0xFFE2D5B8);
        g.fill(x+4,y+4,x+172,y+25,0xFF657755);
        g.fill(x+5,y+29,x+171,y+117,0xFFCCBE9B);

        g.text(font,Component.translatable("gui.greenhouse.soil"),x+39,y+31,0xFF382E26,false);
        g.text(font,Component.translatable("gui.greenhouse.plant"),x+76,y+31,0xFF382E26,false);
        g.text(font,Component.translatable("gui.greenhouse.fertilizer"),x+111,y+31,0xFF382E26,false);
        for(int i=0;i<3;i++) slot(g,x+44+i*36,y+44);

        g.fill(x+12,y+76,x+164,y+89,0xFF544936);
        g.fill(x+14,y+78,x+162,y+87,0xFF2B3C2F);
        int progress=menu.mature()?148:(menu.duration()==0?0:Math.min(148,148*menu.elapsed()/Math.max(1,menu.duration())));
        g.fill(x+14,y+78,x+14+progress,y+87,menu.mature()?0xFFD5B65F:0xFF70AE68);
        String status;
        if(menu.crop()==Crop.EMPTY) status=Component.translatable("gui.greenhouse.missing_plant").getString();
        else if(!menu.validCrop()) status=Component.translatable("gui.greenhouse.invalid_soil").getString();
        else if(menu.mature()) status=Component.translatable("gui.greenhouse.ready").getString();
        else status=(menu.duration()==0?0:100*menu.elapsed()/Math.max(1,menu.duration()))+"%";
        g.centeredText(font,status,x+88,y+94,0xFF403427);
        g.text(font,Component.translatable("gui.greenhouse.bonus").getString()+": "+menu.bonusCycles()+"/4",x+12,y+109,0xFF514833,false);

        if(menu.isAutomatic()) {
            g.text(font,Component.translatable("gui.greenhouse.storage"),x+8,y+121,0xFF403427,false);
            for(int i=0;i<9;i++) slot(g,x+8+i*18,y+135);
        } else {
            g.fill(x+7,y+121,x+169,y+151,0xFFB4A47E);
        }
        g.fill(x+4,y+158,x+172,y+260,0xFFC9BDA1);
        for(int i=0;i<3;i++) for(int j=0;j<9;j++) slot(g,x+8+j*18,y+177+i*18);
        for(int i=0;i<9;i++) slot(g,x+8+i*18,y+235);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY) {
        g.text(font,this.title,10,11,0xFFFFFFFF,false);
        g.text(font,playerInventoryTitle,8,163,0xFF403427,false);
    }
}
