package shop.liaozalie.zjt.service.bean;

import shop.liaozalie.zjt.gb28181.transmit.callback.RequestMessage;

public interface PlayBackCallback {

    void call(PlayBackResult<RequestMessage> msg);

}
