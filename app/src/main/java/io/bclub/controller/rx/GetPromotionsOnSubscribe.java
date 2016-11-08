package io.bclub.controller.rx;

import android.location.Location;

import com.backendless.BackendlessCollection;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.bclub.controller.ModelMapper;
import io.bclub.controller.model.FetchPromotionsResponse;
import io.bclub.model.EstablishmentCategory;
import io.bclub.model.EstablishmentPromotion;
import io.bclub.model.Promotion;
import io.bclub.model.backendless.BackendlessEstablishmentPromotion;
import rx.Single;
import rx.SingleSubscriber;

public class GetPromotionsOnSubscribe implements Single.OnSubscribe<FetchPromotionsResponse> {

    private static final String[] PROMOTION_WEEKDAYS = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};

    private static final int OFFSET = 50;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);

    Location location;
    Filters filters;

    int page;
    boolean featuredFirst;

    public GetPromotionsOnSubscribe(Location location, Filters filters, int page, boolean featuredFirst) {
        this.location = location;
        this.page = page;
        this.filters = filters;
        this.featuredFirst = featuredFirst;
    }

    @Override
    public void call(SingleSubscriber<? super FetchPromotionsResponse> singleSubscriber) {
        BackendlessCollection<BackendlessEstablishmentPromotion> establishments = getOrdinaryPromotions();

        if (singleSubscriber.isUnsubscribed()) {
            return;
        }

        List<BackendlessEstablishmentPromotion> page = establishments.getCurrentPage();
        EstablishmentPromotion featuredPromotion = null;
        List<EstablishmentPromotion> result = new ArrayList<>();

        do {
            result.addAll(ModelMapper.buildEstablishmentsPromotions(page, location));

            establishments = establishments.nextPage();
            page = establishments.getCurrentPage();
        } while (establishments.getTotalObjects() != result.size());

        if (featuredFirst) {
            BackendlessCollection<BackendlessEstablishmentPromotion> featuredPromotions = getFeaturedPromotions();
            List<BackendlessEstablishmentPromotion> featuredPage = featuredPromotions.getData();

            if (featuredPage != null && !featuredPage.isEmpty()) {
                BackendlessEstablishmentPromotion firstFeatured = featuredPage.get(0);
                featuredPromotion = ModelMapper.buildEstablishmentPromotion(firstFeatured, location);
            }
        }

        singleSubscriber.onSuccess(new FetchPromotionsResponse(sortEstablishments(result), featuredPromotion, location, page.size() >= OFFSET));
    }

    BackendlessCollection<BackendlessEstablishmentPromotion> getFeaturedPromotions() {
        BackendlessDataQuery query = new BackendlessDataQuery();
        QueryOptions options = getEstablishmentPromotionOptions();

        StringBuilder sb = new StringBuilder(String.format("active = true and featuredDate >= '%s'", DATE_FORMAT.format(new Date())));

        query.setQueryOptions(options);

        options.addSortByOption("featuredDate ASC");

        if (filters != null) {
            appendFilters(sb, filters);
        }

        query.setWhereClause(sb.toString());

        return BackendlessEstablishmentPromotion.find(query);
    }

    BackendlessCollection<BackendlessEstablishmentPromotion> getOrdinaryPromotions() {
        BackendlessDataQuery query = new BackendlessDataQuery();
        StringBuilder sb = new StringBuilder("active = true");

        if (featuredFirst) {
            sb.append(" and featuredDate is null");
        }

        query.setQueryOptions(getEstablishmentPromotionOptions());

        if (filters != null) {
            appendFilters(sb, filters);
        }

        query.setWhereClause(sb.toString());

        return BackendlessEstablishmentPromotion.find(query);
    }

    void appendFilters(StringBuilder sb, Filters filters) {
        if (filters.cityId != null) {
            sb.append(" and establishment.address.neighborhood.city.objectId = '").append(filters.cityId).append("'");
        }

        if (filters.categories != null && !filters.categories.isEmpty()) {
            sb.append(" and establishment.category.objectId in (");

            for (int i = 0, size = filters.categories.size(); i < size; ++i) {
                sb.append("'").append(filters.categories.get(i).id).append("'");

                if ((i + 1) < size) {
                    sb.append(", ");
                }
            }

            sb.append(")");
        }

        if (filters.percents != null && !filters.percents.isEmpty()) {
            sb.append(" and promotions.percent in (");

            for (int i = 0, size = filters.percents.size(); i < size; ++i) {
                sb.append("'").append(filters.percents.get(i)).append("'");

                if ((i + 1) < size) {
                    sb.append(", ");
                }
            }

            sb.append(")");
        }

        if (filters.query != null) {
            sb.append(" and establishment.name LIKE '%").append(filters.query).append("%'");
        }

        if (filters.weekDays != null) {
            sb.append(" and (");

            boolean hasPrevious = false;

            for (int i = 0, size = filters.weekDays.length; i < size; ++i) {
                if (filters.weekDays[i]) {
                    if (hasPrevious) {
                        sb.append(" or ");
                    }

                    sb.append("promotions.").append(PROMOTION_WEEKDAYS[i]).append(" is not null");

                    hasPrevious = true;
                }
            }

            sb.append(")");
        }
    }

    QueryOptions getEstablishmentPromotionOptions() {
        QueryOptions options = new QueryOptions();

        options.setRelationsDepth(3);

        options.setOffset(page);
        options.setPageSize(OFFSET);

        options.addRelated("promotions");

        options.addRelated("establishment");
        options.addRelated("establishment.category");
        options.addRelated("establishment.address");
        options.addRelated("establishment.address.neighborhood");

        return options;
    }

    List<EstablishmentPromotion> sortEstablishments(List<EstablishmentPromotion> list) {
        Collections.sort(list, (lhs, rhs) -> {
            double lhsValue = lhs.establishment.distance;
            double rhsValue = rhs.establishment.distance;

            if (lhsValue == rhsValue) {
                Promotion lhsPromotion = lhs.getLast();
                Promotion rhsPromotion = rhs.getLast();

                if (lhsPromotion != null && rhsPromotion != null) {
                    lhsValue = lhsPromotion.percent;
                    rhsValue = rhsPromotion.percent;
                }
            }

            return Double.compare(lhsValue, rhsValue);
        });

        return list;
    }

    public static class Filters {
        public final String cityId;
        public final String query;

        public final List<EstablishmentCategory> categories;
        public final List<Float> percents;

        public final boolean[] weekDays;

        public Filters() {
            this(null, null, null, null, null);
        }

        public Filters(String cityId, List<EstablishmentCategory> categories, List<Float> percents, boolean[] weekDays, String query) {
            this.cityId = cityId;
            this.query = query;

            this.categories = categories;
            this.percents = percents;

            this.weekDays = weekDays;
        }

        public Filters copy(String cityId) {
            return new Filters(cityId, categories, percents, weekDays, query);
        }
    }
}