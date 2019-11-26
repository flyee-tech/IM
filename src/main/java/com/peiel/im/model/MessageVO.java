package com.peiel.im.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author Peiel
 * @version V1.0
 * @date 2019-11-26
 */
@Data
@Builder
public class MessageVO {

    private MsgIndexDO msgIndex;
    private UserDO ownerUser;
    private UserDO otherUser;
    private MsgDO msg;

}
