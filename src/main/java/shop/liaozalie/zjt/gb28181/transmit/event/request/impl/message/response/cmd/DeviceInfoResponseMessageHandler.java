package shop.liaozalie.zjt.gb28181.transmit.event.request.impl.message.response.cmd;

import shop.liaozalie.zjt.conf.SipConfig;
import shop.liaozalie.zjt.gb28181.bean.Device;
import shop.liaozalie.zjt.gb28181.bean.ParentPlatform;
import shop.liaozalie.zjt.gb28181.event.EventPublisher;
import shop.liaozalie.zjt.gb28181.transmit.callback.DeferredResultHolder;
import shop.liaozalie.zjt.gb28181.transmit.callback.RequestMessage;
import shop.liaozalie.zjt.gb28181.transmit.event.request.SIPRequestProcessorParent;
import shop.liaozalie.zjt.gb28181.transmit.event.request.impl.message.IMessageHandler;
import shop.liaozalie.zjt.gb28181.transmit.event.request.impl.message.response.ResponseMessageHandler;
import shop.liaozalie.zjt.service.IDeviceService;
import shop.liaozalie.zjt.storager.IRedisCatchStorage;
import shop.liaozalie.zjt.storager.IVideoManagerStorage;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import shop.liaozalie.zjt.gb28181.utils.XmlUtil;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.message.Response;
import java.text.ParseException;

/**
 * @author lin
 */
@Component
public class DeviceInfoResponseMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private Logger logger = LoggerFactory.getLogger(DeviceInfoResponseMessageHandler.class);
    private final String cmdType = "DeviceInfo";

    @Autowired
    private ResponseMessageHandler responseMessageHandler;

    @Autowired
    private IVideoManagerStorage storager;

    @Autowired
    private IRedisCatchStorage redisCatchStorage;

    @Autowired
    private DeferredResultHolder deferredResultHolder;

    @Autowired
    private SipConfig config;

    @Autowired
    private EventPublisher publisher;

    @Autowired
    private IDeviceService deviceService;

    @Override
    public void afterPropertiesSet() throws Exception {
        responseMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, Device device, Element rootElement) {
        logger.debug("接收到DeviceInfo应答消息");
        // 检查设备是否存在， 不存在则不回复
        if (device == null || device.getOnline() == 0) {
            logger.warn("[接收到DeviceInfo应答消息,但是设备已经离线]：" + (device != null ? device.getDeviceId():"" ));
            return;
        }
        try {
            rootElement = getRootElement(evt, device.getCharset());
            Element deviceIdElement = rootElement.element("DeviceID");
            String channelId = deviceIdElement.getTextTrim();
            String key = DeferredResultHolder.CALLBACK_CMD_DEVICEINFO + device.getDeviceId() + channelId;
            device.setName(XmlUtil.getText(rootElement, "DeviceName"));

            device.setManufacturer(XmlUtil.getText(rootElement, "Manufacturer"));
            device.setModel(XmlUtil.getText(rootElement, "Model"));
            device.setFirmware(XmlUtil.getText(rootElement, "Firmware"));
            if (StringUtils.isEmpty(device.getStreamMode())) {
                device.setStreamMode("UDP");
            }
            deviceService.updateDevice(device);

            RequestMessage msg = new RequestMessage();
            msg.setKey(key);
            msg.setData(device);
            deferredResultHolder.invokeAllResult(msg);
            // 回复200 OK
            responseAck(evt, Response.OK);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (SipException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handForPlatform(RequestEvent evt, ParentPlatform parentPlatform, Element rootElement) {

    }
}
