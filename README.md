## 패키지 & 클래스 분포도 (포인트 적립 기능)

Java 21
springboot 3.5.10
Spring Web Web
H2 Database SQL

Spring Data JPA SQL
Lombok Developer Tools

application.yml
application-test.yml
application-dev.yml

본 프로젝트는 4-Layered Architecture(API / Application / Domain / Infrastructure)를
기반으로 포인트 적립 기능을 구현하였습니다.


### 패키지 구조
'''
com.payment.point
│
├── api                                # Presentation Layer
│   ├── PointCommandController         # 포인트 적립 API
│   └── request
│       └── GrantPointRequest          # 적립 요청 DTO
│
├── application                        # Application Layer
│   └── PointCommandService            # 포인트 적립 유스케이스
│
├── domain                             # Domain Layer
│   ├── grant
│   │   └── PointGrant                 # 포인트 적립 도메인 개념
│   │
│   └── policy
│       ├── PointPolicy                # 포인트 정책 정의
│       └── PointPolicyReader          # 정책 조회 및 변환 책임
│
└── infrastructure                    # Infrastructure Layer
└── persistence
├── grant
│   ├── PointGrantEntity       # 포인트 적립 엔티티
│   └── PointGrantJpaRepository
│
└── policy
├── PointPolicyEntity      # 포인트 정책 엔티티
└── PointPolicyJpaRepository
'''

### devdb 테스트 샘플
$ ./gradlew bootRun --args='--spring.profiles.active=dev'

bash:
$ curl -X POST http://localhost:8080/points/grants \
-H "Content-Type: application/json" \
-d '{
"userId": 1,
"amount": 1000
}'
{"grantId":2,"totalAmount":1000,"remainingAmount":1000,"grantType":null,"expireAt":"2026-12-28T17:10:40.4410619"}
