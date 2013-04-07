package triaina.injector.internal;

import roboguice.inject.ContextSingleton;
import triaina.injector.TriainaInjector;
import triaina.injector.provider.TriainaInjectorProvider;

import com.google.inject.AbstractModule;

public class TriainaModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(TriainaInjector.class).toProvider(TriainaInjectorProvider.class).in(ContextSingleton.class);
    }

}
