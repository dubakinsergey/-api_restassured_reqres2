package api.reqres;

import api.reqres.colors.ColorsData;
import api.reqres.registration.Register;
import api.reqres.registration.SuccesReg;
import api.reqres.registration.UnSuccessReg;
import api.reqres.spec.Specification;
import api.reqres.users.UserData;
import api.reqres.users.UserTime;
import api.reqres.users.UserTimeResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

public class ReqresPojoTest {
    private final static String URL = "https://reqres.in/";

    //Тест 1
    //1. Используя сервис https://reqres.in/ получить список пользователей со второй (2) страницы
    //2. Убедиться что имена файлов-аватаров пользоваталей совпадают;
    //3. Убедиться, что email пользователей имеет окончание reqres.in;

    @Test
    public void checkAvatarAndIdTest() {
        Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecOK200());
        List<UserData> users = given()
                .when()
                .get("api/users?page=2")
                .then().log().all()
                .extract().body().jsonPath().getList("data", UserData.class);

        users.forEach(x -> Assertions.assertTrue(x.getAvatar().contains(x.getId().toString())));

        Assertions.assertTrue(users.stream().allMatch(x -> x.getEmail().endsWith("@reqres.in"))); //allMatch все совпадения

        List<String> avatars = users.stream().map(UserData::getAvatar).collect(Collectors.toList());
        List<String> ids = users.stream().map(x -> x.getId().toString()).collect(Collectors.toList());

        for (int i = 0; i < avatars.size(); i++) {
            Assertions.assertTrue(avatars.get(i).contains(ids.get(i)));
        }
    }

    //Тест 2
    //1. Используя сервис https://reqres.in/ протестировать регистрацию пользователя в системе
    //2. Необходимо создание 2 тестов:
    //- успешная регистрация
    //- регистрация с ошибкой из-за отсутствия пароля,
    //3. Проверить коды ошибок.

    @Test
    public void succesRegTest() {
        Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecOK200());
        Integer id = 4;
        String token = "QpwL5tke4Pnpja7X4";
        Register user = new Register("eve.holt@reqres.in", "pistol");
        SuccesReg succesReg = given()
                .body(user)
                .when()
                .post("api/register")
                .then().log().all()
                .extract().as(SuccesReg.class);
        Assertions.assertNotNull(succesReg.getId());
        Assertions.assertNotNull(succesReg.getToken());

        Assertions.assertEquals(id, succesReg.getId());
        Assertions.assertEquals(token, succesReg.getToken());
    }

    @Test
    public void unSuccesRegTest() {
        Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecError400());
        Register user = new Register("sydney@fife", "");
        UnSuccessReg unSuccessReg = given()
                .body(user)
                .post("api/register")
                .then().log().all()
                .extract().as(UnSuccessReg.class);
        Assertions.assertEquals("Missing password", unSuccessReg.getError());
    }

    //Тест 3
    //Используя сервис https://reqres.in/ убедиться, что операция LIST<RESOURCE> возвращает данные, отсортированные по годам.

    @Test
    public void sortedYearsTest() {
        Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecOK200());
        List<ColorsData> colors = given()
                .when()
                .get("api/unknown")
                .then().log().all()
                .extract().body().jsonPath().getList("data", ColorsData.class);
        List<Integer> years = colors.stream().map(ColorsData::getYear).collect(Collectors.toList());
        System.out.println(years);

        List<Integer> sortedYears = years.stream().sorted().collect(Collectors.toList());
        System.out.println(sortedYears);

        Assertions.assertEquals(sortedYears, years);

    }

    //Тест 4.1
    //Используя сервис https://reqres.in/ попробовать удалить второго пользователя и сравнить статус-код

    @Test
    public void deleteUserTest() {
        Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecUnique(204));
        given()
                .when()
                .delete("api/users/2")
                .then().log().all();
    }

    //Тест 4.2
    //Используя сервис https://reqres.in/ обновить информацию о пользователе и сравнить дату обновления с текущей датой на машине
    @Test
    public void timeTest() {
        Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecOK200());
        UserTime user = new UserTime("morpheus", "zion resident");
        UserTimeResponse response = given()
                .body(user)
                .when()
                .put("api/users/2")
                .then().log().all()
                .extract().as(UserTimeResponse.class);

        String regex = ".(?=[^.]*$)";
        String currentTime = Clock.systemUTC().instant().toString().replaceAll(regex, "");
        System.out.println(currentTime);
        Assertions.assertEquals(currentTime, response.getUpdatedAt().replaceAll(regex, ""));
        System.out.println(response.getUpdatedAt().replaceAll(regex, ""));
    }
}
