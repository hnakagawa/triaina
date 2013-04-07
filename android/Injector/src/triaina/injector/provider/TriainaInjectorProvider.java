package triaina.injector.provider;

import javax.inject.Inject;
import javax.inject.Provider;

import triaina.injector.TriainaInjector;
import triaina.injector.TriainaInjectorFactory;
import android.content.Context;

public class TriainaInjectorProvider implements Provider<TriainaInjector> {
    @Inject
    private Context mContext;

    @Override
    public TriainaInjector get() {
        return TriainaInjectorFactory.getInjector(mContext);
    }
}
