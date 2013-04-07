package triaina.injector.internal;

import javax.inject.Provider;

import proton.inject.Injector;
import proton.inject.listener.ProviderListeners;
import android.content.Context;
import triaina.injector.TriainaInjector;

public class TriainaInjectorImpl implements TriainaInjector {
    private final Injector mInjector;

    public TriainaInjectorImpl(Injector injector) {
        mInjector = injector;
    }

    @Override
    public <T> T getInstance(Class<T> key) {
        return mInjector.getInstance(key);
    }

    @Override
    public <T> Provider<T> getProvider(Class<T> key) {
        return mInjector.getProvider(key);
    }

    @Override
    public <T> T inject(T obj) {
        return mInjector.inject(obj);
    }

    @Override
    public Injector getApplicationInjector() {
        return mInjector.getApplicationInjector();
    }

    @Override
    public Context getContext() {
        return mInjector.getContext();
    }

    @Override
    public ProviderListeners getProviderListeners() {
        return mInjector.getProviderListeners();
    }

}
