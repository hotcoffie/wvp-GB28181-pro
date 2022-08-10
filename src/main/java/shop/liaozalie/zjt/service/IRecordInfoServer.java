package shop.liaozalie.zjt.service;

import shop.liaozalie.zjt.storager.dao.dto.RecordInfo;
import com.github.pagehelper.PageInfo;

public interface IRecordInfoServer {
    PageInfo<RecordInfo> getRecordList(int page, int count);
}
