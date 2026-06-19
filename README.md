# spring-aop-demo

A proof-of-concept showing how to build a **general, cross-cutting, centralized exception handler** with Spring AOP — one that is **not tied to the HTTP layer**.

Unlike `@ControllerAdvice`/`@ExceptionHandler` (Spring) or JAX-RS `ExceptionMapper`, which only handle exceptions at the web boundary, this approach intercepts exceptions thrown by **any** Spring-managed bean (service, scheduled job, event consumer, etc.) and dispatches them to centralized handler methods. Handlers can return **any type**: `void`, a concrete fallback value, or a reactive type (`Mono`/`Flux`).

## The idea

Handlers live in one place, annotated with custom annotations:

```java
@GlobalExceptionHandler
public class GlobalExceptionHandlers {

    @Handles(NotificationFailedException.class)        // void: just log / ack
    public void onNotificationFailed(NotificationFailedException ex) { ... }

    @Handles(OrderNotFoundException.class)             // value: typed fallback
    public Order onOrderNotFound(OrderNotFoundException ex) { ... }

    @Handles(ReportGenerationException.class)          // reactive: Mono fallback
    public Mono<Report> onReportFailedReactive(ReportGenerationException ex) { ... }
}
```

Services simply throw — no local `try/catch`:

```java
public Order findOrder(String id) {
    if (id == null || id.startsWith("missing")) {
        throw new OrderNotFoundException("Order not found: " + id);
    }
    return new Order(id, "Anna Kovacs", new BigDecimal("250.00"), false);
}
```

An `@Around` aspect intercepts the call, selects the most specific `@Handles` handler for the thrown exception type, invokes it, and adapts the result to the method's declared return type. The caller sees a type-correct result (or a silent void return) instead of an exception.

## How it works

```
caller -> Spring AOP proxy -> ExceptionHandlingAspect (@Around)
                                   |
                                   |-- proceed() throws / Mono emits error
                                   v
                          ExceptionHandlerRegistry.resolve(exceptionType)
                                   |  (walks the superclass chain -> most specific @Handles)
                                   v
                          GlobalExceptionHandlers.<handler>(ex)
                                   |
                                   v
                  adapt result to return type: void -> null,
                  value -> fallback object, Mono/Flux -> reactive fallback
```

Key behaviors:
- **Startup:** `ExceptionHandlerRegistry` scans every `@GlobalExceptionHandler` bean and maps `exceptionType -> handler method`.
- **Synchronous methods:** `proceed()` runs inside a `try/catch`; on exception the handler is resolved and invoked, and the result is adapted to the return type.
- **Reactive methods (`Mono`/`Flux`):** the publisher is wrapped with `onErrorResume`, so an error *signal* (not just a thrown exception) is dispatched to the handler.
- **Most-specific match:** resolution walks the exception's superclass chain and picks the closest registered handler.
- **Selective handling:** an exception with no matching `@Handles` handler is rethrown unchanged — unexpected errors are never silently swallowed.
- **Pass-through:** calls that don't fail return their original result untouched.

## Two branches

The project contains two parallel implementations so you can compare styles:

- `baseline/` — the traditional approach: every service handles `DemoException` locally with repetitive `try/catch` blocks.
- `poc/` — the same functionality with no local handling: services throw specific exceptions and the central aspect handles everything.

## Project layout

```
src/main/java/org/darvasr/springaopdemo/
├── model/                     Order, Receipt, StockLevel, Report, Summary (records with a `fallback` flag)
├── baseline/                  DemoException + 5 services with local try/catch
├── poc/
│   ├── exception/             PocException + 6 specific subtypes
│   ├── annotation/            @GlobalExceptionHandler, @Handles
│   ├── support/               FallbackFactory, ExceptionHandlerRegistry (dispatcher)
│   ├── handler/               GlobalExceptionHandlers (void/value/reactive handler examples)
│   ├── aspect/                ExceptionHandlingAspect (@Around)
│   └── service/               5 services that throw specific exceptions (no try/catch) + a reactive method
├── web/                       PocController, BaselineController (REST endpoints)
├── DemoRunner.java            Optional boot-time demo (disabled by default)
└── SpringAopDemoApplication.java
```

## Requirements

- **JDK 17+** to launch Gradle, and a **JDK 25** toolchain to compile (configured in `build.gradle`).
- Spring Boot 4.1.0, Spring Framework 7, Project Reactor, AspectJ weaver.

> Note: Spring Boot 4 removed `spring-boot-starter-aop`. Proxy-based `@Aspect`/`@Around` support comes from `org.aspectj:aspectjweaver` on the classpath; Spring's AOP auto-configuration activates from it.

## Build & run

On Windows, point Gradle at a JDK 17+ launcher (the project still compiles with the Java 25 toolchain):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'

# run tests
.\gradlew.bat test

# run the application (Tomcat on port 8080)
.\gradlew.bat bootRun
```

## REST endpoints

All endpoints are `GET` for easy testing in a browser or with `curl`. Each takes a "trigger" input to force the error path; any other input takes the success/pass-through path. The POC endpoints are aspect-handled; the baseline endpoints use local `try/catch`.

Base paths: `/api/poc/**` and `/api/baseline/**`.

| Endpoint | Service method | Trigger input → result |
|---|---|---|
| `GET /api/poc/orders/{id}` | `findOrder` | `missing-*` → fallback `Order` |
| `GET /api/poc/orders/create?customer=` | `createOrder` | blank customer → fallback `Order` |
| `GET /api/poc/orders/{id}/cancel` | `cancelOrder` (void) | `locked-*` → handled, logged, `{"status":"ok"}` |
| `GET /api/poc/payments/charge?account=&amount=` | `charge` | `amount<=0` → fallback `Receipt` |
| `GET /api/poc/payments/refund?paymentId=` | `refund` (void) | `settled-*` → handled, logged |
| `GET /api/poc/inventory/reserve?sku=&quantity=` | `reserve` (void) | `quantity<=0` → handled, logged |
| `GET /api/poc/inventory/release?sku=` | `release` (void) | blank sku → handled, logged |
| `GET /api/poc/inventory/stock/{sku}` | `checkStock` | `unknown-*` → fallback `StockLevel` |
| `GET /api/poc/notifications/email?to=` | `sendEmail` (void) | no `@` → handled, logged |
| `GET /api/poc/reports/generate?name=` | `generate` | pass-through (success) |
| `GET /api/poc/reports/summarize?name=` | `summarize` | pass-through (success) |
| `GET /api/poc/reports/reactive?name=` | `generateReactive` | blank name → reactive fallback `Report` |

The `/api/baseline/**` paths mirror these (except the reactive endpoint) for side-by-side comparison.

Examples:

```bash
# value fallback
curl http://localhost:8080/api/poc/orders/missing-1
# {"id":"FALLBACK","customer":"UNKNOWN","total":0,"fallback":true}

# pass-through
curl http://localhost:8080/api/poc/orders/ORD-9
# {"id":"ORD-9","customer":"Anna Kovacs","total":250.00,"fallback":false}

# void (handled, swallowed)
curl http://localhost:8080/api/poc/orders/locked-1/cancel
# {"status":"ok","operation":"cancelOrder","note":"void method completed; check logs"}

# reactive fallback
curl "http://localhost:8080/api/poc/reports/reactive?name="
# {"name":"FALLBACK","rows":[],"fallback":true}
```

## Boot-time demo (optional)

`DemoRunner` runs the baseline and POC branches side by side at startup. It is **disabled by default**. Enable it with:

```properties
# application.properties
demo.runner.enabled=true
```

or pass `--demo.runner.enabled=true` as a CLI argument.

## Tests

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
.\gradlew.bat test
```

Coverage includes:
- `FallbackFactory` unit tests + jqwik property-based tests (type compatibility, determinism).
- `ExceptionHandlerRegistry` most-specific resolution tests.
- AOP integration tests (`@SpringBootTest`): value fallback, void swallowing, pass-through, and no-handler rethrow.
- Reactive `StepVerifier` test for the `Mono` fallback path.
- Baseline ↔ POC behavioral equivalence test.

## Spec

The design, requirements, and task breakdown live under `.kiro/specs/aop-exception-handling/` (these documents are written in Hungarian).

## Quarkus equivalent

The same pattern ports 1:1 to Quarkus: replace the Spring AOP aspect with a CDI interceptor (`@AroundInvoke`) bound by an interceptor-binding annotation (instead of a package pointcut), keep the same `@GlobalExceptionHandler`/`@Handles` registry (built on a `StartupEvent`), and use Mutiny `Uni`/`Multi` in place of Reactor `Mono`/`Flux`.
