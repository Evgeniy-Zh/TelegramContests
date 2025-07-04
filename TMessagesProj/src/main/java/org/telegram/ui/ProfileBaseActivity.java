package org.telegram.ui;

import android.os.Bundle;

import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.ProfileChannelCell;
import org.telegram.ui.Components.BackButtonMenu;
import org.telegram.ui.Components.UndoView;

public abstract class ProfileBaseActivity extends BaseFragment {

    public boolean saved;

    public ProfileBaseActivity(Bundle args) {
        super(args);
    }

   public abstract void setUserInfo(
            TLRPC.UserFull value,
            ProfileChannelCell.ChannelMessageFetcher channelMessageFetcher,
            ProfileBirthdayEffect.BirthdayEffectFetcher birthdayAssetsFetcher
    );

    public abstract long getTopicId();

    public abstract long getDialogId();

    public abstract UndoView getUndoView();

    public abstract void setPlayProfileAnimation(int type);

    public abstract void setChatInfo(TLRPC.ChatFull value);

    public boolean isSettings() {
        return false;
    }

    public abstract void scrollToGifts();

    public abstract boolean isChat();

    public abstract TLRPC.Chat getCurrentChat();

    public abstract TLRPC.UserFull getUserInfo();
}
