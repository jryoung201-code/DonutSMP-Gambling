package com.jryoung.moneygraph.compat;

import com.jryoung.moneygraph.screen.MoneyGraphScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screens.Screen;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return MoneyGraphScreen::new;
    }
}