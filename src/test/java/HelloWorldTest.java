import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
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

    /*
    Необходимо написать тест, который создает GET-запрос на адрес из предыдущего задания: https://playground.learnqa.ru/api/long_redirect
    На самом деле этот URL ведет на другой, который мы должны были узнать на предыдущем занятии. Но этот другой URL тоже куда-то редиректит. И так далее. Мы не знаем заранее количество всех редиректов и итоговый адрес.
    Наша задача — написать цикл, которая будет создавать запросы в цикле, каждый раз читая URL для редиректа из нужного заголовка. И так, пока мы не дойдем до ответа с кодом 200.
    Ответом должна быть ссылка на тест в вашем репозитории и количество редиректов.
     */
    @Test
    public void lastRedirect() {
        String url = "https://playground.learnqa.ru/api/long_redirect";
        int countRedirects = 0;
        while (true) {
            System.out.println("URL - " + url);
            Response response = RestAssured
                    .given()
                    .redirects()
                    .follow(false)
                    .when()
                    .get(url)
                    .andReturn();
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                break;
            } else {
                url = response.getHeader("Location");
                countRedirects++;
            }
        }
        System.out.printf("LastRedirect - %s, CountRedirects - %d%n", url, countRedirects);
    }

    /*
    Иногда API-метод выполняет такую долгую задачу, что за один HTTP-запрос от него нельзя сразу получить готовый ответ. Это может быть подсчет каких-то сложных вычислений или необходимость собрать информацию по разным источникам.
    В этом случае на первый запрос API начинает выполнения задачи, а на последующие ЛИБО говорит, что задача еще не готова, ЛИБО выдает результат. Сегодня я предлагаю протестировать такой метод.
    Сам API-метод находится по следующему URL: https://playground.learnqa.ru/ajax/api/longtime_job
    Если мы вызываем его БЕЗ GET-параметра token, метод заводит новую задачу, а в ответ выдает нам JSON со следующими полями:
    * seconds - количество секунд, через сколько задача будет выполнена
    * token - тот самый токен, по которому можно получить результат выполнения нашей задачи

    Если же вызвать API-метод, УКАЗАВ GET-параметром token, то мы получим следующий JSON:
    * error - будет только в случае, если передать token, для которого не создавалась задача. В этом случае в ответе будет следующая надпись - No job linked to this token
    * status - если задача еще не готова, будет надпись Job is NOT ready, если же готова - будет надпись Job is ready
    * result - будет только в случае, если задача готова, это поле будет содержать результат

    Наша задача - написать тест, который сделал бы следующее:
    1) создавал задачу
    2) делал один запрос с token ДО того, как задача готова, убеждался в правильности поля status
    3) ждал нужное количество секунд с помощью функции Thread.sleep() - для этого надо сделать import time
    4) делал бы один запрос c token ПОСЛЕ того, как задача готова, убеждался в правильности поля status и наличии поля result
    Как всегда, код нашей программы выкладываем ссылкой на коммит.
     */
    @Test
    public void longWork() throws InterruptedException {
        Response response = RestAssured
                .given()
                .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                .andReturn();
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            JsonPath jsonPath = response.jsonPath();
            int seconds = jsonPath.getInt("seconds");
            String token = jsonPath.getString("token");

            while (true) {
                System.out.printf("Wait %d seconds%n", seconds);
                Thread.sleep(seconds * 1000L);
                response = RestAssured
                        .given()
                        .queryParam("token", token)
                        .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                        .andReturn();
                if (response.getStatusCode() == HttpStatus.SC_OK) {
                    jsonPath = response.jsonPath();
                    if (jsonPath.get("result") != null) {
                        System.out.printf("Result - %d%n", jsonPath.getInt("result"));
                        break;
                    } else {
                        System.out.println(jsonPath.getString("status"));
                    }
                }
            }
        }
    }
}