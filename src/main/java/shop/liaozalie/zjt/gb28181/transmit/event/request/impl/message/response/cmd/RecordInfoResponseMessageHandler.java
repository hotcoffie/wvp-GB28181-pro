package shop.liaozalie.zjt.gb28181.transmit.event.request.impl.message.response.cmd;

import shop.liaozalie.zjt.gb28181.bean.*;
import shop.liaozalie.zjt.gb28181.event.EventPublisher;
import shop.liaozalie.zjt.gb28181.session.RecordDataCatch;
import shop.liaozalie.zjt.gb28181.transmit.callback.DeferredResultHolder;
import shop.liaozalie.zjt.gb28181.transmit.callback.RequestMessage;
import shop.liaozalie.zjt.gb28181.transmit.event.request.SIPRequestProcessorParent;
import shop.liaozalie.zjt.gb28181.transmit.event.request.impl.message.IMessageHandler;
import shop.liaozalie.zjt.gb28181.transmit.event.request.impl.message.response.ResponseMessageHandler;
import shop.liaozalie.zjt.utils.DateUtil;
import shop.liaozalie.zjt.vmanager.bean.WVPResult;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import shop.liaozalie.zjt.gb28181.bean.*;
import shop.liaozalie.zjt.gb28181.utils.XmlUtil;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author lin
 */
@Component
public class RecordInfoResponseMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private Logger logger = LoggerFactory.getLogger(RecordInfoResponseMessageHandler.class);
    private final String cmdType = "RecordInfo";

    private ConcurrentLinkedQueue<HandlerCatchData> taskQueue = new ConcurrentLinkedQueue<>();

    private boolean taskQueueHandlerRun = false;
    @Autowired
    private ResponseMessageHandler responseMessageHandler;

    @Autowired
    private RecordDataCatch recordDataCatch;

    @Autowired
    private DeferredResultHolder deferredResultHolder;

    @Autowired
    private EventPublisher eventPublisher;

    @Qualifier("taskExecutor")
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Override
    public void afterPropertiesSet() throws Exception {
        responseMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, Device device, Element rootElement) {

        // 回复200 OK
        try {
            responseAck(evt, Response.OK);
            taskQueue.offer(new HandlerCatchData(evt, device, rootElement));
            if (!taskQueueHandlerRun) {
                taskQueueHandlerRun = true;
                taskExecutor.execute(()->{
                    try {
                        while (!taskQueue.isEmpty()) {
                            HandlerCatchData take = taskQueue.poll();
                            Element rootElementForCharset = getRootElement(take.getEvt(), take.getDevice().getCharset());
                            String sn = XmlUtil.getText(rootElementForCharset, "SN");
                            String channelId = XmlUtil.getText(rootElementForCharset, "DeviceID");
                            RecordInfo recordInfo = new RecordInfo();
                            recordInfo.setChannelId(channelId);
                            recordInfo.setDeviceId(take.getDevice().getDeviceId());
                            recordInfo.setSn(sn);
                            recordInfo.setName(XmlUtil.getText(rootElementForCharset, "Name"));
                            String sumNumStr = XmlUtil.getText(rootElementForCharset, "SumNum");
                            int sumNum = 0;
                            if (!StringUtils.isEmpty(sumNumStr)) {
                                sumNum = Integer.parseInt(sumNumStr);
                            }
                            recordInfo.setSumNum(sumNum);
                            Element recordListElement = rootElementForCharset.element("RecordList");
                            if (recordListElement == null || sumNum == 0) {
                                logger.info("无录像数据");
                                eventPublisher.recordEndEventPush(recordInfo);
                                recordDataCatch.put(take.getDevice().getDeviceId(), sn, sumNum, new ArrayList<>());
                                releaseRequest(take.getDevice().getDeviceId(), sn);
                            } else {
                                Iterator<Element> recordListIterator = recordListElement.elementIterator();
                                if (recordListIterator != null) {
                                    List<RecordItem> recordList = new ArrayList<>();
                                    // 遍历DeviceList
                                    while (recordListIterator.hasNext()) {
                                        Element itemRecord = recordListIterator.next();
                                        Element recordElement = itemRecord.element("DeviceID");
                                        if (recordElement == null) {
                                            logger.info("记录为空，下一个...");
                                            continue;
                                        }
                                        RecordItem record = new RecordItem();
                                        record.setDeviceId(XmlUtil.getText(itemRecord, "DeviceID"));
                                        record.setName(XmlUtil.getText(itemRecord, "Name"));
                                        record.setFilePath(XmlUtil.getText(itemRecord, "FilePath"));
                                        record.setFileSize(XmlUtil.getText(itemRecord, "FileSize"));
                                        record.setAddress(XmlUtil.getText(itemRecord, "Address"));

                                        String startTimeStr = XmlUtil.getText(itemRecord, "StartTime");
                                        record.setStartTime(DateUtil.ISO8601Toyyyy_MM_dd_HH_mm_ss(startTimeStr));

                                        String endTimeStr = XmlUtil.getText(itemRecord, "EndTime");
                                        record.setEndTime(DateUtil.ISO8601Toyyyy_MM_dd_HH_mm_ss(endTimeStr));

                                        record.setSecrecy(itemRecord.element("Secrecy") == null ? 0
                                                : Integer.parseInt(XmlUtil.getText(itemRecord, "Secrecy")));
                                        record.setType(XmlUtil.getText(itemRecord, "Type"));
                                        record.setRecorderId(XmlUtil.getText(itemRecord, "RecorderID"));
                                        recordList.add(record);
                                    }
                                    recordInfo.setRecordList(recordList);
                                    // 发送消息，如果是上级查询此录像，则会通过这里通知给上级
                                    eventPublisher.recordEndEventPush(recordInfo);
                                    int count = recordDataCatch.put(take.getDevice().getDeviceId(), sn, sumNum, recordList);
                                    logger.info("[国标录像]， {}->{}: {}/{}", take.getDevice().getDeviceId(), sn, count, sumNum);
                                }

                                if (recordDataCatch.isComplete(take.getDevice().getDeviceId(), sn)){
                                    releaseRequest(take.getDevice().getDeviceId(), sn);
                                }
                            }
                        }
                        taskQueueHandlerRun = false;
                    }catch (DocumentException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

        } catch (SipException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handForPlatform(RequestEvent evt, ParentPlatform parentPlatform, Element element) {

    }

    public void releaseRequest(String deviceId, String sn){
        String key = DeferredResultHolder.CALLBACK_CMD_RECORDINFO + deviceId + sn;
        WVPResult<RecordInfo> wvpResult = new WVPResult<>();
        wvpResult.setCode(0);
        wvpResult.setMsg("success");
        // 对数据进行排序
        Collections.sort(recordDataCatch.getRecordInfo(deviceId, sn).getRecordList());
        wvpResult.setData(recordDataCatch.getRecordInfo(deviceId, sn));

        RequestMessage msg = new RequestMessage();
        msg.setKey(key);
        msg.setData(wvpResult);
        deferredResultHolder.invokeAllResult(msg);
        recordDataCatch.remove(deviceId, sn);
    }
}
