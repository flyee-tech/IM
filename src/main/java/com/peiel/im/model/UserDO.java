package com.peiel.im.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author Peiel
 * @version V1.0
 * @date 2019-11-25
 */
@Data
@TableName("user")
public class UserDO {
    private Long id;
    private String name;
    private String imgUrl;
    private String passwd;
    private String status;
}
