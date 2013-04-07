package triaina.injector;

import roboguice.inject.RoboInjector;

public interface TriainaInjector extends RoboInjector {
    public <T> T inject(T instance);
}
