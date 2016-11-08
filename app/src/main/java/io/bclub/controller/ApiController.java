package io.bclub.controller;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Pair;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.exceptions.BackendlessException;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;
import com.google.gson.Gson;
import com.stripe.android.model.Token;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.bclub.R;
import io.bclub.application.Application;
import io.bclub.controller.model.CitySearchResponse;
import io.bclub.controller.model.FetchPromotionsResponse;
import io.bclub.controller.rx.CitySearchOnSubscribe;
import io.bclub.controller.rx.GetEstablishmentCategoriesOnSubscribe;
import io.bclub.controller.rx.GetLastKnownLocationOnSubscribe;
import io.bclub.controller.rx.GetPromotionsOnSubscribe;
import io.bclub.exception.ActionException;
import io.bclub.exception.EntityNotFoundException;
import io.bclub.exception.UserNotAuthenticatedException;
import io.bclub.model.City;
import io.bclub.model.EstablishmentCategory;
import io.bclub.model.EstablishmentPromotion;
import io.bclub.model.User;
import io.bclub.model.UserInfo;
import io.bclub.model.backendless.BackendlessCity;
import io.bclub.model.backendless.BackendlessEstablishmentPromotion;
import io.bclub.model.backendless.BackendlessVoucher;
import io.bclub.util.LocationCache;
import rx.Single;

public class ApiController extends BaseController {

    private static final long HOUR = TimeUnit.HOURS.toMillis(1);

    private static final String CARD_DECLINED_EXCEPTION = "Your card was declined";
    private static final String CARD_PROCESSING_EXCEPTION = "An error occurred while processing your card. Try again in a little bit.";

    LocationCache locationCache = new LocationCache(HOUR);

    Gson gson;

    public ApiController(Application app, PreferencesManager preferencesManager, Gson gson) {
        super(app, preferencesManager);
        this.gson = gson;
    }

    public Single<FetchPromotionsResponse> getPromotions(final int page) {
        return getPromotions(page, new GetPromotionsOnSubscribe.Filters(), true);
    }

    public Single<FetchPromotionsResponse> getPromotions(final int page, final GetPromotionsOnSubscribe.Filters filters, boolean featuredFirst) {
        Single<Location> locationSingle = locationSingle();

        if (preferencesManager.getCityId() == null) {
            locationSingle = locationSingle.map(location -> {
                if (location != null) {
                    reverseLocation(location);
                }

                return location;
            });
        }

        return locationSingle
                .flatMap(location -> {
                    final GetPromotionsOnSubscribe.Filters copy;

                    if (filters.cityId == null) {
                        copy = filters.copy(preferencesManager.getCityId());
                    } else {
                        copy = filters;
                    }

                    return checkConnectivity(Single.create(new GetPromotionsOnSubscribe(location, copy, page, featuredFirst)));
                })
                .retry((integer, throwable) -> throwable instanceof BackendlessException && integer < 3);
    }

    void reverseLocation(Location location) {
        if (Geocoder.isPresent() && location != null) {
            Geocoder geocoder = new Geocoder(app, Locale.getDefault());

            try {
                List<Address> list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                if (!list.isEmpty()) {
                    Address first = list.get(0);
                    String locality = first.getLocality();

                    if (locality != null) {
                        City city = getCityByName(locality);

                        if (city != null) {
                            preferencesManager.setCityInfo(city.objectId, city.name);
                        }
                    }
                }

            } catch (IOException e) { }
        }
    }

    City getCityByName(String name) {
        BackendlessDataQuery query = new BackendlessDataQuery();

        query.setWhereClause(String.format("name = '%s'", name));
        query.setPageSize(1);

        BackendlessCollection<BackendlessCity> result = BackendlessCity.find(query);
        List<BackendlessCity> list = result.getData();

        if (list == null || list.isEmpty()) {
            return null;
        }

        BackendlessCity first = list.get(0);

        return new City(first.getObjectId(), first.getName());
    }

    public Single<EstablishmentPromotion> getEstablishmentPromotion(final String objectId) {
        return checkConnectivity(locationSingle())
                .map(location -> {
                    try {
                        BackendlessEstablishmentPromotion object = BackendlessEstablishmentPromotion.findById(objectId, 2);

                        if (object == null) {
                            throw new EntityNotFoundException();
                        }

                        return ModelMapper.buildEstablishmentPromotion(object, location);
                    } catch (BackendlessException e) {
                        throw new EntityNotFoundException();
                    }
                });
    }

    private Single<Location> locationSingle() {
        Location cachedLocation = locationCache.get();

        Single<Location> locationSingle;

        if (cachedLocation != null) {
            locationSingle = Single.just(cachedLocation);
        } else {
            locationSingle = GetLastKnownLocationOnSubscribe.create(app, false)
                    .doOnNext(location -> {
                        if (location != null) {
                            locationCache.setLocation(location);
                        }
                    })
                    .toSingle();
        }

        return locationSingle;
    }

    public City getStoredCity() {
        String cityName = preferencesManager.getCityName();
        String cityId = preferencesManager.getCityId();

        if (cityId == null) {
            return null;
        } else {
            return new City(cityId, cityName);
        }
    }

    public Single<CitySearchResponse> searchCity(String query, CitySearchResponse response) {
        return checkConnectivity(Single.create(new CitySearchOnSubscribe(query, response)));
    }

    public Single<ArrayList<EstablishmentCategory>> getEstablishmentCategories() {
        return checkConnectivity(Single.create(new GetEstablishmentCategoriesOnSubscribe()));
    }

    public Single<Boolean> validateEmailAndVoucher(String email, String voucher) {
        Single<Boolean> single = checkUserExistenceByFields(Pair.create("email", email))
                .map(result -> !result);

        if (voucher != null) {
            single = single
                    .flatMap(result -> {
                        if (result) {
                            return getVoucher(voucher, email)
                                    .map(object -> object != null);
                        }

                        return Single.just(false);
                    });
        }

        return checkConnectivity(single);
    }

    public Single<Boolean> checkCpfExistence(String cpf) {
        return checkConnectivity(checkUserExistenceByFields(Pair.create("cpf", cpf)));
    }

    private Single<Boolean> checkUserExistenceByFields(Pair<String, String>... pairs) {
        return Single.create(singleSubscriber -> {
            BackendlessUser user = getUserByFields(pairs);

            if (!singleSubscriber.isUnsubscribed()) {
                singleSubscriber.onSuccess(user != null);
            }
        });
    }

    public Single<User> login(String cpf, String email) {
        Single<User> single = Single.create(singleSubscriber -> {
            try {
                BackendlessUser loggedUser = Backendless.UserService.login(email, cpf, true);

                if (singleSubscriber.isUnsubscribed()) {
                    return;
                }

                if (loggedUser == null) {
                    singleSubscriber.onError(UserNotAuthenticatedException.INSTANCE);
                }

                User user = ModelMapper.from(loggedUser);

                preferencesManager.storeUserInfo(loggedUser.getUserId(), loggedUser.getEmail());

                if (!singleSubscriber.isUnsubscribed()) {
                    singleSubscriber.onSuccess(user);
                }
            } catch (BackendlessException e) {
                if (!singleSubscriber.isUnsubscribed()) {
                    if ("3003".equals(e.getCode())) {
                        singleSubscriber.onError(new ActionException(R.string.invalid_email_or_cpf));
                    } else if ("3087".equals(e.getCode())) {
                        singleSubscriber.onError(new ActionException(R.string.must_confirm_email));
                    } else {
                        singleSubscriber.onError(e);
                    }
                }
            }
        });

        return checkConnectivity(single);
    }

    public void storeCity(City city) {
        preferencesManager.setCityInfo(city.objectId, city.name);
    }

    public boolean isLoggedIn() {
        return preferencesManager.isLoggedIn();
    }

    public Single<Boolean> addTokenAndPlan(User user, Token token, String plan) {
        Single<Boolean> single = Single.create(singleSubscriber -> {
            BackendlessUser backendlessUser = Backendless.UserService.findById(user.objectId);

            if (token != null) {
                backendlessUser.setProperty("token", token.getId());
                backendlessUser.setProperty("cardLast4", token.getCard().getLast4());
                backendlessUser.setProperty("cardOwnerName", token.getCard().getName());
            }

            backendlessUser.setProperty("plan", plan);

            backendlessUser = Backendless.UserService.update(backendlessUser);

            try {
                if (!singleSubscriber.isUnsubscribed()) {
                    singleSubscriber.onSuccess(true);
                }
            } catch (BackendlessException e) {
                if (!singleSubscriber.isUnsubscribed()) {
                    singleSubscriber.onError(e);
                }
            }
        });

        return checkConnectivity(single);
    }

    public Single<User> createUser(UserInfo userInfo, Token token) {
        Single<User> single = Single.create(singleSubscriber -> {
            try {
                Map<String, Object> eventArgs = new HashMap<>();

                eventArgs.put("EMAIL", userInfo.email);
                eventArgs.put("CPF", userInfo.cpf);
                eventArgs.put("NAME", userInfo.name);
                eventArgs.put("SURNAME", userInfo.surname);
                eventArgs.put("TELEPHONE", userInfo.telephone);
                eventArgs.put("BIRTHDATE", userInfo.birthdate);

                eventArgs.put("ADDRESS", userInfo.address);
                eventArgs.put("COMPLEMENT", userInfo.complement);
                eventArgs.put("ZIPCODE", userInfo.zipcode);
                eventArgs.put("STATE", userInfo.state);
                eventArgs.put("CITY", userInfo.city);

                if (token != null) {
                    eventArgs.put("STRIPETOKEN", token.getId());
                    eventArgs.put("CARDLAST4", token.getCard().getLast4());
                    eventArgs.put("CARDOWNERNAME", token.getCard().getName());
                    eventArgs.put("PLAN", userInfo.plan);
                }

                if (userInfo.voucher != null) {
                    eventArgs.put("VOUCHER_NAME", userInfo.voucher);
                }

                Map userMap = Backendless.Events.dispatch("CreateUser", eventArgs);

                BackendlessUser user = new BackendlessUser();

                user.setProperties(userMap);

                if (!singleSubscriber.isUnsubscribed()) {
                    singleSubscriber.onSuccess(ModelMapper.from(user));
                }
            } catch (BackendlessException e) {
                if (!singleSubscriber.isUnsubscribed()) {
                    singleSubscriber.onError(e);
                }
            }
        });

        return checkConnectivity(single);
    }

    private BackendlessUser getUserByFields(Pair<String, String>... pairs) {
        BackendlessDataQuery query = new BackendlessDataQuery();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < pairs.length; ++i) {
            Pair pair = pairs[i];

            sb.append(String.format("%s = '%s'", pair.first, pair.second));

            if ((i + 1) < pairs.length) {
                sb.append(" AND ");
            }
        }

        query.setWhereClause(sb.toString());

        BackendlessCollection<BackendlessUser> result = Backendless.Data.of(BackendlessUser.class).find(query);

        return result == null || result.getTotalObjects() == 0 ? null : result.getData().get(0);
    }

    public Single<Boolean> subscribeUserToPlan(User user, Token token) {
        Single<Boolean> single = Single.create(singleSubscriber -> {
            HashMap<String, Object> arguments = new HashMap<>();

            arguments.put("USER_ID", user.objectId);

            try {
                Backendless.Events.dispatch("SubscribeUser", arguments);
            } catch (BackendlessException e) {
                if (CARD_DECLINED_EXCEPTION.equals(e.getMessage())) {
                    if (!singleSubscriber.isUnsubscribed()) {
                        singleSubscriber.onError(new ActionException(R.string.card_declined));
                    }

                    return;
                } else if (CARD_PROCESSING_EXCEPTION.equals(e.getMessage())) {
                    if (!singleSubscriber.isUnsubscribed()) {
                        singleSubscriber.onError(new ActionException(R.string.card_processing_failed));
                    }

                    return;
                }
            }

            if (!singleSubscriber.isUnsubscribed()) {
                singleSubscriber.onSuccess(true);
            }
        });

        return checkConnectivity(single);
    }

    public Single<String> validateVoucher(String voucher, String email) {
        return checkConnectivity(getVoucher(voucher, email))
                .map(BackendlessVoucher::getName);
    }

    private Single<BackendlessVoucher> getVoucher(String name, String email) {
        return Single.create(singleSubscriber -> {
            BackendlessDataQuery query = new BackendlessDataQuery();

            if (email != null) {
                query.setWhereClause(String.format(Locale.getDefault(), "name = '%s' AND email = '%s' AND used = false", name, email));
            } else {
                query.setWhereClause(String.format(Locale.getDefault(), "name = '%s' AND used = false", name));
            }

            BackendlessCollection<BackendlessVoucher> result = Backendless.Data.of(BackendlessVoucher.class).find(query);

            if (!singleSubscriber.isUnsubscribed()) {
                if (result.getTotalObjects() != 0) {
                    singleSubscriber.onSuccess(result.getData().get(0));
                } else {
                    singleSubscriber.onError(new ActionException(R.string.invalid_voucher));
                }
            }
        });
    }

    public Single<Boolean> resendConfirmationEmail(String email, String cpf) {
        Single<Boolean> single = Single.create(singleSubscriber -> {

            HashMap<String, Object> arguments = new HashMap<>();

            arguments.put("USER_EMAIL", email);
            arguments.put("USER_CPF", cpf);

            Backendless.Events.dispatch("ResendConfirmation", arguments, null);

            if (!singleSubscriber.isUnsubscribed()) {
                singleSubscriber.onSuccess(true);
            }
        });

        return checkConnectivity(single);
    }

    public Single<User> getCurrentUser() {
        if (!preferencesManager.isLoggedIn()) {
            return Single.error(UserNotAuthenticatedException.INSTANCE);
        }

        Single<User> single = Single.create(singleSubscriber -> {
            BackendlessDataQuery query = new BackendlessDataQuery();
            QueryOptions options = new QueryOptions();

            options.setRelationsDepth(1);

            options.addRelated("voucher");

            query.setQueryOptions(options);
            query.setWhereClause(String.format("objectId = '%s'", preferencesManager.getUserId()));

            BackendlessCollection<BackendlessUser> user = Backendless.Data.of(BackendlessUser.class).find(query);

            if (user == null || user.getTotalObjects() == 0) {
                preferencesManager.clearsUserInfo();

                if (!singleSubscriber.isUnsubscribed()) {
                    singleSubscriber.onError(new ActionException(R.string.user_not_found));
                }
            } else {
                singleSubscriber.onSuccess(ModelMapper.from(user.getCurrentPage().get(0)));
            }
        });

        return checkConnectivity(single);
    }
}