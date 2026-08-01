package com.gibbdev.recipebookplus;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class ConditionalMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.equals("CookingPotScreenMixin")) return isModLoaded("farmersdelight");
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    private boolean isModLoaded(String id) {
        try {
            Class<?> FabricLoader = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object instance = FabricLoader.getMethod("getInstance").invoke(null);
            Method isModLoaded = FabricLoader.getMethod("isModLoaded", String.class);
            return (boolean) isModLoaded.invoke(instance, id);
        } catch (Throwable ignored) {}
        try {
            Class<?> LoadingModList = Class.forName("net.neoforged.fml.loading.LoadingModList");
            Object instance = LoadingModList.getMethod("get").invoke(null);
            if (instance != null) {
                Method getModFileById = LoadingModList.getMethod("getModFileById", String.class);
                return getModFileById.invoke(instance, id) != null;
            }
        } catch (Throwable ignored) {}
        return false;
    }

}
