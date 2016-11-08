package io.bclub.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.bclub.R;
import io.bclub.model.EstablishmentPromotion;
import io.bclub.model.Promotion;
import io.bclub.util.Constants;
import io.bclub.widget.WeekDaysView;
import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

public class PromotionView extends CardView implements AbstractView<EstablishmentPromotion> {

    @BindView(R.id.iv_promotion_banner)
    ImageView ivBanner;

    @BindView(R.id.tv_establishment_title)
    TextView tvTitle;

    @Nullable
    @BindView(R.id.tv_establishment_category)
    TextView tvCategory;

    @Nullable
    @BindView(R.id.tv_establishment_neighborhood)
    TextView tvNeighborhood;

    @Nullable
    @BindView(R.id.tv_establishment_distance)
    TextView tvDistance;

    @Nullable
    @BindView(R.id.workdays_view)
    WeekDaysView weekDaysView;

    @BindView(R.id.tv_promotion_percent)
    TextView tvPercent;

    EstablishmentPromotion promotion;

    Typeface boldTypeface;

    public PromotionView(Context context) {
        super(context);
    }

    public PromotionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PromotionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ButterKnife.bind(this);

        boldTypeface = TypefaceUtils.load(getContext().getAssets(), "fonts/OpenSans-Bold.ttf");
    }

    @Override
    public void bind(EstablishmentPromotion promotion) {
        Context context = getContext();

        this.promotion = promotion;

        tvTitle.setText(promotion.establishment.name);

        if (tvCategory != null) {
            if (promotion.establishment.categoryName == null) {
                tvCategory.setVisibility(View.GONE);
            } else {
                tvCategory.setText(promotion.establishment.categoryName);
            }
        }

        if (tvNeighborhood != null) {
            tvNeighborhood.setText(promotion.establishment.neighborhood.concat(promotion.hasDistanceCalculated() ? " - " : ""));
        }

        tvPercent.setText(formatPercent(promotion.getFirst(), promotion.isSinglePromotion() ? null : promotion.getLast()), TextView.BufferType.SPANNABLE);

        if (weekDaysView != null) {
            weekDaysView.setVisibility(View.VISIBLE);
            weekDaysView.setWeekDays(promotion.weekDays);
        }

        if (promotion.imageUrl != null) {
            ViewGroup.LayoutParams lp = getLayoutParams();

            DrawableRequestBuilder<String> request = Glide.with(context)
                    .load(promotion.imageUrl)
                    .bitmapTransform(new FitCenter(context), new CenterCrop(context))
                    .diskCacheStrategy(DiskCacheStrategy.ALL);

            if (lp.width > 0 && lp.height > 0) {
                request = request.override(lp.width, lp.height);
            }

            request.into(ivBanner);

            ivBanner.setVisibility(View.VISIBLE);
        } else {
            ivBanner.setVisibility(View.VISIBLE);
        }

        if (tvDistance != null) {
            if (promotion.hasDistanceCalculated()) {
                tvDistance.setText(promotion.establishment.getFormattedDistance());
            } else {
                tvDistance.setText(null);
            }
        }
    }

    CharSequence formatPercent(Promotion first, Promotion last) {
        SpannableStringBuilder sb = new SpannableStringBuilder();

        if (first != null) {
            sb.append(formatPercent(first.percent, last == null));
        }

        if (last != null) {
            sb.append(" - ");
            sb.append(formatPercent(last.percent, true));
        }

        if (sb.length() == 0) {
            return null;
        }

        return sb;
    }

    CharSequence formatPercent(double percent, boolean includePercent) {
        String format = includePercent ? "%d%%" : "%d";
        SpannableString spannableString = new SpannableString(String.format(Constants.PT_BR, format, (int) (Math.round(percent * 100))));

        CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(boldTypeface);

        spannableString.setSpan(typefaceSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    @Override
    public EstablishmentPromotion get() {
        return promotion;
    }

    public void setImageSize(int imageWidth, int imageHeight) {
        ViewGroup.LayoutParams lp = ivBanner.getLayoutParams();

        lp.width = imageWidth;
        lp.height = imageHeight;
    }
}
