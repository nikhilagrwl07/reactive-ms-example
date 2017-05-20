package org.learning.by.example.reactive.microservices.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.learning.by.example.reactive.microservices.exceptions.GetSunriseSunsetException;
import org.learning.by.example.reactive.microservices.model.Location;
import org.learning.by.example.reactive.microservices.model.SunriseSunset;
import org.learning.by.example.reactive.microservices.model.SunriseSunsetResult;
import org.learning.by.example.reactive.microservices.test.tags.UnitTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.SpyBean;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.learning.by.example.reactive.microservices.test.RestServiceHelper.getMonoFromJsonPath;
import static org.learning.by.example.reactive.microservices.test.RestServiceHelper.mockWebClient;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@DisplayName("SunriseSunsetServiceImplTests Unit Tests")
class SunriseSunsetServiceImplTests {

    private static final String STATUS_OK = "OK";
    private static final String BAD_EXCEPTION = "bad exception";
    private static final String SUNRISE_TIME = "12:55:17 PM";
    private static final String SUNSET_TIME = "3:14:28 AM";
    private static final double GOOGLE_LAT = 37.4224082;
    private static final double GOOGLE_LNG = -122.0856086;
    private static final Location GOOGLE_LOCATION = new Location(GOOGLE_LAT, GOOGLE_LNG);
    private static final Mono<Location> GOOGLE_LOCATION_MONO = Mono.just(GOOGLE_LOCATION);
    private static final String GOOGLE_LOCATION_IN_PARAMS = "?lat="+Double.toString(GOOGLE_LAT)+"&lng="+Double.toString(GOOGLE_LNG);

    private static final String JSON_OK = "/json/SunriseSunsetResult_OK.json";
    private static final String JSON_KO = "/json/SunriseSunsetResult_KO.json";
    private static final String JSON_EMPTY = "/json/SunriseSunsetResult_EMPTY.json";
    private static final Mono<SunriseSunsetResult> SUNRISE_SUNSET_OK = getMonoFromJsonPath(JSON_OK, SunriseSunsetResult.class);
    private static final Mono<SunriseSunsetResult> SUNRISE_SUNSET_KO = getMonoFromJsonPath(JSON_KO, SunriseSunsetResult.class);
    private static final Mono<SunriseSunsetResult> SUNRISE_SUNSET_EMPTY = getMonoFromJsonPath(JSON_EMPTY, SunriseSunsetResult.class);
    private static final Mono<SunriseSunsetResult> LOCATION_EXCEPTION = Mono.error(new GetSunriseSunsetException(BAD_EXCEPTION));
    private static final Mono<SunriseSunsetResult> BIG_EXCEPTION = Mono.error(new RuntimeException(BAD_EXCEPTION));


    @Value("${SunriseSunsetServiceImpl.endPoint}")
    private
    String endPoint;

    @SpyBean(SunriseSunsetService.class)
    private SunriseSunsetServiceImpl sunriseSunsetService;

    @Test
    void getBeamTest() {
        assertThat(sunriseSunsetService, is(notNullValue()));
    }

    @Test
    void getMockingWebClientTest() {
        sunriseSunsetService.webClient = mockWebClient(sunriseSunsetService.webClient, SUNRISE_SUNSET_OK);

        SunriseSunsetResult result = Mono.just(endPoint.concat(GOOGLE_LOCATION_IN_PARAMS))
                .transform(sunriseSunsetService::get).block();

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatus(), is(STATUS_OK));
        assertThat(result.getResults().getSunrise(), is(SUNRISE_TIME));
        assertThat(result.getResults().getSunset(), is(SUNSET_TIME));

        reset(sunriseSunsetService.webClient);
    }

    @Test
    void fromLocationTest() {

        doReturn(SUNRISE_SUNSET_OK).when(sunriseSunsetService).get(any());

        SunriseSunset result = GOOGLE_LOCATION_MONO.transform(sunriseSunsetService::fromLocation).block();

        assertThat(result, is(notNullValue()));
        assertThat(result.getSunrise(), is(SUNRISE_TIME));
        assertThat(result.getSunset(), is(SUNSET_TIME));

        verify(sunriseSunsetService, times(1)).fromLocation(any());
        verify(sunriseSunsetService, times(1)).buildUrl(any());
        verify(sunriseSunsetService, times(1)).get(any());
        verify(sunriseSunsetService, times(1)).createResult(any());

        reset(sunriseSunsetService);
    }

    @Test
    void fromLocationKOTest() {

        doReturn(SUNRISE_SUNSET_KO).when(sunriseSunsetService).get(any());

        SunriseSunset result = GOOGLE_LOCATION_MONO.transform(sunriseSunsetService::fromLocation)
                .onErrorResume(throwable -> {
                    assertThat(throwable, instanceOf(GetSunriseSunsetException.class));
                    return Mono.empty();
                }).block();

        assertThat(result, is(nullValue()));

        verify(sunriseSunsetService, times(1)).fromLocation(any());
        verify(sunriseSunsetService, times(1)).buildUrl(any());
        verify(sunriseSunsetService, times(1)).get(any());
        verify(sunriseSunsetService, times(1)).createResult(any());

        reset(sunriseSunsetService);
    }

    @Test
    void fromLocationExceptionTest() {

        doReturn(LOCATION_EXCEPTION).when(sunriseSunsetService).get(any());

        SunriseSunset result = GOOGLE_LOCATION_MONO.transform(sunriseSunsetService::fromLocation)
                .onErrorResume(throwable -> {
                    assertThat(throwable, instanceOf(GetSunriseSunsetException.class));
                    return Mono.empty();
                }).block();

        assertThat(result, is(nullValue()));

        verify(sunriseSunsetService, times(1)).fromLocation(any());
        verify(sunriseSunsetService, times(1)).buildUrl(any());
        verify(sunriseSunsetService, times(1)).get(any());
        verify(sunriseSunsetService, times(1)).createResult(any());

        reset(sunriseSunsetService);
    }

    @Test
    void fromLocationBigExceptionTest() {

        doReturn(BIG_EXCEPTION).when(sunriseSunsetService).get(any());

        SunriseSunset result = GOOGLE_LOCATION_MONO.transform(sunriseSunsetService::fromLocation)
                .onErrorResume(throwable -> {
                    assertThat(throwable, instanceOf(GetSunriseSunsetException.class));
                    return Mono.empty();
                }).block();

        assertThat(result, is(nullValue()));

        verify(sunriseSunsetService, times(1)).fromLocation(any());
        verify(sunriseSunsetService, times(1)).buildUrl(any());
        verify(sunriseSunsetService, times(1)).get(any());
        verify(sunriseSunsetService, times(1)).createResult(any());

        reset(sunriseSunsetService);
    }


    @Test
    void fromLocationErrorTest() {

        doReturn(SUNRISE_SUNSET_EMPTY).when(sunriseSunsetService).get(any());

        SunriseSunset result = GOOGLE_LOCATION_MONO.transform(sunriseSunsetService::fromLocation)
                .onErrorResume(throwable -> {
                    assertThat(throwable, instanceOf(GetSunriseSunsetException.class));
                    return Mono.empty();
                }).block();

        assertThat(result, is(nullValue()));

        verify(sunriseSunsetService, times(1)).fromLocation(any());
        verify(sunriseSunsetService, times(1)).buildUrl(any());
        verify(sunriseSunsetService, times(1)).get(any());
        verify(sunriseSunsetService, times(1)).createResult(any());

        reset(sunriseSunsetService);
    }

    @Test
    void buildUrlTest() {
        String url = GOOGLE_LOCATION_MONO.transform(sunriseSunsetService::buildUrl).block();

        assertThat(url, is(notNullValue()));
        assertThat(url, is(endPoint.concat(GOOGLE_LOCATION_IN_PARAMS)));
    }
}