package triaina.injector;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

import proton.inject.DefaultModule;
import proton.inject.Module;
import proton.inject.Proton;
import proton.inject.ProvisionException;
import triaina.injector.internal.TriainaInjectorImpl;
import triaina.injector.internal.TriainaModule;
import android.app.Application;
import android.content.Context;

public final class TriainaInjectorFactory {
    private static Map<Context, TriainaInjector> sInjectors = new WeakHashMap<Context, TriainaInjector>();

    private TriainaInjectorFactory() {
    }

    public static void initialize(Application application) {
        ArrayList<Module> list = new ArrayList<Module>();
        list.add(new DefaultModule());
        list.add(new TriainaModule());

        try {
            int id = application.getResources().getIdentifier("triaina_modules", "array", application.getPackageName());
            String[] moduleNames = id > 0 ? application.getResources().getStringArray(id) : new String[] {};
            for (String name : moduleNames) {
                Class<? extends Module> clazz = Class.forName(name).asSubclass(Module.class);
                try {
                    list.add(clazz.getDeclaredConstructor(Context.class).newInstance(application));
                } catch (final NoSuchMethodException noActivityConstructorException) {
                    list.add(clazz.newInstance());
                }
            }
        } catch (Exception exp) {
            throw new ProvisionException(exp);
        }

        synchronized (TriainaInjectorFactory.class) {
            Proton.initialize(application, list.toArray(new Module[list.size()]));
            sInjectors.put(application, new TriainaInjectorImpl(Proton.getInjector(application)));
        }
    }

    public static TriainaInjector getBaseApplicationInjector(Application application) {
        synchronized (TriainaInjectorFactory.class) {
            return getInjector(application);
        }
    }

    public static TriainaInjector getInjector(Context context) {
        synchronized (TriainaInjectorFactory.class) {
            TriainaInjector injector = sInjectors.get(context);
            if (injector == null) {
                injector = new TriainaInjectorImpl(Proton.getInjector(context));
                sInjectors.put(context, injector);
            }
            return injector;
        }
    }

    public static void destroyInjector(Context context) {
        synchronized (TriainaInjectorFactory.class) {
            sInjectors.remove(context);
            Proton.destroyInjector(context);
        }
    }
}
