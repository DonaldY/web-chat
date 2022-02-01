package donald.im.service;


import donald.im.vo.MessageContactVO;
import donald.im.vo.MessageVO;

import java.util.List;

public interface MessageService {

    /**
     * 发新消息
     *
     * @param senderUid
     * @param recipientUid
     * @param content
     * @param msgType
     * @return
     */
    MessageVO sendNewMsg(long senderUid, long recipientUid, String content, int msgType);

    /**
     * 查两人的历史消息
     * @param ownerUid
     * @param otherUid
     * @return
     */
    List<MessageVO> queryConversationMsg(long ownerUid, long otherUid);

    /**
     * 查询两人从某一条消息开始的新消息
     * @param ownerUid
     * @param otherUid
     * @param fromMid
     * @return
     */
    List<MessageVO> queryNewerMsgFrom(long ownerUid, long otherUid, long fromMid);

    /**
     * 查询某个用户的最近联系人
     * @param ownerUid
     * @return
     */
    MessageContactVO queryContacts(long ownerUid);

    /**
     * 查询某人总未读
     * @param ownerUid
     * @return
     */
    long queryTotalUnread(long ownerUid);
}
