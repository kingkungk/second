package com.kingkung.train;

import com.kingkung.train.contract.base.BaseContract;

public abstract class BasePage<P extends BaseContract.Presenter> implements BaseContract.View {

    public P presenter;

    public void onCreate() {
        inject();
        attach();
        create();
    }

    public void onDestroy() {
        detach();
    }

    protected void attach() {
        presenter.attachView(this);
    }

    protected void detach() {
        presenter.detachView();
    }

    @Override
    public void showMsg(String msg) {
        System.out.println(msg);
    }

    @Override
    public void complete() {

    }

    @Override
    public void gotoSignIn() {

    }

    protected abstract void inject();

    protected abstract void create();
}
