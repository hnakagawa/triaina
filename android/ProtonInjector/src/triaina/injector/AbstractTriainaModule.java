package triaina.injector;

import proton.inject.AbstractModule;

public abstract class AbstractTriainaModule extends AbstractModule {
    @Override
    abstract protected void configure();
}
