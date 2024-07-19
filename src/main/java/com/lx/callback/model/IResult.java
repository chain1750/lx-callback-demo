package com.lx.callback.model;

import lombok.Data;

/**
 * 接口结果
 *
 * @author chenhaizhuang
 */
@Data
public class IResult<T> {

    /**
     * 成功码
     */
    public static final Integer SUCCESS_CODE = 0;

    /**
     * 结果码
     */
    private Integer code;

    /**
     * 结果信息
     */
    private String msg;

    /**
     * 接口数据
     */
    private T data;

    /**
     * 是否成功
     *
     * @return boolean
     */
    public boolean isSuccess() {
        return SUCCESS_CODE.equals(getCode());
    }
}
