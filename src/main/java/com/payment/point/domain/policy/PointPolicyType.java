package com.payment.point.domain.policy;

public enum PointPolicyType {

    MAX_GRANT_AMOUNT,      // 1회 적립 한도
    MAX_POINT_BALANCE,     // 개인별 최대 보유 포인트
    DEFAULT_EXPIRE_DAYS,   // 기본 만료일
    MIN_EXPIRE_DAYS,       // 최소 만료일
    MAX_EXPIRE_DAYS        // 최대 만료일
}
