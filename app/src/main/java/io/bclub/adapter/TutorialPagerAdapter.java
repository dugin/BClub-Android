package io.bclub.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import io.bclub.R;

public class TutorialPagerAdapter extends PagerAdapter {

    private static final int[] LAYOUT_RES = {R.layout.include_tutorial_1, R.layout.include_tutorial_2, R.layout.include_tutorial_3};

    LayoutInflater layoutInflater;

    OnPageInstantiatedListener listener;

    Button joined;

    Button enter;

    public TutorialPagerAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    public void setOnPageInstantiatedListener(OnPageInstantiatedListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return LAYOUT_RES.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = layoutInflater.inflate(LAYOUT_RES[position], container, false);

        joined = (Button) view.findViewById(R.id.btn_joined);
        enter = (Button) view.findViewById(R.id.btn_enter);

        if (joined != null) {

            joined.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onJoined();
                    }
                }
            });

        }

        if (enter != null) {
            enter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onEnter();
                    }
                }
            });
        }

        container.addView(view);

        return view;
    }

    public interface OnPageInstantiatedListener {
        void onJoined();
        void onEnter();
    }
}
