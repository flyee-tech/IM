package com.peiel.im.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author Peiel
 * @version V1.0
 * @date 2019-11-26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("msg_index")
public class MsgIndexDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ownerUserId;
    private Long otherUserId;
    private Integer type;
    private Long msgId;
    private Integer status;
    private Date createdDate;

}
