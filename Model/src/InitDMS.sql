/*==============================================================*/
/* 初始化用户                                                    */
/*==============================================================*/
truncate table dms_user;
insert into dms_user (ID, ACC, PWD, LOCK_FLAG, ENABLE_FLAG, RETRY_COUNT, LOCK_TIME, UPDATED_AT, CREATED_AT, CREATED_BY, UPDATED_BY, NAME, SEX, MAIL, LOCALE, PHONE, OTHER_INFO)
values ('10000', 'admin', '0q6tIQ4drasyjJkfkP5IN7wKrn8=', 'N', 'Y', 0, null, sysdate,sysdate, '10000', '10000', 'Admin', 'M', 'xiangjia.he@hand-china.com', 'zh_CN', '18018690198', '');
/*==============================================================*/
/* 初始化字典表                                                   */
/*==============================================================*/
truncate table dms_lookup;
insert into dms_lookup (ID, LOCALE, LOOKUP_TYPE, CODE, MEANING, IDX, CREATED_AT, UPDATED_AT, UPDATED_BY, CREATED_BY)
values ('10000', 'zh_CN', 'YES_NO', 'Y', '是', 10, sysdate, sysdate, '10000', '10000');

insert into dms_lookup (ID, LOCALE, LOOKUP_TYPE, CODE, MEANING, IDX, CREATED_AT, UPDATED_AT, UPDATED_BY, CREATED_BY)
values ('10000', 'en', 'YES_NO', 'Y', 'Yes', 10, sysdate, sysdate, '10000', '10000');

insert into dms_lookup (ID, LOCALE, LOOKUP_TYPE, CODE, MEANING, IDX, CREATED_AT, UPDATED_AT, UPDATED_BY, CREATED_BY)
values ('10001', 'zh_CN', 'YES_NO', 'N', '否', 20, sysdate, sysdate, '10000', '10000');

insert into dms_lookup (ID, LOCALE, LOOKUP_TYPE, CODE, MEANING, IDX, CREATED_AT, UPDATED_AT, UPDATED_BY, CREATED_BY)
values ('10001', 'en', 'YES_NO', 'N', 'No', 20, sysdate, sysdate, '10000', '10000');

insert into dms_lookup (ID, LOCALE, LOOKUP_TYPE, CODE, MEANING, IDX, CREATED_AT, UPDATED_AT, UPDATED_BY, CREATED_BY)
values ('10002', 'zh_CN', 'DMS_SEX', 'M', '男', 10, sysdate, sysdate, '10000', '10000');

insert into dms_lookup (ID, LOCALE, LOOKUP_TYPE, CODE, MEANING, IDX, CREATED_AT, UPDATED_AT, UPDATED_BY, CREATED_BY)
values ('10002', 'en', 'DMS_SEX', 'M', 'Male', 10, sysdate, sysdate, '10000', '10000');

insert into dms_lookup (ID, LOCALE, LOOKUP_TYPE, CODE, MEANING, IDX, CREATED_AT, UPDATED_AT, UPDATED_BY, CREATED_BY)
values ('10003', 'zh_CN', 'DMS_SEX', 'F', '女', 20, sysdate, sysdate, '10000', '10000');

insert into dms_lookup (ID, LOCALE, LOOKUP_TYPE, CODE, MEANING, IDX, CREATED_AT, UPDATED_AT, UPDATED_BY, CREATED_BY)
values ('10003', 'en', 'DMS_SEX', 'F', 'Female', 20, sysdate, sysdate, '10000', '10000');

insert into dms_lookup (ID, LOCALE, LOOKUP_TYPE, CODE, MEANING, IDX, CREATED_AT, UPDATED_AT, UPDATED_BY, CREATED_BY)
values ('10004', 'zh_CN', 'DMS_LANGUAGE', 'zh_CN', '简体中文', 10, sysdate, sysdate, '10000', '10000');

insert into dms_lookup (ID, LOCALE, LOOKUP_TYPE, CODE, MEANING, IDX, CREATED_AT, UPDATED_AT, UPDATED_BY, CREATED_BY)
values ('10004', 'en', 'DMS_LANGUAGE', 'zh_CN', 'Simplified Chinese', 10, sysdate, sysdate, '10000', '10000');

insert into dms_lookup (ID, LOCALE, LOOKUP_TYPE, CODE, MEANING, IDX, CREATED_AT, UPDATED_AT, UPDATED_BY, CREATED_BY)
values ('10005', 'zh_CN', 'DMS_LANGUAGE', 'en', '英文', 20, sysdate, sysdate, '10000', '10000');

insert into dms_lookup (ID, LOCALE, LOOKUP_TYPE, CODE, MEANING, IDX, CREATED_AT, UPDATED_AT, UPDATED_BY, CREATED_BY)
values ('10005', 'en', 'DMS_LANGUAGE', 'en', 'English', 20, sysdate, sysdate, '10000', '10000');


/*==============================================================*/
/* 初始化功能                                                     */
/*==============================================================*/
truncate table dms_function;
insert into dms_function(id,function_locale,name,created_at,updated_at,updated_by,created_by,action,category,seq,p_function_id)
values('10000','zh_CN','主页',sysdate,sysdate,'10000','10000','index','通用',10000,null);
insert into dms_function(id,function_locale,name,created_at,updated_at,updated_by,created_by,action,category,seq,p_function_id)
values('10000','en','Home',sysdate,sysdate,'10000','10000','GoHome','index',10000,null);
insert into dms_function(id,function_locale,name,created_at,updated_at,updated_by,created_by,action,category,seq,p_function_id)
values('10100','zh_CN','设置',sysdate,sysdate,'10000','10000','settings','设置',10100,null);
insert into dms_function(id,function_locale,name,created_at,updated_at,updated_by,created_by,action,category,seq,p_function_id)
values('10100','en','Setting',sysdate,sysdate,'10000','10000','settings','Settings',10100,null);

/*==============================================================*/
/* 初始化菜单                                                     */
/*==============================================================*/
truncate table dms_menu;
insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('10000','zh_CN',10000,'主页','Y',null,'10000',sysdate,sysdate,'10000','10000');
insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('10000','en',10000,'Home','Y',null,'10000',sysdate,sysdate,'10000','10000');
insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('10100','zh_CN',10100,'设置','Y',null,'10100',sysdate,sysdate,'10000','10000');
insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('10100','en',10100,'Settings','Y',null,'10100',sysdate,sysdate,'10000','10000');
insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('10110','zh_CN',10110,'用户管理','Y','10100','10100',sysdate,sysdate,'10000','10000');
insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('10110','en',10110,'User Manage','Y','10100','10100',sysdate,sysdate,'10000','10000');