function sendMsg(event) {
    event.preventDefault();
    var recipient_id = $("#recipient_id").val();
    var msg_content = $("#message-text").val();
    var sender_id = $("#sender_id").val();
    var sender_avatar = $("#sender_avatar").val();
    var send_time;
    $("#message-text").val("");
    var new_mid;
    $.post(
        '/sendMsg',
        {
            userId: sender_id,
            otherUserId: recipient_id,
            content: msg_content,
            msgType: 1
        },
        function (returnContent) {
            var jsonContent = $.parseJSON(returnContent);
            send_time = jsonContent.msgIndex.createdDate;
            new_mid = jsonContent.msg.id;
            var ul_pane = $('.chat-thread');
            var li_current = $('<li></li>');//创建一个li
            li_current.attr("id", "self-chat-li");
            li_current.text(msg_content);
            li_current.attr("mid", new_mid);
            li_current.attr("other_uid", recipient_id);
            li_current.attr("create_time", send_time);
            li_current.attr("avatar", 'url(' + sender_avatar + ')');
            ul_pane.append(li_current);
            $('.chat-thread').animate({scrollTop: $('.chat-thread').prop("scrollHeight")}, 10);
            return false;
        }
    );
    return false;
}

function queryNewcomingMsg() {

    var lastMid = $('.chat-thread li:last-child').attr("mid");
    var ownerUid = $("#sender_id").val();
    var otherUid = $('.chat-thread li:last-child').attr("other_uid");

    $.get(
        '/queryIncrOneByOneMsg',
        {
            userId: ownerUid,
            otherUserId: otherUid,
            lastMsgId: lastMid
        },
        function (msgsJson) {
            if (msgsJson != "") {
                var jsonarray = $.parseJSON(msgsJson);
                var ul_pane = $('.chat-thread');
                var owner_uid_avatar, other_uid_avatar;
                $.each(jsonarray, function (i, vo) {
                    var relation_type = vo.msgIndex.type;
                    owner_uid_avatar = vo.ownerUser.imgUrl;
                    other_uid_avatar = vo.otherUser.imgUrl;

                    var ul_pane = $('.chat-thread');
                    var li_current = $('<li></li>');//创建一个li

                    li_current.text(vo.msg.content);
                    li_current.attr("mid", vo.msg.id);
                    li_current.attr("other_uid", vo.otherUser.id);
                    li_current.attr("create_time", vo.msgIndex.createdDate);

                    if ((relation_type == 0) && (vo.ownerUser.id == ownerUid)) { //自己发的
                        li_current.attr("id", "self-chat-li");
                        li_current.attr("avatar", 'url(' + sender_avatar + ')');

                    } else if ((relation_type == 1) && (vo.ownerUser.id == ownerUid)) {//别人发的

                        console.log("into type == 1 other send msg!");

                        li_current.attr("id", "other-chat-li");
                        li_current.attr("avatar", 'url(' + other_uid_avatar + ')');
                    }

                    ul_pane.append(li_current);
                    $('.chat-thread').animate({scrollTop: $('.chat-thread').prop("scrollHeight")}, 10);

                });
            }
        }
    );
}


function queryContactsAndUnread() {
    $.get(
        '/queryContacts',
        {
            userId: $("#sender_id").val()
        },
        function (returnContacts) {
            if (returnContacts != "") {
                var jsonContacts = $.parseJSON(returnContacts);
                $("#totalUnread").text(jsonContacts.totalUnread);
                var contactsTR = "";
                $.each(jsonContacts.contactInfos, function (index, info) {
                    var td_images = "<td><img width='50px' src=" + info.imgUrl + "></td>";
                    var td_otherName = "<td>" + info.name + "</td>";
                    var td_content = "<td>" + info.content + "</td>";
                    var td_convUnread = "<td>" + info.hisUnread + "</td>";
                    var td_button = "<td><button type='button' class='btn btn-info' data-toggle='modal' data-target='#chatModal' data-recipient_id='" + info.userId + "' data-recipient_name='" + info.name + "'>和他聊天</button></td>";
                    var tr_html = "<tr>" + td_images + td_otherName + td_content + td_convUnread + td_button + "</tr>";
                    contactsTR += tr_html;
                });
                $("#contactsBody").html(contactsTR);
            }
        }
    );
}

function queryMsg(event) {
    $('.chat-thread').empty();
    $("#self-chat-li-style").remove();
    $("#other-chat-li-style").remove();
    var button = $(event.relatedTarget);
    var recipient_id = button.data('recipient_id');
    var recipient_name = button.data('recipient_name');
    var modal = $("#chatModal");
    modal.find('.modal-title').text('给' + recipient_name + '发送信息：');
    modal.find("#recipient_id").val(recipient_id);
    modal.find("#recipient_name").val(recipient_name);
    var sender_id = $("#sender_id").val();
    $.get(
        '/queryOneByOneMsg',
        {
            userId: sender_id,
            otherUserId: recipient_id
        },
        function (msgsJson) {
            if (msgsJson != "") {
                var jsonarray = $.parseJSON(msgsJson);
                var ul_pane = $('.chat-thread');
                var owner_uid_avatar, other_uid_avatar;
                $.each(jsonarray, function (i, vo) {
                    var li_msg = $('<li></li>');//创建一个li
                    var relation_type = vo.msgIndex.type;
                    var owner_uid = vo.msgIndex.ownerUserId;
                    owner_uid_avatar = vo.ownerUser.imgUrl;
                    other_uid_avatar = vo.otherUser.imgUrl;
                    if ((relation_type == 0) && (owner_uid == sender_id)) { //自己发的
                        li_msg.attr("id", "self-chat-li");
                        li_msg.text(vo.msg.content);
                        li_msg.attr("mid", vo.msg.id);
                        li_msg.attr("other_uid", vo.msgIndex.ownerUserId);
                        li_msg.attr("create_time", vo.msgIndex.createdDate);
                        li_msg.attr("avatar", 'url(' + owner_uid_avatar + ')');
                        li_msg.appendTo(ul_pane);

                    } else if ((relation_type == 1) && (owner_uid == sender_id)) {//别人发的
                        li_msg.attr("id", "other-chat-li");
                        li_msg.text(vo.msg.content);
                        li_msg.attr("mid", vo.msg.id);
                        li_msg.attr("other_uid", vo.msgIndex.otherUserId);
                        li_msg.attr("create_time", vo.msgIndex.createdDate);
                        li_msg.attr("avatar", 'url(' + other_uid_avatar + ')');
                        li_msg.appendTo(ul_pane);
                    }
                    ul_pane.append(li_msg);

                });

                $("<style id='self-chat-li-style'>#self-chat-li:before{background-image:url('" + owner_uid_avatar + "')}</style>").appendTo('head');
                $("<style id='other-chat-li-style'>#other-chat-li:before{background-image:url('" + other_uid_avatar + "')}</style>").appendTo('head');
            }
        }

    );

    newMsgLoop = setInterval(queryNewcomingMsg, 3000);
}