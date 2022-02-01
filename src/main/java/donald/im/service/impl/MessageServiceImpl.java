package donald.im.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import donald.im.Constants;
import donald.im.dao.MessageContactRepository;
import donald.im.dao.MessageContentRepository;
import donald.im.dao.MessageRelationRepository;
import donald.im.dao.UserRepository;
import donald.im.entity.*;
import donald.im.service.MessageService;
import donald.im.vo.MessageContactVO;
import donald.im.vo.MessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageContentRepository contentRepository;
    @Autowired
    private MessageRelationRepository relationRepository;
    @Autowired
    private MessageContactRepository contactRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public MessageVO sendNewMsg(long senderUid, long recipientUid, String content, int msgType) {
        Date currentTime = new Date();
        /**存内容*/
        MessageContent messageContent = new MessageContent();
        messageContent.setSenderId(senderUid);
        messageContent.setRecipientId(recipientUid);
        messageContent.setContent(content);
        messageContent.setMsgType(msgType);
        messageContent.setCreateTime(currentTime);
        messageContent = contentRepository.saveAndFlush(messageContent);
        Long mid = messageContent.getMid();

        /**存发件人的发件箱*/
        MessageRelation messageRelationSender = new MessageRelation();
        messageRelationSender.setMid(mid);
        messageRelationSender.setOwnerUid(senderUid);
        messageRelationSender.setOtherUid(recipientUid);
        messageRelationSender.setType(0);
        messageRelationSender.setCreateTime(currentTime);
        relationRepository.save(messageRelationSender);

        /**存收件人的收件箱*/
        MessageRelation messageRelationRecipient = new MessageRelation();
        messageRelationRecipient.setMid(mid);
        messageRelationRecipient.setOwnerUid(recipientUid);
        messageRelationRecipient.setOtherUid(senderUid);
        messageRelationRecipient.setType(1);
        messageRelationRecipient.setCreateTime(currentTime);
        relationRepository.save(messageRelationRecipient);

        /**更新发件人的最近联系人 */
        MessageContact messageContactSender = contactRepository.findOne(new ContactMultiKeys(senderUid, recipientUid));
        if (messageContactSender != null) {
            messageContactSender.setMid(mid);
        } else {
            messageContactSender = new MessageContact();
            messageContactSender.setOwnerUid(senderUid);
            messageContactSender.setOtherUid(recipientUid);
            messageContactSender.setMid(mid);
            messageContactSender.setCreateTime(currentTime);
            messageContactSender.setType(0);
        }
        contactRepository.save(messageContactSender);

        /**更新收件人的最近联系人 */
        MessageContact messageContactRecipient = contactRepository.findOne(new ContactMultiKeys(recipientUid, senderUid));
        if (messageContactRecipient != null) {
            messageContactRecipient.setMid(mid);
        } else {
            messageContactRecipient = new MessageContact();
            messageContactRecipient.setOwnerUid(recipientUid);
            messageContactRecipient.setOtherUid(senderUid);
            messageContactRecipient.setMid(mid);
            messageContactRecipient.setCreateTime(currentTime);
            messageContactRecipient.setType(1);
        }
        contactRepository.save(messageContactRecipient);

        /**更未读更新 */
        redisTemplate.opsForValue().increment(recipientUid + "_T", 1); //加总未读
        redisTemplate.opsForHash().increment(recipientUid + "_C", senderUid, 1); //加会话未读

        /** 待推送消息发布到redis */
        User self = userRepository.findOne(senderUid);
        User other = userRepository.findOne(recipientUid);
        MessageVO messageVO = new MessageVO(mid, content, self.getUid(), messageContactSender.getType(), other.getUid(), messageContent.getCreateTime(), self.getAvatar(), other.getAvatar(), self.getUsername(), other.getUsername());
        redisTemplate.convertAndSend(Constants.WEBSOCKET_MSG_TOPIC, JSONObject.toJSONString(messageVO));

        return messageVO;
    }

    @Override
    public List<MessageVO> queryConversationMsg(long ownerUid, long otherUid) {
        List<MessageRelation> relationList = relationRepository.findAllByOwnerUidAndOtherUidOrderByMidAsc(ownerUid, otherUid);
        return composeMessageVO(relationList, ownerUid, otherUid);
    }

    @Override
    public List<MessageVO> queryNewerMsgFrom(long ownerUid, long otherUid, long fromMid) {
        List<MessageRelation> relationList = relationRepository.findAllByOwnerUidAndOtherUidAndMidIsGreaterThanOrderByMidAsc(ownerUid, otherUid, fromMid);
        return composeMessageVO(relationList, ownerUid, otherUid);
    }

    private List<MessageVO> composeMessageVO(List<MessageRelation> relationList, long ownerUid, long otherUid) {
        if (null != relationList && !relationList.isEmpty()) {
            /** 先拼接消息索引和内容 */
            List<MessageVO> msgList = Lists.newArrayList();
            User self = userRepository.findOne(ownerUid);
            User other = userRepository.findOne(otherUid);
            relationList.stream().forEach(relation -> {
                Long mid = relation.getMid();
                MessageContent contentVO = contentRepository.findOne(mid);
                if (null != contentVO) {
                    String content = contentVO.getContent();
                    MessageVO messageVO = new MessageVO(mid, content, relation.getOwnerUid(), relation.getType(), relation.getOtherUid(), relation.getCreateTime(), self.getAvatar(), other.getAvatar(), self.getUsername(), other.getUsername());
                    msgList.add(messageVO);
                }
            });

            /** 再变更未读 */
            Object convUnreadObj = redisTemplate.opsForHash().get(ownerUid + Constants.CONVERSION_UNREAD_SUFFIX, otherUid);
            if (null != convUnreadObj) {
                long convUnread = Long.parseLong((String) convUnreadObj);
                redisTemplate.opsForHash().delete(ownerUid + Constants.CONVERSION_UNREAD_SUFFIX, otherUid);
                long afterCleanUnread = redisTemplate.opsForValue().increment(ownerUid + Constants.TOTAL_UNREAD_SUFFIX, -convUnread);
                /** 修正总未读 */
                if (afterCleanUnread <= 0) {
                    redisTemplate.delete(ownerUid + Constants.TOTAL_UNREAD_SUFFIX);
                }
            }
            return msgList;
        }
        return null;
    }

    @Override
    public MessageContactVO queryContacts(long ownerUid) {
        List<MessageContact> contacts = contactRepository.findMessageContactsByOwnerUidOrderByMidDesc(ownerUid);
        if (contacts != null) {
            User user = userRepository.findOne(ownerUid);
            long totalUnread = 0;
            Object totalUnreadObj = redisTemplate.opsForValue().get(user.getUid() + Constants.TOTAL_UNREAD_SUFFIX);
            if (null != totalUnreadObj) {
                totalUnread = Long.parseLong((String) totalUnreadObj);
            }

            MessageContactVO contactVO = new MessageContactVO(user.getUid(), user.getUsername(), user.getAvatar(), totalUnread);
            contacts.stream().forEach(contact -> {
                Long mid = contact.getMid();
                MessageContent contentVO = contentRepository.findOne(mid);
                User otherUser = userRepository.findOne(contact.getOtherUid());

                if (null != contentVO) {
                    long convUnread = 0;
                    Object convUnreadObj = redisTemplate.opsForHash().get(user.getUid() + Constants.CONVERSION_UNREAD_SUFFIX, otherUser.getUid());
                    if (null != convUnreadObj) {
                        convUnread = Long.parseLong((String) convUnreadObj);
                    }
                    MessageContactVO.ContactInfo contactInfo = contactVO.new ContactInfo(otherUser.getUid(), otherUser.getUsername(), otherUser.getAvatar(), mid, contact.getType(), contentVO.getContent(), convUnread, contact.getCreateTime());
                    contactVO.appendContact(contactInfo);
                }
            });
            return contactVO;
        }
        return null;
    }

    @Override
    public long queryTotalUnread(long ownerUid) {
        long totalUnread = 0;
        Object totalUnreadObj = redisTemplate.opsForValue().get(ownerUid + Constants.TOTAL_UNREAD_SUFFIX);
        if (null != totalUnreadObj) {
            totalUnread = Long.parseLong((String) totalUnreadObj);
        }
        return totalUnread;
    }
}
