package triaina.injector;

import javax.inject.Inject;
import javax.inject.Provider;

import roboguice.inject.ContextScope;
import android.content.Context;

public class TriainaContextScopedProvider<T> {
    @Inject
    protected ContextScope scope;
    @Inject
    protected Provider<T> provider;

    public T get(final Context context) {
        synchronized (ContextScope.class) {
            scope.enter(context);
            try {
                return provider.get();
            } finally {
                scope.exit(context);
            }
        }
    }
}
