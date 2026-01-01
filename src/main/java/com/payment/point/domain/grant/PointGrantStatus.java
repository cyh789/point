package com.payment.point.domain.grant;

public enum PointGrantStatus {
    GRANTED,        // 전액 사용 가능
    CANCELED,       // 적립 취소
    USED_ALL,       // 전액 사용됨
    USED_PARTIAL,   // 일부 사용됨
}
