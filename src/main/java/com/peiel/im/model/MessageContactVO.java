package com.peiel.im.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 联系人列表
 *
 * @author Peiel
 * @version V1.0
 * @date 2019-11-25
 */
@Data
public class MessageContactVO {
    private Long userId;
    private String name;
    private String imgUrl;
    private Long totalUnread;
    private List<ContactInfo> contactInfos;

    @Data
    public class ContactInfo {
        private Long userId;
        private String name;
        private String imgUrl;
        private Long mid;
        private Integer type;
        private String content;
        private Long hisUnread;
        private Date createdDate;
    }

    public void appendContactInfo(ContactInfo info) {
        if (contactInfos == null) {
            contactInfos = new ArrayList<>();
        }
        contactInfos.add(info);
    }

}
