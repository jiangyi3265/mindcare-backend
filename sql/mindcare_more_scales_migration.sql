-- Additional sourced public assessments. Idempotent; do not infer crisis risk from
-- K6/K10 or RSES scores without a validated local clinical protocol.
-- K6/K10: © Ronald C. Kessler, PhD; cite Kessler et al. (2003),
-- https://rckessler.scholars.harvard.edu/k10-and-k6-scales
-- RSES: Morris Rosenberg (1965); public-domain use statement and scoring,
-- https://socy.umd.edu/about-us/using-rosenberg-self-esteem-scale

insert into mc_content (content_key, content_type, title, category, summary, payload_json, status, sort_order, create_by, create_time)
select 'k6', 'assessment', 'K6心理困扰筛查', '心理困扰', '六项核心题，了解过去30天心理困扰出现的频率；不单独用于疾病诊断或危机预警。',
json_object('id','k6','title','K6心理困扰筛查','category','心理困扰','count',6,'minutes',3,'art','flowers','hero','rest',
'description','依据 Kessler K6 官网普通话版核心条目改为逐题网页呈现，了解过去30天心理困扰的频率；本平台表述未单独进行信效度检验，也不用于独立诊断或危机判定。',
'sourceName','Ronald C. Kessler et al. (2003), K6 普通话版条目参考','sourceUrl','https://rckessler.scholars.harvard.edu/k10-and-k6-scales',
'license','© Ronald C. Kessler, PhD；官网允许免费使用，须注明版权与文献','version','K6 Mandarin online adaptation','period','过去30天',
'interpretation','总分0–24，分数越高表示过去30天心理困扰出现得越频繁。这里不依据单一分数判定疾病或危机；如感到难以承受，请联系专业人员。',
'options',json_array('无','偶尔','一部分时间','大部分时间','全部时间'),'optionValues',json_array(0,1,2,3,4),
'scoring',json_object('type','sum','maxScore',24,'label','K6总分'),'crisisRules',json_object('direction','none'),
'questions',json_array('过去30天中，您感到紧张的频率如何？','过去30天中，您感到绝望的频率如何？','过去30天中，您感到不安或烦躁的频率如何？','过去30天中，您感到太沮丧以至于什么都不能让您愉快起来的频率如何？','过去30天中，您感到做每一件事情都很费劲的频率如何？','过去30天中，您感到无价值的频率如何？')),
'0', 15, 'admin', sysdate()
where not exists (select 1 from mc_content where content_type='assessment' and content_key='k6');

insert into mc_content (content_key, content_type, title, category, summary, payload_json, status, sort_order, create_by, create_time)
select 'k10', 'assessment', 'K10心理困扰筛查', '心理困扰', '十项核心题，较K6更细致地了解过去30天心理困扰出现的频率。',
json_object('id','k10','title','K10心理困扰筛查','category','心理困扰','count',10,'minutes',4,'art','sunrise','hero','rest',
'description','依据 Kessler K10 官网普通话版核心条目改为逐题网页呈现，记录过去30天心理困扰的频率；本平台表述未单独进行信效度检验，也不用于独立诊断或危机判定。',
'sourceName','Ronald C. Kessler et al. (2003), K10 普通话版条目参考','sourceUrl','https://rckessler.scholars.harvard.edu/k10-and-k6-scales',
'license','© Ronald C. Kessler, PhD；官网允许免费使用，须注明版权与文献','version','K10 Mandarin online adaptation','period','过去30天',
'interpretation','总分0–40，分数越高表示过去30天心理困扰出现得越频繁。量表分数不能单独判断疾病或危机；如感到难以承受，请联系专业人员。',
'options',json_array('无','偶尔','一部分时间','大部分时间','全部时间'),'optionValues',json_array(0,1,2,3,4),
'scoring',json_object('type','sum','maxScore',40,'label','K10总分'),'crisisRules',json_object('direction','none'),
'questions',json_array('过去30天中，您感到无法解释的筋疲力尽的频率如何？','过去30天中，您感到紧张的频率如何？','过去30天中，您感到太紧张以至于什么都不能让您平静下来的频率如何？','过去30天中，您感到绝望的频率如何？','过去30天中，您感到不安或烦躁的频率如何？','过去30天中，您感到太不安以至于静坐不能的频率如何？','过去30天中，您感到沮丧的频率如何？','过去30天中，您感到太沮丧以至于什么都不能让您愉快起来的频率如何？','过去30天中，您感到做每一件事情都很费劲的频率如何？','过去30天中，您感到无价值的频率如何？')),
'0', 16, 'admin', sysdate()
where not exists (select 1 from mc_content where content_type='assessment' and content_key='k10');

insert into mc_content (content_key, content_type, title, category, summary, payload_json, status, sort_order, create_by, create_time)
select 'rses-10', 'assessment', 'Rosenberg自尊量表', '自我认知', '十题了解总体自我评价，按原量表正反向题规则计分；不设未经验证的高低截断。',
json_object('id','rses-10','title','Rosenberg自尊量表','category','自我认知','count',10,'minutes',4,'art','meadow','hero','counseling',
'description','Rosenberg Self-Esteem Scale 的中文工作译本，十题帮助了解总体自我评价；不设置缺乏本地常模支持的高低分界。',
'sourceName','Morris Rosenberg (1965), Rosenberg Self-Esteem Scale','sourceUrl','https://socy.umd.edu/about-us/using-rosenberg-self-esteem-scale',
'license','University of Maryland 声明为公共领域，可翻译改编并注明出处','version','RSES 10-item translated','period','',
'interpretation','总分0–30，分数越高表示更积极的自我评价。反向题已按原量表规则换算；没有适用于所有人的统一高低截断值。',
'options',json_array('非常不同意','不同意','同意','非常同意'),'optionValues',json_array(0,1,2,3),
'scoring',json_object('type','sum','maxScore',30,'label','RSES总分','reverseItems',json_array(2,4,7,8,9)),'crisisRules',json_object('direction','none'),
'questions',json_array('我觉得自己是一个有价值的人。','我觉得自己有不少优点。','总的来说，我倾向于认为自己是一个失败者。','我能像大多数人一样把事情做好。','我觉得自己没有多少值得骄傲的地方。','我对自己持积极的态度。','总体而言，我对自己感到满意。','我希望自己能更尊重自己。','有时我确实觉得自己毫无用处。','有时我觉得自己一点也不好。')),
'0', 17, 'admin', sysdate()
where not exists (select 1 from mc_content where content_type='assessment' and content_key='rses-10');

-- Earlier 12/20-item questionnaires were project-written explorations, not
-- official IPIP/O*NET forms. Correct provenance without changing answers or
-- historical records. Only touch unchanged seed versions.
update mc_content set title='五因素人格体验题组',
summary='参考五因素人格框架自编的20题体验问卷，未经信效度验证，不能据总分判断人格特质。',
payload_json=json_set(payload_json,
'$.title','五因素人格体验题组',
'$.description','参考五因素人格框架自编的20题体验问卷，不是 IPIP 标准量表或经验证的中文译本；不适合用总分判断人格特质。',
'$.sourceName','理论参考：International Personality Item Pool (IPIP)',
'$.license','项目自编题组，非 IPIP 标准量表；未经信效度验证',
'$.version','MindCare 自编体验题','$.isExploratory',true,
'$.interpretation','本题组为自编体验题，题项涉及不同方向，作答合计不具备标准人格测量意义；请不要据此给自己贴标签。')
where content_type='assessment' and content_key='ipip-big5-20'
and json_unquote(json_extract(payload_json,'$.version'))='IPIP short-form seed';

update mc_content set title='气质倾向体验题组',
summary='参考气质相关维度自编的12题体验问卷，未经信效度验证。',
payload_json=json_set(payload_json,
'$.title','气质倾向体验题组',
'$.description','参考气质相关维度自编的12题体验问卷，不是 IPIP 标准量表或经验证的中文译本。',
'$.sourceName','理论参考：International Personality Item Pool (IPIP)',
'$.license','项目自编题组，未经信效度验证',
'$.version','MindCare 自编体验题','$.isExploratory',true,
'$.interpretation','本题组仅供观察自己的作答倾向，没有标准气质类型或高低分界，作答合计不用于正式解释。')
where content_type='assessment' and content_key='ipip-temperament-12'
and json_unquote(json_extract(payload_json,'$.version'))='IPIP temperament seed';

update mc_content set title='情绪觉察与共情体验题组',
summary='围绕情绪觉察、表达与共情自编的12题体验问卷，未经信效度验证。',
payload_json=json_set(payload_json,
'$.title','情绪觉察与共情体验题组',
'$.description','围绕情绪觉察、表达与共情自编的12题体验问卷，不是标准情绪智商量表或经验证的中文译本。',
'$.sourceName','理论参考：International Personality Item Pool (IPIP)',
'$.license','项目自编题组，未经信效度验证',
'$.version','MindCare 自编体验题','$.isExploratory',true,
'$.interpretation','本题组仅供自我探索，不提供有效的情绪智商分数或水平判定。')
where content_type='assessment' and content_key='ipip-emotional-12'
and json_unquote(json_extract(payload_json,'$.version'))='IPIP emotionality seed';

update mc_content set title='职业活动兴趣体验题组',
summary='参考 RIASEC 职业兴趣框架自编的12题体验问卷，未经信效度验证。',
payload_json=json_set(payload_json,
'$.title','职业活动兴趣体验题组',
'$.description','参考 RIASEC 职业兴趣框架自编的12题体验问卷，不是 O*NET Interest Profiler 的官方短版或授权译本。',
'$.sourceName','理论参考：RIASEC 职业兴趣框架',
'$.license','项目自编题组，非 O*NET 官方工具；未经信效度验证',
'$.version','MindCare 自编体验题','$.isExploratory',true,
'$.interpretation','本题组用于回顾感兴趣的活动，不产生标准职业兴趣代码或适岗结论；作答合计不用于正式解释。')
where content_type='assessment' and content_key='onet-riasec-12'
and json_unquote(json_extract(payload_json,'$.version'))='RIASEC experience seed';
