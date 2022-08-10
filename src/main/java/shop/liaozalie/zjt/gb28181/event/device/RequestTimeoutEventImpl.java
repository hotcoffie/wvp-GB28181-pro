package shop.liaozalie.zjt.gb28181.event.device;

import shop.liaozalie.zjt.gb28181.bean.Device;
import shop.liaozalie.zjt.service.IDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.sip.ClientTransaction;
import javax.sip.address.SipURI;
import javax.sip.message.Request;

/**
 * @author lin
 */
@Component
public class RequestTimeoutEventImpl implements ApplicationListener<RequestTimeoutEvent> {

    @Autowired
    private IDeviceService deviceService;

    @Override
    public void onApplicationEvent(RequestTimeoutEvent event) {
        ClientTransaction clientTransaction = event.getTimeoutEvent().getClientTransaction();
        if (clientTransaction != null) {
            Request request = clientTransaction.getRequest();
            if (request != null) {
                String host = ((SipURI) request.getRequestURI()).getHost();
                int port = ((SipURI) request.getRequestURI()).getPort();
                Device device = deviceService.getDeviceByHostAndPort(host, port);
                if (device == null) {
                    return;
                }
                deviceService.offline(device.getDeviceId());
            }
        }
    }
}
