package it.bonsaigreenhouse;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Greenhouse v0.2.2: compact, contrast-first GUI with a stable localized title. */
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
            harvest = Button.builder(Component.translatable("gui.greenhouse.harvest"), button -> {
                if (minecraft != null && minecraft.gameMode != null) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
                }
            }).bounds(leftPos + 42, topPos + 101, 92, 20).build();
            harvest.active = menu.mature();
            addRenderableWidget(harvest);
        }
    }

    @Override protected void containerTick() {
        super.containerTick();
        if (harvest != null) harvest.active = menu.mature();
    }

    private void slot(GuiGraphicsExtractor g, int x, int y) {
        g.fill(x-1,y-1,x+17,y+17,0xFF534B3E);
        g.fill(x,y,x+16,y+16,0xFFB5AA8F);
        g.fill(x+1,y+1,x+15,y+15,0xFF756E62);
    }

    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta) {
        super.extractBackground(g,mouseX,mouseY,delta);
        int x = leftPos, y = topPos;

        // Framed oak + parchment structure. Font stays vanilla, contrast is increased.
        g.fill(x-4,y-4,x+180,y+220,0xFF4C3828);
        g.fill(x-2,y-2,x+178,y+218,0xFF866747);
        g.fill(x,y,x+176,y+216,0xFFE1D6B9);

        // Title is deliberately short and independent of stale alpha-world translation keys.
        g.fill(x+5,y+5,x+171,y+27,0xFF4C6449);
        g.centeredText(font,Component.literal(menu.isAutomatic()?"Automatic Greenhouse":"Greenhouse"),
                       x+88,y+12,0xFFFFFFFF);

        // Inputs: three aligned positions, each with its own header.
        g.fill(x+7,y+23,x+169,y+59,0xFFCBBD9D);
        g.centeredText(font,Component.translatable("gui.greenhouse.soil"),x+52,y+26,0xFF29241D);
        g.centeredText(font,Component.translatable("gui.greenhouse.plant"),x+88,y+26,0xFF29241D);
        g.centeredText(font,Component.translatable("gui.greenhouse.fertilizer"),x+124,y+26,0xFF29241D);
        for (int i=0;i<3;i++) slot(g,x+44+i*36,y+39);

        // Progress block. Percentage sits on the same line as its heading.
        g.fill(x+7,y+61,x+169,y+96,0xFFCBBF9F);
        g.text(font,Component.translatable("gui.greenhouse.progress"),x+13,y+63,0xFF342C22,false);
        int percent = menu.mature()?100:
            (menu.duration()==0?0:Math.min(100,100*menu.elapsed()/Math.max(1,menu.duration())));
        String percentText = percent+"%";
        g.text(font,Component.literal(percentText),x+141,y+63,0xFF342C22,false);
        g.fill(x+13,y+74,x+163,y+83,0xFF554D39);
        g.fill(x+15,y+76,x+161,y+81,0xFF243729);
        int fill = (146*percent)/100;
        if (fill>0) g.fill(x+15,y+76,x+15+fill,y+81,menu.mature()?0xFFD5B45B:0xFF75B36D);

        String state;
        if(menu.crop()==Crop.EMPTY) state=Component.translatable("gui.greenhouse.missing_plant").getString();
        else if(!menu.validCrop()) state=Component.translatable("gui.greenhouse.invalid_soil").getString();
        else if(menu.mature()) state=Component.translatable("gui.greenhouse.ready").getString();
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
