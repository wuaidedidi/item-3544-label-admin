SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS label_admin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE label_admin;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
    nickname VARCHAR(50) DEFAULT '' COMMENT '昵称',
    email VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
    avatar VARCHAR(255) DEFAULT '' COMMENT '头像路径',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(255) DEFAULT '' COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 权限/菜单表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
    name VARCHAR(50) NOT NULL COMMENT '权限名称',
    path VARCHAR(200) DEFAULT '' COMMENT '路由路径',
    component VARCHAR(200) DEFAULT '' COMMENT '组件路径',
    icon VARCHAR(50) DEFAULT '' COMMENT '图标',
    perm_type TINYINT DEFAULT 1 COMMENT '类型: 1-菜单, 2-按钮',
    order_num INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限菜单表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    UNIQUE KEY uk_role_perm (role_id, permission_id),
    KEY idx_role_id (role_id),
    KEY idx_perm_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 文件表
CREATE TABLE IF NOT EXISTS sys_file (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_name VARCHAR(255) NOT NULL COMMENT '存储文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    file_type VARCHAR(100) DEFAULT '' COMMENT '文件MIME类型',
    uploader_id BIGINT NOT NULL COMMENT '上传者ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT DEFAULT NULL COMMENT '操作用户ID',
    username VARCHAR(50) DEFAULT '' COMMENT '操作用户名',
    operation VARCHAR(100) DEFAULT '' COMMENT '操作描述',
    method VARCHAR(200) DEFAULT '' COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) DEFAULT '' COMMENT 'IP地址',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-成功, 0-失败',
    error_msg TEXT COMMENT '错误信息',
    duration BIGINT DEFAULT 0 COMMENT '执行时长(ms)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ========== 初始化数据 ==========

-- 初始化角色
INSERT INTO sys_role (id, role_name, role_code, description, status) VALUES
(1, '管理员', 'ADMIN', '系统管理员，拥有所有权限', 1),
(2, '普通用户', 'USER', '普通用户，拥有基础权限', 1);

-- 初始化用户 (密码由 DataInitializer 在运行时设置)
INSERT INTO sys_user (id, username, password, nickname, email, phone, status) VALUES
(1, 'admin', 'PLACEHOLDER', '系统管理员', 'admin@example.com', '13800000001', 1),
(2, 'user1', 'PLACEHOLDER', '张三', 'zhangsan@example.com', '13800000002', 1),
(3, 'user2', 'PLACEHOLDER', '李四', 'lisi@example.com', '13800000003', 1);

-- 初始化用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 2);

-- 初始化权限/菜单
INSERT INTO sys_permission (id, parent_id, name, path, component, icon, perm_type, order_num, status) VALUES
(1, 0, '仪表盘', '/dashboard', 'Dashboard', 'Odometer', 1, 1, 1),
(2, 0, '用户管理', '/users', 'UserManagement', 'User', 1, 2, 1),
(3, 0, '角色管理', '/roles', 'RoleManagement', 'UserFilled', 1, 3, 1),
(4, 0, '文件管理', '/files', 'FileManagement', 'FolderOpened', 1, 4, 1),
(5, 0, '个人中心', '/profile', 'Profile', 'Setting', 1, 5, 1),
(10, 2, '用户新增', '', '', '', 2, 1, 1),
(11, 2, '用户编辑', '', '', '', 2, 2, 1),
(12, 2, '用户删除', '', '', '', 2, 3, 1),
(13, 2, '用户导出', '', '', '', 2, 4, 1),
(14, 2, '用户导入', '', '', '', 2, 5, 1),
(20, 3, '角色新增', '', '', '', 2, 1, 1),
(21, 3, '角色编辑', '', '', '', 2, 2, 1),
(22, 3, '角色删除', '', '', '', 2, 3, 1),
(30, 4, '文件上传', '', '', '', 2, 1, 1),
(31, 4, '文件下载', '', '', '', 2, 2, 1),
(32, 4, '文件删除', '', '', '', 2, 3, 1);

-- 管理员拥有所有权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
(1, 10), (1, 11), (1, 12), (1, 13), (1, 14),
(1, 20), (1, 21), (1, 22),
(1, 30), (1, 31), (1, 32);

-- 普通用户拥有基础权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(2, 1), (2, 4), (2, 5),
(2, 30), (2, 31);
