package io.bclub.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import io.bclub.R;
import io.bclub.bus.GenericPublishSubject;
import io.bclub.bus.PublishItem;
import io.bclub.model.City;
import io.bclub.view.CityView;

public class CitySearchAdapter extends RecyclerViewAdapter<City, CityView> implements View.OnClickListener {

    public CitySearchAdapter(Context context) {
        super(context);
    }

    @Override
    int getLayoutResForViewType(int viewType) {
        return R.layout.list_item_search_city;
    }

    @Override
    void onPostCreateViewHolder(RecyclerViewHolder<CityView> holder, ViewGroup parent) {
        super.onPostCreateViewHolder(holder, parent);

        holder.itemView.setOnClickListener(this);
    }

    @Override
    void onPostBindViewHolder(RecyclerViewHolder<CityView> holder, City city, int position) {
        super.onPostBindViewHolder(holder, city, position);

        if (position == (getItemCount() - 1)) {
            ((CityView)holder.itemView).hideDivider();
        } else {
            ((CityView)holder.itemView).showDivider();
        }
    }

    @Override
    public void onClick(View v) {
        CityView cityView = (CityView) v;

        GenericPublishSubject.PUBLISH_SUBJECT
                .onNext(PublishItem.of(GenericPublishSubject.CITY_CLICKED_TYPE, cityView.get()));
    }
}
