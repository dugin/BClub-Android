package io.bclub.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import io.bclub.R;
import io.bclub.bus.GenericPublishSubject;
import io.bclub.bus.PublishItem;
import io.bclub.model.EstablishmentPromotion;
import io.bclub.view.PromotionView;

public class PromotionsAdapter extends RecyclerViewAdapter<EstablishmentPromotion, PromotionView> implements View.OnClickListener {

    private static final int FEATURED_VIEW_TYPE = 1;
    private static final int PROMOTION_VIEW_TYPE = 2;

    public PromotionsAdapter(Context context) {
        super(context);
    }

    @Override
    int getLayoutResForViewType(int viewType) {
        if (viewType == FEATURED_VIEW_TYPE) {
            return R.layout.list_item_featured_promotion;
        } else {
            return R.layout.list_item_promotion;
        }
    }

    @Override
    int getViewTypeForPosition(int position) {
        EstablishmentPromotion establishmentPromotion = list.get(position);

        //noinspection ConstantConditions
        if (establishmentPromotion.isSinglePromotion() && establishmentPromotion.featuredDate != null) {
            return FEATURED_VIEW_TYPE;
        }

        return PROMOTION_VIEW_TYPE;
    }

    @Override
    void onPostCreateViewHolder(RecyclerViewHolder<PromotionView> holder, ViewGroup parent) {
        holder.itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        EstablishmentPromotion promotion = ((PromotionView) v).get();
        GenericPublishSubject.PUBLISH_SUBJECT.onNext(PublishItem.of(GenericPublishSubject.PROMOTION_CLICKED_TYPE, promotion));
    }
}
