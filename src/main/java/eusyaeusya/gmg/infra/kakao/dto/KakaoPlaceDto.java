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
     * 필터링(저장 제외) 대상 여부 확인
     * 패밀리레스토랑, 뷔페 등은 저장하지 않음
     */
    public boolean isExcluded() {
        if (categoryName != null) {
            String lower = categoryName.toLowerCase();
            if (lower.contains("뷔페") || lower.contains("패밀리레스토랑") || lower.contains("구내식당") || lower.contains("푸드코트")) {
                return true;
            }
        }
        if (placeName != null) {
            String lower = placeName.toLowerCase();
            if (lower.contains("뷔페") || lower.contains("패밀리레스토랑")) {
                return true;
            }
        }
        return false;
    }

    /**
     * PlaceType 추론
     */
    public String inferPlaceTypeCode() {
        // 1. categoryGroupCode가 있으면 우선 사용
        if (categoryGroupCode != null) {
            return switch (categoryGroupCode) {
                case "FD6" -> {
                    // 음식점인데 카페나 술집 관련 카테고리면 해당 타입으로 분류
                    if (isStudyRelated()) {
                        yield "STUDY";
                    }
                    if (isCafeRelated()) {
                        yield "CAFE";
                    }
                    if (isBarRelated()) {
                        yield "BAR";
                    }
                    yield "RESTAURANT";
                }
                case "CE7" -> "CAFE";
                default -> {
                    if (isStudyRelated()) yield "STUDY";
                    if (isCafeRelated()) yield "CAFE";
                    if (isBarRelated()) yield "BAR";
                    yield "RESTAURANT";
                }
            };
        }

        if (isStudyRelated()) return "STUDY";
        if (isCafeRelated()) return "CAFE";
        if (isBarRelated()) return "BAR";
        
        return "RESTAURANT";
    }

    private boolean isBarRelated() {
        String lowerCat = categoryName != null ? categoryName.toLowerCase() : "";
        String lowerName = placeName != null ? placeName.toLowerCase() : "";

        return lowerCat.contains("실내포장마차") || lowerCat.contains("호프") ||
                lowerCat.contains("요리주점") || lowerCat.contains("칵테일바") ||
                lowerCat.contains("일본식주점") || lowerCat.contains("이자카야") || lowerCat.contains("술집") ||
                lowerName.contains("이자카야") || lowerName.contains("술집") || lowerName.contains("호프");
    }

    private boolean isCafeRelated() {
        String lowerCat = categoryName != null ? categoryName.toLowerCase() : "";
        String lowerName = placeName != null ? placeName.toLowerCase() : "";

        return lowerCat.contains("카페") || lowerCat.contains("테마카페") || lowerCat.contains("커피전문점") ||
                lowerCat.contains("제과") || lowerCat.contains("베이커리") || lowerCat.contains("디저트") ||
                lowerName.contains("카페") || lowerName.contains("베이커리") || lowerName.contains("제과");
    }

    private boolean isStudyRelated() {
        String lowerCat = categoryName != null ? categoryName.toLowerCase() : "";
        String lowerName = placeName != null ? placeName.toLowerCase() : "";
        String combined = lowerCat + " " + lowerName;

        return combined.contains("독서실") || combined.contains("스터디") || combined.contains("study cafe") ||
                lowerCat.contains("열람실") || lowerCat.contains("도서관");
    }

    /**
     * PlaceCategory 코드 매핑
     * 우선순위: 구체적 카테고리 → 일반적 카테고리
     */
    public String mapToCategoryCode() {
        String category = mapCategoryByKeywords();
        if (category != null) {
            return category;
        }

        // Null Fallback 로직
        String inferredType = inferPlaceTypeCode();
        return switch (inferredType) {
            case "RESTAURANT" -> "KOREAN_FOOD"; // 무난한 한식 기본
            case "CAFE" -> "LOCAL_CAFE";        // 기본 개인카페
            case "BAR" -> "FOOD_BAR";           // 기본 요리주점
            case "STUDY" -> "STUDY_CAFE";       // 기본 스터디카페
            default -> null; // 이론상 도달하지 않음
        };
    }

    private String mapCategoryByKeywords() {
        if (categoryName == null && placeName == null) return null;

        String lowerCat = categoryName != null ? categoryName.toLowerCase() : "";
        String lowerName = placeName != null ? placeName.toLowerCase() : "";
        String combined = lowerCat + " " + lowerName;

        // 음식
        if (combined.contains("닭강정")) return "CHICKEN";
        
        if (lowerCat.contains("고기") || lowerCat.contains("육류")) return "MEAT";
        if (lowerCat.contains("치킨")) return "CHICKEN";
        if (lowerCat.contains("패스트푸드") || lowerCat.contains("햄버거")) return "FAST_FOOD";
        if (lowerCat.contains("분식")) return "SNACK_BAR";
        if (lowerCat.contains("한식") || lowerCat.contains("도시락") || lowerCat.contains("샤브샤브")) return "KOREAN_FOOD";
        if (lowerCat.contains("일식")) return "JAPANESE_FOOD";
        if (lowerCat.contains("중식")) return "CHINESE_FOOD";
        if (lowerCat.contains("양식") || lowerCat.contains("이탈리안") || lowerCat.contains("피자")) return "WESTERN_FOOD";
        if (lowerCat.contains("아시아음식") || lowerCat.contains("동남아음식") ||
            lowerCat.contains("베트남음식") || lowerCat.contains("인도음식") || lowerCat.contains("태국음식")) {
            return "ASIAN_FOOD";
        }

        // 스터디 (우선순위를 높여 스터디"카페"가 디저트카페 등으로 안 빠지도록)
        if (lowerCat.contains("도서관")) return "LIBRARY";
        if (combined.contains("스터디카페") || combined.contains("study cafe") || combined.contains("스터디룸") ||
            combined.contains("스터디") || combined.contains("독서실")) {
            return "STUDY_CAFE";
        }

        // 카페
        if (combined.contains("디저트") || combined.contains("제과") || combined.contains("베이커리")) {
            return "DESSERT_CAFE";
        }
        if (lowerCat.contains("커피전문")) return "FRANCHISE_CAFE";
        if (combined.contains("보드카페") || combined.contains("보드게임")) return "BOARDGAME_CAFE";
        if (categoryGroupCode != null && categoryGroupCode.equals("CE7")) return "LOCAL_CAFE";

        // 술집
        if (lowerCat.contains("요리주점")) return "FOOD_BAR";
        if (combined.contains("이자카야") || lowerCat.contains("일본식주점")) return "IZAKAYA"; // 이자카야, 일본식주점
        if (combined.contains("실내포차") || lowerCat.contains("실내포장마차")) return "INDOOR_POCHA"; // 실내포차, 포장마차
        if (lowerCat.contains("칵테일바") || lowerCat.contains("캌테일바")) return "COCKTAIL_BAR";

        return null;
    }
}