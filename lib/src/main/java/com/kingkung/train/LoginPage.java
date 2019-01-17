package com.kingkung.train;


import com.kingkung.train.api.TrainApiService;
import com.kingkung.train.contract.LoginContract;
import com.kingkung.train.presenter.LoginPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 * A login screen that offers login via email/password.
 */
public class LoginPage extends BasePage<LoginPresenter> implements LoginContract.View {


    public final static int SCALE = 3;
    public List<Integer> codes = new ArrayList<>();

    int[][] coordinates = new int[][]{{40, 40}, {110, 40}, {180, 40}, {250, 40}, {40, 110}, {110, 110}, {180, 110}, {250, 110}};

    private String userName = "13857715526";
    private String password = "chenghang123";

    @Override
    protected void inject() {
        presenter = new LoginPresenter(TrainApiService.getTrainApi());
    }

    @Override
    protected void create() {
        presenter.captcha();
    }

    @Override
    public void captchaSuccess() {
        System.out.println("请输入验证码");
        codes.clear();
        Scanner scan = new Scanner(System.in);
        String content = scan.next();
        String[] codeIndexs = content.split(",");
        for (int i = 0; i < codeIndexs.length; i++) {
            int[] coordinate = coordinates[Integer.parseInt(codeIndexs[i])];
            codes.add(coordinate[0]);
            codes.add(coordinate[1]);
        }
        presenter.captchaCheck(codes);
    }

    @Override
    public void captchaCheckSuccess() {
        System.out.println("验证码通过");
        presenter.login(userName, password);
    }

    @Override
    public void captchaCheckFaild() {
        System.out.println("验证码失败");
        presenter.captcha();
    }

    @Override
    public void loginSuccess() {
        TrainPage trainPage = new TrainPage();
        trainPage.onCreate();
        onDestroy();
    }
}

