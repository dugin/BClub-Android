package io.bclub.controller;

import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import io.bclub.BuildConfig;
import io.bclub.application.Application;
import io.bclub.controller.rx.StripeTokenCreatorOnSubscribe;
import rx.Single;

public class StripeController extends BaseController {
    public StripeController(Application app, PreferencesManager preferencesManager) {
        super(app, preferencesManager);
    }

    public Card getCard(String number, Integer expMonth, Integer expYear, String cvc, String name) {
        Card card = new Card(number, expMonth, expYear, cvc);

        card.setName(name);

        if (card.validateCard()) {
            return card;
        }

        return null;
    }

    public Single<Token> getToken(Card card) {
        return checkConnectivity(Single.create(new StripeTokenCreatorOnSubscribe(BuildConfig.STRIPE_KEY, card)));
    }
}
