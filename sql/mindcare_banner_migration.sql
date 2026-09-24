-- Safe to run on an existing MindCare database. Does not replace existing content.
insert ignore into mc_content (content_key, content_type, title, category, summary, payload_json, status, sort_order, create_by, create_time) values
('home-welcome', 'banner', '给心情，一点被看见的时间', '首页轮播图', '', json_object('id', 'home-welcome', 'title', '给心情，一点被看见的时间', 'image', 'builtin:hero'), '0', 1, 'admin', sysdate()),
('home-rest', 'banner', '慢下来，听见自己', '首页轮播图', '', json_object('id', 'home-rest', 'title', '慢下来，听见自己', 'image', 'builtin:rest'), '0', 2, 'admin', sysdate());

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values (2008, '首页轮播图', 2000, 2, 'banners', 'mindcare/content/banner', null, 'MindcareBanners', 1, 0, 'C', '0', '0', 'mindcare:content:list', 'picture', 'admin', sysdate(), '', null, '测评首页轮播图管理')
on duplicate key update menu_name=values(menu_name), parent_id=values(parent_id), order_num=values(order_num), path=values(path), component=values(component), route_name=values(route_name), perms=values(perms), icon=values(icon), status='0', visible='0';

insert ignore into sys_role_menu (role_id, menu_id) values (1, 2008);
