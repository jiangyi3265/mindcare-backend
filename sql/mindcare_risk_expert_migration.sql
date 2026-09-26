-- Safety signals and expert profile content for existing MindCare deployments.
-- Run once against the mindcare database before deploying the matching backend.
set @has_risk_level := (select count(*) from information_schema.columns where table_schema=database() and table_name='mc_record' and column_name='risk_level');
set @sql := if(@has_risk_level = 0, 'alter table mc_record add column risk_level varchar(20) not null default ''normal'' after data_json', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;
set @has_risk_reason := (select count(*) from information_schema.columns where table_schema=database() and table_name='mc_record' and column_name='risk_reason');
set @sql := if(@has_risk_reason = 0, 'alter table mc_record add column risk_reason varchar(255) not null default '''' after risk_level', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 2009, '咨询专家', 2000, 5, 'experts', 'mindcare/content/expert', 1, 0, 'C', '0', '0', 'mindcare:content:list', 'user', 'admin', sysdate(), '咨询专家资料管理'
where not exists (select 1 from sys_menu where menu_id=2009);
insert into sys_role_menu (role_id, menu_id) select 1, 2009 where not exists (select 1 from sys_role_menu where role_id=1 and menu_id=2009);

-- Existing four demo scales remain for backwards compatibility. The following
-- published seed content uses public/attributed instruments; SCL-90-R and
-- other licensed instruments must be imported only after obtaining permission.
insert into mc_content (content_key, content_type, title, category, summary, payload_json, status, sort_order, create_by, create_time)
select 'who5', 'assessment', 'WHO-5幸福感指数', '幸福感', '世界卫生组织五项幸福感指数，用于了解近两周的主观幸福感。',
json_object('id','who5','title','WHO-5幸福感指数','category','幸福感','count',5,'minutes',2,'art','meadow','hero','rest',
'description','世界卫生组织五项幸福感指数（WHO-5）的中文工作译本。分数越高代表近两周主观幸福感越好，低分建议进一步关注。',
'sourceName','World Health Organization (WHO-5 Well-Being Index)','sourceUrl','https://www.who.int/publications/m/item/WHO-UCN-MSD-MHE-2024.01','license','CC BY-NC-SA 3.0 IGO','version','2024.01','instrumentType','wellbeing',
'options',json_array('任何时候','有些时候','少部分时间','大部分时间','绝大部分时间','所有时间'),
'optionValues',json_array(0,1,2,3,4,5),
'scoring',json_object('type','sum','maxScore',25,'displayMax',25,'label','WHO-5原始分'),
'crisisRules',json_object('direction','low','threshold',12,'level','high','reason','WHO-5原始分≤12，建议及时关注近期心理状态并寻求专业支持。'),
'questions',json_array('近两周，我一直感到精神愉快、心情舒畅。','近两周，我一直感到平静和安宁。','近两周，我一直感到积极、精力充沛。','近两周，我醒来时感到清醒和充分休息。','近两周，我的日常生活充满了令我感兴趣的事情。')),
'0', 10, 'admin', sysdate()
where not exists (select 1 from mc_content where content_type='assessment' and content_key='who5');

insert into mc_content (content_key, content_type, title, category, summary, payload_json, status, sort_order, create_by, create_time)
select 'ipip-big5-20', 'assessment', 'IPIP五因素人格简表', '人格', '基于公开题库 IPIP 的五因素人格体验测评，帮助了解开放性、尽责性、外向性、宜人性与情绪稳定性。',
json_object('id','ipip-big5-20','title','IPIP五因素人格简表','category','人格','count',20,'minutes',4,'art','room','hero','counseling',
'description','基于 International Personality Item Pool（IPIP）公开领域题库的中文工作译本；结果仅用于自我了解。',
'sourceName','International Personality Item Pool (IPIP)','sourceUrl','https://www.ipip.ori.org/','license','Public domain item pool','version','IPIP short-form seed','instrumentType','personality',
'options',json_array('非常不符合','不太符合','不确定','比较符合','非常符合'),'optionValues',json_array(0,1,2,3,4),
'scoring',json_object('type','sum','maxScore',80,'displayMax',80,'label','人格体验分'),
'crisisRules',json_object('direction','none'),'questions',json_array(
'我喜欢探索新鲜的想法。','我做事有条理并按计划完成。','我愿意主动与人交流。','我通常会体谅别人的感受。','我容易因小事过度担心。',
'我喜欢欣赏艺术、音乐或文学。','我会认真检查自己的工作。','在群体中我常常主动发言。','我愿意帮助遇到困难的人。','遇到压力时我能较快恢复平静。',
'我对不同观点保持好奇。','我会坚持完成已经开始的事情。','我能自然地认识新朋友。','我会尊重并接纳他人的差异。','我常常觉得自己难以放松。',
'我喜欢学习不熟悉的知识。','我会提前安排重要任务。','我享受参加社交活动。','朋友需要时我愿意倾听。','我能稳定地处理日常压力。')),
'0', 11, 'admin', sysdate()
where not exists (select 1 from mc_content where content_type='assessment' and content_key='ipip-big5-20');

insert into mc_content (content_key, content_type, title, category, summary, payload_json, status, sort_order, create_by, create_time)
select 'ipip-temperament-12', 'assessment', 'IPIP气质倾向简表', '气质', '基于 IPIP 公开题库的气质倾向体验测评，观察精力、社交与压力反应方式。',
json_object('id','ipip-temperament-12','title','IPIP气质倾向简表','category','气质','count',12,'minutes',3,'art','sunrise','hero','rest',
'description','基于 International Personality Item Pool（IPIP）公开领域题库的中文工作译本，用于自我了解气质倾向，不作为临床诊断。','sourceName','International Personality Item Pool (IPIP)','sourceUrl','https://www.ipip.ori.org/','license','Public domain item pool','version','IPIP temperament seed','instrumentType','temperament',
'options',json_array('非常不符合','不太符合','比较符合','非常符合'),'optionValues',json_array(0,1,2,3),'scoring',json_object('type','sum','maxScore',36,'displayMax',36,'label','气质倾向分'),'crisisRules',json_object('direction','none'),
'questions',json_array('我喜欢保持忙碌和充满活力。','我在陌生环境中也能较快适应。','我愿意主动表达自己的想法。','我做决定时通常比较果断。','我喜欢独处并从中恢复精力。','我遇到变化时会先观察再行动。','我容易因为压力而变得急躁。','我会为重要目标持续投入。','我在社交场合通常比较放松。','我更偏好安静、稳定的生活节奏。','我愿意尝试新的活动和体验。','我能在兴奋后较快恢复平静。')),
'0', 12, 'admin', sysdate()
where not exists (select 1 from mc_content where content_type='assessment' and content_key='ipip-temperament-12');

insert into mc_content (content_key, content_type, title, category, summary, payload_json, status, sort_order, create_by, create_time)
select 'ipip-emotional-12', 'assessment', 'IPIP情绪能力体验', '情绪智商', '基于 IPIP 公开题库的情绪觉察与共情体验测评，帮助识别表达、倾听和调节优势。',
json_object('id','ipip-emotional-12','title','IPIP情绪能力体验','category','情绪智商','count',12,'minutes',3,'art','flowers','hero','counseling',
'description','基于 IPIP 公开领域题库的情绪与人际相关条目中文工作译本；这不是 WLEIS 等授权工具的替代品。','sourceName','International Personality Item Pool (IPIP)','sourceUrl','https://www.ipip.ori.org/','license','Public domain item pool','version','IPIP emotionality seed','instrumentType','emotional-ability',
'options',json_array('非常不符合','不太符合','比较符合','非常符合'),'optionValues',json_array(0,1,2,3),'scoring',json_object('type','sum','maxScore',36,'displayMax',36,'label','情绪能力体验分'),'crisisRules',json_object('direction','none'),
'questions',json_array('我能较准确地说出自己正在经历的情绪。','我会留意身体发出的紧张或疲惫信号。','我能用合适的方式表达自己的需要。','我愿意耐心听完别人的感受。','我能理解别人话语背后的情绪。','冲突后我会思考双方真正关心的事情。','我遇到挫折时会寻找可行的调整方式。','我会给自己留出恢复情绪的时间。','我能在压力下保持基本的沟通。','我愿意为自己的情绪负责。','我会用尊重的方式提出不同意见。','朋友需要倾诉时我知道如何陪伴。')),
'0', 13, 'admin', sysdate()
where not exists (select 1 from mc_content where content_type='assessment' and content_key='ipip-emotional-12');

insert into mc_content (content_key, content_type, title, category, summary, payload_json, status, sort_order, create_by, create_time)
select 'onet-riasec-12', 'assessment', 'O*NET职业兴趣方向', '职业', '参考美国劳工部 O*NET Interest Profiler 的 RIASEC 兴趣框架，帮助发现更感兴趣的工作活动类型。',
json_object('id','onet-riasec-12','title','O*NET职业兴趣方向','category','职业','count',12,'minutes',3,'art','meadow','hero','activityHero',
'description','根据 U.S. Department of Labor O*NET Interest Profiler 的 RIASEC 框架制作的中文体验版；结果用于探索兴趣，不等同于职业资格或录用测评。','sourceName','U.S. Department of Labor O*NET Interest Profiler','sourceUrl','https://www.onetcenter.org/IP.html','license','O*NET data/public-domain reference; verify current terms','version','RIASEC experience seed','instrumentType','career-interest',
'options',json_array('非常不喜欢','不太喜欢','比较喜欢','非常喜欢'),'optionValues',json_array(0,1,2,3),'scoring',json_object('type','sum','maxScore',36,'displayMax',36,'label','职业兴趣体验分'),'crisisRules',json_object('direction','none'),
'questions',json_array('动手安装、修理或操作设备。','观察自然现象并记录数据。','设计海报、文字或视觉作品。','帮助他人解决学习或生活问题。','组织团队完成计划或项目。','整理资料、表格和流程。','使用工具制作或改造物品。','阅读研究报告并寻找规律。','创作故事、音乐或影像内容。','倾听并支持需要帮助的人。','说服他人接受一个方案。','按步骤处理文件和信息。')),
'0', 14, 'admin', sysdate()
where not exists (select 1 from mc_content where content_type='assessment' and content_key='onet-riasec-12');

insert into mc_content (content_key, content_type, title, category, summary, payload_json, status, sort_order, create_by, create_time)
select 'expert-lin', 'expert', '林老师', '心理咨询师', '擅长情绪管理、压力与睡眠议题，提供温和、结构化的初步支持。',
json_object('id','expert-lin','title','林老师','category','心理咨询师','name','林老师','photo','builtin:avatar','credentials','国家二级心理咨询师（示例资料）','profile','擅长情绪管理、压力与睡眠议题，采用认知行为与正念练习帮助来访者建立可执行的调整计划。','methods',json_array('情绪管理','压力调节','睡眠支持'),'available',true),
'0', 1, 'admin', sysdate()
where not exists (select 1 from mc_content where content_type='expert' and content_key='expert-lin');

insert into mc_content (content_key, content_type, title, category, summary, payload_json, status, sort_order, create_by, create_time)
select 'expert-zhou', 'expert', '周老师', '心理咨询师', '关注亲子沟通与人际关系，帮助来访者梳理关系中的需要与边界。',
json_object('id','expert-zhou','title','周老师','category','心理咨询师','name','周老师','photo','builtin:avatar','credentials','心理学专业背景（示例资料）','profile','关注亲子沟通、人际关系与成长议题，擅长用倾听和沟通练习陪伴来访者找到更合适的表达方式。','methods',json_array('亲子沟通','人际关系','个人成长'),'available',true),
'0', 2, 'admin', sysdate()
where not exists (select 1 from mc_content where content_type='expert' and content_key='expert-zhou');
