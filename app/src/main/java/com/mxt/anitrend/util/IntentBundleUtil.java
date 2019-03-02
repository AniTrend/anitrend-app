package com.mxt.anitrend.util;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ShareCompat;
import android.text.TextUtils;

import java.util.regex.Matcher;

/**
 * Created by max on 2017/12/01.
 * Intent data formatter and helper methods
 */

public class IntentBundleUtil {

    private ShareCompat.IntentReader sharedIntent;
    private Matcher deepLinkMatcher;
    private String INTENT_ACTION;

    public IntentBundleUtil(Intent intent, FragmentActivity context) {
        if(intent != null) {
            if(intent.hasExtra(KeyUtil.arg_shortcut_used) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                ShortcutUtil.reportShortcutUsage(context, intent.getIntExtra(KeyUtil.arg_shortcut_used, KeyUtil.SHORTCUT_SEARCH));
            checkIntentData(intent, context);
        }
    }

    private boolean hasAction(Intent intent) {
        return (INTENT_ACTION = intent.getAction()) != null;
    }

    private @Nullable Uri hasData(Intent intent) {
        return intent.getData();
    }

    private @Nullable String[] hasDepth(String key) {
        if(key.contains("/"))
            return key.split("/");
        return null;
    }

    private void injectIntentParams(Intent intent) {
        String type = deepLinkMatcher.group(1);
        int groupLimit = deepLinkMatcher.groupCount();
        String lastKey = deepLinkMatcher.group(groupLimit);
        String[] splitKeys;
        switch (type) {
            case KeyUtil.DEEP_LINK_USER:
                if(TextUtils.isDigitsOnly(lastKey))
                    intent.putExtra(KeyUtil.arg_id, Long.valueOf(lastKey));
                else {
                    if (lastKey.contains("/"))
                        lastKey = lastKey.replace("/", "");
                    intent.putExtra(KeyUtil.arg_userName, lastKey);
                }
                break;
            case KeyUtil.DEEP_LINK_MANGA:
                if ((splitKeys = hasDepth(lastKey)) != null)
                    intent.putExtra(KeyUtil.arg_id, Long.valueOf(splitKeys[0]));
                else
                    intent.putExtra(KeyUtil.arg_id, Long.valueOf(lastKey));
                intent.putExtra(KeyUtil.arg_mediaType, KeyUtil.MANGA);
                    break;
            case KeyUtil.DEEP_LINK_ANIME:
                if ((splitKeys = hasDepth(lastKey)) != null)
                    intent.putExtra(KeyUtil.arg_id, Long.valueOf(splitKeys[0]));
                else
                    intent.putExtra(KeyUtil.arg_id, Long.valueOf(lastKey));
                intent.putExtra(KeyUtil.arg_mediaType, KeyUtil.ANIME);
                    break;
            case KeyUtil.DEEP_LINK_CHARACTER:
                if ((splitKeys = hasDepth(lastKey)) != null)
                    intent.putExtra(KeyUtil.arg_id, Long.valueOf(splitKeys[0]));
                else
                    intent.putExtra(KeyUtil.arg_id, Long.valueOf(lastKey));
                break;
            case KeyUtil.DEEP_LINK_ACTOR:
                if ((splitKeys = hasDepth(lastKey)) != null)
                    intent.putExtra(KeyUtil.arg_id, Long.valueOf(splitKeys[0]));
                else
                    intent.putExtra(KeyUtil.arg_id, Long.valueOf(lastKey));
                break;
            case KeyUtil.DEEP_LINK_STAFF:
                if ((splitKeys = hasDepth(lastKey)) != null)
                    intent.putExtra(KeyUtil.arg_id, Long.valueOf(splitKeys[0]));
                else
                    intent.putExtra(KeyUtil.arg_id, Long.valueOf(lastKey));
                break;
            case KeyUtil.DEEP_LINK_STUDIO:
                if ((splitKeys = hasDepth(lastKey)) != null)
                    intent.putExtra(KeyUtil.arg_id, Long.valueOf(splitKeys[0]));
                else
                    intent.putExtra(KeyUtil.arg_id, Long.valueOf(lastKey));
                break;
        }
    }

    private void checkIntentData(Intent intent, FragmentActivity context) {
        Uri INTENT_DATA;
        if (hasAction(intent) && INTENT_ACTION.equals(Intent.ACTION_SEND))
            sharedIntent = ShareCompat.IntentReader.from(context);
        else
            if ((INTENT_DATA = hasData(intent)) != null)
                if((deepLinkMatcher = RegexUtil.INSTANCE.findIntentKeys(INTENT_DATA.getPath())) != null)
                    injectIntentParams(intent);
    }

    public @Nullable ShareCompat.IntentReader getSharedIntent() {
        return sharedIntent;
    }
}
