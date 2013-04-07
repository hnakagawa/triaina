package triaina.injector.internal;

import proton.inject.AbstractModule;
import triaina.injector.TriainaInjector;
import triaina.injector.provider.TriainaInjectorProvider;

public class TriainaModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TriainaInjector.class).toProvider(TriainaInjectorProvider.class);
    }
}
