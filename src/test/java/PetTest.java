import dto.CategoryDto;
import dto.PetDto;
import dto.PetResponse;
import dto.TagDto;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import services.PetApi;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class PetTest {

    private static Stream<Arguments> dataProvider() {
        CategoryDto categoryDto = CategoryDto.builder().id(1L).name("Собаки").build();
        List<TagDto> tagsList = List.of(TagDto.builder().id(2L).name("Крупные породы").build());

        PetDto statusDoesNotExistPetDto = PetDto.builder()
                .category(categoryDto)
                .name("Английский мастиф")
                .photoUrls(List.of("https://vk.com/@kinologia_official-istoriya-porody-angliiskii-mastif"))
                .tags(tagsList)
                .status("99")
                .build();

        PetDto noIdNoDto = PetDto.builder()
                .category(categoryDto)
                .name("Австралийская пастушья собака")
                .photoUrls(List.of("https://lapkins.ru/dog/avstraliyskaya-pastushya-sobaka"))
                .tags(tagsList)
                .status("available")
                .build();

        return Stream.of(
                Arguments.of(statusDoesNotExistPetDto, HttpStatus.SC_OK),
                Arguments.of(noIdNoDto, HttpStatus.SC_OK)
        );
    }

    //Проверка создания питомца через POST /v2/pet с полным набором параметров в теле запроса, проверка тела ответа
    @Test
    public void createValidPetCheckResponse() {
        PetApi petApi = new PetApi();

        CategoryDto categoryDto = CategoryDto.builder().id(1L).name("Собаки").build();
        List<TagDto> tagsList = List.of(TagDto.builder().id(2L).name("Крупные породы").build());

        PetDto petDto = PetDto.builder()
                .id(12L)
                .category(categoryDto)
                .name("Английский мастиф")
                .photoUrls(List.of("https://vk.com/@kinologia_official-istoriya-porody-angliiskii-mastif"))
                .tags(tagsList)
                .status("available")
                .build();
        PetResponse petResponse = petApi.createPet(petDto).statusCode(HttpStatus.SC_OK).extract().body().as(PetResponse.class);

        Assertions.assertAll(
                () -> assertEquals(petDto.getId(), petResponse.getId()),
                () -> assertEquals(petDto.getCategory().getId(), petResponse.getCategory().getId()),
                () -> assertEquals(petDto.getCategory().getName(), petResponse.getCategory().getName()),
                () -> assertEquals(petDto.getName(), petResponse.getName()),
                () -> assertEquals(petDto.getPhotoUrls().get(0), petResponse.getPhotoUrls().get(0)),
                () -> assertEquals(petDto.getTags().get(0).getId(), petResponse.getTags().get(0).getId()),
                () -> assertEquals(petDto.getTags().get(0).getName(), petResponse.getTags().get(0).getName()),
                () -> assertEquals(petDto.getStatus(), petResponse.getStatus()));
    }

    //Проверка создания питомца с несуществующим статусом и получение питомца
    //Проверка создания питомца без передачи id в теле запроса и получение питомца
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void createPetCheckStatusCode(PetDto petDto, Integer statusCode) {
        PetApi petApi = new PetApi();

        PetResponse petResponse = petApi.createPet(petDto).statusCode(statusCode).extract().body().as(PetResponse.class);
        petApi.getPet(petResponse.getId().toString()).statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void updatePetCheckResponse() {
        PetApi petApi = new PetApi();
        CategoryDto categoryDto = CategoryDto.builder().id(1L).name("Собаки").build();
        List<TagDto> tagsList = List.of(TagDto.builder().id(2L).name("Крупные породы").build());

        PetDto petDto = PetDto.builder()
                .category(categoryDto)
                .name("Английский мастиф")
                .photoUrls(List.of("https://vk.com/@kinologia_official-istoriya-porody-angliiskii-mastif"))
                .tags(tagsList)
                .status("available")
                .build();
        PetResponse petResponse = petApi.createPet(petDto).statusCode(HttpStatus.SC_OK).extract().body().as(PetResponse.class);

        List<TagDto> tagsListMini = List.of(TagDto.builder().id(2L).name("собака компаньон мини").build());
        petResponse.setName("Чихуахуа");
        petResponse.setTags(tagsListMini);

        //обновление записи, через PUT запрос
        petApi.updatePet(petResponse).statusCode(HttpStatus.SC_OK).extract().body().as(PetResponse.class);

        //получение обновленной записи, через GET запрос
        PetResponse petActualResponse =  petApi.getPet(petResponse.getId().toString()).statusCode(HttpStatus.SC_OK).extract().body().as(PetResponse.class);

        Assertions.assertAll(
                () -> assertEquals(petResponse.getId(), petActualResponse.getId()),
                () -> assertEquals(petResponse.getCategory().getId(), petActualResponse.getCategory().getId()),
                () -> assertEquals(petResponse.getCategory().getName(), petActualResponse.getCategory().getName()),
                () -> assertEquals(petResponse.getName(), petActualResponse.getName()),
                () -> assertEquals(petResponse.getPhotoUrls().get(0), petActualResponse.getPhotoUrls().get(0)),
                () -> assertEquals(petResponse.getTags().get(0).getId(), petActualResponse.getTags().get(0).getId()),
                () -> assertEquals(petResponse.getTags().get(0).getName(), petActualResponse.getTags().get(0).getName()),
                () -> assertEquals(petResponse.getStatus(), petActualResponse.getStatus()));
    }

    //Получение питомцев с несуществующим статусом
    //Получение питомцев со статусом available
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void getPetByStatus(PetDto petDto) {
        PetApi petApi = new PetApi();
        petApi.findPetsByStatus(petDto.getStatus()).statusCode(HttpStatus.SC_OK);
    }
}
