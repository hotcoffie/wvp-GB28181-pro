package shop.liaozalie.zjt.conf;

import shop.liaozalie.zjt.gb28181.bean.ParentPlatform;
import shop.liaozalie.zjt.gb28181.bean.ParentPlatformCatch;
import shop.liaozalie.zjt.gb28181.event.EventPublisher;
import shop.liaozalie.zjt.gb28181.transmit.cmd.ISIPCommanderForPlatform;
import shop.liaozalie.zjt.storager.IRedisCatchStorage;
import shop.liaozalie.zjt.storager.IVideoManagerStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 系统启动时控制上级平台重新注册
 */
@Component
@Order(value=3)
public class SipPlatformRunner implements CommandLineRunner {

    @Autowired
    private IVideoManagerStorage storager;

    @Autowired
    private IRedisCatchStorage redisCatchStorage;

    @Autowired
    private EventPublisher publisher;

    @Autowired
    private ISIPCommanderForPlatform sipCommanderForPlatform;


    @Override
    public void run(String... args) throws Exception {
        // 设置所有平台离线
        storager.outlineForAllParentPlatform();

        // 清理所有平台注册缓存
        redisCatchStorage.cleanPlatformRegisterInfos();

        // 停止所有推流
//        zlmrtpServerFactory.closeAllSendRtpStream();

        List<ParentPlatform> parentPlatforms = storager.queryEnableParentPlatformList(true);

        for (ParentPlatform parentPlatform : parentPlatforms) {
            redisCatchStorage.updatePlatformRegister(parentPlatform);

            redisCatchStorage.updatePlatformKeepalive(parentPlatform);

            ParentPlatformCatch parentPlatformCatch = new ParentPlatformCatch();

            parentPlatformCatch.setParentPlatform(parentPlatform);
            parentPlatformCatch.setId(parentPlatform.getServerGBId());
            redisCatchStorage.updatePlatformCatchInfo(parentPlatformCatch);

            // 取消订阅
            sipCommanderForPlatform.unregister(parentPlatform, null, (eventResult)->{
                // 发送平台未注册消息
                publisher.platformNotRegisterEventPublish(parentPlatform.getServerGBId());
            });
        }
    }
}
