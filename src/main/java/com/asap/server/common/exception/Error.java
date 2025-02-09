package com.asap.server.common.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Error {

    /**
     * 400 BAD REQUEST
     **/
    INVALID_MEETING_URL_EXCEPTION(HttpStatus.BAD_REQUEST, "유효하지 않는 URL 입니다."),
    VALIDATION_REQUEST_MISSING_EXCEPTION(HttpStatus.BAD_REQUEST, "요청값이 유효하지 않습니다."),
    ILLEGAL_ARGUMENT_EXCEPTION(HttpStatus.BAD_REQUEST, "유효하지 않은 값이 입력되었습니다."),
    DUPLICATED_DATE_EXCEPTION(HttpStatus.BAD_REQUEST, "중복 입력된 날짜가 있습니다."),
    DUPLICATED_TIME_EXCEPTION(HttpStatus.BAD_REQUEST, "중복 입력된 시간이 있습니다."),
    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "입력한 시간이 회의 가능 일시에 해당하지 않습니다."),
    INVALID_JSON_INPUT_EXCEPTION(HttpStatus.BAD_REQUEST, "입력 형식이 맞지 않습니다."),
    INVALID_TOKEN_EXCEPTION(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰을 입력했습니다."),
    BAD_REQUEST_EXCEPTION(HttpStatus.BAD_REQUEST, "잘못된 요청이 있습니다."),
    INVALID_DATE_FORMAT_EXCEPTION(HttpStatus.BAD_REQUEST, "유요하지 않은 날짜를 입력했습니다."),
    // user
    USERNAME_NOT_NULL_EXCEPTION(HttpStatus.BAD_REQUEST, "사용자 이름에는 null이 들어올 수 없습니다."),
    USERNAME_NOT_BLANK_EXCEPTION(HttpStatus.BAD_REQUEST, "사용자 이름에는 빈 값이 들어올 수 없습니다."),
    USERNAME_TOO_LONG_EXCEPTION(HttpStatus.BAD_REQUEST, "사용자 이름의 최대 입력 길이(8자)를 초과했습니다."),
    /**
     * 401 UNAUTHORIZED
     **/
    TOKEN_NOT_CONTAINED_EXCEPTION(HttpStatus.UNAUTHORIZED, "방장의 토큰이 필요합니다."),
    EXPIRE_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다. 다시 로그인 해주세요."),
    TOKEN_TIME_EXPIRED_EXCEPTION(HttpStatus.UNAUTHORIZED, "토큰이 유효하지 않습니다."),
    INVALID_MEETING_HOST_EXCEPTION(HttpStatus.UNAUTHORIZED, "해당 유저는 해당 방의 방장이 아닙니다."),
    INVALID_HOST_ID_PASSWORD_EXCEPTION(HttpStatus.UNAUTHORIZED, "유효하지 않은 사용자 이름 또는 비밀번호입니다."),
    /**
     * 403 FORBIDDEN
     */
    HOST_MEETING_TIME_NOT_PROVIDED(HttpStatus.FORBIDDEN, "회의 가능 시간이 입력되지 않았습니다."),
    MEETING_NOT_CONFIRMED_EXCEPTION(HttpStatus.FORBIDDEN, "확정되지 않은 회의입니다."),
    /**
     * 404 NOT FOUND
     */
    URI_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "지원하지 않는 URL 입니다."),
    USER_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "해당 유저는 존재하지 않습니다."),
    MEETING_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "해당 회의는 존재하지 않습니다."),
    MEETING_TIME_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "해당 유저의 회의 시간이 존재하지 않습니다."),
    TIME_BLOCK_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "해당 회의의 가능 시간을 입력한 유저가 없습니다."),
    AVAILABLE_DATE_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "회의 가능 일자가 존재하지 않습니다."),
    PREFER_TIME_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "회의 선호 시간대가 존재하지 않습니다."),

    /**
     * 405 METHOD_NOT_ALLOWED
     */
    METHOD_NOT_ALLOWED_EXCEPTION(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 메소드 입니다."),
    /**
     * 409 CONFLICT
     */
    MEETING_VALIDATION_FAILED_EXCEPTION(HttpStatus.CONFLICT, "이미 확정된 회의입니다."),
    HOST_TIME_EXIST_EXCEPTION(HttpStatus.CONFLICT, "이미 가능 시간 입력을 마쳤습니다."),
    /*
    * 429 TOO MANY REQUEST
     */
    TOO_MANY_REQUEST_EXCEPTION(HttpStatus.TOO_MANY_REQUESTS, "중복된 요청입니다."),
    /**
     * 500 INTERNAL SERVER ERROR
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류"),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}
