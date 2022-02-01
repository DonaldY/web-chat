package donald.im.service;

import donald.im.entity.User;
import donald.im.vo.MessageContactVO;

import java.util.List;

public interface UserService {

    User login(String email, String password);

    List<User> getAllUsersExcept(long exceptUid);

    List<User> getAllUsersExcept(User exceptUser);

    MessageContactVO getContacts(User ownerUser);
}
