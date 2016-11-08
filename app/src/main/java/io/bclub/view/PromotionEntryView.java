package io.bclub.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.bclub.R;
import io.bclub.model.Promotion;
import io.bclub.rx.ObservableSchedulerTransformer;
import io.bclub.util.Constants;
import rx.Observable;
import rx.Subscription;

public class PromotionEntryView extends LinearLayout {

    private static final int[] DAYS_RES_ID = {R.string.monday, R.string.tuesday, R.string.wednesday, R.string.thursday, R.string.friday, R.string.saturday, R.string.sunday};

    @BindView(R.id.tv_promotion_percent)
    TextView tvPercent;

    @BindView(R.id.tv_promotion_title)
    TextView tvTitle;

    Promotion promotion;

    Subscription subscription;

    public PromotionEntryView(Context context) {
        super(context);
    }

    public PromotionEntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PromotionEntryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PromotionEntryView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ButterKnife.bind(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (subscription != null) {
            subscription.unsubscribe();
        }

        subscription = null;
    }

    public void bind(final Promotion promotion) {
        this.promotion = promotion;

        Observable.range(0, promotion.weekDays.length)
                .filter(index -> promotion.weekDays[index] != null)
                .toList()
                .compose(ObservableSchedulerTransformer.ofComputationToMainThread())
                .subscribe(list -> {
                    tvTitle.setText(join(list));
                });

        tvPercent.setText(formatPercent(promotion.percent));
    }

    String join(List<Integer> weekDays) {
        StringBuilder sb = new StringBuilder();
        Context context = getContext();

        for (int i = 0, size = weekDays.size(); i < size; ++i) {
            sb.append(context.getString(DAYS_RES_ID[weekDays.get(i)]));

            if ((i + 2) < size) {
                sb.append(", ");
            } else if ((i + 1) < size) {
                sb.append(context.getString(R.string.and));
            }
        }

        sb.append(".");

        return sb.toString();
    }

    CharSequence formatPercent(double percent) {
        return String.format(Constants.PT_BR, "%d%%", (int) (Math.round(percent * 100)));
    }
}
