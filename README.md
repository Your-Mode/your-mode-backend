# Your Mode Backend

Your Mode Backend는 패션 관련 콘텐츠 관리 및 사용자 인증을 위한 Spring Boot 기반 백엔드 애플리케이션입니다.

## 🚀 기술 스택

- **Framework**: Spring Boot 3.2.4
- **Language**: Java 17
- **Build Tool**: Gradle
- **Database**: MySQL
- **Cache**: Redis
- **Security**: Spring Security, JWT
- **Cloud Storage**: AWS S3
- **SMS Service**: CoolSMS
- **API Documentation**: Swagger (SpringDoc OpenAPI)
- **Deployment**: AWS CodeDeploy

## 📁 프로젝트 구조

```
src/main/java/com/yourmode/yourmodebackend/
├── domain/                    # 도메인별 패키지
│   ├── content/              # 콘텐츠 관리
│   │   ├── controller/       
│   │   ├── dto/             
│   │   ├── entity/          
│   │   ├── repository/      
│   │   ├── service/         
│   │   └── status/          
│   ├── request/             # 콘텐츠 요청 관리
│   │   ├── controller/      
│   │   ├── dto/           
│   │   ├── entity/         
│   │   ├── repository/    
│   │   ├── service/        
│   │   └── status/       
│   ├── survey/             # 설문조사 관리
│   │   ├── controller/    
│   │   ├── dto/          
│   │   ├── entity/       
│   │   ├── repository/    
│   │   ├── service/       
│   │   └── status/        
│   └── user/              # 사용자 관리
│       ├── controller/    
│       ├── dto/          
│       ├── entity/       
│       ├── enums/        
│       ├── redis/       
│       ├── repository/   
│       ├── service/      
│       ├── status/       
│       └── util/        
└── global/               # 전역 설정
    ├── common/           # 공통 클래스
    │   ├── base/        # 기본 응답 클래스
    │   └── exception/   # 예외 처리
    └── config/          # 설정 클래스
        ├── security/    # 보안 설정
        └── SwaggerConfig.java
```
