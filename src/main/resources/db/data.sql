INSERT INTO IM_USER (uid, username,password, email, avatar) VALUES
  (1000, '罗翔', '1234','luoxiang@gmail.com', 'luoxiang.png'),
  (1001, '张三', '1234','zhangsan@gmail.com', 'zhangsan.png'),
  (1002, '李四', '1234','lisi@hotmail.com', 'lisi.png');


  INSERT INTO IM_MSG_CONTENT (mid, content, sender_id, recipient_id, msg_type, create_time) VALUES
  (1, '法外狂徒张三', 1000, 1001, 0, '2022-02-01 11:52:38.000'),
  (2, '干啥', 1001, 1000, 0, '2022-02-01 11:53:38.000'),
  (3, '今天晚上想吃啥？', 1000, 1001, 0, '2022-02-01 11:54:38.000'),
  (4, '有啥想法没有？', 1000, 1001, 0, '2022-02-01 11:55:38.000'),
  (5, '我告你诽谤', 1001, 1000, 0, '2022-02-01 11:56:38.000');

  INSERT INTO IM_MSG_RELATION (owner_uid, other_uid, mid, type, create_time) VALUES
  (1000, 1001, 1, 0, '2022-02-01 11:52:38.000'),
  (1001, 1000, 1, 1, '2022-02-01 11:52:38.000'),
  (1001, 1000, 2, 0, '2022-02-01 11:53:38.000'),
  (1000, 1001, 2, 1, '2022-02-01 11:53:38.000'),
  (1000, 1001, 3, 0, '2022-02-01 11:54:38.000'),
  (1001, 1000, 3, 1, '2022-02-01 11:54:38.000'),
  (1000, 1001, 4, 0, '2022-02-01 11:55:38.000'),
  (1001, 1000, 4, 1, '2022-02-01 11:55:38.000'),
  (1001, 1000, 5, 0, '2022-02-01 11:56:38.000'),
  (1000, 1001, 5, 1, '2022-02-01 11:56:38.000');

  INSERT INTO IM_MSG_CONTACT (owner_uid, other_uid, mid, type, create_time) VALUES
  (1000, 1001, 5, 1, '2022-02-01 11:56:38.000'),
  (1001, 1000, 5, 0, '2022-02-01 11:56:38.000');
