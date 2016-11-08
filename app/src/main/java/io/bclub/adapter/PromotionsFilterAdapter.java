package io.bclub.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import io.bclub.R;
import io.bclub.bus.GenericPublishSubject;
import io.bclub.bus.PublishItem;
import io.bclub.model.EstablishmentPromotion;
import io.bclub.util.DisplayHelper;
import io.bclub.view.PromotionView;

public class PromotionsFilterAdapter extends RecyclerViewAdapter<EstablishmentPromotion, PromotionView> implements View.OnClickListener {

    Integer imageWidth;

    public PromotionsFilterAdapter(Context context) {
        super(context);
    }

    @Override
    int getLayoutResForViewType(int viewType) {
        return R.layout.list_item_filter_promotion;
    }

    @Override
    void onPostCreateViewHolder(RecyclerViewHolder<PromotionView> holder, ViewGroup parent) {
        if (imageWidth == null) {
            // Considering card margins
            imageWidth = (int) (((float) (parent.getWidth() - DisplayHelper.dpToPixels(parent.getContext(), 32))) / 3.75F);
        }

        holder.itemView.setOnClickListener(this);

        ((PromotionView) (holder.itemView)).setImageSize(imageWidth, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onClick(View v) {
        EstablishmentPromotion promotion = ((PromotionView) v).get();
        GenericPublishSubject.PUBLISH_SUBJECT.onNext(PublishItem.of(GenericPublishSubject.FILTER_PROMOTION_CLICKED_TYPE, promotion));
    }
}
