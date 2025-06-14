package myapp;

import config.TestKafkaConfiguration;
import service.dto.EmailRequestDto;
import service.dto.EmailResponseDto;
import service.dto.UserEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Интеграционные тесты для Notification Service
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(
        partitions = 1,
        topics = {"user-events"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:0", "port=0"}
)
@TestPropertySource(properties = {
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=test-group",
        "spring.mail.host=localhost",
        "spring.mail.port=3025",
        "spring.mail.username=test@example.com",
        "spring.mail.password=password",
        "notification.topics.user-events=user-events",
        "notification.site-name=TestSite",
        "logging.level.org.springframework.kafka=INFO",
        "logging.level.root=INFO"
})
@Import(TestKafkaConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NotificationServiceIntegrationTest {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("test@example.com", "password"));

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private KafkaTemplate<String, UserEventDto> testKafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/notifications";
        greenMail.reset();
        greenMail.start();
    }

    @Test
    void shouldSendEmailWhenUserCreatedEventReceived() throws Exception {
        // Given
        String testEmail = "newuser@example.com";
        UserEventDto userEvent = new UserEventDto(
                testEmail,
                UserEventDto.EventType.USER_CREATED,
                LocalDateTime.now()
        );

        // When
        testKafkaTemplate.send("user-events", userEvent).get(10, TimeUnit.SECONDS);

        // Then
        await().atMost(30, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
                    assertThat(receivedMessages).hasSize(1);

                    MimeMessage message = receivedMessages[0];
                    assertThat(message.getAllRecipients()[0].toString()).isEqualTo(testEmail);
                    assertThat(message.getSubject()).isEqualTo("Добро пожаловать!");
                    assertThat(message.getContent().toString()).contains("Ваш аккаунт на сайте TestSite был успешно создан");
                });
    }

    @Test
    void shouldSendEmailWhenUserDeletedEventReceived() throws Exception {
        // Given
        String testEmail = "deleteduser@example.com";
        UserEventDto userEvent = new UserEventDto(
                testEmail,
                UserEventDto.EventType.USER_DELETED,
                LocalDateTime.now()
        );

        // When
        testKafkaTemplate.send("user-events", userEvent).get(10, TimeUnit.SECONDS);

        // Then
        await().atMost(30, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
                    assertThat(receivedMessages).hasSize(1);

                    MimeMessage message = receivedMessages[0];
                    assertThat(message.getAllRecipients()[0].toString()).isEqualTo(testEmail);
                    assertThat(message.getSubject()).isEqualTo("Удаление аккаунта");
                    assertThat(message.getContent().toString()).contains("Ваш аккаунт был удалён");
                });
    }

    @Test
    void shouldSendEmailViaRestApiForUserCreated() {
        // Given
        String testEmail = "apiuser@example.com";
        EmailRequestDto request = new EmailRequestDto(testEmail, UserEventDto.EventType.USER_CREATED);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmailRequestDto> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<EmailResponseDto> response = restTemplate.postForEntity(
                baseUrl + "/email",
                entity,
                EmailResponseDto.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getEmail()).isEqualTo(testEmail);

        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
                    assertThat(receivedMessages).hasSize(1);

                    MimeMessage message = receivedMessages[0];
                    assertThat(message.getAllRecipients()[0].toString()).isEqualTo(testEmail);
                    assertThat(message.getSubject()).isEqualTo("Добро пожаловать!");
                });
    }

    @Test
    void shouldSendEmailViaRestApiForUserDeleted() {
        // Given
        String testEmail = "deletedapiuser@example.com";
        EmailRequestDto request = new EmailRequestDto(testEmail, UserEventDto.EventType.USER_DELETED);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmailRequestDto> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<EmailResponseDto> response = restTemplate.postForEntity(
                baseUrl + "/email",
                entity,
                EmailResponseDto.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getEmail()).isEqualTo(testEmail);

        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
                    assertThat(receivedMessages).hasSize(1);

                    MimeMessage message = receivedMessages[0];
                    assertThat(message.getAllRecipients()[0].toString()).isEqualTo(testEmail);
                    assertThat(message.getSubject()).isEqualTo("Удаление аккаунта");
                });
    }

    @Test
    void shouldReturnValidationErrorForInvalidEmail() {
        // Given
        EmailRequestDto request = new EmailRequestDto("invalid-email", UserEventDto.EventType.USER_CREATED);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmailRequestDto> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/email",
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Некорректный формат email");
    }

    @Test
    void shouldReturnValidationErrorForEmptyEmail() {
        // Given
        EmailRequestDto request = new EmailRequestDto("", UserEventDto.EventType.USER_CREATED);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmailRequestDto> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/email",
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Email не может быть пустым");
    }

    @Test
    void shouldReturnValidationErrorForNullEventType() {
        // Given
        EmailRequestDto request = new EmailRequestDto("test@example.com", null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmailRequestDto> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/email",
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Тип события не может быть null");
    }

    @Test
    void shouldReturnHealthStatus() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/health",
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Notification Service is running");
    }
}