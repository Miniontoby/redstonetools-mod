package com.domain.redstonetools.macros;

import com.domain.redstonetools.macros.actions.Action;
import com.domain.redstonetools.macros.gui.InvisibleKeyBinding;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Key;

import java.util.ArrayList;
import java.util.List;

public class Macro {

    public static Macro buildEmpty() {
        return new Macro("",true,InputUtil.UNKNOWN_KEY,new ArrayList<>());
    }

    private KeyBinding keyBinding;
    public String name;
    private Key key;
    public boolean enabled;
    public List<Action> actions;

    private final Macro original;

    public Macro(String name, boolean enabled, Key key, List<Action> actions) {
        this(name,enabled,key,actions,null);
        keyBinding = new InvisibleKeyBinding("macro." + System.nanoTime(),-1,"macros");
        addKeyBindingToOptions();
    }

    public Macro(String name, boolean enabled, Key key, List<Action> actions, Macro original) {
        this.name = name;
        this.enabled = enabled;
        this.key = key;
        this.actions = actions;
        this.original = original;
    }

    public void addKeyBindingToOptions() {
        if (keyBinding == null) return;

        keyBinding = KeyBindingHelper.registerKeyBinding(keyBinding);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (keyBinding == null || key == InputUtil.UNKNOWN_KEY) return;

            if (keyBinding.wasPressed()) {
                run();
            }
        });
    }

    public void run() {
        if (!enabled) {
            return;
        }

        for (Action action : actions) {
            action.run();
        }
    }

    public void applyChangesToOriginal() {
        assert isCopy();

        original.name = name;
        original.enabled = enabled;
        original.setKey(key);
        original.actions = new ArrayList<>(actions);
    }

    public boolean isCopy(){
        return original != null;
    }

    public boolean isCopyOf(Macro macro) {
        return original == macro;
    }

    public void setKey(Key key) {
        this.key = key;
        if (this.keyBinding != null) {

            MinecraftClient.getInstance().options.setKeyCode(keyBinding,key);
            KeyBinding.updateKeysByCode();
        }
    }

    public Key getKey(){
        return key;
    }

    public Macro createCopy() {
        return new Macro(name,enabled,key,new ArrayList<>(actions),this);
    }

}
