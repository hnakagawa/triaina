package triaina.webview.config;

import java.util.WeakHashMap;

/**
 * Note This is _NOT_ thread-safe.
 */
public class ConfigCache {
    private WeakHashMap<Class<?>, LayoutConfig> mLayoutConfigCache = new WeakHashMap<Class<?>, LayoutConfig>();

    private WeakHashMap<Class<?>, DomainConfig> mDomainConfigCache = new WeakHashMap<Class<?>, DomainConfig>();

    private WeakHashMap<Class<?>, BridgeConfig> mBridgeConfigCache = new WeakHashMap<Class<?>, BridgeConfig>();

    public LayoutConfig getLayoutConfig(Class<?> clazz) {
        return mLayoutConfigCache.get(clazz);
    }

    public void putLayoutConfig(Class<?> clazz, LayoutConfig config) {
        mLayoutConfigCache.put(clazz, config);
    }

    public DomainConfig getDomainConfig(Class<?> clazz) {
        return mDomainConfigCache.get(clazz);
    }

    public void putDomainConfig(Class<?> clazz, DomainConfig config) {
        mDomainConfigCache.put(clazz, config);
    }

    public BridgeConfig getBridgeObjectConfig(Class<?> clazz) {
        return mBridgeConfigCache.get(clazz);
    }

    public void putBridgeObjectConfig(Class<?> clazz, BridgeConfig configSet) {
        mBridgeConfigCache.put(clazz, configSet);
    }
}
