package triaina.injector;

import java.util.ArrayList;
import java.util.WeakHashMap;

import roboguice.event.EventManager;
import roboguice.inject.ContextScope;
import roboguice.inject.ContextScopedRoboInjector;
import roboguice.inject.ResourceListener;
import roboguice.inject.ViewListener;
import triaina.commons.exception.CommonRuntimeException;
import triaina.injector.internal.TriainaInjectorImpl;
import triaina.injector.internal.TriainaModule;
import android.app.Application;
import android.content.Context;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.StaticInjectionRequest;

public final class TriainaInjectorFactory {
    public static final Stage DEFAULT_STAGE = Stage.PRODUCTION;

    private static WeakHashMap<Application, Injector> sBaseInjectors = new WeakHashMap<Application, Injector>();
    private static WeakHashMap<Context, TriainaInjector> sInjectors = new WeakHashMap<Context, TriainaInjector>();
    private static WeakHashMap<Application, ResourceListener> sResourceListeners = new WeakHashMap<Application, ResourceListener>();
    private static WeakHashMap<Application, ViewListener> sViewListeners = new WeakHashMap<Application, ViewListener>();

    private TriainaInjectorFactory() {
    }

    public static Injector getBaseApplicationInjector(Application application) {
        synchronized (TriainaInjectorFactory.class) {
            Injector injector = sBaseInjectors.get(application);
            if (injector == null)
                injector = initialize(application, DEFAULT_STAGE);

            return injector;
        }
    }

    public static Injector initialize(final Application application, Stage stage, Module... modules) {
        for (Element element : Elements.getElements(modules)) {
            element.acceptVisitor(new DefaultElementVisitor<Void>() {
                @Override
                public Void visit(StaticInjectionRequest element) {
                    getResourceListener(application).requestStaticInjection(element.getType());
                    return null;
                }
            });
        }

        synchronized (TriainaInjectorFactory.class) {
            final Injector injector = Guice.createInjector(stage, modules);
            sBaseInjectors.put(application, injector);
            return injector;
        }
    }

    /**
     * Return the cached Injector instance for this application, or create a new
     * one if necessary.
     */
    public static Injector initialize(Application application, Stage stage) {
        synchronized (TriainaInjectorFactory.class) {
            final ArrayList<Module> modules = new ArrayList<Module>();
            modules.add(newDefaultRoboModule(application));
            modules.add(new TriainaModule());

            try {
                final int id = application.getResources().getIdentifier("triaina_modules", "array",
                        application.getPackageName());
                final String[] moduleNames = id > 0 ? application.getResources().getStringArray(id) : new String[] {};
                for (String name : moduleNames) {
                    final Class<? extends Module> clazz = Class.forName(name).asSubclass(Module.class);
                    try {
                        modules.add(clazz.getDeclaredConstructor(Context.class).newInstance(application));
                    } catch (final NoSuchMethodException noActivityConstructorException) {
                        modules.add(clazz.newInstance());
                    }
                }
            } catch (Exception exp) {
                throw new CommonRuntimeException(exp);
            }

            return initialize(application, stage, modules.toArray(new Module[modules.size()]));
        }
    }

    public static TriainaInjector getInjector(Context context) {
        final Application application = (Application) context.getApplicationContext();
        synchronized (TriainaInjectorFactory.class) {
            TriainaInjector injector = sInjectors.get(context);
            if (injector == null) {
                injector = new TriainaInjectorImpl(new ContextScopedRoboInjector(context,
                        getBaseApplicationInjector(application), getViewListener(application)));
                sInjectors.put(context, injector);
            }
            return injector;
        }
    }

    public static <T> T injectMembers(Context context, T obj) {
        getInjector(context).injectMembers(obj);
        return obj;
    }

    public static DefaultTriainaModule newDefaultRoboModule(final Application application) {
        return new DefaultTriainaModule(application, new ContextScope(), getViewListener(application),
                getResourceListener(application));
    }

    protected static ResourceListener getResourceListener(Application application) {
        synchronized (TriainaInjectorFactory.class) {
            ResourceListener resourceListener = sResourceListeners.get(application);
            if (resourceListener == null) {
                resourceListener = new ResourceListener(application);
                sResourceListeners.put(application, resourceListener);
            }
            return resourceListener;
        }
    }

    protected static ViewListener getViewListener(final Application application) {
        synchronized (TriainaInjectorFactory.class) {
            ViewListener viewListener = sViewListeners.get(application);
            if (viewListener == null) {
                viewListener = new ViewListener();
                sViewListeners.put(application, viewListener);
            }
            return viewListener;
        }
    }

    public static void destroyInjector(Context context) {
        synchronized (TriainaInjectorFactory.class) {
            final TriainaInjector injector = getInjector(context);
            if (injector == null)
                return;

            sInjectors.remove(context);
            injector.getInstance(EventManager.class).destroy();
            injector.getInstance(ContextScope.class).destroy(context);
        }
    }
}
