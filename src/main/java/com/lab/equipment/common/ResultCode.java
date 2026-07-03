package com.lab.equipment.common;

/**
 * 响应码枚举
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),

    // 用户相关 4000-4999
    USER_NOT_EXIST(4001, "用户不存在"),
    USER_PASSWORD_ERROR(4002, "密码错误"),
    USER_ACCOUNT_DISABLED(4003, "账户已被禁用"),
    USER_ALREADY_EXIST(4004, "用户已存在"),
    USER_USERNAME_EXIST(4005, "用户名已存在"),

    // 设备相关 5000-5999
    EQUIPMENT_NOT_EXIST(5001, "设备不存在"),
    EQUIPMENT_NOT_AVAILABLE(5002, "设备不可用"),
    EQUIPMENT_ALREADY_BORROWED(5003, "设备已被借用"),
    EQUIPMENT_EQUIPMENT_NO_EXIST(5004, "设备编号已存在"),

    // 预约相关 6000-6999
    RESERVATION_NOT_EXIST(6001, "预约不存在"),
    RESERVATION_CONFLICT(6002, "预约时间冲突"),
    RESERVATION_NOT_APPROVED(6003, "预约未通过审核"),
    RESERVATION_ALREADY_APPROVED(6004, "预约已审核"),

    // 借用相关 7000-7999
    BORROW_RECORD_NOT_EXIST(7001, "借用记录不存在"),
    BORROW_OVERDUE(7002, "借用已逾期"),
    BORROW_LIMIT_EXCEEDED(7003, "超过借用限制"),

    // 报修相关 8000-8999
    REPAIR_REQUEST_NOT_EXIST(8001, "报修记录不存在"),
    REPAIR_REQUEST_PROCESSED(8002, "报修记录已处理"),

    // 权限相关 9000-9999
    UNAUTHORIZED(9001, "未授权"),
    FORBIDDEN(9002, "无权访问"),
    TOKEN_INVALID(9003, "Token无效"),
    TOKEN_EXPIRED(9004, "Token已过期");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
