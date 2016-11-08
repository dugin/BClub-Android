package io.bclub.controller.rx;

import android.text.TextUtils;

import com.backendless.BackendlessCollection;
import com.backendless.persistence.BackendlessDataQuery;

import java.util.ArrayList;
import java.util.List;

import io.bclub.controller.model.CitySearchResponse;
import io.bclub.model.City;
import io.bclub.model.backendless.BackendlessCity;
import rx.Single;
import rx.SingleSubscriber;

public class CitySearchOnSubscribe implements Single.OnSubscribe<CitySearchResponse> {

    String query;
    CitySearchResponse response;

    public CitySearchOnSubscribe(String query, CitySearchResponse response) {
        this.query = query;
        this.response = response;
    }

    @Override
    public void call(SingleSubscriber<? super CitySearchResponse> singleSubscriber) {
        BackendlessCollection<BackendlessCity> result;
        BackendlessDataQuery query = new BackendlessDataQuery();

        int currentPage = 0;

        if (this.query != null && !this.query.isEmpty()) {
            query.setWhereClause(String.format("name LIKE '%s%%'", TextUtils.htmlEncode(this.query)));
        }

        query.setOffset(0);
        query.setPageSize(20);

        query.getQueryOptions().addSortByOption("name ASC");

        if (response != null) {
            currentPage = response.currentPage + 1;
            query.setOffset(currentPage);
        }

        result = BackendlessCity.find(query);

        List<BackendlessCity> page = result.getCurrentPage();
        ArrayList<City> cities = new ArrayList<>(page.size());

        if (singleSubscriber.isUnsubscribed()) {
            return;
        }

        for (int i = 0, size = page.size(); i < size; ++i) {
            BackendlessCity district = page.get(i);

            cities.add(new City(district.getObjectId(), district.getName()));
        }

        if (singleSubscriber.isUnsubscribed()) {
            return;
        }

        singleSubscriber.onSuccess(new CitySearchResponse(currentPage, cities));
    }
}
