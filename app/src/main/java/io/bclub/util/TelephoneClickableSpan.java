package io.bclub.util;

import android.content.Context;
import android.content.Intent;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class TelephoneClickableSpan extends ClickableSpan {

    String telephone;

    public TelephoneClickableSpan(String telephone) {
        this.telephone = telephone;
    }

    @Override
    public void onClick(View widget) {
        Context context = widget.getContext();
        Intent intent = IntentHelper.dial(telephone);

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
    }
}
