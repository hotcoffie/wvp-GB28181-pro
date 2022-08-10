package shop.liaozalie.zjt.gb28181.transmit.event.request.impl.message.response.cmd;

import com.alibaba.fastjson.JSONObject;
import shop.liaozalie.zjt.gb28181.bean.Device;
import shop.liaozalie.zjt.gb28181.bean.ParentPlatform;
import shop.liaozalie.zjt.gb28181.transmit.callback.DeferredResultHolder;
import shop.liaozalie.zjt.gb28181.transmit.callback.RequestMessage;
import shop.liaozalie.zjt.gb28181.transmit.event.request.SIPRequestProcessorParent;
import shop.liaozalie.zjt.gb28181.transmit.event.request.impl.message.IMessageHandler;
import shop.liaozalie.zjt.gb28181.transmit.event.request.impl.message.response.ResponseMessageHandler;
import shop.liaozalie.zjt.gb28181.utils.XmlUtil;
import shop.liaozalie.zjt.service.IDeviceService;
import shop.liaozalie.zjt.storager.IRedisCatchStorage;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.Objects;

@Component
public class DeviceStatusResponseMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private Logger logger = LoggerFactory.getLogger(DeviceStatusResponseMessageHandler.class);
    private final String cmdType = "DeviceStatus";

    @Autowired
    private ResponseMessageHandler responseMessageHandler;

    @Autowired
    private DeferredResultHolder deferredResultHolder;

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private IRedisCatchStorage redisCatchStorage;

    @Override
    public void afterPropertiesSet() throws Exception {
        responseMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, Device device, Element element) {
        logger.info("接收到DeviceStatus应答消息");
        // 检查设备是否存在， 不存在则不回复
        if (device == null) {
            return;
        }
        // 回复200 OK
        try {
            responseAck(evt, Response.OK);
        } catch (SipException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Element deviceIdElement = element.element("DeviceID");
        Element onlineElement = element.element("Online");
        String channelId = deviceIdElement.getText();
        JSONObject json = new JSONObject();
        XmlUtil.node2Json(element, json);
        if (logger.isDebugEnabled()) {
            logger.debug(json.toJSONString());
        }
        String text = onlineElement.getText();
        if (Objects.equals(text.trim().toUpperCase(), "ONLINE")) {
            deviceService.online(device);
        }else {
            deviceService.offline(device.getDeviceId());
        }
        RequestMessage msg = new RequestMessage();
        msg.setKey(DeferredResultHolder.CALLBACK_CMD_DEVICESTATUS + device.getDeviceId());
        msg.setData(json);
        deferredResultHolder.invokeAllResult(msg);
    }

    @Override
    public void handForPlatform(RequestEvent evt, ParentPlatform parentPlatform, Element rootElement) {


    }
}
