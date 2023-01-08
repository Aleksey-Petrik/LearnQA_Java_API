import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

public class HelloWorldTest {
    @Test
    public void testHelloWorld() {
        System.out.println("Hello from Aleksey");
    }

    @Test
    public void testApi() {
        Response response = RestAssured.get("https://playground.learnqa.ru/api/get_text").andReturn();
        response.prettyPrint();
    }

    /*
    В рамках этой задачи нужно создать тест, который будет делать GET-запрос на адрес https://playground.learnqa.ru/api/get_json_homework
    Полученный JSON необходимо распечатать и изучить. Мы увидим, что это данные с сообщениями и временем, когда они были написаны. Наша задача вывести текст второго сообщения.
    Ответом должна быть ссылка на тест в вашем репозитории.
     */
    @Test
    public void parseJson() {
        JsonPath response = RestAssured
                .given()
                .get("https://playground.learnqa.ru/api/get_json_homework")
                .jsonPath();

        response.prettyPrint();

        System.out.println(response.getString("messages.find{it.message.contains('second')}"));
    }

    /*
    Необходимо написать тест, который создает GET-запрос на адрес: https://playground.learnqa.ru/api/long_redirect
    С этого адреса должен происходит редирект на другой адрес. Наша задача — распечатать адрес, на который редиректит указанные URL.
    Ответом должна быть ссылка на тест в вашем репозитории.
     */
    @Test
    public void redirect() {
        System.out.println(RestAssured
                .given()
                .redirects()
                .follow(false)
                .when()
                .get("https://playground.learnqa.ru/api/long_redirect")
                .andReturn()
                .getHeader("Location"));
    }
}