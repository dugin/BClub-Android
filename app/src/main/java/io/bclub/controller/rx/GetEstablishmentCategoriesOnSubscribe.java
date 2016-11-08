package io.bclub.controller.rx;

import com.backendless.BackendlessCollection;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;

import java.util.ArrayList;
import java.util.List;

import io.bclub.model.EstablishmentCategory;
import io.bclub.model.backendless.BackendlessEstablishmentCategory;
import rx.Single;
import rx.SingleSubscriber;

public class GetEstablishmentCategoriesOnSubscribe implements Single.OnSubscribe<ArrayList<EstablishmentCategory>> {

    @Override
    public void call(SingleSubscriber<? super ArrayList<EstablishmentCategory>> singleSubscriber) {
        BackendlessDataQuery query = new BackendlessDataQuery();

        QueryOptions queryOptions = new QueryOptions();

        queryOptions.addSortByOption("name ASC");

        query.setQueryOptions(queryOptions);

        BackendlessCollection<BackendlessEstablishmentCategory> collection = BackendlessEstablishmentCategory.find(query);
        List<BackendlessEstablishmentCategory> page = collection.getCurrentPage();

        if (singleSubscriber.isUnsubscribed()) {
            return;
        }

        ArrayList<EstablishmentCategory> categories = new ArrayList<>(page.size());

        for (int i = 0, size = page.size(); i < size; ++i) {
            BackendlessEstablishmentCategory category = page.get(i);

            categories.add(new EstablishmentCategory(category.getObjectId(), category.getName(), category.getIcon()));
        }

        singleSubscriber.onSuccess(categories);
    }
}
