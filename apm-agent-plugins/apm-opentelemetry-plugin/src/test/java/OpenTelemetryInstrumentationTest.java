import co.elastic.apm.agent.AbstractInstrumentationTest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class OpenTelemetryInstrumentationTest  extends AbstractInstrumentationTest {

    @Test
    void simpleGet() throws IOException {

        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("http://httpbin.org/");

        HttpResponse response = httpclient.execute(httpget);

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);

    }
}
