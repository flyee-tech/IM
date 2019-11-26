package com.peiel.im.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

/**
 * @author Peiel
 * @version V1.0
 * @date 2019-11-26
 */
@Data
@Builder
@TableName("msg")
public class MsgDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String content;
}
