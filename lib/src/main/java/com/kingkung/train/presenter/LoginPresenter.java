package com.kingkung.train.presenter;

import com.kingkung.train.api.TrainApi;
import com.kingkung.train.bean.response.DataObserver;
import com.kingkung.train.bean.response.Result;
import com.kingkung.train.bean.response.ResultObserver;
import com.kingkung.train.contract.LoginContract;
import com.kingkung.train.presenter.base.BasePresenter;
import com.kingkung.train.utils.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import okio.BufferedSource;

public class LoginPresenter extends BasePresenter<LoginContract.View> implements LoginContract.Presenter {

    private TrainApi api;

    public LoginPresenter(TrainApi api) {
        this.api = api;
    }

    @Override
    public void captcha() {
        Map<String, String> fields = new HashMap<>();
        fields.put("login_site", "E");
        fields.put("module", "login");
        fields.put("rand", "sjrand");
        Disposable disposable = api.captcha(fields)
                .map(body -> {
                    BufferedSource source = body.source();
                    FileOutputStream fos = new FileOutputStream(new File(FileUtil.trainDataDir, "login.png"));
                    byte[] buf = new byte[1024 * 8];
                    int len;
                    while ((len = source.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                    fos.close();
                    source.close();
                    return true;
                })
                .subscribeWith(new DataObserver<Boolean>(mView) {
                    @Override
                    public void success(Boolean b) {
                        mView.captchaSuccess();
                    }
                });
        addSubscription(disposable);
    }

    @Override
    public void captchaCheck(List<Integer> codes) {
        Map<String, String> fields = new HashMap<>();
        fields.put("login_site", "E");
        fields.put("rand", "sjrand");
        String codeStr = codes.toString();
        fields.put("answer", codeStr.substring(1, codeStr.length() - 1));
        Disposable disposable = api.captchaCheck(fields)
                .subscribeWith(new ResultObserver<Result>(mView) {
                    @Override
                    public void succeed(Result result) {
                        int code = Integer.valueOf(result.getResult_code());
                        if (code == 4) {
                            mView.captchaCheckSuccess();
                        } else if (code == 5 || code == 7 || code == 8) { //5.验证码错误；7.验证码过期；8.验证码为空
                            mView.captchaCheckFaild();
                        }
                    }
                });
        addSubscription(disposable);
    }

    @Override
    public void login(String userName, String password) {
        Map<String, String> fields = new HashMap<>();
        fields.put("username", userName);
        fields.put("password", password);
        fields.put("appid", "otn");
        Disposable disposable = api.login(fields)
                .subscribeWith(new ResultObserver<Result>(mView) {
                    @Override
                    public void succeed(Result result) {
                        int code = Integer.parseInt(result.getResult_code());
                        if (code == 0) {
                            mView.loginSuccess();
                        }
                    }
                });
        addSubscription(disposable);
    }
}
