package dev.rnd.rest.client;

import java.time.Duration;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
@PropertySources({
  @PropertySource("classpath:application.properties"),
  @PropertySource(value = "classpath:application-${spring.profiles.active}.properties", ignoreResourceNotFound = true)
})
public class AppConfig {
	
	@Bean
	RestClient restClient() {
		return new RestClient();
	}

	@Bean
	RestTemplate restTemplate(@Value("${rest.server.address}") String serverBaseUrl, 
			@Value("${tls.enabled}") boolean tlsEnabled, @Value("${tls.trust-store}") String trustStoreFile,
			@Value("${tls.trust-store-password}") String trustStorePassword) throws Exception {
		
		if (tlsEnabled) {
		  System.setProperty("javax.net.ssl.trustStore", trustStoreFile);
		  System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
		}
		
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
	  connectionManager.setMaxTotal(100);
	  connectionManager.setDefaultMaxPerRoute(100);

	  HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
	      .setConnectionManager(connectionManager);
	  
//	  if (tlsEnabled) {
//		  SSLContext sslContext = new SSLContextBuilder()
//		  		.loadTrustMaterial(new File(trustStoreFile), trustStorePassword.toCharArray())
//		  		.build();
//		  SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);
//		  httpClientBuilder.setSSLSocketFactory(sslSocketFactory);		  
//	  }
	  
	  HttpClient httpClient = httpClientBuilder.build();
	  return new RestTemplateBuilder().rootUri(serverBaseUrl)
	      .setConnectTimeout(Duration.ofMillis(10000))
	      .setReadTimeout(Duration.ofMillis(10000))
	      .messageConverters(new StringHttpMessageConverter(), new MappingJackson2HttpMessageConverter())
	      .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
	      .build();
	}

}
