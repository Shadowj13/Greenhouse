package it.bonsaigreenhouse;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class GreenhouseScreen extends AbstractContainerScreen<GreenhouseMenu> {
    private Button harvest;
    public GreenhouseScreen(GreenhouseMenu menu, Inventory player, Component title) {
        super(menu, player, title, 176, 220);
        this.titleLabelX = 10;
        this.titleLabelY = 7;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 123;
    }
    @Override protected void init() {
        super.init();
        if (!menu.isAutomatic()) {
            harvest = Button.builder(Component.translatable("gui.greenhouse.harvest"), button -> {
                if (minecraft != null && minecraft.gameMode != null)
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0);
            }).bounds(leftPos+51,topPos+98,74,20).build();
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
    @Override public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractBackground(g,mouseX,mouseY,delta);
        int x=leftPos,y=topPos;
        g.fill(x-4,y-4,x+180,y+224,0xFF3D3025);
        g.fill(x,y,x+176,y+220,0xFFE0D2AB);
        g.fill(x+4,y+4,x+172,y+22,0xFF68784F);
        g.fill(x+4,y+24,x+172,y+92,0xFFC5B88E);
        g.fill(x+6,y+94,x+170,y+121,0xFFB7AB88);
        g.text(font,Component.translatable("gui.greenhouse.soil"),x+38,y+28,0xFF423426,false);
        g.text(font,Component.translatable("gui.greenhouse.plant"),x+77,y+28,0xFF423426,false);
        g.text(font,Component.translatable("gui.greenhouse.fertilizer"),x+112,y+28,0xFF423426,false);
        for (int i=0;i<3;i++) slot(g,x+44+i*36,y+41);
        g.fill(x+11,y+72,x+165,y+85,0xFF3A4934);
        g.fill(x+13,y+74,x+163,y+83,0xFF172C1F);
        int amount=menu.mature()?150:(menu.duration()==0?0:Math.min(150,150*menu.elapsed()/Math.max(1,menu.duration())));
        g.fill(x+13,y+74,x+13+amount,y+83,menu.mature()?0xFFD3B350:0xFF5EAE65);
        String status;
        if (menu.crop()==Crop.EMPTY) status=Component.translatable("gui.greenhouse.missing_plant").getString();
        else if (!menu.validCrop()) status=Component.translatable("gui.greenhouse.invalid_soil").getString();
        else if (menu.mature()) status=Component.translatable("gui.greenhouse.ready").getString();
        else status=(menu.duration()==0?0:100*menu.elapsed()/Math.max(1,menu.duration()))+"%";
        g.centeredText(font,status,x+88,y+87,0xFF362B20);
        g.text(font,Component.translatable("gui.greenhouse.bonus").getString()+": "+menu.bonusCycles()+"/4",x+9,y+109,0xFF433A2B,false);
        if (menu.isAutomatic()) {
            for (int i=0;i<9;i++) slot(g,x+8+i*18,y+103);
        }
        g.fill(x+4,y+122,x+172,y+217,0xFFCCC2A7);
        for (int i=0;i<3;i++) for (int j=0;j<9;j++) slot(g,x+8+j*18,y+136+i*18);
        for (int i=0;i<9;i++) slot(g,x+8+i*18,y+194);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY) {
        g.text(font,this.title,10,7,0xFFFFFFFF,false);
        g.text(font,playerInventoryTitle,8,123,0xFF3C3629,false);
        if (menu.isAutomatic())
            g.text(font,Component.translatable("gui.greenhouse.storage"),8,94,0xFF312C25,false);
    }
}
