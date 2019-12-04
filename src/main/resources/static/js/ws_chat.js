function init() {
    if (window.WebSocket) {
        websocket = new WebSocket("ws://127.0.0.1:8080");
        websocket.onmessage = function (event) {
            onmsg(event);
        };

        websocket.onopen = function () {
            bind();
            heartBeat.start();
        }

        websocket.onclose = function () {
            console.log('onclose');
            $("#ws_status").text("连接中断");
            reconnect();
        };

        websocket.onerror = function () {
            console.log('onerror');
            reconnect();
        };

    } else {
        alert("您的浏览器不支持WebSocket协议！");
    }
}

function bind() { //上线
    if (window.WebSocket) {
        if (websocket.readyState == WebSocket.OPEN) {
            var bindJson = '{ "type": 1, "data": {"uid":' + $("#sender_id").val() + ' }}';
            websocket.send(bindJson);
        }
    }
}

//处理所有web端接收到的数据
var onmsg = function (event) {
    if (event != '') {
        heartBeat.reset();
        var resp = $.parseJSON(event.data);
        console.log(resp);
        if (resp != null) {
            switch (resp.type) {
                case 1:
                    handleBindResp(resp);
                    break;

                case 2:
                    handleQueryMsgResp(resp);
                    break;

                case 3:
                    handleSendMsgResp(resp);
                    break;

                case 4:
                    handleReceivedMsg(resp);
                    break;

                case 5:
                    handleLoopUnreadResp(resp);
                    break;
            }
        }
    }
};


function reconnect() {
    setTimeout(function () {
        var isReconnect = false;
        websocket = new WebSocket("ws://127.0.0.1:8080");
        $("#ws_status").text("重新上线");
        websocket.onmessage = function (event) {
            onmsg(event);
        };

        websocket.onopen = function () {
            bind();
            heartBeat.start();
        }

        websocket.onclose = function () {
            $("#ws_status").text("连接中断");
            if (!isReconnect) {
                console.log('onclose1');
                reconnect();
                isReconnect = true;
            }
        };

        websocket.onerror = function () {
            if (!isReconnect) {
                console.log('onerror1');
                reconnect();
                isReconnect = true;
            }
        };
    }, 5000);
}

function sendMsg(event) {
    event.preventDefault();
    var recipient_id = $("#recipient_id").val();
    var msg_content = $("#message-text").val();
    var sender_id = $("#sender_id").val();
    $("#message-text").val("");
    var sendMsgJson = '{ "type": 3, "data": {"userId":' + sender_id + ',"otherUserId":' + recipient_id + ', "content":"' + msg_content + '","msgType":1  }}';
    websocket.send(sendMsgJson);
    return false;
}

/** 每2分钟发送一次心跳包，接收到消息或者服务端的响应又会重置来重新计时。 */
var heartBeat = {
    timeout: 120000,
    timeoutObj: null,
    serverTimeoutObj: null,
    reset: function () {
        clearTimeout(this.timeoutObj);
        clearTimeout(this.serverTimeoutObj);
        this.start();
    },
    start: function () {
        var self = this;
        this.timeoutObj = setTimeout(function () {
            var sender_id = $("#sender_id").val();
            var sendMsgJson = '{ "type": 0, "data": {"uid":' + sender_id + ',"timeout": 120000}}';
            websocket.send(sendMsgJson);
            self.serverTimeoutObj = setTimeout(function () {
                websocket.close();
                $("#ws_status").text("失去连接！");
            }, self.timeout)
        }, this.timeout)
    },
}

//轮询总未读
function queryUnread() {
    var ownerUid = $("#sender_id").val();
    var queryUnreadJson = '{ "type": 5, "data": {"uid":' + ownerUid + '}}';
    websocket.send(queryUnreadJson);
}

//通过websocket长连来查询消息
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

    var queryMsgJson = '{ "type": 2, "data": {"userId":' + sender_id + ',"otherUserId":' + recipient_id + ' }}';
    websocket.send(queryMsgJson);

    var tbodyID = $(event.relatedTarget).parent().parent().parent().attr("id");
    if (tbodyID == "contactsBody") {
        $(event.relatedTarget).parent().prev().text(0);
    }

}

//处理上线bind请求，主要是回显通知用户
function handleBindResp(resp) {
    var status = resp.status;
    if (status != "" && status == 'success') {
        $("#ws_status").text("上线成功!");
    } else {
        $("#ws_status").text("未上线!");
    }
}

//处理查询消息的响应，界面联动
function handleQueryMsgResp(resp) {
    var jsonarray = resp.data;
    var sender_id = $("#sender_id").val();
    if (jsonarray != "") {
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


//处理发消息的发送方的响应
function handleSendMsgResp(resp) {
    var jsonContent = resp.data;
    console.log(jsonContent)
    var sender_avatar = $("#sender_avatar").val();
    var ul_pane = $('.chat-thread');
    var li_current = $('<li></li>');//创建一个li
    li_current.attr("id", "self-chat-li");
    li_current.text(jsonContent.msg.content);
    li_current.attr("mid", jsonContent.msg.id);
    li_current.attr("other_uid", jsonContent.otherUser.id);
    li_current.attr("create_time", jsonContent.msgIndex.createdDate);
    li_current.attr("avatar", 'url(/images/' + sender_avatar + ')');
    ul_pane.append(li_current);
    $('.chat-thread').animate({scrollTop: $('.chat-thread').prop("scrollHeight")}, 10);


    var currentContactsTR = $("#contactsBody tr");
    var flag1 = false;
    if (currentContactsTR) {
        $.each(currentContactsTR, function (i, contactTR) {
            var chat_uid = $(contactTR).attr("chat_uid");
            if (chat_uid == jsonContent.otherUser.id) {
                $(contactTR).children(":nth-child(3)").text(jsonContent.msg.content);
                flag1 = true;
            }
        });
    }

    if (flag1 == false) {
        var td_images = "<td><img width='50px' src='" + jsonContent.otherUser.imgUrl + "'/></td>";
        var td_otherName = "<td>" + jsonContent.otherUser.name + "</td>";
        var td_content = "<td>" + jsonContent.msg.content + "</td>";
        var td_convUnread = "<td>0</td>";
        var td_button = "<td><button type='button' class='btn btn-info' data-toggle='modal' data-target='#chatModal' data-recipient_id='" + jsonContent.otherUser.id + "' data-recipient_name='" + jsonContent.otherUser.name + "'>和他聊天</button></td>";
        var tr_html = "<tr chat_uid='" + jsonContent.otherUser.id + "'>" + td_images + td_otherName + td_content + td_convUnread + td_button + "</tr>";
        $("#contactsBody").prepend(tr_html);
    }
}

//处理接收到的推送消息，主要是最近联系人界面和聊天界面的展示
function handleReceivedMsg(pushedMsg) {
    var jsonRecipient= pushedMsg.data;
    var tid = pushedMsg.tid;
    if (tid != "") {
        var ackJson = '{ "type": 6, "data": {"tid":' + tid + ' }}';
        websocket.send(ackJson);
    }
    var ul_pane = $('.chat-thread');
    var li_current = $('<li></li>');//创建一个li
    var flag2 = false;
    li_current.attr("id", "other-chat-li");
    li_current.text(jsonRecipient.msg.content);
    li_current.attr("mid", jsonRecipient.msg.id);
    li_current.attr("other_uid", jsonRecipient.ownerUser.id);
    li_current.attr("create_time", jsonRecipient.msgIndex.createdDate);
    li_current.attr("avatar", 'url(' + jsonRecipient.ownerUser.imgUrl + ')');
    ul_pane.append(li_current);
    $('.chat-thread').animate({scrollTop: $('.chat-thread').prop("scrollHeight")}, 10);

    var currentContactsTR = $("#contactsBody tr");
    $.each(currentContactsTR, function (i, contactTR) {
        var chat_uid = $(contactTR).attr("chat_uid");
        if (chat_uid == jsonRecipient.ownerUser.id) {
            $(contactTR).children(":nth-child(3)").text(jsonRecipient.msg.content);
            var unread = parseInt($(contactTR).children(":nth-child(4)").text());
            $(contactTR).children(":nth-child(4)").text(unread + 1);
            flag2 = true;
        }
    });

    if (flag2 == false) {
        var td_images = "<td><img width='50px' src='" + jsonRecipient.ownerUser.imgUrl + "'/></td>";
        var td_otherName = "<td>" + jsonRecipient.ownerUser.name + "</td>";
        var td_content = "<td>" + jsonRecipient.msg.content + "</td>";
        var td_convUnread = "<td>1</td>";
        var td_button = "<td><button type='button' class='btn btn-info' data-toggle='modal' data-target='#chatModal' data-recipient_id='" + jsonRecipient.ownerUser.id + "' data-recipient_name='" + jsonRecipient.ownerUser.name + "'>和他聊天</button></td>";
        var tr_html = "<tr chat_uid='" + jsonRecipient.ownerUser.id + "'>" + td_images + td_otherName + td_content + td_convUnread + td_button + "</tr>";
        $("#contactsBody").prepend(tr_html);
    }

}

//处理轮询总未读的响应
function handleLoopUnreadResp(resp) {
    var totalUnreadData = resp.data;
    $("#totalUnread").text(totalUnreadData.unread);
}



