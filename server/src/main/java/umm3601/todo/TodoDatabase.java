package umm3601.todo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.BadRequestResponse;

/**
 * A fake "database" of user info
 * <p>
 * Since we don't want to complicate this lab with a real database, we're going
 * to instead just read a bunch of user data from a specified JSON file, and
 * then provide various database-like methods that allow the `UserController` to
 * "query" the "database".
 */
public class TodoDatabase {

  private Todo[] allTodos;

  public TodoDatabase(String todoDataFile) throws IOException {
    InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(todoDataFile));
    ObjectMapper objectMapper = new ObjectMapper();
    allTodos = objectMapper.readValue(reader, Todo[].class);
  }

  public int size() {
    return allTodos.length;
  }

  /**
   * Get the single user specified by the given ID. Return `null` if there is no
   * user with that ID.
   *
   * @param id the ID of the desired user
   * @return the user with the given ID, or null if there is no user with that ID
   */
  public Todo getTodo(String id) {
    return Arrays.stream(allTodos).filter(x -> x._id.equals(id)).findFirst().orElse(null);
  }

  /**
   * Get an array of all the users satisfying the queries in the params.
   *
   * @param queryParams map of key-value pairs for the query
   * @return an array of all the users matching the given criteria
   */
  public Todo[] listTodos(Map<String, List<String>> queryParams) {
    Todo[] filteredTodos = allTodos;

    // Filter age if defined
    if (queryParams.containsKey("owner")) {
      String targetOwner = queryParams.get("owner").get(0);
      filteredTodos = filterTodosByOwner(filteredTodos, targetOwner);
    }
    if (queryParams.containsKey("category")) {
      String categoryParam = queryParams.get("category").get(0);
      filteredTodos = filterTodosByCatagory(filteredTodos, categoryParam);
    }
    if (queryParams.containsKey("status")) {
      String statusParam = queryParams.get("status").get(0);
      Boolean status = false;
      if (statusParam.equals("complete")) {
        status = true;
      }
      filteredTodos = filterTodosByStatus(filteredTodos, status);
    }


    if (queryParams.containsKey("contains")) {
      String targetContains = queryParams.get("contains").get(0);
      filteredTodos = filterTodosByContains(filteredTodos, targetContains);
    }
    if (queryParams.containsKey("orderBy")) {
      String orderByParam = queryParams.get("orderBy").get(0);
      filteredTodos = filterTodosByOrderBy(filteredTodos, orderByParam);
    }
    if (queryParams.containsKey("limit")) {
      String limitParam = queryParams.get("limit").get(0);
      try {
        int targetLimit = Integer.parseInt(limitParam);
        filteredTodos = filterTodosByLimit(filteredTodos, targetLimit);
      } catch (NumberFormatException e) {
        throw new BadRequestResponse("Specified limit '" + limitParam + "' can't be parsed to an integer");
      }

    }



    // Process other query parameters here...

    return filteredTodos;
  }

  /**
   * Get an array of all the users having the target age.
   *
   * @param Todos     the list of users to filter by age
   * @param targetAge the target age to look for
   * @return an array of all the users from the given list that have the target
   *         age
   */
  public Todo[] filterTodosByOwner(Todo[] todos, String targetOwner) {
    return Arrays.stream(todos).filter(x -> x.owner.equals(targetOwner)).toArray(Todo[]::new);
  }

  public Todo[] filterTodosByCatagory(Todo[] todos, String targetCategory) {
    return Arrays.stream(todos).filter(x -> x.category.equals(targetCategory)).toArray(Todo[]::new);
  }
  public Todo[] filterTodosByStatus(Todo[] todos, Boolean targetStatus) {
    return Arrays.stream(todos).filter(x -> x.status.equals(targetStatus)).toArray(Todo[]::new);
  }

  public Todo[] filterTodosByContains(Todo[] todos, String targetContains) {
    return Arrays.stream(todos).filter(x -> x.body.contains(targetContains)).toArray(Todo[]::new);
  }

  public Todo[] filterTodosByOrderBy(Todo[] todos, String targetOrderBy) {
    if (targetOrderBy.equals("owner")) {
      return Arrays.stream(todos).sorted((t1, t2) -> t1.owner.compareTo(t2.owner)).toArray(Todo[]::new);
    }
    if (targetOrderBy.equals("body")) {
      return Arrays.stream(todos).sorted((t1, t2) -> t1.body.compareTo(t2.body)).toArray(Todo[]::new);
    }
    if (targetOrderBy.equals("status")) {
      return Arrays.stream(todos).sorted((t1, t2) -> t1.status.compareTo(t2.status)).toArray(Todo[]::new);
    }
    if (targetOrderBy.equals("category")) {
      return Arrays.stream(todos).sorted((t1, t2) -> t1.category.compareTo(t2.category)).toArray(Todo[]::new);
    }
    return new Todo[0];
  }
  public Todo[] filterTodosByLimit(Todo[] todos, int targetLimit) {
    return Arrays.stream(todos).limit(targetLimit).toArray(Todo[]::new);
  }



}
