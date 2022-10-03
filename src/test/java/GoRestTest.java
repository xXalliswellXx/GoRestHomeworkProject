import Pojo.GoRestPosts;
import Pojo.GoRestUser;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class GoRestTest {

    private RequestSpecification reqSpec;
    private GoRestUser user;
    private GoRestPosts post;

    @BeforeClass
    public void setup() {

        baseURI = "https://gorest.co.in";

        reqSpec = given()
                .log().uri()
                .log().body()
                .header("Authorization", "Bearer 9cf83a65b514ceeadf16bcdd3a4a4bfd7cd9bbf0a1ce1e687d166bed5d12b176")
                .contentType(ContentType.JSON);

    }

    @Test
    public void createUserTest() {

        user = new GoRestUser();
        user.setName("GoRest Test User");
        user.setEmail("gorest@test.com");
        user.setGender("female");
        user.setStatus("active");

        user.setId(given()
                .spec(reqSpec)
                .body(user)
                .when()
                .post("/public/v2/users")
                .then()
                .log().body()
                .body("name", equalTo(user.getName()))
                .statusCode(201)
                .extract().jsonPath().getString("id"));

    }

    @Test (dependsOnMethods = "createUserTest")
    public void createUserNegativeTest() {

        given()
                .spec(reqSpec)
                .body(user)
                .when()
                .post("/public/v2/users")
                .then()
                .log().body()
                .body("message[0]", equalTo("has already been taken"))
                .statusCode(422);

    }

    @Test (dependsOnMethods = "createUserNegativeTest")
    public void createPostTest() {

        post = new GoRestPosts();
        post.setUser_id(user.getId());
        post.setTitle("GeRest Test Post Title");
        post.setBody("GeRest Test Post Body");

        post.setId(given()
                .spec(reqSpec)
                .body(post)
                .when()
                .post("/public/v2/posts")
                .then()
                .log().body()
                .body("user_id", equalTo(Integer.parseInt(user.getId())))
                .statusCode(201)
                .extract().jsonPath().getString("id"));

    }

    @Test (dependsOnMethods = "createPostTest")
    public void editPostTest() {

        HashMap<String, String> updatePostBody = new HashMap<>();
        updatePostBody.put("title", "Updated GeRest Test Post Title");
        updatePostBody.put("body", "Updated GeRest Test Post Body");

        given()
                .spec(reqSpec)
                .body(updatePostBody)
                .when()
                .put("/public/v2/posts/" + post.getId())
                .then()
                .log().body()
                .body("title", equalTo(updatePostBody.get("title")))
                .statusCode(200);

    }

    @Test (dependsOnMethods = "editPostTest")
    public void deletePostTest() {

        given()
                .spec(reqSpec)
                .when()
                .delete("/public/v2/posts/" + post.getId())
                .then()
                .log().body()
                .statusCode(204);

    }

    @Test (dependsOnMethods = "deletePostTest")
    public void deleteUserTest() {

        given()
                .spec(reqSpec)
                .when()
                .delete("/public/v2/users/" + user.getId())
                .then()
                .log().body()
                .statusCode(204);

    }

    @Test (dependsOnMethods = "deleteUserTest")
    public void deleteUserNegativeTest() {

        given()
                .spec(reqSpec)
                .when()
                .delete("/public/v2/users/" + user.getId())
                .then()
                .log().body()
                .body("message", equalTo("Resource not found"))
                .statusCode(404);

    }

}
