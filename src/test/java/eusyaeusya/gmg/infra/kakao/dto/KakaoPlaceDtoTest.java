package eusyaeusya.gmg.infra.kakao.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KakaoPlaceDtoTest {

    @Test
    @DisplayName("뷔페와 패밀리레스토랑은 제외 카테고리로 판별된다")
    void testIsExcluded() {
        KakaoPlaceDto buffet = createDto("애슐리퀸즈", "뷔페");
        KakaoPlaceDto familyRest = createDto("아웃백", "패밀리레스토랑");
        KakaoPlaceDto normalPlace = createDto("김밥천국", "분식");

        assertThat(buffet.isExcluded()).isTrue();
        assertThat(familyRest.isExcluded()).isTrue();
        assertThat(normalPlace.isExcluded()).isFalse();
    }

    @Test
    @DisplayName("스터디카페는 예외없이 STUDY PlaceType과 STUDY_CAFE Category를 가진다")
    void testStudyCafeMapping() {
        KakaoPlaceDto studyCafe = createDto("작심스터디카페", "스터디룸");
        
        assertThat(studyCafe.inferPlaceTypeCode()).isEqualTo("STUDY");
        assertThat(studyCafe.mapToCategoryCode()).isEqualTo("STUDY_CAFE");
    }

    @Test
    @DisplayName("FD6 코드여도 제과점/베이커리는 카페로 매핑된다")
    void testCafeMappingWithFoodCode() {
        // FD6 = 음식점 코드
        KakaoPlaceDto bakery = createDtoWithGroup("파리바게뜨", "제과점", "FD6");
        
        assertThat(bakery.inferPlaceTypeCode()).isEqualTo("CAFE");
        assertThat(bakery.mapToCategoryCode()).isEqualTo("DESSERT_CAFE");
    }

    @Test
    @DisplayName("FD6 코드여도 이자카야, 실내포장마차는 술집으로 매핑된다")
    void testBarMappingWithFoodCode() {
        KakaoPlaceDto izakaya = createDtoWithGroup("토라 이자카야", "일본식주점", "FD6");
        KakaoPlaceDto pocha = createDtoWithGroup("한신포차", "실내포장마차", "FD6");
        
        assertThat(izakaya.inferPlaceTypeCode()).isEqualTo("BAR");
        assertThat(izakaya.mapToCategoryCode()).isEqualTo("IZAKAYA");
        
        assertThat(pocha.inferPlaceTypeCode()).isEqualTo("BAR");
        assertThat(pocha.mapToCategoryCode()).isEqualTo("INDOOR_POCHA");
    }

    @Test
    @DisplayName("닭강정은 분식으로 맵핑된다")
    void testChickenGangjeongMapping() {
        KakaoPlaceDto gangjeong = createDto("만석닭강정", "치킨");
        
        // 치킨이지만 이름에 닭강정이 들어가면 분식
        assertThat(gangjeong.inferPlaceTypeCode()).isEqualTo("RESTAURANT");
        assertThat(gangjeong.mapToCategoryCode()).isEqualTo("SNACK_BAR");
    }

    @Test
    @DisplayName("알 수 없는 카테고리는 Type에 맞는 Fallback 카테고리를 반환한다")
    void testFallbackMapping() {
        KakaoPlaceDto unknownFood = createDtoWithGroup("알수없는식당", "외계음식", "FD6"); // 알 수 없는 음식
        
        assertThat(unknownFood.inferPlaceTypeCode()).isEqualTo("RESTAURANT");
        assertThat(unknownFood.mapToCategoryCode()).isEqualTo("KOREAN_FOOD"); // 한식으로 fallback
    }

    private KakaoPlaceDto createDto(String placeName, String categoryName) {
        return createDtoWithGroup(placeName, categoryName, null);
    }
    
    private KakaoPlaceDto createDtoWithGroup(String placeName, String categoryName, String categoryGroupCode) {
        return new KakaoPlaceDto(
                "1",
                placeName,
                categoryGroupCode,
                "GroupName",
                categoryName,
                "x", "y",
                "주소",
                "도로명",
                "전화번호",
                "url",
                "100"
        );
    }
}
