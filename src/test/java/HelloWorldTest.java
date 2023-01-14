import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

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
                response = RestAssured
                        .given()
                        .queryParam("token", token)
                        .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                        .andReturn();
                if (response.getStatusCode() == HttpStatus.SC_OK) {
                    jsonPath = response.jsonPath();
                    if (jsonPath.get("status") != null)
                        if (jsonPath.getString("status").equals("Job is ready")) {
                            if (jsonPath.get("result") != null) {
                                System.out.printf("Result - %d%n", jsonPath.getInt("result"));
                                break;
                            }
                        } else {
                            System.out.println(jsonPath.getString("status"));
                        }
                    System.out.printf("Wait %d seconds%n", seconds);
                    Thread.sleep(seconds * 1000L);
                }
            }
        }
    }

    /*
    Сегодня к нам пришел наш коллега и сказал, что забыл свой пароль от важного сервиса. Он просит нас помочь ему написать программу, которая подберет его пароль.
    Условие следующее. Есть метод: https://playground.learnqa.ru/ajax/api/get_secret_password_homework
    Его необходимо вызывать POST-запросом с двумя параметрами: login и password
    Если вызвать метод без поля login или указать несуществующий login, метод вернет 500
    Если login указан и существует, метод вернет нам авторизационную cookie с названием auth_cookie и каким-то значением.
    У метода существует защита от перебора. Если верно указано поле login, но передан неправильный password, то авторизационная cookie все равно вернется. НО с "неправильным" значением, которое на самом деле не позволит создавать авторизованные запросы. Только если и login, и password указаны верно, вернется cookie с "правильным" значением. Таким образом используя только метод get_secret_password_homework невозможно узнать, передали ли мы верный пароль или нет.
    По этой причине нам потребуется второй метод, который проверяет правильность нашей авторизованной cookie: https://playground.learnqa.ru/ajax/api/check_auth_cookie
    Если вызвать его без cookie с именем auth_cookie или с cookie, у которой выставлено "неправильное" значение, метод вернет фразу "You are NOT authorized".
    Если значение cookie “правильное”, метод вернет: “You are authorized”
    Коллега говорит, что точно помнит свой login - это значение super_admin
    А вот пароль забыл, но точно помнит, что выбрал его из списка самых популярных паролей на Википедии (вот тебе и супер админ...).
    Ссылка: https://en.wikipedia.org/wiki/List_of_the_most_common_passwords
    Искать его нужно среди списка Top 25 most common passwords by year according to SplashData - список паролей можно скопировать в ваш тест вручную или придумать более хитрый способ, если сможете.

    Итак, наша задача - написать тест и указать в нем login нашего коллеги и все пароли из Википедии в виде списка. Программа должна делать следующее:
    1. Брать очередной пароль и вместе с логином коллеги вызывать первый метод get_secret_password_homework. В ответ метод будет возвращать авторизационную cookie с именем auth_cookie и каким-то значением.
    2. Далее эту cookie мы должна передать во второй метод check_auth_cookie. Если в ответ вернулась фраза "You are NOT authorized", значит пароль неправильный. В этом случае берем следующий пароль и все заново. Если же вернулась другая фраза - нужно, чтобы программа вывела верный пароль и эту фразу.

    Ответом к задаче должен быть верный пароль и ссылка на коммит со скриптом.
     */
    @Test
    public void bruteforce() {
        Map<String, String> data = new HashMap<>();
        data.put("login", "super_admin");
        data.put("password", "");
        //TODO: придумать как взять пароли с сайта
        String[] passwords = new String[]{
                "password", "123456", "12345678", "qwerty", "abc123",
                "monkey", "1234567", "letmein", "trustno1", "dragon",
                "baseball", "111111", "iloveyou", "master", "sunshine",
                "ashley", "bailey", "passw0rd", "shadow", "123123",
                "654321", "superman", "qazwsx", "michael", "Football",
                "123456", "abc123", "football", "1234", "sunshine",
                "!@#$%^&*", "welcome", "12345679", "123456789", "qwerty",
                "12345678", "111111", "1234567890", "1234567", "password",
                "123123", "987654321", "qwertyuiop", "mynoob", "123321",
                "666666", "18atcskd2w", "7777777", "1q2w3e4r", "654321",
                "555555", "3rjs1la7qe", "google", "1q2w3e4r5t", "123qwe",
                "zxcvbnm", "1q2w3e"

        };

        for (String password : passwords) {
            data.put("password", password);
            Response response = RestAssured
                    .given()
                    .body(data)
                    .when()
                    .post("https://playground.learnqa.ru/ajax/api/get_secret_password_homework")
                    .andReturn();
            response.prettyPrint();
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                Response responseCheck = RestAssured
                        .given()
                        .body(data)
                        .cookies(response.getCookies())
                        .when()
                        .post(" https://playground.learnqa.ru/ajax/api/check_auth_cookie")
                        .andReturn();
                if (!responseCheck.asString().equals("You are NOT authorized")) {
                    System.out.printf("Фраза - %s, Пароль - %s%n", responseCheck.asString(), password);
                    break;
                }
            }
        }
    }
}




