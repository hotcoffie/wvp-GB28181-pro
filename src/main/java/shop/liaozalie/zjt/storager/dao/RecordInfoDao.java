package shop.liaozalie.zjt.storager.dao;

import shop.liaozalie.zjt.storager.dao.dto.RecordInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface RecordInfoDao {

    @Insert("INSERT INTO recordInfo (app, stream, mediaServerId, createTime, type, deviceId, channelId, name) VALUES" +
            "('${app}', '${stream}', '${mediaServerId}', datetime('now','localtime')), '${type}', '${deviceId}', '${channelId}', '${name}'")
    int add(RecordInfo recordInfo);

    @Delete("DELETE FROM user WHERE createTime < '${beforeTime}'")
    int deleteBefore(String beforeTime);

    @Select("select * FROM recordInfo")
    List<RecordInfo> selectAll();
}
