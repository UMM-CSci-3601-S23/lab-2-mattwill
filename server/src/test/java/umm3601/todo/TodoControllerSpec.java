package umm3601.todo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;

import umm3601.Server;

/**
 * Tests the logic of the TodoController
 *
 * @throws IOException
 */
// The tests here include a ton of "magic numbers" (numeric constants).
// It wasn't clear to me that giving all of them names would actually
// help things. The fact that it wasn't obvious what to call some
// of them says a lot. Maybe what this ultimately means is that
// these tests can/should be restructured so the constants (there are
// also a lot of "magic strings" that Checkstyle doesn't actually
// flag as a problem) make more sense.
@SuppressWarnings({ "MagicNumber" })
public class TodoControllerSpec {

  private Context ctx = mock(Context.class);

  private TodoController userController;
  private static TodoDatabase db;

  @BeforeEach
  public void setUp() throws IOException {
    db = new TodoDatabase(Server.TODO_DATA_FILE);
    userController = new TodoController(db);
  }

  /**
   * Confirms that we can get all the users.
   *
   * @throws IOException
   */
  @Test
  public void canGetAllTodos() throws IOException {
    // Call the method on the mock context, which doesn't
    // include any filters, so we should get all the users
    // back.
    userController.getTodos(ctx);

    // Confirm that `json` was called with all the users.
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    assertEquals(db.size(), argument.getValue().length);
  }

  @Test
  public void canGetUsersWithCategoryhomework() throws IOException {
    // Add a query param map to the context that maps "category"
    // to "homework".
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("category", Arrays.asList(new String[] {"homework"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    userController.getTodos(ctx);

    // Confirm that all the todos passed to `json` have category homework.
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    for (Todo todo : argument.getValue()) {
      assertEquals("homework", todo.category);
    }
    // Confirm that there are 79 users with category homework
    assertEquals(79, argument.getValue().length);
  }

  @Test
  public void canGetUsersWithOnwer() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("owner", Arrays.asList(new String[] {"Fry"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    userController.getTodos(ctx);

    // Confirm that all the todos passed to `json` work for Fry.
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    for (Todo todo : argument.getValue()) {
      assertEquals("Fry", todo.owner);
    }
  }

  @Test
  public void canGetUsersWithBodiesContainingGivenString() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("body", Arrays.asList(new String[] {"magna eu"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    userController.getTodos(ctx);

    // Confirm that all the todos passed to `json` work for magna eu.
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    // for (Todo todo : argument.getValue()) {
    //   assertTrue(todo.body.contains("magna eu"));
    // }
    assertEquals(3, argument.getValue().length);
  }


  @Test
  public void canGetUsersWithGivenOwnerAndCategory() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("owner", Arrays.asList(new String[] {"Blanche"}));
    queryParams.put("category", Arrays.asList(new String[] {"software design"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    userController.getTodos(ctx);

    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    for (Todo todo : argument.getValue()) {
      assertEquals("Blanche", todo.owner);
      assertEquals("software design", todo.category);
    }
    assertEquals(14, argument.getValue().length);
  }

  @Test
  public void canGetUserWithSpecifiedId() throws IOException {
    String id = "58895985a22c04e761776d54";
    Todo todo = db.getTodo(id);

    when(ctx.pathParam("id")).thenReturn(id);

    userController.getTodo(ctx);

    verify(ctx).json(todo);
    verify(ctx).status(HttpStatus.OK);
    assertEquals("Blanche", todo.owner);
  }

  @Test
  public void respondsAppropriatelyToRequestForNonexistentId() throws IOException {
    when(ctx.pathParam("id")).thenReturn(null);
    Throwable exception = Assertions.assertThrows(NotFoundResponse.class, () -> {
      userController.getTodo(ctx);
    });
    assertEquals("No todo with id " + null + " was found.", exception.getMessage());
  }

  @Test
  public void canGetUsersWithStatusComplete() throws IOException {
    // Add a query param map to the context that maps "category"
    // to "homework".
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("status", Arrays.asList(new String[] {"Complete"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    userController.getTodos(ctx);

    // Confirm that all the todos passed to `json` have category homework.
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    for (Todo todo : argument.getValue()) {
      assertEquals("Complete", todo.status);
    }
    // Confirm that there are 79 users with category homework
    //assertEquals(79, argument.getValue().length);
  }

}
