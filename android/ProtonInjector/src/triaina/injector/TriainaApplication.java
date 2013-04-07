package triaina.injector;

import android.app.Application;
import android.util.Log;

public class TriainaApplication extends Application {
    private static final String TAG = TriainaApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            TriainaInjectorFactory.initialize(this);
        } catch (Exception exp) {
            Log.e(TAG, exp.getMessage() + "", exp);
        }
    }
}
