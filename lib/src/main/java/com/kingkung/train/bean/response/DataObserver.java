package com.kingkung.train.bean.response;

import com.kingkung.train.contract.base.BaseContract;

import io.reactivex.observers.DisposableObserver;

public abstract class DataObserver<D> extends DisposableObserver<D> {
    private BaseContract.View view;

    public DataObserver(BaseContract.View view) {
        this.view = view;
    }

    @Override
    public void onNext(D d) {
        success(d);
    }

    public abstract void success(D d);

    @Override
    public void onError(Throwable e) {
        view.showMsg(e.toString());
    }

    @Override
    public void onComplete() {

    }
}
