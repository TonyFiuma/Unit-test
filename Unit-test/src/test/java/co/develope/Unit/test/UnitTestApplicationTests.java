package co.develope.Unit.test;

import co.develope.Unit.test.controllers.UserController;
import co.develope.Unit.test.entities.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UnitTestApplicationTests {

	@Autowired
	private UserController userController;

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Test
	void contextLoads() {
		assertThat(userController).isNotNull();
	}

	private User getUserFromId(Long id) throws Exception{
		MvcResult result = this.mockMvc.perform(get("/user/" + id))
				.andDo(print())
				.andExpect(status().isOk())
				.andReturn();

		try {
			String userJSON = result.getResponse().getContentAsString();
			return objectMapper.readValue(userJSON, User.class);
		}catch (Exception e){
			return null;
		}
	}

	private User createAUser() throws Exception {
		User user = new User();
		user.setActive(true);
		user.setName("Axel");
		user.setSurname("Foley");
		user.setAge(30);

		return createAUser(user);
	}

	private User createAUser(User user) throws Exception {
		MvcResult result = createAUserRequest(user);
		User userFromResponse = objectMapper.readValue(result.getResponse().getContentAsString(), User.class);

		Assertions.assertThat(userFromResponse).isNotNull();
		Assertions.assertThat(userFromResponse.getId()).isNotNull();

		return userFromResponse;
	}
	private MvcResult createAUserRequest() throws Exception {
		User user = new User();
		user.setActive(true);
		user.setName("Axel");
		user.setSurname("Foley");
		user.setAge(26);

		return createAUserRequest(user);
	}

	private MvcResult createAUserRequest(User user) throws Exception{
		if(user == null) return null;
		String userJSON = objectMapper.writeValueAsString(user);

		return this.mockMvc.perform(post("/user")
						.contentType(MediaType.APPLICATION_JSON)
						.content(userJSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andReturn();
	}

	@Test
	void createAUserTest() throws Exception {
		User userFromResponse = createAUser();
		//assertThat(userFromResponse.getId()).isNotNull();
	}

	@Test
	void readUserList() throws Exception {
		createAUserRequest();

		MvcResult result = this.mockMvc.perform(get("/user/"))
				.andDo(print())
				.andExpect(status().isOk())
				.andReturn();

		List<User> usersFromResponse = objectMapper.readValue(result.getResponse().getContentAsString(), List.class);
		System.out.println("User in database are:" + usersFromResponse.size());
		assertThat(usersFromResponse.size()).isNotZero();
	}

	@Test
	void readSingleUser() throws Exception {
		User user = createAUser();
		//assertThat(user.getId()).isNotNull();
		User userFromResponse = getUserFromId(user.getId());
		assertThat(userFromResponse.getId()).isEqualTo(user.getId());
	}

	@Test
	void updateUser() throws Exception{
		User user = createAUser();
		//assertThat(user.getId()).isNotNull();

		String newName = "Axel";
		user.setName(newName);

		String userJSON = objectMapper.writeValueAsString(user);

		MvcResult result = this.mockMvc.perform(put("/user/" + user.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(userJSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andReturn();

		User usersFromResponse = objectMapper.readValue(result.getResponse().getContentAsString(), User.class);

		// Check user from PUT
		assertThat(usersFromResponse.getId()).isEqualTo(user.getId());
		assertThat(usersFromResponse.getName()).isEqualTo(newName);

		// Check user from GET
		User userFromResponseGet = getUserFromId(user.getId());
		assertThat(userFromResponseGet.getId()).isEqualTo(user.getId());
		assertThat(userFromResponseGet.getName()).isEqualTo(newName);
	}

	@Test
	void deleteUser()throws Exception{
		User user = createAUser();
		assertThat(user.getId()).isNotNull();

		this.mockMvc.perform(delete("/user/" + user.getId()))
				.andDo(print())
				.andExpect(status().isOk())
				.andReturn();

		User userFromResponseGet = getUserFromId(user.getId());
		assertThat(userFromResponseGet).isNull();
	}


	@Test
	void activateUser()throws Exception{
		User user = createAUser();
		assertThat(user.getId()).isNotNull();

		MvcResult result = this.mockMvc.perform(put("/user/" + user.getId() + "/activation?activated=true"))
				.andDo(print())
				.andExpect(status().isOk())
				.andReturn();

		User userFromResponse = objectMapper.readValue(result.getResponse().getContentAsString(), User.class);
		assertThat(userFromResponse.getId()).isEqualTo(user.getId());
		assertThat(userFromResponse.isActive()).isEqualTo(true);

		User userFromResponseGet = getUserFromId(user.getId());
		assertThat(userFromResponseGet).isNotNull();
		assertThat(userFromResponseGet.getId()).isEqualTo(user.getId());
		assertThat(userFromResponseGet.isActive()).isEqualTo(true);
	}



}
