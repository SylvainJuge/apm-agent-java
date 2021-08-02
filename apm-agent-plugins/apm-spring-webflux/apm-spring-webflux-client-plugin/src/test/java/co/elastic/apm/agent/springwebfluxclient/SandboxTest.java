package co.elastic.apm.agent.springwebfluxclient;

import co.elastic.apm.agent.AbstractInstrumentationTest;
import co.elastic.apm.agent.reactor.TracedSubscriber;
import co.elastic.apm.agent.springwebflux.testapp.GreetingWebClient;
import co.elastic.apm.agent.springwebflux.testapp.WebFluxApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

public  class SandboxTest extends AbstractInstrumentationTest {

    protected static WebFluxApplication.App app;
    protected GreetingWebClient client;

    @BeforeAll
    static void startApp() {
        app = WebFluxApplication.run(-1, "netty", true);
    }

    @AfterAll
    static void stopApp() {
        app.close();
    }

    @BeforeEach
    void beforeEach() {
        assertThat(reporter.getTransactions()).isEmpty();
        client = app.clientBuilder()
            // TODO : make test apply on all other implementations
            .httpConnector(GreetingWebClient.HttpConnector.ReactorNetty)
            .build();
    }

    @AfterEach
    void afterEach() {
        flushGcExpiry(TracedSubscriber.getContextMap(), 1);
//        flushGcExpiry(TransactionAwareSubscriber.getTransactionMap(), 3);
    }

    // org.springframework.web.reactive.function.client.DefaultWebClient.DefaultWebClient
    // constructor 1st argument is org.springframework.web.reactive.function.client.ExchangeFunction
    // instrument the exchange function implementation (apply instrumentation at runtime)
    // 	method to wrap mono for request execution : Mono<ClientResponse> exchange(ClientRequest request)

    // two implementations for client-side
    // reactor (netty-based)
    // jetty (jetty-based)

    @Test
    void hello() {
        StepVerifier.create(client.getHelloMono())
            .expectNext("Hello, Spring!")
            .verifyComplete();
    }
}
