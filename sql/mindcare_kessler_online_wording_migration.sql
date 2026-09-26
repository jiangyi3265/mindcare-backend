-- Align the online K6/K10 wording with the author's Mandarin core items.
-- Existing completed records are not changed. Only the earlier seed version is updated.
-- Source and free-use terms: https://rckessler.scholars.harvard.edu/k10-and-k6-scales

update mc_content set payload_json=json_set(payload_json,
  '$.description','依据 Kessler K6 官网普通话版核心条目改为逐题网页呈现，了解过去30天心理困扰的频率；本平台表述未单独进行信效度检验，也不用于独立诊断或危机判定。',
  '$.sourceName','Ronald C. Kessler et al. (2003), K6 普通话版条目参考',
  '$.version','K6 Mandarin online adaptation',
  '$.options',json_array('无','偶尔','一部分时间','大部分时间','全部时间'),
  '$.questions',json_array('过去30天中，您感到紧张的频率如何？','过去30天中，您感到绝望的频率如何？','过去30天中，您感到不安或烦躁的频率如何？','过去30天中，您感到太沮丧以至于什么都不能让您愉快起来的频率如何？','过去30天中，您感到做每一件事情都很费劲的频率如何？','过去30天中，您感到无价值的频率如何？'))
where content_type='assessment' and content_key='k6'
  and json_unquote(json_extract(payload_json,'$.version'))='K6 Mandarin core 6';

update mc_content set payload_json=json_set(payload_json,
  '$.description','依据 Kessler K10 官网普通话版核心条目改为逐题网页呈现，记录过去30天心理困扰的频率；本平台表述未单独进行信效度检验，也不用于独立诊断或危机判定。',
  '$.sourceName','Ronald C. Kessler et al. (2003), K10 普通话版条目参考',
  '$.version','K10 Mandarin online adaptation',
  '$.options',json_array('无','偶尔','一部分时间','大部分时间','全部时间'),
  '$.questions',json_array('过去30天中，您感到无法解释的筋疲力尽的频率如何？','过去30天中，您感到紧张的频率如何？','过去30天中，您感到太紧张以至于什么都不能让您平静下来的频率如何？','过去30天中，您感到绝望的频率如何？','过去30天中，您感到不安或烦躁的频率如何？','过去30天中，您感到太不安以至于静坐不能的频率如何？','过去30天中，您感到沮丧的频率如何？','过去30天中，您感到太沮丧以至于什么都不能让您愉快起来的频率如何？','过去30天中，您感到做每一件事情都很费劲的频率如何？','过去30天中，您感到无价值的频率如何？'))
where content_type='assessment' and content_key='k10'
  and json_unquote(json_extract(payload_json,'$.version'))='K10 Mandarin core 10';
