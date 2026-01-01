package com.payment.point.domain.grant;

public enum PointGrantType {
    AUTO,           // 정상 적립
    ADMIN_GRANTED,  // 관리자 적립
    EXPIRED_GRANTED // 만료되어 신규 적립처리
}
