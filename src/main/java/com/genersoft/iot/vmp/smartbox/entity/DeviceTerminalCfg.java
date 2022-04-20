package com.genersoft.iot.vmp.smartbox.entity;

/**
 * Description
 *
 * @author xieyu
 * @version v1.0.0
 * Date: 2022/4/18 13:56
 */
public class DeviceTerminalCfg {
    private Long id;
    private String deviceId;
    private String channelId;
    private String passwd;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    @Override
    public String toString() {
        return "DeviceTerminalCfg{" +
                "id=" + id +
                ", deviceId='" + deviceId + '\'' +
                ", channelId='" + channelId + '\'' +
                ", passwd='" + passwd + '\'' +
                '}';
    }
}
