package com.genersoft.iot.vmp.smartbox.dao;

import com.genersoft.iot.vmp.smartbox.entity.DeviceTerminalCfg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Description
 *
 * @author xieyu
 * @version v1.0.0
 * Date: 2022/4/18 13:54
 */
@Mapper
@Repository
public interface DeviceTerminalCfgMapper {
    @Select("select * from device_terminal_cfg where deviceId = #{deviceId}")
    List<DeviceTerminalCfg> list(String deviceId);
}
