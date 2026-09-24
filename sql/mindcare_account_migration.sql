-- Existing MindCare installations only. Run once after a verified database backup.
-- Do not rerun the destructive sql/mindcare.sql on an existing deployment.
create table if not exists mc_account (
  account_id bigint(20) not null auto_increment,
  phone varchar(11) not null,
  password_hash varchar(100) not null,
  recovery_hash varchar(100) not null,
  nickname varchar(50) default '',
  failed_attempts int not null default 0,
  locked_until datetime default null,
  create_time datetime,
  update_time datetime,
  primary key (account_id),
  unique key uk_mc_account_phone (phone)
) engine=innodb default charset=utf8mb4;

alter table mc_client add column account_id bigint(20) default null after token_hash;
alter table mc_client add key idx_mc_client_account (account_id);
alter table mc_client add constraint fk_mc_client_account foreign key (account_id) references mc_account(account_id) on delete set null;

alter table mc_record add column owner_key varchar(80) not null default '' after client_id;
update mc_record set owner_key = concat('c:', client_id) where owner_key = '';
alter table mc_record add unique key uk_mc_record_owner_key (owner_key, record_key);
alter table mc_record add key idx_mc_record_owner (owner_key, update_time);

update sys_menu set menu_name = '用户账户与终端' where menu_id = 2007;
