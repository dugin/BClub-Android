package io.bclub.rx;

import android.support.annotation.NonNull;

import rx.Scheduler;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SingleSchedulerTransformer<T> implements Single.Transformer<T, T> {

    public static final SingleSchedulerTransformer IO_AND_MAIN_THREAD;
    public static final SingleSchedulerTransformer COMPUTATION_AND_MAIN_THREAD;

    Scheduler mSubscribeScheduler;
    Scheduler mObserverScheduler;

    static {
        IO_AND_MAIN_THREAD = new SingleSchedulerTransformer<>();
        COMPUTATION_AND_MAIN_THREAD = new SingleSchedulerTransformer<>();

        IO_AND_MAIN_THREAD.mObserverScheduler = AndroidSchedulers.mainThread();
        IO_AND_MAIN_THREAD.mSubscribeScheduler = Schedulers.io();

        COMPUTATION_AND_MAIN_THREAD.mObserverScheduler = AndroidSchedulers.mainThread();
        COMPUTATION_AND_MAIN_THREAD.mSubscribeScheduler = Schedulers.computation();
    }

    @Override
    public Single<T> call(Single<T> single) {
        return single
                .subscribeOn(mSubscribeScheduler)
                .observeOn(mObserverScheduler);
    }

    @NonNull
    public static <T> SingleSchedulerTransformer<T> ofIOToMainThread() {
        //noinspection unchecked
        return IO_AND_MAIN_THREAD;
    }

    @NonNull
    public static <T> SingleSchedulerTransformer<T> ofComputationToMainThread() {
        //noinspection unchecked
        return COMPUTATION_AND_MAIN_THREAD;
    }
}