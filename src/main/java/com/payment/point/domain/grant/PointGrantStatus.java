package com.payment.point.domain.grant;

public enum PointGrantStatus {

    GRANTED,        // 정상 적립
    CANCELED,       // 적립 취소
    EXPIRED,        // 만료
    USED_ALL,       // 전액 사용됨
    USED_PARTIAL    // 일부 사용됨
}
