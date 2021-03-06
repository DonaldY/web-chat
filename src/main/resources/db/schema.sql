DROP TABLE IF EXISTS IM_USER;
DROP TABLE IF EXISTS IM_MSG_CONTENT;
DROP TABLE IF EXISTS IM_MSG_RELATION;
DROP TABLE IF EXISTS IM_MSG_CONTACT;

-- 用户表
CREATE TABLE IM_USER (
  uid INT PRIMARY KEY,
  username VARCHAR(500) NOT NULL,
  password VARCHAR(500) NOT NULL,
  email VARCHAR(250) DEFAULT NULL,
  avatar VARCHAR(500) NOT NULL
);

-- 消息内容表
CREATE TABLE IM_MSG_CONTENT (
  mid INT AUTO_INCREMENT  PRIMARY KEY,
  content VARCHAR(1000) NOT NULL,
  sender_id INT NOT NULL,
  recipient_id INT NOT NULL,
  msg_type INT NOT NULL,
  create_time TIMESTAMP NOT NUll
);

-- 消息关系表
CREATE TABLE IM_MSG_RELATION (
  owner_uid INT NOT NULL,
  other_uid INT NOT NULL,
  mid INT NOT NULL,
  type INT NOT NULL,
  create_time TIMESTAMP NOT NULL,
  PRIMARY KEY (`owner_uid`,`mid`)
);
CREATE INDEX `idx_owneruid_otheruid_msgid` ON IM_MSG_RELATION(`owner_uid`,`other_uid`,`mid`);

CREATE TABLE IM_MSG_CONTACT (
  owner_uid INT NOT NULL,
  other_uid INT NOT NULL,
  mid INT NOT NULL,
  type INT NOT NULL,
  create_time TIMESTAMP NOT NULL,
  PRIMARY KEY (`owner_uid`,`other_uid`)
);