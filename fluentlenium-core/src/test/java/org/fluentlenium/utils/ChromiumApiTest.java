package org.fluentlenium.utils;

import com.google.common.collect.ImmutableMap;
import org.fluentlenium.utils.chromium.ChromiumApi;
import org.fluentlenium.utils.chromium.ChromiumApiNotSupportedException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.Command;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

/**
 * Unit test for {@link ChromiumApi}.
 */
public class ChromiumApiTest {

    private ChromiumApi chromiumApi;
    private RemoteWebDriver remoteWebDriver;
    @Mock
    private CommandExecutor executor;
    @Mock
    private SessionId sessionId;

    @Before
    public void before() throws IOException {
        MockitoAnnotations.initMocks(this);
        when(sessionId.toString()).thenReturn("test");
        remoteWebDriver = makeDriver("chrome");
        chromiumApi = new ChromiumApi(remoteWebDriver);
    }

    @Test
    public void shouldReturnSessionIdWhenSendCommandAndGetResponseIsCalled() {
        Response response = chromiumApi.sendCommandAndGetResponse("", ImmutableMap.of());
        assertThat(sessionId).hasToString(response.getSessionId());
    }

    @Test
    public void shouldInvokeExecuteTwiceWhenDriverIsInstantiatedAndSendCommandIsCalled() throws IOException {
        chromiumApi.sendCommand("", ImmutableMap.of());
        verify(executor, times(2)).execute(any(Command.class));
    }

    @Test
    public void shouldThrowAnExceptionIfWebDriverInstanceIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new ChromiumApi(null))
                .withMessage("WebDriver instance must not be null");
    }

    @Test
    public void shouldThrowAnExceptionIfBrowserOtherThanChrome() throws IOException {
        remoteWebDriver = makeDriver("firefox");
        assertThatExceptionOfType(ChromiumApiNotSupportedException.class)
                .isThrownBy(() -> new ChromiumApi(remoteWebDriver))
                .withMessage("API currently supports only Chrome browser");
    }

    private RemoteWebDriver makeDriver(String browserName) throws IOException {
        DesiredCapabilities cap = new DesiredCapabilities();
        cap.setBrowserName(browserName);
        Response response = new Response(sessionId);
        response.setValue(cap.asMap());
        when(executor.execute(any(Command.class))).thenReturn(response);
        return new RemoteWebDriver(executor, cap);
    }
}
