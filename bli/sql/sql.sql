create table chat_message
(
    id                     bigint                             not null
        primary key,
    content                text                               null,
    image_url              varchar(255)                       null,
    send_user_id           bigint                             not null,
    receive_user_id        bigint                             not null,
    type                   int                                null comment '1文字消息2图片消息3点赞4关注5回复',
    visibility_by_sender   int                                null comment '对于发送方的可见性',
    visibility_by_receiver int                                null comment '对于接收者的可见性',
    status                 int                                null,
    created_time           datetime default CURRENT_TIMESTAMP null,
    is_expired             tinyint  default 0                 null comment '0未过期1已过期'
);

create table comment
(
    id           bigint                             not null
        primary key,
    entity_type  tinyint                            not null comment '1-视频 2-动态',
    entity_id    bigint                             not null,
    user_id      bigint                             not null,
    content      text                               not null,
    image_url    varchar(255)                       null,
    likes        int      default 0                 null,
    comments     int                                null comment '回复数量',
    created_time datetime default CURRENT_TIMESTAMP null,
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
);

create table favorites_folder
(
    id            bigint                             not null
        primary key,
    title         varchar(50)                        not null,
    video_ids     json                               null,
    user_id       bigint                             not null,
    cover_url     varchar(255)                       null comment '封面',
    is_cover_auto int                                null comment '封面是否自动提取',
    visibility    tinyint  default 1                 null,
    video_count   int      default 0                 null,
    clicks        int      default 0                 null,
    subscribers   json                               null,
    is_default    int                                null comment '是否是默认收藏夹',
    created_time  datetime default CURRENT_TIMESTAMP null,
    updated_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
);

create table history
(
    id           bigint                             not null
        primary key,
    entity_type  tinyint                            not null comment '1-视频 2-动态',
    entity_id    bigint                             not null,
    user_id      mediumtext                         null,
    created_time datetime default CURRENT_TIMESTAMP null
);

create table hot_search
(
    id           bigint                             not null
        primary key,
    content      varchar(100)                       not null,
    search_count int      default 0                 null,
    created_time datetime default CURRENT_TIMESTAMP null
);

create table likes
(
    id           bigint   null,
    entity_id    bigint   null comment '实体类型',
    entity_type  int      null comment '实体类型（1视频2动态3评论',
    user_id      bigint   null,
    created_time datetime null
)
    comment '点赞表';

create table log_manager
(
    id            bigint       null,
    operateUserId bigint       null,
    className     varchar(255) null,
    methodName    varchar(255) null,
    methodParams  text         null,
    returnValue   text         null,
    costTime      double       null,
    operateTime   datetime     null
);

create table log_user
(
    id            bigint       null,
    operateUserId bigint       null,
    className     varchar(255) null,
    methodName    varchar(255) null,
    methodParams  text         null,
    returnValue   text         null,
    costTime      double       null,
    operateTime   datetime     null
);

create table manager
(
    id           bigint                             not null
        primary key,
    username     varchar(50)                        not null,
    password     varchar(255)                       not null,
    status       int                                null,
    created_time datetime default CURRENT_TIMESTAMP null,
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
);

create table post
(
    id           bigint                             not null
        primary key,
    title        varchar(100)                       null,
    content      text                               not null,
    image_url    json                               null,
    tags         json                               null,
    video_id     bigint                             null comment '转发视频id',
    clicks       int      default 0                 null,
    likes        int      default 0                 null,
    favorites    int      default 0                 null,
    shares       int      default 0                 null,
    comments     int      default 0                 null,
    user_id      bigint                             not null comment '作者id',
    visibility   tinyint  default 1                 null comment '0-私密 1-公开',
    created_time datetime default CURRENT_TIMESTAMP null,
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
);

create table share
(
    id           bigint   null,
    entity_id    bigint   null,
    entity_type  int      null,
    user_id      bigint   null,
    created_time datetime null
);

create table user
(
    id              bigint                             not null
        primary key,
    username        varchar(50)                        not null,
    password        varchar(255)                       not null,
    description     text                               null,
    image           varchar(255)                       null,
    following_count int      default 0                 null,
    followers_count int      default 0                 null,
    post_count      int                                null,
    video_count     int                                null,
    coins           int      default 0                 null,
    status          tinyint  default 1                 null comment '0-冻结 1-正常',
    reason          varchar(500)                       null comment '封禁原因',
    is_Record       int                                null comment '是否记录历史',
    created_time    datetime default CURRENT_TIMESTAMP null,
    updated_time    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint username
        unique (username)
);

create table user_following
(
    id           bigint                             not null
        primary key,
    followed_id  bigint                             not null,
    follower_id  bigint                             null,
    is_special   tinyint  default 0                 null comment '0-普通关注 1-特别关注',
    updated_time datetime default CURRENT_TIMESTAMP null,
    created_time datetime default CURRENT_TIMESTAMP null
);

create table video
(
    id            bigint                             not null
        primary key,
    last_id       bigint                             null comment '上一次的id',
    title         varchar(100)                       not null,
    description   text                               null,
    video_url     varchar(255)                       not null,
    cover_url     varchar(255)                       null comment '封面路径',
    sort          int                                null comment '分类',
    tags          json                               null,
    barrages      int                                null comment '弹幕数',
    views         int      default 0                 null,
    likes         int      default 0                 null,
    favorites     int      default 0                 null,
    shares        int      default 0                 null,
    coins         int      default 0                 null,
    comments      int      default 0                 null,
    time          bigint                             null comment '视频时长',
    user_id       bigint                             not null comment '作者id',
    collection_id bigint                             null comment '合集id',
    visibility    tinyint  default 1                 null comment '0-私密 1-公开',
    created_time  datetime default CURRENT_TIMESTAMP null,
    updated_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
);

create table video_coins
(
    id           bigint                             not null
        primary key,
    video_id     bigint                             not null,
    user_id      bigint                             not null,
    created_time datetime default CURRENT_TIMESTAMP null
);

create table video_collection
(
    id           bigint                             not null
        primary key,
    title        varchar(100)                       not null,
    user_id      bigint                             not null,
    video_ids    json                               null,
    is_default   int                                null comment '是否是默认投稿合集',
    visibility   int                                null,
    created_time datetime default CURRENT_TIMESTAMP null,
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
);

create table video_favorites
(
    id           bigint                             not null
        primary key,
    video_id     bigint                             not null,
    folder_id    bigint                             not null,
    user_id      bigint                             not null,
    created_time datetime default CURRENT_TIMESTAMP null,
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
);

create table video_pending
(
    id            bigint                             not null
        primary key,
    last_id       bigint                             null,
    title         varchar(100)                       not null,
    description   text                               null,
    video_url     varchar(255)                       not null,
    cover_url     varchar(255)                       null,
    sort          int                                null comment '分类',
    tags          json                               null,
    barrages      int                                null,
    views         int      default 0                 null,
    likes         int      default 0                 null,
    favorites     int      default 0                 null,
    shares        int      default 0                 null,
    coins         int      default 0                 null,
    comments      int      default 0                 null,
    time          bigint                             null comment '视频时长',
    user_id       bigint                             not null,
    collection_id bigint                             null,
    visibility    tinyint  default 1                 null,
    status        int                                null comment '0不通过1通过2待审核',
    reason        varchar(255)                       null,
    created_time  datetime default CURRENT_TIMESTAMP null,
    updated_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
);


