package org.telegram.ui;

import android.os.Bundle;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.SharedMediaLayout;

import kotlin.jvm.functions.Function2;


public class ProfileActivityFactory {
    public static Function2<Bundle, SharedMediaLayout.SharedMediaPreloader, BaseFragment> factory;

    public static BaseFragment _new(Bundle args, SharedMediaLayout.SharedMediaPreloader preloader) {

        try {
            Class.forName("org.telegram.ui.profile.ProfileActivity");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return factory.invoke(args, preloader);
    }
}
