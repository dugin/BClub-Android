package io.bclub.rx;

import android.support.annotation.NonNull;

import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ObservableSchedulerTransformer<T> implements Observable.Transformer<T, T> {

    public static final ObservableSchedulerTransformer IO_AND_MAIN_THREAD;
    public static final ObservableSchedulerTransformer COMPUTATION_AND_MAIN_THREAD;

    Scheduler mSubscribeScheduler;
    Scheduler mObserverScheduler;

    static {
        IO_AND_MAIN_THREAD = new ObservableSchedulerTransformer<>();
        COMPUTATION_AND_MAIN_THREAD = new ObservableSchedulerTransformer<>();

        IO_AND_MAIN_THREAD.mObserverScheduler = AndroidSchedulers.mainThread();
        IO_AND_MAIN_THREAD.mSubscribeScheduler = Schedulers.io();

        COMPUTATION_AND_MAIN_THREAD.mObserverScheduler = AndroidSchedulers.mainThread();
        COMPUTATION_AND_MAIN_THREAD.mSubscribeScheduler = Schedulers.computation();
    }

    @Override
    public Observable<T> call(Observable<T> single) {
        return single
                .subscribeOn(mSubscribeScheduler)
                .observeOn(mObserverScheduler);
    }

    @NonNull
    public static <T> ObservableSchedulerTransformer<T> ofIOToMainThread() {
        //noinspection unchecked
        return IO_AND_MAIN_THREAD;
    }

    @NonNull
    public static <T> ObservableSchedulerTransformer<T> ofComputationToMainThread() {
        //noinspection unchecked
        return COMPUTATION_AND_MAIN_THREAD;
    }
}