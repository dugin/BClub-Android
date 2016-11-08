package io.bclub.widget;

import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;

public class DebounceTextWatcher implements TextWatcher {

    private static final int MESSAGE = 123;
    private static final long DELAY = 1000L;

    Handler handler;

    public DebounceTextWatcher(OnTextChangeListener listener) {
        this.handler = new CustomHandler(listener);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Message msg = Message.obtain();

        msg.obj = s;
        msg.what = MESSAGE;

        handler.removeMessages(MESSAGE);
        handler.sendMessageDelayed(msg, DELAY);
    }

    @Override
    public void afterTextChanged(Editable s) { }

    public interface OnTextChangeListener {
        void onTextChanged(CharSequence text);
    }

    private static class CustomHandler extends Handler {

        OnTextChangeListener listener;

        public CustomHandler(OnTextChangeListener listener) {
            this.listener = listener;
        }

        @Override
        public void handleMessage(Message msg) {
            if (listener != null) {
                listener.onTextChanged((CharSequence) msg.obj);
            }
        }
    }
}
