package shop.liaozalie.zjt.gb28181.transmit.event.request.impl.message.response.cmd;

import shop.liaozalie.zjt.conf.UserSetting;
import shop.liaozalie.zjt.gb28181.bean.*;
import shop.liaozalie.zjt.gb28181.transmit.event.request.SIPRequestProcessorParent;
import shop.liaozalie.zjt.gb28181.transmit.event.request.impl.message.IMessageHandler;
import shop.liaozalie.zjt.gb28181.transmit.event.request.impl.message.response.ResponseMessageHandler;
import shop.liaozalie.zjt.gb28181.utils.NumericUtil;
import shop.liaozalie.zjt.service.IDeviceChannelService;
import shop.liaozalie.zjt.storager.IVideoManagerStorage;
import shop.liaozalie.zjt.utils.DateUtil;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import shop.liaozalie.zjt.gb28181.bean.Device;
import shop.liaozalie.zjt.gb28181.bean.DeviceChannel;
import shop.liaozalie.zjt.gb28181.bean.MobilePosition;
import shop.liaozalie.zjt.gb28181.bean.ParentPlatform;
import shop.liaozalie.zjt.gb28181.utils.XmlUtil;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.message.Response;
import java.text.ParseException;

@Component
public class MobilePositionResponseMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private Logger logger = LoggerFactory.getLogger(MobilePositionResponseMessageHandler.class);
    private final String cmdType = "MobilePosition";

    @Autowired
    private ResponseMessageHandler responseMessageHandler;

    @Autowired
    private UserSetting userSetting;

    @Autowired
    private IVideoManagerStorage storager;

    @Autowired
    private IDeviceChannelService deviceChannelService;

    @Override
    public void afterPropertiesSet() throws Exception {
        responseMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, Device device, Element rootElement) {

        try {
            rootElement = getRootElement(evt, device.getCharset());

            MobilePosition mobilePosition = new MobilePosition();
            mobilePosition.setCreateTime(DateUtil.getNow());
            if (!StringUtils.isEmpty(device.getName())) {
                mobilePosition.setDeviceName(device.getName());
            }
            mobilePosition.setDeviceId(device.getDeviceId());
            mobilePosition.setChannelId(XmlUtil.getText(rootElement, "DeviceID"));
            mobilePosition.setTime(XmlUtil.getText(rootElement, "Time"));
            mobilePosition.setLongitude(Double.parseDouble(XmlUtil.getText(rootElement, "Longitude")));
            mobilePosition.setLatitude(Double.parseDouble(XmlUtil.getText(rootElement, "Latitude")));
            if (NumericUtil.isDouble(XmlUtil.getText(rootElement, "Speed"))) {
                mobilePosition.setSpeed(Double.parseDouble(XmlUtil.getText(rootElement, "Speed")));
            } else {
                mobilePosition.setSpeed(0.0);
            }
            if (NumericUtil.isDouble(XmlUtil.getText(rootElement, "Direction"))) {
                mobilePosition.setDirection(Double.parseDouble(XmlUtil.getText(rootElement, "Direction")));
            } else {
                mobilePosition.setDirection(0.0);
            }
            if (NumericUtil.isDouble(XmlUtil.getText(rootElement, "Altitude"))) {
                mobilePosition.setAltitude(Double.parseDouble(XmlUtil.getText(rootElement, "Altitude")));
            } else {
                mobilePosition.setAltitude(0.0);
            }
            mobilePosition.setReportSource("Mobile Position");

            // 更新device channel 的经纬度
            DeviceChannel deviceChannel = new DeviceChannel();
            deviceChannel.setDeviceId(device.getDeviceId());
            deviceChannel.setChannelId(mobilePosition.getChannelId());
            deviceChannel.setLongitude(mobilePosition.getLongitude());
            deviceChannel.setLatitude(mobilePosition.getLatitude());
            deviceChannel.setGpsTime(mobilePosition.getTime());

            deviceChannel = deviceChannelService.updateGps(deviceChannel, device);

            mobilePosition.setLongitudeWgs84(deviceChannel.getLongitudeWgs84());
            mobilePosition.setLatitudeWgs84(deviceChannel.getLatitudeWgs84());
            mobilePosition.setLongitudeGcj02(deviceChannel.getLongitudeGcj02());
            mobilePosition.setLatitudeGcj02(deviceChannel.getLatitudeGcj02());

            if (userSetting.getSavePositionHistory()) {
                storager.insertMobilePosition(mobilePosition);
            }
            storager.updateChannelPosition(deviceChannel);
            //回复 200 OK
            responseAck(evt, Response.OK);
        } catch (DocumentException | SipException | InvalidArgumentException | ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handForPlatform(RequestEvent evt, ParentPlatform parentPlatform, Element element) {

    }
}
