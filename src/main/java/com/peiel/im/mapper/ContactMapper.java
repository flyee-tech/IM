package com.peiel.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.peiel.im.model.ContactDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Peiel
 * @version V1.0
 * @date 2019-11-25
 */
@Mapper
public interface ContactMapper extends BaseMapper<ContactDO> {

    List<ContactDO> getContactListByUserId(Long userId);

}
