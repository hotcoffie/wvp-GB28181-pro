package com.genersoft.iot.vmp.gb28181.transmit.event.request.impl.message.notify.cmd;

import com.genersoft.iot.vmp.conf.SipConfig;
import com.genersoft.iot.vmp.conf.UserSetting;
import com.genersoft.iot.vmp.gb28181.bean.*;
import com.genersoft.iot.vmp.gb28181.event.DeviceOffLineDetector;
import com.genersoft.iot.vmp.gb28181.event.EventPublisher;
import com.genersoft.iot.vmp.gb28181.transmit.event.request.SIPRequestProcessorParent;
import com.genersoft.iot.vmp.gb28181.transmit.event.request.impl.message.IMessageHandler;
import com.genersoft.iot.vmp.gb28181.transmit.event.request.impl.message.notify.NotifyMessageHandler;
import com.genersoft.iot.vmp.gb28181.utils.Coordtransform;
import com.genersoft.iot.vmp.gb28181.utils.NumericUtil;
import com.genersoft.iot.vmp.service.IDeviceAlarmService;
import com.genersoft.iot.vmp.storager.IVideoManagerStorage;
import com.genersoft.iot.vmp.utils.GpsUtil;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.sip.RequestEvent;

import static com.genersoft.iot.vmp.gb28181.utils.XmlUtil.*;

@Component
public class AlarmNotifyMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private Logger logger = LoggerFactory.getLogger(AlarmNotifyMessageHandler.class);
    private final String cmdType = "Alarm";

    @Autowired
    private NotifyMessageHandler notifyMessageHandler;

    @Autowired
    private EventPublisher publisher;

    @Autowired
    private UserSetting userSetting;

    @Autowired
    private SipConfig sipConfig;

    @Autowired
    private IVideoManagerStorage storager;

    @Autowired
    private IDeviceAlarmService deviceAlarmService;

    @Autowired
    private DeviceOffLineDetector offLineDetector;

    @Override
    public void afterPropertiesSet() throws Exception {
        notifyMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, Device device, Element rootElement) {
        if (!sipConfig.isAlarm()) {
            return;
        }
        Element deviceIdElement = rootElement.element("DeviceID");
        String channelId = deviceIdElement.getText().toString();
        DeviceAlarm deviceAlarm = new DeviceAlarm();
        deviceAlarm.setDeviceId(device.getDeviceId());
        deviceAlarm.setChannelId(channelId);
        deviceAlarm.setAlarmPriority(getText(rootElement, "AlarmPriority"));
        deviceAlarm.setAlarmMethod(getText(rootElement, "AlarmMethod"));
        deviceAlarm.setAlarmTime(getText(rootElement, "AlarmTime"));
        if (getText(rootElement, "AlarmDescription") == null) {
            deviceAlarm.setAlarmDescription("");
        } else {
            deviceAlarm.setAlarmDescription(getText(rootElement, "AlarmDescription"));
        }
        if (NumericUtil.isDouble(getText(rootElement, "Longitude"))) {
            deviceAlarm.setLongitude(Double.parseDouble(getText(rootElement, "Longitude")));
        } else {
            deviceAlarm.setLongitude(0.00);
        }
        if (NumericUtil.isDouble(getText(rootElement, "Latitude"))) {
            deviceAlarm.setLatitude(Double.parseDouble(getText(rootElement, "Latitude")));
        } else {
            deviceAlarm.setLatitude(0.00);
        }

        if (!StringUtils.isEmpty(deviceAlarm.getAlarmMethod())) {
            if ( deviceAlarm.getAlarmMethod().equals("4")) {
                MobilePosition mobilePosition = new MobilePosition();
                mobilePosition.setDeviceId(deviceAlarm.getDeviceId());
                mobilePosition.setTime(deviceAlarm.getAlarmTime());
                mobilePosition.setLongitude(deviceAlarm.getLongitude());
                mobilePosition.setLatitude(deviceAlarm.getLatitude());
                mobilePosition.setReportSource("GPS Alarm");
                // 默认来源坐标系为WGS-84处理
                Double[] gcj02Point = Coordtransform.WGS84ToGCJ02(mobilePosition.getLongitude(), mobilePosition.getLatitude());
                logger.info("GCJ02坐标：" + gcj02Point[0] + ", " + gcj02Point[1]);
                mobilePosition.setGeodeticSystem("GCJ-02");
                mobilePosition.setCnLng(gcj02Point[0] + "");
                mobilePosition.setCnLat(gcj02Point[1] + "");
                if (!userSetting.getSavePositionHistory()) {
                    storager.clearMobilePositionsByDeviceId(device.getDeviceId());
                }
                storager.insertMobilePosition(mobilePosition);
            }
        }
        if (!StringUtils.isEmpty(deviceAlarm.getDeviceId())) {
            if (deviceAlarm.getAlarmMethod().equals("5")) {
                deviceAlarm.setAlarmType(getText(rootElement.element("Info"), "AlarmType"));
            }
        }
        logger.debug("存储报警信息、报警分类");
        // 存储报警信息、报警分类
        deviceAlarmService.add(deviceAlarm);

        if (offLineDetector.isOnline(device.getDeviceId())) {
            publisher.deviceAlarmEventPublish(deviceAlarm);
        }
    }

    @Override
    public void handForPlatform(RequestEvent evt, ParentPlatform parentPlatform, Element element) {

    }
}
