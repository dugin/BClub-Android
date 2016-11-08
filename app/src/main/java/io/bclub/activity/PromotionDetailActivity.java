package io.bclub.activity;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.samwolfand.aspectlockedimagview.AspectLockedImageView;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import io.bclub.BuildConfig;
import io.bclub.R;
import io.bclub.controller.ApiController;
import io.bclub.exception.EntityNotFoundException;
import io.bclub.model.EstablishmentPromotion;
import io.bclub.model.Promotion;
import io.bclub.rx.SingleSchedulerTransformer;
import io.bclub.util.Constants;
import io.bclub.util.DisplayHelper;
import io.bclub.util.Mask;
import io.bclub.util.TelephoneClickableSpan;
import io.bclub.view.PromotionEntryView;
import rx.Subscription;
import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

public class PromotionDetailActivity extends BaseActivity {

    private static final String MAPS_PACKAGE = "com.google.android.apps.maps";

    public static final String PROMOTION_EXTRA = "PROMOTION";

    @Inject
    ApiController apiController;

    @BindView(R.id.iv_promotion_banner)
    AspectLockedImageView ivPromotionBanner;

    @BindView(R.id.tv_establishment_title)
    TextView tvTitle;

    @BindView(R.id.tv_establishment_category)
    TextView tvCategory;

    @BindView(R.id.tv_establishment_neighborhood_distance)
    TextView tvDistance;

    @BindView(R.id.tv_establishment_description)
    TextView tvDescription;

    @BindView(R.id.tv_establishment_restrictions)
    TextView tvRestrictions;

    @BindView(R.id.tv_establishment_address_line)
    TextView tvAddressLine;

    @BindView(R.id.tv_establishment_telephone_line)
    TextView tvTelephoneLine;

    @BindView(R.id.entries_container)
    ViewGroup entriesContainer;

    SupportMapFragment map;

    EstablishmentPromotion establishmentPromotion;

    Typeface boldTypeface;

    String shareText;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(PROMOTION_EXTRA, establishmentPromotion);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getIntent().hasExtra(PROMOTION_EXTRA)) {
            finish();
            return;
        }

        establishmentPromotion = getIntent().getParcelableExtra(PROMOTION_EXTRA);

        activityComponent.inject(this);

        setContentView(R.layout.activity_promotion_detail);
        showAppBarArrow();
        setupToolbarAlpha(establishmentPromotion.establishment.name);

        map = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag("MAP_FRAGMENT");

        boldTypeface = TypefaceUtils.load(getAssets(), "fonts/OpenSans-Bold.ttf");

        restoreState(savedInstanceState);

        setupImageSize();
        setupData();
        setupMap();

        if (savedInstanceState == null) {
            fetchUpdatedPromotion();
        } else {
            setupPromotionEntries();
        }
    }

    void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }

        establishmentPromotion = savedInstanceState.getParcelable(PROMOTION_EXTRA);
    }

    void setupMap() {
        if (map == null) {
            return;
        }

        if (!checkPlayServices() || establishmentPromotion.establishment.location == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(map)
                    .commit();
            return;
        } else {
            if (map.isHidden()) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .show(map)
                        .commit();
            }
        }

        map.getMapAsync(googleMap -> {
            LatLng latLng = new LatLng(establishmentPromotion.establishment.location.getLatitude(), establishmentPromotion.establishment.location.getLongitude());

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(((BitmapDrawable) ContextCompat.getDrawable(app, R.drawable.ic_pin_shadow)).getBitmap());

            MarkerOptions options = new MarkerOptions();

            Projection projection = googleMap.getProjection();
            Point point = projection.toScreenLocation(latLng);

            point.offset(0, DisplayHelper.dpToPixels(app, 13));

            options = options
                    .icon(bitmapDescriptor)
                    .flat(true)
                    .position(projection.fromScreenLocation(point));

            googleMap.addMarker(options);
            googleMap.getUiSettings().setMapToolbarEnabled(false);

            googleMap.setOnMapClickListener(latLng1 -> openGoogleMaps());

            googleMap.setOnMarkerClickListener(marker -> {
                openGoogleMaps();
                return false;
            });
        });
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, Constants.RC_GPS_ERROR_RESOLUTION)
                        .show();
            }

            return false;
        }

        return true;
    }

    void openGoogleMaps() {
        double latitude = establishmentPromotion.establishment.location.getLatitude();
        double longitude = establishmentPromotion.establishment.location.getLongitude();

        Uri gmmIntentUri = Uri.parse(String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", latitude, longitude, latitude, longitude, establishmentPromotion.establishment.name));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        mapIntent.setPackage(MAPS_PACKAGE);

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    void setupImageSize() {
        TypedValue outValue = new TypedValue();

        getResources().getValue(R.dimen.image_ar, outValue, true);

        float value = outValue.getFloat();

        ivPromotionBanner.setAspectRatio(value);
    }

    void setupData() {
        if (establishmentPromotion.imageUrl != null) {
            Glide.with(this)
                    .load(establishmentPromotion.imageUrl)
                    .bitmapTransform(new CenterCrop(this), new FitCenter(this))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivPromotionBanner);
        } else {
            ivPromotionBanner.setVisibility(View.GONE);
        }

        String distance = establishmentPromotion.establishment.neighborhood;

        if (establishmentPromotion.hasDistanceCalculated()) {
            distance = distance.concat(" - ").concat(establishmentPromotion.establishment.getFormattedDistance());
        }

        tvDistance.setText(distance);
        tvCategory.setText(establishmentPromotion.establishment.categoryName);
        tvAddressLine.setText(establishmentPromotion.establishment.addressLine);
        tvTitle.setText(establishmentPromotion.establishment.name);
        tvDescription.setText(establishmentPromotion.establishment.description);
        tvRestrictions.setText(establishmentPromotion.restriction);

        if (!establishmentPromotion.active) {
            snack(R.string.promotion_not_available, Snackbar.LENGTH_INDEFINITE);
        }

        setupTelephone();

        shareText = null;
    }

    void setupTelephone() {
        SpannableStringBuilder sb = new SpannableStringBuilder();

        for (int i = 0, size = establishmentPromotion.establishment.telephones.size(); i < size; ++i) {
            String telephone = Mask.unmask(establishmentPromotion.establishment.telephones.get(i));

            telephone = Mask.insertPhoneMask(telephone);

            SpannableString spannableString = new SpannableString(telephone);

            spannableString.setSpan(new TelephoneClickableSpan(telephone), 0, telephone.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            sb.append(spannableString);

            if ((i + 1) < size) {
                sb.append(" | ");
            }
        }

        tvTelephoneLine.setMovementMethod(LinkMovementMethod.getInstance());
        tvTelephoneLine.setText(sb);
    }

    void fetchUpdatedPromotion() {
        Subscription subscription = apiController.getEstablishmentPromotion(establishmentPromotion.objectId)
                .compose(SingleSchedulerTransformer.ofIOToMainThread())
                .subscribe(establishmentPromotion -> {
                    PromotionDetailActivity.this.establishmentPromotion = PromotionDetailActivity.this.establishmentPromotion.copyPromotionsAndOwnProperties(establishmentPromotion);

                    setupPromotionEntries();
                    setupData();
                }, error -> {
                    if (error instanceof EntityNotFoundException) {
                        snack(R.string.promotion_not_available, Snackbar.LENGTH_INDEFINITE);
                    } else {
                        snack(error);
                    }

                    removeContainerProgress();
                });

        addSubscription(subscription);
    }

    void setupPromotionEntries() {
        LayoutInflater layoutInflater = getLayoutInflater();
        List<Promotion> promotions = establishmentPromotion.promotions;

        removeContainerProgress();

        for (int i = 0, size = promotions.size(); i < size; ++i) {
            PromotionEntryView entryView = (PromotionEntryView) layoutInflater.inflate(R.layout.list_item_promotion_entry, entriesContainer, false);

            entryView.bind(promotions.get(i));

            entriesContainer.addView(entryView);
        }
    }

    void removeContainerProgress() {
        if (entriesContainer.getChildCount() > 0 && entriesContainer.getChildAt(0) instanceof ProgressBar) {
            entriesContainer.removeViewAt(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.promotion_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            share();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void share() {
        ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(this);
        String text = buildShareText();

        builder.setChooserTitle(R.string.share)
                .setText(text)
                .setType("text/plain");

        startActivity(builder.createChooserIntent());
    }

    String buildShareText() {
        if (shareText == null) {
            StringBuilder sb = new StringBuilder(100);

            Promotion first = establishmentPromotion.getFirst();
            Promotion last = null;

            if (!establishmentPromotion.isSinglePromotion()) {
                last = establishmentPromotion.getLast();
            }

            sb
                    .append(getString(R.string.app_name))
                    .append("\n\n")
                    .append(getString(R.string.promotions))
                    .append(" ")
                    .append(formatPercent(first, last))
                    .append(getString(R.string.at_location))
                    .append(establishmentPromotion.establishment.name)
                    .append(" - ")
                    .append(establishmentPromotion.establishment.neighborhood)
                    .append("\n\n")
                    .append(getString(R.string.more_info_on))
                    .append(" ")
                    .append(BuildConfig.WEBSITE);

            shareText = sb.toString();
        }

        return shareText;
    }

    CharSequence formatPercent(Promotion first, Promotion last){
            SpannableStringBuilder sb = new SpannableStringBuilder();

            if (last != null) {
                sb.append(getString(R.string.from));
            }

            if (first != null) {
                sb.append(formatPercent(first.percent, last != null));
            }

            if (last != null) {
                sb.append(getString(R.string.to));
                sb.append(formatPercent(last.percent, true));
            }

            if (sb.length() == 0) {
                return null;
            }

            return sb;
    }

    CharSequence formatPercent(double percent, boolean includePercent) {
        String format = includePercent ? "%d%%" : "%d";
        SpannableString spannableString = new SpannableString(String.format(Constants.PT_BR, format, (int) (Math.round(percent * 100))));

        CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(boldTypeface);

        spannableString.setSpan(typefaceSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.RC_GPS_ERROR_RESOLUTION) {
            setupMap();
        }
    }

    @Override
    protected String getScreenName() {
        return "Promotion Detail Screen";
    }
}
