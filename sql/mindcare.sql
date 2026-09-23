-- MindCare 三端业务表、初始内容与后台菜单
-- 先执行 sql/ry_20250522.sql，再执行本文件。

drop table if exists mc_record;
drop table if exists mc_client;
drop table if exists mc_content;

create table mc_content (
  content_id       bigint(20)    not null auto_increment comment '内容ID',
  content_key      varchar(64)   not null                comment '端内稳定标识',
  content_type     varchar(20)   not null                comment 'assessment/course/activity',
  title            varchar(100)  not null                comment '标题',
  category         varchar(50)   default ''              comment '分类',
  summary          varchar(500)  default ''              comment '简介',
  payload_json     json          not null                comment '用户端完整内容配置',
  status           char(1)       default '0'             comment '0发布 1下架',
  sort_order       int(4)        default 0               comment '显示顺序',
  create_by        varchar(64)   default ''              comment '创建者',
  create_time      datetime                              comment '创建时间',
  update_by        varchar(64)   default ''              comment '更新者',
  update_time      datetime                              comment '更新时间',
  primary key (content_id),
  unique key uk_mc_content_type_key (content_type, content_key),
  key idx_mc_content_status_sort (content_type, status, sort_order)
) engine=innodb default charset=utf8mb4 comment='MindCare发布内容';

create table mc_client (
  client_id        varchar(64)   not null                comment '匿名安装标识',
  token_hash       varchar(100)  not null                comment '客户端凭证BCrypt摘要',
  nickname         varchar(50)   default ''              comment '昵称',
  phone            varchar(30)   default ''              comment '联系电话',
  last_seen_time   datetime                              comment '最近同步时间',
  create_time      datetime                              comment '创建时间',
  update_time      datetime                              comment '更新时间',
  primary key (client_id),
  key idx_mc_client_seen (last_seen_time)
) engine=innodb default charset=utf8mb4 comment='MindCare用户端匿名身份';

create table mc_record (
  record_id        bigint(20)    not null auto_increment comment '记录ID',
  record_key       varchar(64)   not null                comment '端内稳定记录标识',
  client_id        varchar(64)   not null                comment '用户端标识',
  record_type      varchar(20)   not null                comment 'assessment/consultation/course/activity/message',
  content_key      varchar(64)   default null            comment '关联内容标识',
  title            varchar(100)  default ''              comment '记录标题',
  contact_name     varchar(50)   default ''              comment '联系人',
  contact_phone    varchar(30)   default ''              comment '联系电话',
  status           varchar(20)   default 'submitted'     comment '业务状态',
  score            int          default null             comment '测评分数',
  progress         decimal(5,1)  default null             comment '课程进度百分比',
  data_json        json                                  comment '业务明细',
  create_time      datetime                              comment '创建时间',
  update_by        varchar(64)   default ''              comment '后台更新人',
  update_time      datetime                              comment '更新时间',
  primary key (record_id),
  unique key uk_mc_record_client_key (client_id, record_key),
  key idx_mc_record_type_status (record_type, status),
  key idx_mc_record_update (update_time),
  constraint fk_mc_record_client foreign key (client_id) references mc_client(client_id) on delete cascade
) engine=innodb default charset=utf8mb4 comment='MindCare业务记录';

-- 量表
insert into mc_content (content_key, content_type, title, category, summary, payload_json, status, sort_order, create_by, create_time) values
('emotion', 'assessment', '情绪状态自评', '情绪', '了解近期的情绪感受与变化，帮助你更好地认识和照顾自己的情绪状态。',
 json_object('id','emotion','title','情绪状态自评','category','情绪','count',20,'minutes',5,'art','flowers','hero','rest','description','了解近期的情绪感受与变化，帮助你更好地认识和照顾自己的情绪状态。','questions',json_array('你是否感到情绪低落？','你是否对日常活动提不起兴趣？','你是否感到难以放松？','你是否感到精力不足？','你是否因为小事而烦恼？','你是否感到担忧难以停止？','你是否在晚上难以入睡？','你是否容易分心？','你是否感到紧张？','你是否觉得生活节奏太快？','你是否想独自待着？','你是否感到缺少支持？','你是否难以做出决定？','你是否经常感到疲惫？','你是否为尚未发生的事担心？','你是否因压力而影响食欲？','你是否难以平静地表达感受？','你是否觉得休息不足？','你是否感到心里有很多事？','你是否觉得需要有人倾听？')), '0', 1, 'admin', sysdate()),
('sleep', 'assessment', '睡眠质量自评', '睡眠', '关注你的入睡体验与白天精神状态，给自己更好的休息。',
 json_object('id','sleep','title','睡眠质量自评','category','睡眠','count',18,'minutes',4,'art','sleep','hero','sleep','description','关注你的入睡体验与白天精神状态，给自己更好的休息。','questions',json_array('你是否难以入睡？','你是否在夜间醒来？','你是否早醒后难以再入睡？','你是否白天感到困倦？','你是否因担忧而影响睡眠？','你是否觉得睡眠不足？','你是否难以入睡？','你是否在夜间醒来？','你是否早醒后难以再入睡？','你是否白天感到困倦？','你是否因担忧而影响睡眠？','你是否觉得睡眠不足？','你是否难以入睡？','你是否在夜间醒来？','你是否早醒后难以再入睡？','你是否白天感到困倦？','你是否因担忧而影响睡眠？','你是否觉得睡眠不足？')), '0', 2, 'admin', sysdate()),
('stress', 'assessment', '压力水平自评', '压力', '停下来，留意身体与情绪发出的信号。',
 json_object('id','stress','title','压力水平自评','category','压力','count',10,'minutes',3,'art','flowers','hero','rest','description','停下来，留意身体与情绪发出的信号。','questions',json_array('你是否感到情绪低落？','你是否对日常活动提不起兴趣？','你是否感到难以放松？','你是否感到精力不足？','你是否因为小事而烦恼？','你是否感到担忧难以停止？','你是否在晚上难以入睡？','你是否容易分心？','你是否感到紧张？','你是否觉得生活节奏太快？')), '0', 3, 'admin', sysdate()),
('social', 'assessment', '人际感受自评', '人际', '温柔地观察你与他人的连接和相处体验。',
 json_object('id','social','title','人际感受自评','category','人际','count',10,'minutes',3,'art','room','hero','counseling','description','温柔地观察你与他人的连接和相处体验。','questions',json_array('你是否想独自待着？','你是否感到缺少支持？','你是否难以做出决定？','你是否经常感到疲惫？','你是否为尚未发生的事担心？','你是否因压力而影响食欲？','你是否难以平静地表达感受？','你是否觉得休息不足？','你是否感到心里有很多事？','你是否觉得需要有人倾听？')), '0', 4, 'admin', sysdate());

-- 课程
insert into mc_content (content_key, content_type, title, category, summary, payload_json, status, sort_order, create_by, create_time) values
('stress', 'course', '与压力温柔相处', '情绪管理', '学习识别压力，练习调整呼吸，在日常生活里找回从容。', json_object('id','stress','title','与压力温柔相处','category','情绪管理','minutes',12,'learners','1.2万','art','meadow','hero','video','teacher','林老师','intro','学习识别压力，练习调整呼吸，在日常生活里找回从容。','video','','chapters',json_array(json_object('title','认识压力','duration','03:00'),json_object('title','调整呼吸','duration','05:00'),json_object('title','日常练习','duration','04:00'))), '0', 1, 'admin', sysdate()),
('emotion', 'course', '读懂自己的情绪', '情绪管理', '理解情绪的来处，用温和的方式表达真实感受。', json_object('id','emotion','title','读懂自己的情绪','category','情绪管理','minutes',18,'learners','8600','art','room','hero','video','teacher','林老师','intro','理解情绪的来处，用温和的方式表达真实感受。','video','','chapters',json_array(json_object('title','看见情绪','duration','06:00'),json_object('title','表达感受','duration','06:00'),json_object('title','照顾自己','duration','06:00'))), '0', 2, 'admin', sysdate()),
('breath', 'course', '正念呼吸入门', '睡眠健康', '跟随呼吸，给自己十分钟安静的时间。', json_object('id','breath','title','正念呼吸入门','category','睡眠健康','minutes',10,'learners','6200','art','breathing','hero','rest','teacher','林老师','intro','跟随呼吸，给自己十分钟安静的时间。','video','','chapters',json_array(json_object('title','准备练习','duration','02:00'),json_object('title','专注呼吸','duration','08:00'))), '0', 3, 'admin', sysdate()),
('family', 'course', '亲子沟通的温柔练习', '亲子关系', '先倾听，再表达，让亲子关系多一点理解。', json_object('id','family','title','亲子沟通的温柔练习','category','亲子关系','minutes',15,'learners','3200','art','family','hero','activityHero','teacher','林老师','intro','先倾听，再表达，让亲子关系多一点理解。','video','','chapters',json_array(json_object('title','倾听与理解','duration','07:00'),json_object('title','沟通练习','duration','08:00'))), '0', 4, 'admin', sysdate());

-- 活动
insert into mc_content (content_key, content_type, title, category, summary, payload_json, status, sort_order, create_by, create_time) values
('forest', 'activity', '周末森林疗愈散步', '线下活动', '在自然中散步，通过呼吸练习与交流放松身心。', json_object('id','forest','title','周末森林疗愈散步','date','2026-09-26','time','09:30–11:30','location','城市森林公园·南门','capacity',30,'enrolled',18,'status','报名中','art','walking','hero','forest','intro','在自然中散步，通过呼吸练习与交流放松身心，感受自然的温柔力量。','schedule',json_array(json_array('09:30','集合签到','集合，向导介绍'),json_array('10:00','森林漫步','放慢脚步，感受自然'),json_array('11:00','分享交流','分享感受，温暖倾听'))), '0', 1, 'admin', sysdate()),
('family', 'activity', '亲子情绪沟通工作坊', '亲子活动', '一起练习倾听，在轻松互动中学习表达感受。', json_object('id','family','title','亲子情绪沟通工作坊','date','2026-09-27','time','14:00–16:00','location','阳光社区活动中心','capacity',20,'enrolled',12,'status','报名中','art','family','hero','activityHero','intro','一起练习倾听，在轻松的互动中学习表达感受，建立更亲密的亲子关系。','schedule',json_array(json_array('14:00','签到入场','认识彼此'),json_array('14:30','互动练习','感受与表达'),json_array('15:30','分享交流','一起总结收获'))), '0', 2, 'admin', sysdate());

-- 后台菜单。父目录 + 7 个页面 + 通用按钮权限。
delete from sys_menu where menu_id between 2000 and 2099;
insert into sys_menu values
(2000, 'MindCare运营', 0, 1, 'mindcare', 'Layout', null, 'Mindcare', 1, 0, 'M', '0', '0', null, 'guide', 'admin', sysdate(), '', null, 'MindCare三端运营管理'),
(2001, '运营概览', 2000, 1, 'dashboard', 'mindcare/dashboard/index', null, 'MindcareDashboard', 1, 0, 'C', '0', '0', 'mindcare:dashboard:view', 'dashboard', 'admin', sysdate(), '', null, ''),
(2002, '心理量表', 2000, 2, 'assessments', 'mindcare/content/assessment', null, 'MindcareAssessments', 1, 0, 'C', '0', '0', 'mindcare:content:list', 'edit', 'admin', sysdate(), '', null, ''),
(2003, '心理课程', 2000, 3, 'courses', 'mindcare/content/course', null, 'MindcareCourses', 1, 0, 'C', '0', '0', 'mindcare:content:list', 'education', 'admin', sysdate(), '', null, ''),
(2004, '疗愈活动', 2000, 4, 'activities', 'mindcare/content/activity', null, 'MindcareActivities', 1, 0, 'C', '0', '0', 'mindcare:content:list', 'date', 'admin', sysdate(), '', null, ''),
(2005, '咨询预约', 2000, 5, 'consultations', 'mindcare/record/consultation', null, 'MindcareConsultations', 1, 0, 'C', '0', '0', 'mindcare:record:list', 'message', 'admin', sysdate(), '', null, ''),
(2006, '业务记录', 2000, 6, 'records', 'mindcare/record/index', null, 'MindcareRecords', 1, 0, 'C', '0', '0', 'mindcare:record:list', 'list', 'admin', sysdate(), '', null, ''),
(2007, '用户终端', 2000, 7, 'clients', 'mindcare/client/index', null, 'MindcareClients', 1, 0, 'C', '0', '0', 'mindcare:client:list', 'user', 'admin', sysdate(), '', null, ''),
(2010, '内容查询', 2002, 1, '', '', null, '', 1, 0, 'F', '0', '0', 'mindcare:content:query', '#', 'admin', sysdate(), '', null, ''),
(2011, '内容新增', 2002, 2, '', '', null, '', 1, 0, 'F', '0', '0', 'mindcare:content:add', '#', 'admin', sysdate(), '', null, ''),
(2012, '内容修改', 2002, 3, '', '', null, '', 1, 0, 'F', '0', '0', 'mindcare:content:edit', '#', 'admin', sysdate(), '', null, ''),
(2013, '内容删除', 2002, 4, '', '', null, '', 1, 0, 'F', '0', '0', 'mindcare:content:remove', '#', 'admin', sysdate(), '', null, ''),
(2020, '记录查询', 2005, 1, '', '', null, '', 1, 0, 'F', '0', '0', 'mindcare:record:query', '#', 'admin', sysdate(), '', null, ''),
(2021, '记录处理', 2005, 2, '', '', null, '', 1, 0, 'F', '0', '0', 'mindcare:record:edit', '#', 'admin', sysdate(), '', null, '');

delete from sys_role_menu where menu_id between 2000 and 2099;
insert into sys_role_menu (role_id, menu_id) select 1, menu_id from sys_menu where menu_id between 2000 and 2099;
