package org.example.studentlogincrud.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("admin")
public class Admin {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 管理员登录名（业务上即管理员姓名）。 */
    private String username;

    /** 数据库存储的密码，可以是 BCrypt 哈希。 */
    private String password;
}
