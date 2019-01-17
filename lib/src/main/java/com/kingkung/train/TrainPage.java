package com.kingkung.train;

import com.kingkung.train.api.TrainApiService;
import com.kingkung.train.bean.PassengerInfo;
import com.kingkung.train.bean.TrainDetails;
import com.kingkung.train.contract.TrainContract;
import com.kingkung.train.presenter.TrainPresenter;
import com.kingkung.train.utils.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class TrainPage extends BasePage<TrainPresenter> implements TrainContract.View {
    private int count = 1;

    private int dateIndex = 0;

    private SimpleDateFormat leaveDateFormat = new SimpleDateFormat("HH:mm");
    public static SimpleDateFormat timerDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    //刷新间隔
    private int refreshQueryInterval = 750;
    //刷新登录会话
    private int refreshLoginInterval = 1000 * 60 * 5;
    //出发站
    private String fromStation = "杭州";
    //到达站
    private String toStation = "蕲春";
    //    private String toStation = "义乌";
    // 车次，如果为空就不过滤
    private List<String> trainNo = Arrays.asList("K", "G", "D", "Z");
    //    private List<String> trainNo = Arrays.asList("K");
    //乘车日期
    private List<String> trainDate = Arrays.asList("2019-01-30", "2019-01-31", "2019-02-01", "2019-02-02");
    //乘车人姓名
    private List<String> passengerNames = Arrays.asList("程航");
    //选择车次的起始时间
    private String leaveStartDate = "7:30";
    private long leaveStartTime;
    //选择车次的结束时间
    private String leaveEndDate = "17:00";
    private long leaveEndTime;
    //座位类别,如果为空就不过滤
    private List<SeatType> seatType = Arrays.asList(SeatType.HARD_SLEEP, SeatType.HARD_SEAT, SeatType.SECOND_CLASS);
    //定时抢票时间
    private String timerDate = "2019-01-15 10:20";
    //接受通知的邮箱
    private List<String> sendEmails = Arrays.asList("745440793@qq.com", "894474628@qq.com");

    private boolean isStartQuery = true;

    public enum SeatType {
        HARD_SLEEP("硬卧", "3"),
        HARD_SEAT("硬座", "1"),
        SECOND_CLASS("二等座", "O"),
        FIRST_CLASS("一等座", "M");

        public String name;

        public String seatType;

        SeatType(String name, String seatType) {
            this.name = name;
            this.seatType = seatType;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Override
    protected void inject() {
        presenter = new TrainPresenter(TrainApiService.getTrainApi());
    }

    @Override
    protected void create() {
        try {
            leaveStartTime = leaveDateFormat.parse(leaveStartDate).getTime();
            leaveEndTime = leaveDateFormat.parse(leaveEndDate).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        presenter.interval(0, refreshLoginInterval, () -> presenter.uamtk());
    }

    @Override
    public void uamtkSuccess(String newapptk) {
        presenter.uamauthClient(newapptk);
    }

    @Override
    public void uamtkFaild() {
        System.out.println("验证失败，请重新登录");
        LoginPage loginPage = new LoginPage();
        loginPage.onCreate();
        onDestroy();
    }

    @Override
    public void uamauthClientSuccess(String username) {
        System.out.println("验证成功，用户名：" + username);
        if (!isStartQuery) {
            return;
        }
        try {
            isStartQuery = false;
            long timer;
            long timerTime = timerDateFormat.parse(timerDate).getTime();
            long curTime = System.currentTimeMillis();
            if (timerTime <= curTime) {
                timer = 0;
            } else {
                timer = timerTime - curTime + 50;
            }
            presenter.interval(timer, refreshQueryInterval, () -> {
                if (dateIndex >= trainDate.size()) {
                    dateIndex = 0;
                }
                presenter.queryTrain(trainDate.get(dateIndex), fromStation, toStation);
                dateIndex++;
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void queryTrainSuccess(List<TrainDetails> details) {
        Iterator<TrainDetails> it = details.iterator();
        while (it.hasNext()) {
            TrainDetails trainDetail = it.next();
            if (filterSeatType(trainDetail)) {
                it.remove();
                continue;
            }
            if (filterTrainNo(trainDetail)) {
                it.remove();
                continue;
            }
            if (filterLeaveTime(trainDetail)) {
                it.remove();
                continue;
            }
        }
        showMsg("第" + count + "次");
        count++;
        List<TrainDetails> result = new ArrayList<>(details);
        if (result.size() == 0) {
            showMsg("没有查询到可买的票");
            showMsg("");
            return;
        }
        presenter.sendEmail(sendEmails, "这是标题1", "查询到可买的票");
        for (TrainDetails detail : result) {
            showMsg("车次:" + detail.trainNo + "\t\t可买票类型:" +
                    detail.seatTypes.toString() + "\t\t张数:" + detail.count);
            presenter.submitOrder(detail);
            break;
        }
        showMsg("");
    }

    @Override
    public void checkUserSuccess() {
    }

    @Override
    public void submitOrderSuccess(TrainDetails detail) {
        presenter.initDc(detail);
    }

    @Override
    public void initDcSuccess(TrainDetails detail) {
        presenter.getPassenger(detail);
    }

    @Override
    public void getPassengerSuccess(TrainDetails detail) {
        List<String> copyPassengerNames = new ArrayList<>(passengerNames);
        if (copyPassengerNames.size() == 0) {
            presenter.checkOrderInfo(detail);
            return;
        }
        List<PassengerInfo> passengerInfos = detail.passengerInfos;
        Iterator<PassengerInfo> it = passengerInfos.iterator();
        while (it.hasNext()) {
            PassengerInfo info = it.next();
            if (!copyPassengerNames.contains(info.passenger_name)) {
                it.remove();
            }
        }
        presenter.checkOrderInfo(detail);
    }

    @Override
    public void checkOrderInfoSuccess(TrainDetails detail) {
        presenter.getQueueCount(detail);
    }

    @Override
    public void getQueueCountSuccess(TrainDetails detail) {
        presenter.confirmSingleForQueue(detail);
    }

    @Override
    public void confirmSingleForQueueSuccess(TrainDetails detail) {
        presenter.sendEmail(sendEmails, "这是标题2", "订单排队中");
        presenter.queryOrderWaitTime(detail);
    }

    @Override
    public void queryOrderWaitTimeSuccess(TrainDetails detail) {
        presenter.resultOrderForQueue(detail);
    }

    @Override
    public void resultOrderForQueueSuccess() {
        presenter.sendEmail(sendEmails, "这是标题3", "订单提交成功");
        presenter.detachView();
    }

    public boolean filterTrainNo(TrainDetails detail) {
        if (trainNo.size() == 0) {
            return false;
        }
        List<String> copyTrainNo = new ArrayList<>(trainNo);
        int position = 0;
        for (; position < copyTrainNo.size(); position++) {
            String type = copyTrainNo.get(position);
            if (detail.trainNo.startsWith(type)) {
                break;
            }
        }
        if (position == copyTrainNo.size()) {
            return true;
        }
        return false;
    }

    public boolean filterLeaveTime(TrainDetails detail) {
        try {
            long leaveTime = leaveDateFormat.parse(detail.leaveTime).getTime();
            if (leaveTime < leaveStartTime || leaveTime > leaveEndTime) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean filterSeatType(TrainDetails detail) {
        if (seatType.size() == 0) {
            return false;
        }
        List<SeatType> copySeatType = new ArrayList<>(seatType);
        for (int i = 0; i < copySeatType.size(); i++) {
            SeatType type = copySeatType.get(i);
            if (type == SeatType.HARD_SLEEP && !TextUtils.isEmpty(detail.hardSleep)
                    && !"无".equals(detail.hardSleep) && !"*".equals(detail.hardSleep)) {
                detail.seatTypes.add(SeatType.HARD_SLEEP);
                detail.count = detail.hardSleep;
            }
            if (type == SeatType.HARD_SEAT && !TextUtils.isEmpty(detail.hardSeat)
                    && !"无".equals(detail.hardSeat) && !"*".equals(detail.hardSeat)) {
                detail.seatTypes.add(SeatType.HARD_SEAT);
                detail.count = detail.hardSeat;
            }
            if (type == SeatType.SECOND_CLASS && !TextUtils.isEmpty(detail.secondClassSeat)
                    && !"无".equals(detail.secondClassSeat) && !"*".equals(detail.secondClassSeat)) {
                detail.seatTypes.add(SeatType.SECOND_CLASS);
                detail.count = detail.secondClassSeat;
            }
            if (type == SeatType.FIRST_CLASS && !TextUtils.isEmpty(detail.firstClassSeat)
                    && !"无".equals(detail.firstClassSeat) && !"*".equals(detail.firstClassSeat)) {
                detail.seatTypes.add(SeatType.FIRST_CLASS);
                detail.count = detail.firstClassSeat;
            }
        }
        if (detail.seatTypes.size() == 0) {
            return true;
        }
        return false;
    }
}
