
---

# Point Service (Payments Domain)

## 1. 프로젝트 개요

```
* 필요 기능 : 적립, 적립취소, 사용, 사용취소
    1. 적립
        1. 1회 적립가능 포인트는 1포인트 이상, 10만포인트 이하로 가능하며 1회 최대 적립가능 포인트는 하드코딩이 아닌 방법으로 제어할수 있어야 한다.
        2. 개인별로 보유 가능한 무료포인트의 최대금액 제한이 존재하며, 하드코딩이 아닌 별도의 방법으로 변경할 수 있어야 한다.
        3. 특정 시점에 적립된 포인트는 1원단위까지 어떤 주문에서 사용되었는지 추적할수 있어야 한다.
        4. 포인트 적립은 관리자가 수기로 지급할 수 있으며, 수기지급한 포인트는 다른 적립과 구분되어 식별할 수 있어야 한다.
        5. 모든 포인트는 만료일이 존재하며, 최소 1일이상 최대 5년 미만의 만료일을 부여할 수 있다. (기본 365일)
    2. 적립 취소
        1. 특정 적립행위에서 적립한 금액만큼 취소 가능하며, 적립한 금액중 일부가 사용된 경우라면 적립 취소 될 수 없다.
    3. 사용
        1. 주문시에만 포인트를 사용할 수 있다고 가정한다.
        2. 포인트 사용시에는 주문번호를 함께 기록하여 어떤 주문에서 얼마의 포인트를 사용했는지 식별할 수 있어야 한다.
        3. 포인트 사용시에는 관리자가 수기 지급한 포인트가 우선 사용되어야 하며, 만료일이 짧게 남은 순서로 사용해야 한다.
    4. 사용 취소
        1. 사용한 금액중 전제 또는 일부를 사용취소 할수 있다.
        2. 사용취소 시점에 이미 만료된 포인를 사용취소 해야 한다면 그 금액만큼 신규적립 처리 한다.
```

---


## 2. 기술 스택

```
Java 21
springboot 3.5.10
Spring Web Web
H2 Database SQL

Spring Data JPA SQL
Lombok Developer Tools
```

---

## 3. 패키지 구조

```
com.payment.point
├─ api
│  ├─ controller
│  │  ├─ PointGrantController
│  │  └─ PointUsageController
│  ├─ request
│  │  ├─ GrantPointRequest
│  │  ├─ CancelGrantRequest
│  │  ├─ UsePointRequest
│  │  └─ CancelUsageRequest
│  └─ response
│     ├─ PointGrantResponse
│     └─ PointUsageResponse
│
├─ application
│  └─ service
│     └── PointService
│
├─ domain
│  ├─ allocation
│  │  ├─ PointAllocation
│  │  └─ PointUsageAllocation
│  ├─ grant
│  │  ├─ PointGrant
│  │  ├─ PointGrantStatus
│  │  └─ PointGrantType
│  ├─ policy
│  │  ├─ PointPolicyReader
│  │  ├─ PointPolicyType
│  │  └─ PointPolicyValidator
│  └─ usage
│     ├─ PointUsage
│     └─ PointUsageStatus
│
└─ infrastructure
   └─ persistence
      ├─ allocation
      │  ├── PointAllocationEntity
      │  ├── PointAllocationRepository
      │  ├── PointUsageAllocationEntity
      │  └── PointUsageAllocationRepository
      ├─ grant
      │  ├── PointGrantEntity
      │  └── PointGrantRepository
      ├─ policy
      │  ├── PointPolicyEntity
      │  └── PointPolicyRepository
      └─ usage
         ├── PointUsageEntity
         └── PointUsageRepository
```

---
