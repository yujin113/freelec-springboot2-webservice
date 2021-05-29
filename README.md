# 스프링 부트와 AWS로 혼자 구현하는 웹 서비스       

--------
<br />

## 스프링 부트에서 JPA로 데이터베이스 다루기
- JPA에서 Repository는 보통 MyBatis에서 Dao로 불리는 DB Layer 접근자이다.
- JPA에서 Repository라고 부르며 인터페이스로 생성한다. 단순히 인터페이스 생성 후, JpaRepository<Entity 클래스, PK 타입> 상속하면 기본적인 CRUD 메소드가 자동으로 생성된다.
- @Repository 추가할 필요도 X
- Entity 클래스와 기본 Entity Repository는 함께 위치해야 한다. Entity 클래스는 기본 Repository 없이는 제대로 역할을 못 함
- 플젝 규모가 커져 도메인 별로 분리해야 한다면 Entity 클래스 & 기본 Repository는 도메인 패키지 안에서 함께 관리
  <br /><br />
## Spring 웹 계층
1. Web Layer
    - 컨트롤러 등의 뷰 템플릿 영역
    - 외부 요청과 응답에 대한 전반적인 영역
2. Service Layer
    - @Service 에 사용되는 서비스 영역
    - 일반적으로 Controller와 Dao의 중간 영역에서 사용
    - @Transactional 이 사용되는 영역
3. Repository Layer
    - Database와 같이 데이터 저장소에 접근하는 영역
    - 기존의 Dao 영역
4. Dtos
    - 계층 간 데이터 교환을 위한 객체들의 영역
    - ex : 뷰 템플릿 엔진에서 사용될 객체나 Repository Layer에서 결과로 넘겨준 객체 등
5. Domain Model
    - @Entity 가 사용된 영역
    - 비지니스 처리를 담당해야 할 곳
    - ex : 택시 앱이라고 하면 배차, 탑승, 요금 등이 모두 도메인이 될 수 있음
    - 무조건 database의 테이블과 관계가 있어야 하는 것은 아님. VO처럼 값 객체들도 이 영역에 해당
    
````
Service에서 비지니스 로직 처리하는 것 아님❗️
Service는 트랜잭션, 도메인 간 순서 보장의 역할만 할 뿐.
비지니스 처리는 Domain에서 담당.
````
````
Entity 클래스를 Request/Response 클래스로 사용해서는 안 됨.
Entity 클래스는 database와 맞닿은 핵심 클래스❗️
Dto 클래스를 사용해야 함. Request와 Response용 Dto는 View를 위한 클래스라 자주 변경이 필요함.
````
````
꼭 Entity 클래스와 Controller에서 쓸 Dto는 분리해서 사용해야 함.
````
View Resolver : URL 요청의 결과를 전달할 타입과 값을 지정하는 관리자 격     
<br /><br />

## OAuth 2.0으로 로그인 기능 구현
스프링 부트에서는 properties의 이름을 application-xxx.properties로 만들면 xxx라는 이름의 profile이 생성    
→ profile=xxx 로 호출하면 해당 properties의 설정들을 가져올 수 있음
1. User 클래스 생성
2. 각 사용자 권한을 관리할 Enum 클래스인 Role을 생성   
   → Spring Security에서는 권한 코드에 항상 ROLE_이 앞에 있어야만 함

3. User의 CRUD를 책임질 UserRepository도 생성
4. 스프링 시큐리티 설정
    1) build.gradle에 의존성 추가
    2) SecurityConfig 클래스 생성
    3) CustomOAuth2UserService 클래스 생성   
        : 구글 로그인 이후 가져온 사용자의 정보들을 기반으로 가입 및 정보 수정, 세션 저장 등의 기능 지원
    4) Dto인 OAuthAttributes 클래스를 생성
    5) Dto인 SessionUser 클래스 생성     
       : 세션에 사용자 정보 저장하기 위한 클래스   
       : User 클래스 사용하면 안됨    
        → User 클래스에 직렬화를 구현하지 않았다는 에러가 뜸   
        → User 클래스가 엔티티이기 때문에 언제 다른 엔티티와 관계가 형성될지 모름   
        → 자식 엔티티를 갖고 있다면 직렬화 대상에 자식들까지 포함되어 성능 이슈, 부수 효과가 발생   
        → 그래서 직렬화 기능을 가진 Session Dto를 추가로 만드는 것
5. 어노테이션 기반으로 개선   
    httpSession.getAttribute("user")로 세션값을 가져오는 부분 → 메소드 인자로 세션값을 바로 받을 수 있도록 변경   
    어느 컨트롤러든지 @LoginUser만 사용하면 세션 정보를 가져올 수 있게 함
    
6. 세션 저장소로 데이터베이스 사용하기  
    세션이 내장 톰캣의 메모리에 저장되기 때문에 애플리케이션 실행 시 초기화됨
   
    1) 톰캣 세션 사용   
        톰캣에 세션이 저장되므로 2대 이상의 WAS가 구동되는 환경에서는 톰캣들 간의 세션 공유를 위한 추가 설정 필요
    2) MySQL 같은 데이터베이스를 세션 저장소로 사용   
        여러 WAS 간의 공용 세션 사용할 수 있는 가장 쉬운 방법   
       로그인 요청마다 DB IO가 발생하여 성능상 이슈가 발생할 수 있음 → 로그인 요청 많이 없는 백오피스, 사내 시스템 용도에서 사용
    3) Redis, Memcached와 같은 메모리 DB를 세션 저장소로 사용  
        B2C 서비스에서 가장 많이 사용하는 방식  
    
    → 두 번째 방식 선택! 설정이 간단하고 비용 절감을 위해