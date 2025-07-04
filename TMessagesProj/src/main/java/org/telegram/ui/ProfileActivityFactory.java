package org.telegram.ui;

import android.os.Bundle;
import android.util.Log;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.SharedMediaLayout;

import kotlin.jvm.functions.Function2;


public class ProfileActivityFactory {
    private static final String TAG = "ProfileActivityFactory";
    public static Function2<Bundle, SharedMediaLayout.SharedMediaPreloader, ProfileBaseActivity> factory;

    public static ProfileBaseActivity newInstance(Bundle args, SharedMediaLayout.SharedMediaPreloader preloader) {

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        StackTraceElement caller = stackTrace[3];

        Log.d(TAG, "Called from: " +
                caller.getClassName() + "." +
                caller.getMethodName() + "(): line " +
                caller.getLineNumber());

        try {
            Class.forName("org.telegram.ui.profile.ProfileActivity");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Log.d(TAG, "newInstance args: " + args);

        if (args.getBoolean("my_profile", false)
                || args.getBoolean("open_settings", false)) {
            return new org.telegram.ui.ProfileActivity(args, preloader);
        }
        return factory.invoke(args, preloader);
    }

    public static ProfileBaseActivity of(long dialogId) {
        Bundle bundle = new Bundle();
        if (dialogId >= 0) {
            bundle.putLong("user_id", dialogId);
        } else {
            bundle.putLong("chat_id", -dialogId);
        }
        return newInstance(bundle, null);
    }
}
