package io.bclub.dagger;

import dagger.Subcomponent;
import io.bclub.activity.BaseActivity;
import io.bclub.activity.BillingInformationActivity;
import io.bclub.activity.FilterActivity;
import io.bclub.activity.LoginRegistrationActivity;
import io.bclub.activity.MainActivity;
import io.bclub.activity.MenuActivity;
import io.bclub.activity.PromotionDetailActivity;
import io.bclub.activity.RegistrationActivity;
import io.bclub.activity.TutorialActivity;
import io.bclub.activity.UserDetailActivity;
import io.bclub.activity.UserInformationActivity;
import io.bclub.dagger.modules.ActivityModule;
import io.bclub.dialog.VoucherInputDialogFragment;
import io.bclub.widget.CityEditView;

@ActivityScope
@Subcomponent(modules = {ActivityModule.class})
public interface ActivityComponent {

    void inject(BaseActivity baseActivity);
    void inject(MainActivity mainActivity);
    void inject(PromotionDetailActivity promotionDetailActivity);
    void inject(FilterActivity filterActivity);
    void inject(TutorialActivity tutorialActivity);

    void inject(MenuActivity menuActivity);

    void inject(LoginRegistrationActivity loginRegistrationActivity);

    void inject(RegistrationActivity registrationActivity);
    void inject(UserInformationActivity userInformationActivity);
    void inject(BillingInformationActivity billingInformationActivity);

    void inject(UserDetailActivity userDetailActivity);

    void inject(CityEditView cityEditView);

    void inject(VoucherInputDialogFragment voucherInputDialogFragment);
}
