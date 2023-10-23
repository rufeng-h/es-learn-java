package com.windcf.eslearn.mapper;

import com.windcf.eslearn.entity.model.Hotel;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author chunf
 */
@Mapper
public interface HotelMapper {
    /**
     * delete by primary key
     * @param id primaryKey
     * @return deleteCount
     */
    int deleteByPrimaryKey(Long id);

    /**
     * insert record to table
     * @param record the record
     * @return insert count
     */
    int insert(Hotel record);

    int insertOrUpdate(Hotel record);

    int insertOrUpdateSelective(Hotel record);

    /**
     * insert record to table selective
     * @param record the record
     * @return insert count
     */
    int insertSelective(Hotel record);

    /**
     * select by primary key
     * @param id primary key
     * @return object by primary key
     */
    Hotel selectByPrimaryKey(Long id);

    /**
     * update record selective
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(Hotel record);

    /**
     * update record
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(Hotel record);

    int updateBatch(List<Hotel> list);

    int updateBatchSelective(List<Hotel> list);

    int batchInsert(@Param("list") List<Hotel> list);

    List<Hotel> list();
}