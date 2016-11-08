package io.bclub.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import java.lang.ref.WeakReference;

import io.bclub.R;
import io.bclub.model.Establishment;

public class EmailClickListener implements View.OnClickListener {

    String[] email;
    String subject;

    WeakReference<Activity> weakReference;

    public EmailClickListener(Activity activity, Establishment establishment) {
        this(activity, establishment.email, null);
        this.subject = buildSubject(activity, establishment);
    }

    public EmailClickListener(Activity activity, String email, String subject) {
        weakReference = new WeakReference<>(activity);
        this.email = new String[] {email};
        this.subject = subject;
    }

    @Override
    public void onClick(View v) {
        Activity activity = weakReference.get();

        if (activity == null) {
            return;
        }

        Intent intent = IntentHelper.sendEmail(activity, email, subject);

        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    String buildSubject(Context context, Establishment establishment) {
        StringBuilder sb = new StringBuilder(100);

        sb.append(context.getString(R.string.app_name))
                .append(" ")
                .append(establishment.name)
                .append(" - ")
                .append(establishment.neighborhood);

        return sb.toString();
    }
}
