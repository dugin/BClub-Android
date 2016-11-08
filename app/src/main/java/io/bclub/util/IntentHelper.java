package io.bclub.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ShareCompat;
import android.telephony.PhoneNumberUtils;

import java.util.ArrayList;

import io.bclub.R;
import io.bclub.activity.MainActivity;

public abstract class IntentHelper {

    public static final String INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED = "finishActivityOnSaveCompleted";

    private IntentHelper() { }

    public static Intent sendEmail(Activity activity, String[] email, String subject) {
        Intent intent = ShareCompat.IntentBuilder.from(activity)
                .setEmailTo(email)
                .setSubject(subject)
                .setType("message/rfc822")
                .setChooserTitle(R.string.send_mail)
                .createChooserIntent();

        return intent;
    }


    public static Intent view(String uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setData(Uri.parse(uri));

        return intent;
    }

    public static Intent dial(String telephone) {
        Intent intent = new Intent(Intent.ACTION_DIAL);

        intent.setData(Uri.parse(String.format("tel: %s", telephone)));

        return intent;
    }
}
