-- Back-office follow-up details for risk and consultation records.
-- Run once against the MindCare database before deploying the matching backend.
set @has_handling_method := (select count(*) from information_schema.columns where table_schema=database() and table_name='mc_record' and column_name='handling_method');
set @sql := if(@has_handling_method = 0, 'alter table mc_record add column handling_method varchar(30) default null comment ''后台处理方式'' after risk_reason', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @has_handling_note := (select count(*) from information_schema.columns where table_schema=database() and table_name='mc_record' and column_name='handling_note');
set @sql := if(@has_handling_note = 0, 'alter table mc_record add column handling_note varchar(500) default null comment ''后台处理备注'' after handling_method', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @has_handling_time := (select count(*) from information_schema.columns where table_schema=database() and table_name='mc_record' and column_name='handling_time');
set @sql := if(@has_handling_time = 0, 'alter table mc_record add column handling_time datetime default null comment ''后台处理时间'' after handling_note', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;
