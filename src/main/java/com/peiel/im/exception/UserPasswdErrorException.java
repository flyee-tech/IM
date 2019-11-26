package com.peiel.im.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Peiel
 * @version V1.0
 * @date 2019-11-25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserPasswdErrorException extends RuntimeException {
    private int code;

    public UserPasswdErrorException(int code, String message) {
        super(message);
        this.setCode(code);
    }
}
