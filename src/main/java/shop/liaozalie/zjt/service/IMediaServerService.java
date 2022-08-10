package shop.liaozalie.zjt.service;

import com.alibaba.fastjson.JSONObject;
import shop.liaozalie.zjt.media.zlm.ZLMServerConfig;
import shop.liaozalie.zjt.media.zlm.dto.MediaServerItem;
import shop.liaozalie.zjt.service.bean.SSRCInfo;
import shop.liaozalie.zjt.vmanager.bean.WVPResult;

import java.util.List;

/**
 * 媒体服务节点
 */
public interface IMediaServerService {

    List<MediaServerItem> getAll();

    List<MediaServerItem> getAllFromDatabase();

    List<MediaServerItem> getAllOnline();

    MediaServerItem getOne(String generalMediaServerId);

    void syncCatchFromDatabase();

    /**
     * 新的节点加入
     * @param zlmServerConfig
     * @return
     */
    void zlmServerOnline(ZLMServerConfig zlmServerConfig);

    /**
     * 节点离线
     * @param mediaServerId
     * @return
     */
    void zlmServerOffline(String mediaServerId);

    MediaServerItem getMediaServerForMinimumLoad();

    void setZLMConfig(MediaServerItem mediaServerItem, boolean restart);

    void updateVmServer(List<MediaServerItem>  mediaServerItemList);

    SSRCInfo openRTPServer(MediaServerItem mediaServerItem, String streamId, boolean ssrcCheck, boolean isPlayback);

    SSRCInfo openRTPServer(MediaServerItem mediaServerItem, String streamId, String ssrc, boolean ssrcCheck, boolean isPlayback);

    SSRCInfo openRTPServer(MediaServerItem mediaServerItem, String streamId, String ssrc, boolean ssrcCheck, boolean isPlayback, Integer port);

    void closeRTPServer(String deviceId, String channelId, String ssrc);

    void clearRTPServer(MediaServerItem mediaServerItem);

    void update(MediaServerItem mediaSerItem);

    void addCount(String mediaServerId);

    void removeCount(String mediaServerId);

    void releaseSsrc(String mediaServerItemId, String ssrc);

    void clearMediaServerForOnline();

    WVPResult<String> add(MediaServerItem mediaSerItem);

    int addToDatabase(MediaServerItem mediaSerItem);

    int updateToDatabase(MediaServerItem mediaSerItem);

    void resetOnlineServerItem(MediaServerItem serverItem);

    WVPResult<MediaServerItem> checkMediaServer(String ip, int port, String secret);

    boolean checkMediaRecordServer(String ip, int port);

    void delete(String id);

    void deleteDb(String id);

    MediaServerItem getDefaultMediaServer();

    void updateMediaServerKeepalive(String mediaServerId, JSONObject data);
}
