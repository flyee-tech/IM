package com.peiel.im.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * @date 2019-11-25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("contact")
public class ContactDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ownerUserId;
    private Long otherUserId;
    @TableField(exist = false)
    private String otherUserName;
    @TableField(exist = false)
    private String otherImgUrl;
    private Long msgId;
    @TableField(exist = false)
    private String content;
    private Integer type;
    private Date updatedDate;

}
