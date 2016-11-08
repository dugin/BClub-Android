package io.bclub.bus;

import rx.subjects.PublishSubject;

public abstract class GenericPublishSubject  {

    public static final int CONNECTIVITY_CHANGE_TYPE = 1;
    public static final int PROMOTION_CLICKED_TYPE = 2;
    public static final int CITY_CLICKED_TYPE = 3;
    public static final int FILTER_PROMOTION_CLICKED_TYPE = 4;
    public static final int FILTER_CATEGORY_CLICKED_TYPE = 5;
    public static final int FILTER_DISCOUNT_CLICKED_TYPE = 6;
    public static final int FILTER_DAY_OF_WEEK_CLICKED_TYPE = 7;

    public static final PublishSubject<PublishItem> PUBLISH_SUBJECT = PublishSubject.create();

    private GenericPublishSubject() { }
}
