package shop.liaozalie.zjt.service.bean;

import shop.liaozalie.zjt.gb28181.bean.SendRtpItem;
import shop.liaozalie.zjt.media.zlm.dto.MediaServerItem;

/**
 * redis消息：下级回复推送信息
 * @author lin
 */
public class ResponseSendItemMsg {

    private SendRtpItem sendRtpItem;

    private MediaServerItem mediaServerItem;

    public SendRtpItem getSendRtpItem() {
        return sendRtpItem;
    }

    public void setSendRtpItem(SendRtpItem sendRtpItem) {
        this.sendRtpItem = sendRtpItem;
    }

    public MediaServerItem getMediaServerItem() {
        return mediaServerItem;
    }

    public void setMediaServerItem(MediaServerItem mediaServerItem) {
        this.mediaServerItem = mediaServerItem;
    }
}
