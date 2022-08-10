package shop.liaozalie.zjt.service;

import com.alibaba.fastjson.JSONObject;
import shop.liaozalie.zjt.common.StreamInfo;
import shop.liaozalie.zjt.gb28181.bean.Device;
import shop.liaozalie.zjt.gb28181.bean.InviteStreamCallback;
import shop.liaozalie.zjt.gb28181.bean.InviteStreamInfo;
import shop.liaozalie.zjt.gb28181.event.SipSubscribe;
import shop.liaozalie.zjt.media.zlm.ZLMHttpHookSubscribe;
import shop.liaozalie.zjt.media.zlm.dto.MediaServerItem;
import shop.liaozalie.zjt.service.bean.InviteTimeOutCallback;
import shop.liaozalie.zjt.service.bean.PlayBackCallback;
import shop.liaozalie.zjt.service.bean.SSRCInfo;
import shop.liaozalie.zjt.vmanager.gb28181.play.bean.PlayResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * 点播处理
 */
public interface IPlayService {

    void onPublishHandlerForPlay(MediaServerItem mediaServerItem, JSONObject resonse, String deviceId, String channelId, String uuid);

    void play(MediaServerItem mediaServerItem, SSRCInfo ssrcInfo, Device device, String channelId,
              ZLMHttpHookSubscribe.Event hookEvent, SipSubscribe.Event errorEvent,
              InviteTimeOutCallback timeoutCallback, String uuid);
    PlayResult play(MediaServerItem mediaServerItem, String deviceId, String channelId, ZLMHttpHookSubscribe.Event event, SipSubscribe.Event errorEvent, Runnable timeoutCallback);

    MediaServerItem getNewMediaServerItem(Device device);

    void onPublishHandlerForDownload(InviteStreamInfo inviteStreamInfo, String deviceId, String channelId, String toString);

    DeferredResult<ResponseEntity<String>> playBack(String deviceId, String channelId, String startTime, String endTime, InviteStreamCallback infoCallBack, PlayBackCallback hookCallBack);
    DeferredResult<ResponseEntity<String>> playBack(MediaServerItem mediaServerItem, SSRCInfo ssrcInfo,String deviceId, String channelId, String startTime, String endTime, InviteStreamCallback infoCallBack, PlayBackCallback hookCallBack);

    void zlmServerOffline(String mediaServerId);

    DeferredResult<ResponseEntity<String>> download(String deviceId, String channelId, String startTime, String endTime, int downloadSpeed, InviteStreamCallback infoCallBack, PlayBackCallback hookCallBack);
    DeferredResult<ResponseEntity<String>> download(MediaServerItem mediaServerItem, SSRCInfo ssrcInfo,String deviceId,  String channelId, String startTime, String endTime, int downloadSpeed, InviteStreamCallback infoCallBack, PlayBackCallback hookCallBack);

    StreamInfo getDownLoadInfo(String deviceId, String channelId, String stream);

    void zlmServerOnline(String mediaServerId);
}
