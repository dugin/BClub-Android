package io.bclub.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ShareCompat;

import io.bclub.R;

public abstract class IntentHelper {
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
