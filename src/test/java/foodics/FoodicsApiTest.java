package foodics;

import io.restassured.RestAssured;
import io.restassured.path.json.exception.JsonPathException;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class FoodicsApiTest {

    private static final String BASE_URL = "https://pay2.foodics.dev/cp_internal";
    private static final String LOGIN_ENDPOINT = BASE_URL + "/login";
    private static final String WHOAMI_ENDPOINT = BASE_URL + "/whoami";

    private String token;

    @BeforeClass
    public void setup() {
        // Setting the base URI for RestAssured
        RestAssured.baseURI = BASE_URL;

        // Perform login and retrieve the token
        token = performLogin("merchant@foodics.com", "123456");
    }

    /**
     * This method performs the login operation and returns the token.
     */
    private String performLogin(String email, String password) {
        Response response = given()
                .contentType("application/json")
                .body("{\"email\": \"" + email + "\", \"password\": \"" + password + "\", \"token\": \"Lyz22cfYKMetFhKQybx5HAmVimF1i0xO\"}")
                .when()
                .post(LOGIN_ENDPOINT)
                .then()
                .extract()
                .response();

        // Validate response status and extract token if login is successful
        Assert.assertEquals(response.getStatusCode(), 200, "Login failed with status code: " + response.getStatusCode());

        // Assuming token is part of the response body
        return response.jsonPath().getString("token");
    }

    @Test(priority = 1)
    public void testLoginSuccess() {
        // Validate successful login by checking the token
        Assert.assertNotNull(token, "Token should not be null after successful login.");
    }

    @Test(priority = 2)
    public void testWhoami() {
        // Test the /whoami endpoint
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(WHOAMI_ENDPOINT)
                .then()
                .extract()
                .response();

        System.out.println("Response Body: " + response.getBody().asString());

        // Validate response status and check for user details
        Assert.assertEquals(response.getStatusCode(), 200, "Whoami call failed with status code: " + response.getStatusCode());

        try {
            String email = response.jsonPath().getString("email");
            Assert.assertTrue(email.contains("merchant@foodics.com"), "User email does not match.");
        } catch (JsonPathException e) {
            Assert.fail("Failed to parse JSON response: " + e.getMessage());
        }
    }

    @Test(priority = 3)
    public void testLoginInvalidCredentials() {
        // Prepare the payload with invalid credentials
        String invalidLoginPayload = "{ \"email\": \"wrong@foodics.com\", \"password\": \"wrongpassword\" }";

        // Test login with invalid credentials
        Response response = given()
                .contentType("application/json")
                .body(invalidLoginPayload)
                .when()
                .post(LOGIN_ENDPOINT)
                .then()
                .extract()
                .response();

        // Log the response body for debugging
        System.out.println("Response Body: " + response.getBody().asString());

        // Validate response status
        Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401 for invalid login.");
    }
}
