package donald.im.dao;

import donald.im.entity.MessageContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageContentRepository extends JpaRepository<MessageContent, Long> {

}
