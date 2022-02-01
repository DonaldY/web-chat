package donald.im.service.impl;

import donald.im.dao.MessageContactRepository;
import donald.im.dao.MessageContentRepository;
import donald.im.dao.UserRepository;
import donald.im.entity.MessageContact;
import donald.im.entity.MessageContent;
import donald.im.entity.User;
import donald.im.exceptions.InvalidUserInfoException;
import donald.im.exceptions.UserNotExistException;
import donald.im.service.UserService;
import donald.im.vo.MessageContactVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageContactRepository contactRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MessageContentRepository contentRepository;

    @Override
    public User login(String email, String password) {
        List<User> users = userRepository.findByEmail(email);
        if (null == users || users.isEmpty()) {
            log.warn("该用户不存在:" + email);
            throw new UserNotExistException("该用户不存在:" + email);
        } else {
            User user = users.get(0);
            if (user.getPassword().equals(password)) {
                log.info(user.getUsername() + " logged in!");
                return user;
            } else {
                log.warn(user.getUsername() + " failed to log in!");
                throw new InvalidUserInfoException("invalid user info:" + user.getUsername());
            }
        }
    }


    @Override
    public List<User> getAllUsersExcept(long exceptUid) {
        List<User> otherUsers = userRepository.findAll();
        otherUsers.remove(userRepository.findOne(exceptUid));
        return otherUsers;
    }

    @Override
    public List<User> getAllUsersExcept(User exceptUser) {
        List<User> otherUsers = userRepository.findUsersByUidIsNot(exceptUser.getUid());
        return otherUsers;
    }

    @Override
    public MessageContactVO getContacts(User ownerUser) {
        List<MessageContact> contacts = contactRepository.findMessageContactsByOwnerUidOrderByMidDesc(ownerUser.getUid());
        if (contacts != null) {
            long totalUnread = 0;
            Object totalUnreadObj = redisTemplate.opsForValue().get(ownerUser.getUid() + "_T");
            if (null != totalUnreadObj) {
                totalUnread = Long.parseLong((String) totalUnreadObj);
            }

            final MessageContactVO contactVO = new MessageContactVO(ownerUser.getUid(), ownerUser.getUsername(), ownerUser.getAvatar(), totalUnread);
            contacts.stream().forEach(contact -> {
                Long mid = contact.getMid();
                MessageContent contentVO = contentRepository.findOne(mid);
                User otherUser = userRepository.findOne(contact.getOtherUid());

                if (null != contentVO) {
                    long convUnread = 0;
                    Object convUnreadObj = redisTemplate.opsForHash().get(ownerUser.getUid() + "_C", otherUser.getUid());
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
}
