package io.bclub.dialog;

import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.bclub.R;

public class SplashScreenDialog extends DialogFragment {

    private static final long TIMEOUT = 7000L;

    private static final int ANIMATION_MESSAGE = 123;
    private static final int END_MESSAGE = 124;

    @BindView(R.id.indicator_containers)
    ViewGroup indicatorContainers;

    AnimationHandler handler;

    public SplashScreenDialog() {
        setShowsDialog(true);
        setCancelable(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (handler != null) {
            handler.removeMessages(ANIMATION_MESSAGE);
            handler.removeMessages(END_MESSAGE);
            handler = null;
        }

        if (indicatorContainers != null) {
            for (int i = 0, size = indicatorContainers.getChildCount(); i < size; ++i) {
                View view = indicatorContainers.getChildAt(i);

                if (view != null) {
                    ViewCompat.animate(view).setListener(null).cancel();
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.splash_screen, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        handler = new AnimationHandler(this);
        handler.sendEmptyMessageDelayed(END_MESSAGE, TIMEOUT);

        startAnimation();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
    }

    void startAnimation() {
        for (int i = 0, size = indicatorContainers.getChildCount(); i < size; ++i) {
            View child = indicatorContainers.getChildAt(i);
            animate(child, i, size - 1);
        }
    }

    void animate(View child, final int i, final int max) {
        ViewCompat.animate(child)
                .scaleX(1F)
                .scaleY(1F)
                .setStartDelay(i * 50L)
                .setListener(new ReduceVPAListener(i == max ? this : null))
                .setDuration(250);
    }

    private static class ReduceVPAListener extends ViewPropertyAnimatorListenerAdapter {

        WeakReference<SplashScreenDialog> reference;

        public ReduceVPAListener(SplashScreenDialog dialog) {
            reference = new WeakReference<>(dialog);
        }

        @Override
        public void onAnimationEnd(View view) {
            ViewCompat.animate(view)
                    .scaleX(4F)
                    .scaleY(4F)
                    .setListener(new RestartVPAListener(reference.get()))
                    .setDuration(250);
        }
    }

    private static class RestartVPAListener extends ViewPropertyAnimatorListenerAdapter {
        WeakReference<SplashScreenDialog> reference;

        public RestartVPAListener(SplashScreenDialog dialog) {
            reference = new WeakReference<>(dialog);
        }

        @Override
        public void onAnimationEnd(View view) {
            SplashScreenDialog dialog = reference.get();

            if (dialog != null) {
                dialog.startAnimation();
            }
        }
    }

    private static class AnimationHandler extends Handler {
        SplashScreenDialog dialog;

        int size;

        public AnimationHandler(SplashScreenDialog dialog) {
            this.dialog = dialog;
            size = dialog.indicatorContainers.getChildCount();
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == END_MESSAGE) {
                dialog.dismiss();
            }
        }
    }
}
