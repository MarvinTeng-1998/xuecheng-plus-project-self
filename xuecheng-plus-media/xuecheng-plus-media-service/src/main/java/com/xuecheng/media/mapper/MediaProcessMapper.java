package com.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

    /*
     * @Description: 根据xxl分片参数获取待处理任务
     * @Author: dengbin
     * @Date: 1/3/24 15:42
     * @param shardTotal: 分片总数
     * @param shardIndex: 分片索引
     * @param count: 任务数
     * @return: java.util.List<com.xuecheng.media.model.po.MediaProcess>
     **/
    @Select("select * from media_process t where t.id % #{shardTotal} = #{shardIndex} " +
            "and (t.status = '1' or t.status = '3')" +
            "and t.fail_count < 3 limit #{count}")
    List<MediaProcess> selectListBySharedIndex(@Param("shardTotal") int shardTotal,
                                               @Param("shardIndex") int shardIndex,
                                               @Param("count") int count);
}
