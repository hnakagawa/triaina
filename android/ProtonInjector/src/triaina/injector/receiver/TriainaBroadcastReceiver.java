package triaina.injector.receiver;

import proton.inject.receiver.ProtonBroadcastReceiver;

import android.content.Context;
import android.content.Intent;

public abstract class TriainaBroadcastReceiver extends ProtonBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        handleReceive(context, intent);
    }

    protected abstract void handleReceive(Context context, Intent intent);
}
