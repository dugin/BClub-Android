package io.bclub.controller.rx;

import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.util.TextUtils;
import com.stripe.net.RequestOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import rx.Single;
import rx.SingleSubscriber;

// From Stripe 1.0.4
public class StripeTokenCreatorOnSubscribe implements Single.OnSubscribe<Token> {

    private final String publishableKey;
    private Card card;

    public StripeTokenCreatorOnSubscribe(String publishableKey, Card card) {
        this.publishableKey = publishableKey;
        this.card = card;
    }

    @Override
    public void call(SingleSubscriber<? super Token> singleSubscriber) {
        try {
            Token token = getToken();

            if (!singleSubscriber.isUnsubscribed()) {
                singleSubscriber.onSuccess(token);
            }
        } catch (Exception e) {
            if (!singleSubscriber.isUnsubscribed()) {
                singleSubscriber.onError(e);
            }
        }
    }

    private Map<String, Object> hashMapFromCard(Card card) {
        Map<String, Object> tokenParams = new HashMap<>();
        Map<String, Object> cardParams = new HashMap<>();

        addIfNotNull(cardParams, "number", TextUtils.nullIfBlank(card.getNumber()));
        addIfNotNull(cardParams, "cvc", TextUtils.nullIfBlank(card.getCVC()));
        addIfNotNull(cardParams, "exp_month", card.getExpMonth());
        addIfNotNull(cardParams, "exp_year", card.getExpYear());
        addIfNotNull(cardParams, "name", TextUtils.nullIfBlank(card.getName()));
        addIfNotNull(cardParams, "currency", TextUtils.nullIfBlank(card.getCurrency()));
        addIfNotNull(cardParams, "address_line1", TextUtils.nullIfBlank(card.getAddressLine1()));
        addIfNotNull(cardParams, "address_line2", TextUtils.nullIfBlank(card.getAddressLine2()));
        addIfNotNull(cardParams, "address_city", TextUtils.nullIfBlank(card.getAddressCity()));
        addIfNotNull(cardParams, "address_zip", TextUtils.nullIfBlank(card.getAddressZip()));
        addIfNotNull(cardParams, "address_state", TextUtils.nullIfBlank(card.getAddressState()));
        addIfNotNull(cardParams, "address_country", TextUtils.nullIfBlank(card.getAddressCountry()));

        tokenParams.put("card", cardParams);

        return tokenParams;
    }

    void addIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private Token getToken() throws Exception {
        RequestOptions requestOptions = RequestOptions.builder().setApiKey(publishableKey).build();

        com.stripe.model.Token stripeToken;

        stripeToken = com.stripe.model.Token.create(hashMapFromCard(card), requestOptions);

        com.stripe.model.Card stripeCard = stripeToken.getCard();

        card = androidCardFromStripeCard(stripeCard);

        return androidTokenFromStripeToken(card, stripeToken);
    }

    private Card androidCardFromStripeCard(com.stripe.model.Card stripeCard) {
        return new Card(null, stripeCard.getExpMonth(), stripeCard.getExpYear(), null, stripeCard.getName(), stripeCard.getAddressLine1(), stripeCard.getAddressLine2(), stripeCard.getAddressCity(), stripeCard.getAddressState(), stripeCard.getAddressZip(), stripeCard.getAddressCountry(), stripeCard.getLast4(), stripeCard.getType(), stripeCard.getFingerprint(), stripeCard.getCountry());
    }

    private Token androidTokenFromStripeToken(Card androidCard, com.stripe.model.Token stripeToken) {
        return new Token(stripeToken.getId(), stripeToken.getLivemode(), new Date(stripeToken.getCreated() * 1000), stripeToken.getUsed(), androidCard);
    }
}
