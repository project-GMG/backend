package eusyaeusya.gmg.infra.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoPlaceDto(
        String id,
        @JsonProperty("place_name") String placeName,
        @JsonProperty("category_group_code") String categoryGroupCode,
        @JsonProperty("category_group_name") String categoryGroupName,
        @JsonProperty("category_name") String categoryName,
        String x, String y,
        @JsonProperty("address_name") String addressName,
        @JsonProperty("road_address_name") String roadAddressName,
        String phone,
        @JsonProperty("place_url") String placeUrl,
        String distance

) {
    /**
     * PlaceType 추론
     */
    public String inferPlaceTypeCode() {
        // 1. categoryGroupCode가 있으면 우선 사용
        if (categoryGroupCode != null) {
            return switch (categoryGroupCode) {
                case "FD6" -> {
                    // 음식점인데 술집 관련 카테고리면 BAR로 분류
                    if (isBarRelated()) {
                        yield "BAR";
                    }
                    yield "RESTAURANT";
                }
                case "CE7" -> "CAFE";
                default -> "RESTAURANT";
            };
        }

        if (categoryName != null) {
            String lower = categoryName.toLowerCase();

            if (lower.contains("실내포장마차") ||
                    lower.contains("호프") ||
                    lower.contains("요리주점") ||
                    lower.contains("캌테일바") ||
                    lower.contains("일본식주점")) {
                return "BAR";
            }

            if (lower.contains("독서실") || lower.contains("스터디") ||
                    lower.contains("열람실") || lower.contains("도서관")) {
                return "STUDY";
            }

            if (lower.contains("카페") || lower.contains("테마카페") || lower.contains("커피전문점")) {
                return "CAFE";
            }
        }

        if (placeName != null) {
            String lower = placeName.toLowerCase();

            if (lower.contains("독서실") || lower.contains("스터디")) {
                return "STUDY";
            }

            if (lower.contains("이자카야") || lower.contains("술집") || lower.contains("호프")) {
                return "BAR";
            }
        }

        return "RESTAURANT";
    }

    private boolean isBarRelated() {
        if (categoryName == null) return false;

        String lower = categoryName.toLowerCase();
        return lower.contains("실내포장마차") ||
                lower.contains("호프") ||
                lower.contains("요리주점") ||
                lower.contains("캌테일바") ||
                lower.contains("일본식주점");
    }

    /**
     * PlaceCategory 코드 매핑
     * 우선순위: 구체적 카테고리 → 일반적 카테고리
     * <p>
     * 매핑 규칙:
     * 1. 고기, 치킨, 햄버거(패스트푸드) 등 구체적 음식 카테고리 우선
     * 2. 한식, 중식, 일식, 양식 등 일반 음식 카테고리는 후순위
     * 3. 카페의 경우: 디저트 → 가맹점 → 보드게임 → 개인카페 순서
     */
    public String mapToCategoryCode() {
        if (categoryName == null) return null;

        String lower = categoryName.toLowerCase();

        // 음식
        // 1. 고기 (한식>육류,고기 등을 우선 처리)
        if (lower.contains("고기") || lower.contains("육류")) {
            return "MEAT";
        }

        // 2. 치킨
        if (lower.contains("치킨")) {
            return "CHICKEN";
        }

        // 3. 패스트푸드 (양식>햄버거 등을 우선 처리)
        if (lower.contains("패스트푸드") || lower.contains("햄버거")) {
            return "FAST_FOOD";
        }

        // 4. 분식
        if (lower.contains("분식")) {
            return "SNACK_BAR";
        }

        // 5. 한식 (고기는 이미 처리되었으므로 나머지 한식)
        if (lower.contains("한식") || lower.contains("도시락") || lower.contains("샤브샤브")) {
            return "KOREAN_FOOD";
        }

        // 6. 일식
        if (lower.contains("일식")) {
            return "JAPANESE_FOOD";
        }

        // 7. 중식
        if (lower.contains("중식")) {
            return "CHINESE_FOOD";
        }

        // 8. 양식 (햄버거는 이미 처리되었으므로 나머지 양식)
        if (lower.contains("양식") || lower.contains("이탈리안") || lower.contains("피자")) {
            return "WESTERN_FOOD";
        }

        // 9. 아시안
        if (lower.contains("아시아음식") || lower.contains("동남아음식") ||
                lower.contains("베트남음식") || lower.contains("인도음식") || lower.contains("태국음식")) {
            return "ASIAN_FOOD";
        }

        // 카페
        // 1. 디저트 카페
        if (lower.contains("디저트카페")) {
            return "DESSERT_CAFE";
        }

        // 2. 가맹점 (프랜차이즈)
        if (lower.contains("커피전문")) {
            return "FRANCHISE_CAFE";
        }

        // 3. 보드게임 카페
        if (lower.contains("보드카페") || lower.contains("보드게임")) {
            return "BOARDGAME_CAFE";
        }

        // 4. 개인카페 (기본 카페, 마지막 순위)
        if (categoryGroupCode != null && categoryGroupCode.equals("CE7")) {
            return "LOCAL_CAFE";
        }


        // 술집
        // 1. 요리주점
        if (lower.contains("요리주점")) {
            return "FOOD_BAR";
        }

        // 2. 이자카야
        if (lower.contains("이자카야")) {
            return "IZAKAYA";
        }

        // 3. 실내포차
        if (lower.contains("실내포차")) {
            return "INDOOR_POCHA";
        }

        // 4. 칵테일바
        if (lower.contains("칵테일바")) {
            return "COCKTAIL_BAR";
        }


        // 스터디
        // 1. 도서관
        if (lower.contains("도서관")) {
            return "LIBRARY";
        }

        // 2. 스터디카페
        if (lower.contains("스터디카페") || lower.contains("study cafe") ||
                lower.contains("공부") || lower.contains("독서실") || lower.contains("스터디룸")) {
            return "STUDY_CAFE";
        }

        return null;
    }
}