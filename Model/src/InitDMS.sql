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
values('100000','zh_CN','主页',sysdate,sysdate,'10000','10000','index','通用',100000,null);
insert into dms_function(id,function_locale,name,created_at,updated_at,updated_by,created_by,action,category,seq,p_function_id)
values('100000','en','Home',sysdate,sysdate,'10000','10000','GoHome','index',100000,null);

insert into dms_function(id,function_locale,name,created_at,updated_at,updated_by,created_by,action,category,seq,p_function_id)
values('101000','zh_CN','设置',sysdate,sysdate,'10000','10000','settings','设置',101000,null);
insert into dms_function(id,function_locale,name,created_at,updated_at,updated_by,created_by,action,category,seq,p_function_id)
values('101000','en','Setting',sysdate,sysdate,'10000','10000','settings','Settings',101000,null);

insert into dms_function(id,function_locale,name,created_at,updated_at,updated_by,created_by,action,category,seq,p_function_id)
values('101011','zh_CN','帐号管理',sysdate,sysdate,'10000','10000','/WEB-INF/dmsUser/user_manage_tsk.xml#user_manage_tsk','用户管理',101011,'101010');
insert into dms_function(id,function_locale,name,created_at,updated_at,updated_by,created_by,action,category,seq,p_function_id)
values('101011','en','Account Management',sysdate,sysdate,'10000','10000','/WEB-INF/dmsUser/user_manage_tsk.xml#user_manage_tsk','User Manage',101011,'101010');

insert into dms_function(id,function_locale,name,created_at,updated_at,updated_by,created_by,action,category,seq,p_function_id)
values('101012','zh_CN','个人信息',sysdate,sysdate,'10000','10000','/WEB-INF/dmsUser/user_info_tsk.xml#user_info_tsk','用户管理',101012,'101010');
insert into dms_function(id,function_locale,name,created_at,updated_at,updated_by,created_by,action,category,seq,p_function_id)
values('101012','en','Personal Info',sysdate,sysdate,'10000','10000','/WEB-INF/dmsUser/user_info_tsk.xml#user_info_tsk','User Manage',101012,'101010');

insert into dms_function(id,function_locale,name,created_at,updated_at,updated_by,created_by,action,category,seq,p_function_id)
values('101013','zh_CN','用户组管理',sysdate,sysdate,'10000','10000','/WEB-INF/dmsGroup/edit_group_tsk.xml#edit_group_tsk','用户组管理',101013,'101010');
insert into dms_function(id,function_locale,name,created_at,updated_at,updated_by,created_by,action,category,seq,p_function_id)
values('101013','en','User Group Manage',sysdate,sysdate,'10000','10000','/WEB-INF/dmsGroup/edit_group_tsk.xml#edit_group_tsk','User Group Manage',101013,'101010');

insert into dms_function(id,function_locale,name,created_at,updated_at,updated_by,created_by,action,category,seq,p_function_id)
values('101014','zh_CN','用户组分配',sysdate,sysdate,'10000','10000','/WEB-INF/dmsGroupUser/user_group_tsk.xml#user_group_tsk','用户组管理',101014,'101010');
insert into dms_function(id,function_locale,name,created_at,updated_at,updated_by,created_by,action,category,seq,p_function_id)
values('101014','en','User Group Assign',sysdate,sysdate,'10000','10000','/WEB-INF/dmsGroupUser/user_group_tsk.xml#user_group_tsk','User Group Manage',101014,'101010');

/*==============================================================*/
/* 初始化菜单                                                     */
/*==============================================================*/
truncate table dms_menu;
insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('100000','zh_CN',100000,'主页','Y',null,'100000',sysdate,sysdate,'10000','10000');
insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('100000','en',100000,'Home','Y',null,'100000',sysdate,sysdate,'10000','10000');

insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('101000','zh_CN',101000,'设置','Y',null,'101000',sysdate,sysdate,'10000','10000');
insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('101000','en',101000,'Settings','Y',null,'101000',sysdate,sysdate,'10000','10000');

insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('101010','zh_CN',101010,'用户管理','Y','101000','',sysdate,sysdate,'10000','10000');
insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('101010','en',101010,'User Manage','Y','101000','',sysdate,sysdate,'10000','10000');

insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('101011','zh_CN',101011,'帐号管理','Y','101010','101011',sysdate,sysdate,'10000','10000');
insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('101011','en',101011,'Account Management','Y','101010','101011',sysdate,sysdate,'10000','10000');

insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('101012','zh_CN',101012,'个人信息','Y','101010','101012',sysdate,sysdate,'10000','10000');
insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('101012','en',101012,'Personal Info','Y','101010','101012',sysdate,sysdate,'10000','10000');

insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('101020','zh_CN',101020,'用户组管理','Y','101000','',sysdate,sysdate,'10000','10000');
insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('101020','en',101020,'User Group Manage','Y','101000','',sysdate,sysdate,'10000','10000');

insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('101021','zh_CN',101021,'用户组编辑','Y','101020','101013',sysdate,sysdate,'10000','10000');
insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('101021','en',101021,'User Group Manage','Y','101020','101013',sysdate,sysdate,'10000','10000');

insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('101022','zh_CN',101022,'用户组分配','Y','101020','101014',sysdate,sysdate,'10000','10000');
insert into dms_menu(id,locale,seq,label,enable_flag,p_id,function_id,created_at,updated_at,updated_by,created_by)
values('101022','en',101022,'User Group Assign','Y','101020','101014',sysdate,sysdate,'10000','10000');