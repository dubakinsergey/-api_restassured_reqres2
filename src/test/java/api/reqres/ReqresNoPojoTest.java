package api.reqres;

import api.reqres.spec.Specification;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class ReqresNoPojoTest {
    private final static String URL = "https://reqres.in/";

    //Тест 1
    //1. Используя сервис https://reqres.in/ получить список пользователей со второй (2) страницы
    //2. Убедиться что имена файлов-аватаров пользоваталей совпадают;
    //3. Убедиться, что email пользователей имеет окончание reqres.in;

    @Test
    public void checkAvatarNoPojoTest() {
        Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecOK200());
        Response response = given()
                .when()
                .get("api/users?page=2")
                .then().log().all()
                .body("page", equalTo(2))
                .body("data.id", notNullValue())
                .body("data.email", notNullValue())
                .body("data.first_name", notNullValue())
                .body("data.last_name", notNullValue())
                .body("data.avatar", notNullValue())
                .extract().response();
        JsonPath jsonPath = response.jsonPath();
        List<String> emails = jsonPath.get("data.email");
        List<Integer> ids = jsonPath.get("data.id");
        List<String> avatars = jsonPath.get("data.avatar");

        for (int i = 0; i < avatars.size(); i++) {
            Assertions.assertTrue(avatars.get(i).contains(ids.get(i).toString())); //contains содержание одной строки в другой строке
        }
        Assertions.assertTrue(emails.stream().allMatch(x -> x.endsWith("@reqres.in"))); //allMatch абсолютные все проверки, если какой-то элемент не пройдет проверку, то вся проверка провалится

    }

    //    @Test
//    public void succesUserRegTestNoPojo(){
//        Specification.installSpecification(Specification.requestSpec(URL),Specification.responseSpecOK200());
//        Map<String,String>user = new HashMap<>();
//        user.put("email","eve.holt@reqres.in");
//        user.put("password","pistol");
//        given()
//                .body(user)
//                .when()
//                .post("api/register")
//                .then().log().all()
//                .body("id",equalTo(4))
//                .body("token",equalTo("QpwL5tke4Pnpja7X4"));
//    }

    //Тест 2
    //1. Используя сервис https://reqres.in/ протестировать регистрацию пользователя в системе
    //2. Необходимо создание 2 тестов:
    //- успешная регистрация
    //- регистрация с ошибкой из-за отсутствия пароля,
    //3. Проверить коды ошибок.

    @Test
    public void succesUserRegTestNoPojo() {
        Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecOK200());
        Map<String, String> user = new HashMap<>();
        user.put("email", "eve.holt@reqres.in");
        user.put("password", "pistol");
        Response response = given()
                .body(user)
                .when()
                .post("api/register")
                .then().log().all()
                .extract().response();
        JsonPath jsonPath = response.jsonPath();
        int id = jsonPath.get("id");
        String token = jsonPath.get("token");
        Assertions.assertEquals(4, id);
        Assertions.assertEquals("QpwL5tke4Pnpja7X4", token);
    }

//    @Test
//    public void unSuccessUserRegTestNoPojo(){
//        Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecError400());
//        Map<String, String> user = new HashMap<>();
//        user.put("email","sydney@fife");
//        given()
//                .body(user)
//                .when()
//                .post("api/register")
//                .then().log().all()
//                .body("error",equalTo("Missing password"));
//    }

    @Test
    public void unSuccessUserRegTestNoPojo() {
        Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecError400());
        Map<String, String> user = new HashMap<>();
        user.put("email", "sydney@fife");
        Response response = given()
                .body(user)
                .when()
                .post("api/register")
                .then().log().all()
                .extract().response();
        JsonPath jsonPath = response.jsonPath();
        String error = jsonPath.get("error");
        Assertions.assertEquals("Missing password", error);
    }
}
