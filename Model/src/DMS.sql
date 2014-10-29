/*==============================================================*/
/* DBMS name:      ORACLE Version 11g                           */
/* Created on:     2014/7/31 9:48:05                            */
/*==============================================================*/


drop table DCM_COMBINATION cascade constraints;

drop table DCM_ERROR cascade constraints;

drop table DCM_JOB cascade constraints;

drop table DCM_ROLE_TEMPLATE cascade constraints;

drop table DCM_TEMPLATE cascade constraints;

drop table DCM_TEMPLATE_COLUMN cascade constraints;

drop table DCM_TEMPLATE_COMBINATION cascade constraints;

drop table DCM_TEMPLATE_CTRL cascade constraints;

drop table DCM_TEMPLATE_VALIDATION cascade constraints;

drop table DCM_VALIDATION cascade constraints;

drop table DMS_AUDIT_MSG cascade constraints;

drop table DMS_FUNCTION cascade constraints;

drop table DMS_GROUP cascade constraints;

drop table DMS_GROUP_ROLE cascade constraints;

drop table DMS_MENU cascade constraints;

drop table DMS_PROPERTY cascade constraints;

drop table DMS_ROLE cascade constraints;

drop table DMS_ROLE_FUNCTION cascade constraints;

drop table DMS_ROLE_MENU cascade constraints;

drop table DMS_ROLE_VALUE cascade constraints;

drop table DMS_USER cascade constraints;

drop table DMS_USER_GROUP cascade constraints;

drop table DMS_USER_KEY cascade constraints;

drop table DMS_VALUE_SET cascade constraints;

/*==============================================================*/
/* Table: DCM_COMBINATION                                       */
/*==============================================================*/
create table DCM_COMBINATION 
(
   ID                   VARCHAR2(32)         not null,
   VALUE_SET_ID         VARCHAR2(32),
   VALUE_ID             VARCHAR2(100),
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   constraint PK_DCM_COMBINATION primary key (ID)
);

comment on table DCM_COMBINATION is
'模版组合';

comment on column DCM_COMBINATION.ID is
'标识ID';

comment on column DCM_COMBINATION.VALUE_SET_ID is
'值集ID';

comment on column DCM_COMBINATION.VALUE_ID is
'值ID';

comment on column DCM_COMBINATION.CREATED_AT is
'创建时间';

comment on column DCM_COMBINATION.UPDATED_AT is
'更新时间';

comment on column DCM_COMBINATION.UPDATED_BY is
'更新者';

comment on column DCM_COMBINATION.CREATED_BY is
'创建者';

/*==============================================================*/
/* Table: DCM_ERROR                                             */
/*==============================================================*/
create table DCM_ERROR 
(
   JOB_ID               VARCHAR2(32)         not null,
   SHEET_NAME           VARCHAR2(300),
   ROW_NUM              INTEGER              not null,
   MSG                  VARCHAR2(3000),
   "LEVEL"              VARCHAR2(10),
   LOCALE               VARCHAR2(10)         not null,
   SHEET_NO             INTEGER              not null,
   VALIDATION_ID        VARCHAR2(32)         not null,
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   constraint PK_DCM_ERROR primary key (JOB_ID, ROW_NUM, LOCALE, SHEET_NO, VALIDATION_ID)
);

comment on table DCM_ERROR is
'错误信息';

comment on column DCM_ERROR.JOB_ID is
'任务ID';

comment on column DCM_ERROR.SHEET_NAME is
'工作簿名称';

comment on column DCM_ERROR.ROW_NUM is
'行号';

comment on column DCM_ERROR.MSG is
'错误消息';

comment on column DCM_ERROR."LEVEL" is
'错误级别';

comment on column DCM_ERROR.LOCALE is
'语言';

comment on column DCM_ERROR.SHEET_NO is
'工作簿编号';

comment on column DCM_ERROR.VALIDATION_ID is
'校验程序ID';

comment on column DCM_ERROR.CREATED_AT is
'创建时间';

comment on column DCM_ERROR.UPDATED_AT is
'更新时间';

comment on column DCM_ERROR.UPDATED_BY is
'更新者';

comment on column DCM_ERROR.CREATED_BY is
'创建者';

/*==============================================================*/
/* Table: DCM_JOB                                               */
/*==============================================================*/
create table DCM_JOB 
(
   ID                   VARCHAR2(32)         not null,
   TEMPLATE_ID          VARCHAR2(32),
   COMBINATION_ID       VARCHAR2(32),
   "MODE"               VARCHAR2(10),
   FILE_PATH            VARCHAR2(300),
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   STATUS               VARCHAR2(10),
   constraint PK_DCM_JOB primary key (ID)
);

comment on table DCM_JOB is
'导入任务';

comment on column DCM_JOB.ID is
'标识ID';

comment on column DCM_JOB.TEMPLATE_ID is
'模版ID';

comment on column DCM_JOB.COMBINATION_ID is
'组合ID';

comment on column DCM_JOB."MODE" is
'导入模式';

comment on column DCM_JOB.FILE_PATH is
'文件路径';

comment on column DCM_JOB.CREATED_AT is
'创建时间';

comment on column DCM_JOB.UPDATED_AT is
'更新时间';

comment on column DCM_JOB.UPDATED_BY is
'更新者';

comment on column DCM_JOB.CREATED_BY is
'创建者';

comment on column DCM_JOB.STATUS is
'状态';

/*==============================================================*/
/* Table: DCM_ROLE_TEMPLATE                                     */
/*==============================================================*/
create table DCM_ROLE_TEMPLATE 
(
   ID                   VARCHAR2(32)         not null,
   ROLE_ID              VARCHAR2(32),
   TEMPLATE_ID          VARCHAR2(32),
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   READ_ONLY            VARCHAR2(10),
   constraint PK_DCM_ROLE_TEMPLATE primary key (ID)
);

comment on table DCM_ROLE_TEMPLATE is
'角色模版对应表';

comment on column DCM_ROLE_TEMPLATE.ID is
'标识ID';

comment on column DCM_ROLE_TEMPLATE.ROLE_ID is
'角色ID';

comment on column DCM_ROLE_TEMPLATE.TEMPLATE_ID is
'模版ID';

comment on column DCM_ROLE_TEMPLATE.CREATED_AT is
'创建时间';

comment on column DCM_ROLE_TEMPLATE.UPDATED_AT is
'更新时间';

comment on column DCM_ROLE_TEMPLATE.UPDATED_BY is
'更新者';

comment on column DCM_ROLE_TEMPLATE.CREATED_BY is
'创建者';

comment on column DCM_ROLE_TEMPLATE.READ_ONLY is
'只读标识';

/*==============================================================*/
/* Table: DCM_TEMPLATE                                          */
/*==============================================================*/
create table DCM_TEMPLATE 
(
   ID                   VARCHAR2(32)         not null,
   LOCALE               VARCHAR2(10)         not null,
   NAME                 VARCHAR2(300),
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   READONLY             VARCHAR2(10),
   SEQ                  INTEGER,
   DESCRIPTION          VARCHAR2(1000),
   DB_TABLE             VARCHAR2(100),
   DB_VIEW              VARCHAR2(100),
   TMP_TABLE            VARCHAR2(100),
   PRE_PROGRAM          VARCHAR2(200),
   HANDLE_PROGRAM       VARCHAR2(200),
   AFTER_PROGRAM        VARCHAR2(200),
   HANDLE_MODE          VARCHAR2(10),
   TEMPLATE_FILE        VARCHAR2(300),
   constraint PK_DCM_TEMPLATE primary key (ID, LOCALE)
);

comment on table DCM_TEMPLATE is
'模版';

comment on column DCM_TEMPLATE.ID is
'标识ID';

comment on column DCM_TEMPLATE.LOCALE is
'语言';

comment on column DCM_TEMPLATE.NAME is
'模版名称';

comment on column DCM_TEMPLATE.CREATED_AT is
'创建时间';

comment on column DCM_TEMPLATE.UPDATED_AT is
'更新时间';

comment on column DCM_TEMPLATE.UPDATED_BY is
'更新者';

comment on column DCM_TEMPLATE.CREATED_BY is
'创建者';

comment on column DCM_TEMPLATE.READONLY is
'只读';

comment on column DCM_TEMPLATE.SEQ is
'序号';

comment on column DCM_TEMPLATE.DESCRIPTION is
'描述';

comment on column DCM_TEMPLATE.DB_TABLE is
'数据库表';

comment on column DCM_TEMPLATE.DB_VIEW is
'数据库视图';

comment on column DCM_TEMPLATE.TMP_TABLE is
'临时表';

comment on column DCM_TEMPLATE.PRE_PROGRAM is
'预处理程序';

comment on column DCM_TEMPLATE.HANDLE_PROGRAM is
'处理程序';

comment on column DCM_TEMPLATE.AFTER_PROGRAM is
'善后程序';

comment on column DCM_TEMPLATE.HANDLE_MODE is
'处理模式';

comment on column DCM_TEMPLATE.TEMPLATE_FILE is
'模版文件路径';

/*==============================================================*/
/* Table: DCM_TEMPLATE_COLUMN                                   */
/*==============================================================*/
create table DCM_TEMPLATE_COLUMN 
(
   ID                   VARCHAR2(32)         not null,
   LOCALE               VARCHAR2(10)         not null,
   COLUMN_LABEL         VARCHAR2(300),
   DB_TABLE_COL         VARCHAR2(100),
   DB_VIEW_COL          VARCHAR2(100),
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   IS_PK                VARCHAR2(10),
   READONLY             VARCHAR2(10),
   DATA_TYPE            VARCHAR2(100),
   VISIBLE              VARCHAR2(10),
   SEQ                  INTEGER,
   TEMPLATE_ID          VARCHAR2(32),
   constraint PK_DCM_TEMPLATE_COLUMN primary key (ID, LOCALE)
);

comment on table DCM_TEMPLATE_COLUMN is
'模版列';

comment on column DCM_TEMPLATE_COLUMN.ID is
'标识ID';

comment on column DCM_TEMPLATE_COLUMN.LOCALE is
'语言';

comment on column DCM_TEMPLATE_COLUMN.COLUMN_LABEL is
'列文本';

comment on column DCM_TEMPLATE_COLUMN.DB_TABLE_COL is
'数据库表列';

comment on column DCM_TEMPLATE_COLUMN.DB_VIEW_COL is
'数据库视图列';

comment on column DCM_TEMPLATE_COLUMN.CREATED_AT is
'创建时间';

comment on column DCM_TEMPLATE_COLUMN.UPDATED_AT is
'更新时间';

comment on column DCM_TEMPLATE_COLUMN.UPDATED_BY is
'更新者';

comment on column DCM_TEMPLATE_COLUMN.CREATED_BY is
'创建者';

comment on column DCM_TEMPLATE_COLUMN.IS_PK is
'主键标识';

comment on column DCM_TEMPLATE_COLUMN.READONLY is
'只读';

comment on column DCM_TEMPLATE_COLUMN.DATA_TYPE is
'数据类型';

comment on column DCM_TEMPLATE_COLUMN.VISIBLE is
'是否可见';

comment on column DCM_TEMPLATE_COLUMN.SEQ is
'序号';

comment on column DCM_TEMPLATE_COLUMN.TEMPLATE_ID is
'模版ID';

/*==============================================================*/
/* Table: DCM_TEMPLATE_COMBINATION                              */
/*==============================================================*/
create table DCM_TEMPLATE_COMBINATION 
(
   ID                   VARCHAR2(32)         not null,
   TEMPLATE_ID          VARCHAR2(32),
   COMBINATION_ID       VARCHAR2(32),
   STATUS               VARCHAR2(10),
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   constraint PK_DCM_TEMPLATE_COMBINATION primary key (ID)
);

comment on table DCM_TEMPLATE_COMBINATION is
'模版组合控制';

comment on column DCM_TEMPLATE_COMBINATION.ID is
'标识ID';

comment on column DCM_TEMPLATE_COMBINATION.TEMPLATE_ID is
'模版ID';

comment on column DCM_TEMPLATE_COMBINATION.COMBINATION_ID is
'组合ID';

comment on column DCM_TEMPLATE_COMBINATION.STATUS is
'状态';

comment on column DCM_TEMPLATE_COMBINATION.CREATED_AT is
'创建时间';

comment on column DCM_TEMPLATE_COMBINATION.UPDATED_AT is
'更新时间';

comment on column DCM_TEMPLATE_COMBINATION.UPDATED_BY is
'更新者';

comment on column DCM_TEMPLATE_COMBINATION.CREATED_BY is
'创建者';

/*==============================================================*/
/* Table: DCM_TEMPLATE_CTRL                                     */
/*==============================================================*/
create table DCM_TEMPLATE_CTRL 
(
   ID                   VARCHAR2(32)         not null,
   VALUE_SET_ID         VARCHAR2(32),
   SEQ                  INTEGER,
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   constraint PK_DCM_TEMPLATE_CTRL primary key (ID)
);

comment on table DCM_TEMPLATE_CTRL is
'模版控制维';

comment on column DCM_TEMPLATE_CTRL.ID is
'标识ID';

comment on column DCM_TEMPLATE_CTRL.VALUE_SET_ID is
'值集ID';

comment on column DCM_TEMPLATE_CTRL.SEQ is
'序号';

comment on column DCM_TEMPLATE_CTRL.CREATED_AT is
'创建时间';

comment on column DCM_TEMPLATE_CTRL.UPDATED_AT is
'更新时间';

comment on column DCM_TEMPLATE_CTRL.UPDATED_BY is
'更新者';

comment on column DCM_TEMPLATE_CTRL.CREATED_BY is
'创建者';

/*==============================================================*/
/* Table: DCM_TEMPLATE_VALIDATION                               */
/*==============================================================*/
create table DCM_TEMPLATE_VALIDATION 
(
   ID                   VARCHAR2(32)         not null,
   COLUMN_ID            VARCHAR2(32),
   VALIDATION_ID        VARCHAR2(32),
   SEQ                  INTEGER,
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   constraint PK_DCM_TEMPLATE_VALIDATION primary key (ID)
);

comment on table DCM_TEMPLATE_VALIDATION is
'模版校验程序对应表';

comment on column DCM_TEMPLATE_VALIDATION.ID is
'标识ID';

comment on column DCM_TEMPLATE_VALIDATION.COLUMN_ID is
'模版列ID';

comment on column DCM_TEMPLATE_VALIDATION.VALIDATION_ID is
'校验程序ID';

comment on column DCM_TEMPLATE_VALIDATION.SEQ is
'序号';

comment on column DCM_TEMPLATE_VALIDATION.CREATED_AT is
'创建时间';

comment on column DCM_TEMPLATE_VALIDATION.UPDATED_AT is
'更新时间';

comment on column DCM_TEMPLATE_VALIDATION.UPDATED_BY is
'更新者';

comment on column DCM_TEMPLATE_VALIDATION.CREATED_BY is
'创建者';

/*==============================================================*/
/* Table: DCM_VALIDATION                                        */
/*==============================================================*/
create table DCM_VALIDATION 
(
   ID                   VARCHAR2(32)         not null,
   LOCALE               VARCHAR2(10)         not null,
   NAME                 VARCHAR2(100),
   PROGRAM              VARCHAR2(300),
   ARGS                 VARCHAR2(1000),
   REQUIRED             VARCHAR2(10),
   DESCRIPTION          VARCHAR2(1000),
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   constraint PK_DCM_VALIDATION primary key (ID, LOCALE)
);

comment on table DCM_VALIDATION is
'校验程序';

comment on column DCM_VALIDATION.ID is
'标识ID';

comment on column DCM_VALIDATION.LOCALE is
'语言';

comment on column DCM_VALIDATION.NAME is
'校验程序名';

comment on column DCM_VALIDATION.PROGRAM is
'校验程序';

comment on column DCM_VALIDATION.ARGS is
'参数';

comment on column DCM_VALIDATION.REQUIRED is
'强制通过标识';

comment on column DCM_VALIDATION.DESCRIPTION is
'描述';

comment on column DCM_VALIDATION.CREATED_AT is
'创建时间';

comment on column DCM_VALIDATION.UPDATED_AT is
'更新时间';

comment on column DCM_VALIDATION.UPDATED_BY is
'更新者';

comment on column DCM_VALIDATION.CREATED_BY is
'创建者';

/*==============================================================*/
/* Table: DMS_AUDIT_MSG                                         */
/*==============================================================*/
create table DMS_AUDIT_MSG 
(
   ID                   VARCHAR2(32)         not null,
   LOCALE               VARCHAR2(10)         not null,
   CATEGORY             VARCHAR2(100),
   MSG                  VARCHAR2(4000),
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   constraint PK_DMS_AUDIT_MSG primary key (ID, LOCALE)
);

comment on table DMS_AUDIT_MSG is
'用户追踪';

comment on column DMS_AUDIT_MSG.ID is
'标识ID';

comment on column DMS_AUDIT_MSG.LOCALE is
'语言';

comment on column DMS_AUDIT_MSG.CATEGORY is
'分类';

comment on column DMS_AUDIT_MSG.MSG is
'消息';

comment on column DMS_AUDIT_MSG.UPDATED_BY is
'更新者';

comment on column DMS_AUDIT_MSG.CREATED_BY is
'创建者';

comment on column DMS_AUDIT_MSG.CREATED_AT is
'创建时间';

comment on column DMS_AUDIT_MSG.UPDATED_AT is
'更新时间';

/*==============================================================*/
/* Table: DMS_FUNCTION                                          */
/*==============================================================*/
create table DMS_FUNCTION 
(
   ID                   VARCHAR2(32)         not null,
   FUNCTION_LOCALE      VARCHAR2(10)         not null,
   DESCRIPTION          VARCHAR2(1000),
   NAME                 VARCHAR2(100),
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   ACTION               VARCHAR2(100),
   PARAMETERS           VARCHAR2(1000),
   CATEGORY             VARCHAR2(100),
   SEQ                  INTEGER,
   P_FUNCTION_ID        VARCHAR2(32),
   constraint PK_DMS_FUNCTION primary key (ID, FUNCTION_LOCALE)
);

comment on table DMS_FUNCTION is
'功能表';

comment on column DMS_FUNCTION.ID is
'标识ID';

comment on column DMS_FUNCTION.FUNCTION_LOCALE is
'语言';

comment on column DMS_FUNCTION.DESCRIPTION is
'描述';

comment on column DMS_FUNCTION.NAME is
'功能名';

comment on column DMS_FUNCTION.CREATED_AT is
'创建时间';

comment on column DMS_FUNCTION.UPDATED_AT is
'更新时间';

comment on column DMS_FUNCTION.UPDATED_BY is
'更新者';

comment on column DMS_FUNCTION.CREATED_BY is
'创建者';

comment on column DMS_FUNCTION.ACTION is
'动作';

comment on column DMS_FUNCTION.PARAMETERS is
'参数';

comment on column DMS_FUNCTION.CATEGORY is
'分类';

comment on column DMS_FUNCTION.SEQ is
'序号';

comment on column DMS_FUNCTION.P_FUNCTION_ID is
'父功能ID';

/*==============================================================*/
/* Table: DMS_GROUP                                             */
/*==============================================================*/
create table DMS_GROUP 
(
   ID                   VARCHAR2(32)         not null,
   LOCALE               VARCHAR2(10)         not null,
   NAME                 VARCHAR2(100),
   ENABLE_FLAG          VARCHAR2(10),
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   constraint PK_DMS_GROUP primary key (ID, LOCALE)
);

comment on table DMS_GROUP is
'用户组';

comment on column DMS_GROUP.ID is
'标识ID';

comment on column DMS_GROUP.LOCALE is
'语言';

comment on column DMS_GROUP.NAME is
'组名称';

comment on column DMS_GROUP.ENABLE_FLAG is
'有效标识';

comment on column DMS_GROUP.CREATED_AT is
'创建时间';

comment on column DMS_GROUP.UPDATED_AT is
'更新时间';

comment on column DMS_GROUP.UPDATED_BY is
'更新者';

comment on column DMS_GROUP.CREATED_BY is
'创建者';

/*==============================================================*/
/* Table: DMS_GROUP_ROLE                                        */
/*==============================================================*/
create table DMS_GROUP_ROLE 
(
   ID                   VARCHAR2(32)         not null,
   GROUP_ID              VARCHAR2(32),
   ROLE_ID              VARCHAR2(32),
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   constraint PK_DMS_GROUP_ROLE primary key (ID)
);

comment on table DMS_GROUP_ROLE is
'用户组角色对应表';

comment on column DMS_GROUP_ROLE.ID is
'标识ID';

comment on column DMS_GROUP_ROLE.GROUP_ID is
'用户组ID';

comment on column DMS_GROUP_ROLE.ROLE_ID is
'角色ID';

comment on column DMS_GROUP_ROLE.CREATED_AT is
'创建时间';

comment on column DMS_GROUP_ROLE.UPDATED_AT is
'更新时间';

comment on column DMS_GROUP_ROLE.UPDATED_BY is
'更新者';

comment on column DMS_GROUP_ROLE.CREATED_BY is
'创建者';

/*==============================================================*/
/* Table: DMS_MENU                                              */
/*==============================================================*/
create table DMS_MENU 
(
   ID                   VARCHAR2(32)         not null,
   LOCALE               VARCHAR2(10)         not null,
   SEQ                  INTEGER,
   LABEL                VARCHAR2(150),
   ENABLE_FLAG          VARCHAR2(10),
   P_ID                 VARCHAR2(32),
   FUNCTION_ID         VARCHAR2(32),
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   constraint PK_DMS_MENU primary key (ID, LOCALE)
);

comment on table DMS_MENU is
'菜单';

comment on column DMS_MENU.ID is
'标识ID';

comment on column DMS_MENU.LOCALE is
'语言';

comment on column DMS_MENU.SEQ is
'序号';

comment on column DMS_MENU.LABEL is
'显示文本';

comment on column DMS_MENU.ENABLE_FLAG is
'有效标识';

comment on column DMS_MENU.P_ID is
'父菜单ID';

comment on column DMS_MENU.FUNCTION_ID is
'功能ID';

comment on column DMS_MENU.CREATED_AT is
'创建时间';

comment on column DMS_MENU.UPDATED_AT is
'更新时间';

comment on column DMS_MENU.UPDATED_BY is
'更新者';

comment on column DMS_MENU.CREATED_BY is
'创建者';

/*==============================================================*/
/* Table: DMS_PROPERTY                                          */
/*==============================================================*/
create table DMS_PROPERTY 
(
   ID                   VARCHAR2(32)         not null,
   KEY                  VARCHAR2(100),
   VALUE                VARCHAR2(100),
   ENABLE_FLAG          VARCHAR2(10),
   "DESC"               VARCHAR2(300),
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   constraint PK_DMS_PROPERTY primary key (ID)
);

comment on table DMS_PROPERTY is
'属性表';

comment on column DMS_PROPERTY.ID is
'标识ID';

comment on column DMS_PROPERTY.KEY is
'属性';

comment on column DMS_PROPERTY.VALUE is
'属性值';

comment on column DMS_PROPERTY.ENABLE_FLAG is
'有效标识';

comment on column DMS_PROPERTY."DESC" is
'描述';

comment on column DMS_PROPERTY.CREATED_AT is
'创建时间';

comment on column DMS_PROPERTY.UPDATED_AT is
'更新时间';

comment on column DMS_PROPERTY.UPDATED_BY is
'更新者';

comment on column DMS_PROPERTY.CREATED_BY is
'创建者';

/*==============================================================*/
/* Table: DMS_ROLE                                              */
/*==============================================================*/
create table DMS_ROLE 
(
   ID                   VARCHAR2(32)         not null,
   LOCALE               VARCHAR2(10)         not null,
   ROLE_NAME            VARCHAR2(100),
   ENABLE_FLAG          VARCHAR2(10),
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   constraint PK_DMS_ROLE primary key (ID, LOCALE)
);

comment on table DMS_ROLE is
'角色';

comment on column DMS_ROLE.ID is
'标识ID';

comment on column DMS_ROLE.LOCALE is
'语言';

comment on column DMS_ROLE.ROLE_NAME is
'角色名';

comment on column DMS_ROLE.ENABLE_FLAG is
'有效标识';

comment on column DMS_ROLE.CREATED_AT is
'创建时间';

comment on column DMS_ROLE.UPDATED_AT is
'更新时间';

comment on column DMS_ROLE.UPDATED_BY is
'更新者';

comment on column DMS_ROLE.CREATED_BY is
'创建者';

/*==============================================================*/
/* Table: DMS_ROLE_FUNCTION                                     */
/*==============================================================*/
create table DMS_ROLE_FUNCTION 
(
   ROLE_FUNCTION_ID     VARCHAR2(32)         not null,
   ROLE_ID              VARCHAR2(32),
   FUNCTION_ID          VARCHAR2(32),
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   constraint PK_DMS_ROLE_FUNCTION primary key (ROLE_FUNCTION_ID)
);

comment on table DMS_ROLE_FUNCTION is
'角色功能对应表';

comment on column DMS_ROLE_FUNCTION.ROLE_FUNCTION_ID is
'ID';

comment on column DMS_ROLE_FUNCTION.ROLE_ID is
'角色ID';

comment on column DMS_ROLE_FUNCTION.FUNCTION_ID is
'功能ID';

comment on column DMS_ROLE_FUNCTION.CREATED_AT is
'创建时间';

comment on column DMS_ROLE_FUNCTION.UPDATED_AT is
'更新时间';

comment on column DMS_ROLE_FUNCTION.UPDATED_BY is
'更新者';

comment on column DMS_ROLE_FUNCTION.CREATED_BY is
'创建者';

/*==============================================================*/
/* Table: DMS_ROLE_MENU                                         */
/*==============================================================*/
create table DMS_ROLE_MENU 
(
   ID                   VARCHAR2(32)         not null,
   ROLE_ID              VARCHAR2(32),
   MENU_ID              VARCHAR2(32),
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   constraint PK_DMS_ROLE_MENU primary key (ID)
);

comment on table DMS_ROLE_MENU is
'角色菜单对应表';

comment on column DMS_ROLE_MENU.ID is
'标识ID';

comment on column DMS_ROLE_MENU.ROLE_ID is
'角色ID';

comment on column DMS_ROLE_MENU.MENU_ID is
'菜单ID';

comment on column DMS_ROLE_MENU.CREATED_AT is
'创建时间';

comment on column DMS_ROLE_MENU.UPDATED_AT is
'更新时间';

comment on column DMS_ROLE_MENU.UPDATED_BY is
'更新者';

comment on column DMS_ROLE_MENU.CREATED_BY is
'创建者';

/*==============================================================*/
/* Table: DMS_ROLE_VALUE                                        */
/*==============================================================*/
create table DMS_ROLE_VALUE 
(
   ID                   VARCHAR2(32)         not null,
   ROLE_ID              VARCHAR2(32),
   VALUE_SET_ID         VARCHAR2(32),
   VALUE_ID             VARCHAR2(100),
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   constraint PK_DMS_ROLE_VALUE primary key (ID)
);

comment on table DMS_ROLE_VALUE is
'角色和值对应表';

comment on column DMS_ROLE_VALUE.ID is
'标识ID';

comment on column DMS_ROLE_VALUE.ROLE_ID is
'角色ID';

comment on column DMS_ROLE_VALUE.VALUE_SET_ID is
'值集ID';

comment on column DMS_ROLE_VALUE.VALUE_ID is
'值ID';

comment on column DMS_ROLE_VALUE.UPDATED_BY is
'更新者';

comment on column DMS_ROLE_VALUE.CREATED_BY is
'创建者';

comment on column DMS_ROLE_VALUE.CREATED_AT is
'创建时间';

comment on column DMS_ROLE_VALUE.UPDATED_AT is
'更新时间';

/*==============================================================*/
/* Table: DMS_USER                                              */
/*==============================================================*/
create table DMS_USER 
(
   ID                   VARCHAR2(32)         not null,
   ACC                  VARCHAR2(100),
   PWD                  VARCHAR2(100),
   LOCK_FLAG            VARCHAR2(10),
   ENABLE_FLAG          VARCHAR2(10),
   RETRY_COUNT          INTEGER,
   LOCK_TIME            DATE,
   UPDATED_AT           DATE,
   CREATED_AT           DATE,
   CREATED_BY           VARCHAR2(32),
   UPDATED_BY           VARCHAR2(32),
   NAME                 VARCHAR2(100),
   SEX                  VARCHAR2(10),
   MAIL                 VARCHAR2(100),
   LOCALE               VARCHAR2(32),
   PHONE                VARCHAR2(100),
   OTHER_INFO           VARCHAR2(1000),
   constraint PK_DMS_USER primary key (ID)
);

comment on table DMS_USER is
'用户表';

comment on column DMS_USER.ID is
'标识ID';

comment on column DMS_USER.ACC is
'帐号';

comment on column DMS_USER.PWD is
'密码';

comment on column DMS_USER.LOCK_FLAG is
'锁定标识';

comment on column DMS_USER.ENABLE_FLAG is
'有效标识';

comment on column DMS_USER.RETRY_COUNT is
'重试次数';

comment on column DMS_USER.LOCK_TIME is
'锁定时间';

comment on column DMS_USER.UPDATED_AT is
'更新时间';

comment on column DMS_USER.CREATED_AT is
'创建时间';

comment on column DMS_USER.CREATED_BY is
'创建者';

comment on column DMS_USER.UPDATED_BY is
'更新者';

comment on column DMS_USER.NAME is
'姓名';

comment on column DMS_USER.SEX is
'性别';

comment on column DMS_USER.MAIL is
'邮箱';

comment on column DMS_USER.LOCALE is
'语言';

comment on column DMS_USER.PHONE is
'电话';

comment on column DMS_USER.OTHER_INFO is
'其它信息';

/*==============================================================*/
/* Table: DMS_USER_GROUP                                        */
/*==============================================================*/
create table DMS_USER_GROUP 
(
   ID                   VARCHAR2(32)         not null,
   USER_ID              VARCHAR2(32),
   GROUP_ID              VARCHAR2(32),
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   constraint PK_DMS_USER_GROUP primary key (ID)
);

comment on table DMS_USER_GROUP is
'用户用户组对应表';

comment on column DMS_USER_GROUP.ID is
'标识ID';

comment on column DMS_USER_GROUP.USER_ID is
'用户ID';

comment on column DMS_USER_GROUP.GROUP_ID is
'用户组ID';

comment on column DMS_USER_GROUP.CREATED_AT is
'创建时间';

comment on column DMS_USER_GROUP.UPDATED_AT is
'更新时间';

comment on column DMS_USER_GROUP.UPDATED_BY is
'更新者';

comment on column DMS_USER_GROUP.CREATED_BY is
'创建者';

/*==============================================================*/
/* Table: DMS_USER_KEY                                          */
/*==============================================================*/
create table DMS_USER_KEY 
(
   ID                   VARCHAR2(32)         not null,
   USER_ID              VARCHAR2(32),
   KEY                  VARCHAR2(100),
   CREATED_AT           DATE,
   constraint PK_DMS_USER_KEY primary key (ID)
);

comment on table DMS_USER_KEY is
'用户临时凭证表';

comment on column DMS_USER_KEY.ID is
'标识ID';

comment on column DMS_USER_KEY.USER_ID is
'用户ID';

comment on column DMS_USER_KEY.KEY is
'用户KEY';

comment on column DMS_USER_KEY.CREATED_AT is
'创建时间';

/*==============================================================*/
/* Table: DMS_VALUE_SET                                         */
/*==============================================================*/
create table DMS_VALUE_SET 
(
   ID                   VARCHAR2(32)         not null,
   LOCALE               VARCHAR2(10)         not null,
   NAME                 VARCHAR2(100),
   SOURCE               VARCHAR2(100),
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   constraint PK_DMS_VALUE_SET primary key (ID, LOCALE)
);

comment on table DMS_VALUE_SET is
'值集';

comment on column DMS_VALUE_SET.ID is
'标识ID';

comment on column DMS_VALUE_SET.LOCALE is
'语言';

comment on column DMS_VALUE_SET.NAME is
'值集名';

comment on column DMS_VALUE_SET.SOURCE is
'值集源';

comment on column DMS_VALUE_SET.CREATED_AT is
'创建时间';

comment on column DMS_VALUE_SET.UPDATED_AT is
'更新时间';

comment on column DMS_VALUE_SET.UPDATED_BY is
'更新者';

comment on column DMS_VALUE_SET.CREATED_BY is
'创建者';

/*==============================================================*/
/* Table: DMS_LOOKUP                                         */
/*==============================================================*/
create table DMS_LOOKUP
(
   ID                   VARCHAR2(32)         not null,
   LOCALE               VARCHAR2(10)         not null,
   LOOKUP_TYPE          VARCHAR2(100)        NOT NULL,
   CODE                 VARCHAR2(100)        not null,
   MEANING              VARCHAR2(100)        not null,
   IDX                  NUMBER,
   CREATED_AT           DATE,
   UPDATED_AT           DATE,
   UPDATED_BY           VARCHAR2(32),
   CREATED_BY           VARCHAR2(32),
   constraint PK_DMS_LOOKUP primary key (LOOKUP_TYPE, LOCALE,CODE)
);

comment on table DMS_LOOKUP is
'字典表';

comment on column DMS_LOOKUP.ID is
'标识ID';

comment on column DMS_LOOKUP.LOCALE is
'语言';

comment on column DMS_LOOKUP.LOOKUP_TYPE is
'分类';

comment on column DMS_LOOKUP.CODE is
'编码';

comment on column DMS_LOOKUP.MEANING is
'含义';

comment on column DMS_LOOKUP.IDX is
'序号';

comment on column DMS_LOOKUP.CREATED_AT is
'创建时间';

comment on column DMS_LOOKUP.UPDATED_AT is
'更新时间';

comment on column DMS_LOOKUP.UPDATED_BY is
'更新者';

comment on column DMS_LOOKUP.CREATED_BY is
'创建者';